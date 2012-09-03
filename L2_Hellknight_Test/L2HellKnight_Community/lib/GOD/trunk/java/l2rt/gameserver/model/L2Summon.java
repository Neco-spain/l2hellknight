package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.config.ConfigSystem;
import l2rt.extensions.scripts.Events;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.L2SummonAI;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.Duel;
import l2rt.gameserver.model.instances.L2CubicInstance;
import l2rt.gameserver.model.instances.L2CubicInstance.CubicType;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PetInventory;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.taskmanager.DecayTaskManager;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;

public abstract class L2Summon extends L2Playable
{
	private static final int SIEGE_GOLEM_ID = 14737;
	private static final int SIEGE_CANNON_ID = 14768;
	private static final int SWOOP_CANNON_ID = 14839;
	private static final int SUMMON_DISAPPEAR_RANGE = 2500;

	protected long _exp = 0;
	protected int _sp = 0;
	private int _maxLoad, _spsCharged = 0, _attackRange = 36; //Melee range
	private boolean _follow = true, _posessed = false, _ssCharged = false;
	private long _ownerStoreId = 0;

	public L2Summon(final int objectId, final L2NpcTemplate template, final L2Player owner)
	{
		super(objectId, template);
		_ownerStoreId = owner.getStoredId();

		GArray<L2ItemInstance> items = new GArray<L2ItemInstance>();
		for(L2ItemInstance item : owner.getInventory().getPaperdollItems())
			if(item != null && !items.contains(item)) // проверяем на дубли, т.к. один предмет может быть в двух слотах сразу
				items.add(item);

		for(L2ItemInstance item : items)
			for(Func func : item.getStatFuncs())
				switch(func._stat)
				{
					case BLEED_RECEPTIVE:
					case POISON_RECEPTIVE:
					case STUN_RECEPTIVE:
					case ROOT_RECEPTIVE:
					case MENTAL_RECEPTIVE:
					case SLEEP_RECEPTIVE:
					case PARALYZE_RECEPTIVE:
					case CANCEL_RECEPTIVE:
					case DEBUFF_RECEPTIVE:
					case MAGIC_RECEPTIVE:
					case BLEED_POWER:
					case POISON_POWER:
					case STUN_POWER:
					case ROOT_POWER:
					case MENTAL_POWER:
					case SLEEP_POWER:
					case PARALYZE_POWER:
					case CANCEL_POWER:
					case DEBUFF_POWER:
					case MAGIC_POWER:
					case FIRE_RECEPTIVE:
					case WIND_RECEPTIVE:
					case WATER_RECEPTIVE:
					case EARTH_RECEPTIVE:
					case UNHOLY_RECEPTIVE:
					case SACRED_RECEPTIVE:
					case ATTACK_ELEMENT_FIRE:
					case ATTACK_ELEMENT_WATER:
					case ATTACK_ELEMENT_WIND:
					case ATTACK_ELEMENT_EARTH:
					case ATTACK_ELEMENT_SACRED:
					case ATTACK_ELEMENT_UNHOLY:
						addStatFunc(func);
						break;
				}

		setXYZInvisible(owner.getX() + Rnd.get(-100, 100), owner.getY() + Rnd.get(-100, 100), owner.getZ());
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		onSpawn();
	}

	@Override
	public void onSpawn()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		L2Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowAdd(this));

		L2Effect[] effects = owner.getEffectList().getAllFirstEffects();
		for(L2Effect effect : effects)
			if(effect != null) { //TODO: скорей всего не все еффекты хозяина вешаются...
				this.getEffectList().addEffect(effect);
				this.updateEffectIcons();
				this.broadcastPetInfo();
			}
			
	}

	@Override
	public L2SummonAI getAI()
	{
		if(_ai == null)
			_ai = new L2SummonAI(this);
		return (L2SummonAI) _ai;
	}

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) _template;
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPetInfo();
	}

	@Override
	public void updateStats()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		for(L2Player player : L2World.getAroundPlayers(this))
			if(player != null)
				if(player == owner)
					player.sendPacket(new PetInfo(this, 1));
				else
					player.sendPacket(new NpcInfo(this, player, 1));
	}

	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(final L2Player player, boolean shift)
	{
		L2Player owner = getPlayer();
		if(owner == null)
		{
			player.sendActionFailed();
			return;
		}

		if(Events.onAction(player, this, shift))
			return;

		// Check if the L2Player is confused
		if(player.isConfused() || player.isBlocked())
			player.sendActionFailed();

		if(player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0), makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
		}
		else if(player == owner)
			player.sendPacket(new PetInfo(this), new PetStatusShow(this), Msg.ActionFail);
		else if(isAutoAttackable(player))
		{
			// Player with lvl < 21 can't attack a cursed weapon holder
			// And a cursed weapon holder  can't attack players with lvl < 21
			if(owner.isCursedWeaponEquipped() && player.getLevel() < 21 || player.isCursedWeaponEquipped() && owner.getLevel() < 21)
				player.sendActionFailed();
			else
				player.getAI().Attack(this, false, shift);
		}
		else if(player != owner)
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this, Config.FOLLOW_RANGE);
		else
			sendActionFailed();
	}

	public long getExpForThisLevel()
	{
		if(getLevel() >= Experience.LEVEL.length)
			return 0;
		return Experience.LEVEL[getLevel()];
	}

	public long getExpForNextLevel()
	{
		if(getLevel() + 1 >= Experience.LEVEL.length)
			return 0;
		return Experience.LEVEL[getLevel() + 1];
	}

	@Override
	public int getNpcId()
	{
		return getTemplate().npcId;
	}

	public final long getExp()
	{
		return _exp;
	}

	public final void setExp(final long exp)
	{
		_exp = exp;
	}

	public final int getSp()
	{
		return _sp;
	}

	public void setSp(final int sp)
	{
		_sp = sp;
	}

	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public void setMaxLoad(final int maxLoad)
	{
		_maxLoad = maxLoad;
	}

	@Override
	public int getBuffLimit()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return ConfigSystem.getInt("BuffLimit");
		return (int) calcStat(Stats.BUFF_LIMIT, owner.getBuffLimit(), null, null);
	}
	
	@Override
	public int getSongLimit()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return ConfigSystem.getInt("SongLimit");
		return (int) calcStat(Stats.SONG_LIMIT, owner.getSongLimit(), null, null);
	}	

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	@Override
	public void doDie(L2Character killer)
	{
		super.doDie(killer);

		L2Player owner = getPlayer();
		if(owner == null)
			return;

		if(killer == null || killer == owner || killer.getObjectId() == _objectId || isInZoneBattle() || killer.isInZoneBattle())
			return;

		if(killer instanceof L2Summon)
			killer = killer.getPlayer();

		if(killer == null)
			return;

		if(killer.isPlayer())
		{
			L2Player pk = (L2Player) killer;

			if(isInZone(ZoneType.Siege))
				return;

			if(owner.getPvpFlag() > 0 || owner.atMutualWarWith(pk))
				pk.setPvpKills(pk.getPvpKills() + 1);
			else if((getDuel() == null || getDuel() != pk.getDuel()) && getKarma() >= 0)
			{
				int pkCountMulti = Math.max(pk.getPkKills() / 2, 1);
				pk.increaseKarma(Config.KARMA_MIN_KARMA * pkCountMulti);
			}

			// Send a Server->Client UserInfo packet to attacker with its PvP Kills Counter
			pk.sendChanges();
		}
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	@Override
	public void onDecay()
	{
		deleteMe();
	}

	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();

		L2Player owner = getPlayer();
		if(owner == null)
			return;

		if(isVisible())
			owner.sendPacket(new PetStatusUpdate(this));

		L2Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowUpdate(this));
	}

	@Override
	public void deleteMe()
	{
		L2Player owner = getPlayer();
		if(owner != null)
		{
			L2Party party = owner.getParty();
			if(party != null)
				party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
			owner.sendPacket(new PetDelete(getObjectId(), 2));
			owner.setPet(null);
		}

		_ownerStoreId = 0;
		super.deleteMe();
	}

	public void unSummon()
	{
		deleteMe();
	}

	public int getAttackRange()
	{
		return _attackRange;
	}

	public void setAttackRange(int range)
	{
		if(range < 36)
			range = 36;
		_attackRange = range;
	}

	@Override
	public void setFollowStatus(boolean state, boolean changeIntention)
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;

		_follow = state;
		if(changeIntention)
			if(_follow)
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
			else
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}

	public boolean isFollow()
	{
		return _follow;
	}

	@Override
	public void updateEffectIcons()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;

		// broadcastPetInfo();

		PartySpelled ps = new PartySpelled(this, true);
		L2Party party = owner.getParty();
		if(party != null)
			party.broadcastToPartyMembers(ps);
		else
			owner.sendPacket(ps);
	}

	public int getControlItemObjId()
	{
		return 0;
	}

	public L2Weapon getActiveWeapon()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void doPickupItem(final L2Object object)
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

	public abstract void displayHitMessage(L2Character target, int damage, boolean crit, boolean miss);

	@Override
	public boolean unChargeShots(final boolean spirit)
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return false;

		if(spirit)
		{
			if(_spsCharged != 0)
			{
				_spsCharged = 0;
				owner.AutoShot();
				return true;
			}
		}
		else if(_ssCharged)
		{
			_ssCharged = false;
			owner.AutoShot();
			return true;
		}

		return false;
	}

	@Override
	public boolean getChargedSoulShot()
	{
		return _ssCharged;
	}

	@Override
	public int getChargedSpiritShot()
	{
		return _spsCharged;
	}

	public void chargeSoulShot()
	{
		_ssCharged = true;
	}

	public void chargeSpiritShot(final int state)
	{
		_spsCharged = state;
	}

	public int getSoulshotConsumeCount()
	{
		return getLevel() / 27 + 1;
	}

	public int getSpiritshotConsumeCount()
	{
		return getLevel() / 58 + 1;
	}

	@Override
	public void doAttack(final L2Character target)
	{
		super.doAttack(target);
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		for(L2CubicInstance cubic : owner.getCubics())
			if(cubic.getType() != CubicType.LIFE_CUBIC)
				cubic.doAction(target);
		if(owner.getAgathion() != null)
			owner.getAgathion().doAction(target);
	}

	@Override
	public void doCast(final L2Skill skill, final L2Character target, boolean forceUse)
	{
		super.doCast(skill, target, forceUse);
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		if(skill.isOffensive() && target != null)
		{
			for(L2CubicInstance cubic : owner.getCubics())
				if(cubic.getType() != CubicType.LIFE_CUBIC)
					cubic.doAction(target);
			if(owner.getAgathion() != null)
				owner.getAgathion().doAction(target);
		}
	}

	public boolean isPosessed()
	{
		return _posessed;
	}

	public void setPossessed(final boolean possessed)
	{
		_posessed = possessed;
	}

	public boolean isInRange()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return false;
		return getDistance(owner) < SUMMON_DISAPPEAR_RANGE;
	}

	public void teleportToOwner()
	{
		if(isDead())
			return;
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		setLoc(Location.getAroundPosition(owner, owner, 50, 150, 10));
		setReflection(owner.getReflection());
		broadcastPetInfo();
		if(_follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
		updateEffectIcons();
	}

	public void broadcastPetInfo()
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		for(L2Player player : L2World.getAroundPlayers(this))
			if(player != null)
				if(player == owner)
					player.sendPacket(new PetInfo(this, 1));
				else
					player.sendPacket(new NpcInfo(this, player, 1));
	}

	@Override
	public void startPvPFlag(L2Character target)
	{
		L2Player owner = getPlayer();
		if(owner == null)
			return;
		owner.startPvPFlag(target);
	}

	@Override
	public int getPvpFlag()
	{
		L2Player owner = getPlayer();
		return owner == null ? 0 : owner.getPvpFlag();
	}

	@Override
	public int getKarma()
	{
		L2Player owner = getPlayer();
		return owner == null ? 0 : owner.getKarma();
	}

	@Override
	public Duel getDuel()
	{
		L2Player owner = getPlayer();
		return owner == null ? null : owner.getDuel();
	}

	@Override
	public int getTeam()
	{
		L2Player owner = getPlayer();
		return owner == null ? 0 : owner.getTeam();
	}

	@Override
	public L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(_ownerStoreId);
	}

	public boolean isSiegeWeapon()
	{
		return getNpcId() == SIEGE_GOLEM_ID || getNpcId() == SIEGE_CANNON_ID || getNpcId() == SWOOP_CANNON_ID;
	}

	// Не отображаем на суммонах значки клана. 
	@Override 
	public boolean isCrestEnable() 
	{ 
		return false; 
	}

	public abstract float getExpPenalty();
}