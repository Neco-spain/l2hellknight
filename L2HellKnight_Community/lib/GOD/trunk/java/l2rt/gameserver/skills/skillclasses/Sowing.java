package l2rt.gameserver.skills.skillclasses;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Manor;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class Sowing extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		// Проверки в хэндлере
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	public Sowing(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		if(!activeChar.isPlayer())
			return;

		L2Player player = (L2Player) activeChar;
		int seed_id = player.getUseSeed();
		// remove seed from inventory
		L2ItemInstance seedItem = player.getInventory().getItemByItemId(seed_id);
		if(seedItem != null)
		{
			player.sendPacket(SystemMessage.removeItems(seed_id, 1));
			player.getInventory().destroyItem(seedItem, 1, true);
		}
		else
		{
			activeChar.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		for(L2Character target : targets)
			if(target != null)
			{
				L2MonsterInstance monster = (L2MonsterInstance) target;
				if(monster.isSeeded())
					continue;

				// обработка
				double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;

				double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
				double diffSeedTarget = Math.abs(L2Manor.getInstance().getSeedLevel(seed_id) - target.getLevel());

				// Штраф, на разницу уровней между мобом и игроком
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET)
					SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;

				// Штраф, на разницу уровней между семечкой и мобом
				// 5% на каждый уровень при разнице >5 - по умолчанию
				if(diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET)
					SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;

				if(ItemTemplates.getInstance().getTemplate(seed_id).isAltSeed())
					SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;

				// Минимальный шанс успеха всегда 1%
				if(SuccessRate < 1)
					SuccessRate = 1;

				if(ConfigSystem.getBoolean("SkillsShowChance") && !player.getVarB("SkillsHideChance"))
					activeChar.sendMessage(new CustomMessage("l2rt.gameserver.skills.skillclasses.Sowing.Chance", activeChar).addNumber((long) SuccessRate));

				if(Rnd.chance(SuccessRate))
				{
					monster.setSeeded(seedItem.getItem(), player);
					activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
				}
				else
					activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
			}
	}
}