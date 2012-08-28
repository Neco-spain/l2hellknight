package l2m.commons.net.nio.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectorThread<T extends MMOClient> extends Thread
{
  private static final Logger _log = LoggerFactory.getLogger(SelectorThread.class);

  private final Selector _selector = Selector.open();
  private final IPacketHandler<T> _packetHandler;
  private final IMMOExecutor<T> _executor;
  private final IClientFactory<T> _clientFactory;
  private IAcceptFilter _acceptFilter;
  private boolean _shutdown;
  private final SelectorConfig _sc;
  private final int HELPER_BUFFER_SIZE;
  private ByteBuffer DIRECT_WRITE_BUFFER;
  private final ByteBuffer WRITE_BUFFER;
  private final ByteBuffer READ_BUFFER;
  private T WRITE_CLIENT;
  private final Queue<ByteBuffer> _bufferPool;
  private final List<MMOConnection<T>> _connections;
  private static final List<SelectorThread> ALL_SELECTORS = new ArrayList();
  private static SelectorStats stats = new SelectorStats();

  public SelectorThread(SelectorConfig sc, IPacketHandler<T> packetHandler, IMMOExecutor<T> executor, IClientFactory<T> clientFactory, IAcceptFilter acceptFilter) throws IOException
  {
    synchronized (ALL_SELECTORS)
    {
      ALL_SELECTORS.add(this);
    }

    _sc = sc;
    _acceptFilter = acceptFilter;
    _packetHandler = packetHandler;
    _clientFactory = clientFactory;
    _executor = executor;

    _bufferPool = new ArrayDeque(_sc.HELPER_BUFFER_COUNT);
    _connections = new CopyOnWriteArrayList();

    DIRECT_WRITE_BUFFER = ByteBuffer.wrap(new byte[_sc.WRITE_BUFFER_SIZE]).order(_sc.BYTE_ORDER);
    WRITE_BUFFER = ByteBuffer.wrap(new byte[_sc.WRITE_BUFFER_SIZE]).order(_sc.BYTE_ORDER);
    READ_BUFFER = ByteBuffer.wrap(new byte[_sc.READ_BUFFER_SIZE]).order(_sc.BYTE_ORDER);
    HELPER_BUFFER_SIZE = Math.max(_sc.READ_BUFFER_SIZE, _sc.WRITE_BUFFER_SIZE);

    for (int i = 0; i < _sc.HELPER_BUFFER_COUNT; i++)
      _bufferPool.add(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(_sc.BYTE_ORDER));
  }

  public void openServerSocket(InetAddress address, int tcpPort) throws IOException
  {
    ServerSocketChannel selectable = ServerSocketChannel.open();
    selectable.configureBlocking(false);

    selectable.socket().bind(address == null ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort));
    selectable.register(getSelector(), selectable.validOps());
    setName(new StringBuilder().append("SelectorThread:").append(selectable.socket().getLocalPort()).toString());
  }

  protected ByteBuffer getPooledBuffer()
  {
    if (_bufferPool.isEmpty())
      return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(_sc.BYTE_ORDER);
    return (ByteBuffer)_bufferPool.poll();
  }

  protected void recycleBuffer(ByteBuffer buf)
  {
    if (_bufferPool.size() < _sc.HELPER_BUFFER_COUNT)
    {
      buf.clear();
      _bufferPool.add(buf);
    }
  }

  protected void freeBuffer(ByteBuffer buf, MMOConnection<T> con)
  {
    if (buf == READ_BUFFER) {
      READ_BUFFER.clear();
    }
    else {
      con.setReadBuffer(null);
      recycleBuffer(buf);
    }
  }

  public void run()
  {
    int totalKeys = 0;
    Set keys = null;
    Iterator itr = null;
    Iterator conItr = null;
    SelectionKey key = null;
    MMOConnection con = null;
    long currentMillis = 0L;
    while (true)
    {
      try
      {
        if (!isShuttingDown())
          continue;
        closeSelectorThread();
        break;

        currentMillis = System.currentTimeMillis();

        conItr = _connections.iterator();
        if (!conItr.hasNext())
          continue;
        con = (MMOConnection)conItr.next();
        if ((!con.isPengingClose()) || (
          (con.isPendingWrite()) && (currentMillis - con.getPendingCloseTime() < 10000L)))
          continue;
        closeConnectionImpl(con);
        continue;

        if ((!con.isPendingWrite()) || 
          (currentMillis - con.getPendingWriteTime() < _sc.INTEREST_DELAY)) continue;
        con.enableWriteInterest(); continue;

        totalKeys = getSelector().selectNow();

        if (totalKeys <= 0)
          continue;
        keys = getSelector().selectedKeys();
        itr = keys.iterator();

        if (!itr.hasNext())
          continue;
        key = (SelectionKey)itr.next();
        itr.remove();

        if (!key.isValid())
          continue;
        try {
          if (!key.isAcceptable())
            continue;
          acceptConnection(key);
          continue;

          if (!key.isConnectable())
            continue;
          finishConnection(key);
          continue;

          if (!key.isReadable())
            continue;
          if (key == null)
            continue;
          readPacket(key);

          if ((!key.isValid()) || 
            (!key.isWritable())) continue;
          writePacket(key);
        }
        catch (CancelledKeyException cke)
        {
        }
        continue;
        try
        {
          Thread.sleep(_sc.SLEEP_TIME);
        }
        catch (InterruptedException ie)
        {
        }

        continue;
      }
      catch (IOException e)
      {
        _log.error(new StringBuilder().append("Error in ").append(getName()).toString(), e);
        try
        {
          Thread.sleep(1000L);
        }
        catch (InterruptedException ie)
        {
        }
      }
    }
  }

  protected void finishConnection(SelectionKey key)
  {
    try
    {
      ((SocketChannel)key.channel()).finishConnect();
    }
    catch (IOException e)
    {
      MMOConnection con = (MMOConnection)key.attachment();
      MMOClient client = con.getClient();
      client.getConnection().onForcedDisconnection();
      closeConnectionImpl(client.getConnection());
    }
  }

  protected void acceptConnection(SelectionKey key)
  {
    ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
    try
    {
      SocketChannel sc;
      while ((sc = ssc.accept()) != null) {
        if ((getAcceptFilter() == null) || (getAcceptFilter().accept(sc)))
        {
          sc.configureBlocking(false);
          SelectionKey clientKey = sc.register(getSelector(), 1);

          MMOConnection con = new MMOConnection(this, sc.socket(), clientKey);
          MMOClient client = getClientFactory().create(con);
          client.setConnection(con);
          con.setClient(client);
          clientKey.attach(con);

          _connections.add(con);
          stats.increaseOpenedConnections();
          continue;
        }

        sc.close();
      }
    }
    catch (IOException e)
    {
      _log.error(new StringBuilder().append("Error in ").append(getName()).toString(), e);
    }
  }

  protected void readPacket(SelectionKey key)
  {
    MMOConnection con = (MMOConnection)key.attachment();

    if (con.isClosed()) {
      return;
    }

    int result = -2;
    ByteBuffer buf;
    if ((buf = con.getReadBuffer()) == null) {
      buf = READ_BUFFER;
    }

    if ((buf == null) && (buf.position() == buf.limit()))
    {
      _log.error(new StringBuilder().append("Read buffer exhausted for client : ").append(con.getClient()).append(", try to adjust buffer size, current : ").append(buf.capacity()).append(", primary : ").append(buf == READ_BUFFER).append(". Closing connection.").toString());
      closeConnectionImpl(con);
    }
    else
    {
      try
      {
        result = con.getReadableByteChannel().read(buf);
      }
      catch (IOException e)
      {
      }

      if ((result > 0) && (key != null) && (buf != null))
      {
        buf.flip();

        stats.increaseIncomingBytes(result);

        while (tryReadPacket2(key, con, buf));
      }
      else if (result == 0) {
        closeConnectionImpl(con);
      } else if (result == -1) {
        closeConnectionImpl(con);
      }
      else {
        con.onForcedDisconnection();
        closeConnectionImpl(con);
      }
    }

    if (buf == READ_BUFFER)
      buf.clear();
  }

  protected boolean tryReadPacket2(SelectionKey key, MMOConnection<T> con, ByteBuffer buf)
  {
    if ((con.isClosed()) || (buf == null)) {
      return false;
    }
    int pos = buf.position();

    if (buf.remaining() > _sc.HEADER_SIZE)
    {
      int size = buf.getShort() & 0xFFFF;

      if ((size <= _sc.HEADER_SIZE) || (size > _sc.PACKET_SIZE))
      {
        _log.error(new StringBuilder().append("Incorrect packet size : ").append(size).append("! Client : ").append(con.getClient()).append(". Closing connection.").toString());
        closeConnectionImpl(con);
        return false;
      }

      size -= _sc.HEADER_SIZE;

      if (size <= buf.remaining())
      {
        stats.increaseIncomingPacketsCount();
        parseClientPacket(getPacketHandler(), buf, size, con);
        buf.position(pos + size + _sc.HEADER_SIZE);

        if (!buf.hasRemaining())
        {
          freeBuffer(buf, con);
          return false;
        }

        return true;
      }

      buf.position(pos);
    }

    if (pos == buf.capacity()) {
      _log.warn(new StringBuilder().append("Read buffer exhausted for client : ").append(con.getClient()).append(", try to adjust buffer size, current : ").append(buf.capacity()).append(", primary : ").append(buf == READ_BUFFER).append(".").toString());
    }

    if (buf == READ_BUFFER)
      allocateReadBuffer(con);
    else {
      buf.compact();
    }
    return false;
  }

  protected void allocateReadBuffer(MMOConnection<T> con)
  {
    con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
    READ_BUFFER.clear();
  }

  protected boolean parseClientPacket(IPacketHandler<T> handler, ByteBuffer buf, int dataSize, MMOConnection<T> con)
  {
    MMOClient client = con.getClient();

    int pos = buf.position();

    client.decrypt(buf, dataSize);
    buf.position(pos);

    if (buf.hasRemaining())
    {
      int limit = buf.limit();
      buf.limit(pos + dataSize);
      ReceivablePacket rp = handler.handlePacket(buf, client);

      if (rp != null)
      {
        rp.setByteBuffer(buf);
        rp.setClient(client);

        if (rp.read()) {
          con.recvPacket(rp);
        }
        rp.setByteBuffer(null);
      }
      buf.limit(limit);
    }
    return true;
  }

  protected void writePacket(SelectionKey key)
  {
    MMOConnection con = (MMOConnection)key.attachment();

    prepareWriteBuffer(con);

    DIRECT_WRITE_BUFFER.flip();
    int size = DIRECT_WRITE_BUFFER.remaining();

    int result = -1;
    try
    {
      result = con.getWritableChannel().write(DIRECT_WRITE_BUFFER);
    }
    catch (IOException e)
    {
    }

    if (result >= 0)
    {
      stats.increaseOutgoingBytes(result);

      if (result != size) {
        con.createWriteBuffer(DIRECT_WRITE_BUFFER);
      }
      if ((!con.getSendQueue().isEmpty()) || (con.hasPendingWriteBuffer()))
      {
        con.scheduleWriteInterest();
      }
    }
    else {
      con.onForcedDisconnection();
      closeConnectionImpl(con);
    }
  }

  protected T getWriteClient()
  {
    return WRITE_CLIENT;
  }

  protected ByteBuffer getWriteBuffer()
  {
    return WRITE_BUFFER;
  }

  protected void prepareWriteBuffer(MMOConnection<T> con)
  {
    WRITE_CLIENT = con.getClient();
    DIRECT_WRITE_BUFFER.clear();

    if (con.hasPendingWriteBuffer()) {
      con.movePendingWriteBufferTo(DIRECT_WRITE_BUFFER);
    }
    if ((DIRECT_WRITE_BUFFER.hasRemaining()) && (!con.hasPendingWriteBuffer()))
    {
      Queue sendQueue = con.getSendQueue();

      for (int i = 0; i < _sc.MAX_SEND_PER_PASS; i++)
      {
        SendablePacket sp;
        synchronized (con)
        {
          if ((sp = (SendablePacket)sendQueue.poll()) == null) {
            break;
          }
        }
        try
        {
          stats.increaseOutgoingPacketsCount();
          putPacketIntoWriteBuffer(sp, true);
          WRITE_BUFFER.flip();
          if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit()) {
            DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
          }
          else
          {
            con.createWriteBuffer(WRITE_BUFFER);
            break;
          }
        }
        catch (Exception e)
        {
          _log.error(new StringBuilder().append("Error in ").append(getName()).toString(), e);
          break;
        }
      }
    }

    WRITE_BUFFER.clear();
    WRITE_CLIENT = null;
  }

  protected final void putPacketIntoWriteBuffer(SendablePacket<T> sp, boolean encrypt)
  {
    WRITE_BUFFER.clear();

    int headerPos = WRITE_BUFFER.position();
    WRITE_BUFFER.position(headerPos + _sc.HEADER_SIZE);

    sp.write();

    int dataSize = WRITE_BUFFER.position() - headerPos - _sc.HEADER_SIZE;
    if (dataSize == 0)
    {
      WRITE_BUFFER.position(headerPos);
      return;
    }
    WRITE_BUFFER.position(headerPos + _sc.HEADER_SIZE);
    if (encrypt)
    {
      WRITE_CLIENT.encrypt(WRITE_BUFFER, dataSize);

      dataSize = WRITE_BUFFER.position() - headerPos - _sc.HEADER_SIZE;
    }

    WRITE_BUFFER.position(headerPos);
    WRITE_BUFFER.putShort((short)(_sc.HEADER_SIZE + dataSize));
    WRITE_BUFFER.position(headerPos + _sc.HEADER_SIZE + dataSize);
  }

  protected SelectorConfig getConfig()
  {
    return _sc;
  }

  protected Selector getSelector()
  {
    return _selector;
  }

  protected IMMOExecutor<T> getExecutor()
  {
    return _executor;
  }

  protected IPacketHandler<T> getPacketHandler()
  {
    return _packetHandler;
  }

  protected IClientFactory<T> getClientFactory()
  {
    return _clientFactory;
  }

  public void setAcceptFilter(IAcceptFilter acceptFilter)
  {
    _acceptFilter = acceptFilter;
  }

  protected IAcceptFilter getAcceptFilter()
  {
    return _acceptFilter;
  }

  protected void closeConnectionImpl(MMOConnection<T> con)
  {
    try
    {
      con.onDisconnection();
    }
    finally
    {
      try
      {
        con.close();
      }
      catch (IOException e)
      {
      }
      finally
      {
        con.releaseBuffers();

        con.clearQueues();

        con.getClient().setConnection(null);

        con.getSelectionKey().attach(null);

        con.getSelectionKey().cancel();

        _connections.remove(con);

        stats.decreseOpenedConnections();
      }
    }
  }

  public void shutdown()
  {
    _shutdown = true;
  }

  public boolean isShuttingDown()
  {
    return _shutdown;
  }

  protected void closeAllChannels()
  {
    Set keys = getSelector().keys();
    for (SelectionKey key : keys)
      try
      {
        key.channel().close();
      }
      catch (IOException e)
      {
      }
  }

  protected void closeSelectorThread()
  {
    closeAllChannels();
    try
    {
      getSelector().close();
    }
    catch (IOException e)
    {
    }
  }

  public static CharSequence getStats()
  {
    StringBuilder list = new StringBuilder();

    list.append("selectorThreadCount: .... ").append(ALL_SELECTORS.size()).append("\n");
    list.append("=================================================\n");
    list.append("getTotalConnections: .... ").append(stats.getTotalConnections()).append("\n");
    list.append("getCurrentConnections: .. ").append(stats.getCurrentConnections()).append("\n");
    list.append("getMaximumConnections: .. ").append(stats.getMaximumConnections()).append("\n");
    list.append("getIncomingBytesTotal: .. ").append(stats.getIncomingBytesTotal()).append("\n");
    list.append("getOutgoingBytesTotal: .. ").append(stats.getOutgoingBytesTotal()).append("\n");
    list.append("getIncomingPacketsTotal:  ").append(stats.getIncomingPacketsTotal()).append("\n");
    list.append("getOutgoingPacketsTotal:  ").append(stats.getOutgoingPacketsTotal()).append("\n");
    list.append("getMaxBytesPerRead: ..... ").append(stats.getMaxBytesPerRead()).append("\n");
    list.append("getMaxBytesPerWrite: .... ").append(stats.getMaxBytesPerWrite()).append("\n");
    list.append("=================================================\n");

    return list;
  }
}