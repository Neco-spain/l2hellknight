package l2m.gameserver.loginservercon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.loginservercon.gspackets.AuthRequest;
import l2m.gameserver.network.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginServerCommunication extends Thread
{
  private static final Logger _log = LoggerFactory.getLogger(LoginServerCommunication.class);

  private static final LoginServerCommunication instance = new LoginServerCommunication();

  private final Map<String, GameClient> waitingClients = new HashMap();
  private final Map<String, GameClient> authedClients = new HashMap();

  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  private final Lock readLock = lock.readLock();
  private final Lock writeLock = lock.writeLock();

  private final ByteBuffer readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
  private final ByteBuffer writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);

  private final Queue<SendablePacket> sendQueue = new ArrayDeque();
  private final Lock sendLock = new ReentrantLock();

  private final AtomicBoolean isPengingWrite = new AtomicBoolean();
  private SelectionKey key;
  private Selector selector;
  private boolean shutdown;
  private boolean restart;

  public static final LoginServerCommunication getInstance()
  {
    return instance;
  }

  private LoginServerCommunication()
  {
    try
    {
      selector = Selector.open();
    }
    catch (IOException e)
    {
      _log.error("", e);
    }
  }

  private void connect() throws IOException
  {
    _log.info("Connecting to loginserver on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);

    SocketChannel channel = SocketChannel.open();
    channel.configureBlocking(false);

    key = channel.register(selector, 8);
    channel.connect(new InetSocketAddress(Config.GAME_SERVER_LOGIN_HOST, Config.GAME_SERVER_LOGIN_PORT));
  }

  public void sendPacket(SendablePacket packet)
  {
    if (isShutdown()) {
      return;
    }
sendLock.lock();
    boolean wakeUp;
    try {
      sendQueue.add(packet);
      wakeUp = enableWriteInterest();
    }
    catch (CancelledKeyException e)
    {
      return;
    }
    finally {
      sendLock.unlock();
    }

    if (wakeUp)
      selector.wakeup();
  }

  private boolean disableWriteInterest() throws CancelledKeyException
  {
    if (isPengingWrite.compareAndSet(true, false))
    {
      key.interestOps(key.interestOps() & 0xFFFFFFFB);
      return true;
    }
    return false;
  }

  private boolean enableWriteInterest() throws CancelledKeyException
  {
    if (!isPengingWrite.getAndSet(true))
    {
      key.interestOps(key.interestOps() | 0x4);
      return true;
    }
    return false;
  }

  protected ByteBuffer getReadBuffer()
  {
    return readBuffer;
  }

  protected ByteBuffer getWriteBuffer()
  {
    return writeBuffer;
  }

  public void run()
  {
    while (!shutdown)
    {
      restart = false;
      try
      {
        while (true) {
          if (!isShutdown())
          {
            connect();

            selector.select(5000L);
            Set keys = selector.selectedKeys();
            if (keys.isEmpty()) {
              throw new IOException("Connection timeout.");
            }
            Iterator iterator = keys.iterator();
            try
            {
              while (iterator.hasNext())
              {
                SelectionKey key = (SelectionKey)iterator.next();
                iterator.remove();

                int opts = key.readyOps();

                switch (opts)
                {
                case 8:
                  connect(key);
                  break label139;
                }
              }
            }
            catch (CancelledKeyException e)
            {
            }
          }
        }
        while (true)
        {
          label139: if (!isShutdown())
          {
            selector.select();
            Set keys = selector.selectedKeys();
            Iterator iterator = keys.iterator();
            try
            {
              while (iterator.hasNext())
              {
                SelectionKey key = (SelectionKey)iterator.next();
                iterator.remove();

                int opts = key.readyOps();

                switch (opts)
                {
                case 4:
                  write(key);
                  break;
                case 1:
                  read(key);
                  break;
                case 5:
                  write(key);
                  read(key);
                case 2:
                case 3:
                }
              }
            }
            catch (CancelledKeyException e)
            {
            }
          }
        }
      }
      catch (IOException e)
      {
        _log.error("LoginServer I/O error: " + e.getMessage());
      }

      close();
      try
      {
        Thread.sleep(5000L);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  private void read(SelectionKey key)
    throws IOException
  {
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buf = getReadBuffer();

    int count = channel.read(buf);

    if (count == -1) {
      throw new IOException("End of stream.");
    }
    if (count == 0) {
      return;
    }buf.flip();

    while (tryReadPacket(key, buf));
  }

  private boolean tryReadPacket(SelectionKey key, ByteBuffer buf) throws IOException {
    int pos = buf.position();

    if (buf.remaining() > 2)
    {
      int size = buf.getShort() & 0xFFFF;

      if (size <= 2) throw new IOException("Incorrect packet size: <= 2");

      size -= 2;

      if (size <= buf.remaining())
      {
        int limit = buf.limit();
        buf.limit(pos + size + 2);

        ReceivablePacket rp = PacketHandler.handlePacket(buf);

        if (rp != null)
        {
          if (rp.read()) {
            ThreadPoolManager.getInstance().execute(rp);
          }
        }
        buf.limit(limit);
        buf.position(pos + size + 2);

        if (!buf.hasRemaining())
        {
          buf.clear();
          return false;
        }

        return true;
      }

      buf.position(pos);
    }

    buf.compact();

    return false;
  }
  private void write(SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel)key.channel();
    ByteBuffer buf = getWriteBuffer();

    sendLock.lock();
    boolean done;
    try {
      int i = 0;
      SendablePacket sp;
      while ((i++ < 64) && ((sp = (SendablePacket)sendQueue.poll()) != null))
      {
        int headerPos = buf.position();
        buf.position(headerPos + 2);

        sp.write();

        int dataSize = buf.position() - headerPos - 2;
        if (dataSize == 0)
        {
          buf.position(headerPos);
          continue;
        }

        buf.position(headerPos);
        buf.putShort((short)(dataSize + 2));
        buf.position(headerPos + dataSize + 2);
      }

      done = sendQueue.isEmpty();
      if (done)
        disableWriteInterest();
    }
    finally
    {
      sendLock.unlock();
    }
    buf.flip();

    channel.write(buf);

    if (buf.remaining() > 0)
    {
      buf.compact();
      done = false;
    }
    else
    {
      buf.clear();
    }

    if (!done)
    {
      if (enableWriteInterest())
        selector.wakeup();
    }
  }

  private void connect(SelectionKey key) throws IOException
  {
    SocketChannel channel = (SocketChannel)key.channel();
    channel.finishConnect();

    key.interestOps(key.interestOps() & 0xFFFFFFF7);
    key.interestOps(key.interestOps() | 0x1);

    sendPacket(new AuthRequest());
  }

  private void close()
  {
    restart = (!shutdown);

    sendLock.lock();
    try
    {
      sendQueue.clear();
    }
    finally
    {
      sendLock.unlock();
    }

    readBuffer.clear();
    writeBuffer.clear();

    isPengingWrite.set(false);
    try
    {
      if (key != null)
      {
        key.channel().close();
        key.cancel();
      }
    }
    catch (IOException e)
    {
    }
    writeLock.lock();
    try
    {
      waitingClients.clear();
    }
    finally
    {
      writeLock.unlock();
    }
  }

  public void shutdown()
  {
    shutdown = true;
    selector.wakeup();
  }

  public boolean isShutdown()
  {
    return (shutdown) || (restart);
  }

  public void restart()
  {
    restart = true;
    selector.wakeup();
  }

  public GameClient addWaitingClient(GameClient client)
  {
    writeLock.lock();
    try
    {
      GameClient localGameClient = (GameClient)waitingClients.put(client.getLogin(), client);
      return localGameClient; } finally { writeLock.unlock(); } throw localObject;
  }

  public GameClient removeWaitingClient(String account)
  {
    writeLock.lock();
    try
    {
      GameClient localGameClient = (GameClient)waitingClients.remove(account);
      return localGameClient; } finally { writeLock.unlock(); } throw localObject;
  }

  public GameClient addAuthedClient(GameClient client)
  {
    writeLock.lock();
    try
    {
      GameClient localGameClient = (GameClient)authedClients.put(client.getLogin(), client);
      return localGameClient; } finally { writeLock.unlock(); } throw localObject;
  }

  public GameClient removeAuthedClient(String login)
  {
    writeLock.lock();
    try
    {
      GameClient localGameClient = (GameClient)authedClients.remove(login);
      return localGameClient; } finally { writeLock.unlock(); } throw localObject;
  }

  public GameClient getAuthedClient(String login)
  {
    readLock.lock();
    try
    {
      GameClient localGameClient = (GameClient)authedClients.get(login);
      return localGameClient; } finally { readLock.unlock(); } throw localObject;
  }

  public GameClient removeClient(GameClient client)
  {
    writeLock.lock();
    try
    {
      if (client.isAuthed()) {
        localGameClient = (GameClient)authedClients.remove(client.getLogin());
        return localGameClient;
      }
      GameClient localGameClient = (GameClient)waitingClients.remove(client.getSessionKey());
      return localGameClient; } finally { writeLock.unlock(); } throw localObject;
  }

  public String[] getAccounts()
  {
    readLock.lock();
    try
    {
      String[] arrayOfString = (String[])authedClients.keySet().toArray(new String[authedClients.size()]);
      return arrayOfString; } finally { readLock.unlock(); } throw localObject;
  }
}