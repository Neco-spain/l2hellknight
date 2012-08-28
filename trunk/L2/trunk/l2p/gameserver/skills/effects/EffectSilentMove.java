package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.stats.Env;

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