package l2m.gameserver.skills.effects;

import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.ChangeWaitType;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.Revive;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Env;

public final class EffectFakeDeath extends Effect
{
  public EffectFakeDeath(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();

    Player player = (Player)getEffected();
    player.setFakeDeath(true);
    player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
    player.broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(player, 2) });
    player.broadcastCharInfo();
  }

  public void onExit()
  {
    super.onExit();

    Player player = (Player)getEffected();
    player.setNonAggroTime(System.currentTimeMillis() + 5000L);
    player.setFakeDeath(false);
    player.broadcastPacket(new L2GameServerPacket[] { new ChangeWaitType(player, 3) });
    player.broadcastPacket(new L2GameServerPacket[] { new Revive(player) });
    player.broadcastCharInfo();
  }

  public boolean onActionTime()
  {
    if (getEffected().isDead()) {
      return false;
    }
    double manaDam = calc();

    if ((manaDam > getEffected().getCurrentMp()) && 
      (getSkill().isToggle()))
    {
      getEffected().sendPacket(Msg.NOT_ENOUGH_MP);
      getEffected().sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
      return false;
    }

    getEffected().reduceCurrentMp(manaDam, null);
    return true;
  }
}