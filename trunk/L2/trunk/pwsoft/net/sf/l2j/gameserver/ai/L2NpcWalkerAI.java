package net.sf.l2j.gameserver.ai;

import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.L2NpcWalkerNode;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcWalkerInstance;

public class L2NpcWalkerAI extends L2CharacterAI
  implements Runnable
{
  private static final int DEFAULT_MOVE_DELAY = 0;
  private long _nextMoveTime;
  private boolean _walkingToNextPoint = false;
  int _homeX;
  int _homeY;
  int _homeZ;
  private final FastList<L2NpcWalkerNode> _route = NpcWalkerRoutesTable.getInstance().getRouteForNpc(getActor().getNpcId());
  private int _currentPos;

  public L2NpcWalkerAI(L2Character.AIAccessor accessor)
  {
    super(accessor);

    ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 0L, 1000L);
  }

  public void run()
  {
    onEvtThink();
  }

  protected void onEvtThink()
  {
    if (!Config.ALLOW_NPC_WALKERS) {
      return;
    }
    if (isWalkingToNextPoint())
    {
      checkArrived();
      return;
    }

    if (_nextMoveTime < System.currentTimeMillis())
      walkToLocation();
  }

  protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
  {
    _log.warning("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + _currentPos + "], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z + ". Teleporting to next point");

    int destinationX = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveX();
    int destinationY = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveY();
    int destinationZ = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveZ();

    getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
    super.onEvtArrivedBlocked(blocked_at_pos);
  }

  private void checkArrived()
  {
    int destinationX = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveX();
    int destinationY = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveY();
    int destinationZ = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveZ();

    if ((getActor().getX() == destinationX) && (getActor().getY() == destinationY) && (getActor().getZ() == destinationZ))
    {
      String chat = ((L2NpcWalkerNode)_route.get(_currentPos)).getChatText();
      if ((chat != null) && (!chat.equals("")))
      {
        try
        {
          getActor().broadcastChat(chat);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
          _log.info("L2NpcWalkerInstance: Error, " + e);
        }

      }

      long delay = ((L2NpcWalkerNode)_route.get(_currentPos)).getDelay() * 1000;

      if (delay <= 0L)
      {
        delay = 0L;
        if (Config.DEVELOPER) {
          _log.warning("Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " + 0 + " secs instead.");
        }
      }
      _nextMoveTime = (System.currentTimeMillis() + delay);
      setWalkingToNextPoint(false);
    }
  }

  private void walkToLocation()
  {
    if (_currentPos < _route.size() - 1)
      _currentPos += 1;
    else {
      _currentPos = 0;
    }
    boolean moveType = ((L2NpcWalkerNode)_route.get(_currentPos)).getRunning();

    if (moveType)
      getActor().setRunning();
    else {
      getActor().setWalking();
    }

    int destinationX = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveX();
    int destinationY = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveY();
    int destinationZ = ((L2NpcWalkerNode)_route.get(_currentPos)).getMoveZ();

    setWalkingToNextPoint(true);

    setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destinationX, destinationY, destinationZ, getActor().calcHeading(destinationX, destinationY)));
  }

  public L2NpcWalkerInstance getActor()
  {
    return (L2NpcWalkerInstance)super.getActor();
  }

  public int getHomeX()
  {
    return _homeX;
  }

  public int getHomeY()
  {
    return _homeY;
  }

  public int getHomeZ()
  {
    return _homeZ;
  }

  public void setHomeX(int homeX)
  {
    _homeX = homeX;
  }

  public void setHomeY(int homeY)
  {
    _homeY = homeY;
  }

  public void setHomeZ(int homeZ)
  {
    _homeZ = homeZ;
  }

  public boolean isWalkingToNextPoint()
  {
    return _walkingToNextPoint;
  }

  public void setWalkingToNextPoint(boolean value)
  {
    _walkingToNextPoint = value;
  }
}