package l2p.gameserver.instancemanager.naia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NaiaTowerManager
{
  private static final Logger _log = LoggerFactory.getLogger(NaiaTowerManager.class);

  private static Map<Integer, List<Player>> _groupList = new HashMap();
  private static Map<Integer, List<Player>> _roomsDone = new HashMap();
  private static Map<Integer, Long> _groupTimer = new HashMap();
  private static Map<Integer, List<NpcInstance>> _roomMobs;
  private static List<NpcInstance> _roomMobList;
  private static long _towerAccessible = 0L;
  private static int _index = 0;
  public static HashMap<Integer, Boolean> lockedRooms;
  private static final NaiaTowerManager _instance = new NaiaTowerManager();

  public static final NaiaTowerManager getInstance()
  {
    return _instance;
  }

  private NaiaTowerManager()
  {
    if (lockedRooms == null)
    {
      lockedRooms = new HashMap();
      for (int i = 18494; i <= 18505; i++) {
        lockedRooms.put(Integer.valueOf(i), Boolean.valueOf(false));
      }
      _roomMobs = new HashMap();
      for (int i = 18494; i <= 18505; i++)
      {
        _roomMobList = new ArrayList();
        _roomMobs.put(Integer.valueOf(i), _roomMobList);
      }

      _log.info("Naia Tower Manager: Loaded 12 rooms");
    }
    ThreadPoolManager.getInstance().schedule(new GroupTowerTimer(null), 30000L);
  }

  public static void startNaiaTower(Player leader)
  {
    if (leader == null) {
      return;
    }
    if (_towerAccessible > System.currentTimeMillis()) {
      return;
    }
    for (Player member : leader.getParty().getPartyMembers()) {
      member.teleToLocation(new Location(-47271, 246098, -9120));
    }
    addGroupToTower(leader);
    _towerAccessible += 1200000L;

    ReflectionUtils.getDoor(18250001).openMe();
  }

  private static void addGroupToTower(Player leader)
  {
    _index = _groupList.keySet().size() + 1;
    _groupList.put(Integer.valueOf(_index), leader.getParty().getPartyMembers());
    _groupTimer.put(Integer.valueOf(_index), Long.valueOf(System.currentTimeMillis() + 300000L));

    leader.sendMessage("The Tower of Naia countdown has begun. You have only 5 minutes to pass each room.");
  }

  public static void updateGroupTimer(Player player)
  {
    for (Iterator i$ = _groupList.keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
      if (((List)_groupList.get(Integer.valueOf(i))).contains(player))
      {
        _groupTimer.put(Integer.valueOf(i), Long.valueOf(System.currentTimeMillis() + 300000L));
        player.sendMessage("Group timer has been updated");
        break;
      } }
  }

  public static void removeGroupTimer(Player player)
  {
    for (Iterator i$ = _groupList.keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
      if (((List)_groupList.get(Integer.valueOf(i))).contains(player))
      {
        _groupList.remove(Integer.valueOf(i));
        _groupTimer.remove(Integer.valueOf(i));
      } }
  }

  public static boolean isLegalGroup(Player player)
  {
    if ((_groupList == null) || (_groupList.isEmpty())) {
      return false;
    }
    for (Iterator i$ = _groupList.keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
      if (((List)_groupList.get(Integer.valueOf(i))).contains(player))
        return true;
    }
    return false;
  }

  public static void lockRoom(int npcId)
  {
    lockedRooms.put(Integer.valueOf(npcId), Boolean.valueOf(true));
  }

  public static void unlockRoom(int npcId)
  {
    lockedRooms.put(Integer.valueOf(npcId), Boolean.valueOf(false));
  }

  public static boolean isLockedRoom(int npcId)
  {
    return ((Boolean)lockedRooms.get(Integer.valueOf(npcId))).booleanValue();
  }

  public static void addRoomDone(int roomId, Player player)
  {
    if (player.getParty() != null)
      _roomsDone.put(Integer.valueOf(roomId), player.getParty().getPartyMembers());
  }

  public static boolean isRoomDone(int roomId, Player player)
  {
    if ((_roomsDone == null) || (_roomsDone.isEmpty())) {
      return false;
    }
    if ((_roomsDone.get(Integer.valueOf(roomId)) == null) || (((List)_roomsDone.get(Integer.valueOf(roomId))).isEmpty())) {
      return false;
    }

    return ((List)_roomsDone.get(Integer.valueOf(roomId))).contains(player);
  }

  public static void addMobsToRoom(int roomId, List<NpcInstance> mob)
  {
    _roomMobs.put(Integer.valueOf(roomId), mob);
  }

  public static List<NpcInstance> getRoomMobs(int roomId)
  {
    return (List)_roomMobs.get(Integer.valueOf(roomId));
  }

  public static void removeRoomMobs(int roomId)
  {
    ((List)_roomMobs.get(Integer.valueOf(roomId))).clear();
  }

  private class GroupTowerTimer extends RunnableImpl {
    private GroupTowerTimer() {
    }

    public void runImpl() throws Exception {
      ThreadPoolManager.getInstance().schedule(new GroupTowerTimer(NaiaTowerManager.this), 30000L);
      Iterator i$;
      if ((!NaiaTowerManager._groupList.isEmpty()) && (!NaiaTowerManager._groupTimer.isEmpty()))
        for (i$ = NaiaTowerManager._groupTimer.keySet().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();
          if (((Long)NaiaTowerManager._groupTimer.get(Integer.valueOf(i))).longValue() < System.currentTimeMillis())
          {
            for (Player kicked : (List)NaiaTowerManager._groupList.get(Integer.valueOf(i)))
            {
              kicked.teleToLocation(new Location(17656, 244328, 11595));
              kicked.sendMessage("The time has expired. You cannot stay in Tower of Naia any longer");
            }
            NaiaTowerManager._groupList.remove(Integer.valueOf(i));
            NaiaTowerManager._groupTimer.remove(Integer.valueOf(i));
          }
        }
    }
  }
}