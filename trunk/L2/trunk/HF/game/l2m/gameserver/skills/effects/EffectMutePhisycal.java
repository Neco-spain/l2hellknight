package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.Env;

public class EffectMutePhisycal extends Effect
{
  public EffectMutePhisycal(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (!_effected.startPMuted())
    {
      Skill castingSkill = _effected.getCastingSkill();
      if ((castingSkill != null) && (!castingSkill.isMagic()))
        _effected.abortCast(true, true);
    }
  }

  public void onExit()
  {
    super.onExit();
    _effected.stopPMuted();
  }

  public boolean onActionTime()
  {
    return false;
  }
}