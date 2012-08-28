package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
  private String _storename;

  protected void readImpl()
  {
    _storename = readS(32);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setBuyStoreName(_storename);
  }
}