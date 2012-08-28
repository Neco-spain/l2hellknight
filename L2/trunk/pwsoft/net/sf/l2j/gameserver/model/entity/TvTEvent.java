package net.sf.l2j.gameserver.model.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class TvTEvent
{
  private static final Logger _log = AbstractLogger.getLogger(TvTEvent.class.getName());

  private static TvTEventTeam[] _teams = new TvTEventTeam[2];

  private static EventState _state = EventState.INACTIVE;

  private static FastList<L2Spawn> _npcSpawns = new FastList();
  private static FastList<Location> _npcLocs = new FastList();
  private static int _npcLocsSize = 0;
  private static ConcurrentLinkedQueue<String> _ips = new ConcurrentLinkedQueue();

  public static void init()
  {
    _teams[0] = new TvTEventTeam(Config.TVT_EVENT_TEAM_1_NAME, Config.TVT_EVENT_TEAM_1_COORDINATES);
    _teams[1] = new TvTEventTeam(Config.TVT_EVENT_TEAM_2_NAME, Config.TVT_EVENT_TEAM_2_COORDINATES);

    _npcLocs.addAll(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES);
    _npcLocsSize = _npcLocs.size() - 1;
  }

  public static boolean startParticipation()
  {
    if (NpcTable.getInstance().getTemplate(Config.TVT_EVENT_PARTICIPATION_NPC_ID) == null) {
      _log.warning("TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
      return false;
    }

    L2Spawn spawn = null;
    L2NpcInstance npc = null;
    GrandBossManager gb = GrandBossManager.getInstance();
    FastList.Node n = _npcLocs.head(); for (FastList.Node end = _npcLocs.tail(); (n = n.getNext()) != end; ) {
      Location loc = (Location)n.getValue();
      if (loc == null)
      {
        continue;
      }
      spawn = gb.createOneSpawnEx(Config.TVT_EVENT_PARTICIPATION_NPC_ID, loc.x, loc.y, loc.z, 30000, false);
      _npcSpawns.add(spawn);
      npc = spawn.spawnOne();
      npc.setTitle("TvT Event Participation");
      npc.decayMe();
      npc.spawnMe(loc.x, loc.y, loc.z);
      npc.broadcastPacket(new MagicSkillUser(npc, npc, 1034, 1, 1, 1));
    }
    npc = null;
    spawn = null;

    setState(EventState.PARTICIPATING);
    return true;
  }

  public static boolean startFight()
  {
    setState(EventState.STARTING);

    if ((_teams[0].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS)) {
      setState(EventState.INACTIVE);
      _teams[0].cleanMe();
      _teams[1].cleanMe();
      _ips.clear();
      unSpawnNpc();
      return false;
    }

    openDoors();
    setState(EventState.STARTED);
    TvTEventTeam team;
    for (team : _teams) {
      for (String playerName : team.getParticipatedPlayerNames()) {
        L2PcInstance player = (L2PcInstance)team.getParticipatedPlayers().get(playerName);

        if (player == null)
        {
          continue;
        }
        player.setChannel(8);
        player.setTvtPassive(true);

        new TvTEventTeleporter(player, team.getCoordinates(), false, false);
      }
    }
    ThreadPoolManager.getInstance().scheduleGeneral(new CheckZone(), 30000L);
    return true;
  }

  public static String calculateRewards()
  {
    if (_teams[0].getPoints() == _teams[1].getPoints()) {
      if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
      {
        setState(EventState.REWARDING);
        return Static.TVT_FINISHED_INACTIVE;
      }

      sysMsgToAllParticipants(Static.TVT_TEAMS_IN_TIE);
    }

    while (_teams[0].getPoints() == _teams[1].getPoints())
      try {
        Thread.sleep(1L);
      }
      catch (InterruptedException ie)
      {
      }
    setState(EventState.REWARDING);

    byte teamId = (byte)(_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1);
    TvTEventTeam team = _teams[teamId];

    for (String playerName : team.getParticipatedPlayerNames()) {
      L2PcInstance player = (L2PcInstance)team.getParticipatedPlayers().get(playerName);
      if ((player == null) || (
        (Config.TVT_NO_PASSIVE) && (player.isTvtPassive())))
      {
        continue;
      }
      for (int[] reward : Config.TVT_EVENT_REWARDS)
      {
        PcInventory inv = player.getInventory();

        if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
          inv.addItem("TvT Event", reward[0], reward[1], player, player);
        else {
          for (int i = 0; i < reward[1]; i++) {
            inv.addItem("TvT Event", reward[0], 1, player, player);
          }
        }

        SystemMessage sm = null;
        if (reward[1] > 1)
          sm = SystemMessage.id(SystemMessageId.EARNED_S2_S1_S).addItemName(reward[0]).addNumber(reward[1]);
        else {
          sm = SystemMessage.id(SystemMessageId.EARNED_ITEM).addItemName(reward[0]);
        }
        player.sendPacket(sm);
        sm = null;
      }

      StatusUpdate statusUpdate = new StatusUpdate(player.getObjectId());

      statusUpdate.addAttribute(14, player.getCurrentLoad());
      player.sendPacket(statusUpdate);

      NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(0);

      npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u041F\u043E\u0431\u0435\u0434\u0430! \u0412\u0437\u0433\u043B\u044F\u043D\u0438\u0442\u0435 \u0432\u0438\u043D\u0432\u0435\u043D\u0442\u0430\u0440\u044C, \u043F\u043E\u043B\u0443\u0447\u0435\u043D\u0430 \u043D\u0430\u0433\u0440\u0430\u0434\u0430.</body></html>");
      player.sendPacket(npcHtmlMessage);
      player.setTvtPassive(true);
    }

    String event_end = Static.TVT_S1_WIN_KILLS_S2.replaceAll("%a%", team.getName());
    return event_end.replaceAll("%b%", String.valueOf(team.getPoints()));
  }

  public static void stopFight()
  {
    setState(EventState.INACTIVATING);
    unSpawnNpc();
    closeDoors();
    TvTEventTeam team;
    for (team : _teams) {
      for (String playerName : team.getParticipatedPlayerNames()) {
        L2PcInstance playerInstance = (L2PcInstance)team.getParticipatedPlayers().get(playerName);

        if (playerInstance == null)
        {
          continue;
        }
        new TvTEventTeleporter(playerInstance, getRandomLoc(), false, false);
      }
    }

    _teams[0].cleanMe();
    _teams[1].cleanMe();
    _ips.clear();
    setState(EventState.INACTIVE);
  }

  public static int[] getRandomLoc() {
    int[] rndPoint = new int[3];
    Location loc = (Location)_npcLocs.get(Rnd.get(_npcLocsSize));

    rndPoint[0] = loc.x;
    rndPoint[1] = loc.y;
    rndPoint[2] = loc.z;

    return rndPoint;
  }

  public static synchronized boolean addParticipant(L2PcInstance player)
  {
    if (player == null) {
      return false;
    }

    byte teamId = 0;

    if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount()) {
      teamId = (byte)Rnd.get(2);
    }
    else if (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount())
      teamId = 1;
    else {
      teamId = 0;
    }

    if (!Config.EVENTS_SAME_IP) {
      _ips.add(player.getIP());
    }
    return _teams[teamId].addPlayer(player);
  }

  public static boolean removeParticipant(String playerName)
  {
    byte teamId = getParticipantTeamId(playerName);

    if (teamId == -1) {
      return false;
    }

    _teams[teamId].removePlayer(playerName);
    if (!Config.EVENTS_SAME_IP) {
      L2PcInstance player = L2World.getInstance().getPlayer(playerName);
      if (player != null) {
        _ips.remove(player.getIP());
      }
    }
    return true;
  }

  public static void sysMsgToAllParticipants(String message)
  {
    for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values()) {
      if (playerInstance != null) {
        playerInstance.sendMessage(message);
      }
    }

    for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
      if (playerInstance != null)
        playerInstance.sendMessage(message);
  }

  public static void spMsgToAllParticipants(String message)
  {
    CreatureSay cs = new CreatureSay(0, 18, " ", message);

    for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values()) {
      if (playerInstance != null) {
        playerInstance.sendPacket(cs);
      }
    }

    for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
      if (playerInstance != null)
        playerInstance.sendPacket(cs);
  }

  private static void closeDoors()
  {
    for (Iterator i$ = Config.TVT_EVENT_DOOR_IDS.iterator(); i$.hasNext(); ) { int doorId = ((Integer)i$.next()).intValue();
      L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));

      if (doorInstance != null)
        doorInstance.closeMe();
    }
  }

  private static void openDoors()
  {
    for (Iterator i$ = Config.TVT_EVENT_DOOR_IDS.iterator(); i$.hasNext(); ) { int doorId = ((Integer)i$.next()).intValue();
      L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));

      if (doorInstance != null)
        doorInstance.openMe();
    }
  }

  private static void unSpawnNpc()
  {
    L2Spawn spawn = null;
    FastList.Node n = _npcSpawns.head(); for (FastList.Node end = _npcSpawns.tail(); (n = n.getNext()) != end; ) {
      spawn = (L2Spawn)n.getValue();
      if ((spawn == null) || (spawn.getLastSpawn() == null))
      {
        continue;
      }
      spawn.getLastSpawn().deleteMe();
    }
    spawn = null;
    _npcSpawns.clear();
  }

  public static void onLogout(L2PcInstance player)
  {
    if ((player == null) || (!Config.TVT_EVENT_ENABLED)) {
      return;
    }

    if (removeParticipant(player.getName())) {
      player.setXYZ(82737, 148571, -3470);
      player.setChannel(1);
    }
  }

  public static synchronized void onBypass(String command, L2PcInstance playerInstance)
  {
    if ((playerInstance == null) || (!isParticipating())) {
      return;
    }

    if (command.equals("tvt_event_participation")) {
      NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(0);
      int playerLevel = playerInstance.getLevel();

      if (playerInstance.isCursedWeaponEquiped())
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u041F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0438 \u0441 \u043F\u0440\u043E\u043A\u043B\u044F\u0442\u044B\u043C \u043E\u0440\u0443\u0436\u0438\u0435\u043C \u043D\u0435 \u043C\u043E\u0433\u0443\u0442 \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u043E\u0432\u0430\u0442\u044C.</body></html>");
      else if (playerInstance.getKarma() > 0)
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u0423 \u0432\u0430\u0441 \u043F\u043B\u043E\u0445\u0430\u044F \u043A\u0430\u0440\u043C\u0430.</body></html>");
      else if ((Config.TVT_NOBL) && (!playerInstance.isNoble()))
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u0422\u043E\u043B\u044C\u043A\u043E \u043D\u043E\u0431\u043B\u0435\u0441\u0441\u044B \u043C\u043E\u0433\u0443\u0442 \u043F\u0440\u0438\u043D\u0438\u043C\u0430\u0442\u044C \u0443\u0447\u0430\u0441\u0442\u0438\u0435.</body></html>");
      else if ((_teams[0].getParticipatedPlayerCount() >= Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() >= Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u041C\u0435\u0441\u0442 \u043D\u0435\u0442!</body></html>");
      else if ((playerLevel < Config.TVT_EVENT_MIN_LVL) || (playerLevel > Config.TVT_EVENT_MAX_LVL))
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u0422\u043E\u043B\u044C\u043A\u043E \u043F\u0435\u0440\u0441\u043E\u043D\u0430\u0436\u0438 \u0441 " + Config.TVT_EVENT_MIN_LVL + " \u0443\u0440\u043E\u0432\u043D\u044F \u043F\u043E " + Config.TVT_EVENT_MAX_LVL + " \u043C\u043E\u0433\u0443\u0442 \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u043E\u0432\u0430\u0442\u044C.</body></html>");
      else if ((_teams[0].getParticipatedPlayerCount() > Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() > Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u041C\u0435\u0441\u0442 \u043D\u0435\u0442! \u041C\u0430\u043A\u0441\u0438\u043C\u0443\u043C " + Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS + "  \u0443\u0447\u0430\u0441\u0442\u043D\u0438\u043A\u043E\u0432 \u0432 \u043A\u0430\u0436\u0434\u043E\u0439 \u043A\u043E\u043C\u0430\u043D\u0434\u0435.</body></html>");
      else if ((!Config.EVENTS_SAME_IP) && (_ips.contains(playerInstance.getIP())))
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u041A\u0442\u043E-\u0442\u043E \u0443\u0436\u0435 \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u0443\u0435\u0442 \u0441 \u0432\u0430\u0448\u0435\u0433\u043E IP.</body></html>");
      else if (addParticipant(playerInstance)) {
        npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u0412\u044B \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043B\u0438\u0441\u044C.</body></html>");
      }
      else {
        return;
      }

      playerInstance.sendPacket(npcHtmlMessage);
    } else if (command.equals("tvt_event_remove_participation")) {
      removeParticipant(playerInstance.getName());

      NpcHtmlMessage npcHtmlMessage = NpcHtmlMessage.id(0);

      npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>\u042F \u043D\u0435 \u0442\u0440\u0443\u0441, \u043D\u043E \u044F \u0431\u043E\u044E\u0441\u044C?</body></html>");
      playerInstance.sendPacket(npcHtmlMessage);
    }
  }

  public static boolean onAction(String playerName, String targetPlayerName)
  {
    if (!isStarted()) {
      return true;
    }

    L2PcInstance playerInstance = L2World.getInstance().getPlayer(playerName);

    if (playerInstance == null) {
      return false;
    }

    if (playerInstance.isGM()) {
      return true;
    }

    byte playerTeamId = getParticipantTeamId(playerName);
    byte targetPlayerTeamId = getParticipantTeamId(targetPlayerName);

    if ((playerTeamId != -1) && (targetPlayerTeamId == -1)) {
      return true;
    }

    if ((playerTeamId == -1) && (targetPlayerTeamId != -1)) {
      return false;
    }

    return (playerTeamId == -1) || (targetPlayerTeamId == -1) || (playerTeamId != targetPlayerTeamId) || (Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED);
  }

  public static boolean onPotionUse(String playerName, int itemId)
  {
    if ((!isStarted()) || (!isPlayerParticipant(playerName))) {
      return true;
    }

    if (Config.TVT_WHITE_POTINS.contains(Integer.valueOf(itemId))) {
      return true;
    }

    switch (itemId) {
    case 734:
    case 735:
    case 1062:
    case 1374:
    case 1375:
    case 1539:
    case 6035:
    case 6036:
      return true;
    }

    return Config.TVT_EVENT_POTIONS_ALLOWED;
  }

  public static boolean onEscapeUse(String playerName)
  {
    if (!isStarted()) {
      return true;
    }

    return !isPlayerParticipant(playerName);
  }

  public static boolean onItemSummon(String playerName)
  {
    if (!isStarted()) {
      return true;
    }

    return (!isPlayerParticipant(playerName)) || (Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED);
  }

  public static void onKill(L2Character killer, L2PcInstance killedPlayerInstance)
  {
    if ((killer == null) || (killedPlayerInstance == null) || ((!killer.isPlayer()) && (!killer.isPet()) && (!killer.isSummon())) || (!isStarted()))
    {
      return;
    }

    L2PcInstance pcKiller = killer.getPlayer();
    if (pcKiller == null) {
      return;
    }

    pcKiller.setTvtPassive(false);
    String playerName = pcKiller.getName();
    byte killerTeamId = getParticipantTeamId(playerName);

    playerName = killedPlayerInstance.getName();

    byte killedTeamId = getParticipantTeamId(playerName);

    if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId)) {
      _teams[killerTeamId].increasePoints();
    }

    if (killedTeamId != -1)
      new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);
  }

  private static void setState(EventState state)
  {
    synchronized (_state) {
      _state = state;
    }
  }

  public static boolean isInactive()
  {
    boolean isInactive;
    synchronized (_state) {
      isInactive = _state == EventState.INACTIVE;
    }

    return isInactive;
  }

  public static boolean isInactivating()
  {
    boolean isInactivating;
    synchronized (_state) {
      isInactivating = _state == EventState.INACTIVATING;
    }

    return isInactivating;
  }

  public static boolean isParticipating()
  {
    boolean isParticipating;
    synchronized (_state) {
      isParticipating = _state == EventState.PARTICIPATING;
    }

    return isParticipating;
  }

  public static boolean isStarting()
  {
    boolean isStarting;
    synchronized (_state) {
      isStarting = _state == EventState.STARTING;
    }

    return isStarting;
  }

  public static boolean isStarted()
  {
    boolean isStarted;
    synchronized (_state) {
      isStarted = _state == EventState.STARTED;
    }

    return isStarted;
  }

  public static boolean isRewarding()
  {
    boolean isRewarding;
    synchronized (_state) {
      isRewarding = _state == EventState.REWARDING;
    }

    return isRewarding;
  }

  public static byte getParticipantTeamId(String playerName)
  {
    return (byte)(_teams[1].containsPlayer(playerName) ? 1 : _teams[0].containsPlayer(playerName) ? 0 : -1);
  }

  public static int[] getParticipantTeamCoordinates(String playerName)
  {
    return _teams[1].containsPlayer(playerName) ? _teams[1].getCoordinates() : _teams[0].containsPlayer(playerName) ? _teams[0].getCoordinates() : null;
  }

  public static boolean isPlayerParticipant(String playerName)
  {
    return (_teams[0].containsPlayer(playerName)) || (_teams[1].containsPlayer(playerName));
  }

  public static int getParticipatedPlayersCount()
  {
    return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
  }

  public static String[] getTeamNames()
  {
    return new String[] { _teams[0].getName(), _teams[1].getName() };
  }

  public static int[] getTeamsPlayerCounts()
  {
    return new int[] { _teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount() };
  }

  public static int[] getTeamsPoints()
  {
    return new int[] { _teams[0].getPoints(), _teams[1].getPoints() };
  }

  static class CheckZone
    implements Runnable
  {
    public void run()
    {
      TvTEventTeam team;
      for (team : TvTEvent._teams)
        for (String playerName : team.getParticipatedPlayerNames()) {
          L2PcInstance player = (L2PcInstance)team.getParticipatedPlayers().get(playerName);
          if ((player == null) || 
            (Config.TVT_POLY.contains(player.getX(), player.getY(), player.getZ())))
          {
            continue;
          }
          TvTEvent.removeParticipant(player.getName());
          player.setChannel(1);
          player.setTvtPassive(true);
          player.teleToClosestTown();
        }
    }
  }

  static enum EventState
  {
    INACTIVE, 
    INACTIVATING, 
    PARTICIPATING, 
    STARTING, 
    STARTED, 
    REWARDING;
  }
}