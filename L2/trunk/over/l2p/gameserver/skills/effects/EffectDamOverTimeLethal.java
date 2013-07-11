package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;

public class EffectDamOverTimeLethal extends Effect
{
  public EffectDamOverTimeLethal(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean onActionTime()
  {
    if (_effected.isDead()) {
      return false;
    }
    double damage = calc();

    if (getSkill().isOffensive()) {
      damage *= 2.0D;
    }
    damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());

    _effected.reduceCurrentHp(damage, _effector, getSkill(), (!_effected.isNpc()) && (_effected != _effector), _effected != _effector, (_effector.isNpc()) || (_effected == _effector), false, false, true, false);

    return true;
  }
}