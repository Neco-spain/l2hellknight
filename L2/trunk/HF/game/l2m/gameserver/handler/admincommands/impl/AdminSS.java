package l2m.gameserver.handler.admincommands.impl;

import l2m.gameserver.handler.admincommands.IAdminCommandHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.model.entity.SevenSigns;

public class AdminSS
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().Menu) {
      return false;
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminSS$Commands[command.ordinal()])
    {
    case 1:
      if (wordList.length > 2)
      {
        int period = Integer.parseInt(wordList[1]);
        int minutes = Integer.parseInt(wordList[2]);
        SevenSigns.getInstance().changePeriod(period, minutes * 60);
      }
      else if (wordList.length > 1)
      {
        int period = Integer.parseInt(wordList[1]);
        SevenSigns.getInstance().changePeriod(period);
      }
      else {
        SevenSigns.getInstance().changePeriod();
      }break;
    case 2:
      if (wordList.length <= 1)
        break;
      int time = Integer.parseInt(wordList[1]);
      SevenSigns.getInstance().setTimeToNextPeriodChange(time);
      break;
    case 3:
      if (wordList.length <= 3)
        break;
      int player = Integer.parseInt(wordList[1]);
      int cabal = Integer.parseInt(wordList[2]);
      int seal = Integer.parseInt(wordList[3]);
      SevenSigns.getInstance().setPlayerInfo(player, cabal, seal);
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_ssq_change, 
    admin_ssq_time, 
    admin_ssq_cabal;
  }
}