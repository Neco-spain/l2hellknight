package net.sf.l2j.gameserver.skills.effects;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSignetNoise extends L2Effect
{
  public EffectSignetNoise(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SIGNET_GROUND;
  }

  public void onStart()
  {
    FastTable effects = getEffected().getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++)
    {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null) {
        continue;
      }
      if (e.getSkill().isDance())
        e.exit();
    }
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
  }
}