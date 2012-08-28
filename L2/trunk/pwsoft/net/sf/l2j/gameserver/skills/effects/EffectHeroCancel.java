package net.sf.l2j.gameserver.skills.effects;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.util.Rnd;

public class EffectHeroCancel extends L2Effect
{
  public EffectHeroCancel(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.FEAR;
  }

  public void onStart()
  {
    if (Rnd.get(100) < 35)
    {
      onExit();
      return;
    }

    int max = Rnd.get(2, 4);
    int canceled = 0;
    int count = 0;
    int finish = getEffected().getBuffCount();

    FastTable effects = getEffected().getAllEffectsTable();
    int i = 0; for (int n = effects.size(); i < n; i++) {
      L2Effect e = (L2Effect)effects.get(i);
      if (e == null)
      {
        continue;
      }
      if (e.getSkill().isCancelProtected())
      {
        continue;
      }
      if (e.getSkill().isBuff()) {
        if (Rnd.get(100) < 40) {
          e.exit();
          canceled++;
        }

        if ((canceled >= max) || (count >= finish)) {
          onActionTime();
          break;
        }
        count++;
      }
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