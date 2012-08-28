package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class Logout extends L2GameClientPacket
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

    if (activeChar.isInCombat())
    {
      activeChar.sendPacket(SystemMsg.YOU_CANNOT_EXIT_THE_GAME_WHILE_IN_COMBAT);
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
      activeChar.sendActionFailed();
      return;
    }

    if ((activeChar.isBlocked()) && (!activeChar.isFlying()))
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.OutOfControl", activeChar, new Object[0]));
      activeChar.sendActionFailed();
      return;
    }

    if ((activeChar.isFestivalParticipant()) && 
      (SevenSignsFestival.getInstance().isFestivalInitialized()))
    {
      activeChar.sendMessage("You cannot log out while you are a participant in a festival.");
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Olympiad", activeChar, new Object[0]));
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInObserverMode())
    {
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.Logout.Observer", activeChar, new Object[0]));
      activeChar.sendActionFailed();
      return;
    }

    activeChar.kick();
  }
}