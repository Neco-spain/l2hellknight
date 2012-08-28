package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Skill;
import l2p.gameserver.stats.Env;

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