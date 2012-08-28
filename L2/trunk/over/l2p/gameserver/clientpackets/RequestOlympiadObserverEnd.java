package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if ((activeChar.getObserverMode() == 3) && 
      (activeChar.getOlympiadObserveGame() != null))
      activeChar.leaveOlympiadObserverMode(true);
  }
}