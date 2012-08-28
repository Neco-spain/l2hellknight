package l2m.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.dao.CastleDamageZoneDAO;
import l2m.gameserver.data.dao.CastleDoorUpgradeDAO;
import l2m.gameserver.data.dao.CastleHiredGuardDAO;
import l2m.gameserver.data.dao.SiegeClanDAO;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Spawner;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.base.RestartType;
import l2m.gameserver.model.entity.Hero;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.objects.DoorObject;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.events.objects.SiegeToggleNpcObject;
import l2m.gameserver.model.entity.events.objects.SpawnExObject;
import l2m.gameserver.model.entity.events.objects.SpawnSimpleObject;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.instances.DoorInstance;
import l2m.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.PlaySound;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.item.support.MerchantGuard;
import l2m.gameserver.templates.spawn.SpawnRange;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.Containers;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.TreeIntSet;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject>
{
  public static final int MAX_SIEGE_CLANS = 20;
  public static final long DAY_IN_MILISECONDS = 86400000L;
  public static final String DEFENDERS_WAITING = "defenders_waiting";
  public static final String DEFENDERS_REFUSED = "defenders_refused";
  public static final String CONTROL_TOWERS = "control_towers";
  public static final String FLAME_TOWERS = "flame_towers";
  public static final String BOUGHT_ZONES = "bought_zones";
  public static final String GUARDS = "guards";
  public static final String HIRED_GUARDS = "hired_guards";
  private IntSet _nextSiegeTimes = Containers.EMPTY_INT_SET;
  private Future<?> _nextSiegeDateSetTask = null;
  private boolean _firstStep = false;

  public CastleSiegeEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void initEvent()
  {
    super.initEvent();

    List doorObjects = getObjects("doors");

    addObjects("bought_zones", CastleDamageZoneDAO.getInstance().load(getResidence()));

    for (DoorObject doorObject : doorObjects)
    {
      doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
      doorObject.getDoor().addListener(_doorDeathListener);
    }
  }

  public void processStep(Clan newOwnerClan)
  {
    Clan oldOwnerClan = ((Castle)getResidence()).getOwner();

    ((Castle)getResidence()).changeOwner(newOwnerClan);

    if (oldOwnerClan != null)
    {
      SiegeClanObject ownerSiegeClan = getSiegeClan("defenders", oldOwnerClan);
      removeObject("defenders", ownerSiegeClan);

      ownerSiegeClan.setType("attackers");
      addObject("attackers", ownerSiegeClan);
    }
    else
    {
      if (getObjects("attackers").size() == 1)
      {
        stopEvent();
        return;
      }

      int allianceObjectId = newOwnerClan.getAllyId();
      if (allianceObjectId > 0)
      {
        List attackers = getObjects("attackers");
        boolean sameAlliance = true;
        for (SiegeClanObject sc : attackers)
          if ((sc != null) && (sc.getClan().getAllyId() != allianceObjectId))
            sameAlliance = false;
        if (sameAlliance)
        {
          stopEvent();
          return;
        }
      }

    }

    SiegeClanObject newOwnerSiegeClan = getSiegeClan("attackers", newOwnerClan);
    newOwnerSiegeClan.deleteFlag();
    newOwnerSiegeClan.setType("defenders");

    removeObject("attackers", newOwnerSiegeClan);

    List defenders = removeObjects("defenders");
    for (SiegeClanObject siegeClan : defenders) {
      siegeClan.setType("attackers");
    }

    addObject("defenders", newOwnerSiegeClan);

    addObjects("attackers", defenders);

    updateParticles(true, new String[] { "attackers", "defenders" });

    teleportPlayers("attackers");
    teleportPlayers("spectators");

    if (!_firstStep)
    {
      _firstStep = true;

      broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, new String[] { "attackers", "defenders" });

      if (_oldOwner != null)
      {
        spawnAction("hired_guards", false);
        damageZoneAction(false);
        removeObjects("hired_guards");
        removeObjects("bought_zones");

        CastleDamageZoneDAO.getInstance().delete(getResidence());
      }
      else {
        spawnAction("guards", false);
      }
      List doorObjects = getObjects("doors");
      for (DoorObject doorObject : doorObjects)
      {
        doorObject.setWeak(true);
        doorObject.setUpgradeValue(this, 0);

        CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
      }
    }

    spawnAction("doors", true);
    despawnSiegeSummons();
  }

  public void startEvent()
  {
    _oldOwner = ((Castle)getResidence()).getOwner();
    if (_oldOwner != null)
    {
      addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));

      if (((Castle)getResidence()).getSpawnMerchantTickets().size() > 0)
      {
        for (ItemInstance item : ((Castle)getResidence()).getSpawnMerchantTickets())
        {
          MerchantGuard guard = ((Castle)getResidence()).getMerchantGuard(item.getItemId());

          addObject("hired_guards", new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));

          item.deleteMe();
        }

        CastleHiredGuardDAO.getInstance().delete(getResidence());

        spawnAction("hired_guards", true);
      }
    }

    List attackers = getObjects("attackers");
    if (attackers.isEmpty())
    {
      if (_oldOwner == null)
        broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()));
      else {
        broadcastToWorld(new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()));
      }
      reCalcNextTime(false);
      return;
    }

    SiegeClanDAO.getInstance().delete(getResidence());

    updateParticles(true, new String[] { "attackers", "defenders" });

    broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, new String[] { "attackers" });
    broadcastTo(new SystemMessage2(SystemMsg.YOU_ARE_PARTICIPATING_IN_THE_SIEGE_OF_S1_THIS_SIEGE_IS_SCHEDULED_FOR_2_HOURS).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });

    super.startEvent();

    if (_oldOwner == null)
      initControlTowers();
    else
      damageZoneAction(true);
  }

  public void stopEvent(boolean step)
  {
    List doorObjects = getObjects("doors");
    for (DoorObject doorObject : doorObjects) {
      doorObject.setWeak(false);
    }
    damageZoneAction(false);

    updateParticles(false, new String[] { "attackers", "defenders" });

    List attackers = removeObjects("attackers");
    for (SiegeClanObject siegeClan : attackers) {
      siegeClan.deleteFlag();
    }
    broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()));

    removeObjects("defenders");
    removeObjects("defenders_waiting");
    removeObjects("defenders_refused");

    Clan ownerClan = ((Castle)getResidence()).getOwner();
    if (ownerClan != null)
    {
      if (_oldOwner == ownerClan)
      {
        ((Castle)getResidence()).setRewardCount(((Castle)getResidence()).getRewardCount() + 1);
        ownerClan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1500, false, toString())) });
      }
      else
      {
        broadcastToWorld(((SystemMessage2)new SystemMessage2(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName())).addResidenceName(getResidence()));

        ownerClan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(3000, false, toString())) });

        if (_oldOwner != null) {
          _oldOwner.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS).addInteger(-_oldOwner.incReputation(-3000, false, toString())) });
        }
        for (UnitMember member : ownerClan)
        {
          Player player = member.getPlayer();
          if (player != null)
          {
            player.sendPacket(PlaySound.SIEGE_VICTORY);
            if ((player.isOnline()) && (player.isNoble())) {
              Hero.getInstance().addHeroDiary(player.getObjectId(), 3, ((Castle)getResidence()).getId());
            }
          }
        }
      }
      ((Castle)getResidence()).getOwnDate().setTimeInMillis(System.currentTimeMillis());
      ((Castle)getResidence()).getLastSiegeDate().setTimeInMillis(((Castle)getResidence()).getSiegeDate().getTimeInMillis());

      DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
      runnerEvent.registerDominion(((Castle)getResidence()).getDominion());
    }
    else
    {
      broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));

      ((Castle)getResidence()).getOwnDate().setTimeInMillis(0L);
      ((Castle)getResidence()).getLastSiegeDate().setTimeInMillis(0L);

      DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
      runnerEvent.unRegisterDominion(((Castle)getResidence()).getDominion());
    }

    despawnSiegeSummons();

    if (_oldOwner != null)
    {
      spawnAction("hired_guards", false);
      removeObjects("hired_guards");
    }

    super.stopEvent(step);
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    long currentTimeMillis = System.currentTimeMillis();
    Calendar startSiegeDate = ((Castle)getResidence()).getSiegeDate();
    Calendar ownSiegeDate = ((Castle)getResidence()).getOwnDate();
    if (onInit)
    {
      if (startSiegeDate.getTimeInMillis() > currentTimeMillis)
        registerActions();
      else if (startSiegeDate.getTimeInMillis() == 0L)
      {
        if (currentTimeMillis - ownSiegeDate.getTimeInMillis() > 86400000L)
          setNextSiegeTime();
        else
          generateNextSiegeDates();
      }
      else if (startSiegeDate.getTimeInMillis() <= currentTimeMillis) {
        setNextSiegeTime();
      }

    }
    else if (((Castle)getResidence()).getOwner() != null)
    {
      ((Castle)getResidence()).getSiegeDate().setTimeInMillis(0L);
      ((Castle)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
      ((Castle)getResidence()).update();

      generateNextSiegeDates();
    }
    else {
      setNextSiegeTime();
    }
  }

  public void loadSiegeClans()
  {
    super.loadSiegeClans();

    addObjects("defenders_waiting", SiegeClanDAO.getInstance().load(getResidence(), "defenders_waiting"));
    addObjects("defenders_refused", SiegeClanDAO.getInstance().load(getResidence(), "defenders_refused"));
  }

  public void setRegistrationOver(boolean b)
  {
    if (b) {
      broadcastToWorld(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()));
    }
    super.setRegistrationOver(b);
  }

  public void announce(int val)
  {
    int min = val / 60;
    int hour = min / 60;
    SystemMessage2 msg;
    SystemMessage2 msg;
    if (hour > 0) {
      msg = (SystemMessage2)new SystemMessage2(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(hour);
    }
    else
    {
      SystemMessage2 msg;
      if (min > 0)
        msg = (SystemMessage2)new SystemMessage2(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(min);
      else
        msg = (SystemMessage2)new SystemMessage2(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addInteger(val);
    }
    broadcastTo(msg, new String[] { "attackers", "defenders" });
  }

  private void initControlTowers()
  {
    List objects = getObjects("guards");
    List spawns = new ArrayList();
    for (SpawnExObject o : objects) {
      spawns.addAll(o.getSpawns());
    }
    List ct = getObjects("control_towers");

    for (Iterator i$ = spawns.iterator(); i$.hasNext(); ) { spawn = (Spawner)i$.next();

      spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(ReflectionManager.DEFAULT.getGeoIndex());

      closestCt = null;
      distanceClosest = 0.0D;

      for (SiegeToggleNpcObject c : ct)
      {
        SiegeToggleNpcInstance npcTower = c.getToggleNpc();
        double distance = npcTower.getDistance(spawnLoc);

        if ((closestCt == null) || (distance < distanceClosest))
        {
          closestCt = npcTower;
          distanceClosest = distance;
        }

        closestCt.register(spawn);
      } } Spawner spawn;
    Location spawnLoc;
    SiegeToggleNpcInstance closestCt;
    double distanceClosest;
  }
  private void damageZoneAction(boolean active) { zoneAction("bought_zones", active);
  }

  public void generateNextSiegeDates()
  {
    if (((Castle)getResidence()).getSiegeDate().getTimeInMillis() != 0L) {
      return;
    }
    Calendar calendar = (Calendar)Config.CASTLE_VALIDATION_DATE.clone();
    calendar.set(7, 1);
    if (calendar.before(Config.CASTLE_VALIDATION_DATE))
      calendar.add(3, 1);
    validateSiegeDate(calendar, 2);

    _nextSiegeTimes = new TreeIntSet();

    calendar.set(11, getNextSiegeHour());
    _nextSiegeTimes.add((int)(calendar.getTimeInMillis() / 1000L));

    long diff = ((Castle)getResidence()).getOwnDate().getTimeInMillis() + 86400000L - System.currentTimeMillis();
    _nextSiegeDateSetTask = ThreadPoolManager.getInstance().schedule(new NextSiegeDateSet(null), diff);
  }

  public void setNextSiegeTime(int id)
  {
    if ((!_nextSiegeTimes.contains(id)) || (_nextSiegeDateSetTask == null)) {
      return;
    }
    _nextSiegeTimes = Containers.EMPTY_INT_SET;
    _nextSiegeDateSetTask.cancel(false);
    _nextSiegeDateSetTask = null;

    setNextSiegeTime(id * 1000L);
  }

  private void setNextSiegeTime()
  {
    Calendar calendar = (Calendar)Config.CASTLE_VALIDATION_DATE.clone();
    calendar.set(7, 1);
    calendar.set(11, ((Castle)getResidence()).getLastSiegeDate().get(11));
    if (calendar.before(Config.CASTLE_VALIDATION_DATE))
      calendar.add(3, 1);
    validateSiegeDate(calendar, 2);

    setNextSiegeTime(calendar.getTimeInMillis());
  }

  private void setNextSiegeTime(long g)
  {
    broadcastToWorld(new SystemMessage2(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME).addResidenceName(getResidence()));

    clearActions();

    ((Castle)getResidence()).getSiegeDate().setTimeInMillis(g);
    ((Castle)getResidence()).getSiegeDate().set(11, getNextSiegeHour());
    ((Castle)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
    ((Castle)getResidence()).update();

    registerActions();
  }

  private int getNextSiegeHour()
  {
    int[] castles = { 3, 4, 6, 7 };
    return ArrayUtils.contains(castles, ((Castle)getResidence()).getId()) ? Config.CASTLE_SELECT_HOURS[1] : Config.CASTLE_SELECT_HOURS[0];
  }

  public boolean isAttackersInAlly()
  {
    return !_firstStep;
  }

  public int[] getNextSiegeTimes()
  {
    return _nextSiegeTimes.toArray();
  }

  public boolean canRessurect(Player resurrectPlayer, Creature target, boolean force)
  {
    boolean playerInZone = resurrectPlayer.isInZone(Zone.ZoneType.SIEGE);
    boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);

    if ((!playerInZone) && (!targetInZone)) {
      return true;
    }
    if (!targetInZone) {
      return false;
    }
    Player targetPlayer = target.getPlayer();

    CastleSiegeEvent siegeEvent = (CastleSiegeEvent)target.getEvent(CastleSiegeEvent.class);
    if (siegeEvent != this)
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
      return false;
    }

    SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
    if (targetSiegeClan == null) {
      targetSiegeClan = siegeEvent.getSiegeClan("defenders", targetPlayer.getClan());
    }
    if (targetSiegeClan.getType() == "attackers")
    {
      if (targetSiegeClan.getFlag() == null)
      {
        if (force)
          targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
        resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
        return false;
      }
    }
    else
    {
      List towers = getObjects("control_towers");

      boolean canRes = true;
      for (SiegeToggleNpcObject t : towers) {
        if (!t.isAlive())
          canRes = false;
      }
      if (!canRes)
      {
        if (force)
          targetPlayer.sendPacket(SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
        resurrectPlayer.sendPacket(force ? SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
      }
    }

    if (force) {
      return true;
    }

    resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
    return false;
  }

  public Location getRestartLoc(Player player, RestartType type)
  {
    Location loc = null;
    switch (1.$SwitchMap$l2p$gameserver$model$base$RestartType[type.ordinal()])
    {
    case 1:
      if (SevenSigns.getInstance().getSealOwner(3) == 2)
        break;
      loc = ((Castle)_residence).getNotOwnerRestartPoint(player);
      break;
    case 2:
      if ((!getObjects("flag_zones").isEmpty()) && (getSiegeClan("attackers", player.getClan()) != null) && (getSiegeClan("attackers", player.getClan()).getFlag() != null)) {
        loc = Location.findPointToStay(getSiegeClan("attackers", player.getClan()).getFlag(), 50, 75);
      }
      else {
        player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
      }
    }

    return loc;
  }

  private class NextSiegeDateSet extends RunnableImpl
  {
    private NextSiegeDateSet()
    {
    }

    public void runImpl()
      throws Exception
    {
      CastleSiegeEvent.this.setNextSiegeTime();
    }
  }
}