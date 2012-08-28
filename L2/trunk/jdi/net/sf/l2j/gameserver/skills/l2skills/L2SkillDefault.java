package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillDefault extends L2Skill
{
  public L2SkillDefault(StatsSet set)
  {
    super(set);
  }

  public void useSkill(L2Character caster, L2Object[] targets)
  {
    caster.sendPacket(new ActionFailed());
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("Skill not implemented.  Skill ID: " + getId() + " " + getSkillType());
    caster.sendPacket(sm);
  }
}