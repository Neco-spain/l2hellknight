package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class EffectHeal extends Effect
{
  private final boolean _ignoreHpEff;

  public EffectHeal(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreHpEff = template.getParam().getBool("ignoreHpEff", false);
  }

  public boolean checkCondition()
  {
    if (_effected.isHealBlocked())
      return false;
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();

    if (_effected.isHealBlocked()) {
      return;
    }
    double hp = calc();
    double newHp = hp * (!_ignoreHpEff ? _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToHp = Math.max(0.0D, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0D - _effected.getCurrentHp()));

    if (addToHp > 0.0D)
    {
      _effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));
      _effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}