package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

public class CharKnownList extends ObjectKnownList
{
  private Map<Integer, L2PcInstance> _knownPlayers;
  private Map<Integer, Integer> _knownRelations;

  public CharKnownList(L2Character activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper) {
    if (!super.addKnownObject(object, dropper)) return false;
    if ((object instanceof L2PcInstance)) {
      getKnownPlayers().put(Integer.valueOf(object.getObjectId()), (L2PcInstance)object);
      getKnownRelations().put(Integer.valueOf(object.getObjectId()), Integer.valueOf(-1));
    }
    return true;
  }

  public final boolean knowsThePlayer(L2PcInstance player)
  {
    return (getActiveChar() == player) || (getKnownPlayers().containsKey(Integer.valueOf(player.getObjectId())));
  }

  public final void removeAllKnownObjects()
  {
    super.removeAllKnownObjects();
    getKnownPlayers().clear();
    getKnownRelations().clear();

    getActiveChar().setTarget(null);

    if (getActiveChar().hasAI()) getActiveChar().setAI(null);
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) return false;
    if ((object instanceof L2PcInstance)) {
      getKnownPlayers().remove(Integer.valueOf(object.getObjectId()));
      getKnownRelations().remove(Integer.valueOf(object.getObjectId()));
    }

    if (object == getActiveChar().getTarget()) getActiveChar().setTarget(null);

    return true;
  }

  public L2Character getActiveChar()
  {
    return (L2Character)super.getActiveObject();
  }
  public int getDistanceToForgetObject(L2Object object) {
    return 0;
  }
  public int getDistanceToWatchObject(L2Object object) {
    return 0;
  }

  public Collection<L2Character> getKnownCharacters() {
    FastList result = new FastList();

    for (L2Object obj : getKnownObjects().values())
    {
      if ((obj instanceof L2Character)) result.add((L2Character)obj);
    }

    return result;
  }

  public Collection<L2Character> getKnownCharactersInRadius(long radius)
  {
    FastList result = new FastList();

    for (L2Object obj : getKnownObjects().values())
    {
      if ((obj instanceof L2PcInstance))
      {
        if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))
          result.add((L2PcInstance)obj);
      }
      else if ((obj instanceof L2MonsterInstance))
      {
        if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))
          result.add((L2MonsterInstance)obj);
      }
      else if ((obj instanceof L2NpcInstance))
      {
        if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true)) {
          result.add((L2NpcInstance)obj);
        }
      }
    }
    return result;
  }

  public final Map<Integer, L2PcInstance> getKnownPlayers()
  {
    if (_knownPlayers == null) _knownPlayers = new FastMap().setShared(true);
    return _knownPlayers;
  }

  public final Map<Integer, Integer> getKnownRelations()
  {
    if (_knownRelations == null) _knownRelations = new FastMap().setShared(true);
    return _knownRelations;
  }

  public final Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
  {
    FastList result = new FastList();

    for (L2PcInstance player : getKnownPlayers().values()) {
      if (Util.checkIfInRange((int)radius, getActiveChar(), player, true))
        result.add(player);
    }
    return result;
  }
}