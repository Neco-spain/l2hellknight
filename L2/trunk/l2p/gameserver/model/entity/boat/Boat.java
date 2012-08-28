package l2p.gameserver.model.entity.boat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2p.gameserver.ai.BoatAI;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.entity.events.impl.BoatWayEvent;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.templates.item.WeaponTemplate;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.PositionUtils;

public abstract class Boat extends Creature
{
  private int _moveSpeed;
  private int _rotationSpeed;
  protected int _fromHome;
  protected int _runState;
  private final BoatWayEvent[] _ways = new BoatWayEvent[2];
  protected final Set<Player> _players = new CopyOnWriteArraySet();

  public Boat(int objectId, CharTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    _fromHome = 1;

    getCurrentWay().reCalcNextTime(false);
  }

  public void setXYZ(int x, int y, int z, boolean MoveTask)
  {
    super.setXYZ(x, y, z, MoveTask);

    updatePeopleInTheBoat(x, y, z);
  }

  public void onEvtArrived()
  {
    getCurrentWay().moveNext();
  }

  protected void updatePeopleInTheBoat(int x, int y, int z)
  {
    for (Player player : _players)
    {
      if (player != null)
        player.setXYZ(x, y, z, true);
    }
  }

  public void addPlayer(Player player, Location boatLoc)
  {
    synchronized (_players)
    {
      _players.add(player);

      player.setBoat(this);
      player.setLoc(getLoc(), true);
      player.setInBoatPosition(boatLoc);
      player.broadcastPacket(new L2GameServerPacket[] { getOnPacket(player, boatLoc) });
    }
  }

  public void moveInBoat(Player player, Location ori, Location loc)
  {
    if (player.getPet() != null)
    {
      player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN, ActionFail.STATIC });
      return;
    }

    if (player.getTransformation() != 0)
    {
      player.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_BOARD_A_SHIP_WHILE_YOU_ARE_POLYMORPHED, ActionFail.STATIC });
      return;
    }

    if ((player.isMovementDisabled()) || (player.isSitting()))
    {
      player.sendActionFailed();
      return;
    }

    if (!player.isInBoat()) {
      player.setBoat(this);
    }
    loc.h = PositionUtils.getHeadingTo(ori, loc);
    player.setInBoatPosition(loc);
    player.broadcastPacket(new L2GameServerPacket[] { inMovePacket(player, ori, loc) });
  }

  public void trajetEnded(boolean oust)
  {
    _runState = 0;
    _fromHome = (_fromHome == 1 ? 0 : 1);

    L2GameServerPacket checkLocation = checkLocationPacket();
    if (checkLocation != null) {
      broadcastPacket(new L2GameServerPacket[] { infoPacket(), checkLocation });
    }
    if (oust)
    {
      oustPlayers();
      getCurrentWay().reCalcNextTime(false);
    }
  }

  public void teleportShip(int x, int y, int z)
  {
    if (isMoving) {
      stopMove(false);
    }
    for (Player player : _players) {
      player.teleToLocation(x, y, z);
    }
    setHeading(calcHeading(x, y));

    setXYZ(x, y, z, true);

    getCurrentWay().moveNext();
  }

  public void oustPlayer(Player player, Location loc, boolean teleport)
  {
    synchronized (_players)
    {
      player._stablePoint = null;

      player.setBoat(null);
      player.setInBoatPosition(null);
      player.broadcastPacket(new L2GameServerPacket[] { getOffPacket(player, loc) });

      if (teleport) {
        player.teleToLocation(loc);
      }
      _players.remove(player);
    }
  }

  public void removePlayer(Player player)
  {
    synchronized (_players)
    {
      _players.remove(player);
    }
  }

  public void broadcastPacketToPassengers(IStaticPacket packet)
  {
    for (Player player : _players)
      player.sendPacket(packet);  } 
  public abstract L2GameServerPacket infoPacket();

  public abstract L2GameServerPacket movePacket();

  public abstract L2GameServerPacket inMovePacket(Player paramPlayer, Location paramLocation1, Location paramLocation2);

  public abstract L2GameServerPacket stopMovePacket();

  public abstract L2GameServerPacket inStopMovePacket(Player paramPlayer);

  public abstract L2GameServerPacket startPacket();

  public abstract L2GameServerPacket validateLocationPacket(Player paramPlayer);

  public abstract L2GameServerPacket checkLocationPacket();

  public abstract L2GameServerPacket getOnPacket(Player paramPlayer, Location paramLocation);

  public abstract L2GameServerPacket getOffPacket(Player paramPlayer, Location paramLocation);

  public abstract void oustPlayers();

  public CharacterAI getAI() { if (_ai == null) {
      _ai = new BoatAI(this);
    }
    return _ai;
  }

  public void broadcastCharInfo()
  {
    broadcastPacket(new L2GameServerPacket[] { infoPacket() });
  }

  public void broadcastPacket(L2GameServerPacket[] packets)
  {
    List players = new ArrayList();
    players.addAll(_players);
    players.addAll(World.getAroundPlayers(this));

    for (Player player : players)
    {
      if (player != null)
        player.sendPacket(packets);
    }
  }

  public void validateLocation(int broadcast)
  {
  }

  public void sendChanges()
  {
  }

  public int getMoveSpeed()
  {
    return _moveSpeed;
  }

  public int getRunSpeed()
  {
    return _moveSpeed;
  }

  public ItemInstance getActiveWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getActiveWeaponItem()
  {
    return null;
  }

  public ItemInstance getSecondaryWeaponInstance()
  {
    return null;
  }

  public WeaponTemplate getSecondaryWeaponItem()
  {
    return null;
  }

  public int getLevel()
  {
    return 0;
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return false;
  }

  public int getRunState()
  {
    return _runState;
  }

  public void setRunState(int runState)
  {
    _runState = runState;
  }

  public void setMoveSpeed(int moveSpeed)
  {
    _moveSpeed = moveSpeed;
  }

  public void setRotationSpeed(int rotationSpeed)
  {
    _rotationSpeed = rotationSpeed;
  }

  public int getRotationSpeed()
  {
    return _rotationSpeed;
  }

  public BoatWayEvent getCurrentWay()
  {
    return _ways[_fromHome];
  }

  public void setWay(int id, BoatWayEvent v)
  {
    _ways[id] = v;
  }

  public Set<Player> getPlayers()
  {
    return _players;
  }

  public boolean isDocked()
  {
    return _runState == 0;
  }

  public Location getReturnLoc()
  {
    return getCurrentWay().getReturnLoc();
  }

  public boolean isBoat()
  {
    return true;
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    if (!isMoving)
    {
      return Collections.singletonList(infoPacket());
    }

    List list = new ArrayList(2);
    list.add(infoPacket());
    list.add(movePacket());
    return list;
  }
}