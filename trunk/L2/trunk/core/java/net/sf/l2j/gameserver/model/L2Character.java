package net.sf.l2j.gameserver.model;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.geodata.pathfind.AbstractNodeLoc;
import net.sf.l2j.gameserver.geodata.pathfind.PathFinding;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MinionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcWalkerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.actor.status.CharStatus;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Attack;
import net.sf.l2j.gameserver.network.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.network.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.CharMoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.network.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Point3D;
import net.sf.l2j.util.Rnd;

import net.sf.l2j.gameserver.network.serverpackets.BeginRotation;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;
public abstract class L2Character extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());

	// =========================================================
	// Data Field
	private List<L2Character> _attackByList;
	// private L2Character _attackingChar;
	private L2Skill _lastSkillCast;
	private boolean _isAfraid                               = false; // Flee in a random direction
	private boolean _isRaid 								= false;
	private boolean _isConfused                             = false; // Attack anyone randomly
	private boolean _isFakeDeath                            = false; // Fake death
	private boolean _isFlying                               = false; //Is flying Wyvern?
	private boolean _isFallsdown                            = false; // Falls down [L2J_JP_ADD]
	private boolean _isMuted                                = false; // Cannot use magic
	private boolean _isPsychicalMuted                       = false; // Cannot use psychical skills
	private boolean _isKilledAlready                        = false;
	private boolean _isImobilised                           = false;
	private boolean _isOverloaded                           = false; // the char is carrying too much
	private boolean _isParalyzed                            = false;
	private boolean _isRiding                               = false; //Is Riding strider?
	private boolean _isPendingRevive                        = false;
	private boolean _isRooted                               = false; // Cannot move until root timed out
	private boolean _isRunning                              = false;
	private boolean _isSleeping                             = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean _isMeditation                           = false;
	private boolean _isStunned                              = false; // Cannot move/attack until stun timed out
	private boolean _isAttackDisable                        = false;
	private boolean _isBetrayed                             = false; // Betrayed by own summon
	protected boolean _showSummonAnimation                    = false;
	protected boolean _isTeleporting                        = false;
	private L2Character _lastBuffer							= null;
	protected boolean _isInvul                              = false;
	protected byte					_zoneValidateCounter				= 4;
	private int _lastHealAmount								= 0;
	private int[] lastPosition								= {0,0,0};
	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template;                       // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	private String _aiClass = "default";
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _isBuffBlocked                        = false; //for antibuff
	/** Table of Calculators containing all used calculator */
	private Calculator[] _calculators;

	/** FastMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, L2Skill> _skills;
    protected ChanceSkillList _chanceSkills;
	/** Zone system */
	public static final int ZONE_PVP = 1;
	public static final int ZONE_PEACE = 2;
	public static final int ZONE_SIEGE = 4;
	public static final int ZONE_MOTHERTREE = 8;
	public static final int ZONE_CLANHALL = 16;
	public static final int ZONE_UNUSED = 32;
	public static final int ZONE_NOLANDING = 64;
	public static final int ZONE_WATER = 128;
	public static final int ZONE_JAIL = 256;
	public static final int ZONE_MONSTERTRACK = 512;
	public static final int ZONE_SWAMP = 2048;
	public static final int ZONE_NOSUMMONFRIEND = 4096;
	public static final int ZONE_OLY = 8192;
	public static final int ZONE_BOSS = 16384;
	public static final int ZONE_TRADE = 32768;
	
	//can cast after attack
	private boolean _canCastAA;
	
	private int _currentZones = 0;

	public boolean isInsideZone(int zone)
	{
		return ((_currentZones & zone) != 0);
	}
	public void setInsideZone(int zone, boolean state)
	{
		if (state)
			_currentZones |= zone;
		else if (isInsideZone(zone)) // zone overlap possible
			_currentZones ^= zone;
	}

	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		getKnownList();

		_template = template;

		if (template != null && this instanceof L2NpcInstance)
		{
			_calculators = NPC_STD_CALCULATOR;

			_skills = ((L2NpcTemplate)template).getSkills();
			if (_skills != null)
			{
				for(Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
		}
		else
		{
			_skills = new FastMap<Integer,L2Skill>().setShared(true);

			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp()/352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp()-_hpUpdateInterval;
	}

	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		if(reg != null) reg.removeFromZones(this);
		decayMe();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.revalidateZone(true);
	}

	public void onTeleported()
	{
		if (!isTeleporting())
			return;
		
		spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		
		lastPosition[0]=getPosition().getX();
		lastPosition[1]=getPosition().getY();
		lastPosition[2]=getPosition().getZ();
		 
		setIsTeleporting(false);
		
			
		if (_isPendingRevive)
		{
			doRevive(false);
		}

		// Modify the position of the pet if necessary
		if(getPet() != null)
		{
			getPet().setFollowStatus(false);
			getPet().teleToLocation(getPosition().getX() + Rnd.get(-100,100), getPosition().getY() + Rnd.get(-100,100), getPosition().getZ(), false);
			((L2SummonAI)getPet().getAI()).setStartFollowController(true);
			getPet().setFollowStatus(true);
		}

	}

	// =========================================================
	// Method - Public
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 * @param player The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList (L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player)) return;
		getAttackByList().add(player);
	}

	public final void broadcastPacket(L2GameServerPacket mov)
	{
		if (!(mov instanceof CharInfo))
			sendPacket(mov);

		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			try
			{
				player.sendPacket(mov);
				if (mov instanceof CharInfo && this instanceof L2PcInstance) {
					int relation = ((L2PcInstance)this).getRelation(player);
					if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
						player.sendPacket(new RelationChanged((L2PcInstance)this, relation, player.isAutoAttackable(this)));
        		}
        	} catch (NullPointerException e) { }
        }
	}

	public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if (!(mov instanceof CharInfo))
			sendPacket(mov);

        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
        {
        	try
        	{
        		if (!isInsideRadius(player, radiusInKnownlist, false, false)) continue;
        		player.sendPacket(mov);
        		if (mov instanceof CharInfo && this instanceof L2PcInstance) {
        			int relation = ((L2PcInstance)this).getRelation(player);
        			if (getKnownList().getKnownRelations().get(player.getObjectId()) != null && getKnownList().getKnownRelations().get(player.getObjectId()) != relation)
        				player.sendPacket(new RelationChanged((L2PcInstance)this, relation, player.isAutoAttackable(this)));
        		}
        	} catch (NullPointerException e) {}
        }
	}

	/**
	 * Returns true if hp update should be done, false if not
	 * @return boolean
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();

	    if (currentHp <= 1.0 || getMaxHp() < barPixels)
	        return true;

	    if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
	    {
	    	if (currentHp == getMaxHp())
	    	{
	    		_hpUpdateIncCheck = currentHp + 1;
	    		_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
	    	}
	    	else
	    	{
	    		double doubleMulti = currentHp / _hpUpdateInterval;
		    	int intMulti = (int)doubleMulti;

	    		_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
	    		_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
	    	}

	    	return true;
	    }

	    return false;
	}

	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty()) return;

		if (!needHpUpdate(352))
			return;

		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int)getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int)getCurrentMp());

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP

		synchronized (getStatus().getStatusListener())
		{
			for (L2Character temp : getStatus().getStatusListener())
			{
				try { temp.sendPacket(su); } catch (NullPointerException e) {}
			}
		}
	}

	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR><BR>
	 *
	 */
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();
		isFalling(false,0);
		setIsTeleporting(true);
		setTarget(null);

		for(L2Character character : getKnownList().getKnownCharacters())
		{
			if(character.getTarget() == this)
			{
				character.stopMove(null, false);
				character.abortAttack();
				character.abortCast();
				character.setTarget(null);
            }
        }

        // Remove from world regions zones
        if (getWorldRegion() != null)
        {
            getWorldRegion().removeFromZones(this);
        }

		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

        if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
        {
            x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
            y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
        }

        z += 5;

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));

		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);

		decayMe();
		isFalling(false, 0);
		if (!(this instanceof L2PcInstance)
				// allow recall of the detached characters
				|| (((L2PcInstance)this).getClient() != null && ((L2PcInstance)this).getClient().isDetached()))
			onTeleported();
	}

	public void teleToLocation(int x, int y, int z) { teleToLocation(x, y, z, false); }

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();

		if (this instanceof L2PcInstance && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) // true -> ignore waiting room :)
		{
			L2PcInstance player = (L2PcInstance)this;
			player.sendMessage("You have been sent to the waiting room.");
			if(player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		teleToLocation(x, y, z, allowRandomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere) { teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true); }

	public int isFalling(boolean falling, int fallHeight)
    {

    	if (isFallsdown() && fallHeight == 0) // Avoid double checks -> let him fall only 1 time =P
    		return -1;

    	if (!falling || (lastPosition[0]==0 && lastPosition[1]==0 && lastPosition[2]==0))
    	{
    		lastPosition = new int[] {getClientX(),getClientY(),getClientZ()};
    		setIsFallsdown(false);
    		return -1;
    	}

    	int moveChangeX = Math.abs(lastPosition[0] - getClientX()),
			moveChangeY = Math.abs(lastPosition[1] - getClientY()),
			moveChangeZ = Math.max(lastPosition[2] - getClientZ(),lastPosition[2] - getZ());

    	if (moveChangeZ > fallSafeHeight() && moveChangeY < moveChangeZ && moveChangeX < moveChangeZ && !isFlying())
    	{

    		setIsFallsdown(true);
    		fallHeight += moveChangeZ;

    		lastPosition = new int[] {getClientX(),getClientY(),getClientZ()};
    		getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

    		CheckFalling cf = new CheckFalling(fallHeight);
    		cf.setTask(ThreadPoolManager.getInstance().scheduleGeneral(cf, Math.min(1200, moveChangeZ)));

    		return fallHeight;
    	}
		lastPosition = new int[] {getClientX(),getClientY(),getClientZ()};
		getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

		if (fallHeight > fallSafeHeight())
		{
			doFallDamage(fallHeight);
			return fallHeight;
		}

    	return -1;
    }
    
    private int fallSafeHeight()
    {
    	
    	int safeFallHeight = Config.ALT_MINIMUM_FALL_HEIGHT;  
    	
    	try
    	{
    		if (this instanceof L2PcInstance)
    		{
    			safeFallHeight = ((L2PcInstance)this).getTemplate().getBaseFallSafeHeight(((L2PcInstance)this).getAppearance().getSex());
    		}
    	}
    	
    	catch(Throwable t)
    	{
    		_log.log(Level.SEVERE, "Template Missing : ", t);
    	}
    	
    	return  safeFallHeight;
    }
    
    
    private int getFallDamage(int fallHeight)
    {
    	int damage = (fallHeight-fallSafeHeight())*2;
    	damage = (int) (damage / getStat().calcStat(Stats.FALL_VULN, 1, this, null));
		if (damage >= getStatus().getCurrentHp())
		{
			damage = (int)(getStatus().getCurrentHp()-1);
		}
	
		//broadcastPacket(new ChangeWaitType(this,ChangeWaitType.WT_START_FAKEDEATH)); rev. 3.1.2
		disableAllSkills();
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
					{
						L2Character.this.enableAllSkills();
						//broadcastPacket(new ChangeWaitType(L2Character.this,ChangeWaitType.WT_STOP_FAKEDEATH)); rev. 3.1.2
						setIsFallsdown(false);
						
						lastPosition = new int[] {getX(),getY(),getZ()};
						setClientX(lastPosition[0]);
						setClientY(lastPosition[1]);
						setClientZ(lastPosition[2]);
					}
			}
			, 250); //rev. 3.1.2 (1100 retail)
		
		return damage;
    }

    private void doFallDamage(int fallHeight)
    {
    	isFalling(false,0);

    	if (this instanceof L2PcInstance)
		{
			L2PcInstance player = ((L2PcInstance)this);
			
			if (player.isInvul() || player.isInFunEvent())
			{
				setIsFallsdown(false);
				return;
			}
		}
    	
    	int damage = getFallDamage(fallHeight);
		
		if (damage < 1)
			return;
    	
    	if (this instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FALL_DAMAGE_S1);
				sm.addNumber(damage);
				sendPacket(sm);
			}
			
		getStatus().reduceHp(damage, this);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
    }
    
    public class CheckFalling implements Runnable
    {
        private int _fallHeight;
        @SuppressWarnings("rawtypes")
		private Future _task;

        public CheckFalling(int fallHeight)
        {
            _fallHeight = fallHeight;
        }

        public void setTask(@SuppressWarnings("rawtypes") Future task)
        {
            _task = task;
        }

        public void run()
        {
            if (_task != null)
            {
                _task.cancel(true);
                _task = null;
            }
            
            try
            {
                isFalling(true , _fallHeight);
            }
            catch (Throwable e)
            {
                _log.log(Level.SEVERE, "L2PcInstance.CheckFalling exception ", e);
            }
        }
    }

	protected void doAttack(L2Character target)
	{
		
		if (isAlikeDead() || target == null || (this instanceof L2NpcInstance && target.isAlikeDead())
                || (this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath())
                || !getKnownList().knowsObject(target)
                || (this instanceof L2PcInstance && isDead())
                || (target instanceof L2PcInstance && ((L2PcInstance)target).getDuelState() == Duel.DUELSTATE_DEAD)
			    || Formulas.getInstance().canCancelAttackerTarget(this, target))
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

			sendPacket(new ActionFailed());
			return;
		}

		if (isAttackingDisabled())
            return;
		
		
		if (this instanceof L2PcInstance)
		{
	        if (((L2PcInstance)this).inObserverMode())
	        {
	            sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
	            sendPacket(new ActionFailed());
	            return;
	        }

	        if (target instanceof L2PcInstance)
	        {
				if (target.isInsidePeaceZone((L2PcInstance)this))
				{
					sendPacket(new ActionFailed());
					return;
				}

				if (target.getObjectId() == this.getObjectId())
				{
					sendPacket(new ActionFailed());
					return;
				}
			
		        if (((L2PcInstance)target).isCursedWeaponEquiped() && ((L2PcInstance)this).getLevel()<=20){
		        	((L2PcInstance)this).sendMessage("Can't attack a cursed player when under level 21.");
		        	sendPacket(new ActionFailed());
		        	return;
		        }

		        if (((L2PcInstance)this).isCursedWeaponEquiped() && ((L2PcInstance)target).getLevel()<=20){
		        	((L2PcInstance)this).sendMessage("Can't attack a newbie player using a cursed weapon.");
		        	sendPacket(new ActionFailed());
		        	return;
		        }
	        }
		}

		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();

		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
		{
			//	You can't make an attack with a fishing pole.
			((L2PcInstance)this).sendPacket(new SystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}

		if (!(target instanceof L2DoorInstance) && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(new ActionFailed());
			return;
		}

		// Check for a bow
		if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.BOW))
		{
			//Check for arrows and MP
			if (this instanceof L2PcInstance)
			{
				// Checking if target has moved to peace zone - only for player-bow attacks at the moment
				// Other melee is checked in movement code and for offensive spells a check is done every time
				if (target.isInsidePeaceZone((L2PcInstance)this))
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(new ActionFailed());
					return;
				}

				// Verify if the bow can be use
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
				    // Verify if L2PcInstance owns enough MP
					int saMpConsume = (int)getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;

				    if (getCurrentMp() < mpConsume)
				    {
				        // If L2PcInstance doesn't have enough MP, stop the attack

				        ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);

				        sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
				        sendPacket(new ActionFailed());
				        return;
				    }
				    // If L2PcInstance have enough MP, the bow consummes it
				    getStatus().reduceMp(mpConsume);

					// Set the period of bow non re-use
					_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);

					sendPacket(new ActionFailed());
					return;
				}

				// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
				if (!checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no arrow
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

					sendPacket(new ActionFailed());
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if (this instanceof L2NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
				    return;
			}
		}

		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);

		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
            setCurrentCp(getCurrentCp() - 10);

        // Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof L2PcInstance)
            ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
        else if (this instanceof L2Summon)
            ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);

        // Verify if soulshots are charged.
        boolean wasSSCharged;

		if (this instanceof L2NpcInstance)
        	wasSSCharged = ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
        else if (this instanceof L2Summon && !(this instanceof L2PetInstance))
            wasSSCharged = (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
        else
            wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		int timeToHit = timeAtk/2;
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (timeAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;

        int ssGrade = 0;

        if (weaponItem != null)
        {
        	ssGrade = weaponItem.getCrystalType();
        	if (ssGrade == 6)
        		ssGrade = 5;
        }

        // Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged, ssGrade);

		boolean hitted;

		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		setHeading(Util.calculateHeadingFrom(this, target));
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);

		// Select the type of attack to start
		if (weaponItem == null)
			hitted = doAttackHitSimple(attack, target, timeToHit);
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
			hitted = doAttackHitByPole(attack, target, timeToHit);
		else if (isUsingDualWeapon())
			hitted = doAttackHitByDual(attack, target, timeToHit);
		else
			hitted = doAttackHitSimple(attack, target, timeToHit);

        // Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = null;

        if (this instanceof L2PcInstance)
            player = (L2PcInstance)this;
        else if (this instanceof L2Summon)
            player = ((L2Summon)this).getOwner();

        if (player != null)
            player.updatePvPStatus(target);

		// Check if hit isn't missed
		if (!hitted)
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
        else
        {
            /* ADDED BY nexus - 2006-08-17
             *
             * As soon as we know that our hit landed, we must discharge any active soulshots.
             * This must be done so to avoid unwanted soulshot consumption.
             */

            // If we didn't miss the hit, discharge the shoulshots, if any
            if (this instanceof L2Summon && !(this instanceof L2PetInstance))
                ((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
            else
                if (weaponInst != null)
                    weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);


        	if (player != null)
        	{
        		if (player.isCursedWeaponEquiped())
        		{
                	// If hitted by a cursed weapon, Cp is reduced to 0
        			if (!target.isInvul())
        				target.setCurrentCp(0);
        		} else if (player.isHero())
        		{
        			if (target instanceof L2PcInstance && ((L2PcInstance)target).isCursedWeaponEquiped())
                    	// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
                		target.setCurrentCp(0);
        		}
        	}
        }

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits())
			broadcastPacket(attack);

		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk+reuse);
	}

	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

		if (!Config.NOT_CONSUME_ARROWS)
		{
		// Consumme arrows
		reduceArrowCount();
		}
		_move = null;

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}

		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));

			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk+reuse);
			sendPacket(sg);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk+reuse)/GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;
	}

	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;

		// Calculate if hits are missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);

		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.getInstance().calcShldUse(this, target);

			// Calculate if hit 1 is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

			// Calculate physical damages of hit 1
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}

		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.getInstance().calcShldUse(this, target);

			// Calculate if hit 2 is critical
			crit2 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

			// Calculate physical damages of hit 2
			damage2 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}

		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk/2);

		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);

		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);

		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}

	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int)getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		angleChar = Util.convertHeadingToDegree(getHeading());
		int attackRandomCountMax = (int)getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3, null, null) - 1;
		int attackcount = 0;

		if (angleChar <= 0)
            angleChar += 360;
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj == target) continue;
			if(obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance &&
						this instanceof L2PcInstance &&
						((L2PetInstance)obj).getOwner() == ((L2PcInstance)this)) continue;

				if (!Util.checkIfInRange(maxRadius, this, obj, false)) continue;
                if(Math.abs(obj.getZ() - getZ()) > 650) continue;
				if (!isFacing(obj, maxAngleDiff)) continue;

				temp = (L2Character) obj;
				if(!temp.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackRandomCountMax)
					{
						if (temp == getAI().getAttackTarget() || temp.isAutoAttackable(this))
						{

							hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
							attackpercent /= 1.15;
						}
					}
				}
			}
		}
		return hitted;
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);

			if (attackpercent != 100)
				damage1 = (int)(damage1*attackpercent/100);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;
	}

	public void doCast(L2Skill skill)
	{
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if (skill.getId() == 3260 || skill.getId() == 3261 || skill.getId() == 3262)
		{
			L2Weapon weapon = getActiveWeaponItem();
			if (weapon.getItemId() != 9140 && weapon.getItemId() != 9141)
			{
				return;
			}
		}

		if (isSkillDisabled(skill.getId()))
		{
			if (this instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(),skill.getLevel());
				sendPacket(sm);
			}
			return;
		}

		if (skill.isMagic() && isMuted() && !skill.isPotion())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
        if (!skill.isMagic() && isPsychicalMuted() && !skill.isPotion())
        {
            getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
            return;
        }
        
        if (isPsychicalMuted() && !skill.isMagic() && !skill.isPotion())
        {
        	sendPacket(new ActionFailed());
        	return;
        }

        if (this instanceof L2PcInstance && ((L2PcInstance)this).isInOlympiadMode() &&
        		(skill.isHeroSkill() || skill.getSkillType() == SkillType.RESURRECT))
        {
        	SystemMessage sm = new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        	sendPacket(sm);
        	return;
        }

        
        if (skill.getSkillType() == L2Skill.SkillType.CHARGE)
        {
          EffectCharge effect = (EffectCharge)getFirstEffect(skill);
          if ((effect != null) && (effect.getLevel() >= skill.getNumCharges()))
          {
            if (isPlayer())
              sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXIMUM));
            getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
            return;
          }

        }
        
        if (skill.getSkillType() == SkillType.SIGNET || skill.getSkillType() == SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null) return;
			boolean canCast = true;
			if (skill.getTargetType() == SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
					canCast = false;
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
				canCast = false;
			if (!canCast)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
				return;
			}
		}

		//Recharge AutoSoulShot
			if (skill.useSoulShot())
			{
				if (this instanceof L2NpcInstance)
					((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
				else if (this instanceof L2PcInstance)
					((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
				else if (this instanceof L2Summon)
					((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
			}
			else if (skill.useSpiritShot())
			{
				if (this instanceof L2PcInstance)
					((L2PcInstance)this).rechargeAutoSoulShot(false, true, false);
				else if (this instanceof L2Summon)
					((L2Summon)this).getOwner().rechargeAutoSoulShot(false, true, true);
			}

        L2Character target = null;
        L2Object[] targets = skill.getTargetList(this);

		if (skill.getTargetType() == SkillTargetType.TARGET_AURA || skill.getTargetType() == SkillTargetType.TARGET_GROUND)
		{
			target = this;
		}
		else 
        {
			if (targets == null || targets.length == 0)  
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			
			if(     skill.getSkillType() == SkillType.BUFF ||
					skill.getSkillType() == SkillType.HEAL ||
					skill.getSkillType() == SkillType.COMBATPOINTHEAL ||
					skill.getSkillType() == SkillType.MANAHEAL ||
					skill.getSkillType() == SkillType.REFLECT ||
					skill.getSkillType() == SkillType.SEED ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN ||
					skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY)
			{
				target = (L2Character) targets[0];

				if (this instanceof L2PcInstance && target instanceof L2PcInstance && target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				{
					if(skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.HOT || skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT || skill.getSkillType() == SkillType.BALANCE_LIFE)
						target.setLastBuffer(this);

					if (((L2PcInstance)this).isInParty() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
					{
						for (L2PcInstance member : ((L2PcInstance)this).getParty().getPartyMembers())
							 member.setLastBuffer(this);
					}
				}
			} 
			else target = (L2Character) getTarget();
        }

		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		
        setLastSkillCast(skill);
		int magicId = skill.getId();
		int displayId = skill.getDisplayId();
		int level = skill.getLevel();

		if (level < 1)
            level = 1;

		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();

		boolean forcebuff = skill.getSkillType() == SkillType.FORCE_BUFF
			|| skill.getSkillType() == SkillType.SIGNET_CASTTIME;

		if(!forcebuff)
		{
			hitTime = Formulas.getInstance().calcMAtkSpd(this, skill, hitTime);
			if (coolTime > 0) 
				coolTime = Formulas.getInstance().calcMAtkSpd(this, skill, coolTime);
		}
			
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
        if (skill.getId() == 1157 || skill.getId() == 1013 || skill.getId() == 1335 || skill.getId () == 1311)
        {
        if (weaponInst != null)
        {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT || weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                hitTime = (int)(0.70 * hitTime);
		coolTime = (int)(0.70 * coolTime);
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
        }
        }
		
		
		if (weaponInst != null && skill.isMagic() && !forcebuff && skill.getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if ((weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					|| (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
			{
				hitTime = (int)(0.70 * hitTime);
				coolTime = (int)(0.70 * coolTime);
				switch (skill.getSkillType())
				{
					case BUFF:
					case MANAHEAL:
					case RESURRECT:
					case RECALL:
					case DOT:
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						break;
				}
			}
		}
		else if (this instanceof L2NpcInstance && skill.useSpiritShot())
        {
    		if(((L2NpcInstance)this).rechargeAutoSoulShot(false, true))
    		{
    			hitTime = (int)(0.70 * hitTime);
    			coolTime = (int)(0.70 * coolTime);
    		}
        }
		
		if (skill.isStaticHitTime())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		
		_castEndTime = 10 + GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = -2 + GameTimeController.getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
		
		int reuseDelay;
		
		if (skill.isStaticReuse())
	    {
	      reuseDelay = skill.getReuseDelay();
	    }
	    else
	    {
	      if (skill.isMagic())
	      {
	        reuseDelay = (int)(skill.getReuseDelay() * getStat().getMReuseRate(skill));
	      }
	      else
	      {
	        reuseDelay = (int)(skill.getReuseDelay() * getStat().getPReuseRate(skill));
	      }
	      reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
	    }

	    if (reuseDelay > 30000) {
	      addTimeStamp(skill.getId(), reuseDelay);
	    }
		
		
		boolean skillMastery = Formulas.getInstance().calcSkillMastery(this, skill);
		
		if (reuseDelay > 30000 && !skillMastery) addTimeStamp(skill.getId(),reuseDelay);

		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			if (skill.isDance())
			{
				getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, initmpcons, null, null));
			}
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}

		if (reuseDelay > 10 && !skillMastery)
		{
			disableSkill(skill.getId(), reuseDelay);
		}
		
		if (skillMastery) // only possible for L2PcInstance
		{	
			reuseDelay = 0;
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addSkillName(skill.getId(),skill.getLevel());
			sendPacket(sm);

		}

		setHeading(Util.calculateHeadingFrom(this, target));
		
		if(forcebuff)
		{
			if (skill.getItemConsume() > 0)
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
            
			if (skill.getSkillType() == SkillType.FORCE_BUFF)
				startForceBuff(target, skill);
			else
				callSkill(skill, targets);
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		broadcastPacket(new MagicSkillUser(this, target, displayId, level, hitTime, reuseDelay));

		if (this instanceof L2PcInstance && magicId != 1312)
        {
			SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
			sm.addSkillName(magicId,skill.getLevel());
			sendPacket(sm);
		}

		if (hitTime > 210)
		{
			if (this instanceof L2PcInstance && !forcebuff)
			{
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, hitTime);
				sendPacket(sg);
			}

			disableAllSkills();

			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}

			if (forcebuff)
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), hitTime);
			else
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1), hitTime-200);
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true);
		}
		final L2Character _character = this;
		final L2Object[] _targets = targets;
		final L2Skill _skill = skill;
		if (this instanceof L2NpcInstance){
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
				public void run(){
					try{
    					if (((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null){
    						L2PcInstance player = null;
    						if (_targets[0] instanceof L2PcInstance)
    							player = (L2PcInstance)_targets[0];
    						else if (_targets[0] instanceof L2Summon)
    							player = ((L2Summon)_targets[0]).getOwner();
    						for (Quest quest: ((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
    						{
    							quest.notifySpellFinished(((L2NpcInstance)_character), player, _skill);
    						}
    					}
					}catch (Throwable e){}}
			},hitTime+coolTime+1000);}
		stopMove(null);
	}

	public void addTimeStamp(int s, int r) {/***/}

	public void removeTimeStamp(int s) {/***/}

	public void startForceBuff(L2Character caster, L2Skill skill) {/***/}

	public boolean doDie(L2Character killer)
	{
        synchronized (this)
        {
            if (isKilledAlready()) return false;
            setIsKilledAlready(true);
        }
		setTarget(null);
		stopMove(null);

		getStatus().stopHpMpRegeneration();
        	if (this instanceof L2PlayableInstance && ((L2PlayableInstance)this).isPhoenixBlessed())
        	{
           		if (((L2PlayableInstance)this).getCharmOfLuck())
              			((L2PlayableInstance)this).stopCharmOfLuck(null);
           		if (((L2PlayableInstance)this).isNoblesseBlessed())
               			((L2PlayableInstance)this).stopNoblesseBlessing(null);
        	}
        	else if (this instanceof L2PlayableInstance && ((L2PlayableInstance)this).isNoblesseBlessed())
        	{ 
			((L2PlayableInstance)this).stopNoblesseBlessing(null); 
			if (((L2PlayableInstance)this).getCharmOfLuck())
				((L2PlayableInstance)this).stopCharmOfLuck(null);
		}
		else
		{
			if(Config.REMOVE_BUFFS_AFTER_DEATH) 
				stopAllEffects();
		}
		if (!(this instanceof L2GuardInstance || 
			this instanceof L2SiegeGuardInstance || 
			(this instanceof L2MinionInstance && ((L2MinionInstance)this).getLeader().isRaid())))
			calculateRewards(killer);
		
		broadcastStatusUpdate();
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		
		if (getWorldRegion() != null)
				getWorldRegion().onDeath(this);
		for (QuestState qs: getNotifyQuestOfDeath())
		{
			qs.getQuest().notifyDeath( (killer==null?this:killer) , this, qs);
		}
		getNotifyQuestOfDeath().clear();
		if (this instanceof L2PlayableInstance && ((L2PlayableInstance)this).isPhoenixBlessed())
		{
			if (this instanceof L2Summon)
			{
				((L2Summon)this).getOwner().reviveRequest(((L2Summon)this).getOwner(), null, true);
			}
			else
				((L2PcInstance)this).reviveRequest(((L2PcInstance)this),null,false);
		}
		getAttackByList().clear();
		return true;
	}

	protected void calculateRewards(L2Character killer)
	{
	}

	public void doRevive(boolean broadcastPacketRevive)
	{
		if (!isDead()) 
		{
			return;
		}
		if (!isTeleporting())
		{
			setIsPendingRevive(false);

			if (this instanceof L2PlayableInstance && ((L2PlayableInstance)this).isPhoenixBlessed())
			{
				((L2PlayableInstance)this).stopPhoenixBlessing(null);
		        setCurrentCp(getMaxCp());
		        setCurrentHp(getMaxHp());
		        setCurrentMp(getMaxMp());
			}
		      else
		      {
		        if (Config.RESPAWN_RESTORE_CP < 0D) 
				{
		          _status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
		        }

		        _status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);

		        if (Config.RESPAWN_RESTORE_MP < 0D)
		          _status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);

		      }
		}
	    if (broadcastPacketRevive) 
		{
	      broadcastPacket(new Revive(this));
	    }

	    if (getWorldRegion() != null)
	      getWorldRegion().onRevive(this);
	    else
	      setIsPendingRevive(true);

	    //fireEvent(BaseExtender.EventType.REVIVE.name, (Object[])null);
	}
	

	public void doRevive(double revivePower) 
	{ 
		doRevive(true); 
	}

	protected void useMagic(L2Skill skill)
	{
		if (skill == null || isDead())
			return;

		if (isAllSkillsDisabled())
		{
			return;
		}

		if (skill.isPassive())
			return;

		L2Object target = null;

		switch (skill.getTargetType())
		{
			case TARGET_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			default:

			    target = skill.getFirstOfTargetList(this);
			    break;
		}

        if(skill.isOffensive() && (target instanceof L2Character) && Formulas.getInstance().canCancelAttackerTarget(this, (L2Character)target))
        {
            getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
            return;
        } else
        {
            getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
            return;
        }
	}


	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null) _ai = new L2CharacterAI(new AIAccessor());
			}
		}

		return _ai;
	}


	public void setAI(L2CharacterAI newAI)
	{
        L2CharacterAI oldAI = getAI();
        if(oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
            ((L2AttackableAI)oldAI).stopAITask();
        _ai = newAI;
    }

	public boolean hasAI() { return _ai != null; }

	public boolean isRaid()
	{
		return _isRaid  ;
	}

	public void setIsRaid(boolean isRaid)
	{
	    	_isRaid = isRaid;
	}


	public final List<L2Character> getAttackByList ()
	{
		if (_attackByList == null) _attackByList = new FastList<L2Character>();
		return _attackByList;
	}

	public final L2Skill getLastSkillCast() { return _lastSkillCast; }
	public void setLastSkillCast (L2Skill skill) { _lastSkillCast = skill; }

	public final boolean isAfraid() { return _isAfraid; }
	public final void setIsAfraid(boolean value) { _isAfraid = value; }

	/** Return True if the L2Character is dead or use fake death.  */
	public final boolean isAlikeDead() { return isFakeDeath() || !(getCurrentHp() > 0.5); }

	/** Return True if the L2Character can't use its skills (ex : stun, sleep...). */
	public final boolean isAllSkillsDisabled() { return _allSkillsDisabled || isStunned() || isSleeping() || isMeditation() || isParalyzed(); }

	public final boolean isPotionsDisabled() { return isStunned() || isSleeping() || isMeditation() || isParalyzed(); }
	/** Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse). */
	public boolean isAttackingDisabled() { return isStunned() || isAttackDisable() || isMeditation() || isSleeping() || _attackEndTime > GameTimeController.getGameTicks() || isFakeDeath() || isParalyzed() || isFallsdown(); }

	public final Calculator[] getCalculators() { return _calculators; }

	public final boolean isConfused() { return _isConfused; }
	public final void setIsConfused(boolean value) { _isConfused = value; }

	/** Return True if the L2Character is dead. */
	public final boolean isDead() { return !(isFakeDeath()) && !(getCurrentHp() > 0.5); }

	public final boolean isFakeDeath() { return _isFakeDeath; }
	public final void setIsFakeDeath(boolean value) { _isFakeDeath = value; }

	public final boolean isFallsdown() { return _isFallsdown; }
	public final void setIsFallsdown(boolean value) { _isFallsdown = value; }
	/** Return True if the L2Character is flying. */
	public final boolean isFlying() { return _isFlying; }
	/** Set the L2Character flying mode to True. */
	public final void setIsFlying(boolean mode) { _isFlying = mode; }

	public boolean isImobilised() { return _isImobilised; }
	public void setIsImobilised(boolean value){ _isImobilised = value; }

	public final boolean isKilledAlready() { return _isKilledAlready; }
	public final void setIsKilledAlready(boolean value) { _isKilledAlready = value; }

	public final boolean isMuted() { return _isMuted; }
	public final void setIsMuted(boolean value) { _isMuted = value; }

	public final boolean isPsychicalMuted() { return _isPsychicalMuted; }
    public final void setIsPsychicalMuted(boolean value) { _isPsychicalMuted = value; }

	/** Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed). */
	public boolean isMovementDisabled() { return isStunned() || isRooted() || isSleeping() || isMeditation() || isOverloaded() || isParalyzed() || isImobilised() || isFakeDeath() || isFallsdown(); }

	/** Return True if the L2Character can be controlled by the player (confused, afraid). */
	public final boolean isOutOfControl() { return isConfused() || isAfraid(); }

	public final boolean isOverloaded() { return _isOverloaded; }
	/** Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item). */
	public final void setIsOverloaded(boolean value) { _isOverloaded = value; }

	public final boolean isParalyzed() { return _isParalyzed; }
	public final void setIsParalyzed(boolean value) { _isParalyzed = value; }

	public final boolean isPendingRevive() 
	{ 
		return isDead() && _isPendingRevive; 
	
	}
	
	public final void setIsPendingRevive(boolean value) 
	{ 
		_isPendingRevive = value; 
	}

	/**
	 * Return the L2Summon of the L2Character.<BR><BR>
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public L2Summon getPet() { return null; }

	/** Return True if the L2Character is ridding. */
	public final boolean isRiding() { return _isRiding; }
	/** Set the L2Character riding mode to True. */
	public final void setIsRiding(boolean mode) { _isRiding = mode; }

	public final boolean isRooted() { return _isRooted; }
	public final void setIsRooted(boolean value) { _isRooted = value; }

	/** Return True if the L2Character is running. */
	public final boolean isRunning() { return _isRunning; }
	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		broadcastPacket(new ChangeMoveType(this));
	}
	
	
	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning() { if (!isRunning()) setIsRunning(true); }

	public final boolean isSleeping() { return _isSleeping; }
	public final void setIsSleeping(boolean value) { _isSleeping = value; }

    public final boolean isMeditation() { return _isMeditation; }
	public final void setIsMeditation(boolean value) { _isMeditation = value; }

	public final boolean isStunned() { return _isStunned; }
	public final void setIsStunned(boolean value) { _isStunned = value; }

	public final boolean isAttackDisable() { return _isAttackDisable; }
	public final void setIsAttackDisable(boolean value) { _isAttackDisable = value; }
	
	public final boolean isBetrayed() { return _isBetrayed; }
	public final void setIsBetrayed(boolean value) { _isBetrayed = value; }

	public final boolean isTeleporting() { return _isTeleporting; }
	public final void setIsTeleporting(boolean value) { _isTeleporting = value; }
	public void setIsInvul(boolean b){_isInvul = b;}
	public boolean isInvul(){return _isInvul  || _isTeleporting;}
	public boolean isUndead() { return _template.isUndead; }

	@Override
	public CharKnownList getKnownList()
	{
		if(super.getKnownList() == null || !(super.getKnownList() instanceof CharKnownList))
			setKnownList(new CharKnownList(this));
		return ((CharKnownList)super.getKnownList());
	}

	public CharStat getStat()
	{
		if (_stat == null) _stat = new CharStat(this);
		return _stat;
	}
	public final void setStat(CharStat value) { _stat = value; }

	public CharStatus getStatus()
	{
		if (_status == null) _status = new CharStatus(this);
		return _status;
	}
	public final void setStatus(CharStatus value) { _status = value; }

	public L2CharTemplate getTemplate() { return _template; }
	/**
	 * Set the template of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2Character</li><BR><BR
	 */
	protected final void setTemplate(L2CharTemplate template) { _template = template; }

	/** Return the Title of the L2Character. */
	public final String getTitle() { return _title; }
	/** Set the Title of the L2Character. */
	public final void setTitle(String value) { _title = value; }

	/** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setWalking() { if (isRunning()) setIsRunning(false); }

	/** Task lauching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int _skillId;

		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}

		public void run()
		{
			try
			{
				enableSkill(_skillId);
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Throwable e)
			{
				_log.warning(e.toString());
			}
		}
	}

	/** Task lauching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		int _coolTime;
		int _phase;

		public MagicUseTask(L2Object[] targets, L2Skill skill, int coolTime, int phase)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
		}

		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
						break;
					case 2:
						onMagicHitTimer(_targets, _skill, _coolTime, false);
						break;
					case 3:
						onMagicFinalizer(_targets, _skill);
						break;
					default:
						break;
				}
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
				enableAllSkills();
			}
		}
	}

    /** Task lauching the function useMagic() */
    class QueuedMagicUseTask implements Runnable
    {
        L2PcInstance _currPlayer;
        L2Skill _queuedSkill;
        boolean _isCtrlPressed;
        boolean _isShiftPressed;

        public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
        {
            _currPlayer = currPlayer;
            _queuedSkill = queuedSkill;
            _isCtrlPressed = isCtrlPressed;
            _isShiftPressed = isShiftPressed;
        }

        public void run()
        {
            try
            {
                _currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
            }
            catch (Throwable e)
            {
                _log.log(Level.SEVERE, "", e);
            }
        }
    }

	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "", t);
			}
		}
	}

	/** Task lauching the function stopPvPFlag() */
	class PvPFlag implements Runnable
	{
		public PvPFlag()
		{

		}

		public void run()
		{
			try
			{
				// _log.fine("Checking pvp time: " + getlastPvpAttack());
				// "lastattack: " _lastAttackTime "currenttime: "
				// System.currentTimeMillis());
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					//  _log.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > (getPvpFlagLasts() - 5000))
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					//checkPvPFlag();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "error in pvp flag task:", e);
			}
		}
	}
	// =========================================================











	// ========================================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	private int _AbnormalEffects;
	private FastTable<L2Effect> _effects;
	protected Map<String, List<L2Effect>> _stackedEffects;

	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];

	public static final int ABNORMAL_EFFECT_BLEEDING		= 0x000001;
	public static final int ABNORMAL_EFFECT_POISON 			= 0x000002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3		= 0x000004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4		= 0x000008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5		= 0x000010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6		= 0x000020;
	public static final int ABNORMAL_EFFECT_STUN			= 0x000040;
	public static final int ABNORMAL_EFFECT_SLEEP			= 0x000080;
	public static final int ABNORMAL_EFFECT_MUTED			= 0x000100;
	public static final int ABNORMAL_EFFECT_ROOT			= 0x000200;
	public static final int ABNORMAL_EFFECT_HOLD_1			= 0x000400;
	public static final int ABNORMAL_EFFECT_HOLD_2			= 0x000800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13		= 0x001000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD		= 0x002000;
	public static final int ABNORMAL_EFFECT_FLAME			= 0x004000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16		= 0x008000;
	public static final int ABNORMAL_EFFECT_GROW			= 0x010000;
	public static final int ABNORMAL_EFFECT_FLOATING_ROOT	= 0x020000;
	public static final int ABNORMAL_EFFECT_DANCE_STUNNED	= 0x040000;
	public static final int ABNORMAL_EFFECT_FIREROOT_STUN	= 0x080000;
	public static final int ABNORMAL_EFFECT_STEALTH			= 0x100000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_1	= 0x200000;
	public static final int ABNORMAL_EFFECT_IMPRISIONING_2	= 0x400000;
	public static final int ABNORMAL_EFFECT_MAGIC_CIRCLE	= 0x800000;
	public static final int ABNORMAL_EFFECT_CONFUSED   = 0x0020;
	public static final int ABNORMAL_EFFECT_AFRAID     = 0x0010;

	public final void addEffect(L2Effect newEffect)
	{
		if(newEffect == null) return;

		
		synchronized (this)
		{
			if (_effects == null)
				_effects = new FastTable<L2Effect>();

			if (_stackedEffects == null)
				_stackedEffects = new FastMap<String, List<L2Effect>>();
		}
		synchronized(_effects)
		{
			L2Effect tempEffect, tempEffect2;
			for (int i=0; i<_effects.size(); i++)
			{
				if (_effects.get(i).getSkill().getId() == newEffect.getSkill().getId()
						&& _effects.get(i).getEffectType() == newEffect.getEffectType()
						&& _effects.get(i).getStackOrder() == newEffect.getStackOrder())
				{
					if (newEffect.getSkill().getSkillType() == L2Skill.SkillType.BUFF
							|| newEffect.getEffectType() == L2Effect.EffectType.BUFF)
					{
						_effects.get(i).exit();
					}
					else
					{
						newEffect.stopEffectTask();
						return;
					}
				}
			}
			L2Skill tempskill = newEffect.getSkill();
			int buffcountmax = -1;
			buffcountmax += Config.BUFFS_MAX_AMOUNT;
			if (getBuffCount() > buffcountmax && !doesStack(tempskill) && ((
				tempskill.getSkillType() == L2Skill.SkillType.BUFF ||
                tempskill.getSkillType() == L2Skill.SkillType.REFLECT ||
                tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                tempskill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)&&
                !(tempskill.getId() > 4360  && tempskill.getId() < 4367) &&
				!(tempskill.getId() > 4550  && tempskill.getId() < 4555))
        	) 
			{
				if (newEffect.isHerbEffect()) 
				{ 
					newEffect.stopEffectTask(); 
					return; 
				}
				removeFirstBuff(tempskill.getId());
			}
			
			if ((getDeBuffCount() >= Config.DEBUFFS_MAX_AMOUNT) && (!doesStack(tempskill)) && (tempskill.getSkillType() == L2Skill.SkillType.DEBUFF))
		      {
		        removeFirstDeBuff(tempskill.getId());
		      }
			
			if (!newEffect.getSkill().isToggle())
			{
				int pos=0;
            	for (int i=0; i<_effects.size(); i++)
            	{
            		if (_effects.get(i) != null)
            		{
            			int skillid = _effects.get(i).getSkill().getId();
            			if (!_effects.get(i).getSkill().isToggle() &&
            				(!(skillid > 4360  && skillid < 4367))
            				) pos++;
            		}
            		else break;
            	}
            	_effects.add(pos, newEffect);
			}
			else _effects.addLast(newEffect);
			if (newEffect.getStackType().equals("none"))
			{
				newEffect.setInUse(true);
				addStatFuncs(newEffect.getStatFuncs());
				updateEffectIcons();
				return;
			}
			List<L2Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());

			if (stackQueue == null)
				stackQueue = new FastList<L2Effect>();

			tempEffect = null;
			if (stackQueue.size() > 0)
			{
				for (int i=0; i<_effects.size(); i++)
				{
					if (_effects.get(i) == stackQueue.get(0))
					{
						tempEffect = _effects.get(i);
						break;
					}
				}
			}
			stackQueue = effectQueueInsert(newEffect, stackQueue);

			if (stackQueue == null) return;
			_stackedEffects.put(newEffect.getStackType(), stackQueue);
			tempEffect2 = null;
			for (int i=0; i<_effects.size(); i++)
			{
				if (_effects.get(i) == stackQueue.get(0))
				{
					tempEffect2 = _effects.get(i);
					break;
				}
			}
			if (tempEffect != tempEffect2)
			{
				if (tempEffect != null)
				{
					removeStatsOwner(tempEffect);
					tempEffect.setInUse(false);
				}
				if (tempEffect2 != null)
				{
					tempEffect2.setInUse(true);
					addStatFuncs(tempEffect2.getStatFuncs());
				}
			}
		}
		updateEffectIcons();
	}

	private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
	{
		if(_effects == null)
            return null;
		Iterator<L2Effect> queueIterator = stackQueue.iterator();

		int i = 0;
		while (queueIterator.hasNext()) {
            L2Effect cur = queueIterator.next();
            if (newStackedEffect.getStackOrder() < cur.getStackOrder())
            	i++;
            else break;
        }

		stackQueue.add(i, newStackedEffect);

		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && stackQueue.size() > 1)
		{
			for (int n=0; n<_effects.size(); n++)
			{
				if (_effects.get(n) == stackQueue.get(1))
				{
					_effects.remove(n);
					break;
				}
			}
			stackQueue.remove(1);
		}

		return stackQueue;
	}

	public final void removeEffect(L2Effect effect)
	{
		if(effect == null || _effects == null)
			return;

		synchronized(_effects)
		{

			if (effect.getStackType() == "none")
			{
				removeStatsOwner(effect);
			}
			else if(effect.getStackType().equalsIgnoreCase("HpRecover"))
			{
				sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
			}
			else
			{
				if(_stackedEffects == null)
					return;

				List<L2Effect> stackQueue = _stackedEffects.get(effect.getStackType());

				if (stackQueue == null || stackQueue.size() < 1)
					return;

				L2Effect frontEffect = stackQueue.get(0);

				boolean removed = stackQueue.remove(effect);

				if (removed)
				{
					if (frontEffect == effect)
					{
						removeStatsOwner(effect);

						if (stackQueue.size() > 0)
						{
							for (int i=0; i<_effects.size(); i++)
							{
								if (_effects.get(i) == stackQueue.get(0))
								{
									addStatFuncs(_effects.get(i).getStatFuncs());
									_effects.get(i).setInUse(true);
									break;
								}
							}
						}
					}
					if (stackQueue.isEmpty())
						_stackedEffects.remove(effect.getStackType());
					else
						_stackedEffects.put(effect.getStackType(), stackQueue);
				}
			}
			for (int i=0; i<_effects.size(); i++)
			{
				if (_effects.get(i) == effect)
				{
					_effects.remove(i);
					break;
				}
			}

		}
		updateEffectIcons();
	}

	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}

	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	public final void startFakeDeath()
	{
		setIsFallsdown(true);
		setIsFakeDeath(true);
		abortAttack();
        abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this,ChangeWaitType.WT_START_FAKEDEATH));
	}

	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	public final void startMuted()
	{
		setIsMuted(true);
        abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

    public final void startPsychicalMuted()
    {
        setIsPsychicalMuted(true);
        getAI().notifyEvent(CtrlEvent.EVT_MUTED);
        updateAbnormalEffect();
    }

	public final void startRooted()
	{
		setIsRooted(true);
        getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}

	public final void startSleeping()
	{
		setIsSleeping(true);
        abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startMeditation()
	{
		setIsMeditation(true);
        abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startStunning()
	{
		setIsStunned(true);
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}

	public final void stopBetray()
	{
		stopEffects(L2Effect.EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}

	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}

	public final void stopAllEffects()
	{
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;
		for (L2Effect e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}

		if (this instanceof L2PcInstance) ((L2PcInstance)this).updateAndBroadcastStatus(2);
 	}

	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);

		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopSkillEffects(int skillId)
	{
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;

		for(L2Effect e : effects)
		{
			if (e.getSkill().getId() == skillId) e.exit();
		}
	}

	public final void stopEffects(L2Effect.EffectType type)
	{
		L2Effect[] effects = getAllEffects();

		if (effects == null) return;
		for(L2Effect e : effects)
		{
			if (e.getEffectType() == type) e.exit();
		}
	}

	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);

		setIsFakeDeath(false);
		setIsFallsdown(false); 
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setRecentFakeDeath(true);
		}

		ChangeWaitType revive = new ChangeWaitType(this,ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);

		setIsAfraid(false);
		updateAbnormalEffect();
	}

	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);

		setIsMuted(false);
		updateAbnormalEffect();
	}

	public final void stopPsychicalMuted(L2Effect effect)
    {
        if (effect == null)
            stopEffects(L2Effect.EffectType.PSYCHICAL_MUTE);
        else
            removeEffect(effect);

        setIsPsychicalMuted(false);
        updateAbnormalEffect();
    }

	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);

		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);

		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	public final void stopMeditation(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.MEDITATION);
		else
			removeEffect(effect);

		setIsMeditation(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);

		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public abstract void updateAbnormalEffect();

    public final void updateEffectIcons()
    {
        updateEffectIcons(false);
    }

	public final void updateEffectIcons(boolean partyOnly)
	{
        L2PcInstance player = null;
        if (this instanceof L2PcInstance)
            player = (L2PcInstance)this;

        L2Summon summon = null;
        if (this instanceof L2Summon)
        {
            summon = (L2Summon)this;
            player = summon.getOwner();
        }

		MagicEffectIcons mi = null;
        if (!partyOnly)
            mi = new MagicEffectIcons();

        PartySpelled ps = null;
        if (summon != null)
            ps = new PartySpelled(summon);
        else if (player != null && player.isInParty())
            ps = new PartySpelled(player);

        ExOlympiadSpelledInfo os = null;
        if (player != null && player.isInOlympiadMode())
            os = new ExOlympiadSpelledInfo(player);

        if (mi == null && ps ==null && os == null)
            return;

        L2Effect[] effects = getAllEffects();
		if (effects != null && effects.length > 0)
		{
			for (int i = 0; i < effects.length; i++)
			{
				L2Effect effect = effects[i];

				if (effect == null || !effect.getShowIcon())
                {
                    continue;
                }
				
				if (effect.getEffectType() == L2Effect.EffectType.CHARGE || effect.getEffectType() == L2Effect.EffectType.SIGNET_GROUND && player != null)
				{
					continue;
				}
				
				if (effect.getInUse())
				{
					if(effect.getStackType().equalsIgnoreCase("HpRecover"))
					{
						sendPacket(new ShortBuffStatusUpdate(effect.getSkill().getId(), effect.getSkill().getLevel(), (effect.getSkill().getBuffDuration()/1000)));
					}
					else
					{
						if (mi != null)
							effect.addIcon(mi);
						if (ps != null)
							effect.addPartySpelledIcon(ps);
						if (os != null)
							effect.addOlympiadSpelledIcon(os);
					}
				}
			}
		}

        if (mi != null)
            sendPacket(mi);
        if (ps != null && player != null)
        {
            if (player.isInParty() && summon == null)
                player.getParty().broadcastToPartyMembers(player, ps);
            else
                player.sendPacket(ps);
        }
        if (os != null)
        {
            if (Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null)
            {
                for (L2PcInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
                {
                    if (spectator == null) continue;
                    spectator.sendPacket(os);
                }
            }
        }
	}

	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (isStunned())  ae |= ABNORMAL_EFFECT_STUN;
		if (isRooted())   ae |= ABNORMAL_EFFECT_ROOT;
		if (isSleeping()) ae |= ABNORMAL_EFFECT_SLEEP;
		if (isMeditation()) ae |= ABNORMAL_EFFECT_FIREROOT_STUN;
		if (isConfused()) ae |= ABNORMAL_EFFECT_CONFUSED;
		if (isMuted())    ae |= ABNORMAL_EFFECT_MUTED;
		if (isAfraid())  ae |= ABNORMAL_EFFECT_AFRAID;
		if (isPsychicalMuted()) ae |= ABNORMAL_EFFECT_MUTED;
		return ae;
	}

	public final L2Effect[] getAllEffects()
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null || effects.isEmpty()) return EMPTY_EFFECTS;
        int ArraySize = effects.size();
        L2Effect[] effectArray = new L2Effect[ArraySize];
        for (int i=0; i<ArraySize; i++) {
            if (i >= effects.size() || effects.get(i) == null) break;
            effectArray[i] = effects.get(i);
        }
		return effectArray;
	}

	public final L2Effect getFirstEffect(int index)
	  {
	    FastTable<L2Effect> effects = _effects;
	    if (effects == null) return null;

	    L2Effect eventNotInUse = null;
	    for (int i = 0; i < effects.size(); i++) {
	      L2Effect e = (L2Effect)effects.get(i);
	      if (e.getSkill().getId() != index)
	        continue;
	      if (e.getInUse()) return e;
	      eventNotInUse = e;
	    }

	    return eventNotInUse;
	  }

	public final L2Effect getFirstEffect(L2Skill skill)
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null) return null;

        L2Effect e;
        L2Effect eventNotInUse = null;
        for (int i=0; i<effects.size(); i++) {
            e = effects.get(i);
            if (e.getSkill() == skill)
            {
            	if (e.getInUse()) return e;
            	else eventNotInUse = e;
            }
        }
        return eventNotInUse;
	}

	public final L2Effect getFirstEffect(L2Effect.EffectType tp)
	{
		FastTable<L2Effect> effects = _effects;
		if (effects == null) return null;

        L2Effect e;
        L2Effect eventNotInUse = null;
        for (int i=0; i<effects.size(); i++) {
            e = effects.get(i);
            if (e.getEffectType() == tp)
            {
            	if (e.getInUse()) return e;
            	else eventNotInUse = e;
            }
        }
        return eventNotInUse;
	}
	
	public EffectCharge getChargeEffect()
	{
	    L2Effect[] effects = getAllEffects();
	    for (L2Effect e : effects)
	    {
	        if (e.getSkill().getSkillType() == L2Skill.SkillType.CHARGE)
	        {
	            return (EffectCharge)e;    
	        }
	    }
	    return null;
	}
	
	/**
	 * @param _canCastAA the _canCastAA to set
	 */
	public void setCanCastAA(boolean v) 
	{
		this._canCastAA = v;
	}

	/**
	 * @return the _canCastAA
	 */
	public boolean getCanCastAA() 
	{
		return _canCastAA;
	}
	// =========================================================
























	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		public AIAccessor() {}

		/**
		 * Return the L2Character managed by this Accessor AI.<BR><BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 */
		public void doCast(L2Skill skill)
		{
			if ((L2Character.this.isAttackingNow()) && (skill.isActive()))
		      {
		        if (!L2Character.this.getCanCastAA())
		        {
		          L2Character.this.setCanCastAA(true);
		        }
		        else
		        {
		          L2Character.this.setCanCastAA(false);
		        }
		        return;
		      }
			L2Character.this.doCast(skill);
		}

		/**
		 * Create a NotifyAITask.<BR><BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR><BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}

	public static class MoveData
	{
		public int				_moveStartTime;
		public int				_moveTimestamp;
		public int				_xDestination;
		public int				_yDestination;
		public int				_zDestination;
		public double			_xAccurate;
		public double			_yAccurate;
		public double			_zAccurate;
		public int				_yMoveFrom;
		public int				_zMoveFrom;
		public int				_heading;

		public boolean			disregardingGeodata;
		public int				onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int				geoPathAccurateTx;
		public int				geoPathAccurateTy;
		public int				geoPathGtx;
		public int				geoPathGty;
	}


	/** Table containing all skillId that are disabled */
	protected List<Integer> _disabledSkills;
	private boolean _allSkillsDisabled;

//	private int _flyingRunSpeed;
//	private int _floatingWalkSpeed;
//	private int _flyingWalkSpeed;
//	private int _floatingRunSpeed;

	/** Movement data of this L2Character */
	protected MoveData _move;

	/** Orientation of the L2Character */
	private int _heading;

	/** L2Charcater targeted by the L2Character */
	private L2Object _target;

	// set by the start of casting, in game ticks
	private int     _castEndTime;
	private int     _castInterruptTime;

	// set by the start of attack, in game ticks
	private int     _attackEndTime;
	private int     _attacking;
	private int     _disableBowAttackEndTime;


	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static {NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();}

	protected L2CharacterAI _ai;

	/** Future Skill Cast */
	@SuppressWarnings("rawtypes")
	protected Future _skillCast;

	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;



	/** List of all QuestState instance that needs to be notified of this character's death */
	private List<QuestState> _NotifyQuestOfDeathList = new FastList<QuestState>();

	/**
	 * Add QuestState instance that is to be notified of character's death.<BR><BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 *
	 */
	public void addNotifyQuestOfDeath (QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.add(qs);
	}

	/**
	 * Return a list of L2Character that attacked.<BR><BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath ()
	{
		if (_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new FastList<QuestState>();

		return _NotifyQuestOfDeathList;
	}



	/**
	 * Add a Func to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR><BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null)
			return;


		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (_calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_calculators = new Calculator[Stats.NUM_STATS];

			for (int i=0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}

		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();

		if (_calculators[stat] == null)
			_calculators[stat] = new Calculator();

		// Add the Func to the calculator corresponding to the state
		_calculators[stat].addFunc(f);

	}
	

	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Equip an item from inventory</li>
	 * <li> Learn a new passive skill</li>
	 * <li> Use an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		
		FastList<Stats> modifiedStats = new FastList<Stats>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}


	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the Func object from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null)
			return;

		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();

		if (_calculators[stat] == null)
			return;

		// Remove the Func object from the Calculator
		_calculators[stat].removeFunc(f);

		if (_calculators[stat].size() == 0)
			_calculators[stat] = null;


		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}

			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}


	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		
		FastList<Stats> modifiedStats = new FastList<Stats>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		
		broadcastModifiedStats(modifiedStats);
		
	}

	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final synchronized void removeStatsOwner(Object owner)
	{

		FastList<Stats> modifiedStats = null;
		// Go through the Calculator set
		for (int i=0; i < _calculators.length; i++)
		{
			if (_calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				if (modifiedStats != null)
					modifiedStats.addAll(_calculators[i].removeOwner(owner));
				else
					modifiedStats = _calculators[i].removeOwner(owner);

				if (_calculators[i].size() == 0)
					_calculators[i] = null;
			}
		}

		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}

			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
		
		if (owner instanceof L2Effect && !((L2Effect)owner).preventExitUpdate)
				broadcastModifiedStats(modifiedStats);
		
	}
	
	private void broadcastModifiedStats(FastList<Stats> stats)
	{
		if (stats == null || stats.isEmpty()) return;
		
		boolean broadcastFull = false;
		boolean otherStats = false;
		StatusUpdate su = null;
		
		for (Stats stat : stats)
		{
			if (stat==Stats.POWER_ATTACK_SPEED) 
			{
				if (su == null) su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat==Stats.MAGIC_ATTACK_SPEED) 
			{
				if (su == null) su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
			//else if (stat==Stats.MAX_HP) // TODO: self only and add more stats...
			//{
			//	if (su == null) su = new StatusUpdate(getObjectId());
			//	su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			//}
			else if (stat==Stats.MAX_CP) 
			{
				if (this instanceof L2PcInstance)
				{
					if (su == null) su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}
			//else if (stat==Stats.MAX_MP) 
			//{
			//	if (su == null) su = new StatusUpdate(getObjectId());
			//	su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			//}
			else if (stat==Stats.RUN_SPEED)
			{
				broadcastFull = true;
			}
			else
				otherStats = true;
		}
		
		if (this instanceof L2PcInstance)
		{
			if (broadcastFull)
				((L2PcInstance)this).updateAndBroadcastStatus(2);
			else
			{
				if (otherStats)
				{
					((L2PcInstance)this).updateAndBroadcastStatus(1);
					if (su != null)
					{
						for (L2PcInstance player : getKnownList().getKnownPlayers().values())
						{
							try { player.sendPacket(su); } catch (NullPointerException e) {}
						}
					}
				}
				else if (su != null) broadcastPacket(su);
			}
		}
		else if (this instanceof L2NpcInstance)
		{
			if (broadcastFull)
			{
				for (L2PcInstance player : getKnownList().getKnownPlayers().values())
					if (player != null)
						player.sendPacket(new NpcInfo((L2NpcInstance)this, player));
			}
			else if (su != null) 
				broadcastPacket(su);
		}
		else if (this instanceof L2Summon)
		{
			if (broadcastFull)
			{
				for (L2PcInstance player : getKnownList().getKnownPlayers().values())
					if (player != null)
						player.sendPacket(new NpcInfo((L2Summon)this, player));
			}
			else if (su != null) 
				broadcastPacket(su);
		} 
		else if (su != null) 
			broadcastPacket(su);
	}

	/**
	 * Return the orientation of the L2Character.<BR><BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR><BR>
	 */
	public final int getClientX()
	{
		return _clientX;
	}
	public final int getClientY()
	{
		return _clientY;
	}
	public final int getClientZ()
	{
		return _clientZ;
	}
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	public final void setClientX(int val)
	{
		_clientX=val;
	}
	public final void setClientY(int val)
	{
		_clientY=val;
	}
	public final void setClientZ(int val)
	{
		_clientZ=val;
	}
	public final void setClientHeading(int val)
	{
		_clientHeading=val;
	}
	public final int getXdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._xDestination;

		return getX();
	}

	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR><BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._yDestination;

		return getY();
	}

	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR><BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._zDestination;

		return getZ();
	}

	/**
	 * Return True if the L2Character is in combat.<BR><BR>
	 */
	public final boolean isInCombat()
	{
		return (getAI().getAttackTarget() != null);
	}

	/**
	 * Return True if the L2Character is moving.<BR><BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * Return True if the L2Character is travelling a calculated path.<BR><BR>
	 */
	public final boolean isOnGeodataPath()
	{
		if (_move == null) return false;
		try
		{
			if (_move.onGeodataPathIndex == -1) return false;
			if (_move.onGeodataPathIndex == _move.geoPath.size()-1)
				return false;
		}
		catch (NullPointerException e) 
		{ 
			return false; 
		}
		return true;
	}


	/**
	 * Return True if the L2Character is casting.<BR><BR>
	 */
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the cast of the L2Character can be aborted.<BR><BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character is attacking.<BR><BR>
	 */
	public final boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character has aborted its attack.<BR><BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}


	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR><BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
	}

	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR><BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}

			if(getForceBuff() != null)
				getForceBuff().delete();

			L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();
			
			// cancels the skill hit scheduled task
			enableAllSkills();                                      // re-enables the skills
			if (this instanceof L2PcInstance) getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			broadcastPacket(new MagicSkillCanceld(getObjectId()));  // broadcast packet to stop animations client-side
			sendPacket(new ActionFailed());                         // send an "action failed" packet to the caster
		}
	}

	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;

		if (m == null)
			return true;

		if (!isVisible())
		{
			_move = null;
			return true;
		}

		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		// Check if the position has already been calculated
		if (m._moveTimestamp == gameTicks)
			return false;

		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations

		double dx, dy, dz, distFraction;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else // otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		// Z coordinate will follow geodata or client values
		if (Config.GEODATA && Config.COORD_SYNCHRONIZE == 2
			&& !isFlying() && !isInsideZone(ZONE_WATER)
			&& !m.disregardingGeodata
			&& GameTimeController.getGameTicks() % 10 == 0
			&& !(this instanceof L2BoatInstance)) // once a second to reduce possible cpu load
		{
			short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev-30, zPrev+30, getObjectId());
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (this instanceof L2PcInstance && Math.abs(((L2PcInstance)this).getClientZ() - geoHeight) > 200
					&& Math.abs(((L2PcInstance)this).getClientZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && Math.abs(dz) > 200 && (dx*dx + dy*dy) < 40000) // allow mob to climb up to pcinstance
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
			dz = m._zDestination - zPrev;

		double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		if ((dx*dx + dy*dy) < 10000 && (dz*dz > 2500)) // close enough, allows error between client and server geodata if it cannot be avoided
		{
			distFraction = distPassed / Math.sqrt(dx*dx + dy*dy);
		}
		else
			distFraction = distPassed / Math.sqrt(dx*dx + dy*dy + dz*dz);


		if (distFraction > 1) // already there
		{
			// Set the position of the L2Character to the destination
			if (this instanceof L2BoatInstance)
			{
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
				((L2BoatInstance) this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
			else
			{
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
				revalidateZone(false);
			}
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;

			// Set the position of the L2Character to estimated after parcial move
			if(this instanceof L2BoatInstance)
			{
				super.getPosition().setXYZ((int)(m._xAccurate), (int)(m._yAccurate), zPrev + (int)(dz * distFraction + 0.5));
				((L2BoatInstance)this).updatePeopleInTheBoat((int)(m._xAccurate), (int)(m._yAccurate), zPrev + (int)(dz * distFraction + 0.5));
			}
			else
			{
				super.getPosition().setXYZ((int)(m._xAccurate), (int)(m._yAccurate), zPrev + (int)(dz * distFraction + 0.5));
				revalidateZone(false);
			}
		}

		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;

		return (distFraction > 1);
	}
	
	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null) return;
		
		// This function is called too often from movement code
    	if (force) _zoneValidateCounter = 4;
    	else
    	{
    		_zoneValidateCounter--;
    		if (_zoneValidateCounter < 0)
    			_zoneValidateCounter = 4;
    		else return;
    	}
    	
		getWorldRegion().revalidateZones(this);
	}

	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR><BR>
	 *
	 */
	public void stopMove(L2CharPosition pos)
	{
		stopMove(pos, false);
	}
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;

		//if (getAI() != null)
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			if (this instanceof L2PcInstance) ((L2PcInstance)this).revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects) this.getKnownList().findObjects();
	}


	public boolean isShowSummonAnimation()
	{
	    return _showSummonAnimation;
	}

	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
	    _showSummonAnimation = showSummonAnimation;
	}

	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;

		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}

		// If object==null, Cancel Attak or Cast
		if (object == null)
		{
			if(_target != null)
			{
				broadcastPacket(new TargetUnselected(this));
			}

			if (isAttackingNow() && getAI().getAttackTarget() == _target)
			{
				abortAttack();

				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Attack is aborted");
					sendPacket(sm);
				}
			}

			if (isCastingNow() && canAbortCast() && getAI().getCastTarget() == _target)
			{
				abortCast();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Casting is aborted");
					sendPacket(sm);
				}
			}
		}

		_target = object;
	}
	/**
	 * Return the identifier of the L2Object targeted or -1.<BR><BR>
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}

		return -1;
	}

	/**
	 * Return the L2Object targeted or null.<BR><BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}

	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li><BR><BR>
	 *
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 *
	 */

	protected void moveToLocation(int x, int y, int z, int offset)
	{
		getAI().setSitDownAfterAction(false);
		// Get the Move Speed of the L2Character
		float speed = getStat().getMoveSpeed();
		// if (speed <= 0 || isMovementDisabled()) return;
		if (speed <= 0)
			return;

		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();

		// Calculate distance (dx,dy) between current position and destination
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);

		int toX = x;
		int toY = y;
		int toZ = z;

		//double distance = Math.sqrt(dx * dx + dy * dy);
		double distSq = dx * dx + dy * dy;

		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		/*
		if (Config.GEODATA && isInsideZone(ZONE_WATER) && distSq > 490000)
		{
			double divider = 490000 / distSq;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distSq = dx * dx + dy * dy;
		}
		 */
		double distance = Math.sqrt(distSq);

		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)

		double cos;
		double sin;

		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
				offset = 5;

			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset <= 0)
			{
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);

				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;

			distance -= (offset - 5); // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);

		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;

		if (Config.GEODATA && !isFlying() // flying chars not checked - even canSeeTarget doesn't work yet
				&& (!isInsideZone(ZONE_WATER) || isInsideZone(ZONE_SIEGE)) // swimming also not checked unless in siege zone - but distance is limited
				&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			int gty = (originalY - L2World.MAP_MIN_Y) >> 4;

			// Movement checks:
			if ((Config.GEO_MOVE_PC && this instanceof L2PcInstance)
					|| (Config.GEO_MOVE_NPC && this instanceof L2Attackable && !((L2Attackable) this).isReturningToSpawnPoint())
					|| (this instanceof L2Summon && (getAI().getIntention() != AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
					|| isAfraid() || this instanceof L2RiftInvaderInstance) // Rift mobs should never walk through walls
			{
				if (isOnGeodataPath())
				{
					if (_move != null) {
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return;

						_move.onGeodataPathIndex = -1; // Set not on geodata path
					}
				}

				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y || curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					_log.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
						((L2PcInstance) this).deleteMe();
					else
						onDecay();
					return;
				}

				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
			}
			// Pathfinding checks. Only when geo-pathfinding is enabled, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (Config.GEO_PATH_FINDING && originalDistance - distance > 1 && distance < 2000 && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if (this instanceof L2PlayableInstance || this.isInCombat() || this instanceof L2MinionInstance)
				{
					@SuppressWarnings("unused")
					int gx = curX - -131072 >> 4;
			        @SuppressWarnings("unused")
					int gy = curY - -262144 >> 4;
			m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, toX, toY, toZ, this instanceof L2PlayableInstance);
			if (m.geoPath == null || m.geoPath.size() < 2) // No path found
					{
						// * Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough. 
						// * With cellpathfinding this approach could be changed but would require taking
						// off the geonodes and some more checks.
						// * Summons will follow their masters no matter what.
						// * Currently minions also must move freely since L2AttackableAI commands
						// them to move along with their leader
						if (this instanceof L2PcInstance
									|| (!(this instanceof L2PlayableInstance)
										&& !(this instanceof L2MinionInstance)
										&& Math.abs(z - curZ) > 140)
									|| (this instanceof L2Summon && !((L2Summon) this).getFollowStatus()))
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}

						m.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
					}
					else
					{
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;

						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();

						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						/*for (int i = 0; i < m.geoPath.getPath().size() - 1; i++)
						{
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.getPath().get(i), m.geoPath.getPath().get(i + 1)))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}*/

						dx = (x - curX);
						dy = (y - curY);
						distance = Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			// Don't uncomment to keep pathfinding working
			/*if (distance < 2
					&& (Config.GEO_PATH_FINDING || this instanceof L2PcInstance || (this instanceof L2Summon && !((L2Summon) this).getFollowStatus())
							|| isAfraid() || this instanceof L2RiftInvaderInstance))
			{
				if (this instanceof L2Summon)
					((L2Summon) this).setFollowStatus(false);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE); // needed?
				return;
			}*/
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);

		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client

		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		setHeading(Util.calculateHeadingFrom(cos, sin));


		m._moveStartTime = GameTimeController.getGameTicks();

		if (((this instanceof L2PcInstance)) && (((L2PcInstance)this).getActiveEnchantItem() != null))
	    {
	      L2PcInstance _player = (L2PcInstance)this;
	      _player.setActiveEnchantItem(null);
	      _player.sendPacket(new EnchantResult(1));
	      _player = null;
	    }
		
		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}
	
	public boolean moveToNextRoutePoint()
	{
		if (!this.isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();
		MoveData md = _move;
		if (md == null)
			return false;

		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;

		if (md.onGeodataPathIndex == md.geoPath.size()-2)
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = (m._xDestination - super.getX());
		double dy = (m._yDestination - super.getY());

		double distance = Math.sqrt(dx * dx + dy * dy);
		double sin = dy / distance;
		double cos = dx / distance;

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);

		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378);
		heading += 32768;
		setHeading(heading);
		m._heading = 0; // initial value for coordinate sync

		m._moveStartTime = GameTimeController.getGameTicks();

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController

		// Send a Server->Client packet MoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		CharMoveToLocation msg = new CharMoveToLocation(this);
		broadcastPacket(msg);

		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData md = _move;

		if (md == null) return true;

		boolean result = true;
		if (md._heading != heading)
		{
			result = (md._heading == 0);
			md._heading = heading;
		}

		return result;
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x   X position of the target
	 * @param y   Y position of the target
     * @return the plan distance
	 *
     * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
    public final double getDistance(int x, int y)
    {
       double dx = x - getX();
       double dy = y - getY();

        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @return the plan distance
     *
     * @deprecated use getPlanDistanceSq(int x, int y, int z)
     */
	@Deprecated
    public final double getDistance(int x, int y, int z)
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();

        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Return the squared distance between the current position of the L2Character and the given object.<BR><BR>
     *
     * @param object   L2Object
     * @return the squared distance
     */
    public final double getDistanceSq(L2Object object)
    {
        return getDistanceSq(object.getX(), object.getY(), object.getZ());
    }

    /**
     * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param z   Z position of the target
     * @return the squared distance
     */
    public final double getDistanceSq(int x, int y, int z)
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();

        return (dx*dx + dy*dy + dz*dz);
    }

    /**
     * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
     * (check only x and y, not z)<BR><BR>
     *
     * @param object   L2Object
     * @return the squared plan distance
     */
    public final double getPlanDistanceSq(L2Object object)
    {
        return getPlanDistanceSq(object.getX(), object.getY());
    }
    /**
     * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
     * (check only x and y, not z)<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @return the squared plan distance
     */
    public final double getPlanDistanceSq(int x, int y)
    {
        double dx = x - getX();
        double dy = y - getY();

        return (dx*dx + dy*dy);
    }

    /**
     * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR><BR>
     *
     * @param object   the target
     * @param radius  the radius around the target
     * @param checkZ  should we check Z axis also
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     *
     * @see net.sf.l2j.gameserver.model.L2Character.isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
     */
    public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
    {
        return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
    }
    /**
     * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param radius  the radius around the target
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     */
    public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
    {
        return isInsideRadius(x, y, 0, radius, false, strictCheck);
    }
    /**
     * Check if this object is inside the given radius around the given point.<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param z   Z position of the target
     * @param radius  the radius around the target
     * @param checkZ  should we check Z axis also
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     */
    public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();

        if (strictCheck)
        {
            if (checkZ)
                return (dx*dx + dy*dy + dz*dz) < radius * radius;
            else
                return (dx*dx + dy*dy) < radius * radius;
        } else
        {
            if (checkZ)
                return (dx*dx + dy*dy + dz*dz) <= radius * radius;
            else
                return (dx*dx + dy*dy) <= radius * radius;
        }
    }

	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}


	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * Retun True if arrows are available.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 *
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getSecondaryWeaponItem();


	/**
	 * Manage hit process (called by Hit Task).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 *
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() ||(this instanceof L2NpcInstance && ((L2NpcInstance) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			sendPacket(new ActionFailed());
			return;
		}

		if ((this instanceof L2NpcInstance && target.isAlikeDead()) || target.isDead()
                || (!getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance)))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			sendPacket(new ActionFailed());
			return;
		}

        if (miss)
        {
            if (target instanceof L2PcInstance)
            {
                SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);

                if (this instanceof L2Summon)
                {
                    int mobId = ((L2Summon)this).getTemplate().npcId;
                    sm.addNpcName(mobId);
                }
                else
                {
                    sm.addString(getName());
                }

                ((L2PcInstance)target).sendPacket(sm);
            }
        }

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid())
			{
				int level = 0;
				if (this instanceof L2PcInstance)
					level = getLevel();
				else if (this instanceof L2Summon)
					level = ((L2Summon)this).getOwner().getLevel();

				if (level > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);

					if (skill != null)
						skill.getEffects(target, this);
					else
						_log.warning("Skill 4515 at level 1 is missing in DP.");

					damage = 0; // prevents messing up drop calculation
				}
			}
			if(target instanceof L2PcInstance)
			{
				if (((L2PcInstance)target).isInOlympiadMode())
				{
					if (this instanceof L2PcInstance)
					{
						if (!((L2PcInstance)this).isInOlympiadMode())
						{
							damage = 0;
							Util.handleIllegalPlayerAction(((L2PcInstance)this),"Warning! Character "+((L2PcInstance)this).getName()+" of account "+((L2PcInstance)this).getAccountName()+" using olympiad bugs.", Config.DEFAULT_PUNISH);
						}
					}
					else if (this instanceof L2Summon)
					{
						if (!(((L2Summon)this).getOwner()).isInOlympiadMode())
						{
							damage = 0;
							Util.handleIllegalPlayerAction(((L2Summon)this).getOwner(),"Warning! Character "+((L2Summon)this).getOwner().getName()+" of account "+((L2Summon)this).getOwner().getAccountName()+" using olympiad bugs.", Config.DEFAULT_PUNISH);
						}
					}
				}
			}
			sendDamageMessage(target, damage, false, crit, miss);

			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance)target;

				// Check if shield is efficient
				if (shld)
					enemy.sendPacket(new SystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
				//else if (!miss && damage < 1)
					//enemy.sendMessage("You hit the target's armor.");
			}
            else if (target instanceof L2Summon)
            {
                L2Summon activeSummon = (L2Summon)target;

                SystemMessage sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
                sm.addString(getName());
                sm.addNumber(damage);
                activeSummon.getOwner().sendPacket(sm);
            }

			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isBow = (weapon != null && weapon.getItemType().toString().equalsIgnoreCase("Bow"));

				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT,0,null,null);

					if (reflectPercent > 0)
					{
						int reflectedDamage = (int)(reflectPercent / 100. * damage);
						damage -= reflectedDamage;

						if(reflectedDamage > target.getMaxHp()) // to prevent extreme damage when hitting a low lvl char...
							reflectedDamage = target.getMaxHp();

						getStatus().reduceHp(reflectedDamage, target, true);
					}

					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT,0, null,null);

					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int)(getMaxHp() - getCurrentHp());
						int absorbDamage = (int)(absorbPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
                            absorbDamage = maxCanAbsorb; // Can't absord more than max hp

                        if (absorbDamage > 0)
                        {
                            setCurrentHp(getCurrentHp() + absorbDamage);
                        }
					}
				}

				target.reduceCurrentHp(damage, this);

                // Notify AI with EVT_ATTACKED
                target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
                getAI().clientStartAutoAttack();

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				if (_chanceSkills != null)
					_chanceSkills.onHit(target, false, crit);

				if (target.getChanceSkills() != null)
					target.getChanceSkills().onHit(this, true, crit);
			}

			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();

			if (activeWeapon != null)
				activeWeapon.getSkillEffects(this, target, crit);

            return;
		}

		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}

	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();

			if (this instanceof L2PcInstance)
			{
				//TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(new ActionFailed());

				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}


	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();

			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			}
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}

	/**
	 * Manage Forced attack (shift + select target).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed </li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li><BR><BR>
	 *
	 * @param player The L2PcInstance to attack
	 *
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
        if (isInsidePeaceZone(player))
		{
            if(!player.isInFunEvent() || !player.getTarget().isInFunEvent())
            {
                // If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
                player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
                player.sendPacket(new ActionFailed());
            }
		}
		else if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget() instanceof L2PlayableInstance)
        {
        	L2PcInstance target;
        	if (player.getTarget() instanceof L2Summon)
        		target=((L2Summon)player.getTarget()).getOwner();
        	else
        		target=(L2PcInstance)player.getTarget();
        	
        	if (target.isInOlympiadMode() && !player.isOlympiadStart() && player.getOlympiadGameId()!=target.getOlympiadGameId())
        	{
        		player.sendPacket(new ActionFailed());
        	}
			else
				if (player.isOlympiadStart() && player.getOlympiadGameId() == target.getOlympiadGameId())
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
        }
		else if (player.getTarget() != null && !player.getTarget().isAttackable() && (player.getAccessLevel() < Config.GM_PEACEATTACK))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
		}
		else if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
		}
		else if (this instanceof L2ArtefactInstance)
		{
			// If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
		}
		else
		{
			// GeoData Los Check or dz > 1000
	        if (!GeoData.getInstance().canSeeTarget(player, this))
	        {
	            player.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
	            player.sendPacket(new ActionFailed());
	            return;
	        }
			// Notify AI with AI_INTENTION_ATTACK
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
	}

	/**
	 * Return True if inside peace zone.<BR><BR>
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
        if(!isInFunEvent() || !attacker.isInFunEvent())
        {
            return isInsidePeaceZone(attacker, this);
        }
        return false;
	}

	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return  (
				(attacker.getAccessLevel() < Config.GM_PEACEATTACK) &&
				isInsidePeaceZone((L2Object)attacker, target)
		);
	}

	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null) return false;
		if (target instanceof L2MonsterInstance) return false;
		if (target instanceof L2NpcInstance) return false;
		if (attacker instanceof L2MonsterInstance) return false;
		if (attacker instanceof L2NpcInstance) return false;
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target instanceof L2PcInstance && ((L2PcInstance)target).getKarma() > 0)
				return false;
			if (target instanceof L2Summon && ((L2Summon)target).getOwner().getKarma() > 0)
				return false;
			if (attacker instanceof L2PcInstance && ((L2PcInstance)attacker).getKarma() > 0)
			{
				if(target instanceof L2PcInstance && ((L2PcInstance)target).getPvpFlag() > 0)
					return false;
				if(target instanceof L2Summon && ((L2Summon)target).getOwner().getPvpFlag() > 0)
					return false;
			}
			if (attacker instanceof L2Summon && ((L2Summon)attacker).getOwner().getKarma() > 0)
			{
				if(target instanceof L2PcInstance && ((L2PcInstance)target).getPvpFlag() > 0)
					return false;
				if(target instanceof L2Summon && ((L2Summon)target).getOwner().getPvpFlag() > 0)
					return false;
			}
		}
		// Right now only L2PcInstance has up-to-date zone status...
		// TODO: ZONETODO: Are there things < L2Characters in peace zones that can be attacked? If not this could be cleaned up
		
		if (attacker instanceof L2Character && target instanceof L2Character)
		{
			return (((L2Character)target).isInsideZone(ZONE_PEACE) || ((L2Character)attacker).isInsideZone(ZONE_PEACE));
		}
		if (attacker instanceof L2Character)
		{
			return (TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null || ((L2Character)attacker).isInsideZone(ZONE_PEACE));
		}

		return (TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null ||
				TownManager.getInstance().getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null);
	}

    /**
     * return true if this character is inside an active grid.
     */
    public Boolean isInActiveRegion()
    {
        try
        {
        	L2WorldRegion region = L2World.getInstance().getRegion(getX(),getY());
        	return  ((region !=null) && (region.isActive()));
        }
        catch (Exception e)
        {
            if (this instanceof L2PcInstance)
            {
            	_log.warning("Player "+ getName() +" at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
            	((L2PcInstance)this).sendMessage("Error with your coordinates! Please reboot your game fully!");
            	((L2PcInstance)this).teleToLocation(80753,145481,-3532, false); // Near Giran luxury shop
            }
            else
            {
            	_log.warning("Object "+ getName() +" at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
            	decayMe();
            }
            return false;
        }
    }

	/**
	 * Return True if the L2Character has a Party in progress.<BR><BR>
	 */
    public boolean isInParty()
	{
		return false;
	}

	/**
	 * Return the L2Party object of the L2Character.<BR><BR>
	 */
	public L2Party getParty()
	{
		return null;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR><BR>
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon !=null)
		{
			switch (weapon.getItemType())
			{
			case BOW:
				atkSpd = getStat().getPAtkSpd();
				return (int)(1500*345/atkSpd);
			case DAGGER:
				atkSpd = getStat().getPAtkSpd();
				//atkSpd /= 1.15;
				break;
			default:
				atkSpd = getStat().getPAtkSpd();
			}
		}
		else
			atkSpd = getPAtkSpd();

		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}
	
	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null) return 0;
		
		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0) return 0; 
		// else if (reuse < 10) reuse = 1500;
		
		reuse *= getStat().getReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
				return (int)(reuse*345/atkSpd);
			default:
				return (int)(reuse*312/atkSpd);
		}
	}
	
	/**
	 * Return True if the L2Character use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill    = null;

		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
				removeStatsOwner(oldSkill);

			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));

			if (oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}

			if (newSkill.isChance())
			{
				addChanceSkill(newSkill);
			}
		}

		return oldSkill;
	}


	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null) return null;

		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skill.getId());

		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			removeStatsOwner(oldSkill);
			stopSkillEffects(oldSkill.getId());
			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
		}

		return oldSkill;
	}

	public void addChanceSkill(L2Skill skill)
	{
		synchronized(this)
		{
			if (_chanceSkills == null)
				_chanceSkills = new ChanceSkillList(this);
			_chanceSkills.put(skill, skill.getChanceCondition());
		}
	}

	public void removeChanceSkill(int id)
	{
		synchronized(this)
		{
			for (L2Skill skill : _chanceSkills.keySet())
			{
				if (skill.getId() == id)
					_chanceSkills.remove(skill);
			}
			if (_chanceSkills.size() == 0)
				_chanceSkills = null;
		}
	}

	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	public int getSkillLevel(int skillId)
	{
		if (_skills == null)
			return -1;

		L2Skill skill = _skills.get(skillId);

		if (skill == null)
			return -1;
		return skill.getLevel();
	}

	/**
	 * Return True if the skill is known by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 *
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
			return null;

		return _skills.get(skillId);
	}


    /**
     * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this L2Character.<BR><BR>
     *
     * @return The number of Buffs affecting this L2Character
     */
    public int getBuffCount() {
        L2Effect[] effects = getAllEffects();
        int numBuffs=0;
        if (effects != null) {
            for (L2Effect e : effects) {
                if (e != null) {
                    if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) &&
                        !(e.getSkill().getId() > 4360  && e.getSkill().getId() < 4367)) { // 7s buffs
                        numBuffs++;
                    }
                }
            }
        }
        return numBuffs;
    }

    /**
     * Removes the first Buff of this L2Character.<BR><BR>
     *
     * @param preferSkill If != 0 the given skill Id will be removed instead of first
     */
    public void removeFirstBuff(int preferSkill) {
        L2Effect[] effects = getAllEffects();
        L2Effect removeMe=null;
        if (effects != null) {
            for (L2Effect e : effects) {
                if (e != null) {
                    if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) &&
                        !(e.getSkill().getId() > 4360  && e.getSkill().getId() < 4367)) {
                        if (preferSkill == 0) { removeMe=e; break; }
                        else if (e.getSkill().getId() == preferSkill) { removeMe=e; break; }
                        else if (removeMe==null) removeMe=e;
                    }
                }
            }
        }
        if (removeMe != null) removeMe.exit();
    }

    public int getDanceCount()
    {
    	int danceCount = 0;
    	L2Effect[] effects = getAllEffects();
    	for (L2Effect effect : effects)
		{
    		if (effect == null)
    			continue;
    		if (effect.getSkill().isDance() && effect.getInUse())
    			danceCount++;
		}
    	return danceCount;
    }

    
  //Debuff count. RightOne.
    public int getDeBuffCount()
    {
      L2Effect[] effects = getAllEffects();
      int numDeBuffs = 0;

      if (effects != null)
      {
        for (L2Effect e : effects)
        {
          if (e == null)
            continue;
          if (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF)
            continue;
          numDeBuffs++;
        }

      }

      effects = null;

      return numDeBuffs;
    }
 //Remove first debuff. RightOne
    public void removeFirstDeBuff(int preferSkill)
    {
      L2Effect[] effects = getAllEffects();

      L2Effect removeMe = null;

      if (effects != null)
      {
        for (L2Effect e : effects)
        {
          if (e == null)
            continue;
          if (e.getSkill().getSkillType() != L2Skill.SkillType.DEBUFF)
            continue;
          if (preferSkill == 0)
          {
            removeMe = e;
            break;
          }
          if (e.getSkill().getId() == preferSkill)
          {
            removeMe = e;
            break;
          }
          if (removeMe != null)
            continue;
          removeMe = e;
        }

      }

      if (removeMe != null)
      {
        removeMe.exit();
      }

      effects = null;
      removeMe = null;
    }
    
    /**
     * Checks if the given skill stacks with an existing one.<BR><BR>
     *
     * @param checkSkill the skill to be checked
     *
     * @return Returns whether or not this skill will stack
     */
    public boolean doesStack(L2Skill checkSkill) {
        if (_effects == null || _effects.size() < 1 ||
                checkSkill._effectTemplates == null ||
                checkSkill._effectTemplates.length < 1 ||
                checkSkill._effectTemplates[0].stackType == null) return false;
        String stackType=checkSkill._effectTemplates[0].stackType;
        if (stackType.equals("none")) return false;

        for (int i=0; i<_effects.size(); i++) {
            if (_effects.get(i).getStackType() != null &&
                    _effects.get(i).getStackType().equals(stackType)) return true;
        }
        return false;
    }

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	public void onMagicLaunchedTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
	{
		if (skill == null)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if ((targets == null || targets.length <= 0) && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if(skill.getEffectRange() > escapeRange) escapeRange = skill.getEffectRange();
		else if(skill.getCastRange() < 0 && skill.getSkillRadius() > 80) escapeRange = skill.getSkillRadius();

		if(escapeRange > 0)
		{
			List<L2Character> targetList = new FastList<L2Character>();
			for (int i = 0; i < targets.length; i++)
			{
				if (targets[i] instanceof L2Character)
				{
					if ((!Util.checkIfInRange(escapeRange, this, targets[i], true)) || !GeoData.getInstance().canSeeTarget(this, targets[i]))
						continue;
					if(skill.isOffensive())
					{
						if(this instanceof L2PcInstance)
						{
							if(((L2Character)targets[i]).isInsidePeaceZone((L2PcInstance)this)) 
								continue;
						}
						else
						{
							if(((L2Character)targets[i]).isInsidePeaceZone(this, targets[i])) 
								continue;
						}
					}
					targetList.add((L2Character)targets[i]);
				}
			}
			if(targetList.isEmpty() && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA)
			{
				abortCast();
				return;
			}
			else targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if (!isCastingNow() || (isAlikeDead() && !skill.isPotion()))
		{
			_skillCast = null;
			enableAllSkills();
			
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}

		
		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();

		// Get the level of the skill
		int level = getSkillLevel(skill.getId());

		if (level < 1)
			level = 1;
		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isPotion()) broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
			
		if (instant)
			onMagicHitTimer(targets, skill, coolTime, true);
		else 
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2), 200);
		
	}
	
	/*
	 * Runs in the end of skill casting 
	 */
	public void onMagicHitTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
	{
		if (skill == null)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if ((targets == null || targets.length <= 0) && skill.getTargetType() != L2Skill.SkillTargetType.TARGET_AURA)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if (skill.getItemConsume() > 0 && !(this instanceof L2NpcInstance))
        {
			L2ItemInstance requiredItems = ((L2PcInstance)this).getInventory().getItemByItemId(skill.getItemConsumeId());
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				_skillCast = null;
				enableAllSkills();
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
		}

		if(getForceBuff() != null)
		{
			_skillCast = null;
			enableAllSkills();
			
			getForceBuff().delete();
			return;
		}

		L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			_skillCast = null;
			enableAllSkills();
			mog.exit();
			return;
		}

		try {
			
			// Go through targets table
			for (int i = 0;i < targets.length;i++)
			{
			  if (targets[i] instanceof L2PlayableInstance)
			  {
				L2Character target = (L2Character) targets[i];

				if (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.SEED)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
			        smsg.addString(skill.getName());
			        target.sendPacket(smsg);
				}

				if (this instanceof L2PcInstance && target instanceof L2Summon)
				{
					((L2Summon)target).getOwner().sendPacket(new PetInfo((L2Summon)target));
					sendPacket(new NpcInfo((L2Summon)target, this));

					// The PetInfo packet wipes the PartySpelled (list of active spells' icons).  Re-add them
					((L2Summon)target).updateEffectIcons(true);
				}
			  }
			}

			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;

			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);
			if (mpConsume > 0)
			{
				if (skill.isDance())
				{
					getStatus().reduceMp(calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else if (skill.isMagic())
				{
					getStatus().reduceMp(calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}
				else
				{
					getStatus().reduceMp(calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null));
				}

			  su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			  isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
			  double consumeHp;

			  consumeHp = calcStat(Stats.HP_CONSUME_RATE,skill.getHpConsume(),null,null);
			  if(consumeHp+1 >= getCurrentHp())
				consumeHp = getCurrentHp()-1.0;

			  getStatus().reduceHp(consumeHp, this);

			  su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			  isSendStatus = true;
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus) sendPacket(su);

			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0 && !(this instanceof L2NpcInstance))
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());

			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);
		} 
		catch (NullPointerException e) {} 
		
		if (instant || coolTime == 0)
			onMagicFinalizer(targets, skill);
		else 
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3), coolTime);
	}
	/*
	 * Runs after skill hitTime+coolTime 
	 */
	public void onMagicFinalizer(L2Object[] targets, L2Skill skill)
	{
		_skillCast = null;
		_castEndTime = 0;
		_castInterruptTime = 0;
		enableAllSkills();
		
		//if the skill has changed the character's state to something other than STATE_CASTING
		//then just leave it that way, otherwise switch back to STATE_IDLE.
		//if(isCastingNow())
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);

		// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target with AI_INTENTION_ATTACK
		if (((getAI().getNextIntention() == null) && (skill.getSkillType() == L2Skill.SkillType.PDAM) && (skill.getCastRange() < 400)) || skill.getSkillType() == SkillType.BLOW 
				|| skill.getSkillType() == SkillType.DRAIN_SOUL || skill.getSkillType() == SkillType.SOW 
				|| skill.getSkillType() == SkillType.SPOIL || skill.getSkillType() == SkillType.STUN)
		{
			if ((getTarget() != null) && (getTarget() instanceof L2Character && (getTarget() != this)))
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
		}
		if (skill.getTargetType() == SkillTargetType.TARGET_CORPSE_MOB || skill.getTargetType() == SkillTargetType.TARGET_AREA_CORPSE_MOB)
		{
			if ((getTarget() != null) && (getTarget() instanceof L2Attackable && (getTarget() != this)))
				getTarget().decayMe();
		}
		
        if (skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) && !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK))
            {
        	if(getTarget() != this) 
         	    getAI().clientStartAutoAttack();
            }
        // Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		if (skill.useSoulShot())
	    {
	      if ((this instanceof L2NpcInstance))
	        ((L2NpcInstance)this).rechargeAutoSoulShot(true, false);
	      else if ((this instanceof L2PcInstance))
	        ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
	      else if ((this instanceof L2Summon))
	        ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
	    }
	    else if (skill.useSpiritShot())
	    {
	      if ((this instanceof L2PcInstance))
	        ((L2PcInstance)this).rechargeAutoSoulShot(false, true, false);
	      else if ((this instanceof L2Summon)) {
	        ((L2Summon)this).getOwner().rechargeAutoSoulShot(false, true, true);
	      }
	    }
		
        /*
         * If character is a player, then wipe their current cast state and
         * check if a skill is queued.
         *
         * If there is a queued skill, launch it and wipe the queue.
         */
        if (this instanceof L2PcInstance)
        {
            L2PcInstance currPlayer = (L2PcInstance)this;
            SkillDat queuedSkill = currPlayer.getQueuedSkill();

            currPlayer.setCurrentSkill(null, false, false);

            if (queuedSkill != null)
            {
                currPlayer.setQueuedSkill(null, false, false);

                // DON'T USE : Recursive call to useMagic() method
                // currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
                ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()) );
            }
        }
	}

	/**
	 * Reduce the item number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to enable
	 *
	 */
	public void enableSkill(int skillId)
	{
		if (_disabledSkills == null) return;

		_disabledSkills.remove(new Integer(skillId));

		if (this instanceof L2PcInstance)
			removeTimeStamp(skillId);
	}

	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public void disableSkill(int skillId)
	{
		if (_disabledSkills == null) _disabledSkills = Collections.synchronizedList(new FastList<Integer>());

		_disabledSkills.add(skillId);
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skillId
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(int skillId, long delay)
	{
	    disableSkill(skillId);
	    if (delay > 10) ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
	}

	/**
	 * Check if a skill is disabled.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (_disabledSkills == null) return false;

		return _disabledSkills.contains(skill.getId());
	}

	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled()) return true;

		if (_disabledSkills == null) return false;

		return _disabledSkills.contains(skillId);
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}

	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR><BR>
	 */
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 *
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			L2PcInstance player = null;
			if (this instanceof L2PcInstance)
				player = (L2PcInstance)this;
			else if (this instanceof L2Summon)
				player = ((L2Summon)this).getOwner();

			if(skill.isToggle() && getFirstEffect(skill.getId()) != null)
				return;
			
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					L2Character target = (L2Character) trg;
					L2Character targetsAttackTarget = target.getAI().getAttackTarget();
					L2Character targetsCastTarget = target.getAI().getCastTarget();
					if(
							(target.isRaid() && getLevel() > target.getLevel() + 8)
							||
							(!skill.isOffensive() && targetsAttackTarget != null && targetsAttackTarget.isRaid() 
									&& targetsAttackTarget.getAttackByList().contains(target)
									&& getLevel() > targetsAttackTarget.getLevel() + 8)
							||
							(!skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid() 
									&& targetsCastTarget.getAttackByList().contains(target)
									&& getLevel() > targetsCastTarget.getLevel() + 8)
					)
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
							if(tempSkill != null)
								tempSkill.getEffects(target, this);
							else
								_log.warning("Skill 4215 at level 1 is missing in DP.");
						}
						else
						{
							L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
							if(tempSkill != null)
								tempSkill.getEffects(target, this);
							else
								_log.warning("Skill 4515 at level 1 is missing in DP.");
						}
						return;
					}

					if (target instanceof L2PcInstance)
					{
						if (((L2PcInstance)target).isInOlympiadMode())
						{
							if (this instanceof L2PcInstance)
							{
								if (!((L2PcInstance)this).isInOlympiadMode())
								{
									Util.handleIllegalPlayerAction(((L2PcInstance)this),"Warning! Character "+((L2PcInstance)this).getName()+" of account "+((L2PcInstance)this).getAccountName()+" using olympiad bugs.", Config.DEFAULT_PUNISH);
									return;
								}
							}
							else if (this instanceof L2Summon)
							{
								if (!(((L2Summon)this).getOwner()).isInOlympiadMode())
								{
									Util.handleIllegalPlayerAction(((L2Summon)this).getOwner(),"Warning! Character "+((L2Summon)this).getOwner().getName()+" of account "+((L2Summon)this).getOwner().getAccountName()+" using olympiad bugs.", Config.DEFAULT_PUNISH);
									return;
								}
							}
						}
					}

		            if(skill.isOverhit())
		            {
		            	if(target instanceof L2Attackable)
		                        ((L2Attackable)target).overhitEnabled(true);
		            }

					if (activeWeapon != null && !target.isDead())
					{
						if (activeWeapon.getSkillEffects(this, target, skill).length > 0 && this instanceof L2PcInstance)
						{
							((L2PcInstance)this).sendMessage("Target affected by weapon special ability!");
						}
					}

					if (_chanceSkills != null)
						_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
					if (target.getChanceSkills() != null)
						target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
				}
			}
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);

			if (player != null)
			{
				for (L2Object target : targets)
				{
					if (target instanceof L2Character)
					{
						if (skill.isOffensive())
						{
							if (target instanceof L2PcInstance || target instanceof L2Summon)
							{
								if (skill.getSkillType() != L2Skill.SkillType.SIGNET && skill.getSkillType() != L2Skill.SkillType.SIGNET_CASTTIME)
								{
									if (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE
											&& skill.getSkillType() != L2Skill.SkillType.AGGREDUCE_CHAR
											&& skill.getSkillType() != L2Skill.SkillType.AGGREMOVE)
									{
										((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
									}
									player.updatePvPStatus((L2Character)target);
								}
							}
							else if (target instanceof L2Attackable)
							{
								if (skill.getSkillType() != L2Skill.SkillType.AGGREDUCE
										&& skill.getSkillType() != L2Skill.SkillType.AGGREDUCE_CHAR
										&& skill.getSkillType() != L2Skill.SkillType.AGGREMOVE)
								{
									((L2Character)target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
								}
							}
						}
						else
						{
							if (target instanceof L2PcInstance)
							{
								if (!target.equals(this) &&
										(((L2PcInstance)target).getPvpFlag() > 0 ||
												((L2PcInstance)target).getKarma() > 0)) player.updatePvPStatus();
							}
							else if (target instanceof L2Attackable 
									&& !(skill.getSkillType() == L2Skill.SkillType.SUMMON)
									&& !(skill.getSkillType() == L2Skill.SkillType.BEAST_FEED) 
									&& !(skill.getSkillType() == L2Skill.SkillType.UNLOCK)
									&& !(skill.getSkillType() == L2Skill.SkillType.DELUXE_KEY_UNLOCK))
								player.updatePvPStatus();
						}
					}
				}
				Collection<L2Object> objs = player.getKnownList().getKnownObjects().values();
				{
					for (L2Object spMob : objs)
					{
						if (spMob instanceof L2NpcInstance)
						{
							L2NpcInstance npcMob = (L2NpcInstance) spMob;
							
							if ((npcMob.isInsideRadius(player, 1000, true, true))
							        && (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null))
								for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
									quest.notifySkillSee(npcMob, player, skill, targets, this instanceof L2Summon);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill) {
		if (this instanceof L2Attackable)
			((L2Attackable)this).addDamageHate(caster, 0, -skill.getAggroPoints());
	}
	
	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public boolean isBehind(L2Object target)
	{
        double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;

        if(target == null)
			return false;

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
            angleChar = Util.calculateAngleFrom(target1, this);
            angleTarget = Util.convertHeadingToDegree(target1.getHeading());
            angleDiff = angleChar - angleTarget;
            if (angleDiff <= -360 + maxAngleDiff) angleDiff += 360;
            if (angleDiff >= 360 - maxAngleDiff) angleDiff -= 360;
            if (Math.abs(angleDiff) <= maxAngleDiff)
            {
                return true;
            }
		}
		else
		{
			_log.fine("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}

	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}

	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public boolean isFront(L2Object target)
	{
        double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;
        if(target == null)
			return false;
		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
            angleChar = Util.calculateAngleFrom(target1, this);
            angleTarget = Util.convertHeadingToDegree(target1.getHeading());
            angleDiff = angleChar - angleTarget;
            if (angleDiff <= -180 + maxAngleDiff) angleDiff += 180;
            if (angleDiff >= 180 - maxAngleDiff) angleDiff -= 180;
            if (Math.abs(angleDiff) <= maxAngleDiff)
            {
                return true;
            }
		}
		else
		{
			_log.fine("isSideTarget's target not an L2 Character.");
		}
		return false;
	}

	public boolean isFacing(L2Object target, int maxAngle)
    {
    	double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if(target == null)
			return false;
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(this.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -180 + maxAngleDiff) angleDiff += 180;
        if (angleDiff >= 180 - maxAngleDiff) angleDiff -= 180;
        if (Math.abs(angleDiff) <= maxAngleDiff)
			return true;
		return false;
    }

	public boolean isFrontTarget()
	{
		return isFront(getTarget());
	}
	/**
	 * Return 1.<BR><BR>
	 */
	public double getLevelMod()
	{
		return 1;
	}

	public final void setSkillCast(@SuppressWarnings("rawtypes") Future newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	public final void setSkillCastEndTime(int newSkillCastEndTime)
	{
		_castEndTime = newSkillCastEndTime;
		_castInterruptTime = newSkillCastEndTime-12; 
	}

	
	@SuppressWarnings("rawtypes")
	private Future _PvPRegTask;
	
	private long _pvpFlagLasts;

	private boolean _isMinion = false;
	
	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}

	public void stopPvpRegTask()
	{
		if (_PvPRegTask != null)
			_PvPRegTask.cancel(true);
	}

	public void stopPvPFlag()
	{
		stopPvpRegTask();

		updatePvPFlag(0);

		_PvPRegTask = null;
	}

	public void updatePvPFlag(int value) {
		if (!(this instanceof L2PcInstance))
			return;
		L2PcInstance player = (L2PcInstance)this;
		if (player.getPvpFlag() == value)
			return;
		player.setPvpFlag(value);
		player.sendPacket(new UserInfo(player));
		if (player.getPet() != null)
	    {
	      player.sendPacket(new PetInfo(player.getPet()));
	    }
	    for (L2PcInstance target : getKnownList().getKnownPlayers().values())
	    {
	      target.sendPacket(new RelationChanged(player, player.getRelation(player), player.isAutoAttackable(target)));
	      if (player.getPet() != null)
	      {
	        target.sendPacket(new RelationChanged(player.getPet(), player.getRelation(player), player.isAutoAttackable(target)));
	      }
	    }
	}

	public final int getRandomDamage(L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem == null)
			return 5+(int)Math.sqrt(getLevel());

		return weaponItem.getRandomDamage();
	}

	@Override
	public String toString()
	{
		return "mob "+getObjectId();
	}

	public int getAttackEndTime()
	{
		return _attackEndTime;
	}

	/**
	 * Not Implemented.<BR><BR>
	 */
	public abstract int getLevel();
	// =========================================================






	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill) { return getStat().calcStat(stat, init, target, skill); }

	// Property - Public
	public int getAccuracy() { return getStat().getAccuracy(); }
	public final float getAttackSpeedMultiplier() { return getStat().getAttackSpeedMultiplier(); }
	public int getCON() { return getStat().getCON(); }
	public int getDEX() { return getStat().getDEX(); }
	public final double getCriticalDmg(L2Character target, double init) { return getStat().getCriticalDmg(target, init); }
	public int getCriticalHit(L2Character target, L2Skill skill) { return getStat().getCriticalHit(target, skill); }
	public int getEvasionRate(L2Character target) { return getStat().getEvasionRate(target); }
	public int getINT() { return getStat().getINT(); }
	public final int getMagicalAttackRange(L2Skill skill) { return getStat().getMagicalAttackRange(skill); }
	public final int getMaxCp() { return getStat().getMaxCp(); }
	public int getMAtk(L2Character target, L2Skill skill) { return getStat().getMAtk(target, skill); }

	public int getMAtkSpd()
	{
		if (Config.MAX_MATK_SPEED > 0)
		{
			if (getStat().getMAtkSpd() > Config.MAX_MATK_SPEED)
				return Config.MAX_MATK_SPEED;
		}
		return getStat().getMAtkSpd();
	}

	public int getMaxMp() { return getStat().getMaxMp(); }
	public int getMaxHp() { return getStat().getMaxHp(); }
	public final int getMCriticalHit(L2Character target, L2Skill skill) { return getStat().getMCriticalHit(target, skill); }
	public int getMDef(L2Character target, L2Skill skill) { return getStat().getMDef(target, skill); }
	public int getMEN() { return getStat().getMEN(); }
	public double getMReuseRate(L2Skill skill) { return getStat().getMReuseRate(skill); }
	public float getMovementSpeedMultiplier() { return getStat().getMovementSpeedMultiplier(); }
	public int getPAtk(L2Character target) { return getStat().getPAtk(target); }
	public double getPAtkAnimals(L2Character target) { return getStat().getPAtkAnimals(target); }
	public double getPAtkDragons(L2Character target) { return getStat().getPAtkDragons(target); }
	public double getPAtkInsects(L2Character target) { return getStat().getPAtkInsects(target); }
	public double getPAtkMonsters(L2Character target) { return getStat().getPAtkMonsters(target); }
    public double getPAtkPlants(L2Character target) { return getStat().getPAtkPlants(target); }

	public int getPAtkSpd() 
	{
		if (Config.MAX_PATK_SPEED > 0)
		{
			if (getStat().getPAtkSpd() > Config.MAX_PATK_SPEED)
				return Config.MAX_PATK_SPEED;
		}
		return getStat().getPAtkSpd();
	}

	public double getPAtkUndead(L2Character target) { return getStat().getPAtkUndead(target); }
	public double getPDefUndead(L2Character target) { return getStat().getPDefUndead(target); }
	public int getPDef(L2Character target) { return getStat().getPDef(target); }
	public final int getPhysicalAttackRange() { return getStat().getPhysicalAttackRange(); }
	public int getRunSpeed() { return getStat().getRunSpeed(); }
	public final int getShldDef() { return getStat().getShldDef(); }
	public int getSTR() { return getStat().getSTR(); }
	public final int getWalkSpeed() { return getStat().getWalkSpeed(); }
	public int getWIT() { return getStat().getWIT(); }
	// =========================================================


	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(L2Character object) { getStatus().addStatusListener(object); }
	public void reduceCurrentHp(double i, L2Character attacker) { reduceCurrentHp(i, attacker, true); }
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if(this instanceof L2NpcInstance && (attacker instanceof L2PcInstance || attacker instanceof L2Summon))
            if(Config.INVUL_NPC_LIST.contains(Integer.valueOf(((L2NpcInstance)this).getNpcId())))
                return;
		if (Config.CHAMPION_ENABLE && isChampion() && Config.CHAMPION_HP != 0)
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake);
		else
			getStatus().reduceHp(i, attacker, awake);
	}
	public void reduceCurrentMp(double i) { getStatus().reduceMp(i); }
	public void removeStatusListener(L2Character object) { getStatus().removeStatusListener(object); }
	protected void stopHpMpRegeneration() { getStatus().stopHpMpRegeneration(); }

	// Property - Public
	public final double getCurrentCp() { return getStatus().getCurrentCp(); }
	public final void setCurrentCp(Double newCp) { setCurrentCp((double) newCp); }
	public final void setCurrentCp(double newCp) { getStatus().setCurrentCp(newCp); }
	public final double getCurrentHp() { return getStatus().getCurrentHp(); }
	public final void setCurrentHp(double newHp) { getStatus().setCurrentHp(newHp); }
	public final void setCurrentHpMp(double newHp, double newMp){ getStatus().setCurrentHpMp(newHp, newMp); }
	public final double getCurrentMp() { return getStatus().getCurrentMp(); }
	public final void setCurrentMp(Double newMp) { setCurrentMp((double)newMp); }
	public final void setCurrentMp(double newMp) { getStatus().setCurrentMp(newMp); }
	// =========================================================

	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}

	public String getAiClass()
	{
		return _aiClass;
	}

	public L2Character getLastBuffer()
	{
		return _lastBuffer;
	}

	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}

	public void setLastBuffer(L2Character buffer)
	{
		_lastBuffer = buffer;
	}

	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}

	public void setDestination(int x, int y, int z)
	{
		MoveData m = new MoveData();
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z;
	}

	public boolean reflectSkill(L2Skill skill)
	{
		double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, null);

		if (!skill.isMagic() && skill.getCastRange() < 100) // is 100 maximum range for melee skills?
		{
			double reflectMeleeSkill = calcStat(Stats.REFLECT_SKILL_MELEE_PHYSIC , 0 , null , null);
			reflect = (reflectMeleeSkill > reflect) ? reflectMeleeSkill : reflect;
		}

		return (Rnd.get(100) < reflect);
	}

	public boolean reflectDamageSkill(L2Skill skill)
    {
        double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_DAMAGE_MAGIC : Stats.REFLECT_DAMAGE_PHYSIC, 0.0, null, null);
        return (double)Rnd.get(100) < reflect;
    }

    public boolean reflectRevengeSkill(L2Skill skill)
    {
        double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_DAMAGE_MAGIC : Stats.REFLECT_REVENGE_SKILL, 0.0, null, null);
        return (double)Rnd.get(100) < reflect;
    }

	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}

	public ForceBuff getForceBuff()
	{
		return null;
	}
	
	public void setForceBuff(ForceBuff fb)
	{
	}
	
	public boolean isRaidMinion()
	{
	        return _isMinion ;
	}

	public void setIsRaidMinion(boolean val)
	{
	   	_isRaid = val;
	   	_isMinion = val;
	}
	private boolean _champion = false;
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	public boolean isChampion()
	{
		return _champion;
	}

	private int _premiumsystem;

    public void setPremiumService(int PS)
    {
    	_premiumsystem = PS;
    }

    public int getPremiumService()
    {
        return _premiumsystem;
	}
    
    public final void setIsBuffBlocked(boolean value)
    {
         _isBuffBlocked = value;
    }
    
    public boolean isBuffBlocked()
    {
         return _isBuffBlocked;	
    }
    
public void turn(L2Character target)
    {
    	double dx = (target.getX()-getX());
	double dy = (target.getY()-getY());
	double distance = Math.sqrt(dx * dx + dy * dy);
	double sin = dy / distance;
	double cos = dx / distance;
	int newHeading=Util.calculateHeadingFrom(cos, sin);
	if(getHeading()!=newHeading)
	{							
		broadcastPacket(new BeginRotation(getObjectId(), getHeading(), 1, 0));
		setHeading(newHeading);
    		broadcastPacket(new StopRotation(getObjectId(), newHeading, 0));
    	}
    }	
}
