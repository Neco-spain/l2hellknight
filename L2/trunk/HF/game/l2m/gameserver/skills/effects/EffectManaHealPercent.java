package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.templates.StatsSet;

public class EffectManaHealPercent extends Effect
{
  private final boolean _ignoreMpEff;

  public EffectManaHealPercent(Env env, EffectTemplate template)
  {
    super(env, template);
    _ignoreMpEff = template.getParam().getBool("ignoreMpEff", true);
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
    double mp = calc() * _effected.getMaxMp() / 100.0D;
    double newMp = mp * (!_ignoreMpEff ? _effected.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0D, _effector, getSkill()) : 100.0D) / 100.0D;
    double addToMp = Math.max(0.0D, Math.min(newMp, _effected.calcStat(Stats.MP_LIMIT, null, null) * _effected.getMaxMp() / 100.0D - _effected.getCurrentMp()));

    _effected.sendPacket(new SystemMessage(1068).addNumber(Math.round(addToMp)));

    if (addToMp > 0.0D)
      _effected.setCurrentMp(addToMp + _effected.getCurrentMp());
  }

  public boolean onActionTime()
  {
    return false;
  }
}