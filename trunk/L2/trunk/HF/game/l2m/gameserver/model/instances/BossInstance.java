package l2m.gameserver.model.instances;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.Hero;
import l2m.gameserver.templates.npc.NpcTemplate;

public class BossInstance extends RaidBossInstance
{
  public static final long serialVersionUID = 1L;
  private boolean _teleportedToNest;

  public BossInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isBoss()
  {
    return true;
  }

  public final boolean isMovementDisabled()
  {
    return (getNpcId() == 29006) || (super.isMovementDisabled());
  }

  protected void onDeath(Creature killer)
  {
    if (killer.isPlayable())
    {
      Player player = killer.getPlayer();
      if (player.isInParty())
      {
        for (Player member : player.getParty().getPartyMembers())
          if (member.isNoble())
            Hero.getInstance().addHeroDiary(member.getObjectId(), 1, getNpcId());
      }
      else if (player.isNoble())
        Hero.getInstance().addHeroDiary(player.getObjectId(), 1, getNpcId());
    }
    super.onDeath(killer);
  }

  public void setTeleported(boolean flag)
  {
    _teleportedToNest = flag;
  }

  public boolean isTeleported()
  {
    return _teleportedToNest;
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }
}