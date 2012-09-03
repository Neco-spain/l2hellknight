package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.Events;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.L2CharacterAI;
import l2rt.gameserver.ai.L2StaticObjectAI;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoControl;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.gameserver.model.entity.residence.ResidenceType;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.MyTargetSelected;
import l2rt.gameserver.network.serverpackets.StaticObject;
import l2rt.gameserver.network.serverpackets.ValidateLocation;
import l2rt.gameserver.templates.L2CharTemplate;
import l2rt.gameserver.templates.L2Weapon;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class L2DoorInstance extends L2Character implements GeoControl
{
	protected static Logger _log = Logger.getLogger(L2DoorInstance.class.getName());

	protected final int _doorId;
	protected final String _name;
	private boolean _open;
	public boolean geoOpen;
	private boolean _geodata = true;
	private boolean _unlockable;
	private boolean _isHPVisible;
	private boolean _siegeWeaponOlnyAttackable;
	private Residence _siegeUnit;
	private int upgradeHp;

	private L2Territory geoPos;
	private HashMap<Long, Byte> geoAround;

	public L2Territory getGeoPos()
	{
		return geoPos;
	}

	public void setGeoPos(L2Territory value)
	{
		geoPos = value;
	}

	public HashMap<Long, Byte> getGeoAround()
	{
		return geoAround;
	}

	public void setGeoAround(HashMap<Long, Byte> value)
	{
		geoAround = value;
	}

	public int key;
	public byte level = 1;

	protected int _autoActionDelay = -1;
	private ScheduledFuture<?> _autoActionTask;

	@Override
	public L2CharacterAI getAI()
	{
		if(_ai == null)
			_ai = new L2StaticObjectAI(this);
		return _ai;
	}

	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable, boolean showHp)
	{
		super(objectId, template);
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
		_isHPVisible = showHp;
		geoOpen = true;
		setIsInvul(true);
	}

	public boolean isUnlockable()
	{
		return _unlockable;
	}

	@Override
	public byte getLevel()
	{
		return level;
	}

	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}

	/**
	 * @return Returns true if door is opened.
	 */
	public boolean isOpen()
	{
		return _open;
	}

	/**
	 * @param open The open to set.
	 */
	public synchronized void setOpen(boolean open)
	{
		_open = open;
	}

	/**
	 * Sets the delay in milliseconds for automatic opening/closing
	 * of this door instance.
	 * <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 *
	 * @param actionDelay время задержки между действием
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if(_autoActionDelay == actionDelay)
			return;

		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(false);
			_autoActionTask = null;
		}

		if(actionDelay > -1)
			_autoActionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoOpenClose(), actionDelay, actionDelay);

		_autoActionDelay = actionDelay;
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHpRatio() * 6);
		if(dmg > 6)
			return 6;
		if(dmg < 0)
			return 0;
		return dmg;
	}

	//TODO разобраться
	public boolean isEnemyOf(L2Character cha)
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return isAttackable(attacker);
	}

	@Override
	public boolean isAttackable(L2Character attacker)
	{
		if(attacker == null)
			return false;
		if(isSiegeWeaponOnlyAttackable() && (!(attacker instanceof L2Summon) || !((L2Summon) attacker).isSiegeWeapon()))
			return false;
		L2Player player = attacker.getPlayer();
		if(player == null)
			return false;
		L2Clan clan = player.getClan();
		if(clan != null && SiegeManager.getSiege(this, true) == clan.getSiege() && clan.isDefender())
			return false;
		if(player.getTerritorySiege() > -1 && getSiegeUnit() != null && player.getTerritorySiege() == getSiegeUnit().getId())
			return false;
		if(clan != null && getSiegeUnit() != null && clan.getClanId() == getSiegeUnit().getOwnerId() && player.getTerritorySiege() == -1)
			return false;
		return !isInvul();
	}

	@Override
	public void updateAbnormalEffect()
	{}

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
	public void onAction(L2Player player, boolean shift)
	{
		if(player == null)
			return;

		if(Events.onAction(player, this, shift))
			return;

		if(this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));

			if(isAutoAttackable(player))
				//player.sendPacket(new DoorStatusUpdate(this));
				player.sendPacket(new StaticObject(this));

			// correct location
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
			if(isAutoAttackable(player))
				player.getAI().Attack(this, false, shift);
			else if(!isInRange(player, INTERACTION_DISTANCE))
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			else
			{
				if(!Config.ALLOW_CH_DOOR_OPEN_ON_CLICK || getSiegeUnit() == null || getSiegeUnit().getSiege() != null && getSiegeUnit().getSiege().isInProgress() || TerritorySiege.isInProgress() && (getSiegeUnit().getType() == ResidenceType.Castle || getSiegeUnit().getType() == ResidenceType.Fortress) || player.getClan() == null || player.getClanId() != getSiegeUnit().getOwnerId())
				{
					player.sendActionFailed();
					return;
				}
				if(getSiegeUnit().getType() == ResidenceType.Castle && (player.getClanPrivileges() & L2Clan.CP_CS_ENTRY_EXIT) == L2Clan.CP_CS_ENTRY_EXIT)
					switchOpenClose();
				else if(getSiegeUnit().getType() == ResidenceType.Fortress && (player.getClanPrivileges() & L2Clan.CP_CS_ENTRY_EXIT) == L2Clan.CP_CS_ENTRY_EXIT)
					switchOpenClose();
				else if(getSiegeUnit().getType() == ResidenceType.Clanhall && (player.getClanPrivileges() & L2Clan.CP_CH_ENTRY_EXIT) == L2Clan.CP_CH_ENTRY_EXIT)
					switchOpenClose();
				player.sendActionFailed();
			}
		}
	}

	public void switchOpenClose()
	{
		if(!isOpen())
			openMe();
		else
			closeMe();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		StaticObject su = new StaticObject(this);
		for(L2Player player : L2World.getAroundPlayers(this))
			if(player != null)
				player.sendPacket(su);
	}

	public void onOpen()
	{
		scheduleCloseMe(60000);
	}

	public void onClose()
	{
		closeMe();
	}

	/**
	 * Вызывает задание на закрытие двери через заданное время.
	 * @param delay Время в миллисекундах
	 */
	public final void scheduleCloseMe(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), delay);
	}

	public final void closeMe()
	{
		if(isDead())
			return;

		synchronized (this)
		{
			_open = false;
		}

		if(geoOpen)
			setGeoOpen(false);

		broadcastStatusUpdate();
	}

	public final void openMe()
	{
		if(isDead())
			return;

		synchronized (this)
		{
			_open = true;
		}

		if(!geoOpen)
			setGeoOpen(true);

		broadcastStatusUpdate();
	}

	@Override
	public String toString()
	{
		return "door " + _doorId;
	}

	public String getDoorName()
	{
		return _name;
	}

	public void setSiegeUnit(Residence siegeUnit)
	{
		_siegeUnit = siegeUnit;
	}

	public Residence getSiegeUnit()
	{
		return _siegeUnit;
	}

	@Override
	public void doDie(L2Character killer)
	{
		Siege s = SiegeManager.getSiege(this, true);
		if(s != null)
		{
			for(SiegeClan sc : s.getDefenderClans().values())
			{
				L2Clan clan = sc.getClan();
				if(clan != null)
					for(L2Player player : clan.getOnlineMembers(0))
						if(player != null)
							player.sendPacket(Msg.THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN);
			}

			for(SiegeClan sc : s.getAttackerClans().values())
			{
				L2Clan clan = sc.getClan();
				if(clan != null)
					for(L2Player player : clan.getOnlineMembers(0))
						if(player != null)
							player.sendPacket(Msg.THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN);
			}
		}

		// TODO territory wars сообщение

		setGeoOpen(true);

		super.doDie(killer);
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		closeMe();
	}

	public boolean isHPVisible()
	{
		return _isHPVisible;
	}

	public void setHPVisible(boolean val)
	{
		_isHPVisible = val;
	}

	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() + upgradeHp;
	}

	public void setUpgradeHp(int hp)
	{
		upgradeHp = hp;
	}

	public int getUpgradeHp()
	{
		return upgradeHp;
	}

	@Override
	public int getPDef(L2Character target)
	{
		return super.getPDef(target);
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return super.getMDef(target, skill);
	}

	/**
	 * Двери на осадах уязвимы во время осады.
	 * Остальные двери не уязвимы вообще.
	 * @return инвульная ли дверь.
	 */
	@Override
	public boolean isInvul()
	{
		if(_siegeUnit != null)
		{
			if(_siegeUnit.getSiege() != null && _siegeUnit.getSiege().isInProgress())
				return false;
			if(TerritorySiege.isInProgress() && (_siegeUnit.getType() == ResidenceType.Castle || _siegeUnit.getType() == ResidenceType.Fortress))
				return false;
		}
		return super.isInvul();
	}

	public int getDoorHeight()
	{
		return geoPos.getZmax() - geoPos.getZmin() & 0xfff0;
	}

	/**
	 * Дверь/стена может быть атаоквана только осадным орудием
	 * @return true если дверь/стену можно атаковать только осадным орудием
	 */
	public boolean isSiegeWeaponOnlyAttackable()
	{
		return _siegeWeaponOlnyAttackable;
	}

	/**
	 * Устанавливает двери/стене признак возможности атаковать только осадным оружием
	 * @param val true - дверь/стену можно атаковать только осадным орудием
	 */
	public void setSiegeWeaponOlnyAttackable(boolean val)
	{
		_siegeWeaponOlnyAttackable = val;
	}

	/**
	 * Устанавливает значение закрытости\открытости в геодате<br>
	 * @param val новое значение
	 */
	private void setGeoOpen(boolean val)
	{
		if(geoOpen == val)
			return;

		geoOpen = val;

		if(!getGeodata())
			return;

		if(val)
			GeoEngine.returnGeoAtControl(this);
		else
			GeoEngine.applyControl(this);
	}

	private class CloseTask implements Runnable
	{
		public void run()
		{
			onClose();
		}
	}

	/**
	 * Manages the auto open and closing of a door.
	 */
	private class AutoOpenClose implements Runnable
	{
		public void run()
		{
			if(!isOpen())
				openMe();
			else
				closeMe();
		}
	}

	public void setGeodata(boolean value)
	{
		_geodata = value;
	}

	public boolean getGeodata()
	{
		return _geodata;
	}

	@Override
	public L2DoorInstance clone()
	{
		L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), _template, _doorId, _name, _unlockable, _isHPVisible);
		door.setXYZInvisible(getLoc());
		door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp(), true);
		door.setOpen(_open);
		door.setSiegeWeaponOlnyAttackable(_siegeWeaponOlnyAttackable);
		door.setGeodata(_geodata);
		door.geoPos = geoPos;
		door.level = level;
		door.key = key;
		door.setIsInvul(isInvul());
		return door;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean isGeoCloser()
	{
		return true;
	}
}