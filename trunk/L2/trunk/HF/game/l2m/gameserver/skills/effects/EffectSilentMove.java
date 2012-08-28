package l2m.gameserver.skills.effects;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;

public final class EffectSilentMove extends Effect
{
  public EffectSilentMove(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isPlayable())
      ((Playable)_effected).startSilentMoving();
  }

  public void onExit()
  {
    super.onExit();
    if (_effected.isPlayable())
      ((Playable)_effected).stopSilentMoving();
  }

  public boolean onActionTime()
  {
    if (_effected.isDead()) {
      return false;
    }
    if (!getSkill().isToggle()) {
      return false;
    }
    double manaDam = calc();
    if (manaDam > _effected.getCurrentMp())
    {
      _effected.sendPacket(Msg.NOT_ENOUGH_MP);
      _effected.sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
      return false;
    }

    _effected.reduceCurrentMp(manaDam, null);
    return true;
  }
}