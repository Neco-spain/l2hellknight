package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
  private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    if (_crestId == 0)
      return;
    if (Config.DEBUG) _log.fine("crestid " + _crestId + " requested");

    byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);

    if (data != null)
    {
      PledgeCrest pc = new PledgeCrest(_crestId, data);
      sendPacket(pc);
    }
    else if (Config.DEBUG) { _log.fine("crest is missing:" + _crestId);
    }
  }

  public String getType()
  {
    return "[C] 68 RequestPledgeCrest";
  }
}