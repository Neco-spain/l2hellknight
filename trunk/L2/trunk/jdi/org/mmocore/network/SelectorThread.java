package org.mmocore.network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.Set;
import javolution.util.FastList;

public class SelectorThread<T extends MMOClient> extends Thread
{
  private Selector _selector;
  private final IPacketHandler<T> _packetHandler;
  private final IPacketHandler<T> _udpPacketHandler;
  private IMMOExecutor<T> _executor;
  private IClientFactory<T> _clientFactory;
  private IAcceptFilter _acceptFilter;
  private final UDPHeaderHandler<T> _udpHeaderHandler;
  private final TCPHeaderHandler<T> _tcpHeaderHandler;
  private boolean _shutdown;
  private NioNetStackList<MMOConnection<T>> _pendingClose = new NioNetStackList();
  private final int HELPER_BUFFER_SIZE;
  private final int HELPER_BUFFER_COUNT;
  private final int MAX_SEND_PER_PASS;
  private int HEADER_SIZE = 2;
  private final ByteOrder BYTE_ORDER;
  private final long SLEEP_TIME;
  private final ByteBuffer DIRECT_WRITE_BUFFER;
  private final ByteBuffer WRITE_BUFFER;
  private final ByteBuffer READ_BUFFER;
  private final FastList<ByteBuffer> _bufferPool = new FastList();

  public SelectorThread(SelectorConfig<T> sc, IMMOExecutor<T> executor, IClientFactory<T> clientFactory, IAcceptFilter acceptFilter) throws IOException
  {
    HELPER_BUFFER_SIZE = sc.getHelperBufferSize();
    HELPER_BUFFER_COUNT = sc.getHelperBufferCount();
    MAX_SEND_PER_PASS = sc.getMaxSendPerPass();
    BYTE_ORDER = sc.getByteOrder();
    SLEEP_TIME = sc.getSelectorSleepTime();

    DIRECT_WRITE_BUFFER = ByteBuffer.allocateDirect(sc.getWriteBufferSize()).order(BYTE_ORDER);
    WRITE_BUFFER = ByteBuffer.wrap(new byte[sc.getWriteBufferSize()]).order(BYTE_ORDER);
    READ_BUFFER = ByteBuffer.wrap(new byte[sc.getReadBufferSize()]).order(BYTE_ORDER);

    _udpHeaderHandler = sc.getUDPHeaderHandler();
    _tcpHeaderHandler = sc.getTCPHeaderHandler();
    initBufferPool();
    _acceptFilter = acceptFilter;
    _packetHandler = sc.getTCPPacketHandler();
    _udpPacketHandler = sc.getUDPPacketHandler();
    _clientFactory = clientFactory;
    setExecutor(executor);
    initializeSelector();
  }

  protected void initBufferPool()
  {
    for (int i = 0; i < HELPER_BUFFER_COUNT; i++)
    {
      getFreeBuffers().addLast(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER));
    }
  }

  public void openServerSocket(InetAddress address, int tcpPort) throws IOException
  {
    ServerSocketChannel selectable = ServerSocketChannel.open();
    selectable.configureBlocking(false);

    ServerSocket ss = selectable.socket();
    if (address == null)
    {
      ss.bind(new InetSocketAddress(tcpPort));
    }
    else
    {
      ss.bind(new InetSocketAddress(address, tcpPort));
    }
    selectable.register(getSelector(), 16);
  }

  public void openDatagramSocket(InetAddress address, int udpPort) throws IOException
  {
    DatagramChannel selectable = DatagramChannel.open();
    selectable.configureBlocking(false);

    DatagramSocket ss = selectable.socket();
    if (address == null)
    {
      ss.bind(new InetSocketAddress(udpPort));
    }
    else
    {
      ss.bind(new InetSocketAddress(address, udpPort));
    }
    selectable.register(getSelector(), 1);
  }

  protected void initializeSelector() throws IOException
  {
    setName(new StringBuilder().append("SelectorThread-").append(getId()).toString());
    setSelector(Selector.open());
  }

  protected ByteBuffer getPooledBuffer()
  {
    if (getFreeBuffers().isEmpty())
    {
      return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
    }

    return (ByteBuffer)getFreeBuffers().removeFirst();
  }

  public void recycleBuffer(ByteBuffer buf)
  {
    if (getFreeBuffers().size() < HELPER_BUFFER_COUNT)
    {
      buf.clear();
      getFreeBuffers().addLast(buf);
    }
  }

  public FastList<ByteBuffer> getFreeBuffers()
  {
    return _bufferPool;
  }

  public SelectionKey registerClientSocket(SelectableChannel sc, int interestOps) throws ClosedChannelException
  {
    SelectionKey sk = null;

    sk = sc.register(getSelector(), interestOps);

    return sk;
  }

  public void run()
  {
    int totalKeys = 0;
    while (true)
    {
      if (isShuttingDown())
      {
        closeSelectorThread();
        break;
      }

      try
      {
        totalKeys = getSelector().selectNow();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      if (totalKeys > 0)
      {
        Set keys = getSelector().selectedKeys();
        Iterator iter = keys.iterator();

        while (iter.hasNext())
        {
          SelectionKey key = (SelectionKey)iter.next();
          iter.remove();

          switch (key.readyOps())
          {
          case 8:
            finishConnection(key);
            break;
          case 16:
            acceptConnection(key);
            break;
          case 1:
            readPacket(key);
            break;
          case 4:
            writePacket2(key);
            break;
          case 5:
            writePacket2(key);

            if (!key.isValid())
              break;
            readPacket(key);
          }

        }

      }

      synchronized (getPendingClose())
      {
        while (!getPendingClose().isEmpty())
        {
          MMOConnection con = (MMOConnection)getPendingClose().removeFirst();
          writeClosePacket(con);
          closeConnectionImpl(con);
        }
      }

      try
      {
        Thread.sleep(SLEEP_TIME);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
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

    if (key.isValid())
    {
      key.interestOps(key.interestOps() | 0x1);
      key.interestOps(key.interestOps() & 0xFFFFFFF7);
    }
  }

  protected void acceptConnection(SelectionKey key)
  {
    try
    {
      SocketChannel sc;
      while ((sc = ((ServerSocketChannel)key.channel()).accept()) != null)
      {
        if ((getAcceptFilter() == null) || (getAcceptFilter().accept(sc)))
        {
          sc.configureBlocking(false);
          SelectionKey clientKey = sc.register(getSelector(), 1);

          MMOConnection con = new MMOConnection(this, new TCPSocket(sc.socket()), clientKey);
          MMOClient client = getClientFactory().create(con);
          clientKey.attach(con);
          continue;
        }

        sc.socket().close();
      }

    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected void readPacket(SelectionKey key)
  {
    if ((key.channel() instanceof SocketChannel))
    {
      readTCPPacket(key);
    }
    else
    {
      readUDPPacket(key);
    }
  }

  protected void readTCPPacket(SelectionKey key)
  {
    MMOConnection con = (MMOConnection)key.attachment();
    MMOClient client = con.getClient();
    ByteBuffer buf;
    if ((buf = con.getReadBuffer()) == null)
    {
      buf = READ_BUFFER;
    }
    int result = -2;

    if (buf.position() == buf.limit())
    {
      System.err.println(new StringBuilder().append("POS ANTES SC.READ(): ").append(buf.position()).append(" limit: ").append(buf.limit()).toString());
      System.err.println(new StringBuilder().append("NOOBISH ERROR ").append(buf == READ_BUFFER ? "READ_BUFFER" : "temp").toString());
      System.exit(0);
    }

    try
    {
      result = con.getReadableByteChannel().read(buf);
    }
    catch (IOException e)
    {
    }

    if (result > 0)
    {
      if (!con.isClosed())
      {
        buf.flip();

        while (tryReadPacket2(key, client, buf));
      }
      if (buf == READ_BUFFER)
      {
        READ_BUFFER.clear();
      }

    }
    else if (result == 0)
    {
      System.out.println(new StringBuilder().append("Read interest but nothing to read? wtf? IP: ").append(con.getSocket().getInetAddress().getHostAddress()).toString());
      con.onForcedDisconnection();
      closeConnectionImpl(con);
    }
    else if (result == -1)
    {
      closeConnectionImpl(con);
    }
    else
    {
      con.onForcedDisconnection();
      closeConnectionImpl(con);
    }
  }

  protected void readUDPPacket(SelectionKey key)
  {
    int result = -2;
    ByteBuffer buf = READ_BUFFER;

    DatagramChannel dc = (DatagramChannel)key.channel();
    if (!dc.isConnected())
    {
      try
      {
        dc.configureBlocking(false);
        SocketAddress address = dc.receive(buf);
        buf.flip();
        _udpHeaderHandler.onUDPConnection(this, dc, address, buf);
      }
      catch (IOException e)
      {
      }

      buf.clear();
    }
    else
    {
      try
      {
        result = dc.read(buf);
      }
      catch (IOException e)
      {
      }

      if (result > 0) {
        buf.flip();

        while (tryReadUDPPacket(key, buf));
      }if (result == 0)
      {
        System.out.println("CRITICAL ERROR ON SELECTOR");
        System.exit(0);
      }
    }
  }

  protected boolean tryReadPacket2(SelectionKey key, T client, ByteBuffer buf)
  {
    MMOConnection con = client.getConnection();

    if (buf.hasRemaining())
    {
      TCPHeaderHandler handler = _tcpHeaderHandler;

      while (!handler.isChildHeaderHandler())
      {
        handler.handleHeader(key, buf);
        handler = (TCPHeaderHandler)handler.getSubHeaderHandler();
      }

      HeaderInfo ret = handler.handleHeader(key, buf);

      if (ret != null)
      {
        int result = buf.remaining();

        if (ret.headerFinished())
        {
          int size = ret.getDataPending();

          if (size <= result)
          {
            if (size > 0)
            {
              int pos = buf.position();
              parseClientPacket(getPacketHandler(), buf, size, client);
              buf.position(pos + size);
            }

            if (!buf.hasRemaining())
            {
              if (buf != READ_BUFFER)
              {
                con.setReadBuffer(null);
                recycleBuffer(buf);
              }
              else
              {
                READ_BUFFER.clear();
              }

              return false;
            }

            return true;
          }

          client.getConnection().enableReadInterest();

          if (buf == READ_BUFFER)
          {
            buf.position(buf.position() - HEADER_SIZE);
            allocateReadBuffer(con);
          }
          else
          {
            buf.position(buf.position() - HEADER_SIZE);
            buf.compact();
          }
          return false;
        }

        client.getConnection().enableReadInterest();

        if (buf == READ_BUFFER)
        {
          allocateReadBuffer(con);
        }
        else
        {
          buf.compact();
        }
        return false;
      }

      closeConnectionImpl(con);
      return false;
    }

    return false;
  }

  protected boolean tryReadUDPPacket(SelectionKey key, ByteBuffer buf)
  {
    if (buf.hasRemaining())
    {
      UDPHeaderHandler handler = _udpHeaderHandler;

      while (!handler.isChildHeaderHandler())
      {
        handler.handleHeader(buf);
        handler = (UDPHeaderHandler)handler.getSubHeaderHandler();
      }

      HeaderInfo ret = handler.handleHeader(buf);

      if (ret != null)
      {
        int result = buf.remaining();

        if (ret.headerFinished())
        {
          MMOClient client = (MMOClient)ret.getClient();
          MMOConnection con = client.getConnection();

          int size = ret.getDataPending();

          if (size <= result)
          {
            if (ret.isMultiPacket())
            {
              while (buf.hasRemaining())
              {
                parseClientPacket(_udpPacketHandler, buf, buf.remaining(), client);
              }

            }

            if (size > 0)
            {
              int pos = buf.position();
              parseClientPacket(_udpPacketHandler, buf, size, client);
              buf.position(pos + size);
            }

            if (!buf.hasRemaining())
            {
              if (buf != READ_BUFFER)
              {
                con.setReadBuffer(null);
                recycleBuffer(buf);
              }
              else
              {
                READ_BUFFER.clear();
              }

              return false;
            }

            return true;
          }

          client.getConnection().enableReadInterest();

          if (buf == READ_BUFFER)
          {
            buf.position(buf.position() - HEADER_SIZE);
            allocateReadBuffer(con);
          }
          else
          {
            buf.position(buf.position() - HEADER_SIZE);
            buf.compact();
          }
          return false;
        }

        buf.clear();
        return false;
      }

      buf.clear();
      return false;
    }

    buf.clear();
    return false;
  }

  protected void allocateReadBuffer(MMOConnection<T> con)
  {
    con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
    READ_BUFFER.clear();
  }

  protected void parseClientPacket(IPacketHandler<T> handler, ByteBuffer buf, int dataSize, T client)
  {
    int pos = buf.position();

    boolean ret = client.decrypt(buf, dataSize);

    if ((buf.hasRemaining()) && (ret))
    {
      int limit = buf.limit();
      buf.limit(pos + dataSize);

      ReceivablePacket cp = handler.handlePacket(buf, client);

      if (cp != null)
      {
        cp.setByteBuffer(buf);
        cp.setClient(client);

        if (cp.read())
        {
          getExecutor().execute(cp);
        }
      }
      buf.limit(limit);
    }
  }

  protected void prepareWriteBuffer(T client, SendablePacket<T> sp)
  {
    WRITE_BUFFER.clear();

    sp.setByteBuffer(WRITE_BUFFER);

    int headerPos = sp.getByteBuffer().position();
    int headerSize = sp.getHeaderSize();
    sp.getByteBuffer().position(headerPos + headerSize);

    sp.write();

    int dataSize = sp.getByteBuffer().position() - headerPos - headerSize;
    sp.getByteBuffer().position(headerPos + headerSize);
    client.encrypt(sp.getByteBuffer(), dataSize);

    sp.writeHeader(dataSize);
  }

  protected void writePacket2(SelectionKey key)
  {
    MMOConnection con = (MMOConnection)key.attachment();
    MMOClient client = con.getClient();

    prepareWriteBuffer2(con);
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
      if (result == size)
      {
        synchronized (con.getSendQueue())
        {
          if ((con.getSendQueue().isEmpty()) && (!con.hasPendingWriteBuffer()))
          {
            con.disableWriteInterest();
          }
        }
      }
      else
      {
        con.createWriteBuffer(DIRECT_WRITE_BUFFER);
      }

      if (result != 0);
    }
    else
    {
      con.onForcedDisconnection();
      closeConnectionImpl(con);
    }
  }

  protected void prepareWriteBuffer2(MMOConnection<T> con)
  {
    DIRECT_WRITE_BUFFER.clear();

    if (con.hasPendingWriteBuffer())
    {
      con.movePendingWriteBufferTo(DIRECT_WRITE_BUFFER);
    }

    if ((DIRECT_WRITE_BUFFER.remaining() > 1) && (!con.hasPendingWriteBuffer()))
    {
      NioNetStackList sendQueue = con.getSendQueue();
      MMOClient client = con.getClient();

      for (int i = 0; i < MAX_SEND_PER_PASS; i++)
      {
        SendablePacket sp;
        synchronized (con.getSendQueue())
        {
          SendablePacket sp;
          if (sendQueue.isEmpty())
            sp = null;
          else {
            sp = (SendablePacket)sendQueue.removeFirst();
          }
        }
        if (sp == null)
        {
          break;
        }
        putPacketIntoWriteBuffer(client, sp);

        WRITE_BUFFER.flip();

        if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit()) {
          DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
        }
        else {
          con.createWriteBuffer(WRITE_BUFFER);
          break;
        }
      }
    }
  }

  protected final void putPacketIntoWriteBuffer(T client, SendablePacket<T> sp)
  {
    WRITE_BUFFER.clear();

    sp.setByteBuffer(WRITE_BUFFER);

    int headerPos = sp.getByteBuffer().position();
    int headerSize = sp.getHeaderSize();
    sp.getByteBuffer().position(headerPos + headerSize);

    sp.write();

    int dataSize = sp.getByteBuffer().position() - headerPos - headerSize;
    sp.getByteBuffer().position(headerPos + headerSize);
    client.encrypt(sp.getByteBuffer(), dataSize);

    dataSize = sp.getByteBuffer().position() - headerPos - headerSize;

    sp.getByteBuffer().position(headerPos);
    sp.writeHeader(dataSize);
    sp.getByteBuffer().position(headerPos + headerSize + dataSize);
  }

  protected void setSelector(Selector selector)
  {
    _selector = selector;
  }

  public Selector getSelector()
  {
    return _selector;
  }

  protected void setExecutor(IMMOExecutor<T> executor)
  {
    _executor = executor;
  }

  protected IMMOExecutor<T> getExecutor()
  {
    return _executor;
  }

  public IPacketHandler<T> getPacketHandler()
  {
    return _packetHandler;
  }

  protected void setClientFactory(IClientFactory<T> clientFactory)
  {
    _clientFactory = clientFactory;
  }

  public IClientFactory<T> getClientFactory()
  {
    return _clientFactory;
  }

  public void setAcceptFilter(IAcceptFilter acceptFilter)
  {
    _acceptFilter = acceptFilter;
  }

  public IAcceptFilter getAcceptFilter()
  {
    return _acceptFilter;
  }

  public void closeConnection(MMOConnection<T> con)
  {
    synchronized (getPendingClose())
    {
      getPendingClose().addLast(con);
    }
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
        con.getSocket().close();
      }
      catch (IOException e)
      {
      }
      finally
      {
        con.releaseBuffers();

        con.getSelectionKey().attach(null);

        con.getSelectionKey().cancel();
      }
    }
  }

  protected NioNetStackList<MMOConnection<T>> getPendingClose()
  {
    return _pendingClose;
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
    for (Iterator iter = keys.iterator(); iter.hasNext(); )
    {
      SelectionKey key = (SelectionKey)iter.next();
      try
      {
        key.channel().close();
      }
      catch (IOException e)
      {
      }
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

  private final void writeClosePacket(MMOConnection<T> con)
  {
    synchronized (con.getSendQueue())
    {
      if (con.getSendQueue().isEmpty())
        return;
      SendablePacket sp;
      while ((sp = (SendablePacket)con.getSendQueue().removeFirst()) != null)
      {
        WRITE_BUFFER.clear();

        putPacketIntoWriteBuffer(con.getClient(), sp);

        WRITE_BUFFER.flip();
        try
        {
          con.write(WRITE_BUFFER);
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }
}