package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    if (_crestId == 0) {
      return;
    }

    byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);

    if (data != null)
      sendPacket(new PledgeCrest(_crestId, data));
  }

  public String getType()
  {
    return "[C] PledgeCrest";
  }
}