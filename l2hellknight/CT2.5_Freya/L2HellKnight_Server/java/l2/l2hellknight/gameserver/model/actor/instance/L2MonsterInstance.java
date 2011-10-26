package l2.hellknight.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.knownlist.MonsterKnownList;
import l2.hellknight.gameserver.templates.L2NpcTemplate;
import l2.hellknight.gameserver.util.MinionList;
import l2.hellknight.util.Rnd;

public class L2MonsterInstance extends L2Attackable
{
	//private static Logger _log = Logger.getLogger(L2MonsterInstance.class.getName());
	
	private boolean _enableMinions = true;
	
	private L2MonsterInstance _master = null;
	private MinionList _minionList = null;
	
	protected ScheduledFuture<?> _maintenanceTask = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MonsterInstance);
		setAutoAttackable(true);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		return (MonsterKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new MonsterKnownList(this));
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return super.isAutoAttackable(attacker) && !isEventMob;
	}
	
	@Override
	public boolean isAggressive()
	{
		return (getAggroRange() > 0) && !isEventMob;
	}
	
	@Override
	public void onSpawn()
	{
		if (!isTeleporting())
		{
			if (getLeader() != null)
			{
				setIsNoRndWalk(true);
				setIsRaidMinion(getLeader().isRaid());
				getLeader().getMinionList().onMinionSpawn(this);
			}

			// delete spawned minions before dynamic minions spawned by script
			if (hasMinions())
				getMinionList().onMasterSpawn(); 

			startMaintenanceTask();

		}
		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();

		if (hasMinions())
		{
			getMinionList().onMasterTeleported();
		}
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	protected void startMaintenanceTask()
	{
		// maintenance task now used only for minions spawn
		if (getTemplate().getMinionData() == null)
			return;
		
		if (_maintenanceTask == null)
		{
			_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					if (_enableMinions)
						getMinionList().spawnMinions();
				}
			}, getMaintenanceInterval() + Rnd.get(1000));
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false); // doesn't do it?
			_maintenanceTask = null;
		}
			
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		if (hasMinions())
			getMinionList().onMasterDie(true);

		if (getLeader() != null)
			getLeader().getMinionList().onMinionDie(this, 0);

		super.deleteMe();
	}
	
	@Override
	public L2MonsterInstance getLeader()
	{
		return _master;
	}

	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}

	public void enableMinions(boolean b)
	{
		_enableMinions = b;
	}

	public boolean hasMinions()
	{
		return _minionList != null;
	}
	
	public MinionList getMinionList()
	{
		if (_minionList == null)
			_minionList = new MinionList(this);

		return _minionList;
	}
		
	private int _aggroRangeOverride = 0;
	private String _clanOverride = null;
	
	public void setIsAggresiveOverride(int aggroR)
	{
		_aggroRangeOverride = aggroR;
	}
	
	public void setClanOverride(String newClan)
	{
		_clanOverride = newClan;
	}
	
	@Override
	public int getAggroRange()
	{
		//Aggresive override for special mobs
		if (_aggroRangeOverride > 0)
			return _aggroRangeOverride;
		
		return super.getAggroRange();
	}
	
	@Override
	public String getClan()
	{
		//Clan name override for special mobs
		if (_clanOverride != null)
			return _clanOverride;
		
		return super.getClan();
	}
	
	@Override
	public int getClanRange()
	{
		//Default faction range 500
		if (_clanOverride != null)
			return 500;
		
		return super.getClanRange();
	}
	
	private boolean _canAgroWhileMoving = false;
	  
	public final boolean canAgroWhileMoving() 
	{
		return _canAgroWhileMoving;
	}

	public final void setCanAgroWhileMoving() 
	{
		_canAgroWhileMoving = true;
	}
}
