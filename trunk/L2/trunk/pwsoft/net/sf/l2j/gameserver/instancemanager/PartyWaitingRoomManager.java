package net.sf.l2j.gameserver.instancemanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExPartyRoomMembers;
import net.sf.l2j.gameserver.network.serverpackets.PartyMatchDetail;
import net.sf.l2j.util.log.AbstractLogger;

public class PartyWaitingRoomManager
{
  protected static final Logger _log = AbstractLogger.getLogger(PartyWaitingRoomManager.class.getName());
  private static PartyWaitingRoomManager _instance;
  private static Map<Integer, ConcurrentLinkedQueue<WaitingRoom>> _rooms = new ConcurrentHashMap();
  private static ConcurrentLinkedQueue<L2PcInstance> _finders = new ConcurrentLinkedQueue();

  public static final PartyWaitingRoomManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new PartyWaitingRoomManager();
    _instance.load();
  }

  private void load()
  {
    _rooms.put(Integer.valueOf(-1), new ConcurrentLinkedQueue());
    _rooms.put(Integer.valueOf(-2), new ConcurrentLinkedQueue());
    _rooms.put(Integer.valueOf(100), new ConcurrentLinkedQueue());
    for (int i = 1; i <= 15; i++)
    {
      _rooms.put(Integer.valueOf(i), new ConcurrentLinkedQueue());
    }
  }

  public void registerRoom(L2PcInstance player, String title, int maxPlayers, int minLvl, int maxLvl, int location)
  {
    WaitingRoom new_room = new WaitingRoom(player, title, maxPlayers, minLvl, maxLvl, location);
    ((ConcurrentLinkedQueue)_rooms.get(Integer.valueOf(location))).add(new_room);
    player.setPartyRoom(new_room);
    player.sendPacket(new PartyMatchDetail(new_room));
    player.sendPacket(new ExPartyRoomMembers(player, new_room));
    _finders.remove(player);
  }

  public void joinRoom(L2PcInstance player, int roomId)
  {
    WaitingRoom room = getRoom(roomId);
    if (room == null) {
      return;
    }
    if (room.players.size() >= room.maxPlayers) {
      return;
    }
    room.addPlayer(player);
    player.setPartyRoom(room);

    refreshRoom(room);

    _finders.remove(player);
    player.setLFP(false);
    player.broadcastUserInfo();
  }

  public void exitRoom(L2PcInstance player, WaitingRoom room)
  {
    if (room == null) {
      return;
    }
    if (room.owner == null) {
      return;
    }
    if (!room.players.contains(player)) {
      return;
    }
    room.delPlayer(player);
    player.setPartyRoom(null);
    if (room.players.isEmpty())
    {
      room = null;
      return;
    }
    if (room.owner.equals(player))
    {
      room.owner = ((L2PcInstance)room.players.peek());
      room.leaderName = room.owner.getName();
    }

    refreshRoom(room);
  }

  public void refreshRoom(WaitingRoom room)
  {
    if (room == null) {
      return;
    }
    for (L2PcInstance member : room.players)
    {
      member.sendPacket(new PartyMatchDetail(room));
      member.sendPacket(new ExPartyRoomMembers(member, room));
    }
  }

  public WaitingRoom getRoom(int roomId)
  {
    for (Map.Entry entry : _rooms.entrySet())
    {
      Integer territoryId = (Integer)entry.getKey();
      ConcurrentLinkedQueue rooms = (ConcurrentLinkedQueue)entry.getValue();
      if ((territoryId == null) || (rooms == null) || 
        (rooms.isEmpty())) {
        continue;
      }
      for (WaitingRoom room : rooms)
      {
        if (room == null) {
          continue;
        }
        if (room.id == roomId)
          return room;
      }
    }
    return null;
  }

  public ConcurrentLinkedQueue<WaitingRoom> getRooms(int levelType, int territoryId, ConcurrentLinkedQueue<WaitingRoom> rooms)
  {
    int minLvl;
    int maxLvl;
    int minLvl;
    int maxLvl;
    if (territoryId == -1)
    {
      minLvl = levelType - 5;
      maxLvl = levelType + 5;
      for (Map.Entry entry : _rooms.entrySet())
      {
        Integer terrId = (Integer)entry.getKey();
        ConcurrentLinkedQueue temp = (ConcurrentLinkedQueue)entry.getValue();
        if ((terrId == null) || (temp == null) || 
          (temp.isEmpty())) {
          continue;
        }
        for (WaitingRoom room : temp)
        {
          if (room == null) {
            continue;
          }
          if (levelType == -1)
            rooms.add(room);
          else if ((room.minLvl >= minLvl) && (room.maxLvl <= maxLvl))
            rooms.add(room);
        }
      }
    }
    else if (levelType == -1)
    {
      for (WaitingRoom room : (ConcurrentLinkedQueue)_rooms.get(Integer.valueOf(territoryId)))
      {
        if (room == null) {
          continue;
        }
        rooms.add(room);
      }
    }
    else
    {
      minLvl = levelType - 5;
      maxLvl = levelType + 5;
      for (WaitingRoom room : (ConcurrentLinkedQueue)_rooms.get(Integer.valueOf(territoryId)))
      {
        if (room == null) {
          continue;
        }
        if ((room.minLvl >= minLvl) && (room.maxLvl <= maxLvl))
          rooms.add(room);
      }
    }
    return rooms;
  }

  public void registerPlayer(L2PcInstance player)
  {
    _finders.add(player);
  }

  public void delPlayer(L2PcInstance player) {
    _finders.remove(player);
  }

  public ConcurrentLinkedQueue<L2PcInstance> getFinders(int page, int minLvl, int maxLvl, ConcurrentLinkedQueue<L2PcInstance> finders)
  {
    for (L2PcInstance player : _finders)
    {
      if (player == null) {
        continue;
      }
      if ((player.getLevel() >= minLvl) && (player.getLevel() <= maxLvl))
        finders.add(player);
    }
    return finders;
  }

  public static class WaitingRoom
  {
    public int id;
    public L2PcInstance owner;
    public String leaderName;
    public String title;
    public int maxPlayers;
    public int minLvl;
    public int maxLvl;
    public int location;
    public int loot = 0;
    public ConcurrentLinkedQueue<L2PcInstance> players;

    public WaitingRoom(L2PcInstance player, String title, int maxPlayers, int minLvl, int maxLvl, int location)
    {
      id = IdFactory.getInstance().getNextId();
      owner = player;
      leaderName = player.getName();
      this.title = title;
      this.maxPlayers = maxPlayers;
      this.minLvl = minLvl;
      this.maxLvl = maxLvl;
      this.location = location;
      players = new ConcurrentLinkedQueue();
      players.add(player);
    }

    public void addPlayer(L2PcInstance player)
    {
      players.add(player);
    }

    public void delPlayer(L2PcInstance player)
    {
      players.remove(player);
    }

    public boolean contains(L2PcInstance player)
    {
      return players.contains(player);
    }
  }
}