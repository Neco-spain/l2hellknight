package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Env;

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