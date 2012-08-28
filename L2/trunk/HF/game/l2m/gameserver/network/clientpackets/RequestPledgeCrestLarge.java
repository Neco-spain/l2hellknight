package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.CrestCache;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExPledgeCrestLarge;

public class RequestPledgeCrestLarge extends L2GameClientPacket
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
    byte[] data = CrestCache.getInstance().getPledgeCrestLarge(_crestId);
    if (data != null)
    {
      ExPledgeCrestLarge pcl = new ExPledgeCrestLarge(_crestId, data);
      sendPacket(pcl);
    }
  }
}