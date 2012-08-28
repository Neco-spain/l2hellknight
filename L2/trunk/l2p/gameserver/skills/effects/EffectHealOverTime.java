package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.serverpackets.ExRegenMax;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class EffectHealOverTime extends Effect
{
  private final boolean _ignoreHpEff;

  public EffectHealOverTime(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreHpEff = template.getParam().getBool("ignoreHpEff", false);
  }

  public void onStart()
  {
    super.onStart();

    if ((getEffected().isPlayer()) && (getCount() > 0) && (getPeriod() > 0L))
      getEffected().sendPacket(new ExRegenMax(calc(), (int)(getCount() * getPeriod() / 1000L), Math.round((float)(getPeriod() / 1000L))));
  }

  public boolean onActionTime()
  {
    if (_effected.isHealBlocked()) {
      return true;
    }
    double hp = calc();
    double newHp = hp * (!_ignoreHpEff ? _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToHp = Math.max(0.0D, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0D - _effected.getCurrentHp()));

    if (addToHp > 0.0D) {
      getEffected().setCurrentHp(_effected.getCurrentHp() + addToHp, false);
    }
    return true;
  }
}