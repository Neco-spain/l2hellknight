package net.sf.l2j.gameserver.model;

import java.util.Collection;
import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;

public class MobGroupTable
{
  private static MobGroupTable _instance;
  private Map<Integer, MobGroup> _groupMap;
  public static final int FOLLOW_RANGE = 300;
  public static final int RANDOM_RANGE = 300;

  public MobGroupTable()
  {
    _groupMap = new FastMap();
  }

  public static MobGroupTable getInstance()
  {
    if (_instance == null) {
      _instance = new MobGroupTable();
    }
    return _instance;
  }

  public void addGroup(int groupKey, MobGroup group)
  {
    _groupMap.put(Integer.valueOf(groupKey), group);
  }

  public MobGroup getGroup(int groupKey)
  {
    return (MobGroup)_groupMap.get(Integer.valueOf(groupKey));
  }

  public int getGroupCount()
  {
    return _groupMap.size();
  }

  public MobGroup getGroupForMob(L2ControllableMobInstance mobInst)
  {
    for (MobGroup mobGroup : _groupMap.values()) {
      if (mobGroup.isGroupMember(mobInst))
        return mobGroup;
    }
    return null;
  }

  public MobGroup[] getGroups()
  {
    return (MobGroup[])_groupMap.values().toArray(new MobGroup[getGroupCount()]);
  }

  public boolean removeGroup(int groupKey)
  {
    return _groupMap.remove(Integer.valueOf(groupKey)) != null;
  }
}