package l2p.gameserver.skills.effects;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.stats.Env;

public class EffectRelax extends Effect
{
  private boolean _isWereSitting;

  public EffectRelax(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    Player player = _effected.getPlayer();
    if (player == null)
      return false;
    if (player.isMounted())
    {
      player.sendPacket(new SystemMessage(113).addSkillName(_skill.getId(), _skill.getLevel()));
      return false;
    }
    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    Player player = _effected.getPlayer();
    if (player.isMoving)
      player.stopMove();
    _isWereSitting = player.isSitting();
    player.sitDown(null);
  }

  public void onExit()
  {
    super.onExit();
    if (!_isWereSitting)
      _effected.getPlayer().standUp();
  }

  public boolean onActionTime()
  {
    Player player = _effected.getPlayer();
    if ((player.isAlikeDead()) || (player == null)) {
      return false;
    }
    if (!player.isSitting()) {
      return false;
    }
    if ((player.isCurrentHpFull()) && (getSkill().isToggle()))
    {
      getEffected().sendPacket(Msg.HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED);
      return false;
    }

    double manaDam = calc();
    if ((manaDam > _effected.getCurrentMp()) && 
      (getSkill().isToggle()))
    {
      player.sendPacket(new IStaticPacket[] { Msg.NOT_ENOUGH_MP, new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()) });
      return false;
    }

    _effected.reduceCurrentMp(manaDam, null);

    return true;
  }
}