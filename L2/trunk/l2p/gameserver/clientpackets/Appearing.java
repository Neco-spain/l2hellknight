package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class Appearing extends L2GameClientPacket
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
    if (activeChar.isLogoutStarted())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.getObserverMode() == 1)
    {
      activeChar.appearObserverMode();
      return;
    }

    if (activeChar.getObserverMode() == 2)
    {
      activeChar.returnFromObserverMode();
      return;
    }

    if (!activeChar.isTeleporting())
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.onTeleported();
  }
}