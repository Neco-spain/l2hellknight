package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class EffectHealCPPercent extends Effect
{
  private final boolean _ignoreCpEff;

  public EffectHealCPPercent(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreCpEff = template.getParam().getBool("ignoreCpEff", true);
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
    double cp = calc() * _effected.getMaxCp() / 100.0D;
    double newCp = cp * (!_ignoreCpEff ? _effected.calcStat(Stats.CPHEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToCp = Math.max(0.0D, Math.min(newCp, _effected.calcStat(Stats.CP_LIMIT, null, null) * _effected.getMaxCp() / 100.0D - _effected.getCurrentCp()));

    _effected.sendPacket(new SystemMessage(1406).addNumber(()addToCp));

    if (addToCp > 0.0D)
      _effected.setCurrentCp(addToCp + _effected.getCurrentCp());
  }

  public boolean onActionTime()
  {
    return false;
  }
}