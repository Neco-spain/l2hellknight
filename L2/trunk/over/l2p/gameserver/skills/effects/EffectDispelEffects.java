package l2p.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Stats;
import l2p.gameserver.templates.StatsSet;
import org.apache.commons.lang3.ArrayUtils;

public class EffectDispelEffects extends Effect
{
  private final String _dispelType;
  private final int _cancelRate;
  private final String[] _stackTypes;
  private final int _negateCount;

  public EffectDispelEffects(Env env, EffectTemplate template)
  {
    super(env, template);
    _dispelType = template.getParam().getString("dispelType", "");
    _cancelRate = template.getParam().getInteger("cancelRate", 0);
    _negateCount = template.getParam().getInteger("negateCount", 5);
    _stackTypes = template.getParam().getString("negateStackTypes", "").split(";");
  }

  public void onStart()
  {
    List _musicList = new ArrayList();
    List _buffList = new ArrayList();

    for (Effect e : _effected.getEffectList().getAllEffects())
    {
      if (_dispelType.equals("cancellation"))
      {
        if ((!e.isOffensive()) && (!e.getSkill().isToggle()) && (e.isCancelable()))
        {
          if (e.getSkill().isMusic())
            _musicList.add(e);
          else
            _buffList.add(e);
        }
      }
      else if (_dispelType.equals("bane"))
      {
        if ((!e.isOffensive()) && ((ArrayUtils.contains(_stackTypes, e.getStackType())) || (ArrayUtils.contains(_stackTypes, e.getStackType2()))) && (e.isCancelable()))
          _buffList.add(e);
      }
      else if (_dispelType.equals("cleanse"))
      {
        if ((e.isOffensive()) && (e.isCancelable())) {
          _buffList.add(e);
        }
      }
    }

    List _effectList = new ArrayList();
    Collections.reverse(_musicList);
    Collections.reverse(_buffList);
    _effectList.addAll(_musicList);
    _effectList.addAll(_buffList);

    if (_effectList.isEmpty()) {
      return;
    }
    double cancel_res_multiplier = _effected.calcStat(Stats.CANCEL_RESIST, 0.0D, null, null);
    int negated = 0;

    for (Effect e : _effectList)
      if (negated < _negateCount)
      {
        double eml = e.getSkill().getMagicLevel();
        double dml = getSkill().getMagicLevel() - (eml == 0.0D ? _effected.getLevel() : eml);
        int buffTime = e.getTimeLeft();
        cancel_res_multiplier = 1.0D - cancel_res_multiplier * 0.01D;
        double prelimChance = (2.0D * dml + _cancelRate + buffTime / 120) * cancel_res_multiplier;

        if (Rnd.chance(calcSkillChanceLimits(prelimChance, _effector.isPlayable())))
        {
          negated++;
          _effected.sendPacket(new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED).addSkillName(e.getSkill().getId(), e.getSkill().getLevel()));
          e.exit();
        }
      }
  }

  private double calcSkillChanceLimits(double prelimChance, boolean isPlayable)
  {
    if (_dispelType.equals("bane"))
    {
      if (prelimChance < 40.0D)
        return 40.0D;
      if (prelimChance > 90.0D)
        return 90.0D;
    }
    else if (_dispelType.equals("cancellation"))
    {
      if (prelimChance < 25.0D)
        return 25.0D;
      if (prelimChance > 75.0D)
        return 75.0D;
    }
    else if (_dispelType.equals("cleanse")) {
      return _cancelRate;
    }return prelimChance;
  }

  protected boolean onActionTime()
  {
    return false;
  }
}