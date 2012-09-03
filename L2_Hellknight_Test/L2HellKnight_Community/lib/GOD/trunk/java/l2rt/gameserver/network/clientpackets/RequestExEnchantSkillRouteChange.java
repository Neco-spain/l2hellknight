package l2rt.gameserver.network.clientpackets;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2ShortCut;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.base.L2EnchantSkillLearn;
import l2rt.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2rt.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2rt.gameserver.network.serverpackets.ShortCutRegister;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.util.Log;
import l2rt.util.Rnd;

public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getTransformation() != 0)
		{
			activeChar.sendMessage("You must leave transformation mode first.");
			return;
		}

		if(activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendMessage("You must have 3rd class change quest completed.");
			return;
		}

		L2EnchantSkillLearn sl = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
		if(sl == null)
			return;

		short slevel = activeChar.getSkillLevel(_skillId);
		if(slevel == -1)
			return;

		if(slevel < sl.getBaseLevel())
			return;

		int[] cost = sl.getCost();
		int requiredSp = cost[1] * sl.getCostMult() / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
		int requiredAdena = cost[0] * sl.getCostMult() / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;

		if(activeChar.getSp() < requiredSp)
		{
			sendPacket(Msg.SP_REQUIRED_FOR_SKILL_ENCHANT_IS_INSUFFICIENT);
			return;
		}

		if(activeChar.getAdena() < requiredAdena)
		{
			sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}

		if(Functions.getItemCount(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK) == 0)
		{
			activeChar.sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
			return;
		}

		Functions.removeItem(activeChar, SkillTreeTable.CHANGE_ENCHANT_BOOK, 1);
		Functions.removeItem(activeChar, 57, requiredAdena);
		activeChar.addExpAndSp(0, -1 * requiredSp, false, false);

		int levelPenalty = Rnd.get(Math.min(4, _skillLvl % 100));

		_skillLvl -= levelPenalty;
		if(_skillLvl % 100 == 0)
			_skillLvl = sl.getBaseLevel();

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, SkillTreeTable.convertEnchantLevel(sl.getBaseLevel(), _skillLvl, sl.getMaxLevel()));

		if(skill != null)
			activeChar.addSkill(skill, true);

		if(levelPenalty == 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Enchant_skill_route_change_was_successful_Lv_of_enchant_skill_S1_will_remain);
			sm.addSkillName(_skillId, _skillLvl);
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.Enchant_skill_route_change_was_successful_Lv_of_enchant_skill_S1_has_been_decreased_by_S2);
			sm.addSkillName(_skillId, _skillLvl);
			sm.addNumber(levelPenalty);
			activeChar.sendPacket(sm);
		}

		Log.add(activeChar.getName() + "|Successfully changed route|" + _skillId + "|to+" + _skillLvl + "|" + levelPenalty, "enchant_skills");

		activeChar.sendPacket(new ExEnchantSkillInfo(_skillId, activeChar.getSkillDisplayLevel(_skillId)), new ExEnchantSkillResult(1));
		updateSkillShortcuts(activeChar);
	}

	private void updateSkillShortcuts(L2Player player)
	{
		// update all the shortcuts to this skill
		for(L2ShortCut sc : player.getAllShortCuts())
			if(sc.id == _skillId && sc.type == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.slot, sc.page, sc.type, sc.id, _skillLvl);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
	}
}