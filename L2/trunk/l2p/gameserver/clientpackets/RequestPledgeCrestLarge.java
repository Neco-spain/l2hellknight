package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.CrestCache;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExPledgeCrestLarge;

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