package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
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
    activeChar.setSellStoreName(_storename);
  }
}