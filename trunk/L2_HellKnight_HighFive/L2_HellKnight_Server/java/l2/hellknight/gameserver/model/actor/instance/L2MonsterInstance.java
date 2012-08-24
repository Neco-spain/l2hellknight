/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2.hellknight.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.knownlist.MonsterKnownList;
import l2.hellknight.gameserver.templates.chars.L2NpcTemplate;
import l2.hellknight.gameserver.util.MinionList;
import l2.hellknight.util.Rnd;


/**
 * This class manages all Monsters.
 *
 * L2MonsterInstance :<BR><BR>
 * <li>L2MinionInstance</li>
 * <li>L2RaidBossInstance </li>
 * <li>L2GrandBossInstance </li>
 *
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class L2MonsterInstance extends L2Attackable
{
	//private static Logger _log = Logger.getLogger(L2MonsterInstance.class.getName());
	
	private boolean _enableMinions = true;
	private boolean _canAgroWhileMoving = false;
	private L2MonsterInstance _master = null;
	private MinionList _minionList = null;
	protected ScheduledFuture<?> _maintenanceTask = null;
	private int _aggroRangeOverride = 0;
	private String _clanOverride = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2MonsterInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR) </li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId the identifier of the object to initialized
	 * @param template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MonsterInstance);
		setAutoAttackable(true);
	}
	
	@Override
	public final MonsterKnownList getKnownList()
	{
		return (MonsterKnownList)super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new MonsterKnownList(this));
	}
	
	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return super.isAutoAttackable(attacker) && !isEventMob;
	}
	
	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR><BR>
	 */
	@Override
	public boolean isAggressive()
	{
		return (getTemplate().getAggroRange() > 0) && !isEventMob;
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
			getMinionList().onMasterTeleported();
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	/**
	 * Spawn all minions at a regular interval
	 *
	 */
	protected void startMaintenanceTask()
	{
		// maintenance task now used only for minions spawn
		if (getTemplate().getMinionData() == null)
			return;

		if (_maintenanceTask == null)
		{
			_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				@Override
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
	  
	public final boolean canAgroWhileMoving() 
	{
		return _canAgroWhileMoving;
	}

	public final void setCanAgroWhileMoving() 
	{
		_canAgroWhileMoving = true;
	}
}
