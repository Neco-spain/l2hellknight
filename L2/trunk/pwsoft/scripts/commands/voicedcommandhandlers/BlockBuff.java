package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import scripts.commands.IVoicedCommandHandler;

public class BlockBuff
  implements IVoicedCommandHandler
{
  private static final String[] VOICED_COMMANDS = { "blockbuff" };

  public boolean useVoicedCommand(String command, L2PcInstance player, String target) {
    if (command.equalsIgnoreCase("blockbuff")) {
      if ((player.isOutOfControl()) || (player.isParalyzed())) {
        return false;
      }

      if (player.underAttack()) {
        return false;
      }

      if (player.getFirstEffect(Config.ANTIBUFF_SKILLID) != null) {
        player.stopSkillEffects(Config.ANTIBUFF_SKILLID);
        player.sendMessage("\u0410\u043D\u0442\u0438\u0431\u0430\u0444\u0444 \u043E\u0442\u043A\u043B\u044E\u0447\u0435\u043D.");
        return true;
      }
      SkillTable.getInstance().getInfo(Config.ANTIBUFF_SKILLID, 1).getEffects(player, player);
      player.sendMessage("\u0410\u043D\u0442\u0438\u0431\u0430\u0444\u0444 \u0430\u043A\u0442\u0438\u0432\u0438\u0440\u043E\u0432\u0430\u043D.");
    }
    return true;
  }

  public String[] getVoicedCommandList() {
    return VOICED_COMMANDS;
  }
}