package l2m.gameserver.skills.effects;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;

public class EffectManaDamOverTime extends Effect
{
  public EffectManaDamOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onActionTime()
  {
    if (_effected.isDead()) {
      return false;
    }
    double manaDam = calc();
    if ((manaDam > _effected.getCurrentMp()) && (getSkill().isToggle()))
    {
      _effected.sendPacket(Msg.NOT_ENOUGH_MP);
      _effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
      return false;
    }

    _effected.reduceCurrentMp(manaDam, null);
    return true;
  }
}