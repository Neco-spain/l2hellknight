package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.Env;

public class EffectMute extends Effect
{
  public EffectMute(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    if (!_effected.startMuted())
    {
      Skill castingSkill = _effected.getCastingSkill();
      if ((castingSkill != null) && (castingSkill.isMagic()))
        _effected.abortCast(true, true);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopMuted();
  }
}