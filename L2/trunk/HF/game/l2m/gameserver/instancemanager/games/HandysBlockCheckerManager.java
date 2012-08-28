package l2m.gameserver.instancemanager.games;

import gnu.trove.TIntIntHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.BlockCheckerEngine;
import l2m.gameserver.model.entity.BlockCheckerEngine.StartEvent;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.impl.KrateisCubeRunnerEvent;
import l2m.gameserver.model.entity.olympiad.Olympiad;
import l2m.gameserver.network.serverpackets.ExCubeGameAddPlayer;
import l2m.gameserver.network.serverpackets.ExCubeGameChangeTeam;
import l2m.gameserver.network.serverpackets.ExCubeGameRemovePlayer;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SystemMessage;

public final class HandysBlockCheckerManager
{
  private static ArenaParticipantsHolder[] _arenaPlayers;
  private static TIntIntHashMap _arenaVotes = new TIntIntHashMap();
  private static Map<Integer, Boolean> _arenaStatus;
  private static List<Integer> _registrationPenalty = new ArrayList();

  public synchronized int getArenaVotes(int arenaId)
  {
    return _arenaVotes.get(arenaId);
  }

  public synchronized void increaseArenaVotes(int arena)
  {
    int newVotes = _arenaVotes.get(arena) + 1;
    ArenaParticipantsHolder holder = _arenaPlayers[arena];

    if ((newVotes > holder.getAllPlayers().size() / 2) && (!holder.getEvent().isStarted()))
    {
      clearArenaVotes(arena);
      if ((holder.getBlueTeamSize() == 0) || (holder.getRedTeamSize() == 0))
        return;
      if (Config.ALT_HBCE_FAIR_PLAY)
        holder.checkAndShuffle();
      BlockCheckerEngine tmp80_77 = holder.getEvent(); tmp80_77.getClass(); ThreadPoolManager.getInstance().execute(new BlockCheckerEngine.StartEvent(tmp80_77));
    }
    else {
      _arenaVotes.put(arena, newVotes);
    }
  }

  public synchronized void clearArenaVotes(int arena)
  {
    _arenaVotes.put(arena, 0);
  }

  private HandysBlockCheckerManager()
  {
    if (_arenaStatus == null)
    {
      _arenaStatus = new HashMap();
      _arenaStatus.put(Integer.valueOf(0), Boolean.valueOf(false));
      _arenaStatus.put(Integer.valueOf(1), Boolean.valueOf(false));
      _arenaStatus.put(Integer.valueOf(2), Boolean.valueOf(false));
      _arenaStatus.put(Integer.valueOf(3), Boolean.valueOf(false));
    }
  }

  public ArenaParticipantsHolder getHolder(int arena)
  {
    return _arenaPlayers[arena];
  }

  public void startUpParticipantsQueue()
  {
    _arenaPlayers = new ArenaParticipantsHolder[4];

    for (int i = 0; i < 4; i++)
      _arenaPlayers[i] = new ArenaParticipantsHolder(i);
  }

  public boolean addPlayerToArena(Player player, int arenaId)
  {
    ArenaParticipantsHolder holder = _arenaPlayers[arenaId];

    synchronized (holder)
    {
      if (isRegistered(player))
      {
        player.sendPacket(new SystemMessage(1502).addName(player));
        return false;
      }

      if (player.isCursedWeaponEquipped())
      {
        player.sendPacket(new SystemMessage(2708));
        return false;
      }

      KrateisCubeRunnerEvent krateis = (KrateisCubeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 2);
      if (krateis.isRegistered(player))
      {
        player.sendPacket(Msg.APPLICANTS_FOR_THE_OLYMPIAD_UNDERGROUND_COLISEUM_OR_KRATEI_S_CUBE_MATCHES_CANNOT_REGISTER);
        return false;
      }

      if (Olympiad.isRegistered(player))
      {
        player.sendPacket(Msg.APPLICANTS_FOR_THE_OLYMPIAD_UNDERGROUND_COLISEUM_OR_KRATEI_S_CUBE_MATCHES_CANNOT_REGISTER);
        return false;
      }

      if (_registrationPenalty.contains(Integer.valueOf(player.getObjectId())))
      {
        player.sendPacket(new SystemMessage(2707));
        return false;
      }
      boolean isRed;
      boolean isRed;
      if (holder.getBlueTeamSize() < holder.getRedTeamSize())
      {
        holder.addPlayer(player, 1);
        isRed = false;
      }
      else
      {
        holder.addPlayer(player, 0);
        isRed = true;
      }
      holder.broadCastPacketToTeam(new ExCubeGameAddPlayer(player, isRed));
      return true;
    }
  }

  public void removePlayer(Player player, int arenaId, int team)
  {
    ArenaParticipantsHolder holder = _arenaPlayers[arenaId];
    synchronized (holder)
    {
      boolean isRed = team == 0;

      holder.removePlayer(player, team);
      holder.broadCastPacketToTeam(new ExCubeGameRemovePlayer(player, isRed));

      int teamSize = isRed ? holder.getRedTeamSize() : holder.getBlueTeamSize();
      if (teamSize == 0) {
        holder.getEvent().endEventAbnormally();
      }
      Integer objId = Integer.valueOf(player.getObjectId());
      if (!_registrationPenalty.contains(objId))
        _registrationPenalty.add(objId);
      schedulePenaltyRemoval(objId.intValue());
    }
  }

  public void changePlayerToTeam(Player player, int arena, int team)
  {
    ArenaParticipantsHolder holder = _arenaPlayers[arena];

    synchronized (holder)
    {
      boolean isFromRed = holder._redPlayers.contains(player);

      if ((isFromRed) && (holder.getBlueTeamSize() == 6))
      {
        player.sendMessage("The team is full");
        return;
      }
      if ((!isFromRed) && (holder.getRedTeamSize() == 6))
      {
        player.sendMessage("The team is full");
        return;
      }

      int futureTeam = isFromRed ? 1 : 0;
      holder.addPlayer(player, futureTeam);

      if (isFromRed)
        holder.removePlayer(player, 0);
      else
        holder.removePlayer(player, 1);
      holder.broadCastPacketToTeam(new ExCubeGameChangeTeam(player, isFromRed));
    }
  }

  public synchronized void clearPaticipantQueueByArenaId(int arenaId)
  {
    _arenaPlayers[arenaId].clearPlayers();
  }

  public static boolean isRegistered(Player player)
  {
    for (int i = 0; i < 4; i++)
      if (_arenaPlayers[i].getAllPlayers().contains(player))
        return true;
    return false;
  }

  public boolean arenaIsBeingUsed(int arenaId)
  {
    if ((arenaId < 0) || (arenaId > 3))
      return false;
    return ((Boolean)_arenaStatus.get(Integer.valueOf(arenaId))).booleanValue();
  }

  public void setArenaBeingUsed(int arenaId)
  {
    _arenaStatus.put(Integer.valueOf(arenaId), Boolean.valueOf(true));
  }

  public void setArenaFree(int arenaId)
  {
    _arenaStatus.put(Integer.valueOf(arenaId), Boolean.valueOf(false));
  }

  public static HandysBlockCheckerManager getInstance()
  {
    return SingletonHolder._instance;
  }

  private void schedulePenaltyRemoval(int objId)
  {
    ThreadPoolManager.getInstance().schedule(new PenaltyRemove(Integer.valueOf(objId)), 10000L);
  }

  private class PenaltyRemove extends RunnableImpl
  {
    Integer objectId;

    public PenaltyRemove(Integer id) {
      objectId = id;
    }

    public void runImpl()
      throws Exception
    {
      HandysBlockCheckerManager._registrationPenalty.remove(objectId);
    }
  }

  public class ArenaParticipantsHolder
  {
    int _arena;
    List<Player> _redPlayers;
    List<Player> _bluePlayers;
    BlockCheckerEngine _engine;

    public ArenaParticipantsHolder(int arena)
    {
      _arena = arena;
      _redPlayers = new ArrayList(6);
      _bluePlayers = new ArrayList(6);
      _engine = new BlockCheckerEngine(this, _arena);
    }

    public List<Player> getRedPlayers()
    {
      return _redPlayers;
    }

    public List<Player> getBluePlayers()
    {
      return _bluePlayers;
    }

    public ArrayList<Player> getAllPlayers()
    {
      ArrayList all = new ArrayList(12);
      all.addAll(_redPlayers);
      all.addAll(_bluePlayers);
      return all;
    }

    public void addPlayer(Player player, int team)
    {
      if (team == 0)
        _redPlayers.add(player);
      else
        _bluePlayers.add(player);
    }

    public void removePlayer(Player player, int team)
    {
      if (team == 0)
        _redPlayers.remove(player);
      else
        _bluePlayers.remove(player);
    }

    public int getPlayerTeam(Player player)
    {
      if (_redPlayers.contains(player))
        return 0;
      if (_bluePlayers.contains(player)) {
        return 1;
      }
      return -1;
    }

    public int getRedTeamSize()
    {
      return _redPlayers.size();
    }

    public int getBlueTeamSize()
    {
      return _bluePlayers.size();
    }

    public void broadCastPacketToTeam(L2GameServerPacket packet)
    {
      ArrayList team = new ArrayList(12);
      team.addAll(_redPlayers);
      team.addAll(_bluePlayers);

      for (Player p : team)
        p.sendPacket(packet);
    }

    public void clearPlayers()
    {
      _redPlayers.clear();
      _bluePlayers.clear();
    }

    public BlockCheckerEngine getEvent()
    {
      return _engine;
    }

    public void updateEvent()
    {
      _engine.updatePlayersOnStart(this);
    }

    private void checkAndShuffle()
    {
      int redSize = _redPlayers.size();
      int blueSize = _bluePlayers.size();
      if (redSize > blueSize + 1)
      {
        broadCastPacketToTeam(new SystemMessage(2703));
        int needed = redSize - (blueSize + 1);
        for (int i = 0; i < needed + 1; i++)
        {
          Player plr = (Player)_redPlayers.get(i);
          if (plr == null)
            continue;
          changePlayerToTeam(plr, _arena, 1);
        }
      }
      else if (blueSize > redSize + 1)
      {
        broadCastPacketToTeam(new SystemMessage(2703));
        int needed = blueSize - (redSize + 1);
        for (int i = 0; i < needed + 1; i++)
        {
          Player plr = (Player)_bluePlayers.get(i);
          if (plr == null)
            continue;
          changePlayerToTeam(plr, _arena, 0);
        }
      }
    }
  }

  private static class SingletonHolder
  {
    private static HandysBlockCheckerManager _instance = new HandysBlockCheckerManager(null);
  }
}