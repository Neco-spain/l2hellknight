package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestObserverEnd extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (activeChar.getObserverMode() == 3)
      if (activeChar.getOlympiadGame() != null)
        activeChar.leaveOlympiadObserverMode(true);
      else
        activeChar.leaveObserverMode();
  }
}