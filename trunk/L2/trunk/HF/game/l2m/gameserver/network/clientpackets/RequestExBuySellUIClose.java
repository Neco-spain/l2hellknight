package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
  protected void runImpl()
  {
  }

  protected void readImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setBuyListId(0);
    activeChar.sendItemList(true);
  }
}