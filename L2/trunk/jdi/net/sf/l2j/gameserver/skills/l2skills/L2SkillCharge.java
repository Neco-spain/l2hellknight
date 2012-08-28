package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill
{
  public L2SkillCharge(StatsSet set)
  {
    super(set);
  }

  public void useSkill(L2Character caster, L2Object[] targets)
  {
    if (caster.isAlikeDead()) {
      return;
    }
    EffectCharge effect = (EffectCharge)caster.getFirstEffect(this);
    if (effect != null)
    {
      if (effect.numCharges < getNumCharges())
      {
        effect.numCharges += 1;
        if ((caster instanceof L2PcInstance))
        {
          caster.sendPacket(new EtcStatusUpdate((L2PcInstance)caster));
          SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
          sm.addNumber(effect.numCharges);
          caster.sendPacket(sm);
        }
      }
      else
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXIMUM);
        caster.sendPacket(sm);
      }
      return;
    }
    getEffects(caster, caster);
    getEffectsSelf(caster);
  }
}