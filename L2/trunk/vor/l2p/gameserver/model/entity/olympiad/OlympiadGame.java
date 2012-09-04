package l2p.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.lang.ArrayUtils;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.instancemanager.OlympiadHistoryManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.ExOlympiadUserInfo;
import l2p.gameserver.serverpackets.ExReceiveOlympiad.MatchResult;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.InstantZone;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.utils.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlympiadGame
{
  private static final Logger _log = LoggerFactory.getLogger(OlympiadGame.class);
  public static final int MAX_POINTS_LOOSE = 10;
  public boolean validated = false;

  private int _winner = 0;
  private int _state = 0;
  private int _id;
  private Reflection _reflection;
  private CompType _type;
  private OlympiadTeam _team1;
  private OlympiadTeam _team2;
  private List<Player> _spectators = new CopyOnWriteArrayList();
  private long _startTime;
  OlympiadGameTask _task;
  ScheduledFuture<?> _shedule;

  public OlympiadGame(int id, CompType type, List<Integer> opponents)
  {
    _type = type;
    _id = id;
    _reflection = new Reflection();
    InstantZone instantZone = InstantZoneHolder.getInstance().getInstantZone(Rnd.get(147, 150));
    _reflection.init(instantZone);

    _team1 = new OlympiadTeam(this, 1);
    _team2 = new OlympiadTeam(this, 2);

    for (int i = 0; i < opponents.size() / 2; i++) {
      _team1.addMember(((Integer)opponents.get(i)).intValue());
    }
    for (int i = opponents.size() / 2; i < opponents.size(); i++) {
      _team2.addMember(((Integer)opponents.get(i)).intValue());
    }
    Log.add("Olympiad System: Game - " + id + ": " + _team1.getName() + " Vs " + _team2.getName(), "olympiad");
  }

  private String getBufferSpawnGroup(int instancedZoneId)
  {
    String bufferGroup = null;
    switch (instancedZoneId)
    {
    case 147:
      bufferGroup = "olympiad_147_buffers";
      break;
    case 148:
      bufferGroup = "olympiad_148_buffers";
      break;
    case 149:
      bufferGroup = "olympiad_149_buffers";
      break;
    case 150:
      bufferGroup = "olympiad_150_buffers";
    }

    return bufferGroup;
  }

  public void addBuffers()
  {
    if (!_type.hasBuffer()) {
      return;
    }
    if (getBufferSpawnGroup(_reflection.getInstancedZoneId()) != null)
      _reflection.spawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
  }

  public void deleteBuffers()
  {
    _reflection.despawnByGroup(getBufferSpawnGroup(_reflection.getInstancedZoneId()));
  }

  public void managerShout()
  {
    for (NpcInstance npc : Olympiad.getNpcs())
    {
      NpcString npcString;
      switch (1.$SwitchMap$l2p$gameserver$model$entity$olympiad$CompType[_type.ordinal()])
      {
      case 1:
        npcString = NpcString.OLYMPIAD_CLASSFREE_TEAM_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
        break;
      case 2:
        npcString = NpcString.OLYMPIAD_CLASS_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
        break;
      case 3:
        npcString = NpcString.OLYMPIAD_CLASSFREE_INDIVIDUAL_MATCH_IS_GOING_TO_BEGIN_IN_ARENA_S1_IN_A_MOMENT;
        break;
      default:
        break;
      }
      Functions.npcShout(npc, npcString, new String[] { String.valueOf(_id + 1) });
    }
  }

  public void portPlayersToArena()
  {
    _team1.portPlayersToArena();
    _team2.portPlayersToArena();
  }

  public void preparePlayers()
  {
    _team1.preparePlayers();
    _team2.preparePlayers();
  }

  public void portPlayersBack()
  {
    _team1.portPlayersBack();
    _team2.portPlayersBack();
  }

  public void collapse()
  {
    portPlayersBack();
    clearSpectators();
    _reflection.collapse();
  }

  public void validateWinner(boolean aborted) throws Exception
  {
    int state = _state;
    _state = 0;

    if (validated)
    {
      Log.add("Olympiad Result: " + _team1.getName() + " vs " + _team2.getName() + " ... double validate check!!!", "olympiad");
      return;
    }
    validated = true;

    if ((state < 1) && (aborted))
    {
      _team1.takePointsForCrash();
      _team2.takePointsForCrash();
      broadcastPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME, true, false);
      return;
    }

    boolean teamOneCheck = _team1.checkPlayers();
    boolean teamTwoCheck = _team2.checkPlayers();

    if (_winner <= 0) {
      if ((!teamOneCheck) && (!teamTwoCheck))
        _winner = 0;
      else if (!teamTwoCheck)
        _winner = 1;
      else if (!teamOneCheck)
        _winner = 2;
      else if (_team1.getDamage() < _team2.getDamage())
        _winner = 1;
      else if (_team1.getDamage() > _team2.getDamage())
        _winner = 2;
    }
    if (_winner == 1)
      winGame(_team1, _team2);
    else if (_winner == 2)
      winGame(_team2, _team1);
    else {
      tie();
    }
    _team1.saveNobleData();
    _team2.saveNobleData();

    broadcastRelation();
    broadcastPacket(new SystemMessage2(SystemMsg.YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECONDS).addInteger(20.0D), true, true);
  }

  public void winGame(OlympiadTeam winnerTeam, OlympiadTeam looseTeam)
  {
    ExReceiveOlympiad.MatchResult packet = new ExReceiveOlympiad.MatchResult(false, winnerTeam.getName());

    int pointDiff = 0;

    TeamMember[] looserMembers = (TeamMember[])looseTeam.getMembers().toArray(new TeamMember[looseTeam.getMembers().size()]);
    TeamMember[] winnerMembers = (TeamMember[])winnerTeam.getMembers().toArray(new TeamMember[winnerTeam.getMembers().size()]);

    for (int i = 0; i < 9; i++)
    {
      TeamMember looserMember = (TeamMember)ArrayUtils.valid(looserMembers, i);
      TeamMember winnerMember = (TeamMember)ArrayUtils.valid(winnerMembers, i);
      if ((looserMember == null) || (winnerMember == null))
        continue;
      winnerMember.incGameCount();
      looserMember.incGameCount();

      int gamePoints = transferPoints(looserMember.getStat(), winnerMember.getStat());

      packet.addPlayer(winnerTeam == _team1 ? 0 : 1, winnerMember, gamePoints);
      packet.addPlayer(looseTeam == _team1 ? 0 : 1, looserMember, -gamePoints);

      pointDiff += gamePoints;
    }

    if (_type != CompType.TEAM)
    {
      int team = _team1 == winnerTeam ? 1 : 2;

      TeamMember member1 = (TeamMember)ArrayUtils.valid(_team1 == winnerTeam ? winnerMembers : looserMembers, 0);
      TeamMember member2 = (TeamMember)ArrayUtils.valid(_team2 == winnerTeam ? winnerMembers : looserMembers, 0);
      if ((member1 != null) && (member2 != null))
      {
        int diff = (int)((System.currentTimeMillis() - _startTime) / 1000L);
        OlympiadHistory h = new OlympiadHistory(member1.getObjectId(), member1.getObjectId(), member1.getClassId(), member2.getClassId(), member1.getName(), member2.getName(), _startTime, diff, team, _type.ordinal());

        OlympiadHistoryManager.getInstance().saveHistory(h);
      }
    }

    for (Player player : winnerTeam.getPlayers())
    {
      ItemInstance item = player.getInventory().addItem(Config.ALT_OLY_BATTLE_REWARD_ITEM, getType().getReward());
      player.sendPacket(SystemMessage2.obtainItems(item.getItemId(), getType().getReward(), 0));
      player.sendChanges();
    }

    List teamsPlayers = new ArrayList();
    teamsPlayers.addAll(winnerTeam.getPlayers());
    teamsPlayers.addAll(looseTeam.getPlayers());
    for (Player player : teamsPlayers) {
      if (player != null)
      {
        for (QuestState qs : player.getAllQuestsStates())
          if (qs.isStarted())
            qs.getQuest().onOlympiadEnd(this, qs);
      }
    }
    broadcastPacket(packet, true, false);

    broadcastPacket(new SystemMessage2(SystemMsg.CONGRATULATIONS_C1_YOU_WIN_THE_MATCH).addString(winnerTeam.getName()), false, true);

    Log.add("Olympiad Result: " + winnerTeam.getName() + " vs " + looseTeam.getName() + " ... (" + (int)winnerTeam.getDamage() + " vs " + (int)looseTeam.getDamage() + ") " + winnerTeam.getName() + " win " + pointDiff + " points", "olympiad");
  }

  public void tie()
  {
    TeamMember[] teamMembers1 = (TeamMember[])_team1.getMembers().toArray(new TeamMember[_team1.getMembers().size()]);
    TeamMember[] teamMembers2 = (TeamMember[])_team2.getMembers().toArray(new TeamMember[_team2.getMembers().size()]);

    ExReceiveOlympiad.MatchResult packet = new ExReceiveOlympiad.MatchResult(true, "");
    for (int i = 0; i < teamMembers1.length; i++) {
      try
      {
        TeamMember member1 = (TeamMember)ArrayUtils.valid(teamMembers1, i);
        TeamMember member2 = (TeamMember)ArrayUtils.valid(teamMembers2, i);

        if (member1 != null)
        {
          member1.incGameCount();
          StatsSet stat1 = member1.getStat();
          packet.addPlayer(0, member1, -2);

          stat1.set("olympiad_points", stat1.getInteger("olympiad_points") - 2);
        }

        if (member2 != null)
        {
          member2.incGameCount();
          StatsSet stat2 = member2.getStat();
          packet.addPlayer(1, member2, -2);

          stat2.set("olympiad_points", stat2.getInteger("olympiad_points") - 2);
        }
      }
      catch (Exception e)
      {
        _log.error("OlympiadGame.tie(): " + e, e);
      }
    }
    if (_type != CompType.TEAM)
    {
      TeamMember member1 = (TeamMember)ArrayUtils.valid(teamMembers1, 0);
      TeamMember member2 = (TeamMember)ArrayUtils.valid(teamMembers2, 0);
      if ((member1 != null) && (member2 != null))
      {
        int diff = (int)((System.currentTimeMillis() - _startTime) / 1000L);
        OlympiadHistory h = new OlympiadHistory(member1.getObjectId(), member1.getObjectId(), member1.getClassId(), member2.getClassId(), member1.getName(), member2.getName(), _startTime, diff, 0, _type.ordinal());

        OlympiadHistoryManager.getInstance().saveHistory(h);
      }
    }

    broadcastPacket(SystemMsg.THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE, false, true);
    broadcastPacket(packet, true, false);

    Log.add("Olympiad Result: " + _team1.getName() + " vs " + _team2.getName() + " ... tie", "olympiad");
  }

  private int transferPoints(StatsSet from, StatsSet to)
  {
    int fromPoints = from.getInteger("olympiad_points");
    int fromLoose = from.getInteger("competitions_loose");
    int fromPlayed = from.getInteger("competitions_done");

    int toPoints = to.getInteger("olympiad_points");
    int toWin = to.getInteger("competitions_win");
    int toPlayed = to.getInteger("competitions_done");

    int pointDiff = Math.max(1, Math.min(fromPoints, toPoints) / getType().getLooseMult());
    pointDiff = pointDiff > 10 ? 10 : pointDiff;

    from.set("olympiad_points", fromPoints - pointDiff);
    from.set("competitions_loose", fromLoose + 1);
    from.set("competitions_done", fromPlayed + 1);

    to.set("olympiad_points", toPoints + pointDiff);
    to.set("competitions_win", toWin + 1);
    to.set("competitions_done", toPlayed + 1);

    return pointDiff;
  }

  public void openDoors()
  {
    for (DoorInstance door : _reflection.getDoors())
      door.openMe();
  }

  public int getId()
  {
    return _id;
  }

  public Reflection getReflection()
  {
    return _reflection;
  }

  public boolean isRegistered(int objId)
  {
    return (_team1.contains(objId)) || (_team2.contains(objId));
  }

  public List<Player> getSpectators()
  {
    return _spectators;
  }

  public void addSpectator(Player spec)
  {
    _spectators.add(spec);
  }

  public void removeSpectator(Player spec)
  {
    _spectators.remove(spec);
  }

  public void clearSpectators()
  {
    for (Player pc : _spectators)
      if ((pc != null) && (pc.isInObserverMode()))
        pc.leaveOlympiadObserverMode(false);
    _spectators.clear();
  }

  public void broadcastInfo(Player sender, Player receiver, boolean onlyToSpectators)
  {
    if (sender != null) {
      if (receiver != null)
        receiver.sendPacket(new ExOlympiadUserInfo(sender, sender.getOlympiadSide()));
      else
        broadcastPacket(new ExOlympiadUserInfo(sender, sender.getOlympiadSide()), !onlyToSpectators, true);
    }
    else
    {
      for (Player player : _team1.getPlayers()) {
        if (receiver != null) {
          receiver.sendPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
        }
        else {
          broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()), !onlyToSpectators, true);
          player.broadcastRelationChanged();
        }
      }

      for (Player player : _team2.getPlayers())
        if (receiver != null) {
          receiver.sendPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
        }
        else {
          broadcastPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()), !onlyToSpectators, true);
          player.broadcastRelationChanged();
        }
    }
  }

  public void broadcastRelation()
  {
    for (Player player : _team1.getPlayers()) {
      player.broadcastRelationChanged();
    }
    for (Player player : _team2.getPlayers())
      player.broadcastRelationChanged();
  }

  public void broadcastPacket(L2GameServerPacket packet, boolean toTeams, boolean toSpectators)
  {
    if (toTeams)
    {
      _team1.broadcast(packet);
      _team2.broadcast(packet);
    }

    if ((toSpectators) && (!_spectators.isEmpty()))
      for (Player spec : _spectators)
        if (spec != null)
          spec.sendPacket(packet);
  }

  public void broadcastPacket(IStaticPacket packet, boolean toTeams, boolean toSpectators)
  {
    if (toTeams)
    {
      _team1.broadcast(packet);
      _team2.broadcast(packet);
    }

    if ((toSpectators) && (!_spectators.isEmpty()))
      for (Player spec : _spectators)
        if (spec != null)
          spec.sendPacket(packet);
  }

  public List<Player> getAllPlayers()
  {
    List result = new ArrayList();
    for (Player player : _team1.getPlayers())
      result.add(player);
    for (Player player : _team2.getPlayers())
      result.add(player);
    if (!_spectators.isEmpty())
      for (Player spec : _spectators)
        if (spec != null)
          result.add(spec);
    return result;
  }

  public void setWinner(int val)
  {
    _winner = val;
  }

  public OlympiadTeam getWinnerTeam()
  {
    if (_winner == 1)
      return _team1;
    if (_winner == 2)
      return _team2;
    return null;
  }

  public void setState(int val)
  {
    _state = val;
    if (_state == 1)
      _startTime = System.currentTimeMillis();
  }

  public int getState()
  {
    return _state;
  }

  public List<Player> getTeamMembers(Player player)
  {
    return player.getOlympiadSide() == 1 ? _team1.getPlayers() : _team2.getPlayers();
  }

  public void addDamage(Player player, double damage)
  {
    if (player.getOlympiadSide() == 1)
      _team1.addDamage(player, damage);
    else
      _team2.addDamage(player, damage);
  }

  public boolean doDie(Player player)
  {
    return player.getOlympiadSide() == 1 ? _team1.doDie(player) : _team2.doDie(player);
  }

  public boolean checkPlayersOnline()
  {
    return (_team1.checkPlayers()) && (_team2.checkPlayers());
  }

  public boolean logoutPlayer(Player player)
  {
    return (player != null) && (player.getOlympiadSide() == 1 ? _team1.logout(player) : _team2.logout(player));
  }

  public synchronized void sheduleTask(OlympiadGameTask task)
  {
    if (_shedule != null)
      _shedule.cancel(false);
    _task = task;
    _shedule = task.shedule();
  }

  public OlympiadGameTask getTask()
  {
    return _task;
  }

  public BattleStatus getStatus()
  {
    if (_task != null)
      return _task.getStatus();
    return BattleStatus.Begining;
  }

  public void endGame(long time, boolean aborted)
  {
    try
    {
      validateWinner(aborted);
    }
    catch (Exception e)
    {
      _log.error("", e);
    }

    sheduleTask(new OlympiadGameTask(this, BattleStatus.Ending, 0, time));
  }

  public CompType getType()
  {
    return _type;
  }

  public String getTeamName1()
  {
    return _team1.getName();
  }

  public String getTeamName2()
  {
    return _team2.getName();
  }
}