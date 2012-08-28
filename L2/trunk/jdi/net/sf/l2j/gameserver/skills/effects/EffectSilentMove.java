package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSilentMove extends L2Effect
{
  public EffectSilentMove(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onStart()
  {
    super.onStart();

    L2Character effected = getEffected();
    if ((effected instanceof L2PcInstance))
    {
      ((L2PcInstance)effected).setSilentMoving(true);
      return true;
    }
    return false;
  }

  public void onExit()
  {
    super.onExit();

    L2Character effected = getEffected();
    if ((effected instanceof L2PcInstance))
      ((L2PcInstance)effected).setSilentMoving(false);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SILENT_MOVE;
  }

  public boolean onActionTime()
  {
    if (getSkill().getSkillType() != L2Skill.SkillType.CONT) {
      return false;
    }
    if (getEffected().isDead()) {
      return false;
    }
    double manaDam = calc();

    if (manaDam > getEffected().getCurrentMp())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
      getEffected().sendPacket(sm);
      return false;
    }

    getEffected().reduceCurrentMp(manaDam);
    return true;
  }
}