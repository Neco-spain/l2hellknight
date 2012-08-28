package scripts.commands.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.GMViewPledgeInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.commands.IAdminCommandHandler;

public class AdminPledge
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_pledge" };

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!activeChar.isGM()) || (activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL) || (activeChar.getTarget() == null) || (!activeChar.getTarget().isPlayer()))) {
      return false;
    }

    L2Object target = activeChar.getTarget();
    L2PcInstance player = null;
    if (target.isPlayer()) {
      player = target.getPlayer();
    } else {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCORRECT_TARGET));
      showMainPage(activeChar);
      return false;
    }
    String name = player.getName();
    if (command.startsWith("admin_pledge")) {
      String action = null;
      String parameter = null;
      GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
      StringTokenizer st = new StringTokenizer(command);
      try {
        st.nextToken();
        action = st.nextToken();
        parameter = st.nextToken();
      } catch (NoSuchElementException nse) {
      }
      if (action.equals("create")) {
        long cet = player.getClanCreateExpiryTime();
        player.setClanCreateExpiryTime(0L);
        L2Clan clan = ClanTable.getInstance().createClan(player, parameter);
        if (clan != null) {
          activeChar.sendAdmResultMessage(new StringBuilder().append("Clan ").append(parameter).append(" created. Leader: ").append(player.getName()).toString());
        } else {
          player.setClanCreateExpiryTime(cet);
          activeChar.sendAdmResultMessage("There was a problem while creating the clan.");
        }
      } else {
        if (!player.isClanLeader()) {
          activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(name));
          showMainPage(activeChar);
          return false;
        }if (action.equals("dismiss")) {
          ClanTable.getInstance().destroyClan(player.getClanId());
          L2Clan clan = player.getClan();
          if (clan == null)
            activeChar.sendAdmResultMessage("Clan disbanded.");
          else
            activeChar.sendAdmResultMessage("There was a problem while destroying the clan.");
        }
        else if (action.equals("info")) {
          activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
        } else if (parameter == null) {
          activeChar.sendAdmResultMessage("Usage: //pledge <setlevel|rep> <number>");
        } else if (action.equals("setlevel")) {
          int level = Integer.parseInt(parameter);
          if ((level >= 0) && (level < 9)) {
            player.getClan().changeLevel(level);
            activeChar.sendAdmResultMessage(new StringBuilder().append("You set level ").append(level).append(" for clan ").append(player.getClan().getName()).toString());
          } else {
            activeChar.sendAdmResultMessage("Level incorrect.");
          }
        } else if (action.startsWith("rep")) {
          try {
            int points = Integer.parseInt(parameter);
            L2Clan clan = player.getClan();
            if (clan.getLevel() < 5) {
              activeChar.sendAdmResultMessage("Only clans of level 5 or above may receive reputation points.");
              showMainPage(activeChar);
              return false;
            }
            clan.setReputationScore(clan.getReputationScore() + points, true);
            activeChar.sendAdmResultMessage(new StringBuilder().append("You ").append(points > 0 ? "add " : "remove ").append(Math.abs(points)).append(" points ").append(points > 0 ? "to " : "from ").append(clan.getName()).append("'s reputation. Their current score is ").append(clan.getReputationScore()).toString());
          } catch (Exception e) {
            activeChar.sendAdmResultMessage("Usage: //pledge <rep> <number>");
          }
        } else if (action.equals("fcs")) {
          try {
            L2Clan clan = player.getClan();
            if (clan.getLevel() < 5) {
              activeChar.sendAdmResultMessage("Only clans of level 5 or above may receive reputation points.");
              showMainPage(activeChar);
              return false;
            }

            CustomServerData.getInstance().addClanSkills(player, clan);

            if (player.equals(activeChar))
            {
              player.sendAdmResultMessage("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u044B \u0444\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435, \u0447\u0442\u043E-\u0431\u044B \u0443\u0432\u0438\u0434\u0435\u0442\u044C \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0438\u044F.");
            }
            else
            {
              activeChar.sendAdmResultMessage(new StringBuilder().append("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u044B \u0444\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B \u043A\u043B\u0430\u043D\u0443 ").append(clan.getName()).append("").toString());
              player.sendAdmResultMessage("\u0414\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u044B \u0444\u0443\u043B\u043B \u043A\u043B\u0430\u043D \u0441\u043A\u0438\u043B\u043B\u044B, \u043F\u0435\u0440\u0435\u0437\u0430\u0439\u0434\u0438\u0442\u0435, \u0447\u0442\u043E-\u0431\u044B \u0443\u0432\u0438\u0434\u0435\u0442\u044C \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0438\u044F.");
            }
          } catch (Exception e) {
            activeChar.sendAdmResultMessage("Usage: //pledge fcs full");
          }
        }
      }
    }
    showMainPage(activeChar);
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private void showMainPage(L2PcInstance activeChar) {
    AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
  }
}