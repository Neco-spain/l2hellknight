package net.sf.l2j.gameserver.model.entity;

import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeam
{
  private String _name;
  private int[] _coordinates = new int[3];
  private short _points;
  private Map<Integer, L2PcInstance> _participatedPlayers = new FastMap();

  public TvTEventTeam(String name, int[] coordinates)
  {
    _name = name;
    _coordinates = coordinates;
    _points = 0;
  }

  public boolean addPlayer(L2PcInstance playerInstance)
  {
    if (playerInstance == null)
    {
      return false;
    }

    synchronized (_participatedPlayers)
    {
      _participatedPlayers.put(Integer.valueOf(playerInstance.getObjectId()), playerInstance);
    }

    return true;
  }

  public void removePlayer(int playerObjectId)
  {
    synchronized (_participatedPlayers)
    {
      _participatedPlayers.remove(Integer.valueOf(playerObjectId));
    }
  }

  public void increasePoints()
  {
    _points = (short)(_points + 1);
  }

  public void cleanMe()
  {
    _participatedPlayers.clear();
    _participatedPlayers = new FastMap();
    _points = 0;
  }

  public boolean containsPlayer(int playerObjectId)
  {
    boolean containsPlayer;
    synchronized (_participatedPlayers)
    {
      containsPlayer = _participatedPlayers.containsKey(Integer.valueOf(playerObjectId));
    }

    return containsPlayer;
  }

  public String getName()
  {
    return _name;
  }

  public int[] getCoordinates()
  {
    return _coordinates;
  }

  public short getPoints()
  {
    return _points;
  }

  public Map<Integer, L2PcInstance> getParticipatedPlayers()
  {
    Map participatedPlayers = null;

    synchronized (_participatedPlayers)
    {
      participatedPlayers = _participatedPlayers;
    }

    return participatedPlayers;
  }

  public int getParticipatedPlayerCount()
  {
    int participatedPlayerCount;
    synchronized (_participatedPlayers)
    {
      participatedPlayerCount = _participatedPlayers.size();
    }

    return participatedPlayerCount;
  }
}