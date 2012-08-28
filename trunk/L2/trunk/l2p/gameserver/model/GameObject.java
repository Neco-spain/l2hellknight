package l2p.gameserver.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.base.InvisibleType;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.entity.events.EventOwner;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.scripts.Events;
import l2p.gameserver.serverpackets.DeleteObject;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.templates.CharTemplate;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
import l2p.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GameObject extends EventOwner
{
  public static final Logger _log = LoggerFactory.getLogger(GameObject.class);

  public static final GameObject[] EMPTY_L2OBJECT_ARRAY = new GameObject[0];
  protected static final int CREATED = 0;
  protected static final int VISIBLE = 1;
  protected static final int DELETED = -1;
  protected int objectId;
  private int _x;
  private int _y;
  private int _z;
  protected Reflection _reflection = ReflectionManager.DEFAULT;
  private WorldRegion _currentRegion;
  private final AtomicInteger _state = new AtomicInteger(0);

  protected GameObject()
  {
  }

  public GameObject(int objectId)
  {
    this.objectId = objectId;
  }

  public HardReference<? extends GameObject> getRef()
  {
    return HardReferences.emptyRef();
  }

  private void clearRef()
  {
    HardReference reference = getRef();
    if (reference != null)
      reference.clear();
  }

  public Reflection getReflection()
  {
    return _reflection;
  }

  public int getReflectionId()
  {
    return _reflection.getId();
  }

  public int getGeoIndex()
  {
    return _reflection.getGeoIndex();
  }

  public void setReflection(Reflection reflection)
  {
    if (_reflection == reflection) {
      return;
    }
    boolean respawn = false;
    if (isVisible())
    {
      decayMe();
      respawn = true;
    }

    Reflection r = getReflection();
    if (!r.isDefault())
    {
      r.removeObject(this);
    }

    _reflection = reflection;

    if (!reflection.isDefault())
    {
      reflection.addObject(this);
    }

    if (respawn)
      spawnMe();
  }

  public void setReflection(int reflectionId)
  {
    Reflection r = ReflectionManager.getInstance().get(reflectionId);
    if (r == null)
    {
      Log.debug("Trying to set unavailable reflection: " + reflectionId + " for object: " + this + "!", new Throwable().fillInStackTrace());
      return;
    }

    setReflection(r);
  }

  public final int hashCode()
  {
    return objectId;
  }

  public final int getObjectId()
  {
    return objectId;
  }

  public int getX()
  {
    return _x;
  }

  public int getY()
  {
    return _y;
  }

  public int getZ()
  {
    return _z;
  }

  public Location getLoc()
  {
    return new Location(_x, _y, _z, getHeading());
  }

  public int getGeoZ(Location loc)
  {
    return GeoEngine.getHeight(loc, getGeoIndex());
  }

  public void setLoc(Location loc)
  {
    setXYZ(loc.x, loc.y, loc.z);
  }

  public void setXYZ(int x, int y, int z)
  {
    _x = World.validCoordX(x);
    _y = World.validCoordY(y);
    _z = World.validCoordZ(z);

    World.addVisibleObject(this, null);
  }

  public final boolean isVisible()
  {
    return _state.get() == 1;
  }

  public InvisibleType getInvisibleType()
  {
    return InvisibleType.NONE;
  }

  public final boolean isInvisible()
  {
    return getInvisibleType() != InvisibleType.NONE;
  }

  public void spawnMe(Location loc)
  {
    spawnMe0(loc, null);
  }

  protected void spawnMe0(Location loc, Creature dropper)
  {
    _x = loc.x;
    _y = loc.y;
    _z = getGeoZ(loc);

    spawn0(dropper);
  }

  public final void spawnMe()
  {
    spawn0(null);
  }

  protected void spawn0(Creature dropper)
  {
    if (!_state.compareAndSet(0, 1)) {
      return;
    }
    World.addVisibleObject(this, dropper);

    onSpawn();
  }

  public void toggleVisible()
  {
    if (isVisible())
      decayMe();
    else
      spawnMe();
  }

  protected void onSpawn()
  {
  }

  public final void decayMe()
  {
    if (!_state.compareAndSet(1, 0)) {
      return;
    }
    World.removeVisibleObject(this);
    onDespawn();
  }

  protected void onDespawn()
  {
  }

  public final void deleteMe()
  {
    decayMe();

    if (!_state.compareAndSet(0, -1)) {
      return;
    }
    onDelete();
  }

  public final boolean isDeleted()
  {
    return _state.get() == -1;
  }

  protected void onDelete()
  {
    Reflection r = getReflection();
    if (!r.isDefault()) {
      r.removeObject(this);
    }
    clearRef();
  }

  public void onAction(Player player, boolean shift)
  {
    if (Events.onAction(player, this, shift)) {
      return;
    }
    player.sendActionFailed();
  }

  public void onForcedAttack(Player player, boolean shift)
  {
    player.sendActionFailed();
  }

  public boolean isAttackable(Creature attacker)
  {
    return false;
  }

  public String getL2ClassShortName()
  {
    return getClass().getSimpleName();
  }

  public final long getXYDeltaSq(int x, int y)
  {
    long dx = x - getX();
    long dy = y - getY();
    return dx * dx + dy * dy;
  }

  public final long getXYDeltaSq(Location loc)
  {
    return getXYDeltaSq(loc.x, loc.y);
  }

  public final long getZDeltaSq(int z)
  {
    long dz = z - getZ();
    return dz * dz;
  }

  public final long getZDeltaSq(Location loc)
  {
    return getZDeltaSq(loc.z);
  }

  public final long getXYZDeltaSq(int x, int y, int z)
  {
    return getXYDeltaSq(x, y) + getZDeltaSq(z);
  }

  public final long getXYZDeltaSq(Location loc)
  {
    return getXYDeltaSq(loc.x, loc.y) + getZDeltaSq(loc.z);
  }

  public final double getDistance(int x, int y)
  {
    return Math.sqrt(getXYDeltaSq(x, y));
  }

  public final double getDistance(int x, int y, int z)
  {
    return Math.sqrt(getXYZDeltaSq(x, y, z));
  }

  public final double getDistance(Location loc)
  {
    return getDistance(loc.x, loc.y, loc.z);
  }

  public final boolean isInRange(GameObject obj, long range)
  {
    if (obj == null)
      return false;
    if (obj.getReflection() != getReflection())
      return false;
    long dx = Math.abs(obj.getX() - getX());
    if (dx > range)
      return false;
    long dy = Math.abs(obj.getY() - getY());
    if (dy > range)
      return false;
    long dz = Math.abs(obj.getZ() - getZ());
    return (dz <= 1500L) && (dx * dx + dy * dy <= range * range);
  }

  public final boolean isInRangeZ(GameObject obj, long range)
  {
    if (obj == null)
      return false;
    if (obj.getReflection() != getReflection())
      return false;
    long dx = Math.abs(obj.getX() - getX());
    if (dx > range)
      return false;
    long dy = Math.abs(obj.getY() - getY());
    if (dy > range)
      return false;
    long dz = Math.abs(obj.getZ() - getZ());
    return (dz <= range) && (dx * dx + dy * dy + dz * dz <= range * range);
  }

  public final boolean isInRange(Location loc, long range)
  {
    return isInRangeSq(loc, range * range);
  }

  public final boolean isInRangeSq(Location loc, long range)
  {
    return getXYDeltaSq(loc) <= range;
  }

  public final boolean isInRangeZ(Location loc, long range)
  {
    return isInRangeZSq(loc, range * range);
  }

  public final boolean isInRangeZSq(Location loc, long range)
  {
    return getXYZDeltaSq(loc) <= range;
  }

  public final double getDistance(GameObject obj)
  {
    if (obj == null)
      return 0.0D;
    return Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
  }

  public final double getDistance3D(GameObject obj)
  {
    if (obj == null)
      return 0.0D;
    return Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
  }

  public final double getRealDistance(GameObject obj)
  {
    return getRealDistance3D(obj, true);
  }

  public final double getRealDistance3D(GameObject obj)
  {
    return getRealDistance3D(obj, false);
  }

  public final double getRealDistance3D(GameObject obj, boolean ignoreZ)
  {
    double distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
    if (isCreature())
      distance -= ((Creature)this).getTemplate().collisionRadius;
    if (obj.isCreature())
      distance -= ((Creature)obj).getTemplate().collisionRadius;
    return distance > 0.0D ? distance : 0.0D;
  }

  public final long getSqDistance(int x, int y)
  {
    return getXYDeltaSq(x, y);
  }

  public final long getSqDistance(GameObject obj)
  {
    if (obj == null)
      return 0L;
    return getXYDeltaSq(obj.getLoc());
  }

  public Player getPlayer()
  {
    return null;
  }

  public int getHeading()
  {
    return 0;
  }

  public int getMoveSpeed()
  {
    return 0;
  }

  public WorldRegion getCurrentRegion()
  {
    return _currentRegion;
  }

  public void setCurrentRegion(WorldRegion region)
  {
    _currentRegion = region;
  }

  public boolean isInObserverMode()
  {
    return false;
  }

  public boolean isInOlympiadMode()
  {
    return false;
  }

  public boolean isInBoat()
  {
    return false;
  }

  public boolean isFlying()
  {
    return false;
  }

  public double getColRadius()
  {
    _log.warn("getColRadius called directly from L2Object");
    Thread.dumpStack();
    return 0.0D;
  }

  public double getColHeight()
  {
    _log.warn("getColHeight called directly from L2Object");
    Thread.dumpStack();
    return 0.0D;
  }

  public boolean isCreature()
  {
    return false;
  }

  public boolean isPlayable()
  {
    return false;
  }

  public boolean isPlayer()
  {
    return false;
  }

  public boolean isPet()
  {
    return false;
  }

  public boolean isSummon()
  {
    return false;
  }

  public boolean isNpc()
  {
    return false;
  }

  public boolean isMonster()
  {
    return false;
  }

  public boolean isItem()
  {
    return false;
  }

  public boolean isRaid()
  {
    return false;
  }

  public boolean isBoss()
  {
    return false;
  }

  public boolean isTrap()
  {
    return false;
  }

  public boolean isDoor()
  {
    return false;
  }

  public boolean isArtefact()
  {
    return false;
  }

  public boolean isSiegeGuard()
  {
    return false;
  }

  public boolean isClanAirShip()
  {
    return false;
  }

  public boolean isAirShip()
  {
    return false;
  }

  public boolean isBoat()
  {
    return false;
  }

  public boolean isVehicle()
  {
    return false;
  }

  public boolean isMinion()
  {
    return false;
  }

  public String getName()
  {
    return getClass().getSimpleName() + ":" + objectId;
  }

  public String dump()
  {
    return dump(true);
  }

  public String dump(boolean simpleTypes)
  {
    return Util.dumpObject(this, simpleTypes, true, true);
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    return Collections.emptyList();
  }

  public List<L2GameServerPacket> deletePacketList()
  {
    return Collections.singletonList(new DeleteObject(this));
  }

  public void addEvent(GlobalEvent event)
  {
    event.onAddEvent(this);

    super.addEvent(event);
  }

  public void removeEvent(GlobalEvent event)
  {
    event.onRemoveEvent(this);

    super.removeEvent(event);
  }

  public boolean equals(Object obj)
  {
    if (obj == this)
      return true;
    if (obj == null)
      return false;
    if (obj.getClass() != getClass())
      return false;
    return ((GameObject)obj).getObjectId() == getObjectId();
  }
}