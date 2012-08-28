package l2m.loginserver.gameservercon;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2m.commons.threading.RunnableImpl;
import l2m.loginserver.Config;
import l2m.loginserver.ThreadPoolManager;
import l2m.loginserver.gameservercon.lspackets.PingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServerConnection
{
  private static final Logger _log = LoggerFactory.getLogger(GameServerConnection.class);

  final ByteBuffer readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
  final Queue<SendablePacket> sendQueue = new ArrayDeque();
  final Lock sendLock = new ReentrantLock();

  final AtomicBoolean isPengingWrite = new AtomicBoolean();
  private final Selector selector;
  private final SelectionKey key;
  private GameServer gameServer;
  private Future<?> _pingTask;
  private int _pingRetry;

  public GameServerConnection(SelectionKey key)
  {
    this.key = key;
    selector = key.selector();
  }
  public void sendPacket(SendablePacket packet) {
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

  protected boolean disableWriteInterest() throws CancelledKeyException
  {
    if (isPengingWrite.compareAndSet(true, false))
    {
      key.interestOps(key.interestOps() & 0xFFFFFFFB);
      return true;
    }
    return false;
  }

  protected boolean enableWriteInterest() throws CancelledKeyException
  {
    if (!isPengingWrite.getAndSet(true))
    {
      key.interestOps(key.interestOps() | 0x4);
      return true;
    }
    return false;
  }

  public void closeNow()
  {
    key.interestOps(8);
    selector.wakeup();
  }

  public void onDisconnection()
  {
    try
    {
      stopPingTask();

      readBuffer.clear();

      sendLock.lock();
      try
      {
        sendQueue.clear();
      }
      finally
      {
        sendLock.unlock();
      }

      isPengingWrite.set(false);

      if ((gameServer != null) && (gameServer.isAuthed()))
      {
        _log.info("Connection with gameserver " + gameServer.getId() + " [" + gameServer.getName() + "] lost.");
        _log.info("Setting gameserver down.");
        gameServer.setDown();
      }

      gameServer = null;
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
  }

  ByteBuffer getReadBuffer()
  {
    return readBuffer;
  }

  GameServer getGameServer()
  {
    return gameServer;
  }

  void setGameServer(GameServer gameServer)
  {
    this.gameServer = gameServer;
  }

  public String getIpAddress()
  {
    return ((SocketChannel)key.channel()).socket().getInetAddress().getHostAddress();
  }

  public void onPingResponse()
  {
    _pingRetry = 0;
  }

  public void startPingTask()
  {
    if (Config.GAME_SERVER_PING_DELAY == 0L) {
      return;
    }
    _pingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new PingTask(null), Config.GAME_SERVER_PING_DELAY, Config.GAME_SERVER_PING_DELAY);
  }

  public void stopPingTask()
  {
    if (_pingTask != null)
    {
      _pingTask.cancel(false);
      _pingTask = null;
    }
  }

  private class PingTask extends RunnableImpl
  {
    private PingTask()
    {
    }

    public void runImpl()
    {
      if (Config.GAME_SERVER_PING_RETRY > 0)
      {
        if (_pingRetry > Config.GAME_SERVER_PING_RETRY)
        {
          _log.warn("Gameserver " + gameServer.getId() + " [" + gameServer.getName() + "] : ping timeout!");
          closeNow();
          return;
        }
      }
      GameServerConnection.access$008(GameServerConnection.this);
      sendPacket(new PingRequest());
    }
  }
}