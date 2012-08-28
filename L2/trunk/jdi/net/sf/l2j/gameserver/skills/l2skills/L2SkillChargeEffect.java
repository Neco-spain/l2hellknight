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

public class L2SkillChargeEffect extends L2Skill
{
  final int chargeSkillId;

  public L2SkillChargeEffect(StatsSet set)
  {
    super(set);
    chargeSkillId = set.getInteger("charge_skill_id");
  }

  public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
  {
    if ((activeChar instanceof L2PcInstance))
    {
      L2PcInstance player = (L2PcInstance)activeChar;
      EffectCharge e = (EffectCharge)player.getFirstEffect(chargeSkillId);
      if ((e == null) || (e.numCharges < getNumCharges()))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
        sm.addSkillName(getId());
        activeChar.sendPacket(sm);
        return false;
      }
    }
    return super.checkCondition(activeChar, target, itemOrWeapon);
  }

  public void useSkill(L2Character activeChar, L2Object[] targets)
  {
    if (activeChar.isAlikeDead()) return;

    EffectCharge effect = (EffectCharge)activeChar.getFirstEffect(chargeSkillId);
    if ((effect == null) || (effect.numCharges < getNumCharges()))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
      sm.addSkillName(getId());
      activeChar.sendPacket(sm);
      return;
    }

    effect.numCharges -= getNumCharges();

    if (effect.numCharges == 0) effect.exit();

    if (hasEffects())
      for (int index = 0; index < targets.length; index++)
        getEffects(activeChar, (L2Character)targets[index]);
    if ((activeChar instanceof L2PcInstance))
    {
      activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance)activeChar));
    }
  }
}