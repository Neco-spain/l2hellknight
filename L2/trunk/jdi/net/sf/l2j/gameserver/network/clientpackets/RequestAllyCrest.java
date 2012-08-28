package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.AllyCrest;

public final class RequestAllyCrest extends L2GameClientPacket
{
  private static final String _C__88_REQUESTALLYCREST = "[C] 88 RequestAllyCrest";
  private static Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    if (Config.DEBUG) _log.fine("allycrestid " + _crestId + " requested");

    byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);

    if (data != null)
    {
      AllyCrest ac = new AllyCrest(_crestId, data);
      sendPacket(ac);
    }
    else if (Config.DEBUG) { _log.fine("allycrest is missing:" + _crestId);
    }
  }

  public String getType()
  {
    return "[C] 88 RequestAllyCrest";
  }
}