package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.instancemanager.HellboundManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import org.apache.commons.lang3.math.NumberUtils;

public class AdminHellbound
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminHellbound$Commands[command.ordinal()])
    {
    case 1:
      HellboundManager.addConfidence(Long.parseLong(wordList[1]));
      activeChar.sendMessage("Added " + NumberUtils.toInt(wordList[1], 1) + " to Hellbound confidence");
      activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
      break;
    case 2:
      HellboundManager.reduceConfidence(Long.parseLong(wordList[1]));
      activeChar.sendMessage("Reduced confidence by " + NumberUtils.toInt(wordList[1], 1));
      activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
      break;
    case 3:
      HellboundManager.setConfidence(Long.parseLong(wordList[1]));
      activeChar.sendMessage("Hellbound confidence is now " + HellboundManager.getConfidence());
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_hbadd, 
    admin_hbsub, 
    admin_hbset;
  }
}