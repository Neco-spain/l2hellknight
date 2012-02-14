package l2rt.gameserver.skills.skillclasses;

import l2rt.config.ConfigSystem;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.skills.Formulas;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class DeleteHateOfMe extends L2Skill
{
	private final boolean _cancelSelfTarget;

	public DeleteHateOfMe(StatsSet set)
	{
		super(set);
		_cancelSelfTarget = set.getBool("cancelSelfTarget", false);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{

				boolean success = _id == SKILL_BLUFF ? false : Rnd.chance(getActivateRate());

				if(_id != SKILL_BLUFF && ConfigSystem.getBoolean("SkillsShowChance") && activeChar.isPlayer() && !((L2Player) activeChar).getVarB("SkillsHideChance"))
					activeChar.sendMessage(new CustomMessage("l2rt.gameserver.skills.Formulas.Chance", activeChar).addString(getName()).addNumber(getActivateRate()));

				if(_id == SKILL_BLUFF ? Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()) : success)
				{
					if(target.isNpc())
					{
						L2NpcInstance npc = (L2NpcInstance) target;
						activeChar.removeFromHatelist(npc, true);
						npc.getAI().clearTasks();
						npc.getAI().setAttackTarget(null);
						if(npc.isNoTarget())
						{
							npc.getAI().setGlobalAggro(System.currentTimeMillis() + 10000);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						}
					}

					if(_cancelSelfTarget)
						activeChar.setTarget(null);
				}

				// Для Bluff шанс прохождения эффекта скила расчитывается как для шоковых атак, причем отдельно
				if(success || _id == SKILL_BLUFF)
					getEffects(activeChar, target, _id == SKILL_BLUFF, false);
			}
	}
}