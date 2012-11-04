package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.model.instances.NpcInstance;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.stats.Formulas;
import l2r.gameserver.templates.StatsSet;

public class DeleteHateOfMe extends Skill
{
	public DeleteHateOfMe(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
			if(target != null)
			{
				if(activeChar.isPlayer() && ((Player) activeChar).isGM())
					activeChar.sendMessage(new CustomMessage("l2r.gameserver.skills.Formulas.Chance", (Player)activeChar).addString(getName()).addNumber(getActivateRate()));

				if(target.isNpc() && Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				{
					NpcInstance npc = (NpcInstance) target;
					npc.getAggroList().remove(activeChar, true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}
				getEffects(activeChar, target, true, false);
			}
	}
}