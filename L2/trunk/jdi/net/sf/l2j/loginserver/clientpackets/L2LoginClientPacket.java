package net.sf.l2j.loginserver.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.loginserver.L2LoginClient;
import org.mmocore.network.ReceivablePacket;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
  private static Logger _log = Logger.getLogger(L2LoginClientPacket.class.getName());

  protected final boolean read()
  {
    try
    {
      return readImpl();
    }
    catch (Exception e)
    {
      _log.severe("ERROR READING: " + getClass().getSimpleName());
      e.printStackTrace();
    }return false;
  }

  protected abstract boolean readImpl();
}