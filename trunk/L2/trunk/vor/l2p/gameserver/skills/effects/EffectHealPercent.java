package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;

public class EffectHealPercent extends Effect
{
  private final boolean _ignoreHpEff;

  public EffectHealPercent(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreHpEff = template.getParam().getBool("ignoreHpEff", true);
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
    double hp = calc() * _effected.getMaxHp() / 100.0D;
    double newHp = hp * (!_ignoreHpEff ? _effected.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToHp = Math.max(0.0D, Math.min(newHp, _effected.calcStat(Stats.HP_LIMIT, null, null) * _effected.getMaxHp() / 100.0D - _effected.getCurrentHp()));

    _effected.sendPacket(new SystemMessage(1066).addNumber(Math.round(addToHp)));

    if (addToHp > 0.0D)
      _effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
  }

  public boolean onActionTime()
  {
    return false;
  }
}