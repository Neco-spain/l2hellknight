package net.sf.l2j.gameserver.model;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.GetItem;
import net.sf.l2j.util.Point3D;

public abstract class L2Object
{
  private boolean _isVisible;
  private ObjectKnownList _knownList;
  private String _name;
  private int _objectId;
  private ObjectPoly _poly;
  private ObjectPosition _position;

  public L2Object(int objectId)
  {
    _objectId = objectId;
  }

  public void onAction(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
  }

  public void onActionShift(L2GameClient client)
  {
    client.getActiveChar().sendPacket(new ActionFailed());
  }

  public void onForcedAttack(L2PcInstance player)
  {
    player.sendPacket(new ActionFailed());
  }

  public void onSpawn()
  {
  }

  public void firstSpawn()
  {
    onSpawn();
  }

  public final void setXYZ(int x, int y, int z)
  {
    getPosition().setXYZ(x, y, z);
  }

  public final void setXYZInvisible(int x, int y, int z)
  {
    getPosition().setXYZInvisible(x, y, z);
  }

  public final int getX()
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();
    return getPosition().getX();
  }

  public final int getY()
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();
    return getPosition().getY();
  }

  public final int getZ()
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getPosition().getWorldRegion() == null) && (!_isVisible)) throw new AssertionError();
    return getPosition().getZ();
  }

  public final void decayMe()
  {
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getPosition().getWorldRegion() == null)) throw new AssertionError();

    L2WorldRegion reg = getPosition().getWorldRegion();

    synchronized (this)
    {
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
    if ((Config.ASSERT) && (!$assertionsDisabled) && (!(this instanceof L2ItemInstance))) throw new AssertionError();
    if ((Config.ASSERT) && (!$assertionsDisabled) && (getPosition().getWorldRegion() == null)) throw new AssertionError();

    L2WorldRegion oldregion = getPosition().getWorldRegion();

    GetItem gi = new GetItem((L2ItemInstance)this, player.getObjectId());
    player.broadcastPacket(gi);

    synchronized (this)
    {
      _isVisible = false;
      getPosition().setWorldRegion(null);
    }

    if ((this instanceof L2ItemInstance))
    {
      int itemId = ((L2ItemInstance)this).getItemId();
      if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
      {
        MercTicketManager.getInstance().removeTicket((L2ItemInstance)this);
        ItemsOnGroundManager.getInstance().removeObject(this);
      }

      if ((itemId == 57) || (itemId == 6353))
      {
        QuestState qs = null;
        if ((player instanceof L2Summon))
        {
          qs = ((L2Summon)player).getOwner().getQuestState("255_Tutorial");
          if (qs != null)
            qs.getQuest().notifyEvent("CE" + itemId + "", null, ((L2Summon)player).getOwner());
        }
        else if ((player instanceof L2PcInstance))
        {
          qs = ((L2PcInstance)player).getQuestState("255_Tutorial");
          if (qs != null) {
            qs.getQuest().notifyEvent("CE" + itemId + "", null, (L2PcInstance)player);
          }

        }

      }

    }

    L2World.getInstance().removeVisibleObject(this, oldregion);
  }

  public void refreshID()
  {
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
    }

    L2World.getInstance().storeObject(this);

    getPosition().getWorldRegion().addVisibleObject(this);

    L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);

    onSpawn();
  }

  public final void spawnMe(int x, int y, int z)
  {
    spawnMe(x, y, z, false);
  }

  public final void spawnMe(int x, int y, int z, boolean firstspawn)
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (getPosition().getWorldRegion() != null)) throw new AssertionError();

    synchronized (this)
    {
      _isVisible = true;

      if (x > 228608)
        x = 223608;
      if (x < -131072)
        x = -126072;
      if (y > 262144)
        y = 257144;
      if (y < -262144) {
        y = -257144;
      }
      getPosition().setWorldPosition(x, y, z);
      getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
    }

    L2World.getInstance().storeObject(this);

    getPosition().getWorldRegion().addVisibleObject(this);

    L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);
    if (firstspawn)
      firstSpawn();
    else
      onSpawn();
  }

  public void toggleVisible()
  {
    if (isVisible())
      decayMe();
    else
      spawnMe();
  }

  public boolean isAttackable()
  {
    return false;
  }

  public boolean isPlayable()
  {
    return this instanceof L2PlayableInstance;
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
    if (!_isVisible) getPosition().setWorldRegion(null);
  }

  public ObjectKnownList getKnownList()
  {
    if (_knownList == null) _knownList = new ObjectKnownList(this);
    return _knownList;
  }
  public final void setKnownList(ObjectKnownList value) { _knownList = value; }

  public final String getName()
  {
    return _name;
  }

  public final void setName(String value) {
    _name = value;
  }

  public final int getObjectId()
  {
    return _objectId;
  }

  public final ObjectPoly getPoly()
  {
    if (_poly == null) _poly = new ObjectPoly(this);
    return _poly;
  }

  public final ObjectPosition getPosition()
  {
    if (_position == null) _position = new ObjectPosition(this);
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

  public boolean isInFunEvent()
  {
    if ((this instanceof L2PcInstance))
    {
      return ((L2PcInstance)this).isInFunEvent();
    }
    if ((this instanceof L2PetInstance))
    {
      return ((L2PetInstance)this).getOwner().isInFunEvent();
    }
    if ((this instanceof L2SummonInstance))
    {
      return ((L2SummonInstance)this).getOwner().isInFunEvent();
    }
    return false;
  }

  public Location getLoc()
  {
    return new Location(getX(), getY(), getZ(), 0);
  }
}