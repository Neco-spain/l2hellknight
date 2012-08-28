package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PledgeCrest;

public class RequestPledgeCrest extends L2GameClientPacket
{
  private int _crestId;

  protected void readImpl()
  {
    _crestId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (_crestId == 0)
      return;
    byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
    if (data != null)
    {
      PledgeCrest pc = new PledgeCrest(_crestId, data);
      sendPacket(pc);
    }
  }
}