package l2p.gameserver.model.entity.events.impl;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.listener.actor.npc.OnSpawnListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.entity.events.objects.DoorObject;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.events.objects.SpawnExObject;
import l2p.gameserver.model.entity.events.objects.StaticObjectObject;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.utils.TimeUtils;

public class FortressSiegeEvent extends SiegeEvent<Fortress, SiegeClanObject>
{
  public static final String FLAG_POLE = "flag_pole";
  public static final String COMBAT_FLAGS = "combat_flags";
  public static final String SIEGE_COMMANDERS = "siege_commanders";
  public static final String PEACE_COMMANDERS = "peace_commanders";
  public static final String UPGRADEABLE_DOORS = "upgradeable_doors";
  public static final String COMMANDER_DOORS = "commander_doors";
  public static final String ENTER_DOORS = "enter_doors";
  public static final String MACHINE_DOORS = "machine_doors";
  public static final String OUT_POWER_UNITS = "out_power_units";
  public static final String IN_POWER_UNITS = "in_power_units";
  public static final String GUARDS_LIVE_WITH_C_CENTER = "guards_live_with_c_center";
  public static final String ENVOY = "envoy";
  public static final String MERCENARY_POINTS = "mercenary_points";
  public static final String MERCENARY = "mercenary";
  public static final long SIEGE_WAIT_PERIOD = 14400000L;
  public static final OnSpawnListener RESTORE_BARRACKS_LISTENER = new RestoreBarracksListener(null);
  private Future<?> _envoyTask;
  private boolean[] _barrackStatus;

  public FortressSiegeEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void processStep(Clan newOwnerClan)
  {
    if (newOwnerClan.getCastle() > 0) {
      ((Fortress)getResidence()).changeOwner(null);
    }
    else {
      ((Fortress)getResidence()).changeOwner(newOwnerClan);

      stopEvent(true);
    }
  }

  public void initEvent()
  {
    super.initEvent();

    SpawnExObject exObject = (SpawnExObject)getFirstObject("siege_commanders");
    _barrackStatus = new boolean[exObject.getSpawns().size()];

    int lvl = ((Fortress)getResidence()).getFacilityLevel(2);
    List doorObjects = getObjects("upgradeable_doors");
    for (DoorObject d : doorObjects)
    {
      d.setUpgradeValue(this, d.getDoor().getMaxHp() * lvl);
      d.getDoor().addListener(_doorDeathListener);
    }

    flagPoleUpdate(false);
    if (((Fortress)getResidence()).getOwnerId() > 0)
      spawnEnvoy();
  }

  public void startEvent()
  {
    _oldOwner = ((Fortress)getResidence()).getOwner();

    if (_oldOwner != null) {
      addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
    }
    SiegeClanDAO.getInstance().delete(getResidence());

    flagPoleUpdate(true);
    updateParticles(true, new String[] { "attackers", "defenders" });

    broadcastTo(new SystemMessage2(SystemMsg.THE_FORTRESS_BATTLE_S1_HAS_BEGUN).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });

    super.startEvent();
  }

  public void stopEvent(boolean step)
  {
    spawnAction("combat_flags", false);
    updateParticles(false, new String[] { "attackers", "defenders" });

    broadcastTo(new SystemMessage2(SystemMsg.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });

    Clan ownerClan = ((Fortress)getResidence()).getOwner();
    if (ownerClan != null)
    {
      if (_oldOwner != ownerClan)
      {
        ownerClan.broadcastToOnlineMembers(new L2GameServerPacket[] { PlaySound.SIEGE_VICTORY });

        ownerClan.incReputation(Config.CLAN_REP_FOR_FORT, false, toString());
        broadcastTo(((SystemMessage2)new SystemMessage2(SystemMsg.S1_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2).addString(ownerClan.getName())).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });

        ((Fortress)getResidence()).getOwnDate().setTimeInMillis(System.currentTimeMillis());

        ((Fortress)getResidence()).startCycleTask();
        spawnEnvoy();
      }
    }
    else {
      ((Fortress)getResidence()).getOwnDate().setTimeInMillis(0L);
    }
    List attackers = removeObjects("attackers");
    for (SiegeClanObject siegeClan : attackers) {
      siegeClan.deleteFlag();
    }
    removeObjects("defenders");

    flagPoleUpdate(false);

    super.stopEvent(step);
  }

  public synchronized void reCalcNextTime(boolean onStart)
  {
    int attackersSize = getObjects("attackers").size();

    Calendar startSiegeDate = ((Fortress)getResidence()).getSiegeDate();
    Calendar lastSiegeDate = ((Fortress)getResidence()).getLastSiegeDate();
    long currentTimeMillis = System.currentTimeMillis();

    if ((startSiegeDate.getTimeInMillis() > currentTimeMillis) && 
      (attackersSize > 0))
    {
      if (onStart)
        registerActions();
      return;
    }

    clearActions();

    if (attackersSize > 0)
    {
      if (currentTimeMillis - lastSiegeDate.getTimeInMillis() > 14400000L)
      {
        startSiegeDate.setTimeInMillis(currentTimeMillis);
        startSiegeDate.add(11, 1);
      }
      else
      {
        startSiegeDate.setTimeInMillis(lastSiegeDate.getTimeInMillis());
        startSiegeDate.add(11, 5);
      }

      registerActions();
    }
    else {
      startSiegeDate.setTimeInMillis(0L);
    }
    ((Fortress)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
    ((Fortress)getResidence()).update();
  }

  public void announce(int val)
  {
    int min = val / 60;
    SystemMessage2 msg;
    SystemMessage2 msg;
    if (min > 0)
      msg = (SystemMessage2)new SystemMessage2(SystemMsg.S1_MINUTES_UNTIL_THE_FORTRESS_BATTLE_STARTS).addInteger(min);
    else {
      msg = (SystemMessage2)new SystemMessage2(SystemMsg.S1_SECONDS_UNTIL_THE_FORTRESS_BATTLE_STARTS).addInteger(val);
    }
    broadcastTo(msg, new String[] { "attackers", "defenders" });
  }

  public void spawnEnvoy()
  {
    long endTime = ((Fortress)getResidence()).getOwnDate().getTimeInMillis() + 3600000L;
    long diff = endTime - System.currentTimeMillis();

    if ((diff > 0L) && (((Fortress)getResidence()).getContractState() == 0))
    {
      SpawnExObject exObject = (SpawnExObject)getFirstObject("envoy");
      if (exObject.isSpawned()) {
        info("Last siege: " + TimeUtils.toSimpleFormat(((Fortress)getResidence()).getLastSiegeDate()) + ", own date: " + TimeUtils.toSimpleFormat(((Fortress)getResidence()).getOwnDate()) + ", siege date: " + TimeUtils.toSimpleFormat(((Fortress)getResidence()).getSiegeDate()));
      }
      spawnAction("envoy", true);
      _envoyTask = ThreadPoolManager.getInstance().schedule(new EnvoyDespawn(null), diff);
    }
    else if (((Fortress)getResidence()).getContractState() == 0)
    {
      ((Fortress)getResidence()).setFortState(1, 0);
      ((Fortress)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
      ((Fortress)getResidence()).update();
    }
  }

  public void despawnEnvoy()
  {
    _envoyTask.cancel(false);
    _envoyTask = null;

    spawnAction("envoy", false);
    if (((Fortress)getResidence()).getContractState() == 0)
    {
      ((Fortress)getResidence()).setFortState(1, 0);
      ((Fortress)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
      ((Fortress)getResidence()).update();
    }
  }

  public void flagPoleUpdate(boolean dis)
  {
    StaticObjectObject object = (StaticObjectObject)getFirstObject("flag_pole");
    if (object != null)
      object.setMeshIndex(((Fortress)getResidence()).getOwner() != null ? 1 : dis ? 0 : 0);
  }

  public synchronized void barrackAction(int id, boolean val)
  {
    _barrackStatus[id] = val;
  }

  public synchronized void checkBarracks()
  {
    boolean allDead = true;
    for (boolean b : getBarrackStatus()) {
      if (!b)
        allDead = false;
    }
    if (allDead)
    {
      if (_oldOwner != null)
      {
        SpawnExObject spawn = (SpawnExObject)getFirstObject("mercenary");
        NpcInstance npc = spawn.getFirstSpawned();
        if ((npc == null) || (npc.isDead())) {
          return;
        }
        Functions.npcShout(npc, NpcString.THE_COMMAND_GATE_HAS_OPENED_CAPTURE_THE_FLAG_QUICKLY_AND_RAISE_IT_HIGH_TO_PROCLAIM_OUR_VICTORY, new String[0]);

        spawnFlags();
      }
      else {
        spawnFlags();
      }
    }
  }

  public void spawnFlags() {
    doorAction("commander_doors", true);
    spawnAction("siege_commanders", false);
    spawnAction("combat_flags", true);

    if (_oldOwner != null) {
      spawnAction("mercenary", false);
    }
    spawnAction("guards_live_with_c_center", false);

    broadcastTo(SystemMsg.ALL_BARRACKS_ARE_OCCUPIED, new String[] { "attackers", "defenders" });
  }

  public boolean ifVar(String name)
  {
    if (name.equals("owner"))
      return ((Fortress)getResidence()).getOwner() != null;
    if (name.equals("old_owner"))
      return _oldOwner != null;
    if (name.equalsIgnoreCase("reinforce_1"))
      return ((Fortress)getResidence()).getFacilityLevel(0) == 1;
    if (name.equalsIgnoreCase("reinforce_2"))
      return ((Fortress)getResidence()).getFacilityLevel(0) == 2;
    if (name.equalsIgnoreCase("dwarvens"))
      return ((Fortress)getResidence()).getFacilityLevel(3) == 1;
    return false;
  }

  public boolean[] getBarrackStatus()
  {
    return _barrackStatus;
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

    FortressSiegeEvent siegeEvent = (FortressSiegeEvent)target.getEvent(FortressSiegeEvent.class);
    if (siegeEvent != this)
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
      return false;
    }

    SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());

    if ((targetSiegeClan == null) || (targetSiegeClan.getFlag() == null))
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
      return false;
    }

    if (force) {
      return true;
    }

    resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
    return false;
  }

  public void setRegistrationOver(boolean b)
  {
    super.setRegistrationOver(b);
    if (b)
    {
      ((Fortress)getResidence()).getLastSiegeDate().setTimeInMillis(((Fortress)getResidence()).getSiegeDate().getTimeInMillis());
      ((Fortress)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
      ((Fortress)getResidence()).update();

      if (((Fortress)getResidence()).getOwner() != null)
        ((Fortress)getResidence()).getOwner().broadcastToOnlineMembers(new IStaticPacket[] { SystemMsg.ENEMY_BLOOD_PLEDGES_HAVE_INTRUDED_INTO_THE_FORTRESS });
    }
  }

  private static class RestoreBarracksListener
    implements OnSpawnListener
  {
    public void onSpawn(NpcInstance actor)
    {
      FortressSiegeEvent siegeEvent = (FortressSiegeEvent)actor.getEvent(FortressSiegeEvent.class);
      SpawnExObject siegeCommanders = (SpawnExObject)siegeEvent.getFirstObject("siege_commanders");
      if (siegeCommanders.isSpawned())
        siegeEvent.broadcastTo(SystemMsg.THE_BARRACKS_FUNCTION_HAS_BEEN_RESTORED, new String[] { "attackers", "defenders" });
    }
  }

  private class EnvoyDespawn extends RunnableImpl
  {
    private EnvoyDespawn()
    {
    }

    public void runImpl()
      throws Exception
    {
      despawnEnvoy();
    }
  }
}