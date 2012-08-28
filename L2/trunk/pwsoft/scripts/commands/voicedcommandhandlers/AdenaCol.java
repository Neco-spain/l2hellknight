package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;
import scripts.commands.IVoicedCommandHandler;

public class AdenaCol
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "adena", "col" };
  private static final Config.EventReward ADENA = Config.CMD_AC_ADENA;
  private static final Config.EventReward COL = Config.CMD_AC_COL;

  public boolean useVoicedCommand(String command, L2PcInstance player, String target)
  {
    if (command.equalsIgnoreCase("col")) {
      if (player.getItemCount(ADENA.id) >= ADENA.count) {
        player.destroyItemByItemId(".col", ADENA.id, ADENA.count, player, true);
        player.addItem(".col", COL.id, ADENA.chance, player, true);
      } else {
        player.sendCritMessage("\u041A\u0443\u0440\u0441 \u043E\u0431\u043C\u0435\u043D\u0430: " + Util.formatAdena(ADENA.count) + " Adena \u043D\u0430 " + Util.formatAdena(ADENA.chance) + " Coin");
      }
    } else if (command.equalsIgnoreCase("adena")) {
      if (player.getItemCount(COL.id) >= COL.count) {
        player.destroyItemByItemId(".col", COL.id, COL.count, player, true);
        player.addItem(".adena", ADENA.id, COL.chance, player, true);
      } else {
        player.sendCritMessage("\u041A\u0443\u0440\u0441 \u043E\u0431\u043C\u0435\u043D\u0430: " + Util.formatAdena(COL.count) + " Coin \u043D\u0430 " + Util.formatAdena(COL.chance) + " Adena");
      }
    } else if (command.startsWith("col")) {
      int count = 0;
      try {
        count = Integer.parseInt(command.substring(4));
      } catch (Exception e) {
        player.sendMessage("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0446\u0435\u043B\u043E\u0435 \u0447\u0438\u0441\u043B\u043E.");
        return true;
      }
      if ((Config.CMD_AC_COL_LIMIT == 0) || (count > Config.CMD_AC_COL_LIMIT)) {
        player.sendMessage("\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C " + Config.CMD_AC_COL_LIMIT + " \u0437\u0430 \u043E\u0434\u0438\u043D \u043E\u0431\u043C\u0435\u043D.");
        return true;
      }

      if (player.getItemCount(ADENA.id) >= ADENA.count * count) {
        player.destroyItemByItemId(".col", ADENA.id, ADENA.count * count, player, true);
        player.addItem(".col", COL.id, ADENA.chance * count, player, true);
      } else {
        player.sendCritMessage("\u041A\u0443\u0440\u0441 \u043E\u0431\u043C\u0435\u043D\u0430: " + Util.formatAdena(ADENA.count) + " Adena \u043D\u0430 " + Util.formatAdena(ADENA.chance) + " Coin");
      }
    } else if (command.startsWith("adena")) {
      int count = 0;
      try {
        count = Integer.parseInt(command.substring(6));
      } catch (Exception e) {
        player.sendMessage("\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0446\u0435\u043B\u043E\u0435 \u0447\u0438\u0441\u043B\u043E.");
        return true;
      }
      if ((Config.CMD_AC_ADENA_LIMIT == 0) || (count > Config.CMD_AC_ADENA_LIMIT)) {
        player.sendMessage("\u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C " + Config.CMD_AC_ADENA_LIMIT + " \u0437\u0430 \u043E\u0434\u0438\u043D \u043E\u0431\u043C\u0435\u043D.");
        return true;
      }

      if (player.getItemCount(COL.id) >= COL.count * count) {
        player.destroyItemByItemId(".col", COL.id, COL.count * count, player, true);
        player.addItem(".adena", ADENA.id, COL.chance * count, player, true);
      } else {
        player.sendCritMessage("\u041A\u0443\u0440\u0441 \u043E\u0431\u043C\u0435\u043D\u0430: " + Util.formatAdena(COL.count) + " Coin \u043D\u0430 " + Util.formatAdena(COL.chance) + " Adena");
      }
    }
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return VOICED_COMMANDS;
  }
}