package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

public class CharKnownList extends ObjectKnownList
{
  private L2Character activeChar;
  private Map<Integer, Integer> _knownRelations = new ConcurrentHashMap();
  private Map<Integer, L2PcInstance> _knownPlayers = new ConcurrentHashMap();

  public CharKnownList(L2Character activeChar)
  {
    super(activeChar);
    this.activeChar = activeChar;
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper)
  {
    if (!super.addKnownObject(object, dropper)) {
      return false;
    }

    if (object.isPlayer()) {
      _knownPlayers.put(Integer.valueOf(object.getObjectId()), object.getPlayer());
      _knownRelations.put(Integer.valueOf(object.getObjectId()), Integer.valueOf(-1));
    }
    return true;
  }

  public final boolean knowsThePlayer(L2PcInstance player)
  {
    return (activeChar.equals(player)) || (_knownPlayers.containsKey(Integer.valueOf(player.getObjectId())));
  }

  public final void removeAllKnownObjects()
  {
    super.removeAllKnownObjects();
    _knownPlayers.clear();
    _knownRelations.clear();

    activeChar.setTarget(null);

    if (activeChar.hasAI())
      activeChar.setAI(null);
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) {
      return false;
    }
    if (object.isPlayer()) {
      _knownPlayers.remove(Integer.valueOf(object.getObjectId()));
      _knownRelations.remove(Integer.valueOf(object.getObjectId()));
    }

    if (object.equals(activeChar.getTarget())) {
      activeChar.setTarget(null);
    }

    return true;
  }

  public L2Character getActiveChar()
  {
    return activeChar;
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    return 0;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    return 0;
  }

  public FastList<L2Character> getKnownCharacters() {
    return getCharactersList(new FastList());
  }

  public FastList<L2Character> getKnownCharactersInRadius(int radius) {
    return getCharactersListInRadius(new FastList(), radius);
  }

  private FastList<L2Character> getCharactersList(FastList<L2Character> result) {
    for (L2Object obj : getKnownObjects().values()) {
      if ((obj != null) && (obj.isL2Character())) {
        result.add(obj.getL2Character());
      }
    }
    return result;
  }

  private FastList<L2Character> getCharactersListInRadius(FastList<L2Character> result, int radius) {
    for (L2Object obj : getKnownObjects().values()) {
      if ((obj != null) && (obj.isL2Character()) && 
        (Util.checkIfInRange(radius, activeChar, obj, true))) {
        result.add(obj.getL2Character());
      }
    }

    return result;
  }

  public final Map<Integer, L2PcInstance> getKnownPlayers()
  {
    return _knownPlayers;
  }

  public final Map<Integer, Integer> getKnownRelations()
  {
    return _knownRelations;
  }

  public final FastList<L2PcInstance> getListKnownPlayers() {
    return getPlayersList(new FastList());
  }

  public final FastList<L2PcInstance> getKnownPlayersInRadius(int radius) {
    return getPlayersListInRadius(new FastList(), radius);
  }

  private FastList<L2PcInstance> getPlayersList(FastList<L2PcInstance> result) {
    for (L2PcInstance player : _knownPlayers.values()) {
      if (player.isInOfflineMode()) {
        continue;
      }
      result.add(player);
    }
    return result;
  }

  private FastList<L2PcInstance> getPlayersListInRadius(FastList<L2PcInstance> result, int radius) {
    for (L2PcInstance player : _knownPlayers.values()) {
      if (player.isInOfflineMode()) {
        continue;
      }
      if (Util.checkIfInRange(radius, activeChar, player, true)) {
        result.add(player);
      }
    }
    return result;
  }

  public boolean existsDoorsInRadius(int radius) {
    for (L2Object obj : getKnownObjects().values()) {
      if (obj.isL2Door())
      {
        if (Util.checkIfInRange(radius, activeChar, obj, true)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean updateRelationsFor(int objId, int idx)
  {
    Integer relation = (Integer)_knownRelations.get(Integer.valueOf(objId));
    if (relation == null) {
      return false;
    }

    return relation.intValue() != idx;
  }

  public void gc() {
    _knownPlayers.clear();
    _knownRelations.clear();
  }
}