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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javolution.util.FastList;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.L2CharacterAI;
import l2.hellknight.gameserver.ai.L2DoorAI;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.instancemanager.CastleManager;
import l2.hellknight.gameserver.instancemanager.ClanHallManager;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.TerritoryWarManager;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.knownlist.DoorKnownList;
import l2.hellknight.gameserver.model.actor.stat.DoorStat;
import l2.hellknight.gameserver.model.actor.status.DoorStatus;
import l2.hellknight.gameserver.model.actor.templates.L2DoorTemplate;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.ClanHall;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.entity.clanhall.SiegableHall;
import l2.hellknight.gameserver.model.items.L2Weapon;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.DoorStatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.OnEventTrigger;
import l2.hellknight.gameserver.network.serverpackets.StaticObject;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{
	protected static final Logger log = Logger.getLogger(L2DoorInstance.class.getName());
	
	private static final byte OPEN_BY_CLICK = 1;
	private static final byte OPEN_BY_TIME = 2;
	private static final byte OPEN_BY_ITEM = 4;
	private static final byte OPEN_BY_SKILL = 8;
	private static final byte OPEN_BY_CYCLE = 16;
	
	/** The castle index in the array of L2Castle this L2NpcInstance belongs to */
	private int _castleIndex = -2;
	/** The fort index in the array of L2Fort this L2NpcInstance belongs to */
	private int _fortIndex = -2;
	private ClanHall _clanHall;
	private boolean _open = false;
	private boolean _isAttackableDoor = false;
	private boolean _isTargetable;
	private boolean _checkCollision;
	private int _openType = 0;
	private int _meshindex = 1;
	private int _level = 0;
	protected int _closeTime = -1;
	protected int _openTime = -1;
	protected int _randomTime = -1;
	// used for autoclose on open
	private Future<?> _autoCloseTask;
	
	/**
	 * @param objectId
	 * @param template
	 * @param data
	 */
	public L2DoorInstance(int objectId, L2DoorTemplate template, StatsSet data)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DoorInstance);
		setIsInvul(false);
		_isTargetable = data.getBool("targetable", true);
		if (getGroupName() != null)
			DoorTable.addDoorGroup(getGroupName(), getDoorId());
		if (data.getString("default_status","close").equals("open"))
			_open = true;
		_closeTime = data.getInteger("close_time", -1);
		_level = data.getInteger("level", 0);
		_openType = data.getInteger("open_method", 0);
		_checkCollision = data.getBool("check_collision", true);
		if (isOpenableByTime())
		{
			_openTime = data.getInteger("open_time");
			_randomTime = data.getInteger("random_time", -1);
			startTimerOpen();
		}
		int clanhallId = data.getInteger("clanhall_id", 0);
		if(clanhallId > 0)
		{
			ClanHall hall = ClanHallManager.getAllClanHalls().get(clanhallId);
			if(hall != null)
			{
				setClanHall(hall);
				hall.getDoors().add(this);
			}
		}
	}
	
	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}
		
		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}
		
		@Override
		public void moveTo(int x, int y, int z)
		{
		}
		
		@Override
		public void stopMove(L2CharPosition pos)
		{
		}
		
		@Override
		public void doAttack(L2Character target)
		{
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2DoorAI(new AIAccessor());
				return _ai;
			}
		}
		return ai;
	}

	private void startTimerOpen()
	{
		int delay = _open ? _openTime : _closeTime;
		if (_randomTime > 0)
			delay += Rnd.get(_randomTime);
		ThreadPoolManager.getInstance().scheduleGeneral(new TimerOpen(), delay*1000);
	}
	
	@Override
	public final DoorKnownList getKnownList()
	{
		return (DoorKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DoorKnownList(this));
	}
	
	@Override
	public final DoorStat getStat()
	{
		return (DoorStat) super.getStat();
	}

	@Override
	public L2DoorTemplate getTemplate()
	{
		return (L2DoorTemplate) super.getTemplate();
		
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new DoorStat(this));
	}
	
	@Override
	public final DoorStatus getStatus()
	{
		return (DoorStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new DoorStatus(this));
	}

	public final boolean isOpenableBySkill()
	{
		return (_openType & OPEN_BY_SKILL) != 0;
	}
	
	public final boolean isOpenableByItem()
	{
		return (_openType & OPEN_BY_ITEM) != 0;
	}
	
	public final boolean isOpenableByClick()
	{
		return (_openType & OPEN_BY_CLICK) != 0;
	}
	
	public final boolean isOpenableByTime()
	{
		return (_openType & OPEN_BY_TIME) != 0;
	}
	
	public final boolean isOpenableByCycle()
	{
		return (_openType & OPEN_BY_CYCLE) != 0;
	}
	
	@Override
	public final int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return getTemplate().doorId;
	}
	
	/**
	 * @return Returns the open.
	 */
	public boolean getOpen()
	{
		return _open;
	}

	/**
	 * @param open The open to set.
	 */
	public void setOpen(boolean open)
	{
		_open = open;
		if (getChildId() > 0)
		{
			getSiblingDoor(getChildId()).notifyChildEvent(open);
		}
	}
	
	public boolean getIsAttackableDoor()
	{
		return _isAttackableDoor;
	}
	
	public boolean getIsShowHp()
	{
		return getTemplate().showHp;
	}
	
	public void setIsAttackableDoor(boolean val)
	{
		_isAttackableDoor = val;
	}
	
	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}
	
	public final Castle getCastle()
	{
		if (_castleIndex < 0)
			_castleIndex = CastleManager.getInstance().getCastleIndex(this);
		if (_castleIndex < 0)
			return null;
		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}
	
	public final Fort getFort()
	{
		if (_fortIndex < 0)
			_fortIndex = FortManager.getInstance().getFortIndex(this);
		if (_fortIndex < 0)
			return null;
		return FortManager.getInstance().getForts().get(_fortIndex);
	}
	
	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}
	
	public ClanHall getClanHall()
	{
		return _clanHall;
	}
	
	public boolean isEnemy()
	{
		if (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isActive() && getIsShowHp())
			return true;
		if (getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isActive() && getIsShowHp())
			return true;
		if(getClanHall() != null && getClanHall().isSiegableHall() && ((SiegableHall)getClanHall()).getSiegeZone().isActive() && getIsShowHp())
			return true;
		return false;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Doors can`t be attacked by NPCs
		if (!(attacker instanceof L2Playable))
			return false;

		if (getIsAttackableDoor())
			return true;
		if (!getIsShowHp())
			return false;
		
		L2PcInstance actingPlayer = attacker.getActingPlayer();
		
		if(getClanHall() != null)
		{
			if(!getClanHall().isSiegableHall())
				return false;
			return ((SiegableHall)getClanHall()).isInSiege()
					&& ((SiegableHall)getClanHall()).getSiege().doorIsAutoAttackable()
					&& ((SiegableHall)getClanHall()).getSiege().checkIsAttacker(actingPlayer.getClan());
		}
		// Attackable  only during siege by everyone (not owner)
		boolean isCastle = (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isActive());
		boolean isFort = (getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isActive());
		int activeSiegeId = (getFort() != null ? getFort().getFortId() : (getCastle() != null ? getCastle().getCastleId() : 0));
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (TerritoryWarManager.getInstance().isAllyField(actingPlayer, activeSiegeId))
				return false;
			return true;
		}
		else if (isFort)
		{
			L2Clan clan = actingPlayer.getClan();
			if (clan != null && clan == getFort().getOwnerClan())
				return false;
		}
		else if (isCastle)
		{
			L2Clan clan = actingPlayer.getClan();
			if (clan != null && clan.getClanId() == getCastle().getOwnerId())
				return false;
		}
		return (isCastle || isFort);
	}
	
	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
	}
	
	/**
	 * Return null.<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;
		
		StaticObject su = new StaticObject(this, false);
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);
		OnEventTrigger oe = null;
		if (getEmitter() > 0)
			oe = new OnEventTrigger(this, getOpen());

		for (L2PcInstance player : knownPlayers)
		{
			if (player == null)
				continue;

			if (player.isGM())
				su = new StaticObject(this, true);
			
			if ((getCastle() != null && getCastle().getCastleId() > 0) || (getFort() != null && getFort().getFortId() > 0))
				su = new StaticObject(this, true);
			
			player.sendPacket(su);
			player.sendPacket(dsu);
			if (oe != null)
				player.sendPacket(oe);
		}
	}

	public final void openMe()
	{
		if (getGroupName() != null)
		{
			manageGroupOpen(true, getGroupName());
			return;
		}
		setOpen(true);
		broadcastStatusUpdate();
		startAutoCloseTask();
	}

	public final void closeMe()
	{
		//remove close task
		Future<?> oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			_autoCloseTask = null;
			oldTask.cancel(false);
		}
		if (getGroupName() != null)
		{
			manageGroupOpen(false, getGroupName());
			return;
		}
		setOpen(false);
		broadcastStatusUpdate();
	}

	private void manageGroupOpen(boolean open, String groupName)
	{
		Set<Integer> set = DoorTable.getDoorsByGroup(groupName);
		L2DoorInstance first = null;
		for (Integer id : set)
		{
			L2DoorInstance door = getSiblingDoor(id);
			if (first == null)
				first = door;
			
			if (door.getOpen() != open)
			{
				door.setOpen(open);
				door.broadcastStatusUpdate();
			}
		}
		if (first != null && open)
			first.startAutoCloseTask(); //only one from group
	}
	
	/**
	 * Door notify child about open state change
	 * @param open true if opened
	 */
	private void notifyChildEvent(boolean open)
	{
		byte openThis = open ? getTemplate().masterDoorOpen : getTemplate().masterDoorClose;
		
		if (openThis == 0)
			return;
		else if (openThis == 1)
			openMe();
		else if (openThis == -1)
			closeMe();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() +"["+ getTemplate().doorId + "]("+getObjectId()+")";
	}
	
	public String getDoorName()
	{
		return getTemplate().name;
	}
	
	public int getX(int i)
	{
		return getTemplate().nodeX[i];
	}
	
	public int getY(int i)
	{
		return getTemplate().nodeY[i];
	}
	
	public int getZMin()
	{
		return getTemplate().nodeZ;
	}
	
	public int getZMax()
	{
		return getTemplate().nodeZ + getTemplate().height;
	}
	
	public Collection<L2DefenderInstance> getKnownDefenders()
	{
		FastList<L2DefenderInstance> result = new FastList<>();
		
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (obj instanceof L2DefenderInstance)
				result.add((L2DefenderInstance) obj);
		}
	
		return result;
	}

	public void setMeshIndex(int mesh)
	{
		_meshindex = mesh;
	}
	
	public int getMeshIndex()
	{
		return _meshindex;
	}
	
	public int getEmitter()
	{
		return getTemplate().emmiter;
	}

	public boolean isWall()
	{
		return getTemplate().isWall;
	}
	
	public String getGroupName()
	{
		return getTemplate().groupName;
	}
	
	public int getChildId()
	{
		return getTemplate().childDoorId;
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isWall() && !(attacker instanceof L2SiegeSummonInstance))
			return;
		
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	@Override
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		// doors can't be damaged by DOTs
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		boolean isFort = (getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress());
		boolean isCastle = (getCastle() != null	&& getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());
		boolean isHall = (getClanHall() != null && getClanHall().isSiegableHall() && ((SiegableHall)getClanHall()).isInSiege());
		
		if (isFort || isCastle || isHall)
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
		return true;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (getEmitter() > 0)
			activeChar.sendPacket(new OnEventTrigger(this, getOpen()));
		
		activeChar.sendPacket(new StaticObject(this, activeChar.isGM()));
	}

	public void setTargetable(boolean b)
	{
		_isTargetable = b;
		broadcastStatusUpdate();
	}
	
	@Override
	public boolean isTargetable()
	{
		return _isTargetable;
	}
	
	public boolean checkCollision()
	{
		return _checkCollision;
	}
	
	/**
	 * All doors are stored at DoorTable except instance doors
	 * @param doorId
	 * @return
	 */
	private L2DoorInstance getSiblingDoor(int doorId)
	{
		if (getInstanceId() == 0)
			return DoorTable.getInstance().getDoor(doorId);

		Instance inst = InstanceManager.getInstance().getInstance(getInstanceId());
		if (inst != null)
			return inst.getDoor(doorId);

		return null; // 2 late
	}
	
	private void startAutoCloseTask()
	{
		if (_closeTime < 0 || isOpenableByTime())
			return;
		Future<?> oldTask = _autoCloseTask;
		if (oldTask != null)
		{
			_autoCloseTask = null;
			oldTask.cancel(false);
		}
		_autoCloseTask = ThreadPoolManager.getInstance().scheduleGeneral(new AutoClose(), _closeTime * 1000);
	}
	
	class AutoClose implements Runnable
	{
		@Override
		public void run()
		{
			if (getOpen())
				closeMe();
		}
	}
	
	class TimerOpen implements Runnable
	{
		@Override
		public void run()
		{
			boolean open = getOpen();
			if (open)
				closeMe();
			else
				openMe();
			
			//_log.info("Door "+L2DoorInstance.this+ " switched state "+open);
			int delay = open ? _closeTime : _openTime;
			if (_randomTime > 0)
				delay += Rnd.get(_randomTime);
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay*1000);
		}
	}
	
	@Override
	public boolean isDoor()
	{
		return true;
	}
}
