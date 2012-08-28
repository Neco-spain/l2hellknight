package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.GameClient;

public class RequestItemList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if ((!activeChar.getPlayerAccess().UseInventory) || (activeChar.isBlocked()))
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.sendItemList(true);
    activeChar.sendStatusUpdate(false, false, new int[] { 14 });
  }
}