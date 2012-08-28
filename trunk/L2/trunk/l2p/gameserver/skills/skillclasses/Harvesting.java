package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.reward.RewardItem;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.ItemFunctions;

public class Harvesting extends Skill
{
  public Harvesting(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    Player player = (Player)activeChar;

    for (Creature target : targets)
      if (target != null)
      {
        if (!target.isMonster()) {
          continue;
        }
        MonsterInstance monster = (MonsterInstance)target;

        if (!monster.isSeeded())
        {
          activeChar.sendPacket(Msg.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
          continue;
        }

        if (!monster.isSeeded(player))
        {
          activeChar.sendPacket(Msg.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
          continue;
        }

        double SuccessRate = Config.MANOR_HARVESTING_BASIC_SUCCESS;
        int diffPlayerTarget = Math.abs(activeChar.getLevel() - monster.getLevel());

        if (diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET) {
          SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
        }

        if (SuccessRate < 1.0D) {
          SuccessRate = 1.0D;
        }
        if ((Config.SKILLS_CHANCE_SHOW) && (player.getVarB("SkillsHideChance"))) {
          player.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Harvesting.Chance", player, new Object[0]).addNumber(()SuccessRate));
        }
        if (!Rnd.chance(SuccessRate))
        {
          activeChar.sendPacket(Msg.THE_HARVEST_HAS_FAILED);
          monster.clearHarvest();
          continue;
        }

        RewardItem item = monster.takeHarvest();
        if (item == null)
        {
          continue;
        }
        if ((!player.getInventory().validateCapacity(item.itemId, item.count)) || (!player.getInventory().validateWeight(item.itemId, item.count)))
        {
          ItemInstance harvest = ItemFunctions.createItem(item.itemId);
          harvest.setCount(item.count);
          harvest.dropToTheGround(player, monster);
          continue;
        }

        player.getInventory().addItem(item.itemId, item.count);

        player.sendPacket(new SystemMessage(1137).addName(player).addNumber(item.count).addItemName(item.itemId));
        if (player.isInParty())
        {
          SystemMessage smsg = new SystemMessage(1137).addString(player.getName()).addNumber(item.count).addItemName(item.itemId);
          player.getParty().broadcastToPartyMembers(player, smsg);
        }
      }
  }
}