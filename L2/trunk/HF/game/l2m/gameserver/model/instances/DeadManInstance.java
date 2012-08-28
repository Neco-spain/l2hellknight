package l2m.gameserver.model.instances;

import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.Die;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.templates.npc.NpcTemplate;

public class DeadManInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public DeadManInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
    setAI(new CharacterAI(this));
  }

  protected void onSpawn()
  {
    super.onSpawn();
    setCurrentHp(0.0D, false);
    broadcastPacket(new L2GameServerPacket[] { new Die(this) });
    setWalking();
  }

  public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect, boolean transferDamage, boolean isDot, boolean sendMessage)
  {
  }

  public boolean isInvul()
  {
    return true;
  }

  public boolean isBlocked()
  {
    return true;
  }
}