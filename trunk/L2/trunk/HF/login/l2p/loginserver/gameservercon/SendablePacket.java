package l2m.loginserver.gameservercon;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SendablePacket extends l2m.commons.net.nio.SendablePacket<GameServer>
{
  private static final Logger _log = LoggerFactory.getLogger(SendablePacket.class);
  protected GameServer _gs;
  protected ByteBuffer _buf;

  protected void setByteBuffer(ByteBuffer buf)
  {
    _buf = buf;
  }

  protected ByteBuffer getByteBuffer()
  {
    return _buf;
  }

  protected void setClient(GameServer gs)
  {
    _gs = gs;
  }

  public GameServer getClient()
  {
    return _gs;
  }

  public GameServer getGameServer()
  {
    return getClient();
  }

  public boolean write()
  {
    try
    {
      writeImpl();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    return true;
  }

  protected abstract void writeImpl();
}