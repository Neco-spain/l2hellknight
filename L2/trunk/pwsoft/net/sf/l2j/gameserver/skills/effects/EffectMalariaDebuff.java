package net.sf.l2j.gameserver.skills.effects;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

public class EffectMalariaDebuff extends L2Effect
{
  public EffectMalariaDebuff(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.MUTE;
  }

  public void onStart()
  {
    if (getEffected().isPlayer())
    {
      getEffected().startAbnormalEffect(8192);

      FastTable effects = getEffected().getAllEffectsTable();
      int i = 0; for (int n = effects.size(); i < n; i++)
      {
        L2Effect e = (L2Effect)effects.get(i);
        if (e == null) {
          continue;
        }
        if ((e.getSkill().getId() == 4554) || (e.getSkill().getId() == 4552))
          e.exit();
      }
    }
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
    getEffected().stopAbnormalEffect(8192);
  }
}