package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import l2p.gameserver.Config;
import l2p.gameserver.model.entity.olympiad.CompType;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.entity.olympiad.OlympiadGame;
import l2p.gameserver.model.entity.olympiad.OlympiadManager;
import l2p.gameserver.model.entity.olympiad.TeamMember;
import l2p.gameserver.templates.StatsSet;

public abstract class ExReceiveOlympiad extends L2GameServerPacket
{
  private int _type;

  public ExReceiveOlympiad(int type)
  {
    _type = type;
  }

  protected void writeImpl()
  {
    writeEx(212);
    writeD(_type);
  }

  public static class MatchResult extends ExReceiveOlympiad
  {
    private List<PlayerInfo>[] _players = new ArrayList[2];
    private boolean _tie;
    private String _name;

    public MatchResult(boolean tie, String name)
    {
      super();
      _tie = tie;
      _name = name;
    }

    public void addPlayer(int team, TeamMember member, int gameResultPoints)
    {
      int points = Config.OLYMPIAD_OLDSTYLE_STAT ? 0 : member.getStat().getInteger("olympiad_points", 0);

      PlayerInfo playerInfo = new PlayerInfo(member.getName(), member.getClanName(), member.getClassId(), points, gameResultPoints, (int)member.getDamage());
      if (_players[team] == null) {
        _players[team] = new ArrayList(2);
      }
      _players[team].add(playerInfo);
    }

    protected void writeImpl()
    {
      super.writeImpl();
      writeD(_tie);
      writeS(_name);
      for (int i = 0; i < _players.length; i++)
      {
        writeD(i + 1);
        List players = _players[i] == null ? Collections.emptyList() : _players[i];
        writeD(players.size());
        for (PlayerInfo playerInfo : players)
        {
          writeS(playerInfo._name);
          writeS(playerInfo._clanName);
          writeD(0);
          writeD(playerInfo._classId);
          writeD(playerInfo._damage);
          writeD(playerInfo._currentPoints);
          writeD(playerInfo._gamePoints); }  }  } 
    private static class PlayerInfo { private String _name;
      private String _clanName;
      private int _classId;
      private int _currentPoints;
      private int _gamePoints;
      private int _damage;

      public PlayerInfo(String name, String clanName, int classId, int currentPoints, int gamePoints, int damage) { _name = name;
        _clanName = clanName;
        _classId = classId;
        _currentPoints = currentPoints;
        _gamePoints = gamePoints;
        _damage = damage;
      }
    }
  }

  public static class MatchList extends ExReceiveOlympiad
  {
    private List<ArenaInfo> _arenaList = Collections.emptyList();

    public MatchList()
    {
      super();
      OlympiadManager manager = Olympiad._manager;
      if (manager != null)
      {
        _arenaList = new ArrayList();
        for (int i = 0; i < Olympiad.STADIUMS.length; i++)
        {
          OlympiadGame game = manager.getOlympiadInstance(i);
          if ((game != null) && (game.getState() > 0))
            _arenaList.add(new ArenaInfo(i, game.getState(), game.getType().ordinal(), game.getTeamName1(), game.getTeamName2()));
        }
      }
    }

    protected void writeImpl()
    {
      super.writeImpl();
      writeD(_arenaList.size());
      writeD(0);
      for (ArenaInfo arena : _arenaList)
      {
        writeD(arena._id);
        writeD(arena._matchType);
        writeD(arena._status);
        writeS(arena._name1);
        writeS(arena._name2); }  } 
    private static class ArenaInfo { private int _id;
      private int _status;
      private int _matchType;
      private String _name1;
      private String _name2;

      public ArenaInfo(int id, int status, int match_type, String name1, String name2) { _id = id;
        _status = status;
        _matchType = match_type;
        _name1 = name1;
        _name2 = name2;
      }
    }
  }
}