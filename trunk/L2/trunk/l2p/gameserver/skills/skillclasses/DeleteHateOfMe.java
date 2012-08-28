package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlIntention;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.templates.StatsSet;

public class DeleteHateOfMe extends Skill
{
  public DeleteHateOfMe(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
      {
        if ((Config.SKILLS_CHANCE_SHOW) && (activeChar.isPlayer()) && (((Player)activeChar).getVarB("SkillsHideChance"))) {
          activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance", (Player)activeChar, new Object[0]).addString(getName()).addNumber(getActivateRate()));
        }
        if ((target.isNpc()) && (Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())))
        {
          NpcInstance npc = (NpcInstance)target;
          npc.getAggroList().remove(activeChar, true);
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
        getEffects(activeChar, target, true, false);
      }
  }
}