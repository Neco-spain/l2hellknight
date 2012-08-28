package l2p.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import l2p.commons.collections.LazyArrayList;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.util.Rnd;
import l2p.gameserver.dao.DominionRewardDAO;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.dao.SiegePlayerDAO;
import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.listener.actor.OnDeathListener;
import l2p.gameserver.listener.actor.OnKillListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.objects.DoorObject;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.events.objects.ZoneObject;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.Dominion;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.ExDominionWarEnd;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PledgeShowInfoUpdate;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.DoorTemplate.DoorType;
import l2p.gameserver.utils.Location;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.IntObjectMap.Entry;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

public class DominionSiegeEvent extends SiegeEvent<Dominion, SiegeClanObject>
{
  public static final int KILL_REWARD = 0;
  public static final int ONLINE_REWARD = 1;
  public static final int STATIC_BADGES = 2;
  public static final int REWARD_MAX = 3;
  public static final String ATTACKER_PLAYERS = "attacker_players";
  public static final String DEFENDER_PLAYERS = "defender_players";
  public static final String DISGUISE_PLAYERS = "disguise_players";
  public static final String TERRITORY_NPC = "territory_npc";
  public static final String CATAPULT = "catapult";
  public static final String CATAPULT_DOORS = "catapult_doors";
  private DominionSiegeRunnerEvent _runnerEvent;
  private Quest _forSakeQuest;
  private IntObjectMap<int[]> _playersRewards = new CHashIntObjectMap();

  public DominionSiegeEvent(MultiValueSet<String> set)
  {
    super(set);
    _killListener = new KillListener();
    _doorDeathListener = new DoorDeathListener();
  }

  public void initEvent()
  {
    _runnerEvent = ((DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1));

    super.initEvent();

    SiegeEvent castleSiegeEvent = ((Dominion)getResidence()).getCastle().getSiegeEvent();

    addObjects("mass_gatekeeper", castleSiegeEvent.getObjects("mass_gatekeeper"));
    addObjects("control_towers", castleSiegeEvent.getObjects("control_towers"));

    List doorObjects = getObjects("doors");
    for (DoorObject doorObject : doorObjects)
      doorObject.getDoor().addListener(_doorDeathListener);
  }

  public void reCalcNextTime(boolean onInit)
  {
  }

  public void startEvent()
  {
    List registeredDominions = _runnerEvent.getRegisteredDominions();
    List dominions = new ArrayList(9);
    for (Dominion d : registeredDominions) {
      if ((d.getSiegeDate().getTimeInMillis() != 0L) && (d != getResidence()))
        dominions.add(d.getSiegeEvent());
    }
    SiegeClanObject ownerClan = new SiegeClanObject("defenders", ((Dominion)getResidence()).getOwner(), 0L);

    addObject("defenders", ownerClan);

    for (Iterator i$ = dominions.iterator(); i$.hasNext(); ) { d = (DominionSiegeEvent)i$.next();

      d.addObject("attackers", ownerClan);

      List defenderPlayers = d.getObjects("defender_players");
      for (Iterator i$ = defenderPlayers.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
        addObject("attacker_players", Integer.valueOf(i));
      }
      List otherDefenders = d.getObjects("defenders");
      for (SiegeClanObject siegeClan : otherDefenders)
        if (siegeClan.getClan() != ((Dominion)d.getResidence()).getOwner())
          addObject("attackers", siegeClan);
    }
    DominionSiegeEvent d;
    int[] flags = ((Dominion)getResidence()).getFlags();
    if (flags.length > 0)
    {
      ((Dominion)getResidence()).removeSkills();
      ((Dominion)getResidence()).getOwner().broadcastToOnlineMembers(new IStaticPacket[] { SystemMsg.THE_EFFECT_OF_TERRITORY_WARD_IS_DISAPPEARING });
    }

    SiegeClanDAO.getInstance().delete(getResidence());
    SiegePlayerDAO.getInstance().delete(getResidence());

    for (int i : flags) {
      spawnAction("ward_" + i, true);
    }
    updateParticles(true, new String[0]);

    super.startEvent();
  }

  public void stopEvent(boolean t)
  {
    getObjects("disguise_players").clear();

    int[] flags = ((Dominion)getResidence()).getFlags();
    for (int i : flags) {
      spawnAction("ward_" + i, false);
    }
    ((Dominion)getResidence()).rewardSkills();
    ((Dominion)getResidence()).setJdbcState(JdbcEntityState.UPDATED);
    ((Dominion)getResidence()).update();

    updateParticles(false, new String[0]);

    List defenders = getObjects("defenders");
    for (SiegeClanObject clan : defenders) {
      clan.deleteFlag();
    }
    super.stopEvent(t);

    DominionRewardDAO.getInstance().insert((Dominion)getResidence());
  }

  public void loadSiegeClans()
  {
    addObjects("defenders", SiegeClanDAO.getInstance().load(getResidence(), "defenders"));
    addObjects("defender_players", SiegePlayerDAO.getInstance().select(getResidence(), 0));

    DominionRewardDAO.getInstance().select((Dominion)getResidence());
  }

  public void updateParticles(boolean start, String[] arg)
  {
    boolean battlefieldChat = _runnerEvent.isBattlefieldChatActive();
    List siegeClans = getObjects("defenders");
    for (SiegeClanObject s : siegeClans)
    {
      PledgeShowInfoUpdate packet;
      if (battlefieldChat)
      {
        s.getClan().setWarDominion(start ? getId() : 0);

        packet = new PledgeShowInfoUpdate(s.getClan());
        for (Player player : s.getClan().getOnlineMembers(0))
        {
          player.sendPacket(packet);

          updatePlayer(player, start);
        }
      }
      else
      {
        for (Player player : s.getClan().getOnlineMembers(0)) {
          updatePlayer(player, start);
        }
      }
    }
    List players = getObjects("defender_players");
    for (Iterator i$ = players.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      Player player = GameObjectsStorage.getPlayer(i);
      if (player != null)
        updatePlayer(player, start);
    }
  }

  public void updatePlayer(Player player, boolean start)
  {
    player.setBattlefieldChatId(_runnerEvent.isBattlefieldChatActive() ? getId() : 0);

    if (_runnerEvent.isBattlefieldChatActive())
    {
      if (start)
      {
        player.addEvent(this);

        addReward(player, 2, 5);
      }
      else
      {
        player.removeEvent(this);

        addReward(player, 2, 5);

        player.getEffectList().stopEffect(5660);
        player.addExpAndSp(270000L, 27000L);
      }

      player.broadcastCharInfo();

      if (!start) {
        player.sendPacket(ExDominionWarEnd.STATIC);
      }
      questUpdate(player, start);
    }
  }

  public void questUpdate(Player player, boolean start)
  {
    if (start)
    {
      QuestState sakeQuestState = _forSakeQuest.newQuestState(player, 1);
      sakeQuestState.setState(2);
      sakeQuestState.setCond(1);

      Quest protectCatapultQuest = QuestManager.getQuest("_729_ProtectTheTerritoryCatapult");
      if (protectCatapultQuest == null) {
        return;
      }
      QuestState questState = protectCatapultQuest.newQuestStateAndNotSave(player, 1);
      questState.setCond(1, false);
      questState.setStateAndNotSave(2);
    }
    else
    {
      for (Quest q : _runnerEvent.getBreakQuests())
      {
        QuestState questState = player.getQuestState(q.getClass());
        if (questState != null)
          questState.abortQuest();
      }
    }
  }

  public boolean isParticle(Player player)
  {
    if ((isInProgress()) || (_runnerEvent.isBattlefieldChatActive()))
    {
      boolean registered = (getObjects("defender_players").contains(Integer.valueOf(player.getObjectId()))) || (getSiegeClan("defenders", player.getClan()) != null);
      if (!registered) {
        return false;
      }

      if (isInProgress()) {
        return true;
      }

      player.setBattlefieldChatId(getId());
      return false;
    }

    return false;
  }

  public int getRelation(Player thisPlayer, Player targetPlayer, int result)
  {
    DominionSiegeEvent event2 = (DominionSiegeEvent)targetPlayer.getEvent(DominionSiegeEvent.class);
    if (event2 == null) {
      return result;
    }
    result |= 524288;
    return result;
  }

  public int getUserRelation(Player thisPlayer, int oldRelation)
  {
    oldRelation |= 4096;
    return oldRelation;
  }

  public SystemMsg checkForAttack(Creature target, Creature attacker, Skill skill, boolean force)
  {
    if (target.getEvent(DominionSiegeEvent.class) == attacker.getEvent(DominionSiegeEvent.class)) {
      return SystemMsg.YOU_CANNOT_FORCE_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY;
    }
    return null;
  }

  public void broadcastTo(IStaticPacket packet, String[] types)
  {
    List siegeClans = getObjects("defenders");
    for (SiegeClanObject siegeClan : siegeClans) {
      siegeClan.broadcast(new IStaticPacket[] { packet });
    }
    List players = getObjects("defender_players");
    for (Iterator i$ = players.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      Player player = GameObjectsStorage.getPlayer(i);
      if (player != null)
        player.sendPacket(packet);
    }
  }

  public void broadcastTo(L2GameServerPacket packet, String[] types)
  {
    List siegeClans = getObjects("defenders");
    for (SiegeClanObject siegeClan : siegeClans) {
      siegeClan.broadcast(new L2GameServerPacket[] { packet });
    }
    List players = getObjects("defender_players");
    for (Iterator i$ = players.iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      Player player = GameObjectsStorage.getPlayer(i);
      if (player != null)
        player.sendPacket(packet);
    }
  }

  public void giveItem(Player player, int itemId, long count)
  {
    Zone zone = player.getZone(Zone.ZoneType.SIEGE);
    if (zone == null) {
      count = 0L;
    }
    else {
      int id = zone.getParams().getInteger("residence");
      if (id < 100)
        count = 125L;
      else {
        count = 31L;
      }
    }
    addReward(player, 1, 1);
    super.giveItem(player, itemId, count);
  }

  public List<Player> itemObtainPlayers()
  {
    List playersInZone = getPlayersInZone();

    List list = new LazyArrayList(playersInZone.size());
    for (Player player : getPlayersInZone())
    {
      if (player.getEvent(DominionSiegeEvent.class) != null)
        list.add(player);
    }
    return list;
  }

  public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
  {
    if (getObjects("flag_zones").isEmpty()) {
      return;
    }
    SiegeClanObject clan = getSiegeClan("defenders", player.getClan());
    if ((clan != null) && (clan.getFlag() != null))
      r.put(RestartType.TO_FLAG, Boolean.TRUE);
  }

  public Location getRestartLoc(Player player, RestartType type)
  {
    if (type == RestartType.TO_FLAG)
    {
      SiegeClanObject defenderClan = getSiegeClan("defenders", player.getClan());

      if ((defenderClan != null) && (defenderClan.getFlag() != null)) {
        return Location.findPointToStay(defenderClan.getFlag(), 50, 75);
      }
      player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);

      return null;
    }

    return super.getRestartLoc(player, type);
  }

  public Location getEnterLoc(Player player)
  {
    Zone zone = player.getZone(Zone.ZoneType.SIEGE);
    if (zone == null) {
      return player.getLoc();
    }
    SiegeClanObject siegeClan = getSiegeClan("defenders", player.getClan());
    if (siegeClan != null)
    {
      if (siegeClan.getFlag() != null) {
        return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
      }
    }
    Residence r = ResidenceHolder.getInstance().getResidence(zone.getParams().getInteger("residence"));
    if (r == null)
    {
      error(toString(), new Exception("Not find residence: " + zone.getParams().getInteger("residence")));
      return player.getLoc();
    }
    return r.getNotOwnerRestartPoint(player);
  }

  public void teleportPlayers(String t)
  {
    List zones = getObjects("siege_zones");
    for (ZoneObject zone : zones)
    {
      Residence r = ResidenceHolder.getInstance().getResidence(zone.getZone().getParams().getInteger("residence"));

      r.banishForeigner();
    }
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

    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)target.getEvent(DominionSiegeEvent.class);
    if (siegeEvent == null)
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
      return false;
    }

    SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("defenders", targetPlayer.getClan());

    if (targetSiegeClan.getFlag() == null)
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

  public void setReward(int objectId, int type, int v)
  {
    int[] val = (int[])_playersRewards.get(objectId);
    if (val == null) {
      _playersRewards.put(objectId, val = new int[3]);
    }
    val[type] = v;
  }

  public void addReward(Player player, int type, int v)
  {
    int[] val = (int[])_playersRewards.get(player.getObjectId());
    if (val == null) {
      _playersRewards.put(player.getObjectId(), val = new int[3]);
    }
    val[type] += v;
  }

  public int getReward(Player player, int type)
  {
    int[] val = (int[])_playersRewards.get(player.getObjectId());
    if (val == null) {
      return 0;
    }
    return val[type];
  }

  public void clearReward(int objectId)
  {
    if (_playersRewards.containsKey(objectId))
    {
      _playersRewards.remove(objectId);
      DominionRewardDAO.getInstance().delete((Dominion)getResidence(), objectId);
    }
  }

  public Collection<IntObjectMap.Entry<int[]>> getRewards()
  {
    return _playersRewards.entrySet();
  }

  public int[] calculateReward(Player player)
  {
    int[] rewards = (int[])_playersRewards.get(player.getObjectId());
    if (rewards == null) {
      return null;
    }
    int[] out = new int[3];

    out[0] += rewards[2];

    out[0] += (rewards[1] >= 14 ? 7 : rewards[1] / 2);

    if (rewards[0] < 50)
    {
      int tmp70_69 = 0;
      int[] tmp70_68 = out; tmp70_68[tmp70_69] = (int)(tmp70_68[tmp70_69] + rewards[0] * 0.1D);
    } else if (rewards[0] < 120) {
      out[0] += 5 + (rewards[0] - 50) / 14;
    } else {
      out[0] += 10;
    }

    if (out[0] > 90)
    {
      out[0] = 90;
      out[1] = 0;
      out[2] = 450;
    }

    return out;
  }

  public void setForSakeQuest(Quest forSakeQuest)
  {
    _forSakeQuest = forSakeQuest;
  }

  public class KillListener
    implements OnKillListener
  {
    public KillListener()
    {
    }

    public void onKill(Creature actor, Creature victim)
    {
      if (!isInProgress()) {
        return;
      }
      if ((actor == null) || (victim == null)) {
        return;
      }
      Player winner = actor.getPlayer();
      Player killed = victim.getPlayer();

      if ((winner == null) || (killed == null)) {
        return;
      }
      if ((winner.getLevel() < 40) || (winner == killed) || (killed.getEvent(DominionSiegeEvent.class) == actor.getEvent(DominionSiegeEvent.class)) || (!actor.isInZone(Zone.ZoneType.SIEGE)) || (!killed.isInZone(Zone.ZoneType.SIEGE))) {
        return;
      }
      winner.setFame(winner.getFame() + Rnd.get(10, 20), toString());

      addReward(winner, 0, 1);

      if (killed.getLevel() >= 61)
      {
        Quest q = _runnerEvent.getClassQuest(killed.getClassId());
        if (q == null) {
          return;
        }
        QuestState questState = winner.getQuestState(q.getClass());
        if (questState == null)
        {
          questState = q.newQuestState(winner, 1);
          q.notifyKill(killed, questState);
        }
        else
        {
          q.notifyKill(killed, questState);
        }
      }
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
      Player player = killer.getPlayer();
      if (player != null) {
        player.sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);
      }
      Clan owner = ((Dominion)getResidence()).getOwner();
      if ((owner != null) && (owner.getLeader().isOnline()))
        owner.getLeader().getPlayer().sendPacket(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED);
    }
  }
}