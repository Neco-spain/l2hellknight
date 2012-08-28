package l2m.gameserver.model.instances;

import java.util.Collections;
import java.util.List;
import l2p.commons.lang.reference.HardReference;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.reference.L2Reference;
import l2m.gameserver.scripts.Events;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MyTargetSelected;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.ShowTownMap;
import l2m.gameserver.network.serverpackets.StaticObject;
import l2m.gameserver.templates.StaticObjectTemplate;
import l2m.gameserver.utils.Location;

public class StaticObjectInstance extends GameObject
{
  public static final long serialVersionUID = 1L;
  private final HardReference<StaticObjectInstance> reference;
  private final StaticObjectTemplate _template;
  private int _meshIndex;

  public StaticObjectInstance(int objectId, StaticObjectTemplate template)
  {
    super(objectId);

    _template = template;
    reference = new L2Reference(this);
  }

  public HardReference<StaticObjectInstance> getRef()
  {
    return reference;
  }

  public int getUId()
  {
    return _template.getUId();
  }

  public int getType()
  {
    return _template.getType();
  }

  public void onAction(Player player, boolean shift)
  {
    if (Events.onAction(player, this, shift)) {
      return;
    }
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      return;
    }

    MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
    player.sendPacket(my);

    if (!isInRange(player, 150L))
    {
      if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
      return;
    }

    if (_template.getType() == 0) {
      player.sendPacket(new NpcHtmlMessage(player, getUId(), "newspaper/arena.htm", 0));
    } else if (_template.getType() == 2)
    {
      player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
      player.sendActionFailed();
    }
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    return Collections.singletonList(new StaticObject(this));
  }

  public boolean isAttackable(Creature attacker)
  {
    return false;
  }

  public void broadcastInfo(boolean force)
  {
    StaticObject p = new StaticObject(this);
    for (Player player : World.getAroundPlayers(this))
      player.sendPacket(p);
  }

  public int getGeoZ(Location loc)
  {
    return loc.z;
  }

  public int getMeshIndex()
  {
    return _meshIndex;
  }

  public void setMeshIndex(int meshIndex)
  {
    _meshIndex = meshIndex;
  }
}