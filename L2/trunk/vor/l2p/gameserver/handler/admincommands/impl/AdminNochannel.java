package l2p.gameserver.handler.admincommands.impl;

import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.AdminFunctions;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Util;

public class AdminNochannel
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().CanBanChat) {
      return false;
    }
    int banChatCount = 0;
    int penaltyCount = 0;
    int banChatCountPerDay = activeChar.getPlayerAccess().BanChatCountPerDay;
    if (banChatCountPerDay > 0)
    {
      String count = activeChar.getVar("banChatCount");
      if (count != null) {
        banChatCount = Integer.parseInt(count);
      }
      String penalty = activeChar.getVar("penaltyChatCount");
      if (penalty != null) {
        penaltyCount = Integer.parseInt(penalty);
      }
      long LastBanChatDayTime = 0L;
      String time = activeChar.getVar("LastBanChatDayTime");
      if (time != null) {
        LastBanChatDayTime = Long.parseLong(time);
      }
      if (LastBanChatDayTime != 0L)
      {
        if (System.currentTimeMillis() - LastBanChatDayTime < 86400000L)
        {
          if (banChatCount >= banChatCountPerDay)
          {
            activeChar.sendMessage("\u0412 \u0441\u0443\u0442\u043A\u0438, \u0432\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u0432\u044B\u0434\u0430\u0442\u044C \u043D\u0435 \u0431\u043E\u043B\u0435\u0435 " + banChatCount + " \u0431\u0430\u043D\u043E\u0432 \u0447\u0430\u0442\u0430.");
            return false;
          }
        }
        else
        {
          int bonus_mod = banChatCount / 10;
          bonus_mod = Math.max(1, bonus_mod);
          bonus_mod = 1;
          if ((activeChar.getPlayerAccess().BanChatBonusId > 0) && (activeChar.getPlayerAccess().BanChatBonusCount > 0))
          {
            int add_count = activeChar.getPlayerAccess().BanChatBonusCount * bonus_mod;

            ItemTemplate item = ItemHolder.getInstance().getTemplate(activeChar.getPlayerAccess().BanChatBonusId);
            activeChar.sendMessage("\u0411\u043E\u043D\u0443\u0441 \u0437\u0430 \u043C\u043E\u0434\u0435\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u0438\u0435: " + add_count + " " + item.getName());

            if (penaltyCount > 0)
            {
              activeChar.sendMessage("\u0428\u0442\u0440\u0430\u0444 \u0437\u0430 \u043D\u0430\u0440\u0443\u0448\u0435\u043D\u0438\u044F: " + penaltyCount + " " + item.getName());
              activeChar.setVar("penaltyChatCount", "" + Math.max(0, penaltyCount - add_count), -1L);
              add_count -= penaltyCount;
            }

            if (add_count > 0)
              ItemFunctions.addItem(activeChar, activeChar.getPlayerAccess().BanChatBonusId, add_count, true);
          }
          activeChar.setVar("LastBanChatDayTime", "" + System.currentTimeMillis(), -1L);
          activeChar.setVar("banChatCount", "0", -1L);
          banChatCount = 0;
        }
      }
      else {
        activeChar.setVar("LastBanChatDayTime", "" + System.currentTimeMillis(), -1L);
      }
    }
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminNochannel$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //nochannel charName [period] [reason]");
        return false;
      }
      int timeval = 30;
      if (wordList.length > 2) {
        try
        {
          timeval = Integer.parseInt(wordList[2]);
        }
        catch (Exception E)
        {
          timeval = 30;
        }
      }
      String msg = AdminFunctions.banChat(activeChar, null, wordList[1], timeval, wordList.length > 3 ? Util.joinStrings(" ", wordList, 3) : null);
      activeChar.sendMessage(msg);

      if ((banChatCountPerDay <= -1) || (!msg.startsWith("\u0412\u044B \u0437\u0430\u0431\u0430\u043D\u0438\u043B\u0438 \u0447\u0430\u0442")))
        break;
      banChatCount++;
      activeChar.setVar("banChatCount", "" + banChatCount, -1L);
      activeChar.sendMessage("\u0423 \u0432\u0430\u0441 \u043E\u0441\u0442\u0430\u043B\u043E\u0441\u044C " + (banChatCountPerDay - banChatCount) + " \u0431\u0430\u043D\u043E\u0432 \u0447\u0430\u0442\u0430.");
    }

    return true;
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_nochannel, 
    admin_nc;
  }
}