package l2m.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.util.Rnd;
import l2m.gameserver.data.dao.SiegeClanDAO;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.listener.actor.OnDeathListener;
import l2m.gameserver.listener.actor.OnKillListener;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.base.RestartType;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.events.objects.ZoneObject;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.model.instances.DoorInstance;
import l2m.gameserver.model.instances.SummonInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.data.tables.ClanTable;
import l2m.gameserver.templates.DoorTemplate.DoorType;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.TimeUtils;

public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends GlobalEvent
{
  public static final String OWNER = "owner";
  public static final String OLD_OWNER = "old_owner";
  public static final String ATTACKERS = "attackers";
  public static final String DEFENDERS = "defenders";
  public static final String SPECTATORS = "spectators";
  public static final String SIEGE_ZONES = "siege_zones";
  public static final String FLAG_ZONES = "flag_zones";
  public static final String DAY_OF_WEEK = "day_of_week";
  public static final String HOUR_OF_DAY = "hour_of_day";
  public static final String REGISTRATION = "registration";
  public static final String DOORS = "doors";
  protected R _residence;
  private boolean _isInProgress;
  private boolean _isRegistrationOver;
  protected int _dayOfWeek;
  protected int _hourOfDay;
  protected Clan _oldOwner;
  protected OnKillListener _killListener = new KillListener();
  protected OnDeathListener _doorDeathListener = new DoorDeathListener();
  protected List<HardReference<SummonInstance>> _siegeSummons = new ArrayList();

  public SiegeEvent(MultiValueSet<String> set)
  {
    super(set);
    _dayOfWeek = set.getInteger("day_of_week", 0);
    _hourOfDay = set.getInteger("hour_of_day", 0);
  }

  public void startEvent()
  {
    setInProgress(true);

    super.startEvent();
  }

  public final void stopEvent()
  {
    stopEvent(false);
  }

  public void stopEvent(boolean step)
  {
    despawnSiegeSummons();
    setInProgress(false);
    reCalcNextTime(false);

    super.stopEvent();
  }

  public void processStep(Clan clan)
  {
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    Calendar startSiegeDate = getResidence().getSiegeDate();
    if (onInit)
    {
      if (startSiegeDate.getTimeInMillis() <= System.currentTimeMillis())
      {
        startSiegeDate.set(7, _dayOfWeek);
        startSiegeDate.set(11, _hourOfDay);

        validateSiegeDate(startSiegeDate, 2);
        getResidence().setJdbcState(JdbcEntityState.UPDATED);
      }
    }
    else
    {
      startSiegeDate.add(3, 2);
      getResidence().setJdbcState(JdbcEntityState.UPDATED);
    }

    registerActions();

    getResidence().update();
  }

  protected void validateSiegeDate(Calendar calendar, int add)
  {
    calendar.set(12, 0);
    calendar.set(13, 0);
    calendar.set(14, 0);

    while (calendar.getTimeInMillis() < System.currentTimeMillis())
      calendar.add(3, add);
  }

  protected long startTimeMillis()
  {
    return getResidence().getSiegeDate().getTimeInMillis();
  }

  public void teleportPlayers(String t)
  {
    List players = new ArrayList();
    Clan ownerClan = getResidence().getOwner();
    if (t.equalsIgnoreCase("owner"))
    {
      if (ownerClan != null)
        for (Player player : getPlayersInZone())
          if (player.getClan() == ownerClan)
            players.add(player);
    }
    else if (t.equalsIgnoreCase("attackers"))
    {
      for (Player player : getPlayersInZone())
      {
        SiegeClanObject siegeClan = getSiegeClan("attackers", player.getClan());
        if ((siegeClan != null) && (siegeClan.isParticle(player)))
          players.add(player);
      }
    }
    else if (t.equalsIgnoreCase("defenders"))
    {
      for (Player player : getPlayersInZone())
      {
        if ((ownerClan != null) && (player.getClan() != null) && (player.getClan() == ownerClan)) {
          continue;
        }
        SiegeClanObject siegeClan = getSiegeClan("defenders", player.getClan());
        if ((siegeClan != null) && (siegeClan.isParticle(player)))
          players.add(player);
      }
    }
    else if (t.equalsIgnoreCase("spectators"))
    {
      for (Player player : getPlayersInZone())
      {
        if ((ownerClan != null) && (player.getClan() != null) && (player.getClan() == ownerClan)) {
          continue;
        }
        if ((player.getClan() == null) || ((getSiegeClan("attackers", player.getClan()) == null) && (getSiegeClan("defenders", player.getClan()) == null)))
          players.add(player);
      }
    }
    else {
      players = getPlayersInZone();
    }
    for (Player player : players)
    {
      Location loc = null;
      if ((t.equalsIgnoreCase("owner")) || (t.equalsIgnoreCase("defenders")))
        loc = getResidence().getOwnerRestartPoint();
      else {
        loc = getResidence().getNotOwnerRestartPoint(player);
      }
      player.teleToLocation(loc, ReflectionManager.DEFAULT);
    }
  }

  public List<Player> getPlayersInZone()
  {
    List zones = getObjects("siege_zones");
    List result = new LazyArrayList();
    for (ZoneObject zone : zones)
      result.addAll(zone.getInsidePlayers());
    return result;
  }

  public void broadcastInZone(L2GameServerPacket[] packet)
  {
    for (Player player : getPlayersInZone())
      player.sendPacket(packet);
  }

  public void broadcastInZone(IStaticPacket[] packet)
  {
    for (Player player : getPlayersInZone())
      player.sendPacket(packet);
  }

  public boolean checkIfInZone(Creature character)
  {
    List zones = getObjects("siege_zones");
    for (ZoneObject zone : zones)
      if (zone.checkIfInZone(character))
        return true;
    return false;
  }

  public void broadcastInZone2(IStaticPacket[] packet)
  {
    for (Player player : getResidence().getZone().getInsidePlayers())
      player.sendPacket(packet);
  }

  public void broadcastInZone2(L2GameServerPacket[] packet)
  {
    for (Player player : getResidence().getZone().getInsidePlayers())
      player.sendPacket(packet);
  }

  public void loadSiegeClans()
  {
    addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
    addObjects("defenders", SiegeClanDAO.getInstance().load(getResidence(), "defenders"));
  }

  public S newSiegeClan(String type, int clanId, long param, long date)
  {
    Clan clan = ClanTable.getInstance().getClan(clanId);
    return clan == null ? null : new SiegeClanObject(type, clan, param, date);
  }

  public void updateParticles(boolean start, String[] arg)
  {
    for (String a : arg)
    {
      List siegeClans = getObjects(a);
      for (SiegeClanObject s : siegeClans)
        s.setEvent(start, this);
    }
  }

  public S getSiegeClan(String name, Clan clan)
  {
    if (clan == null)
      return null;
    return getSiegeClan(name, clan.getClanId());
  }

  public S getSiegeClan(String name, int objectId)
  {
    List siegeClanList = getObjects(name);
    if (siegeClanList.isEmpty())
      return null;
    for (int i = 0; i < siegeClanList.size(); i++)
    {
      SiegeClanObject siegeClan = (SiegeClanObject)siegeClanList.get(i);
      if (siegeClan.getObjectId() == objectId)
        return siegeClan;
    }
    return null;
  }

  public void broadcastTo(IStaticPacket packet, String[] types)
  {
    for (String type : types)
    {
      List siegeClans = getObjects(type);
      for (SiegeClanObject siegeClan : siegeClans)
        siegeClan.broadcast(new IStaticPacket[] { packet });
    }
  }

  public void broadcastTo(L2GameServerPacket packet, String[] types)
  {
    for (String type : types)
    {
      List siegeClans = getObjects(type);
      for (SiegeClanObject siegeClan : siegeClans)
        siegeClan.broadcast(new L2GameServerPacket[] { packet });
    }
  }

  public void initEvent()
  {
    _residence = ResidenceHolder.getInstance().getResidence(getId());

    loadSiegeClans();

    clearActions();

    super.initEvent();
  }

  protected void printInfo()
  {
    long startSiegeMillis = startTimeMillis();

    if (startSiegeMillis == 0L)
      info(getName() + " time - undefined");
    else
      info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
  }

  public boolean ifVar(String name)
  {
    if (name.equals("owner"))
      return getResidence().getOwner() != null;
    if (name.equals("old_owner")) {
      return _oldOwner != null;
    }
    return false;
  }

  public boolean isParticle(Player player)
  {
    if ((!isInProgress()) || (player.getClan() == null))
      return false;
    return (getSiegeClan("attackers", player.getClan()) != null) || (getSiegeClan("defenders", player.getClan()) != null);
  }

  public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
  {
    if (getObjects("flag_zones").isEmpty()) {
      return;
    }
    SiegeClanObject clan = getSiegeClan("attackers", player.getClan());
    if ((clan != null) && 
      (clan.getFlag() != null))
      r.put(RestartType.TO_FLAG, Boolean.TRUE);
  }

  public Location getRestartLoc(Player player, RestartType type)
  {
    SiegeClanObject attackerClan = getSiegeClan("attackers", player.getClan());

    Location loc = null;
    switch (1.$SwitchMap$l2p$gameserver$model$base$RestartType[type.ordinal()])
    {
    case 1:
      if ((!getObjects("flag_zones").isEmpty()) && (attackerClan != null) && (attackerClan.getFlag() != null))
        loc = Location.findPointToStay(attackerClan.getFlag(), 50, 75);
      else {
        player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
      }
    }

    return loc;
  }

  public int getRelation(Player thisPlayer, Player targetPlayer, int result)
  {
    Clan clan1 = thisPlayer.getClan();
    Clan clan2 = targetPlayer.getClan();
    if ((clan1 == null) || (clan2 == null)) {
      return result;
    }
    SiegeEvent siegeEvent2 = (SiegeEvent)targetPlayer.getEvent(SiegeEvent.class);
    if (this == siegeEvent2)
    {
      result |= 512;

      SiegeClanObject siegeClan1 = getSiegeClan("attackers", clan1);
      SiegeClanObject siegeClan2 = getSiegeClan("attackers", clan2);

      if (((siegeClan1 == null) && (siegeClan2 == null)) || ((siegeClan1 != null) && (siegeClan2 != null) && (isAttackersInAlly())))
        result |= 2048;
      else
        result |= 4096;
      if (siegeClan1 != null) {
        result |= 1024;
      }
    }
    return result;
  }

  public int getUserRelation(Player thisPlayer, int oldRelation)
  {
    SiegeClanObject siegeClan = getSiegeClan("attackers", thisPlayer.getClan());
    if (siegeClan != null)
      oldRelation |= 384;
    else
      oldRelation |= 128;
    return oldRelation;
  }

  public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
  {
    SiegeEvent siegeEvent = (SiegeEvent)target.getEvent(SiegeEvent.class);

    if (this != siegeEvent)
      return null;
    if ((!checkIfInZone(target)) || (!checkIfInZone(attacker))) {
      return null;
    }
    Player player = target.getPlayer();
    if (player == null) {
      return null;
    }
    SiegeClanObject siegeClan1 = getSiegeClan("attackers", player.getClan());
    if ((siegeClan1 == null) && (attacker.isSiegeGuard()))
      return SystemMsg.INVALID_TARGET;
    Player playerAttacker = attacker.getPlayer();
    if (playerAttacker == null) {
      return SystemMsg.INVALID_TARGET;
    }
    SiegeClanObject siegeClan2 = getSiegeClan("attackers", playerAttacker.getClan());

    if ((siegeClan1 != null) && (siegeClan2 != null) && (isAttackersInAlly())) {
      return SystemMsg.FORCE_ATTACK_IS_IMPOSSIBLE_AGAINST_A_TEMPORARY_ALLIED_MEMBER_DURING_A_SIEGE;
    }
    if ((siegeClan1 == null) && (siegeClan2 == null)) {
      return SystemMsg.INVALID_TARGET;
    }
    return null;
  }

  public boolean isInProgress()
  {
    return _isInProgress;
  }

  public void action(String name, boolean start)
  {
    if (name.equalsIgnoreCase("registration"))
      setRegistrationOver(!start);
    else
      super.action(name, start);
  }

  public boolean isAttackersInAlly()
  {
    return false;
  }

  public void onAddEvent(GameObject object)
  {
    if (_killListener == null) {
      return;
    }
    if (object.isPlayer())
      ((Player)object).addListener(_killListener);
  }

  public void onRemoveEvent(GameObject object)
  {
    if (_killListener == null) {
      return;
    }
    if (object.isPlayer())
      ((Player)object).removeListener(_killListener);
  }

  public List<Player> broadcastPlayers(int range)
  {
    return itemObtainPlayers();
  }

  public List<Player> itemObtainPlayers()
  {
    List playersInZone = getPlayersInZone();

    List list = new LazyArrayList(playersInZone.size());
    for (Player player : getPlayersInZone())
    {
      if (player.getEvent(getClass()) == this)
        list.add(player);
    }
    return list;
  }

  public Location getEnterLoc(Player player)
  {
    SiegeClanObject siegeClan = getSiegeClan("attackers", player.getClan());
    if (siegeClan != null)
    {
      if (siegeClan.getFlag() != null) {
        return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
      }
      return getResidence().getNotOwnerRestartPoint(player);
    }

    return getResidence().getOwnerRestartPoint();
  }

  public R getResidence()
  {
    return _residence;
  }

  public void setInProgress(boolean b)
  {
    _isInProgress = b;
  }

  public boolean isRegistrationOver()
  {
    return _isRegistrationOver;
  }

  public void setRegistrationOver(boolean b)
  {
    _isRegistrationOver = b;
  }

  public void addSiegeSummon(SummonInstance summon)
  {
    _siegeSummons.add(summon.getRef());
  }

  public boolean containsSiegeSummon(SummonInstance cha)
  {
    return _siegeSummons.contains(cha.getRef());
  }

  public void despawnSiegeSummons()
  {
    for (HardReference ref : _siegeSummons)
    {
      SummonInstance summon = (SummonInstance)ref.get();
      if (summon != null)
        summon.unSummon();
    }
    _siegeSummons.clear();
  }

  public class KillListener
    implements OnKillListener
  {
    public KillListener()
    {
    }

    public void onKill(Creature actor, Creature victim)
    {
      Player winner = actor.getPlayer();

      if ((winner == null) || (!victim.isPlayer()) || (winner.getLevel() < 40) || (winner == victim) || (victim.getEvent(getClass()) != SiegeEvent.this) || (!checkIfInZone(actor)) || (!checkIfInZone(victim))) {
        return;
      }
      winner.setFame(winner.getFame() + Rnd.get(10, 20), toString());
    }

    public boolean ignorePetOrSummon()
    {
      return true;
    }
  }

  public class DoorDeathListener
    implements OnDeathListener
  {
    public DoorDeathListener()
    {
    }

    public void onDeath(Creature actor, Creature killer)
    {
      if (!isInProgress()) {
        return;
      }
      DoorInstance door = (DoorInstance)actor;
      if (door.getDoorType() == DoorTemplate.DoorType.WALL) {
        return;
      }
      broadcastTo(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED, new String[] { "attackers", "defenders" });
    }
  }
}