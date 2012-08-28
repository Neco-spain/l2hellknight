package l2p.gameserver.loginservercon;

import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReceivablePacket extends l2p.commons.net.nio.ReceivablePacket<LoginServerCommunication>
{
  private static final Logger _log = LoggerFactory.getLogger(ReceivablePacket.class);

  public LoginServerCommunication getClient()
  {
    return LoginServerCommunication.getInstance();
  }

  protected ByteBuffer getByteBuffer()
  {
    return getClient().getReadBuffer();
  }

  public final boolean read()
  {
    try
    {
      readImpl();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    return true;
  }

  public final void run()
  {
    try
    {
      runImpl();
    }
    catch (Exception e)
    {
      _log.error("", e);
    }
  }

  protected abstract void readImpl();

  protected abstract void runImpl();

  protected void sendPacket(SendablePacket sp) {
    getClient().sendPacket(sp);
  }
}