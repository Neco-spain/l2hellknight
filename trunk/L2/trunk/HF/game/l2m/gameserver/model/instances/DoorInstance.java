package l2m.gameserver.model.instances;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import l2p.commons.geometry.Shape;
import l2p.commons.listener.Listener;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.DoorAI;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.geodata.GeoCollision;
import l2m.gameserver.geodata.GeoEngine;
import l2m.gameserver.listener.actor.door.OnOpenCloseListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.World;
import l2m.gameserver.model.actor.listener.CharListenerList;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.scripts.Events;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MyTargetSelected;
import l2m.gameserver.network.serverpackets.StaticObject;
import l2m.gameserver.network.serverpackets.ValidateLocation;
import l2m.gameserver.templates.DoorTemplate;
import l2m.gameserver.templates.DoorTemplate.DoorType;
import l2m.gameserver.templates.item.WeaponTemplate;

public final class DoorInstance extends Creature
  implements GeoCollision
{
  public static final long serialVersionUID = 1L;
  private boolean _open = true;
  private boolean _geoOpen = true;

  private Lock _openLock = new ReentrantLock();
  private int _upgradeHp;
  private byte[][] _geoAround;
  protected ScheduledFuture<?> _autoActionTask;

  public DoorInstance(int objectId, DoorTemplate template)
  {
    super(objectId, template);
  }

  public boolean isUnlockable()
  {
    return getTemplate().isUnlockable();
  }

  public String getName()
  {
    return getTemplate().getName();
  }

  public int getLevel()
  {
    return 1;
  }

  public int getDoorId()
  {
    return getTemplate().getNpcId();
  }

  public boolean isOpen()
  {
    return _open;
  }

  protected boolean setOpen(boolean open)
  {
    if (_open == open)
      return false;
    _open = open;
    return true;
  }

  public void scheduleAutoAction(boolean open, long actionDelay)
  {
    if (_autoActionTask != null)
    {
      _autoActionTask.cancel(false);
      _autoActionTask = null;
    }

    _autoActionTask = ThreadPoolManager.getInstance().schedule(new AutoOpenClose(open), actionDelay);
  }

  public int getDamage()
  {
    int dmg = 6 - (int)Math.ceil(getCurrentHpRatio() * 6.0D);
    return Math.max(0, Math.min(6, dmg));
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    return isAttackable(attacker);
  }

  public boolean isAttackable(Creature attacker)
  {
    if ((attacker == null) || (isOpen())) {
      return false;
    }
    SiegeEvent siegeEvent = (SiegeEvent)getEvent(SiegeEvent.class);

    switch (1.$SwitchMap$l2p$gameserver$templates$DoorTemplate$DoorType[getDoorType().ordinal()])
    {
    case 1:
      if ((attacker.isSummon()) && (siegeEvent != null) && (siegeEvent.containsSiegeSummon((SummonInstance)attacker))) break;
      return false;
    case 2:
      Player player = attacker.getPlayer();
      if (player == null)
        return false;
      if (siegeEvent == null)
        break;
      if (siegeEvent.getSiegeClan("defenders", player.getClan()) != null)
        return false;
      if (!siegeEvent.getObjects("defender_players").contains(Integer.valueOf(player.getObjectId()))) break;
      return false;
    }

    return !isInvul();
  }

  public void sendChanges()
  {
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

  public void onAction(Player player, boolean shift)
  {
    if (Events.onAction(player, this, shift)) {
      return;
    }
    if (this != player.getTarget())
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

      if (isAutoAttackable(player)) {
        player.sendPacket(new StaticObject(this, player));
      }
      player.sendPacket(new ValidateLocation(this));
    }
    else
    {
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));

      if (isAutoAttackable(player))
      {
        player.getAI().Attack(this, false, shift);
        return;
      }

      if (!isInRange(player, 200L))
      {
        if (player.getAI().getIntention() != CtrlIntention.AI_INTENTION_INTERACT)
          player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this, null);
        return;
      }

      getAI().onEvtTwiceClick(player);
    }
  }

  public DoorAI getAI()
  {
    if (_ai == null) {
      synchronized (this)
      {
        if (_ai == null)
          _ai = getTemplate().getNewAI(this);
      }
    }
    return (DoorAI)_ai;
  }

  public void broadcastStatusUpdate()
  {
    for (Player player : World.getAroundPlayers(this))
      if (player != null)
        player.sendPacket(new StaticObject(this, player));
  }

  public boolean openMe()
  {
    return openMe(null, true);
  }

  public boolean openMe(Player opener, boolean autoClose)
  {
    _openLock.lock();
    try
    {
      if (!setOpen(true)) {
        int i = 0;
        return i;
      }
      setGeoOpen(true);
    }
    finally
    {
      _openLock.unlock();
    }

    broadcastStatusUpdate();

    if ((autoClose) && (getTemplate().getCloseTime() > 0)) {
      scheduleAutoAction(false, getTemplate().getCloseTime() * 1000L);
    }
    getAI().onEvtOpen(opener);

    for (Listener l : getListeners().getListeners()) {
      if ((l instanceof OnOpenCloseListener))
        ((OnOpenCloseListener)l).onOpen(this);
    }
    return true;
  }

  public boolean closeMe()
  {
    return closeMe(null, true);
  }

  public boolean closeMe(Player closer, boolean autoOpen)
  {
    if (isDead()) {
      return false;
    }
    _openLock.lock();
    try
    {
      if (!setOpen(false)) {
        int i = 0;
        return i;
      }
      setGeoOpen(false);
    }
    finally
    {
      _openLock.unlock();
    }

    broadcastStatusUpdate();

    if ((autoOpen) && (getTemplate().getOpenTime() > 0))
    {
      long openDelay = getTemplate().getOpenTime() * 1000L;
      if (getTemplate().getRandomTime() > 0) {
        openDelay += Rnd.get(0, getTemplate().getRandomTime()) * 1000L;
      }
      scheduleAutoAction(true, openDelay);
    }

    getAI().onEvtClose(closer);

    for (Listener l : getListeners().getListeners()) {
      if ((l instanceof OnOpenCloseListener))
        ((OnOpenCloseListener)l).onClose(this);
    }
    return true;
  }

  public String toString()
  {
    return "[Door " + getDoorId() + "]";
  }

  protected void onDeath(Creature killer)
  {
    _openLock.lock();
    try
    {
      setGeoOpen(true);
    }
    finally
    {
      _openLock.unlock();
    }

    super.onDeath(killer);
  }

  protected void onRevive()
  {
    super.onRevive();

    _openLock.lock();
    try
    {
      if (!isOpen())
        setGeoOpen(false);
    }
    finally
    {
      _openLock.unlock();
    }
  }

  protected void onSpawn()
  {
    super.onSpawn();

    setCurrentHpMp(getMaxHp(), getMaxMp(), true);

    closeMe(null, true);
  }

  protected void onDespawn()
  {
    if (_autoActionTask != null)
    {
      _autoActionTask.cancel(false);
      _autoActionTask = null;
    }

    super.onDespawn();
  }

  public boolean isHPVisible()
  {
    return getTemplate().isHPVisible();
  }

  public int getMaxHp()
  {
    return super.getMaxHp() + _upgradeHp;
  }

  public void setUpgradeHp(int hp)
  {
    _upgradeHp = hp;
  }

  public int getUpgradeHp()
  {
    return _upgradeHp;
  }

  public int getPDef(Creature target)
  {
    switch (SevenSigns.getInstance().getSealOwner(3))
    {
    case 2:
      return (int)(super.getPDef(target) * 1.2D);
    case 1:
      return (int)(super.getPDef(target) * 0.3D);
    }
    return super.getPDef(target);
  }

  public int getMDef(Creature target, Skill skill)
  {
    switch (SevenSigns.getInstance().getSealOwner(3))
    {
    case 2:
      return (int)(super.getMDef(target, skill) * 1.2D);
    case 1:
      return (int)(super.getMDef(target, skill) * 0.3D);
    }
    return super.getMDef(target, skill);
  }

  public boolean isInvul()
  {
    if (!getTemplate().isHPVisible()) {
      return true;
    }

    SiegeEvent siegeEvent = (SiegeEvent)getEvent(SiegeEvent.class);
    if ((siegeEvent != null) && (siegeEvent.isInProgress())) {
      return false;
    }
    return super.isInvul();
  }

  protected boolean setGeoOpen(boolean open)
  {
    if (_geoOpen == open) {
      return false;
    }
    _geoOpen = open;

    if (Config.ALLOW_GEODATA)
    {
      if (open)
        GeoEngine.removeGeoCollision(this, getGeoIndex());
      else {
        GeoEngine.applyGeoCollision(this, getGeoIndex());
      }
    }
    return true;
  }

  public boolean isMovementDisabled()
  {
    return true;
  }

  public boolean isActionsDisabled()
  {
    return true;
  }

  public boolean isFearImmune()
  {
    return true;
  }

  public boolean isParalyzeImmune()
  {
    return true;
  }

  public boolean isLethalImmune()
  {
    return true;
  }

  public boolean isConcrete()
  {
    return true;
  }

  public boolean isHealBlocked()
  {
    return true;
  }

  public boolean isEffectImmune()
  {
    return true;
  }

  public List<L2GameServerPacket> addPacketList(Player forPlayer, Creature dropper)
  {
    return Collections.singletonList(new StaticObject(this, forPlayer));
  }

  public boolean isDoor()
  {
    return true;
  }

  public Shape getShape()
  {
    return getTemplate().getPolygon();
  }

  public byte[][] getGeoAround()
  {
    return _geoAround;
  }

  public void setGeoAround(byte[][] geo)
  {
    _geoAround = geo;
  }

  public DoorTemplate getTemplate()
  {
    return (DoorTemplate)super.getTemplate();
  }

  public DoorTemplate.DoorType getDoorType()
  {
    return getTemplate().getDoorType();
  }

  public int getKey()
  {
    return getTemplate().getKey();
  }

  private class AutoOpenClose extends RunnableImpl
  {
    private boolean _open;

    public AutoOpenClose(boolean open)
    {
      _open = open;
    }

    public void runImpl()
      throws Exception
    {
      if (_open)
        openMe(null, true);
      else
        closeMe(null, true);
    }
  }
}