package net.sf.l2j.gameserver.model.entity.events;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2Radar.RadarOnPlayer;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.status.NpcStatus;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.RadarControl;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class CTF
{
  private static final Logger _log = Logger.getLogger(CTF.class.getName());
  private static int _FlagNPC = 35062; private static int _FLAG_IN_HAND_ITEM_ID = 6718;
  public static String _eventName = new String();
  public static String _eventDesc = new String();
  public static String _topTeam = new String();
  public static String _joiningLocationName = new String();
  public static Vector<String> _teams = new Vector();
  public static Vector<String> _savePlayers = new Vector();
  public static Vector<String> _savePlayerTeams = new Vector();
  public static List<L2PcInstance> _players = new FastList();
  public static List<L2PcInstance> _playersShuffle = new Vector();
  public static Vector<Integer> _teamPlayersCount = new Vector();
  public static Vector<Integer> _teamColors = new Vector();
  public static Vector<Integer> _teamsX = new Vector();
  public static Vector<Integer> _teamsY = new Vector();
  public static Vector<Integer> _teamsZ = new Vector();
  public static boolean _joining = false;
  public static boolean _teleport = false;
  public static boolean _started = false;
  public static boolean _sitForced = false;
  public static L2Spawn _npcSpawn;
  public static int _npcId = 0;
  public static int _npcX = 0;
  public static int _npcY = 0;
  public static int _npcZ = 0;
  public static int _npcHeading = 0;
  public static int _rewardId = 0;
  public static int _rewardAmount = 0;
  public static int _minlvl = 0;
  public static int _maxlvl = 0;
  public static int _joinTime = 0;
  public static int _eventTime = 0;
  public static int _minPlayers = 0;
  public static int _maxPlayers = 0;
  public static Vector<Integer> _teamPointsCount = new Vector();
  public static Vector<Integer> _flagIds = new Vector();
  public static Vector<Integer> _flagsX = new Vector();
  public static Vector<Integer> _flagsY = new Vector();
  public static Vector<Integer> _flagsZ = new Vector();
  public static Vector<L2Spawn> _flagSpawns = new Vector();
  public static Vector<L2Spawn> _throneSpawns = new Vector();
  public static Vector<Boolean> _flagsTaken = new Vector();
  public static int _topScore = 0;
  public static int eventCenterX = 0;
  public static int eventCenterY = 0;
  public static int eventCenterZ = 0;
  public static int eventOffset = 0;

  public static void showFlagHtml(L2PcInstance eventPlayer, String objectId, String teamName)
  {
    if (eventPlayer == null) {
      return;
    }
    try
    {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

      TextBuilder replyMSG = new TextBuilder("<html><head><body><center>");
      replyMSG.append("CTF Flag<br><br>");
      replyMSG.append("<font color=\"00FF00\">" + teamName + "'s Flag</font><br1>");
      if ((_teamNameCTF != null) && (_teamNameCTF.equals(teamName)))
        replyMSG.append("<font color=\"LEVEL\">This is your Flag</font><br1>");
      else
        replyMSG.append("<font color=\"LEVEL\">Enemy Flag!</font><br1>");
      if (_started) {
        processInFlagRange(eventPlayer);
      }
      else
        replyMSG.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");
      replyMSG.append("</center></body></html>");
      adminReply.setHtml(replyMSG.toString());
      eventPlayer.sendPacket(adminReply);
    }
    catch (Exception e)
    {
      System.out.println("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + e.getStackTrace());
    }
  }

  public static void CheckRestoreFlags() {
    Vector teamsTakenFlag = new Vector();
    try {
      for (L2PcInstance player : _players)
      {
        if (player != null) {
          if ((player.isOnline() == 0) && (player._haveFlagCTF)) {
            Announcements(_eventName + "(CTF): " + player.getName() + " logged off with a CTF flag!");
            player._haveFlagCTF = false;
            if ((_teams.indexOf(player._teamNameHaveFlagCTF) >= 0) && 
              (((Boolean)_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF))).booleanValue())) {
              _flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), Boolean.valueOf(false));
              spawnFlag(player._teamNameHaveFlagCTF);
              Announcements(_eventName + "(CTF): " + player._teamNameHaveFlagCTF + " flag now returned to place.");
            }
            removeFlagFromPlayer(player);
            player._teamNameHaveFlagCTF = null;
            return;
          }
          if (player._haveFlagCTF)
            teamsTakenFlag.add(Integer.valueOf(_teams.indexOf(player._teamNameHaveFlagCTF)));
        }
      }
      for (String team : _teams)
      {
        if (team != null) {
          int index = _teams.indexOf(team);
          if ((!teamsTakenFlag.contains(Integer.valueOf(index))) && 
            (((Boolean)_flagsTaken.get(index)).booleanValue())) {
            _flagsTaken.set(index, Boolean.valueOf(false));
            spawnFlag(team);
            Announcements(_eventName + "(CTF): " + team + " flag returned due to player error.");
          }
        }
      }
      for (L2PcInstance player : _players)
        if ((player != null) && (player._haveFlagCTF) && 
          (isOutsideCTFArea(player))) {
          Announcements(_eventName + "(CTF): " + player.getName() + " escaped from the event holding a flag!");
          player._haveFlagCTF = false;
          if ((_teams.indexOf(player._teamNameHaveFlagCTF) >= 0) && 
            (((Boolean)_flagsTaken.get(_teams.indexOf(player._teamNameHaveFlagCTF))).booleanValue())) {
            _flagsTaken.set(_teams.indexOf(player._teamNameHaveFlagCTF), Boolean.valueOf(false));
            spawnFlag(player._teamNameHaveFlagCTF);
            Announcements(_eventName + "(CTF): " + player._teamNameHaveFlagCTF + " flag now returned to place.");
          }
          removeFlagFromPlayer(player);
          player._teamNameHaveFlagCTF = null;
          player.teleToLocation(((Integer)_teamsX.get(_teams.indexOf(player._teamNameCTF))).intValue(), ((Integer)_teamsY.get(_teams.indexOf(player._teamNameCTF))).intValue(), ((Integer)_teamsZ.get(_teams.indexOf(player._teamNameCTF))).intValue());
          player.sendMessage("You have been returned to your team spawn");
          return;
        }
    }
    catch (Exception e) {
      _log.info("CTF.restoreFlags() Error:" + e.toString());
      return;
    }
  }

  public static void Announcements(String announce) {
    CreatureSay cs = new CreatureSay(0, 18, "", "Announcements: " + announce);
    if ((!_started) && (!_teleport)) {
      for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
        if ((player != null) && 
          (player.isOnline() != 0))
          player.sendPacket(cs);
      }
    }
    else if ((_players != null) && (!_players.isEmpty()))
      for (L2PcInstance player : _players)
        if ((player != null) && 
          (player.isOnline() != 0))
          player.sendPacket(cs);
  }

  public static void Started(L2PcInstance player)
  {
    _teamNameHaveFlagCTF = null;
    _haveFlagCTF = false;
  }

  public static void StartEvent() {
    for (L2PcInstance player : _players)
      if (player != null) {
        player._teamNameHaveFlagCTF = null;
        player._haveFlagCTF = false;
      }
    Announcements(_eventName + "(CTF): Started. Go Capture the Flags!");
  }

  public static void addFlagToPlayer(L2PcInstance _player) {
    L2ItemInstance wpn = _player.getInventory().getPaperdollItem(7);
    if (wpn == null) {
      wpn = _player.getInventory().getPaperdollItem(14);
      if (wpn != null)
        _player.getInventory().unEquipItemInBodySlotAndRecord(14);
    }
    else {
      _player.getInventory().unEquipItemInBodySlotAndRecord(7);
      wpn = _player.getInventory().getPaperdollItem(8);
      if (wpn != null)
        _player.getInventory().unEquipItemInBodySlotAndRecord(8);
    }
    _player.getInventory().equipItem(ItemTable.getInstance().createItem("", _FLAG_IN_HAND_ITEM_ID, 1, _player, null));
    _player.broadcastPacket(new SocialAction(_player.getObjectId(), 16));
    _haveFlagCTF = true;
    _player.broadcastUserInfo();
    CreatureSay cs = new CreatureSay(_player.getObjectId(), 15, ":", "You got it! Run back! ::");
    _player.sendPacket(cs);
  }

  public static void removeFlagFromPlayer(L2PcInstance player)
  {
    L2ItemInstance wpn = player.getInventory().getPaperdollItem(14);
    _haveFlagCTF = false;
    if (wpn != null)
    {
      L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
      player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
      InventoryUpdate iu = new InventoryUpdate();
      for (L2ItemInstance element : unequiped)
        iu.addModifiedItem(element);
      player.sendPacket(iu);
      player.sendPacket(new ItemList(player, true));
      player.abortAttack();
      player.broadcastUserInfo();
    }
    else {
      player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
      player.sendPacket(new ItemList(player, true));
      player.abortAttack();
      player.broadcastUserInfo();
    }
  }

  public static void setTeamFlag(String teamName, L2PcInstance activeChar)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1)
      return;
    addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, activeChar.getX(), activeChar.getY(), activeChar.getZ());
  }

  public static void spawnAllFlags()
  {
    while (_flagSpawns.size() < _teams.size())
      _flagSpawns.add(null);
    while (_throneSpawns.size() < _teams.size())
      _throneSpawns.add(null);
    for (String team : _teams)
    {
      int index = _teams.indexOf(team);
      L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(((Integer)_flagIds.get(index)).intValue());
      L2NpcTemplate throne = NpcTable.getInstance().getTemplate(32027);
      try
      {
        _throneSpawns.set(index, new L2Spawn(throne));
        ((L2Spawn)_throneSpawns.get(index)).setLocx(((Integer)_flagsX.get(index)).intValue());
        ((L2Spawn)_throneSpawns.get(index)).setLocy(((Integer)_flagsY.get(index)).intValue());
        ((L2Spawn)_throneSpawns.get(index)).setLocz(((Integer)_flagsZ.get(index)).intValue() - 10);
        ((L2Spawn)_throneSpawns.get(index)).setAmount(1);
        ((L2Spawn)_throneSpawns.get(index)).setHeading(0);
        ((L2Spawn)_throneSpawns.get(index)).setRespawnDelay(1);
        SpawnTable.getInstance().addNewSpawn((L2Spawn)_throneSpawns.get(index), false);
        ((L2Spawn)_throneSpawns.get(index)).init();
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().getStatus().setCurrentHp(999999999.0D);
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().decayMe();
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().spawnMe(((L2Spawn)_throneSpawns.get(index)).getLastSpawn().getX(), ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().getY(), ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().getZ());
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().setTitle(team + " Throne");
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().broadcastPacket(new MagicSkillUser(((L2Spawn)_throneSpawns.get(index)).getLastSpawn(), ((L2Spawn)_throneSpawns.get(index)).getLastSpawn(), 1036, 1, 5500, 1));
        ((L2Spawn)_throneSpawns.get(index)).getLastSpawn()._isCTF_throneSpawn = true;

        _flagSpawns.set(index, new L2Spawn(tmpl));
        ((L2Spawn)_flagSpawns.get(index)).setLocx(((Integer)_flagsX.get(index)).intValue());
        ((L2Spawn)_flagSpawns.get(index)).setLocy(((Integer)_flagsY.get(index)).intValue());
        ((L2Spawn)_flagSpawns.get(index)).setLocz(((Integer)_flagsZ.get(index)).intValue());
        ((L2Spawn)_flagSpawns.get(index)).setAmount(1);
        ((L2Spawn)_flagSpawns.get(index)).setHeading(0);
        ((L2Spawn)_flagSpawns.get(index)).setRespawnDelay(1);
        SpawnTable.getInstance().addNewSpawn((L2Spawn)_flagSpawns.get(index), false);
        ((L2Spawn)_flagSpawns.get(index)).init();
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getStatus().setCurrentHp(999999999.0D);
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().setTitle(team + "'s Flag");
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn()._CTF_FlagTeamName = team;
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().decayMe();
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().spawnMe(((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getX(), ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getY(), ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getZ());
        ((L2Spawn)_flagSpawns.get(index)).getLastSpawn()._isCTF_Flag = true;
        calculateOutSideOfCTF();
      }
      catch (Exception e)
      {
        System.out.println("CTF Engine[spawnAllFlags()]: exception: " + e.getMessage());
      }
    }
  }

  public static void processTopTeam()
  {
    for (L2PcInstance disconnectPlayer : _players)
    {
      if (disconnectPlayer.isOnline() == 0)
      {
        _players.remove(disconnectPlayer);
        _log.info("[CTF DEBUG] processTopTeam() remove disconnect player " + disconnectPlayer.getName());
      }
    }
    _topTeam = null;
    for (String team : _teams) {
      if ((teamPointsCount(team) == _topScore) && (_topScore > 0))
        _topTeam = null;
      if (teamPointsCount(team) > _topScore) {
        _topTeam = team;
        _topScore = teamPointsCount(team);
      }
    }
    if (_topScore <= 0) {
      Announcements(_eventName + "(" + "CTF): No flags taken).");
    }
    else if (_topTeam == null) {
      Announcements(_eventName + "(CTF): Maximum flags taken : " + _topScore + " flags! No one won.");
    } else {
      Announcements(_eventName + "(CTF): Team " + _topTeam + " wins the match, with " + _topScore + " flags taken!");
      rewardTeam(_topTeam);
    }

    teleportFinish();
  }

  public static void unspawnAllFlags()
  {
    try
    {
      if ((_throneSpawns == null) || (_flagSpawns == null) || (_teams == null))
        return;
      for (String team : _teams)
      {
        int index = _teams.indexOf(team);
        if (_throneSpawns.get(index) != null) {
          ((L2Spawn)_throneSpawns.get(index)).getLastSpawn().deleteMe();
          ((L2Spawn)_throneSpawns.get(index)).stopRespawn();
          SpawnTable.getInstance().deleteSpawn((L2Spawn)_throneSpawns.get(index), true);
        }
        if (_flagSpawns.get(index) != null) {
          ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().deleteMe();
          ((L2Spawn)_flagSpawns.get(index)).stopRespawn();
          SpawnTable.getInstance().deleteSpawn((L2Spawn)_flagSpawns.get(index), true);
        }
      }
      _throneSpawns.removeAllElements();
    } catch (Throwable t) {
      return;
    }
  }

  private static void unspawnFlag(String teamName)
  {
    int index = _teams.indexOf(teamName);

    ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().deleteMe();
    ((L2Spawn)_flagSpawns.get(index)).stopRespawn();
    SpawnTable.getInstance().deleteSpawn((L2Spawn)_flagSpawns.get(index), true);
  }

  public static void spawnFlag(String teamName)
  {
    int index = _teams.indexOf(teamName);
    L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(((Integer)_flagIds.get(index)).intValue());
    try
    {
      _flagSpawns.set(index, new L2Spawn(tmpl));

      ((L2Spawn)_flagSpawns.get(index)).setLocx(((Integer)_flagsX.get(index)).intValue());
      ((L2Spawn)_flagSpawns.get(index)).setLocy(((Integer)_flagsY.get(index)).intValue());
      ((L2Spawn)_flagSpawns.get(index)).setLocz(((Integer)_flagsZ.get(index)).intValue());
      ((L2Spawn)_flagSpawns.get(index)).setAmount(1);
      ((L2Spawn)_flagSpawns.get(index)).setHeading(0);
      ((L2Spawn)_flagSpawns.get(index)).setRespawnDelay(1);

      SpawnTable.getInstance().addNewSpawn((L2Spawn)_flagSpawns.get(index), false);

      ((L2Spawn)_flagSpawns.get(index)).init();
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getStatus().setCurrentHp(999999999.0D);
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().setTitle(teamName + "'s Flag");
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn()._CTF_FlagTeamName = teamName;
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn()._isCTF_Flag = true;
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().decayMe();
      ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().spawnMe(((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getX(), ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getY(), ((L2Spawn)_flagSpawns.get(index)).getLastSpawn().getZ());
    }
    catch (Exception e)
    {
      System.out.println("CTF Engine[spawnFlag(" + teamName + ")]: exception: " + e.getStackTrace());
    }
  }

  public static boolean InRangeOfFlag(L2PcInstance _player, int flagIndex, int offset)
  {
    return (_player.getX() > ((Integer)_flagsX.get(flagIndex)).intValue() - offset) && (_player.getX() < ((Integer)_flagsX.get(flagIndex)).intValue() + offset) && (_player.getY() > ((Integer)_flagsY.get(flagIndex)).intValue() - offset) && (_player.getY() < ((Integer)_flagsY.get(flagIndex)).intValue() + offset) && (_player.getZ() > ((Integer)_flagsZ.get(flagIndex)).intValue() - offset) && (_player.getZ() < ((Integer)_flagsZ.get(flagIndex)).intValue() + offset);
  }

  public static void processInFlagRange(L2PcInstance _player)
  {
    try
    {
      CheckRestoreFlags();
      for (String team : _teams)
      {
        if (team.equals(_teamNameCTF))
        {
          int indexOwn = _teams.indexOf(_teamNameCTF);

          if ((InRangeOfFlag(_player, indexOwn, 100)) && (!((Boolean)_flagsTaken.get(indexOwn)).booleanValue()) && (_haveFlagCTF)) {
            int indexEnemy = _teams.indexOf(_teamNameHaveFlagCTF);
            _flagsTaken.set(indexEnemy, Boolean.valueOf(false));
            spawnFlag(_teamNameHaveFlagCTF);
            _player.broadcastPacket(new SocialAction(_player.getObjectId(), 16));
            _player.broadcastUserInfo();
            _player.broadcastPacket(new SocialAction(_player.getObjectId(), 3));
            _player.broadcastUserInfo();
            removeFlagFromPlayer(_player);
            _teamPointsCount.set(indexOwn, Integer.valueOf(teamPointsCount(team) + 1));
            Announcements(_eventName + "(CTF): " + _player.getName() + " scores for " + _teamNameCTF + ".");
          }
        }
        else
        {
          int indexEnemy = _teams.indexOf(team);
          if ((InRangeOfFlag(_player, indexEnemy, 100)) && (!((Boolean)_flagsTaken.get(indexEnemy)).booleanValue()) && (!_haveFlagCTF) && (!_player.isDead()))
          {
            _flagsTaken.set(indexEnemy, Boolean.valueOf(true));
            unspawnFlag(team);
            _teamNameHaveFlagCTF = team;
            addFlagToPlayer(_player);
            _player.broadcastUserInfo();
            _haveFlagCTF = true;
            Announcements(_eventName + "(CTF): " + team + " flag taken by " + _player.getName() + "...");
            pointTeamTo(_player, team);
            break;
          }
        }
      }
    } catch (Exception e) {
      return;
    }
  }

  public static void pointTeamTo(L2PcInstance hasFlag, String ourFlag) {
    try {
      for (L2PcInstance player : _players)
        if ((player != null) && (player.isOnline() != 0) && 
          (player._teamNameCTF.equals(ourFlag))) {
          player.sendMessage(hasFlag.getName() + " took your flag!");
          if (player._haveFlagCTF) {
            player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
            player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
          }
          else {
            player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
            L2Radar rdr = new L2Radar(player);
            L2Radar tmp158_156 = rdr; tmp158_156.getClass(); L2Radar.RadarOnPlayer radar = new L2Radar.RadarOnPlayer(tmp158_156, hasFlag, player);
            ThreadPoolManager.getInstance().scheduleGeneral(radar, 10000 + Rnd.get(30000));
          }
        }
    }
    catch (Throwable t)
    {
    }
  }

  public static int teamPointsCount(String teamName) {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return -1;
    }
    return ((Integer)_teamPointsCount.get(index)).intValue();
  }

  public static void setTeamPointsCount(String teamName, int teamPointCount)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamPointsCount.set(index, Integer.valueOf(teamPointCount));
  }

  public static int teamPlayersCount(String teamName)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return -1;
    }
    return ((Integer)_teamPlayersCount.get(index)).intValue();
  }

  public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamPlayersCount.set(index, Integer.valueOf(teamPlayersCount));
  }

  public static void setNpcPos(L2PcInstance activeChar)
  {
    _npcX = activeChar.getX();
    _npcY = activeChar.getY();
    _npcZ = activeChar.getZ();
    _npcHeading = activeChar.getHeading();
  }

  public static void setNpcPos(int x, int y, int z)
  {
    _npcX = x;
    _npcY = y;
    _npcZ = z;
  }

  public static void addTeam(String teamName)
  {
    if (!checkTeamOk())
    {
      return;
    }

    if (teamName.equals(" ")) {
      return;
    }
    _teams.add(teamName);
    _teamPlayersCount.add(Integer.valueOf(0));
    _teamColors.add(Integer.valueOf(0));
    _teamsX.add(Integer.valueOf(0));
    _teamsY.add(Integer.valueOf(0));
    _teamsZ.add(Integer.valueOf(0));
    _teamPointsCount.add(Integer.valueOf(0));
    addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, 0, 0, 0);
  }

  private static void addOrSet(int listSize, L2Spawn flagSpawn, boolean flagsTaken, int flagId, int flagX, int flagY, int flagZ) {
    while (_flagsX.size() <= listSize) {
      _flagSpawns.add(null);
      _flagsTaken.add(Boolean.valueOf(false));
      _flagIds.add(Integer.valueOf(_FlagNPC));
      _flagsX.add(Integer.valueOf(0));
      _flagsY.add(Integer.valueOf(0));
      _flagsZ.add(Integer.valueOf(0));
    }
    _flagSpawns.set(listSize, flagSpawn);
    _flagsTaken.set(listSize, Boolean.valueOf(flagsTaken));
    _flagIds.set(listSize, Integer.valueOf(flagId));
    _flagsX.set(listSize, Integer.valueOf(flagX));
    _flagsY.set(listSize, Integer.valueOf(flagY));
    _flagsZ.set(listSize, Integer.valueOf(flagZ));
  }

  public static boolean checkMaxLevel(int maxlvl)
  {
    return _minlvl < maxlvl;
  }

  public static boolean checkMinLevel(int minlvl)
  {
    return _maxlvl > minlvl;
  }

  public static boolean checkMinPlayers(int players)
  {
    return _minPlayers <= players;
  }

  public static boolean checkMaxPlayers(int players)
  {
    return _maxPlayers > players;
  }

  public static void removeTeam(String teamName)
  {
    if ((!checkTeamOk()) || (_teams.isEmpty()))
    {
      return;
    }

    if (teamPlayersCount(teamName) > 0)
    {
      return;
    }

    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamsZ.remove(index);
    _teamsY.remove(index);
    _teamsX.remove(index);
    _teamColors.remove(index);
    _teamPointsCount.remove(index);
    _teamPlayersCount.remove(index);
    _teams.remove(index);
    _flagSpawns.remove(index);
    _flagsTaken.remove(index);
    _flagIds.remove(index);
    _flagsX.remove(index);
    _flagsY.remove(index);
    _flagsZ.remove(index);
  }

  public static void setTeamPos(String teamName, L2PcInstance activeChar)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamsX.set(index, Integer.valueOf(activeChar.getX()));
    _teamsY.set(index, Integer.valueOf(activeChar.getY()));
    _teamsZ.set(index, Integer.valueOf(activeChar.getZ()));
  }

  public static void setTeamPos(String teamName, int x, int y, int z)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamsX.set(index, Integer.valueOf(x));
    _teamsY.set(index, Integer.valueOf(y));
    _teamsZ.set(index, Integer.valueOf(z));
  }

  public static void setTeamColor(String teamName, int color)
  {
    if (!checkTeamOk()) {
      return;
    }
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamColors.set(index, Integer.valueOf(color));
  }

  public static boolean checkTeamOk()
  {
    return (!_started) && (!_teleport) && (!_joining);
  }

  public static void startJoin(L2PcInstance activeChar)
  {
    if (!startJoinOk())
    {
      activeChar.sendMessage("Event not setted propertly.");
      return;
    }

    _joining = true;
    spawnEventNpc(activeChar);
    Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
  }

  public static void startJoin()
  {
    if (!startJoinOk())
    {
      _log.warning("Event not setted propertly.");
      return;
    }

    _joining = true;
    spawnEventNpc();
    Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
  }

  public static boolean startAutoJoin()
  {
    if (!startJoinOk())
    {
      return false;
    }

    _joining = true;
    spawnEventNpc();
    Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
    return true;
  }

  public static boolean startJoinOk()
  {
    if ((_started) || (_teleport) || (_joining) || (_teams.size() < 2) || (_eventName.equals("")) || (_joiningLocationName.equals("")) || (_eventDesc.equals("")) || (_npcId == 0) || (_npcX == 0) || (_npcY == 0) || (_npcZ == 0) || (_rewardId == 0) || (_rewardAmount == 0) || (_teamsX.contains(Integer.valueOf(0))) || (_teamsY.contains(Integer.valueOf(0))) || (_teamsZ.contains(Integer.valueOf(0))))
    {
      return false;
    }try {
      if ((_flagsX.contains(Integer.valueOf(0))) || (_flagsY.contains(Integer.valueOf(0))) || (_flagsZ.contains(Integer.valueOf(0))) || (_flagIds.contains(Integer.valueOf(0))))
        return false;
      if ((_flagsX.size() < _teams.size()) || (_flagsY.size() < _teams.size()) || (_flagsZ.size() < _teams.size()) || (_flagIds.size() < _teams.size()))
      {
        return false;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      return false;
    }
    return true;
  }

  private static void spawnEventNpc(L2PcInstance activeChar)
  {
    L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
    try
    {
      _npcSpawn = new L2Spawn(tmpl);

      _npcSpawn.setLocx(_npcX);
      _npcSpawn.setLocy(_npcY);
      _npcSpawn.setLocz(_npcZ);
      _npcSpawn.setAmount(1);
      _npcSpawn.setHeading(_npcHeading);
      _npcSpawn.setRespawnDelay(1);

      SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

      _npcSpawn.init();
      _npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999.0D);
      _npcSpawn.getLastSpawn().setTitle(_eventName);
      _npcSpawn.getLastSpawn()._isEventMobCTF = true;
      _npcSpawn.getLastSpawn().isAggressive();
      _npcSpawn.getLastSpawn().decayMe();
      _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

      _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
    }
    catch (Exception e)
    {
      _log.severe("CTF Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
    }
  }

  private static void spawnEventNpc()
  {
    L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);
    try
    {
      _npcSpawn = new L2Spawn(tmpl);

      _npcSpawn.setLocx(_npcX);
      _npcSpawn.setLocy(_npcY);
      _npcSpawn.setLocz(_npcZ);
      _npcSpawn.setAmount(1);
      _npcSpawn.setHeading(_npcHeading);
      _npcSpawn.setRespawnDelay(1);

      SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

      _npcSpawn.init();
      _npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999.0D);
      _npcSpawn.getLastSpawn().setTitle(_eventName);
      _npcSpawn.getLastSpawn()._isEventMobCTF = true;
      _npcSpawn.getLastSpawn().isAggressive();
      _npcSpawn.getLastSpawn().decayMe();
      _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

      _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
    }
    catch (Exception e)
    {
      _log.severe("CTF Engine[spawnEventNpc(exception: " + e.getMessage());
    }
  }

  public static void teleportStart()
  {
    if ((!_joining) || (_started) || (_teleport)) {
      return;
    }
    if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (checkMinPlayers(_playersShuffle.size())))
    {
      removeOfflinePlayers();
      shuffleTeams();
    }
    else if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (!checkMinPlayers(_playersShuffle.size())))
    {
      Announcements("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
      return;
    }

    _joining = false;
    Announcements(_eventName + "(CTF): Teleport to team spot in 20 seconds!");

    setUserData();
    ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run()
      {
        CTF.sit();
        CTF.spawnAllFlags();
        for (L2PcInstance player : CTF._players)
        {
          if (player != null)
          {
            if (Config.CTF_ON_START_UNSUMMON_PET)
            {
              if (player.getPet() != null)
              {
                L2Summon summon = player.getPet();
                for (L2Effect e : summon.getAllEffects()) {
                  if (e == null) continue; e.exit();
                }
                if ((summon instanceof L2PetInstance)) {
                  summon.unSummon(player);
                }
              }
            }
            if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
            {
              player.stopAllEffects();
            }

            if (player.getParty() != null)
            {
              L2Party party = player.getParty();
              party.removePartyMember(player);
            }
            player.teleToLocation(((Integer)CTF._teamsX.get(CTF._teams.indexOf(player._teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNXMIN, Config.CTF_RND_SPAWNXMAX), ((Integer)CTF._teamsY.get(CTF._teams.indexOf(player._teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNYMIN, Config.CTF_RND_SPAWNYMAX), ((Integer)CTF._teamsZ.get(CTF._teams.indexOf(player._teamNameCTF))).intValue() + Config.CTF_SPAWN_Z);
          }
        }
      }
    }
    , 20000L);

    _teleport = true;
  }

  public static boolean teleportAutoStart()
  {
    if ((!_joining) || (_started) || (_teleport)) {
      return false;
    }
    if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (checkMinPlayers(_playersShuffle.size())))
    {
      removeOfflinePlayers();
      shuffleTeams();
    }
    else if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (!checkMinPlayers(_playersShuffle.size())))
    {
      Announcements("Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
      return false;
    }

    _joining = false;
    Announcements(_eventName + "(CTF): Teleport to team spot in 20 seconds!");

    setUserData();
    ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run()
      {
        CTF.sit();
        CTF.spawnAllFlags();

        for (L2PcInstance player : CTF._players)
        {
          if (player != null)
          {
            if (Config.CTF_ON_START_UNSUMMON_PET)
            {
              if (player.getPet() != null)
              {
                L2Summon summon = player.getPet();
                for (L2Effect e : summon.getAllEffects()) {
                  if (e == null) continue; e.exit();
                }
                if ((summon instanceof L2PetInstance)) {
                  summon.unSummon(player);
                }
              }
            }
            if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
            {
              player.stopAllEffects();
            }

            if (player.getParty() != null)
            {
              L2Party party = player.getParty();
              party.removePartyMember(player);
            }
            player.teleToLocation(((Integer)CTF._teamsX.get(CTF._teams.indexOf(player._teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNXMIN, Config.CTF_RND_SPAWNXMAX), ((Integer)CTF._teamsY.get(CTF._teams.indexOf(player._teamNameCTF))).intValue() + Rnd.get(Config.CTF_RND_SPAWNYMIN, Config.CTF_RND_SPAWNYMAX), ((Integer)CTF._teamsZ.get(CTF._teams.indexOf(player._teamNameCTF))).intValue());
          }
        }
      }
    }
    , 20000L);

    _teleport = true;
    return true;
  }

  public static void startEvent(L2PcInstance activeChar)
  {
    if (!startEventOk())
    {
      return;
    }

    _teleport = false;
    sit();
    _started = true;
    StartEvent();
  }

  public static void setJoinTime(int time)
  {
    _joinTime = time;
  }

  public static void setEventTime(int time)
  {
    _eventTime = time;
  }

  public static boolean startAutoEvent()
  {
    if (!startEventOk())
    {
      return false;
    }

    _teleport = false;
    sit();
    Announcements(_eventName + "(CTF): Started. Go Capture the Flags!");
    _started = true;
    return true;
  }

  public static void autoEvent()
  {
    if (startAutoJoin())
    {
      if (_joinTime > 0) { waiter(_joinTime * 60 * 1000);
      } else if (_joinTime <= 0)
      {
        abortEvent();
        return;
      }
      if (teleportAutoStart())
      {
        waiter(60000L);
        if (startAutoEvent())
        {
          waiter(_eventTime * 60 * 1000);
          finishEvent();
        }
      }
      else if (!teleportAutoStart())
      {
        abortEvent();
      }
    }
  }

  private static void waiter(long interval)
  {
    long startWaiterTime = System.currentTimeMillis();
    int seconds = (int)(interval / 1000L);

    while (startWaiterTime + interval > System.currentTimeMillis())
    {
      seconds--;

      if ((_joining) || (_started) || (_teleport))
      {
        switch (seconds)
        {
        case 3600:
          if (_joining)
          {
            Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
            Announcements("CTF Event: " + seconds / 60 / 60 + " hour(s) till registration close!");
          } else {
            if (!_started) break;
            Announcements("CTF Event: " + seconds / 60 / 60 + " hour(s) till event finish!"); } break;
        case 60:
        case 120:
        case 180:
        case 240:
        case 300:
        case 600:
        case 900:
        case 1800:
          if (_joining)
          {
            removeOfflinePlayers();
            Announcements(_eventName + "(CTF): Joinable in " + _joiningLocationName + "!");
            Announcements("CTF Event: " + seconds / 60 + " minute(s) till registration close!");
          } else {
            if (!_started) break;
            Announcements("CTF Event: " + seconds / 60 + " minute(s) till event finish!"); } break;
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 10:
        case 15:
        case 30:
          if (_joining) {
            Announcements("CTF Event: " + seconds + " second(s) till registration close!");
          } else if (_teleport) {
            Announcements("CTF Event: " + seconds + " seconds(s) till start fight!"); } else {
            if (!_started) break;
            Announcements("CTF Event: " + seconds + " second(s) till event finish!");
          }
        }

      }

      long startOneSecondWaiterStartTime = System.currentTimeMillis();

      while (startOneSecondWaiterStartTime + 1000L > System.currentTimeMillis())
      {
        try
        {
          Thread.sleep(1L);
        }
        catch (InterruptedException ie)
        {
        }
      }
    }
  }

  private static boolean startEventOk() {
    if ((_joining) || (!_teleport) || (_started)) {
      return false;
    }
    if ((Config.CTF_EVEN_TEAMS.equals("NO")) || (Config.CTF_EVEN_TEAMS.equals("BALANCE")))
    {
      if (_teamPlayersCount.contains(Integer.valueOf(0)))
        return false;
    }
    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
    {
      Vector playersShuffleTemp = new Vector();
      int loopCount = 0;

      loopCount = _playersShuffle.size();

      for (int i = 0; i < loopCount; i++)
      {
        if (_playersShuffle != null) {
          playersShuffleTemp.add(_playersShuffle.get(i));
        }
      }
      _playersShuffle = playersShuffleTemp;
      playersShuffleTemp.clear();
    }

    return true;
  }

  public static void shuffleTeams()
  {
    int teamCount = 0;
    int playersCount = 0;

    while (!_playersShuffle.isEmpty())
    {
      int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
      L2PcInstance player = null;
      player = (L2PcInstance)_playersShuffle.get(playerToAddIndex);
      player._originalKarmaCTF = player.getKarma();

      _players.add(player);
      ((L2PcInstance)_players.get(playersCount))._teamNameCTF = ((String)_teams.get(teamCount));
      _savePlayers.add(((L2PcInstance)_players.get(playersCount)).getName());
      _savePlayerTeams.add(_teams.get(teamCount));
      playersCount++;

      if (teamCount == _teams.size() - 1)
        teamCount = 0;
      else {
        teamCount++;
      }
      _playersShuffle.remove(playerToAddIndex);
    }
  }

  public static void setUserData()
  {
    for (L2PcInstance player : _players)
    {
      player.setTeam(_teams.indexOf(player._teamNameCTF) + 1);
      player.setKarma(0);
      player.broadcastUserInfo();
    }
  }

  public static void finishEvent()
  {
    if (!finishEventOk())
    {
      return;
    }

    _started = false;
    unspawnEventNpc();
    unspawnAllFlags();
    processTopTeam();

    if (_topScore != 0) {
      playKneelAnimation(_topTeam);
    }
    Announcements(_eventName + " Team Statistics:");
    for (String team : _teams)
    {
      int _flags_ = teamFlagCount(team);
      Announcements("Team: " + team + " - Flags taken: " + _flags_);
    }

    teleportFinish();
  }

  public static void playKneelAnimation(String teamName) {
    for (L2PcInstance player : _players)
    {
      if (player != null)
      {
        if (!player._teamNameCTF.equals(teamName))
        {
          player.broadcastPacket(new SocialAction(player.getObjectId(), 7));
        }
        else if (player._teamNameCTF.equals(teamName))
        {
          player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
        }
      }
    }
  }

  private static boolean finishEventOk()
  {
    return _started;
  }

  public static void rewardTeam(String teamName)
  {
    for (L2PcInstance player : _players)
    {
      if (player != null)
      {
        if (player._teamNameCTF.equals(teamName))
        {
          player.addItem("CTF Event: " + _eventName, _rewardId, _rewardAmount, null, true);

          NpcHtmlMessage nhm = new NpcHtmlMessage(5);
          TextBuilder replyMSG = new TextBuilder("");

          replyMSG.append("<html><body>Your team wins the event. Look in your inventory for the reward.</body></html>");

          nhm.setHtml(replyMSG.toString());
          player.sendPacket(nhm);
          player.sendPacket(new ActionFailed());
        }
      }
    }
  }

  public static void abortEvent()
  {
    if ((!_joining) && (!_teleport) && (!_started))
      return;
    if ((_joining) && (!_teleport) && (!_started)) {
      unspawnEventNpc();
      cleanCTF();
      _joining = false;
      Announcements(_eventName + "(CTF): Match aborted!");
      return;
    }
    _joining = false;
    _teleport = false;
    _started = false;
    unspawnEventNpc();
    unspawnAllFlags();
    Announcements(_eventName + "(CTF): Match aborted!");
    teleportFinish();
  }

  public static void sit()
  {
    if (_sitForced)
      _sitForced = false;
    else {
      _sitForced = true;
    }
    for (L2PcInstance player : _players)
    {
      if (player != null)
      {
        if (_sitForced)
        {
          player.stopMove(null, false);
          player.abortAttack();
          player.abortCast();

          if (!player.isSitting()) {
            player.sitDown();
          }

        }
        else if (player.isSitting()) {
          player.standUp();
        }
      }
    }
  }

  public static void dumpData()
  {
    _log.info("");
    _log.info("");

    if ((!_joining) && (!_teleport) && (!_started))
    {
      _log.info("<<---------------------------------->>");
      _log.info(">> CTF Engine infos dump (INACTIVE) <<");
      _log.info("<<--^----^^-----^----^^------^^----->>");
    }
    else if ((_joining) && (!_teleport) && (!_started))
    {
      _log.info("<<--------------------------------->>");
      _log.info(">> CTF Engine infos dump (JOINING) <<");
      _log.info("<<--^----^^-----^----^^------^----->>");
    }
    else if ((!_joining) && (_teleport) && (!_started))
    {
      _log.info("<<---------------------------------->>");
      _log.info(">> CTF Engine infos dump (TELEPORT) <<");
      _log.info("<<--^----^^-----^----^^------^^----->>");
    }
    else if ((!_joining) && (!_teleport) && (_started))
    {
      _log.info("<<--------------------------------->>");
      _log.info(">> CTF Engine infos dump (STARTED) <<");
      _log.info("<<--^----^^-----^----^^------^----->>");
    }

    _log.info("Name: " + _eventName);
    _log.info("Desc: " + _eventDesc);
    _log.info("Join location: " + _joiningLocationName);
    _log.info("Min lvl: " + _minlvl);
    _log.info("Max lvl: " + _maxlvl);
    _log.info("");
    _log.info("##########################");
    _log.info("# _teams(Vector<String>) #");
    _log.info("##########################");

    for (String team : _teams) {
      _log.info(team + " Flags Taken :" + _teamPointsCount.get(_teams.indexOf(team)));
    }
    if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
    {
      _log.info("");
      _log.info("#########################################");
      _log.info("# _playersShuffle(Vector<L2PcInstance>) #");
      _log.info("#########################################");

      for (L2PcInstance player : _playersShuffle)
      {
        if (player != null) {
          _log.info("Name: " + player.getName());
        }
      }
    }
    _log.info("");
    _log.info("##################################");
    _log.info("# _players(Vector<L2PcInstance>) #");
    _log.info("##################################");

    for (L2PcInstance player : _players)
    {
      if (player != null) {
        _log.info("Name: " + player.getName() + "   Team: " + player._teamNameCTF + "  Flags :" + player._countCTFflags);
      }
    }
    _log.info("");
    _log.info("#####################################################################");
    _log.info("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
    _log.info("#####################################################################");

    for (String player : _savePlayers) {
      _log.info("Name: " + player + "    Team: " + (String)_savePlayerTeams.get(_savePlayers.indexOf(player)));
    }
    _log.info("");
    _log.info("");
    System.out.println("**********==CTF==************");
    System.out.println("CTF._teamPointsCount:" + _teamPointsCount.toString());
    System.out.println("CTF._flagIds:" + _flagIds.toString());
    System.out.println("CTF._flagSpawns:" + _flagSpawns.toString());
    System.out.println("CTF._throneSpawns:" + _throneSpawns.toString());
    System.out.println("CTF._flagsTaken:" + _flagsTaken.toString());
    System.out.println("CTF._flagsX:" + _flagsX.toString());
    System.out.println("CTF._flagsY:" + _flagsY.toString());
    System.out.println("CTF._flagsZ:" + _flagsZ.toString());
    System.out.println("************EOF**************\n");
    System.out.println("");
  }

  public static void loadData()
  {
    _eventName = new String();
    _eventDesc = new String();
    _topTeam = new String();
    _joiningLocationName = new String();
    _teams = new Vector();
    _savePlayers = new Vector();
    _savePlayerTeams = new Vector();
    _players = new FastList();
    _playersShuffle = new Vector();
    _teamPlayersCount = new Vector();
    _teamPointsCount = new Vector();
    _teamColors = new Vector();
    _teamsX = new Vector();
    _teamsY = new Vector();
    _teamsZ = new Vector();

    _throneSpawns = new Vector();
    _flagSpawns = new Vector();
    _flagsTaken = new Vector();
    _flagIds = new Vector();
    _flagsX = new Vector();
    _flagsY = new Vector();
    _flagsZ = new Vector();

    _joining = false;
    _teleport = false;
    _started = false;
    _sitForced = false;
    _npcId = 0;
    _npcX = 0;
    _npcY = 0;
    _npcZ = 0;
    _npcHeading = 0;
    _rewardId = 0;
    _rewardAmount = 0;
    _topScore = 0;
    _minlvl = 0;
    _maxlvl = 0;
    _joinTime = 0;
    _eventTime = 0;
    _minPlayers = 0;
    _maxPlayers = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("Select * from ctf");
      ResultSet rs = statement.executeQuery();

      int teams = 0;

      while (rs.next())
      {
        _eventName = rs.getString("eventName");
        _eventDesc = rs.getString("eventDesc");
        _joiningLocationName = rs.getString("joiningLocation");
        _minlvl = rs.getInt("minlvl");
        _maxlvl = rs.getInt("maxlvl");
        _npcId = rs.getInt("npcId");
        _npcX = rs.getInt("npcX");
        _npcY = rs.getInt("npcY");
        _npcZ = rs.getInt("npcZ");
        _npcHeading = rs.getInt("npcHeading");
        _rewardId = rs.getInt("rewardId");
        _rewardAmount = rs.getInt("rewardAmount");
        teams = rs.getInt("teamsCount");
        _joinTime = rs.getInt("joinTime");
        _eventTime = rs.getInt("eventTime");
        _minPlayers = rs.getInt("minPlayers");
        _maxPlayers = rs.getInt("maxPlayers");
      }
      statement.close();

      int index = -1;
      if (teams > 0)
        index = 0;
      while ((index < teams) && (index > -1))
      {
        statement = con.prepareStatement("Select * from ctf_teams where teamId = ?");
        statement.setInt(1, index);
        rs = statement.executeQuery();
        while (rs.next())
        {
          _teams.add(rs.getString("teamName"));
          _teamPlayersCount.add(Integer.valueOf(0));
          _teamPointsCount.add(Integer.valueOf(0));
          _teamColors.add(Integer.valueOf(0));
          _teamsX.add(Integer.valueOf(0));
          _teamsY.add(Integer.valueOf(0));
          _teamsZ.add(Integer.valueOf(0));
          _teamsX.set(index, Integer.valueOf(rs.getInt("teamX")));
          _teamsY.set(index, Integer.valueOf(rs.getInt("teamY")));
          _teamsZ.set(index, Integer.valueOf(rs.getInt("teamZ")));
          _teamColors.set(index, Integer.valueOf(rs.getInt("teamColor")));
          _flagsX.add(Integer.valueOf(0));
          _flagsY.add(Integer.valueOf(0));
          _flagsZ.add(Integer.valueOf(0));
          _flagsX.set(index, Integer.valueOf(rs.getInt("flagX")));
          _flagsY.set(index, Integer.valueOf(rs.getInt("flagY")));
          _flagsZ.set(index, Integer.valueOf(rs.getInt("flagZ")));
          _flagSpawns.add(null);
          _flagIds.add(Integer.valueOf(_FlagNPC));
          _flagsTaken.add(Boolean.valueOf(false));
        }

        index++;
        statement.close();
      }
    }
    catch (Exception e)
    {
      _log.severe("Exception: CTF.loadData(): " + e.getMessage()); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public static void saveData() {
    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("Delete from ctf");
      statement.execute();
      statement.close();

      statement = con.prepareStatement("INSERT INTO ctf (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      statement.setString(1, _eventName);
      statement.setString(2, _eventDesc);
      statement.setString(3, _joiningLocationName);
      statement.setInt(4, _minlvl);
      statement.setInt(5, _maxlvl);
      statement.setInt(6, _npcId);
      statement.setInt(7, _npcX);
      statement.setInt(8, _npcY);
      statement.setInt(9, _npcZ);
      statement.setInt(10, _npcHeading);
      statement.setInt(11, _rewardId);
      statement.setInt(12, _rewardAmount);
      statement.setInt(13, _teams.size());
      statement.setInt(14, _joinTime);
      statement.setInt(15, _eventTime);
      statement.setInt(16, _minPlayers);
      statement.setInt(17, _maxPlayers);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("Delete from ctf_teams");
      statement.execute();
      statement.close();

      for (String teamName : _teams)
      {
        int index = _teams.indexOf(teamName);

        if (index == -1) return;
        statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setInt(1, index);
        statement.setString(2, teamName);
        statement.setInt(3, ((Integer)_teamsX.get(index)).intValue());
        statement.setInt(4, ((Integer)_teamsY.get(index)).intValue());
        statement.setInt(5, ((Integer)_teamsZ.get(index)).intValue());
        statement.setInt(6, ((Integer)_teamColors.get(index)).intValue());
        statement.setInt(7, ((Integer)_flagsX.get(index)).intValue());
        statement.setInt(8, ((Integer)_flagsY.get(index)).intValue());
        statement.setInt(9, ((Integer)_flagsZ.get(index)).intValue());
        statement.execute();
        statement.close();
      }
    }
    catch (Exception e)
    {
      PreparedStatement statement;
      _log.severe("Exception: CTF.saveData(): " + e.getMessage()); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public static void showEventHtml(L2PcInstance eventPlayer, String objectId) {
    try {
      NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

      TextBuilder replyMSG = new TextBuilder("<html><body>");
      replyMSG.append("CTF event registration<br><br><br>");

      if ((!_started) && (!_joining)) {
        replyMSG.append("<center>CTF match is not in progress yet.Wait for a GM to start the event<br>.</center>");
      } else if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (!checkMaxPlayers(_playersShuffle.size())))
      {
        if (!_started)
        {
          replyMSG.append("Players : <font color=\"00FF00\">" + _playersShuffle.size() + ".</font><br>");
          replyMSG.append("Max players : <font color=\"00FF00\">" + _maxPlayers + "</font><br><br>");
          replyMSG.append("<font color=\"FFFF00\">The event has reached its maximum capacity.</font><br>");
        }
      }
      else if (eventPlayer.isCursedWeaponEquiped())
      {
        replyMSG.append("<font color=\"FFFF00\">You can't participate in this event with a cursed Weapon.</font><br>");
      }
      else if ((!_started) && (_joining) && (eventPlayer.getLevel() >= _minlvl) && (eventPlayer.getLevel() < _maxlvl))
      {
        if ((_players.contains(eventPlayer)) || (_playersShuffle.contains(eventPlayer)) || (checkShufflePlayers(eventPlayer)))
        {
          if ((Config.CTF_EVEN_TEAMS.equals("NO")) || (Config.CTF_EVEN_TEAMS.equals("BALANCE")))
            replyMSG.append("You are already participating in team <font color=\"LEVEL\">" + _teamNameCTF + "</font><br><br>");
          else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) {
            replyMSG.append("You are already participating!<br><br>");
          }
          replyMSG.append("<table border=\"0\"><tr>");
          replyMSG.append("<td width=\"200\">Wait till event start or</td><br>");
          replyMSG.append("<td width=\"60\"><center><button value=\"Leave\" action=\"bypass -h npc_" + objectId + "_ctf_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
          replyMSG.append("</tr></table>");
        }
        else
        {
          replyMSG.append("You want to participate in Event?<br><br>");
          replyMSG.append("<td width=\"200\">Min level : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
          replyMSG.append("<td width=\"200\">Max level : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");

          if ((Config.CTF_EVEN_TEAMS.equals("NO")) || (Config.CTF_EVEN_TEAMS.equals("BALANCE")))
          {
            replyMSG.append("<center><table border=\"0\">");

            for (String team : _teams)
            {
              replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " \u0432\u0441\u0442\u0443\u043F\u0438\u043B\u043E)</td></tr><br>");
              replyMSG.append("<center><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
            }

            replyMSG.append("</table></center>");
          }
          else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE"))
          {
            replyMSG.append("<center><table border=\"0\">");

            for (String team : _teams) {
              replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
            }
            replyMSG.append("</table></center><br>");

            replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_ctf_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
          }
        }
      }
      else if ((_started) && (!_joining)) {
        replyMSG.append("<center>CTF match is in progress.</center>");
      } else if ((eventPlayer.getLevel() < _minlvl) || (eventPlayer.getLevel() > _maxlvl))
      {
        replyMSG.append("You level : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
        replyMSG.append("Min level : <font color=\"00FF00\">" + _minlvl + "</font><br>");
        replyMSG.append("Max level : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
        replyMSG.append("<font color=\"FFFF00\">You can't participatein this event.</font><br>");
      }

      replyMSG.append("</body></html>");
      adminReply.setHtml(replyMSG.toString());
      eventPlayer.sendPacket(adminReply);
      eventPlayer.sendPacket(new ActionFailed());
    }
    catch (Exception e)
    {
      _log.severe("CTF Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
    }
  }

  public static void addPlayer(L2PcInstance player, String teamName)
  {
    if (!addPlayerOk(teamName, player)) {
      return;
    }
    if ((Config.CTF_EVEN_TEAMS.equals("NO")) || (Config.CTF_EVEN_TEAMS.equals("BALANCE")))
    {
      _teamNameCTF = teamName;
      _players.add(player);
      setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
    }
    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) {
      _playersShuffle.add(player);
    }
    _inEventCTF = true;
    _countCTFflags = 0;
  }

  public static void removeOfflinePlayers()
  {
    try
    {
      if ((_playersShuffle == null) || (_playersShuffle.isEmpty()))
        return;
      if ((_playersShuffle != null) && (!_playersShuffle.isEmpty()))
      {
        for (L2PcInstance player : _playersShuffle)
        {
          if ((player == null) || (player.isOnline() == 0))
            _playersShuffle.remove(player);
        }
      }
    }
    catch (Exception e) {
      return;
    }
  }

  public static boolean checkShufflePlayers(L2PcInstance eventPlayer) {
    try {
      for (L2PcInstance player : _playersShuffle)
      {
        if (player == null)
        {
          _playersShuffle.remove(player);
          continue;
        }
        if (player.getObjectId() == eventPlayer.getObjectId())
        {
          _inEventCTF = true;
          _countCTFflags = 0;
          return true;
        }
        if (player.getName().equals(eventPlayer.getName()))
        {
          _playersShuffle.remove(player);
          _playersShuffle.add(eventPlayer);
          _inEventCTF = true;
          _countCTFflags = 0;
          return true;
        }
      }
    }
    catch (Exception e) {
    }
    return false;
  }

  public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
  {
    if ((checkShufflePlayers(eventPlayer)) || (eventPlayer._inEventCTF))
    {
      eventPlayer.sendMessage("You already participated in the event!");
      return false;
    }

    for (L2PcInstance player : _players)
    {
      if (player.getObjectId() == eventPlayer.getObjectId())
      {
        eventPlayer.sendMessage("You already participated in the event!");
        return false;
      }
      if (player.getName() == eventPlayer.getName())
      {
        eventPlayer.sendMessage("You already participated in the event!");
        return false;
      }
    }
    if (_players.contains(eventPlayer))
    {
      eventPlayer.sendMessage("You already participated in the event!");
      return false;
    }

    if (Config.CTF_EVEN_TEAMS.equals("NO"))
      return true;
    if (Config.CTF_EVEN_TEAMS.equals("BALANCE"))
    {
      boolean allTeamsEqual = true;
      int countBefore = -1;

      for (Iterator i$ = _teamPlayersCount.iterator(); i$.hasNext(); ) { int playersCount = ((Integer)i$.next()).intValue();

        if (countBefore == -1) {
          countBefore = playersCount;
        }
        if (countBefore != playersCount)
        {
          allTeamsEqual = false;
          break;
        }

        countBefore = playersCount;
      }

      if (allTeamsEqual) {
        return true;
      }
      countBefore = 2147483647;

      for (Iterator i$ = _teamPlayersCount.iterator(); i$.hasNext(); ) { int teamPlayerCount = ((Integer)i$.next()).intValue();

        if (teamPlayerCount < countBefore) {
          countBefore = teamPlayerCount;
        }
      }
      Vector joinableTeams = new Vector();

      for (String team : _teams)
      {
        if (teamPlayersCount(team) == countBefore) {
          joinableTeams.add(team);
        }
      }
      if (joinableTeams.contains(teamName))
        return true;
    }
    else if (Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) {
      return true;
    }
    eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
    return false;
  }

  public static synchronized void addDisconnectedPlayer(L2PcInstance player)
  {
    if (((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && ((_teleport) || (_started))) || (Config.CTF_EVEN_TEAMS.equals("NO")) || ((Config.CTF_EVEN_TEAMS.equals("BALANCE")) && ((_teleport) || (_started))))
    {
      if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS)
      {
        player.stopAllEffects();
      }

      for (L2PcInstance disconnectPlayer : _players)
      {
        if (player.getName().equals(disconnectPlayer.getName()))
        {
          _players.remove(disconnectPlayer);
          _log.info("[CTF DEBUG] Remove old data for player " + disconnectPlayer.getName());
        }
      }

      _teamNameCTF = ((String)_savePlayerTeams.get(_savePlayers.indexOf(player.getName())));

      if (!_players.contains(player))
      {
        _players.add(player);
        _log.info("[CTF DEBUG] Add new data for player " + player.getName());
      }

      _originalKarmaCTF = player.getKarma();
      _inEventCTF = true;
      _countCTFflags = 0;

      player.setTeam(_teams.indexOf(_teamNameCTF) + 1);
      player.setKarma(0);
      player.broadcastUserInfo();
      player.teleToLocation(((Integer)_teamsX.get(_teams.indexOf(_teamNameCTF))).intValue(), ((Integer)_teamsY.get(_teams.indexOf(_teamNameCTF))).intValue(), ((Integer)_teamsZ.get(_teams.indexOf(_teamNameCTF))).intValue());
      Started(player);
      CheckRestoreFlags();
    }
  }

  public static void removePlayer(L2PcInstance player)
  {
    if (_inEventCTF)
    {
      if (!_joining)
      {
        player.setTeam(0);
        player.setKarma(_originalKarmaCTF);
        player.broadcastUserInfo();
      }
      _teamNameCTF = new String();
      _countCTFflags = 0;
      _inEventCTF = false;

      if (((Config.CTF_EVEN_TEAMS.equals("NO")) || (Config.CTF_EVEN_TEAMS.equals("BALANCE"))) && (_players.contains(player)))
      {
        setTeamPlayersCount(_teamNameCTF, teamPlayersCount(_teamNameCTF) - 1);
        _players.remove(player);
      }
      else if ((Config.CTF_EVEN_TEAMS.equals("SHUFFLE")) && (!_playersShuffle.isEmpty()) && (_playersShuffle.contains(player))) {
        _playersShuffle.remove(player);
      }
    }
  }

  public static void cleanCTF() {
    _log.info("CTF : Cleaning players.");
    for (L2PcInstance player : _players)
    {
      if (player != null)
      {
        if (player._haveFlagCTF)
          removeFlagFromPlayer(player);
        else
          player.getInventory().destroyItemByItemId("", _FLAG_IN_HAND_ITEM_ID, 1, player, null);
        player._haveFlagCTF = false;
        removePlayer(player);
        if (_savePlayers.contains(player.getName()))
          _savePlayers.remove(player.getName());
        player._inEventCTF = false;
      }
    }
    if ((_playersShuffle != null) && (!_playersShuffle.isEmpty()))
    {
      for (L2PcInstance player : _playersShuffle)
      {
        if (player != null)
          player._inEventCTF = false;
      }
    }
    _log.info("CTF : Cleaning teams and flags.");
    for (String team : _teams)
    {
      int index = _teams.indexOf(team);
      _teamPointsCount.set(index, Integer.valueOf(0));
      _flagSpawns.set(index, null);
      _flagsTaken.set(index, Boolean.valueOf(false));
      _teamPlayersCount.set(index, Integer.valueOf(0));
      _teamPointsCount.set(index, Integer.valueOf(0));
    }
    _topScore = 0;
    _topTeam = new String();
    _players = new FastList();
    _playersShuffle = new Vector();
    _savePlayers = new Vector();
    _savePlayerTeams = new Vector();
    _teamPointsCount = new Vector();
    _flagSpawns = new Vector();
    _flagsTaken = new Vector();
    _teamPlayersCount = new Vector();
    _log.info("Cleaning CTF done.");
    _log.info("Loading new data from MySql");
    loadData();
  }

  public static void unspawnEventNpc()
  {
    if (_npcSpawn == null) {
      return;
    }
    _npcSpawn.getLastSpawn().deleteMe();
    _npcSpawn.stopRespawn();
    SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
  }

  public static void teleportFinish()
  {
    Announcements(_eventName + "(CTF): Teleport back to participation NPC!");

    ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
    {
      public void run()
      {
        for (L2PcInstance player : CTF._players)
        {
          if (player != null)
            player.teleToLocation(CTF._npcX, CTF._npcY, CTF._npcZ);
        }
        CTF.cleanCTF();
      }
    }
    , 3000L);
  }

  public static int teamFlagCount(String teamName)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return -1;
    }
    return ((Integer)_teamPointsCount.get(index)).intValue();
  }

  public static void setTeamFlagCount(String teamName, int teamFlagCount)
  {
    int index = _teams.indexOf(teamName);

    if (index == -1) {
      return;
    }
    _teamPointsCount.set(index, Integer.valueOf(teamFlagCount));
  }

  private static void calculateOutSideOfCTF() {
    if ((_teams == null) || (_flagSpawns == null) || (_teamsX == null) || (_teamsY == null) || (_teamsZ == null))
      return;
    int division = _teams.size() * 2; int pos = 0;
    int[] locX = new int[division]; int[] locY = new int[division]; int[] locZ = new int[division];
    for (L2Spawn flag : _flagSpawns) {
      locX[pos] = flag.getLocx();
      locY[pos] = flag.getLocy();
      locZ[pos] = flag.getLocz();
      pos++;
      if (pos > division / 2)
        break;
    }
    for (int x = 0; x < _teams.size(); x++) {
      locX[pos] = ((Integer)_teamsX.get(x)).intValue();
      locY[pos] = ((Integer)_teamsY.get(x)).intValue();
      locZ[pos] = ((Integer)_teamsZ.get(x)).intValue();
      pos++;
      if (pos > division)
        break;
    }
    int centerX = 0; int centerY = 0; int centerZ = 0;
    for (int x = 0; x < pos; x++) {
      centerX += locX[x] / division;
      centerY += locY[x] / division;
      centerZ += locZ[x] / division;
    }
    int maxX = 0; int maxY = 0; int maxZ = 0;
    for (int x = 0; x < pos; x++) {
      if (maxX < 2 * Math.abs(centerX - locX[x])) maxX = 2 * Math.abs(centerX - locX[x]);
      if (maxY < 2 * Math.abs(centerY - locY[x])) maxY = 2 * Math.abs(centerY - locY[x]);
      if (maxZ >= 2 * Math.abs(centerZ - locZ[x])) continue; maxZ = 2 * Math.abs(centerZ - locZ[x]);
    }

    eventCenterX = centerX;
    eventCenterY = centerY;
    eventCenterZ = centerZ;
    eventOffset = maxX;
    if (eventOffset < maxY) eventOffset = maxY;
    if (eventOffset < maxZ) eventOffset = maxZ; 
  }

  public static boolean isOutsideCTFArea(L2PcInstance _player)
  {
    if ((_player == null) || (_player.isOnline() == 0)) return true;

    return (_player.getX() <= eventCenterX - eventOffset) || (_player.getX() >= eventCenterX + eventOffset) || (_player.getY() <= eventCenterY - eventOffset) || (_player.getY() >= eventCenterY + eventOffset) || (_player.getZ() <= eventCenterZ - eventOffset) || (_player.getZ() >= eventCenterZ + eventOffset);
  }
}