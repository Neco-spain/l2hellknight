package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class EffectDamOverTime extends Effect
{
  private static int[] bleed = { 12, 17, 25, 34, 44, 54, 62, 67, 72, 77, 82, 87 };
  private static int[] poison = { 11, 16, 24, 32, 41, 50, 58, 63, 68, 72, 77, 82 };
  private boolean _percent;

  public EffectDamOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
    _percent = getTemplate().getParam().getBool("percent", false);
  }

  public boolean onActionTime()
  {
    if (_effected.isDead()) {
      return false;
    }
    double damage = calc();
    if (_percent)
      damage = _effected.getMaxHp() * _template._value * 0.01D;
    if ((damage < 2.0D) && (getStackOrder() != -1)) {
      switch (1.$SwitchMap$l2p$gameserver$skills$EffectType[getEffectType().ordinal()])
      {
      case 1:
        damage = poison[(getStackOrder() - 1)] * getPeriod() / 1000L;
        break;
      case 2:
        damage = bleed[(getStackOrder() - 1)] * getPeriod() / 1000L;
      }
    }

    damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());

    if ((damage > _effected.getCurrentHp() - 1.0D) && (!_effected.isNpc()))
    {
      if (!getSkill().isOffensive())
        _effected.sendPacket(Msg.NOT_ENOUGH_HP);
      return false;
    }

    if (getSkill().getAbsorbPart() > 0.0D) {
      _effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(_effected.getCurrentHp(), damage) + _effector.getCurrentHp(), false);
    }
    _effected.reduceCurrentHp(damage, _effector, getSkill(), (!_effected.isNpc()) && (_effected != _effector), _effected != _effector, (_effector.isNpc()) || (_effected == _effector), false, false, true, false);

    return true;
  }
}