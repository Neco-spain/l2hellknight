package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.model.AggroList;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.StatsSet;

public class DeleteHate extends Skill
{
  public DeleteHate(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature target : targets)
      if (target != null)
      {
        if (target.isRaid()) {
          continue;
        }
        if (getActivateRate() > 0)
        {
          if ((Config.SKILLS_CHANCE_SHOW) && (activeChar.isPlayer()) && (((Player)activeChar).getVarB("SkillsHideChance"))) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.Formulas.Chance", (Player)activeChar, new Object[0]).addString(getName()).addNumber(getActivateRate()));
          }
          if (!Rnd.chance(getActivateRate())) {
            return;
          }
        }
        if (target.isNpc())
        {
          NpcInstance npc = (NpcInstance)target;
          npc.getAggroList().clear(false);
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }

        getEffects(activeChar, target, false, false);
      }
  }
}