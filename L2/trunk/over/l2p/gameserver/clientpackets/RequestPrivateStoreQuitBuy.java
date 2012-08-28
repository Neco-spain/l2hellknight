package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
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
    if ((!activeChar.isInStoreMode()) || (activeChar.getPrivateStoreType() != 3))
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.setPrivateStoreType(0);
    activeChar.standUp();
    activeChar.broadcastCharInfo();
  }
}