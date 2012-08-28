package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectSeed;

public class ConditionElementSeed extends Condition
{
  private static int[] seedSkills = { 1285, 1286, 1287 };
  private final int[] _requiredSeeds;

  public ConditionElementSeed(int[] seeds)
  {
    _requiredSeeds = seeds;
  }

  ConditionElementSeed(int fire, int water, int wind, int various, int any)
  {
    _requiredSeeds = new int[5];
    _requiredSeeds[0] = fire;
    _requiredSeeds[1] = water;
    _requiredSeeds[2] = wind;
    _requiredSeeds[3] = various;
    _requiredSeeds[4] = any;
  }

  public boolean testImpl(Env env)
  {
    int[] Seeds = new int[3];
    for (int i = 0; i < Seeds.length; i++)
    {
      Seeds[i] = ((env.cha.getFirstEffect(seedSkills[i]) instanceof EffectSeed) ? ((EffectSeed)env.cha.getFirstEffect(seedSkills[i])).getPower() : 0);
      if (Seeds[i] >= _requiredSeeds[i])
        Seeds[i] -= _requiredSeeds[i];
      else return false;

    }

    if (_requiredSeeds[3] > 0)
    {
      int count = 0;
      for (int i = 0; (i < Seeds.length) && (count < _requiredSeeds[3]); i++)
      {
        if (Seeds[i] <= 0)
          continue;
        Seeds[i] -= 1;
        count++;
      }

      if (count < _requiredSeeds[3]) return false;
    }

    if (_requiredSeeds[4] > 0)
    {
      int count = 0;
      for (int i = 0; (i < Seeds.length) && (count < _requiredSeeds[4]); i++)
      {
        count += Seeds[i];
      }
      if (count < _requiredSeeds[4]) return false;
    }

    return true;
  }
}