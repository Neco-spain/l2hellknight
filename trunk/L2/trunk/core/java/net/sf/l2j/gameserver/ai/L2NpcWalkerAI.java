package net.sf.l2j.gameserver.ai;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2NpcWalkerNode;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcWalkerInstance;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final int DEFAULT_MOVE_DELAY = 0;

	private long _nextMoveTime;

	private boolean _walkingToNextPoint = false;

	/**
	 * home points for xyz
	 */
	int _homeX, _homeY, _homeZ;

	/**
	 * route of the current npc
	 */
	private final FastList<L2NpcWalkerNode> _route = NpcWalkerRoutesTable.getInstance().getRouteForNpc(getActor().getNpcId());

	/**
	 * current node
	 */
	private int _currentPos;


	/**
	 * Constructor of L2CharacterAI.<BR><BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2NpcWalkerAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		// Do we really need 2 minutes delay before start?
		// no we dont... :)
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 0, 1000);
	}

	public void run()
	{
		onEvtThink();
	}

	protected void onEvtThink()
	{
		if(isWalkingToNextPoint())
		{
			checkArrived();
			return;
		}

		if(_nextMoveTime < System.currentTimeMillis())
			walkToLocation();
	}

	/**
	 * If npc can't walk to it's target then just teleport to next point
	 * @param blocked_at_pos ignoring it
	 */
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
		_log.warning("NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + _currentPos + "], coords: " + blocked_at_pos.x + ", " + blocked_at_pos.y + ", " + blocked_at_pos.z + ". Teleporting to next point");

		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	private void checkArrived()
	{
		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		if(getActor().getX() == destinationX && getActor().getY() == destinationY && getActor().getZ() == destinationZ)
		{
			String chat = _route.get(_currentPos).getChatText();
			if(chat != null && !chat.equals(""))
			{
				try
				{
					getActor().broadcastChat(chat);
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					_log.info("L2NpcWalkerInstance: Error, " + e);
				}
			}

			//time in millis
			long delay = _route.get(_currentPos).getDelay()*1000;

			//sleeps between each move
			if(delay <= 0)
			{
				delay = DEFAULT_MOVE_DELAY;
				if(Config.DEVELOPER)
					_log.warning("Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " + DEFAULT_MOVE_DELAY + " secs instead.");
			}

			_nextMoveTime = System.currentTimeMillis() + delay;
			setWalkingToNextPoint(false);
		}
	}

	private void walkToLocation()
	{
		if(_currentPos < (_route.size() - 1))
			_currentPos++;
		else
			_currentPos = 0;

		boolean moveType = _route.get(_currentPos).getRunning();

		/**
		 * false - walking
		 * true - Running
		 */
		if(moveType)
			getActor().setRunning();
		else
			getActor().setWalking();

		//now we define destination
		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		//notify AI of MOVE_TO
		setWalkingToNextPoint(true);
	
		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destinationX, destinationY, destinationZ, 0));
	}

	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) super.getActor();
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
