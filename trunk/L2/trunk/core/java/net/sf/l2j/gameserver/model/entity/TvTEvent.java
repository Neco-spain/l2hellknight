package net.sf.l2j.gameserver.model.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.protection.nProtect;
import net.sf.protection.nProtect.RestrictionType;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.StringUtil;
import net.sf.l2j.util.Rnd;

public class TvTEvent
{
  protected static final Logger _log = Logger.getLogger(TvTEvent.class.getName());

  private static final String htmlPath = "data/html/mods/TvTEvent/";

  private static TvTEventTeam[] _teams = new TvTEventTeam[2];

  private static EventState _state = EventState.INACTIVE;

  private static L2Spawn _npcSpawn = null;

  private static L2NpcInstance _lastNpcSpawn = null;

  private static int _TvTEventInstance = 0;
  public static ArrayList<String> _listedIps;

  public static void init()
  {
    _teams[0] = new TvTEventTeam(Config.TVT_EVENT_TEAM_1_NAME, Config.TVT_EVENT_TEAM_1_COORDINATES);
    _teams[1] = new TvTEventTeam(Config.TVT_EVENT_TEAM_2_NAME, Config.TVT_EVENT_TEAM_2_COORDINATES);

    if (Config.TVT_RESTORE_PLAYER_POS) {
      TvTEventTeleporter.initializeRestoreMap();
    }
    if (Config.TVT_SAME_IP)
      _listedIps = new ArrayList<String>(Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS * 2);
  }

  public static boolean startParticipation()
  {
    L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.TVT_EVENT_PARTICIPATION_NPC_ID);

    if (tmpl == null)
    {
      _log.log(Level.WARNING, "TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
      return false;
    }

    try
    {
      _npcSpawn = new L2Spawn(tmpl);

      _npcSpawn.setLocx(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
      _npcSpawn.setLocy(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
      _npcSpawn.setLocz(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
      _npcSpawn.setAmount(1);
      _npcSpawn.setHeading(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
      _npcSpawn.setRespawnDelay(1);

      SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
      _npcSpawn.init();
      _lastNpcSpawn = _npcSpawn.getLastSpawn();
      _lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
      _lastNpcSpawn.setTitle("TvT Event Participation");
      _lastNpcSpawn.isAggressive();
      _lastNpcSpawn.decayMe();
      _lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
      _lastNpcSpawn.broadcastPacket(new MagicSkillUser(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "TvTEngine[TvT.startParticipation()]: exception: exception: " + e.getMessage(), e);
      return false;
    }

    setState(EventState.PARTICIPATING);
    return true;
  }

  private static int highestLevelPcInstanceOf(Map<Integer, L2PcInstance> players)
  {
    int maxLevel = -2147483648; int maxLevelId = -1;
    for (L2PcInstance player : players.values())
    {
      if (player.getLevel() >= maxLevel)
      {
        maxLevel = player.getLevel();
        maxLevelId = player.getObjectId();
      }
    }
    return maxLevelId;
  }

  @SuppressWarnings("unused")
public static boolean startFight()
  {
    setState(EventState.STARTING);

    Map<Integer, L2PcInstance> allParticipants = new FastMap<Integer, L2PcInstance>();
    allParticipants.putAll(_teams[0].getParticipatedPlayers());
    allParticipants.putAll(_teams[1].getParticipatedPlayers());
    _teams[0].cleanMe();
    _teams[1].cleanMe();
    int[] balance = { 0, 0 }; int priority = 0;

    while (!allParticipants.isEmpty())
    {
      int highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
      L2PcInstance highestLevelPlayer = (L2PcInstance)allParticipants.get(Integer.valueOf(highestLevelPlayerId));
      allParticipants.remove(Integer.valueOf(highestLevelPlayerId));
      _teams[priority].addPlayer(highestLevelPlayer);
      balance[priority] += highestLevelPlayer.getLevel();

      if (allParticipants.isEmpty()) {
        break;
      }
      priority = 1 - priority;
      highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
      highestLevelPlayer = (L2PcInstance)allParticipants.get(Integer.valueOf(highestLevelPlayerId));
      allParticipants.remove(Integer.valueOf(highestLevelPlayerId));
      _teams[priority].addPlayer(highestLevelPlayer);
      balance[priority] += highestLevelPlayer.getLevel();

      priority = balance[0] > balance[1] ? 1 : 0;
    }

    if ((_teams[0].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS) || (_teams[1].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS))
    {
      setState(EventState.INACTIVE);

      _teams[0].cleanMe();
      _teams[1].cleanMe();

      unSpawnNpc();
      return false;
    }
    closeDoors(Config.TVT_DOORS_IDS);

    setState(EventState.STARTED);
    for (TvTEventTeam team : _teams)
    {
      for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
      {
        boolean _canreward = true;

        if (playerInstance != null)
        {
          new TvTEventTeleporter(playerInstance, team.getCoordinates(), false, false);
        }
      }
    }

    return true;
  }

  public static String calculateRewards()
  {
    if (_teams[0].getPoints() == _teams[1].getPoints())
    {
      if ((_teams[0].getParticipatedPlayerCount() == 0) || (_teams[1].getParticipatedPlayerCount() == 0))
      {
        setState(EventState.REWARDING);

        return "TvT: Event has ended. No team won due to inactivity!";
      }

      sysMsgToAllParticipants("TvT Event: Event has ended, both teams have tied.");
      if (Config.TVT_REWARD_TEAM_TIE)
      {
        rewardTeam(_teams[0]);
        rewardTeam(_teams[1]);
        return "TvT: Event has ended with both teams tying.";
      }

      return "TvT Event: Event has ended with both teams tying.";
    }

    setState(EventState.REWARDING);

    byte teamId = (byte)(_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1);
    TvTEventTeam team = _teams[teamId];
    rewardTeam(team);

    return "TvT: Event finish. Team " + team.getName() + " won with " + team.getPoints() + " kills.";
  }

  private static void rewardTeam(TvTEventTeam team)
  {
    for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
    {
      boolean _canreward = true;

      if (playerInstance == null)
      {
        continue;
      }

      if (Config.TVT_REWARD_ONLY_KILLERS)
      {
        if (playerInstance.getTvTKills() <= 0) {
          _canreward = false;
        }
      }
      SystemMessage systemMessage = null;

      if (_canreward)
      {
        for (int[] reward : Config.TVT_EVENT_REWARDS)
        {
          PcInventory inv = playerInstance.getInventory();

          if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
          {
            inv.addItem("TvT Event", reward[0], reward[1], playerInstance, playerInstance);

            if (reward[1] > 1)
            {
              systemMessage = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
              systemMessage.addItemName(reward[0]);
              systemMessage.addNumber(reward[1]);
            }
            else
            {
              systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
              systemMessage.addItemName(reward[0]);
            }

            playerInstance.sendPacket(systemMessage);
          }
          else
          {
            for (int i = 0; i < reward[1]; i++)
            {
              inv.addItem("TvT Event", reward[0], 1, playerInstance, playerInstance);
              systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
              systemMessage.addItemName(reward[0]);
              playerInstance.sendPacket(systemMessage);
            }
          }
        }

        StatusUpdate statusUpdate = new StatusUpdate(playerInstance.getObjectId());
        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

        statusUpdate.addAttribute(14, playerInstance.getCurrentLoad());
        npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward.htm"));
        playerInstance.sendPacket(statusUpdate);
        playerInstance.sendPacket(npcHtmlMessage);
      }
      else
      {
        StatusUpdate statusUpdate = new StatusUpdate(playerInstance.getObjectId());
        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

        statusUpdate.addAttribute(14, playerInstance.getCurrentLoad());
        npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Reward0.htm"));
        playerInstance.sendPacket(statusUpdate);
        playerInstance.sendPacket(npcHtmlMessage);
      }
    }
  }

  @SuppressWarnings("unused")
public static void stopFight()
  {
    setState(EventState.INACTIVATING);
    int RESTORE_PLAYER = 0;
    unSpawnNpc();

    openDoors(Config.TVT_DOORS_IDS);

    for (TvTEventTeam team : _teams)
    {
      for (L2PcInstance playerInstance : team.getParticipatedPlayers().values())
      {
        boolean _canreward = true;

        if (playerInstance != null)
        {
          new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
          RESTORE_PLAYER++;
        }
      }
    }

    if (Config.TVT_SAME_IP) 
    {
      _listedIps.clear();
    }

    _teams[0].cleanMe();
    _teams[1].cleanMe();

    setState(EventState.INACTIVE);
  
    	if (RESTORE_PLAYER == 2)
    	{
    		if (Config.TVT_RESTORE_PLAYER_POS) 
    		{
    		   TvTEventTeleporter.clearRestoreMap();
    		   RESTORE_PLAYER = 0;
    		}
    	}
    
  }

  public static synchronized boolean addParticipant(L2PcInstance playerInstance)
  {
    if (playerInstance == null)
    {
      return false;
    }
    
    if(!nProtect.getInstance().checkRestriction(playerInstance, RestrictionType.RESTRICT_EVENT, new Object[]
                                                                                               		    {
                                                                                               		        TvTEvent.class, _teams
                                                                                               		    }))
                                                                                               		    return false;

    byte teamId = 0;

    if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
    {
      teamId = (byte)Rnd.get(2);
    }
    else
    {
      teamId = (byte)(_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
    }

    if (Config.TVT_SAME_IP)
    {
    	if (playerInstance != null)
        {
    		String host = playerInstance.getClient().getConnection().getInetAddress().getHostAddress();
    		if ((host != null) && (!_listedIps.contains(host)))
    			_listedIps.add(host);
    	else 
    	{
    		return false;
    	}
    }
    }
    return _teams[teamId].addPlayer(playerInstance);
  }

  public static boolean removeParticipant(int playerObjectId)
  {
    byte teamId = getParticipantTeamId(playerObjectId);

    if (teamId != -1)
    {
      _teams[teamId].removePlayer(playerObjectId);
      return true;
    }

    return false;
  }

  public static boolean payParticipationFee(L2PcInstance playerInstance)
  {
    int itemId = Config.TVT_EVENT_PARTICIPATION_FEE[0];
    int itemNum = Config.TVT_EVENT_PARTICIPATION_FEE[1];
    if ((itemId == 0) || (itemNum == 0)) {
      return true;
    }
    if (playerInstance.getInventory().getInventoryItemCount(itemId, -1) < itemNum) {
      return false;
    }
    return playerInstance.destroyItemByItemId("TvT Participation Fee", itemId, itemNum, _lastNpcSpawn, true);
  }

  public static String getParticipationFee()
  {
    int itemId = Config.TVT_EVENT_PARTICIPATION_FEE[0];
    int itemNum = Config.TVT_EVENT_PARTICIPATION_FEE[1];

    if ((itemId == 0) || (itemNum == 0)) {
      return "-";
    }
    return StringUtil.concat(new String[] { String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName() });
  }

  public static void sysMsgToAllParticipants(String message)
  {
    for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
    {
      if (playerInstance != null)
      {
        playerInstance.sendMessage(message);
      }
    }

    for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
    {
      if (playerInstance != null)
      {
        playerInstance.sendMessage(message);
      }
    }
  }
  /*
  public static void sysMsgToAll(SystemMessageId message)
  {
	SystemMessage sm = new SystemMessage(message);
    for (L2PcInstance playerInstance : L2World.getInstance().getAllPlayers())
    {
      if (playerInstance != null)
      {
        playerInstance.sendPacket(sm);
      }
    }
    sm = null;
  }
  */

  private static void closeDoors(List<Integer> doors)
  {
    for (Iterator<Integer> i$ = doors.iterator(); i$.hasNext(); ) { int doorId = ((Integer)i$.next()).intValue();

      L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));

      if (doorInstance != null)
      {
        doorInstance.closeMe();
      }
    }
  }

  private static void openDoors(List<Integer> doors)
  {
    for (Iterator<Integer> i$ = doors.iterator(); i$.hasNext(); ) { int doorId = ((Integer)i$.next()).intValue();

      L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(Integer.valueOf(doorId));

      if (doorInstance != null)
      {
        doorInstance.openMe();
      }
    }
  }

  private static void unSpawnNpc()
  {
    _lastNpcSpawn.deleteMe();
    SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn(), false);

    _npcSpawn.stopRespawn();
    _npcSpawn = null;
    _lastNpcSpawn = null;
  }

  public static void onLogin(L2PcInstance playerInstance)
  {
    if ((playerInstance == null) || ((!isStarting()) && (!isStarted())))
    {
      return;
    }

    byte teamId = getParticipantTeamId(playerInstance.getObjectId());

    if (teamId == -1)
    {
      return;
    }

    _teams[teamId].addPlayer(playerInstance);
    new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
  }

  public static void onLogout(L2PcInstance playerInstance)
  {

    if ((playerInstance != null) && ((isStarting()) || (isStarted()) || (isParticipating())))
    {
      if (removeParticipant(playerInstance.getObjectId()))
        playerInstance.setXYZInvisible(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
      if (Config.TVT_SAME_IP)
      {
        if (playerInstance != null)
        {
          if (_listedIps.contains(playerInstance._tvtIp)) 
          {
            _listedIps.remove(playerInstance._tvtIp);
          }
        }
      }
    }
  }

  public static synchronized void onBypass(String command, L2PcInstance playerInstance)
  {
    if ((playerInstance == null) || (!isParticipating())) {
      return;
    }

    if (command.equals("tvt_event_participation"))
    {
      NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
      int playerLevel = playerInstance.getLevel();

      if ((Config.TVT_SAME_IP) && (isDualBoxing(playerInstance)))
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "DualBox.htm");
        if (htmContent != null)
        {
          npcHtmlMessage.setHtml(htmContent);
          npcHtmlMessage.replace("%pName%", playerInstance.getName());
          String host = playerInstance.getClient().getConnection().getInetAddress().getHostAddress();
          npcHtmlMessage.replace("%ip%", host);
        }
      }
      else if (playerInstance.isCursedWeaponEquiped())
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "CursedWeaponEquipped.htm");
        if (htmContent != null)
          npcHtmlMessage.setHtml(htmContent);
      }
      else if (Olympiad.getInstance().isRegistered(playerInstance))
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "Olympiad.htm");
        if (htmContent != null)
          npcHtmlMessage.setHtml(htmContent);
      }
      else if (playerInstance.getKarma() > 0)
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "Karma.htm");
        if (htmContent != null)
          npcHtmlMessage.setHtml(htmContent);
      }
      else if ((playerLevel < Config.TVT_EVENT_MIN_LVL) || (playerLevel > Config.TVT_EVENT_MAX_LVL))
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "Level.htm");
        if (htmContent != null)
        {
          npcHtmlMessage.setHtml(htmContent);
          npcHtmlMessage.replace("%min%", String.valueOf(Config.TVT_EVENT_MIN_LVL));
          npcHtmlMessage.replace("%max%", String.valueOf(Config.TVT_EVENT_MAX_LVL));
        }
      }
      else if ((_teams[0].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS) && (_teams[1].getParticipatedPlayerCount() == Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS))
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "TeamsFull.htm");
        if (htmContent != null)
        {
          npcHtmlMessage.setHtml(htmContent);
          npcHtmlMessage.replace("%max%", String.valueOf(Config.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
        }
      }
      else if (!payParticipationFee(playerInstance))
      {
        String htmContent = HtmCache.getInstance().getHtm(htmlPath + "ParticipationFee.htm");
        if (htmContent != null)
        {
          npcHtmlMessage.setHtml(htmContent);
          npcHtmlMessage.replace("%fee%", getParticipationFee());
        }
      }
      else if (addParticipant(playerInstance)) {
        npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Registered.htm"));
      } else {
        return;
      }
      playerInstance.sendPacket(npcHtmlMessage);
    }
    else if (command.equals("tvt_event_remove_participation"))
    {
      removeParticipant(playerInstance.getObjectId());
      
      if (Config.TVT_SAME_IP)
      {
        if (playerInstance != null)
        {
          String host = playerInstance.getClient().getConnection().getInetAddress().getHostAddress();
          if (_listedIps.contains(host)) 
          {
            _listedIps.remove(host);
          }
        }
      }

      NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

      npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(htmlPath + "Unregistered.htm"));
      playerInstance.sendPacket(npcHtmlMessage);
    }
  }

  public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
  {
    if ((playerInstance == null) || (!isStarted()))
    {
      return true;
    }

    if (playerInstance.isGM())
    {
      return true;
    }

    byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
    byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);

    if (((playerTeamId != -1) && (targetedPlayerTeamId == -1)) || ((playerTeamId == -1) && (targetedPlayerTeamId != -1)))
    {
      return false;
    }

    return (playerTeamId == -1) || (targetedPlayerTeamId == -1) || (playerTeamId != targetedPlayerTeamId) || (playerInstance.getObjectId() == targetedPlayerObjectId) || (Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED);
  }

  public static boolean onScrollUse(int playerObjectId)
  {
    if (!isStarted()) {
      return true;
    }

    return (!isPlayerParticipant(playerObjectId)) || (Config.TVT_EVENT_SCROLL_ALLOWED);
  }

  public static boolean onEscapeUse(int playerObjectId)
  {
    if (!isStarted())
    {
      return true;
    }

    return !isPlayerParticipant(playerObjectId);
  }

  public static boolean onItemSummon(int playerObjectId)
  {
    if (!isStarted())
    {
      return true;
    }

    return (!isPlayerParticipant(playerObjectId)) || (Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED);
  }

  public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
  {
    if ((killedPlayerInstance == null) || (!isStarted()))
    {
      return;
    }

    byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());

    if (killedTeamId == -1)
    {
      return;
    }

    new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);

    if (killerCharacter == null)
    {
      return;
    }

    L2PcInstance killerPlayerInstance = null;

    if (((killerCharacter instanceof L2PetInstance)) || ((killerCharacter instanceof L2SummonInstance)))
    {
      killerPlayerInstance = ((L2Summon)killerCharacter).getOwner();

      if (killerPlayerInstance == null)
      {
        return;
      }
    }
    else if ((killerCharacter instanceof L2PcInstance))
    {
      killerPlayerInstance = (L2PcInstance)killerCharacter;
    }
    else
    {
      return;
    }

    byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());
    CreatureSay cs;
    if ((killerTeamId != -1) && (killedTeamId != -1) && (killerTeamId != killedTeamId))
    {
      TvTEventTeam killerTeam = _teams[killerTeamId];

      killerTeam.increasePoints();

      if (Config.TVT_REWARD_ONLY_KILLERS) {
        killerPlayerInstance.increaseTvTKills();
      }
      cs = new CreatureSay(killerPlayerInstance.getObjectId(), 2, killerPlayerInstance.getName(), "I have killed " + killedPlayerInstance.getName() + "!");

      for (L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
      {
        if (playerInstance != null)
        {
          playerInstance.sendPacket(cs);
        }
      }
    }
  }

  public static void onTeleported(L2PcInstance playerInstance)
  {
    if ((!isStarted()) || (playerInstance == null) || (!isPlayerParticipant(playerInstance.getObjectId()))) {
      return;
    }

    if (Config.TVT_EVENT_RESTORE_CPHPMP)
    {
      playerInstance.setCurrentCp(playerInstance.getMaxCp());
      playerInstance.setCurrentHpMp(playerInstance.getMaxHp(), playerInstance.getMaxMp());
    }
  }

  public static final boolean checkForTvTSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
  {
    if (!isStarted()) {
      return true;
    }
    int sourcePlayerId = source.getObjectId();
    int targetPlayerId = target.getObjectId();
    boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
    boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);

    if ((!isSourceParticipant) && (!isTargetParticipant)) {
      return true;
    }
    if ((!isSourceParticipant) || (!isTargetParticipant)) {
      return false;
    }
    if (getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
    {
      if (!skill.isOffensive())
        return false;
    }
    return true;
  }

  private static void setState(EventState state)
  {
    synchronized (_state)
    {
      _state = state;
    }
  }

  public static boolean isInactive()
  {
    boolean isInactive;
    synchronized (_state)
    {
      isInactive = _state == EventState.INACTIVE;
    }

    return isInactive;
  }

  public static boolean isInactivating()
  {
    boolean isInactivating;
    synchronized (_state)
    {
      isInactivating = _state == EventState.INACTIVATING;
    }

    return isInactivating;
  }

  public static boolean isParticipating()
  {
    boolean isParticipating = false;
    synchronized (_state)
    {
      isParticipating = _state == EventState.PARTICIPATING;
    }

    return isParticipating;
  }

  public static boolean isStarting()
  {
    boolean isStarting;
    synchronized (_state)
    {
      isStarting = _state == EventState.STARTING;
    }

    return isStarting;
  }

  public static boolean isStarted()
  {
    boolean isStarted;
    synchronized (_state)
    {
      isStarted = _state == EventState.STARTED;
    }

    return isStarted;
  }

  public static boolean isRewarding()
  {
    boolean isRewarding;
    synchronized (_state)
    {
      isRewarding = _state == EventState.REWARDING;
    }

    return isRewarding;
  }

  public static byte getParticipantTeamId(int playerObjectId)
  {
    return (byte)(_teams[1].containsPlayer(playerObjectId) ? 1 : _teams[0].containsPlayer(playerObjectId) ? 0 : -1);
  }

  public static TvTEventTeam getParticipantTeam(int playerObjectId)
  {
    return _teams[1].containsPlayer(playerObjectId) ? _teams[1] : _teams[0].containsPlayer(playerObjectId) ? _teams[0] : null;
  }

  public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId)
  {
    return _teams[1].containsPlayer(playerObjectId) ? _teams[0] : _teams[0].containsPlayer(playerObjectId) ? _teams[1] : null;
  }

  public static int[] getParticipantTeamCoordinates(int playerObjectId)
  {
    return _teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : null;
  }

  public static boolean isPlayerParticipant(int playerObjectId)
  {
    if ((!isParticipating()) && (!isStarting()) && (!isStarted()))
    {
      return false;
    }

    return (_teams[0].containsPlayer(playerObjectId)) || (_teams[1].containsPlayer(playerObjectId));
  }

  public static int getParticipatedPlayersCount()
  {
    if ((!isParticipating()) && (!isStarting()) && (!isStarted()))
    {
      return 0;
    }

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

  public static int getTvTEventInstance()
  {
    return _TvTEventInstance;
  }

  private static boolean isDualBoxing(L2PcInstance player)
  {
    if (player == null) return true;

    String host = player.getClient().getConnection().getInetAddress().getHostAddress();

    return _listedIps.contains(host);
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