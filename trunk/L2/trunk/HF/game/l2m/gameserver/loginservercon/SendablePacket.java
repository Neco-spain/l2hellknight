package l2m.gameserver.loginservercon;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SendablePacket extends l2p.commons.net.nio.SendablePacket<LoginServerCommunication>
{
  private static final Logger _log = LoggerFactory.getLogger(SendablePacket.class);

  public LoginServerCommunication getClient()
  {
    return LoginServerCommunication.getInstance();
  }

  protected ByteBuffer getByteBuffer()
  {
    return getClient().getWriteBuffer();
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