package net.sf.l2j.gameserver.model;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.GetItem;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.util.PeaceZone;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Point3D;

public abstract class L2Object
{
  private boolean _isVisible;
  private ObjectKnownList _knownList;
  private String _name;
  private int _objectId;
  private ObjectPoly _poly;
  private ObjectPosition _position;
  private int _x;
  private int _y;
  private int _z;

  public L2Object(int objectId)
  {
    _objectId = objectId;
  }

  public void onAction(L2PcInstance player)
  {
    player.sendActionFailed();
  }

  public void onActionShift(L2GameClient client) {
    client.getActiveChar().sendActionFailed();
  }

  public void onForcedAttack(L2PcInstance player) {
    player.sendActionFailed();
  }

  public Location getLoc()
  {
    return new Location(getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
  }

  public void setLoc(Location loc)
  {
    setXYZ(loc.x, loc.y, loc.z);
  }

  public void onSpawn()
  {
  }

  public final void setXYZ(int x, int y, int z)
  {
    getPosition().setXYZ(x, y, z);
  }

  public final void setXYZInvisible(int x, int y, int z) {
    getPosition().setXYZInvisible(x, y, z);
  }

  public final int getX() {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();

    return getPosition().getX();
  }

  public final int getY() {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();

    return getPosition().getY();
  }

  public final int getZ() {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();

    return getPosition().getZ();
  }

  public int getHeading() {
    return getPosition().getHeading();
  }

  public float getMoveSpeed() {
    return 0.0F;
  }

  public final void decayMe()
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() == null)) throw new AssertionError();

    L2WorldRegion reg = getPosition().getWorldRegion();

    synchronized (this) {
      _isVisible = false;
      getPosition().setWorldRegion(null);
    }

    L2World.getInstance().removeVisibleObject(this, reg);
    L2World.getInstance().removeObject(this);
    if (Config.SAVE_DROPPED_ITEM)
      ItemsOnGroundManager.getInstance().removeObject(this);
  }

  public final void pickupMe(L2Character player)
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (!isL2Item())) throw new AssertionError();

    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() == null)) throw new AssertionError();

    L2WorldRegion oldregion = getPosition().getWorldRegion();

    player.broadcastPacket(new GetItem((L2ItemInstance)this, player.getObjectId()));

    synchronized (this) {
      _isVisible = false;
      getPosition().setWorldRegion(null);
    }

    if (isL2Item()) {
      int itemId = ((L2ItemInstance)this).getItemId();
      if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0) {
        MercTicketManager.getInstance().removeTicket((L2ItemInstance)this);
        ItemsOnGroundManager.getInstance().removeObject(this);
      }

    }

    L2World.getInstance().removeVisibleObject(this, oldregion);
  }

  public void refreshID() {
    L2World.getInstance().removeObject(this);
    IdFactory.getInstance().releaseId(getObjectId());
    _objectId = IdFactory.getInstance().getNextId();
  }

  public final void spawnMe()
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (
      (getPosition().getWorldRegion() != null) || (getPosition().getWorldPosition().getX() == 0) || (getPosition().getWorldPosition().getY() == 0) || (getPosition().getWorldPosition().getZ() == 0))) throw new AssertionError();

    synchronized (this)
    {
      _isVisible = true;
      getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

      L2World.getInstance().storeObject(this);

      getPosition().getWorldRegion().addVisibleObject(this);
    }

    L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);

    onSpawn();
  }

  public final void spawnMe(int x, int y, int z) {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() != null)) throw new AssertionError();

    synchronized (this)
    {
      _isVisible = true;

      if (x > L2World.MAP_MAX_X) {
        x = L2World.MAP_MAX_X - 5000;
      }
      if (x < L2World.MAP_MIN_X) {
        x = L2World.MAP_MIN_X + 5000;
      }
      if (y > L2World.MAP_MAX_Y) {
        y = L2World.MAP_MAX_Y - 5000;
      }
      if (y < L2World.MAP_MIN_Y) {
        y = L2World.MAP_MIN_Y + 5000;
      }

      getPosition().setWorldPosition(x, y, z);
      getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

      L2World.getInstance().storeObject(this);

      getPosition().getWorldRegion().addVisibleObject(this);
    }

    L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);

    onSpawn();
  }

  public void toggleVisible() {
    if (isVisible())
      decayMe();
    else
      spawnMe();
  }

  public boolean isAttackable()
  {
    return false;
  }
  public abstract boolean isAutoAttackable(L2Character paramL2Character);

  public boolean isMarker() {
    return false;
  }

  public final boolean isVisible()
  {
    return getPosition().getWorldRegion() != null;
  }

  public final void setIsVisible(boolean value) {
    _isVisible = value;
    if (!_isVisible)
      getPosition().setWorldRegion(null);
  }

  public ObjectKnownList getKnownList()
  {
    if (_knownList == null) {
      _knownList = new ObjectKnownList(this);
    }
    return _knownList;
  }

  public final void setKnownList(ObjectKnownList value) {
    _knownList = value;
  }

  public String getName() {
    return _name;
  }

  public final void setName(String value) {
    _name = value;
  }

  public final int getObjectId() {
    return _objectId;
  }

  public final ObjectPoly getPoly() {
    if (_poly == null) {
      _poly = new ObjectPoly(this);
    }
    return _poly;
  }

  public final ObjectPosition getPosition() {
    if (_position == null) {
      _position = new ObjectPosition(this);
    }
    return _position;
  }

  public L2WorldRegion getWorldRegion()
  {
    return getPosition().getWorldRegion();
  }

  public String toString()
  {
    return "" + getObjectId();
  }

  public double getDistance(int x, int y)
  {
    double dx = x - getX();
    double dy = y - getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  public double getDistance(int x, int y, int z) {
    double dx = x - getX();
    double dy = y - getY();
    double dz = z - getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public boolean isMonster()
  {
    if (isL2Attackable()) {
      return true;
    }

    return isL2ControlTower();
  }

  public boolean isPlayer()
  {
    return false;
  }

  public boolean isPet() {
    return false;
  }

  public boolean isSummon() {
    return false;
  }

  public boolean isL2Artefact() {
    return false;
  }

  public boolean isL2Attackable() {
    return false;
  }

  public boolean isL2Monster() {
    return false;
  }

  public boolean isL2Chest() {
    return false;
  }

  public boolean isL2Door() {
    return false;
  }

  public boolean isL2Folk() {
    return false;
  }

  public boolean isL2Summon() {
    return false;
  }

  public boolean isL2SiegeGuard() {
    return false;
  }

  public boolean isL2VillageMaster() {
    return false;
  }

  public boolean isL2RiftInvader() {
    return false;
  }

  public boolean isL2Guard() {
    return false;
  }

  public boolean isL2FriendlyMob() {
    return false;
  }

  public boolean isL2Npc() {
    return false;
  }

  public boolean isL2Penalty() {
    return false;
  }

  public boolean isL2Playable() {
    return false;
  }

  public boolean isL2FestivalMonster() {
    return false;
  }

  public boolean isL2ControlTower() {
    return false;
  }

  public boolean isL2Character() {
    return false;
  }

  public boolean isL2Item() {
    return false;
  }

  public boolean isL2NpcWalker() {
    return false;
  }

  public final boolean isInZonePeace()
  {
    return PeaceZone.getInstance().inPeace(this);
  }

  public final void setInZonePeace(boolean flag)
  {
  }

  public void sendPacket(L2GameServerPacket mov)
  {
  }

  public void sendUserPacket(L2GameServerPacket mov)
  {
  }

  public void sendActionFailed()
  {
    sendPacket(Static.ActionFailed);
  }

  public boolean isInsideSilenceZone() {
    return false;
  }

  public boolean isInsideAqZone() {
    return false;
  }

  public void setInCastleWaitZone(boolean f) {
  }

  public final boolean isInsideCastleWaitZone() {
    return false;
  }

  public boolean isInJail() {
    return false;
  }

  public boolean equals(L2Object obj) {
    return this == obj;
  }

  public void setShowSpawnAnimation(int value) {
  }

  public boolean canSeeTarget(L2Object trg) {
    return GeoData.getInstance().canSeeTarget(this, trg);
  }

  public boolean canMoveFromToTarget(int x, int y, int z, int tx, int ty, int tz)
  {
    return GeoData.getInstance().canMoveFromToTarget(x, y, z, tx, ty, tz);
  }

  public boolean isOlympiadStart() {
    return false;
  }

  public boolean isInOlympiadMode() {
    return false;
  }

  public int getOlympiadGameId() {
    return -1;
  }

  public int getChannel() {
    return 1;
  }

  public void notifySkillUse(L2PcInstance caster, L2Skill skill)
  {
  }

  public boolean getProtectionBlessing() {
    return false;
  }

  public int getKarma() {
    return 0;
  }

  public byte getPvpFlag() {
    return 0;
  }

  public int getLevel() {
    return 1;
  }

  public boolean isInsidePvpZone() {
    return false;
  }

  public L2PcInstance getOwner() {
    return null;
  }

  public int getNpcId() {
    return 0;
  }

  public void removeStatusListener(L2Character object)
  {
  }

  public void addStatusListener(L2Character object)
  {
  }

  public void olympiadClear()
  {
  }

  public void getEffect(int id, int lvl)
  {
  }

  public L2PcInstance getPlayer() {
    return null;
  }

  public L2Character getL2Character() {
    return null;
  }

  public boolean showSoulShotsAnim() {
    return Config.SOULSHOT_ANIM;
  }

  public void kick() {
  }

  public boolean isAlikeDead() {
    return false;
  }
}