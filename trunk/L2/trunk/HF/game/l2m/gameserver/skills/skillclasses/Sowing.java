package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Manor;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.MonsterInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.ItemTemplate;

public class Sowing extends Skill
{
  public Sowing(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    Player player = (Player)activeChar;
    int seedId = player.getUseSeed();
    boolean altSeed = ItemHolder.getInstance().getTemplate(seedId).isAltSeed();

    if (!player.getInventory().destroyItemByItemId(seedId, 1L))
    {
      activeChar.sendActionFailed();
      return;
    }

    player.sendPacket(SystemMessage2.removeItems(seedId, 1L));

    for (Creature target : targets)
      if (target != null)
      {
        MonsterInstance monster = (MonsterInstance)target;
        if (monster.isSeeded())
        {
          continue;
        }
        double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;

        double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
        double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());

        if (diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET) {
          SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
        }

        if (diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET) {
          SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;
        }
        if (altSeed) {
          SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;
        }

        if (SuccessRate < 1.0D) {
          SuccessRate = 1.0D;
        }
        if ((Config.SKILLS_CHANCE_SHOW) && (player.getVarB("SkillsHideChance"))) {
          activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Sowing.Chance", player, new Object[0]).addNumber(()SuccessRate));

          if ((Rnd.chance(SuccessRate)) && (monster.setSeeded(player, seedId, altSeed)))
            activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
          else
            activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
        }
      }
  }
}