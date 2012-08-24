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
package l2.hellknight.gameserver.model.actor;

import static l2.hellknight.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2.hellknight.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.WeakFastSet;

import l2.hellknight.Config;
import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.GeoData;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlEvent;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.ai.L2AttackableAI;
import l2.hellknight.gameserver.ai.L2CharacterAI;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.handler.ISkillHandler;
import l2.hellknight.gameserver.handler.SkillHandler;
import l2.hellknight.gameserver.instancemanager.DimensionalRiftManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.MapRegionManager;
import l2.hellknight.gameserver.instancemanager.MapRegionManager.TeleportWhereType;
import l2.hellknight.gameserver.instancemanager.TerritoryWarManager;
import l2.hellknight.gameserver.instancemanager.TownManager;
import l2.hellknight.gameserver.model.ChanceSkillList;
import l2.hellknight.gameserver.model.CharEffectList;
import l2.hellknight.gameserver.model.FusionSkill;
import l2.hellknight.gameserver.model.IChanceSkillTrigger;
import l2.hellknight.gameserver.model.L2AccessLevel;
import l2.hellknight.gameserver.model.L2CharPosition;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.L2WorldRegion;
import l2.hellknight.gameserver.model.Location;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2NpcWalkerInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.actor.instance.L2RiftInvaderInstance;
import l2.hellknight.gameserver.model.actor.knownlist.CharKnownList;
import l2.hellknight.gameserver.model.actor.position.CharPosition;
import l2.hellknight.gameserver.model.actor.stat.CharStat;
import l2.hellknight.gameserver.model.actor.status.CharStatus;
import l2.hellknight.gameserver.model.actor.templates.L2CharTemplate;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.model.effects.AbnormalEffect;
import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.effects.L2EffectType;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.model.itemcontainer.Inventory;
import l2.hellknight.gameserver.model.items.L2Item;
import l2.hellknight.gameserver.model.items.L2Weapon;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;
import l2.hellknight.gameserver.model.items.type.L2WeaponType;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.model.skills.L2SkillType;
import l2.hellknight.gameserver.model.skills.funcs.Func;
import l2.hellknight.gameserver.model.skills.l2skills.L2SkillAgathion;
import l2.hellknight.gameserver.model.skills.l2skills.L2SkillMount;
import l2.hellknight.gameserver.model.skills.l2skills.L2SkillSummon;
import l2.hellknight.gameserver.model.skills.targets.L2TargetType;
import l2.hellknight.gameserver.model.stats.BaseStats;
import l2.hellknight.gameserver.model.stats.Calculator;
import l2.hellknight.gameserver.model.stats.Formulas;
import l2.hellknight.gameserver.model.stats.Stats;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AbstractNpcInfo;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.Attack;
import l2.hellknight.gameserver.network.serverpackets.ChangeMoveType;
import l2.hellknight.gameserver.network.serverpackets.ChangeWaitType;
import l2.hellknight.gameserver.network.serverpackets.FlyToLocation;
import l2.hellknight.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillCanceld;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillLaunched;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.MoveToLocation;
import l2.hellknight.gameserver.network.serverpackets.Revive;
import l2.hellknight.gameserver.network.serverpackets.ServerObjectInfo;
import l2.hellknight.gameserver.network.serverpackets.SetupGauge;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.StatusUpdate;
import l2.hellknight.gameserver.network.serverpackets.StopMove;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.network.serverpackets.TeleportToLocation;
import l2.hellknight.gameserver.pathfinding.AbstractNodeLoc;
import l2.hellknight.gameserver.pathfinding.PathFinding;
import l2.hellknight.gameserver.scripting.scriptengine.events.AttackEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.DeathEvent;
import l2.hellknight.gameserver.scripting.scriptengine.events.SkillUseEvent;
import l2.hellknight.gameserver.scripting.scriptengine.listeners.character.AttackListener;
import l2.hellknight.gameserver.scripting.scriptengine.listeners.character.DeathListener;
import l2.hellknight.gameserver.scripting.scriptengine.listeners.character.SkillUseListener;
import l2.hellknight.gameserver.taskmanager.AttackStanceTaskManager;
import l2.hellknight.gameserver.util.L2TIntObjectHashMap;
import l2.hellknight.gameserver.util.Point3D;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

/**
 * Mother class of all character objects of the world (PC, NPC...)<br>
 * L2Character:<br>
 * <ul>
 * <li>L2DoorInstance</li>
 * <li>L2Playable</li>
 * <li>L2Npc</li>
 * <li>L2StaticObjectInstance</li>
 * <li>L2Trap</li>
 * <li>L2Vehicle</li>
 * </ul>
 * <br>
 * <b>Concept of L2CharTemplate:</b><br>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).<br>
 * All of those properties are stored in a different template for each type of L2Character.<br>
 * Each template is loaded once in the server cache memory (reduce memory use).<br>
 * When a new instance of L2Character is spawned, server just create a link between the instance and the template.<br>
 * This link is stored in {@link #_template}
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	public static final Logger _log = Logger.getLogger(L2Character.class.getName());
	
	private static List<DeathListener> globalDeathListeners = new FastList<DeathListener>().shared();
	private static List<SkillUseListener> globalSkillUseListeners = new FastList<SkillUseListener>().shared();
	private List<AttackListener> attackListeners = new FastList<AttackListener>().shared();
	private List<DeathListener> deathListeners = new FastList<DeathListener>().shared();
	private List<SkillUseListener> skillUseListeners = new FastList<SkillUseListener>().shared();
	
	private volatile Set<L2Character> _attackByList;
	private volatile boolean _isCastingNow = false;
	/*
	 * Buff protection
	 */
	private boolean _protected = false;
	
	private volatile boolean _isCastingSimultaneouslyNow = false;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	
	private boolean _isDead = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false; // the char is carrying too much
	private boolean _isParalyzed = false;
	private boolean _isPendingRevive = false;
	private boolean _isRunning = false;
	private boolean _isNoRndWalk = false; // Is no random walk
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	private boolean _isInvul = false;
	private boolean _isMortal = true; // Char will die when HP decreased to 0
	private boolean _isFlying = false;
	
	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template; // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _champion = false;

	/** Table of Calculators containing all used calculator */
	private Calculator[] _calculators;
	
	/**
	 * Map containing all skills of this character.
	 */
	private final FastMap<Integer, L2Skill> _skills = new FastMap<>();
	
	/**
	 * Map containing all custom skills of this character.
	 */
	private final FastMap<Integer, SkillHolder> _customSkills = new FastMap<>();
	
	/** FastMap containing the active chance skills on this character */
	private volatile ChanceSkillList _chanceSkills;
	
	/** Current force buff this caster is casting to a target */
	protected FusionSkill _fusionSkill;
	
	/** Zone system */
	public static final byte ZONE_PVP = 0;
	public static final byte ZONE_PEACE = 1;
	public static final byte ZONE_SIEGE = 2;
	public static final byte ZONE_MOTHERTREE = 3;
	public static final byte ZONE_CLANHALL = 4;
	public static final byte ZONE_LANDING = 5;
	public static final byte ZONE_NOLANDING = 6;
	public static final byte ZONE_WATER = 7;
	public static final byte ZONE_JAIL = 8;
	public static final byte ZONE_MONSTERTRACK = 9;
	public static final byte ZONE_CASTLE = 10;
	public static final byte ZONE_SWAMP = 11;
	public static final byte ZONE_NOSUMMONFRIEND = 12;
	public static final byte ZONE_FORT = 13;
	public static final byte ZONE_NOSTORE = 14;
	public static final byte ZONE_TOWN = 15;
	public static final byte ZONE_SCRIPT = 16;
	public static final byte ZONE_HQ = 17;
	public static final byte ZONE_DANGERAREA = 18;
	public static final byte ZONE_ALTERED = 19;
	public static final byte ZONE_NOBOOKMARK = 20;
	public static final byte ZONE_NOITEMDROP = 21;
	public static final byte ZONE_NORESTART = 22;
	
	/** Premium Service */
	private int _PremiumService;
	
	private final byte[] _zones = new byte[23];
	protected byte _zoneValidateCounter = 4;
	
	private L2Character _debugger = null;
	
	private final ReentrantLock _teleportLock;
	
	private int _team;
	/**
	 * @return True if debugging is enabled for this L2Character
	 */
	public boolean isDebug()
	{
		return _debugger != null;
	}
	
	/**
	 * Sets L2Character instance, to which debug packets will be send
	 * @param d
	 */
	public void setDebug(L2Character d)
	{
		_debugger = d;
	}
	
	/**
	 * Send debug packet.
	 * @param pkt
	 */
	public void sendDebugPacket(L2GameServerPacket pkt)
	{
		if (_debugger != null)
			_debugger.sendPacket(pkt);
	}
	
	/**
	 * Send debug text string
	 * @param msg
	 */
	public void sendDebugMessage(String msg)
	{
		if (_debugger != null)
			_debugger.sendMessage(msg);
	}
	
	/**
	 * @return character inventory, default null, overridden in L2Playable types and in L2NPcInstance
	 */
	public Inventory getInventory()
	{
		return null;
	}
	
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}
	
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}
	
	/**
	 * 
	 * @param zone
	 * @return
	 */
	public final boolean isInsideZone(final byte zone)
	{
		Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
		switch (zone)
		{
			case ZONE_PVP:
				if (instance != null && instance.isPvPInstance())
					return true;
				return _zones[ZONE_PVP] > 0 && _zones[ZONE_PEACE] == 0;
			case ZONE_PEACE:
				if (instance != null && instance.isPvPInstance())
					return false;
		}
		return _zones[zone] > 0;
	}
	
	/**
	 * 
	 * @param zone
	 * @param state
	 */
	public final void setInsideZone(final byte zone, final boolean state)
	{
		if (state)
			_zones[zone]++;
		else
		{
			_zones[zone]--;
			if (_zones[zone] < 0)
				_zones[zone] = 0;
		}
	}
	
	/**
	 * This will return true if the player is transformed,<br>
	 * but if the player is not transformed it will return false.
	 * @return transformation status
	 */
	public boolean isTransformed()
	{
		return false;
	}
	
	/**
	 * This will untransform a player if they are an instance of L2Pcinstance
	 * and if they are transformed.
	 */
	public void untransform()
	{
		// Just a place holder
	}
	
	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 * @return GM status
	 */
	public boolean isGM()
	{
		return false;
	}
	
	/**
	 * Overridden in L2PcInstance.
	 * @return the access level.
	 */
	public L2AccessLevel getAccessLevel()
	{
		return null;
	}
	
	public void setProtectedPlayer(boolean prot)
    {
    	if (prot == true)
    	{
    		_protected = true;
    	}
    	else
    	{
    		_protected = false;
    	}
    }
    
    public boolean isProtected()
    {
    	return _protected;
    }

	/**
	 * Constructor of L2Character.<br>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the character can take more items)</li><BR><BR>
	 *
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR><BR>
	 *
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		setInstanceType(InstanceType.L2Character);
		initCharStat();
		initCharStatus();
		
		_skills.shared();
		_customSkills.shared();
		
		// Set its template to the new L2Character
		_template = template;
		
		if (isDoor())
		{
			_calculators = Formulas.getStdDoorCalculators();
		}
		else if (template != null && isNpc())
		{
			// Copy the Standard Calculators of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			if (template.getSkills() != null)
			{
				_skills.putAll(template.getSkills());
			}
			
			if (!_skills.isEmpty())
			{
				for (L2Skill skill : getAllSkills())
				{
					if (skill.getDisplayId() != skill.getId())
					{
						_customSkills.put(Integer.valueOf(skill.getDisplayId()), new SkillHolder(skill));
					}
					addStatFuncs(skill.getStatFuncs(null, this));
				}
			}
		}
		else
		{
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			
			if (isSummon())
			{
				// Copy the skills of the L2Summon from its template to the L2Character Instance
				// The skills list can be affected by spell effects so it's necessary to make a copy
				// to avoid that a spell affecting a L2Summon, affects others L2Summon of the same type too.
				if (template != null)
				{
					_skills.putAll(((L2NpcTemplate) template).getSkills());
				}
				if (!_skills.isEmpty())
				{
					for (L2Skill skill : getAllSkills())
					{
						if (skill.getDisplayId() != skill.getId())
						{
							_customSkills.put(Integer.valueOf(skill.getDisplayId()), new SkillHolder(skill));
						}
						addStatFuncs(skill.getStatFuncs(null, this));
					}
				}
			}
			
			Formulas.addFuncsToNewCharacter(this);
		}
		
		setIsInvul(true);
		_teleportLock = new ReentrantLock();
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateIncCheck = getMaxVisibleHp();
		_hpUpdateInterval = _hpUpdateIncCheck / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateDecCheck = _hpUpdateIncCheck - _hpUpdateInterval;
	}
	
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
			reg.removeFromZones(this);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		this.revalidateZone(true);
	}
	
	public void onTeleported()
	{
		if (!_teleportLock.tryLock())
			return;
		try
		{
			if (!isTeleporting())
				return;
			spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
			setIsTeleporting(false);
		}
		finally
		{
			_teleportLock.unlock();
		}
		if (_isPendingRevive)
			doRevive();
	}
	
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 * @param player The L2Character that attacks this one
	 * 
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Add to list only for attackables, not all other NPCs</li><BR><BR>
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		// DS: moved to L2Attackable
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 * @param mov 
	 */
	public void broadcastPacket(L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null)
				player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 * @param mov 
	 * @param radiusInKnownlist 
	 */
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null && isInsideRadius(player, radiusInKnownlist, false, false))
				player.sendPacket(mov);
		}
	}
	
	/**
	 * @param barPixels 
	 * @return true if hp update should be done, false if not
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		double maxHp = getMaxVisibleHp();
		
		if (currentHp <= 1.0 || maxHp < barPixels)
			return true;
		
		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			// TODO: (Zoey76) This is mathematically wrong.
			// for example 2.2d + 1.1d == 3.3d will return false
			// Also Double.compare(2.2d + 1.1d, 3.3d) will return 1,
			// meaning the second parameter is greater than the first.
			if (currentHp == maxHp)
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * L2Character called _statusListener that must be informed of HP/MP updates of this L2Character </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR><BR>
	 *
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
			return;
		
		if (!needHpUpdate(352))
			return;
		
		if (Config.DEBUG)
			_log.fine("Broadcast Status Update for " + getObjectId() + "(" + getName() + "). HP: " + getCurrentHp());
		
		// Create the Server->Client packet StatusUpdate with current HP
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		
		for (L2Character temp : getStatus().getStatusListener())
		{
			if (temp != null)
				temp.sendPacket(su);
		}
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @param text 
	 */
	public void sendMessage(String text)
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
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @param x 
	 * @param y 
	 * @param z 
	 * @param heading 
	 * @param randomOffset 
	 */
	public void teleToLocation(int x, int y, int z, int heading, int randomOffset)
	{
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.OFFSET_ON_TELEPORT_ENABLED && randomOffset > 0)
		{
			x += Rnd.get(-randomOffset, randomOffset);
			y += Rnd.get(-randomOffset, randomOffset);
		}
		
		z += 5;
		
		if (Config.DEBUG)
			_log.fine("Teleporting to: " + x + ", " + y + ", " + z);
		
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z, heading));
		
		// remove the object from its old location
		decayMe();
		
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);
		
		// temporary fix for heading on teleports
		if (heading != 0)
			getPosition().setHeading(heading);
		
		// allow recall of the detached characters
		if (!isPlayer() || ((getActingPlayer().getClient() != null) && getActingPlayer().getClient().isDetached()))
			onTeleported();
		
		revalidateZone(true);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getHeading(), 0);
	}
	
	public void teleToLocation(int x, int y, int z, int randomOffset)
	{
		teleToLocation(x, y, z, getHeading(), randomOffset);
	}
	
	public void teleToLocation(Location loc, int randomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if (isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) // true -> ignore waiting room :)
		{
			L2PcInstance player = getActingPlayer();
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		teleToLocation(x, y, z, getHeading(), randomOffset);
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		if (allowRandomOffset)
			teleToLocation(loc, Config.MAX_OFFSET_ON_TELEPORT);
		else
			teleToLocation(loc, 0);
	}
	
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (allowRandomOffset)
			teleToLocation(x, y, z, Config.MAX_OFFSET_ON_TELEPORT);
		else
			teleToLocation(x, y, z, 0);
	}
	
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if (allowRandomOffset)
			teleToLocation(x, y, z, heading, Config.MAX_OFFSET_ON_TELEPORT);
		else
			teleToLocation(x, y, z, heading, 0);
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the active weapon (always equipped in the right hand) </li><BR><BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use </li><BR><BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	protected void doAttack(L2Character target)
	{
		if (target == null)
			return;
		
		if (isAttackingDisabled())
			return;
		
		if(!fireAttackListeners(target))
		{
			return;
		}
		if (Config.DEBUG)
			_log.fine(getName() + " doAttack: target=" + target);
		
		if (!isAlikeDead())
		{
			if (isNpc() && target.isAlikeDead() || !getKnownList().knowsObject(target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			else if (isPlayer())
			{
				if (target.isDead())
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				L2PcInstance actor = getActingPlayer();
				/*
				 * Players riding wyvern or with special (flying) transformations can do melee attacks, only with skills
				 */
				if ((actor.isMounted() && actor.getMountNpcId() == 12621) || (actor.isTransformed() && !actor.getTransformation().canDoMeleeAttack()))
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		
		// Check if attacker's weapon can attack
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wpn = getActiveWeaponItem();
			if (!wpn.isAttackWeapon() && !isGM())
			{
				if (wpn.getItemType() == L2WeaponType.FISHINGROD)
					sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
				else
					sendPacket(SystemMessageId.THAT_WEAPON_CANT_ATTACK);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		if (getActingPlayer() != null)
		{
			if (getActingPlayer().inObserverMode())
			{
				sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			else if (target.getActingPlayer() != null && getActingPlayer().getSiegeState() > 0 && isInsideZone(L2Character.ZONE_SIEGE) && target.getActingPlayer().getSiegeState() == getActingPlayer().getSiegeState() && target.getActingPlayer() != this && target.getActingPlayer().getSiegeSide() == getActingPlayer().getSiegeSide())
			{
				if (TerritoryWarManager.getInstance().isTWInProgress())
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				else
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Checking if target has moved to peace zone
			else if (target.isInsidePeaceZone(getActingPlayer()))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		stopEffectsOnAction();
		
		// Get the active weapon instance (always equipped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// BOW and CROSSBOW checks
		if (weaponItem != null && !isTransformed())
		{
			if (weaponItem.getItemType() == L2WeaponType.BOW)
			{
				//Check for arrows and MP
				if (isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-bow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcInstance then return True
					if (!checkAndEquipArrows())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_ARROWS);
						return;
					}
					
					// Verify if the bow can be use
					if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if (weaponItem.getReducedMpConsume() > 0 && Rnd.get(100) < weaponItem.getReducedMpConsumeChance())
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						// If L2PcInstance have enough MP, the bow consumes it
						if (mpConsume > 0)
							getStatus().reduceMp(mpConsume);
						
						// Set the period of bow no re-use
						_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
			if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
			{
				//Check for bolts
				if (isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					
					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipBolts())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(SystemMessageId.NOT_ENOUGH_BOLTS);
						return;
					}
					
					// Verify if the crossbow can be use
					if (_disableCrossBowAttackEndTime <= GameTimeController.getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if (weaponItem.getReducedMpConsume() > 0 && Rnd.get(100) < weaponItem.getReducedMpConsumeChance())
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);
						
						if (getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						
						// If L2PcInstance have enough MP, the bow consumes it
						if (mpConsume > 0)
							getStatus().reduceMp(mpConsume);
						
						// Set the period of crossbow no re-use
						_disableCrossBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if (isNpc())
				{
					if (_disableCrossBowAttackEndTime > GameTimeController.getGameTicks())
						return;
				}
			}
		}
		
		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
			setCurrentCp(getCurrentCp() - 10);
		
		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (isPlayer() || isSummon())
			getActingPlayer().rechargeAutoSoulShot(true, false, isSummon());
		
		// Verify if soulshots are charged.
		boolean wasSSCharged;
		
		if (isSummon() && !(isPet() && weaponInst != null))
			wasSSCharged = (((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
		else
			wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);
		
		if (isL2Attackable())
		{
			if (((L2Npc) this).useSoulShot())
			{
				wasSSCharged = true;
			}
		}
		
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		int timeToHit = timeAtk / 2;
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (timeAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
		
		int ssGrade = 0;
		
		if (weaponItem != null)
			ssGrade = weaponItem.getItemGradeSPlus();
		
		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, target, wasSSCharged, ssGrade);
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		setHeading(Util.calculateHeadingFrom(this, target));
		
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		boolean hitted;
		// Select the type of attack to start
		if (weaponItem == null || isTransformed())
			hitted = doAttackHitSimple(attack, target, timeToHit);
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		else if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
			hitted = doAttackHitByPole(attack, target, timeToHit);
		else if (isUsingDualWeapon())
			hitted = doAttackHitByDual(attack, target, timeToHit);
		else
			hitted = doAttackHitSimple(attack, target, timeToHit);
		
		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = getActingPlayer();
		
		if (player != null)
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
		
		if (player != null)
		{
			if (player.getPet() != target)
				player.updatePvPStatus(target);
		}
		// Check if hit isn't missed
		if (!hitted)
		{
			abortAttack(); // Abort the attack of the L2Character and send Server->Client ActionFailed packet
		}
		else
		{
			/* ADDED BY nexus - 2006-08-17
			 *
			 * As soon as we know that our hit landed, we must discharge any active soulshots.
			 * This must be done so to avoid unwanted soulshot consumption.
			 */

			// If we didn't miss the hit, discharge the shoulshots, if any
			if (isSummon() && !(isPet() && weaponInst != null))
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			else if (weaponInst != null)
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if (player.isHero())
				{
					// If a cursed weapon is hit by a Hero, Cp is reduced to 0
					if (target.isPlayer() && target.getActingPlayer().isCursedWeaponEquipped())
					{
						target.setCurrentCp(0);
					}
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits())
			broadcastPacket(attack);
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}
	
	/**
	 * Launch a Bow attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse 
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume arrows
		reduceArrowCount(false);
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
			
			double range = Math.sqrt(this.getDistanceSq(target));
			if (range < 1600)
			{
				double dmgmod = 1 - (1 - range / 800) * 0.35;
				
				if (dmgmod > 1)
					dmgmod = 2 - dmgmod;
				if (dmgmod > 0)
					damage1 *= dmgmod;
			}
			else
				damage1 *= 0.65;
		}
		
		// Check if the L2Character is a L2PcInstance
		if (isPlayer())
		{
			// Send a system message
			sendPacket(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW);
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a CrossBow attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume bolts </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the crossbow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse 
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume bolts
		reduceArrowCount(true);
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}
		
		// Check if the L2Character is a L2PcInstance
		if (isPlayer())
		{
			// Send a system message
			sendPacket(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE);
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		if (isL2Attackable())
		{
			if (((L2Attackable) this)._soulshotcharged)
			{
				// Create a new hit task with Medium priority
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk);
			}
			else
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk);
		}
		else
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableCrossBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk 
	 *
	 * @return True if hit 1 or hit 2 isn't missed
	 *
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}
		
		if (isL2Attackable())
		{
			if (((L2Attackable) this)._soulshotcharged)
			{
				
				// Create a new hit task with Medium priority for hit 1
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk / 2);
				
				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, true, shld2), sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk / 2);
				
				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, false, shld2), sAtk);
			}
		}
		else
		{
			// Create a new hit task with Medium priority for hit 1
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);
			
			// Create a new hit task with Medium priority for hit 2 with a higher delay
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);
		}
		
		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get all visible objects in a spherical area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attackable, launch a simple attack against it </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target 
	 * @param sAtk 
	 *
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		//double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		if (Config.DEBUG)
		{
			_log.info("doAttackHitByPole: Max radius = " + maxRadius);
			_log.info("doAttackHitByPole: Max angle = " + maxAngleDiff);
		}
		
		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		//          o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2          / (heading)
		//            /
		//          o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree
		
		// Get char's heading degree
		// angleChar = Util.convertHeadingToDegree(getHeading());
		// In H5 ATTACK_COUNT_MAX 1 is by default and 2 was in skill 3599, total 3.
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null) - 1;
		int attackcount = 0;
		
		/*if (angleChar <= 0)
		    angleChar += 360;*/
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		Collection<L2Object> objs = getKnownList().getKnownObjects().values();
		
		for (L2Object obj : objs)
		{
			if (obj == target)
				continue; // do not hit twice
			// Check if the L2Object is a L2Character
			if (obj instanceof L2Character)
			{
				if (obj.isPet() && isPlayer() && ((L2PetInstance) obj).getOwner() == getActingPlayer())
					continue;
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
					continue;
				
				// otherwise hit too high/low. 650 because mob z coord
				// sometimes wrong on hills
				if (Math.abs(obj.getZ() - getZ()) > 650)
					continue;
				if (!isFacing(obj, maxAngleDiff))
					continue;
				
				if (isL2Attackable() && obj.isPlayer() && getTarget().isL2Attackable())
					continue;
				
				if (isL2Attackable() && obj.isL2Attackable() && ((L2Attackable) this).getEnemyClan() == null && ((L2Attackable) this).getIsChaos() == 0)
					continue;
				
				if (isL2Attackable() && obj.isL2Attackable() && !((L2Attackable) this).getEnemyClan().equals(((L2Attackable) obj).getClan()) && ((L2Attackable) this).getIsChaos() == 0)
					continue;
				
				temp = (L2Character) obj;
				
				// Launch a simple attack against the L2Character targeted
				if (!temp.isAlikeDead())
				{
					if (temp == getAI().getAttackTarget() || temp.isAutoAttackable(this))
					{
						hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
						attackpercent /= 1.15;
						
						attackcount++;
						if (attackcount > attackRandomCountMax)
							break;
					}
				}
			}
		}
		
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk 
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null), false, target);
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
			
			if (attackpercent != 100)
				damage1 = (int) (damage1 * attackpercent / 100);
		}
		
		// Create a new hit task with Medium priority
		if (isL2Attackable())
		{
			if (((L2Attackable) this)._soulshotcharged)
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk);
			}
			else
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk);
			
		}
		else
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Verify the possibility of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to display casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}
	
	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}
	
	public void doCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingNow(false);
			return;
		}
		
		// Override casting type
		if (skill.isSimultaneousCast())
		{
			doSimultaneousCast(skill, target, targets);
			return;
		}
		
		stopEffectsOnAction();
		
		// Recharge AutoSoulShot
		// this method should not used with L2Playable
		
		beginCast(skill, false, target, targets);
	}
	
	public void doSimultaneousCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if (!checkDoCastConditions(skill))
		{
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		stopEffectsOnAction();
		
		// Recharge AutoSoulShot
		// this method should not used with L2Playable
		
		beginCast(skill, true, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			if (isPlayer())
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}

		// Override casting type
		if (skill.isSimultaneousCast() && !simultaneously)
			simultaneously = true;
		
		stopEffectsOnAction();
		
		//Recharge AutoSoulShot
		if (isPlayer() || isSummon())
			getActingPlayer().rechargeAutoSoulShot(skill.useSoulShot(), skill.useSpiritShot(), isSummon());
				
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);
		
		boolean doit = false;
		
		// AURA skills should always be using caster as target
		switch (skill.getTargetType())
		{
			case TARGET_AREA_SUMMON: // We need it to correct facing
				target = getPet();
				break;
			case TARGET_AURA:
			case TARGET_AURA_CORPSE_MOB:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
				target = this;
				break;
			case TARGET_SELF:
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_OWNER_PET:
			case TARGET_PARTY:
			case TARGET_CLAN:
			case TARGET_PARTY_CLAN:
			case TARGET_ALLY:
				doit = true;
			default:
				if (targets.length == 0)
				{
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					if (isPlayer())
					{
						sendPacket(ActionFailed.STATIC_PACKET);
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
					case HEAL:
					case COMBATPOINTHEAL:
					case MANAHEAL:
						doit = true;
						break;
					default:
						break;
				}
				
				/*
				 * handled on first switch
				switch (skill.getTargetType())
				{
					case TARGET_SELF:
					case TARGET_PET:
					case TARGET_SUMMON:
					case TARGET_PARTY:
					case TARGET_CLAN:
					case TARGET_ALLY:
						doit = true;
						break;
				}*/

				if (doit)
				{
					target = (L2Character) targets[0];
				}
				else
				{
					target = (L2Character) getTarget();
				}
		}
		beginCast(skill, simultaneously, target, targets);
	}
		
	private void beginCast(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if (!fireSkillCastListeners(skill, simultaneously, target, targets))
		{
			return;
		}
		if (target == null)
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		if (skill.getSkillType() == L2SkillType.RESURRECT)
		{
			if (isResurrectionBlocked() || target.isResurrectionBlocked())
			{
				sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection
				target.sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection
				
				if (simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
				
				if (isPlayer())
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
				}
				return;
			}
		}
		
		// Get the Identifier of the skill
		int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		
		if (level < 1)
			level = 1;
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		
		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting)
		{
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
		}
		
		int shotSave = L2ItemInstance.CHARGED_NONE;
		
		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.isMagic() && !effectWhileCasting)
			{
				if (weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
				{
					// Using SPS/BSPS Casting Time of Magic Skills is reduced in 40%
					hitTime = (int) (0.60 * hitTime);
					coolTime = (int) (0.60 * coolTime);
				}
			}
			
			// Save shots value for repeats
			if (skill.useSoulShot())
				shotSave = weaponInst.getChargedSoulshot();
			else if (skill.useSpiritShot())
				shotSave = weaponInst.getChargedSpiritshot();
		}
		
		if (isNpc())
		{
			// Using SPS/BSPS Casting Time of Magic Skills is reduced in 40%
			if (((L2Npc) this).useSpiritShot())
			{
				hitTime = (int) (0.60 * hitTime);
				coolTime = (int) (0.60 * coolTime);
			}
		}
		
		// if skill is static
		if (skill.isStatic())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		// if basic hitTime is higher than 500 than the min hitTime is 500
		else if (skill.getHitTime() >= 500 && hitTime < 500)
			hitTime = 500;
		
		// queue herbs and potions
		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}
		
		// Set the _castInterruptTime and casting status (L2PcInstance already has this true)
		if (simultaneously)
			setIsCastingSimultaneouslyNow(true);
		else
			setIsCastingNow(true);
		// Note: _castEndTime = GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		if (!simultaneously)
		{
			_castInterruptTime = -2 + GameTimeController.getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
			setLastSkillCast(skill);
		}
		else
			setLastSimultaneousSkillCast(skill);
		
		// Calculate the Reuse Time of the Skill
		int reuseDelay;
		if (skill.isStaticReuse() || skill.isStatic())
		{
			reuseDelay = (skill.getReuseDelay());
		}
		else if (skill.isMagic())
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.MAGIC_REUSE_RATE, 1, null, null));
		}
		else
		{
			reuseDelay = (int) (skill.getReuseDelay() * calcStat(Stats.P_REUSE, 1, null, null));
		}
				
		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		
		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery)
			addTimeStamp(skill, reuseDelay);
		
		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
				
				if (getActingPlayer() != null)
				{
					getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
				}
			}
			
			disableSkill(skill, reuseDelay);
		}
		
		// Make sure that char is facing selected target
		if (target != this)
			setHeading(Util.calculateHeadingFrom(this, target));
		
		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					
					if (isPlayer())
						getAI().setIntention(AI_INTENTION_ACTIVE);
					return;
				}
			}
			
			// Consume Souls if necessary
			if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
			{
				if (isPlayer())
				{
					if (!getActingPlayer().decreaseSouls(skill.getSoulConsumeCount(), skill))
					{
						if (simultaneously)
							setIsCastingSimultaneouslyNow(false);
						else
							setIsCastingNow(false);
						return;
					}
				}
			}
			
			if (skill.getSkillType() == L2SkillType.FUSION)
				startFusionSkill(target, skill);
			else
				callSkill(skill, targets);
		}
		
		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay));
		
		// Send a system message USE_S1 to the L2Character
		if (isPlayer() && magicId != 1312)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
			sm.addSkillName(skill);
			sendPacket(sm);
		}
		
		if (isPlayable())
		{
			if (!effectWhileCasting && skill.getItemConsumeId() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}
			}
			
			//reduce talisman mana on skill use
			if (skill.getReferenceItemId() > 0 && ItemTable.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO)
			{
				for (L2ItemInstance item : getInventory().getItemsByItemId(skill.getReferenceItemId()))
				{
					if (item.isEquipped())
					{
						item.decreaseMana(false, item.useSkillDisTime());
						break;
					}
				}
			}
		}
		
		// Before start AI Cast Broadcast Fly Effect is Need
		if (skill.getFlyType() != null)
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, target, skill), 50);
		
		MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously, shotSave);
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 410)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (isPlayer() && !effectWhileCasting)
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			
			if (skill.getHitCounts() > 0)
			{
				hitTime = hitTime * skill.getHitTimings()[0] / 100;
				
				if (hitTime < 410)
					hitTime = 410;
			}
			
			if (effectWhileCasting)
				mut.phase = 2;
			
			if (simultaneously)
			{
				Future<?> future = _skillCast2;
				if (future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
			else
			{
				Future<?> future = _skillCast;
				if (future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
		}
		else
		{
			mut.hitTime = 0;
			onMagicLaunchedTimer(mut);
		}
	}
	
	/**
	 * Check if casting of skill is possible
	 * @param skill
	 * @return True if casting is possible
	 */
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (skill == null || isSkillDisabled(skill))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!skill.isStatic()) // Skill mute checks.
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic())
			{
				if (isMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else
			{
				// Check if the skill is physical and if the L2Character is not physical_muted
				if (isPhysicalMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (isPhysicalAttackMuted()) // Prevent use attack
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// prevent casting signets to peace zone
		if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
				return false;
			boolean canCast = true;
			if (skill.getTargetType() == L2TargetType.TARGET_GROUND && isPlayer())
			{
				Point3D wp = getActingPlayer().getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
					canCast = false;
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
				canCast = false;
			if (!canCast)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				return false;
			}
		}
		
		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster's weapon is limited to use only its own skills
		if (getActiveWeaponItem() != null)
		{
			L2Weapon wep = getActiveWeaponItem();
			if (wep.useWeaponSkillsOnly() && !isGM() && wep.hasSkills())
			{
				boolean found = false;
				for (SkillHolder sh : wep.getSkills())
				{
					if (sh.getSkillId() == skill.getId())
					{
						found = true;
					}
				}
				
				if (!found)
				{
					if (getActingPlayer() != null)
						sendPacket(SystemMessageId.WEAPON_CAN_USE_ONLY_WEAPON_SKILL);
					return false;
				}
			}
		}
		
		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if (skill.getItemConsumeId() > 0 && getInventory() != null)
		{
			// Get the L2ItemInstance consumed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				}
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Index according to skill id the current timestamp of use.<br><br>
	 *
	 * @param skill id
	 * @param reuse delay
	 * <BR><B>Overridden in :</B>  (L2PcInstance)
	 */
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		/***/
	}
	
	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FUSION)
			return;
		
		if (_fusionSkill == null)
			_fusionSkill = new FusionSkill(this, target, skill);
	}
	
	/**
	 * Kill the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 * @return false if the player is already dead.
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
				return false;
			// now reset currentHp to zero
			setCurrentHp(0);
			setIsDead(true);
		}
		if (!fireDeathListeners(killer))
		{
			return false;
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
		{
			if (((L2Playable) this).getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
				((L2Playable) this).stopCharmOfLuck(null);
			if (((L2Playable) this).isNoblesseBlessed())
				((L2Playable) this).stopNoblesseBlessing(null);
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if (isPlayable() && ((L2Playable) this).isNoblesseBlessed())
		{
			((L2Playable) this).stopNoblesseBlessing(null);
			
			if (((L2Playable) this).getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
				((L2Playable) this).stopCharmOfLuck(null);
		}
		else
			stopAllEffectsExceptThoseThatLastThroughDeath();
		
		if (isPlayer() && getActingPlayer().getAgathionId() != 0)
			getActingPlayer().setAgathionId(0);
		calculateRewards(killer);
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		if (hasAI())
			getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		
		if (getWorldRegion() != null)
			getWorldRegion().onDeath(this);
		
		getAttackByList().clear();
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if (isSummon())
		{
			if (((L2Summon) this).isPhoenixBlessed() && ((L2Summon) this).getOwner() != null)
				((L2Summon) this).getOwner().reviveRequest(((L2Summon) this).getOwner(), null, true);
		}
		if (isPlayer())
		{
			if (((L2Playable) this).isPhoenixBlessed())
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			else if (isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) && getActingPlayer().isInSiege())
			{
				getActingPlayer().reviveRequest(getActingPlayer(), null, false);
			}
		}
		try
		{
			if (_fusionSkill != null)
				abortCast();
			
			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "deleteMe()", e);
		}
		return true;
	}
	
	public void deleteMe()
	{
		setDebug(null);
		
		if (hasAI())
			getAI().stopAITask();
	}
	
	protected void calculateRewards(L2Character killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isDead())
			return;
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);
			boolean restorefull = false;
			
			if (isPlayable() && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				((L2Playable) this).stopPhoenixBlessing(null);
			}
			if (restorefull)
			{
				_status.setCurrentCp(getCurrentCp()); // this is not confirmed, so just trigger regeneration
				_status.setCurrentHp(getMaxHp()); // confirmed
				_status.setCurrentMp(getMaxMp()); // and also confirmed
			}
			else
			{
				if (Config.RESPAWN_RESTORE_CP > 0 && getCurrentCp() < getMaxCp() * Config.RESPAWN_RESTORE_CP)
					_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				if (Config.RESPAWN_RESTORE_HP > 0 && getCurrentHp() < getMaxHp() * Config.RESPAWN_RESTORE_HP)
					_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				if (Config.RESPAWN_RESTORE_MP > 0 && getCurrentMp() < getMaxMp() * Config.RESPAWN_RESTORE_MP)
					_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}
			// Start broadcast status
			broadcastPacket(new Revive(this));
			if (getWorldRegion() != null)
				getWorldRegion().onRevive(this);
		}
		else
			setIsPendingRevive(true);
	}
	
	/**
	 * Revives the L2Character using skill. 
	 * @param revivePower
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	/**
	 * @return the L2CharacterAI of the L2Character and if its null create a new one.
	 */
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2CharacterAI(new AIAccessor());
				return _ai;
			}
		}
		return ai;
	}
	
	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
			((L2AttackableAI) oldAI).stopAITask();
		_ai = newAI;
	}
	
	/**
	 * @return True if the L2Character has a L2CharacterAI.
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * @return True if the L2Character is RaidBoss or his minion.
	 */
	public boolean isRaid()
	{
		return false;
	}
	
	/**
	 * @return True if the L2Character is minion.
	 */
	public boolean isMinion()
	{
		return false;
	}
	
	/**
	 * @return True if the L2Character is minion of RaidBoss.
	 */
	public boolean isRaidMinion()
	{
		return false;
	}
	
	/**
	 * @return a list of L2Character that attacked.
	 */
	public final Set<L2Character> getAttackByList()
	{
		if (_attackByList == null)
		{
			synchronized (this)
			{
				if (_attackByList == null)
				{
					_attackByList = new WeakFastSet<>(true);
				}
			}
		}
		return _attackByList;
	}
	
	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}
	
	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}
	
	public final void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}
	
	public final boolean isAfraid()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_FEAR);
	}
	
	/**
	 * @return True if the L2Character can't use its skills (ex : stun, sleep...).
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed();
	}
	
	/**
	 * @return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyze, attackMute).
	 */
	public boolean isAttackingDisabled()
	{
		return isFlying() || isStunned() || isSleeping() || _attackEndTime > GameTimeController.getGameTicks() || isAlikeDead() || isParalyzed() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}
	
	public final Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final boolean isConfused()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_CONFUSED);
	}
	
	/**
	 * @return True if the L2Character is dead or use fake death.
	 */
	public boolean isAlikeDead()
	{
		return _isDead;
	}
	
	/**
	 * @return True if the L2Character is dead.
	 */
	public final boolean isDead()
	{
		return _isDead;
	}
	
	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}
	
	public boolean isImmobilized()
	{
		return _isImmobilized;
	}
	
	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}
	
	public final boolean isMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_MUTED);
	}
	
	public final boolean isPhysicalMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_PSYCHICAL_MUTED);
	}
	
	public final boolean isPhysicalAttackMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_PSYCHICAL_ATTACK_MUTED);
	}
	
	/**
	 * @return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 */
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isAlikeDead() || isTeleporting();
	}
	
	/**
	 * @return True if the L2Character can not be controlled by the player (confused, afraid).
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}
	
	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item). 
	 * @param value
	 */
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _isParalyzed || isAffected(CharEffectList.EFFECT_FLAG_PARALYZED);
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}
	
	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}
	
	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public final boolean isDisarmed()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_DISARMED);
	}
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return the L2Summon of the L2Character.
	 */
	public L2Summon getPet()
	{
		return null;
	}
	
	public final boolean isRooted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_ROOTED);
	}
	
	/**
	 * @return True if the L2Character is running.
	 */
	public boolean isRunning()
	{
		return _isRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getRunSpeed() != 0)
			broadcastPacket(new ChangeMoveType(this));
		if (isPlayer())
			getActingPlayer().broadcastUserInfo();
		else if (isSummon())
		{
			((L2Summon) this).broadcastStatusUpdate();
		}
		else if (isNpc())
		{
			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if (player == null)
					continue;
				
				else if (getRunSpeed() == 0)
					player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
				else
					player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
			}
		}
	}
	
	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning()
	{
		if (!isRunning())
			setIsRunning(true);
	}
	
	public final boolean isSleeping()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_SLEEP);
	}
	
	public final boolean isStunned()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_STUNNED);
	}
	
	public final boolean isBetrayed()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BETRAYED);
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	public void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || isAffected(CharEffectList.EFFECT_FLAG_INVUL);
	}
	
	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}
	
	public boolean isMortal()
	{
		return _isMortal;
	}
	
	public boolean isUndead()
	{
		return false;
	}
	
	public boolean isResurrectionBlocked()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BLOCK_RESURRECTION);
	}
	
	public final boolean isFlying()
	{
		return _isFlying;
	}
	
	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}
	
	@Override
	public CharKnownList getKnownList()
	{
		return ((CharKnownList) super.getKnownList());
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new CharKnownList(this));
	}
	
	public CharStat getStat()
	{
		return _stat;
	}
	
	/**
	 * Initializes the CharStat class of the L2Object,
	 * is overwritten in classes that require a different CharStat Type.
	 * 
	 * Removes the need for instanceof checks.
	 */
	public void initCharStat()
	{
		_stat = new CharStat(this);
	}
	
	public final void setStat(CharStat value)
	{
		_stat = value;
	}
	
	public CharStatus getStatus()
	{
		return _status;
	}
	
	/**
	 * Initializes the CharStatus class of the L2Object,
	 * is overwritten in classes that require a different CharStatus Type.
	 * 
	 * Removes the need for instanceof checks.
	 */
	public void initCharStatus()
	{
		_status = new CharStatus(this);
	}
	
	public final void setStatus(CharStatus value)
	{
		_status = value;
	}
	
	@Override
	public CharPosition getPosition()
	{
		return (CharPosition) super.getPosition();
	}
	
	@Override
	public void initPosition()
	{
		setObjectPosition(new CharPosition(this));
	}
	
	public L2CharTemplate getTemplate()
	{
		return _template;
	}
	
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
	 * @param template 
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}
	
	/**
	 * @return the Title of the L2Character.
	 */
	public final String getTitle()
	{
		return _title;
	}
	
	/**
	 * Set the Title of the L2Character. 
	 * @param value
	 */
	public final void setTitle(String value)
	{
		if (value == null)
			_title = "";
		else
			_title = value.length() > 16 ? value.substring(0, 15) : value;
	}
	
	/** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setWalking()
	{
		if (isRunning())
			setIsRunning(false);
	}
	
	/**
	 * Task launching the function onHitTimer().<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		boolean _soulshot;
		
		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed executing HitTask.", e);
			}
		}
	}
	
	/** Task launching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		L2Object[] targets;
		L2Skill skill;
		int count;
		int hitTime;
		int coolTime;
		int phase;
		boolean simultaneously;
		int shots;
		
		public MagicUseTask(L2Object[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous, int shot)
		{
			targets = tgts;
			skill = s;
			count = 0;
			phase = 1;
			hitTime = hit;
			coolTime = coolT;
			simultaneously = simultaneous;
			shots = shot;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (phase)
				{
					case 1:
						onMagicLaunchedTimer(this);
						break;
					case 2:
						onMagicHitTimer(this);
						break;
					case 3:
						onMagicFinalizer(this);
						break;
					default:
						break;
				}
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed executing MagicUseTask.", e);
				if (simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
			}
		}
	}
	
	/** Task launching the function useMagic() */
	private static class QueuedMagicUseTask implements Runnable
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
		
		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed executing QueuedMagicUseTask.", e);
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
		
		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "NotifyAITask failed. " + e.getMessage() + " Actor " + L2Character.this, e);
			}
		}
	}
	
	/** Task launching the magic skill phases */
	class FlyToLocationTask implements Runnable
	{
		private final L2Object _tgt;
		private final L2Character _actor;
		private final L2Skill _skill;
		
		public FlyToLocationTask(L2Character actor, L2Object target, L2Skill skill)
		{
			_actor = actor;
			_tgt = target;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			try
			{
				FlyType _flyType;
				
				_flyType = FlyType.valueOf(_skill.getFlyType());
				
				broadcastPacket(new FlyToLocation(_actor, _tgt, _flyType));
				setXYZ(_tgt.getX(), _tgt.getY(), _tgt.getZ());
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "Failed executing FlyToLocationTask.", e);
			}
		}
	}
	
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	/** Map 32 bits (0x0000) containing all abnormal effect in progress */
	private int _AbnormalEffects;
	
	protected CharEffectList _effects = new CharEffectList(this);
	
	private int _SpecialEffects;
	
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 * @param newEffect 
	 */
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 * @param effect 
	 */
	public final void removeEffect(L2Effect effect)
	{
		_effects.queueEffect(effect, true);
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 * @param mask 
	 */
	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	/**
	 * Active special effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 * @param mask 
	 */
	public final void startSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects |= special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void startSpecialEffect(int mask)
	{
		_SpecialEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startConfused()
	{
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFakeDeath()
	{
		if (!isPlayer())
			return;
		
		getActingPlayer().setIsFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFear()
	{
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startMuted()
	{
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startPsychicalMuted()
	{
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startRooted()
	{
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startSleeping()
	{
		/* Aborts any attacks/casts if asleep */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR><BR>
	 *
	 */
	public final void startStunning()
	{
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if (!isSummon())
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		updateAbnormalEffect();
	}
	
	public final void startParalyze()
	{
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.<BR><BR>
	 * @param mask 
	 */
	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the special effect map according to the mask.<BR><BR>
	 * @param mask 
	 */
	public final void stopSpecialEffect(AbnormalEffect[] mask)
	{
		for (AbnormalEffect special : mask)
		{
			_SpecialEffects &= ~special.getMask();
		}
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(AbnormalEffect mask)
	{
		_SpecialEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	public final void stopSpecialEffect(int mask)
	{
		_SpecialEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the L2Character.<BR><BR>
	 */
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param effect 
	 */
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.CONFUSION);
		else
			removeEffect(effect);
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * @param skillId the L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2SkillType and update client magic icon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skillType The L2SkillType of the L2Effect to remove from _effects
	 * @param negateLvl 
	 */
	public final void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		_effects.stopSkillEffects(skillType, negateLvl);
	}
	
	public final void stopSkillEffects(L2SkillType skillType)
	{
		_effects.stopSkillEffects(skillType, -1);
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR><BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 *
	 */
	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	public final void stopEffectsOnAction()
	{
		_effects.stopEffectsOnAction();
	}
	
	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set.
	 * Called on decreasing HP and mana burn.
	 * @param awake 
	 */
	public final void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopFakeDeath(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.FAKE_DEATH);
		
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (isPlayer())
		{
			getActingPlayer().setIsFakeDeath(false);
			getActingPlayer().setRecentFakeDeath(true);
		}
		
		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		//TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new Revive(this));
	}
	
	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _affraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopFear(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.FEAR);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopMuted(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.MUTE);
		
		updateAbnormalEffect();
	}
	
	public final void stopPsychicalMuted(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopRooting(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.ROOT);
		
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopSleeping(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.SLEEP);
		
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopStunning(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.STUN);
		
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	public final void stopParalyze(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.PARALYZE);
		
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
	}
	
	/**
	 * Stop L2Effect: Transformation<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Transformation Effect</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 * @param removeEffects 
	 */
	public final void stopTransformation(boolean removeEffects)
	{
		if (removeEffects)
			stopEffects(L2EffectType.TRANSFORMATION);
		
		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if (isPlayer())
		{
			if (getActingPlayer().getTransformation() != null)
			{
				getActingPlayer().untransform();
			}
		}
		
		if (!isPlayer())
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR><BR>
	 *
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR><BR>
	 *
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	/**
	 * Updates Effect Icons for this character(player/summon) and his party if any<BR>
	 * 
	 * Overridden in:<BR>
	 * L2PcInstance<BR>
	 * L2Summon<BR>
	 * 
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// overridden
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR><BR>
	 * @return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.
	 */
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (!isFlying() && isStunned())
			ae |= AbnormalEffect.STUN.getMask();
		if (!isFlying() && isRooted())
			ae |= AbnormalEffect.ROOT.getMask();
		if (isSleeping())
			ae |= AbnormalEffect.SLEEP.getMask();
		if (isConfused())
			ae |= AbnormalEffect.FEAR.getMask();
		if (isMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isPhysicalMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isAfraid())
			ae |= AbnormalEffect.SKULL_FEAR.getMask();
		return ae;
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : INVULNERABLE = 0x0001 (bit 1), PINK_AFFRO = 0x0020 (bit 6)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, UserInfo...</li><BR><BR>
	 * @return a map of 32 bits (0x00000000) containing all special effect in progress for this L2Character.
	 */
	public int getSpecialEffect()
	{
		int se = _SpecialEffects;
		if (isFlying() && isStunned())
			se |= AbnormalEffect.S_AIR_STUN.getMask();
		if (isFlying() && isRooted())
			se |= AbnormalEffect.S_AIR_ROOT.getMask();
		return se;
	}
	
	/**
	 * Return all active skills effects in progress on the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR><BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 *
	 */
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}
	
	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skillId The L2Skill Identifier of the L2Effect to return from the _effects
	 *
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 *
	 */
	public final L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skill The L2Skill whose effect must be returned
	 *
	 * @return The first L2Effect created by the L2Skill
	 *
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 *
	 * @return The first L2Effect corresponding to the Effect Type
	 *
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}
	
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		public AIAccessor()
		{
		}
		
		/**
		 * @return the L2Character managed by this Accessor AI.
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 * @param x 
		 * @param y 
		 * @param z 
		 * @param offset 
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 * @param x 
		 * @param y 
		 * @param z 
		 */
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 * @param pos 
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}
		
		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 * @param target 
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 * @param skill 
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.<BR><BR>
		 * @param evt 
		 * @return 
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
	
	/**
	 * This class group all mouvement data.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom  : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li><BR><BR>
	 *
	 * */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveStartTime;
		public int _moveTimestamp; // last update
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	/** Table containing all skillId that are disabled */
	protected L2TIntObjectHashMap<Long> _disabledSkills;
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
	
	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	private int _disableCrossBowAttackEndTime;
	
	private int _castInterruptTime;
	
	/**
	 * Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE)
	 */
	private static final Calculator[] NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	
	protected L2CharacterAI _ai;
	
	/** Future Skill Cast */
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	
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
	public final void addStatFunc(Func f)
	{
		if (f == null)
			return;
		
		synchronized (_calculators)
		{
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if (_calculators == NPC_STD_CALCULATOR)
			{
				// Create a copy of the standard NPC Calculator set
				_calculators = new Calculator[Stats.NUM_STATS];
				
				for (int i = 0; i < Stats.NUM_STATS; i++)
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
	public final void addStatFuncs(Func[] funcs)
	{
		
		List<Stats> modifiedStats = new ArrayList<>();
		
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
	public final void removeStatFunc(Func f)
	{
		if (f == null)
			return;
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		
		synchronized (_calculators)
		{
			if (_calculators[stat] == null)
				return;
			
			// Remove the Func object from the Calculator
			_calculators[stat].removeFunc(f);
			
			if (_calculators[stat].size() == 0)
				_calculators[stat] = null;
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (isNpc())
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
	public final void removeStatFuncs(Func[] funcs)
	{
		
		List<Stats> modifiedStats = new ArrayList<>();
		
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
	public final void removeStatsOwner(Object owner)
	{
		
		List<Stats> modifiedStats = null;
		
		int i = 0;
		// Go through the Calculator set
		synchronized (_calculators)
		{
			for (Calculator calc : _calculators)
			{
				if (calc != null)
				{
					// Delete all Func objects of the selected owner
					if (modifiedStats != null)
						modifiedStats.addAll(calc.removeOwner(owner));
					else
						modifiedStats = calc.removeOwner(owner);
					
					if (calc.size() == 0)
						_calculators[i] = null;
				}
				i++;
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (isNpc())
			{
				i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
						break;
				}
				
				if (i >= Stats.NUM_STATS)
					_calculators = NPC_STD_CALCULATOR;
			}
			
			if (owner instanceof L2Effect)
			{
				if (!((L2Effect) owner).preventExitUpdate)
					broadcastModifiedStats(modifiedStats);
			}
			else
				broadcastModifiedStats(modifiedStats);
		}
	}
	
	protected void broadcastModifiedStats(List<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
			return;
		
		boolean broadcastFull = false;
		StatusUpdate su = null;
		
		for (Stats stat : stats)
		{
			if (isSummon() && ((L2Summon) this).getOwner() != null)
			{
				((L2Summon) this).updateAndBroadcastStatus(1);
				break;
				
			}
			else if (stat == Stats.POWER_ATTACK_SPEED)
			{
				if (su == null)
					su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat == Stats.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
					su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
			else if (stat == Stats.MAX_HP && isL2Attackable())
			{
				if (su == null)
					su = new StatusUpdate(this);
				su.addAttribute(StatusUpdate.MAX_HP, getMaxVisibleHp());
			}
			else if (stat == Stats.LIMIT_HP)
			{
				getStatus().setCurrentHp(getCurrentHp()); // start regeneration if needed
			}
			/*else if (stat == Stats.MAX_CP)
			{
				if (isPlayer())
				{
					if (su == null) su = new StatusUpdate(getObjectId());
					su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
				}
			}*/
			//else if (stat==Stats.MAX_MP)
			//{
			//	if (su == null) su = new StatusUpdate(getObjectId());
			//	su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			//}
			else if (stat == Stats.RUN_SPEED)
			{
				broadcastFull = true;
			}
		}
		
		if (isPlayer())
		{
			if (broadcastFull)
				getActingPlayer().updateAndBroadcastStatus(2);
			else
			{
				getActingPlayer().updateAndBroadcastStatus(1);
				if (su != null)
				{
					broadcastPacket(su);
				}
			}
		}
		else if (isNpc())
		{
			if (broadcastFull)
			{
				Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
				for (L2PcInstance player : plrs)
				{
					if (player == null)
						continue;
					if (getRunSpeed() == 0)
						player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
					else
						player.sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc) this, player));
				}
			}
			else if (su != null)
				broadcastPacket(su);
		}
		else if (su != null)
			broadcastPacket(su);
	}
	
	/**
	 * @return the orientation of the L2Character.
	 */
	public final int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 * @param heading 
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public Location getLocation()
	{
		return new Location(getX(), getY(), getZ(), getHeading(), getInstanceId());
	}
	
	public final int getXdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._xDestination;
		
		return getX();
	}
	
	/**
	 * @return the Y destination of the L2Character or the Y position if not in movement.
	 */
	public final int getYdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._yDestination;
		
		return getY();
	}
	
	/**
	 * @return the Z destination of the L2Character or the Z position if not in movement.
	 */
	public final int getZdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._zDestination;
		
		return getZ();
	}
	
	/**
	 * @return True if the L2Character is in combat.
	 */
	public boolean isInCombat()
	{
		return hasAI() && (getAI().getAttackTarget() != null || getAI().isAutoAttacking());
	}
	
	/**
	 * @return True if the L2Character is moving.
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * @return True if the L2Character is travelling a calculated path.
	 */
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
			return false;
		if (m.onGeodataPathIndex == -1)
			return false;
		if (m.onGeodataPathIndex == m.geoPath.size() - 1)
			return false;
		return true;
	}
	
	/**
	 * @return True if the L2Character is casting.
	 */
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}
	
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}
	
	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}
	
	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}
	
	/**
	 * @return True if the cast of the L2Character can be aborted.
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}
	
	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}
	
	/**
	 * @return True if the L2Character is attacking.
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * @return True if the L2Character has aborted its attack.
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
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	/**
	 * @return body part (paperdoll slot) we are targeting right now
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
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			Future<?> future = _skillCast;
			// cancels the skill hit scheduled task
			if (future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if (future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			
			if (getFusionSkill() != null)
				getFusionSkill().onCastAbort();
			
			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();
			
			if (_allSkillsDisabled)
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			if (isPlayer())
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 *
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR><BR>
	 *
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR><BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
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
		
		double dx, dy, dz;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		final boolean isFloating = isFlying() || isInsideZone(L2Character.ZONE_WATER);
		
		// Z coordinate will follow geodata or client values
		if (Config.GEODATA > 0 && Config.COORD_SYNCHRONIZE == 2 && !isFloating && !m.disregardingGeodata && GameTimeController.getGameTicks() % 10 == 0 // once a second to reduce possible cpu load
				&& GeoData.getInstance().hasGeo(xPrev, yPrev))
		{
			short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30, null);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (isPlayer() && Math.abs(getActingPlayer().getClientZ() - geoHeight) > 200 && Math.abs(getActingPlayer().getClientZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (this.isInCombat() && Math.abs(dz) > 200 && (dx * dx + dy * dy) < 40000) // allow mob to climb up to pcinstance
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
		
		double delta = dx * dx + dy * dy;
		if (delta < 10000 && (dz * dz > 2500) // close enough, allows error between client and server geodata if it cannot be avoided
				&& !isFloating) // should not be applied on vertical movements in water or during flight
			delta = Math.sqrt(delta);
		else
			delta = Math.sqrt(delta + dz * dz);
		
		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}
		
		// if (Config.DEVELOPER) _log.warning("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1) // already there
			// Set the position of the L2Character to the destination
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			// Set the position of the L2Character to estimated after parcial move
			super.getPosition().setXYZ((int) (m._xAccurate), (int) (m._yAccurate), zPrev + (int) (dz * distFraction + 0.5));
		}
		revalidateZone(false);
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		
		return (distFraction > 1);
	}
	
	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null)
			return;
		
		// This function is called too often from movement code
		if (force)
			_zoneValidateCounter = 4;
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
				_zoneValidateCounter = 4;
			else
				return;
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
	 * @param pos 
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
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
			this.getKnownList().findObjects();
	}
	
	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR><BR>
	 *
	 * @param object L2object to target
	 *
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;
		
		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		_target = object;
	}
	
	/**
	 * @return the identifier of the L2Object targeted or -1.
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
	 * @return the L2Object targeted or null.
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
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
			return;
		
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt(dx * dx + dy * dy);
		
		final boolean verticalMovementOnly = isFlying() && distance == 0 && dz != 0;
		if (verticalMovementOnly)
			distance = Math.abs(dz);
		
		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if (Config.GEODATA > 0 && isInsideZone(ZONE_WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.sqrt(dx * dx + dy * dy);
		}
		
		if (Config.DEBUG)
			_log.fine("distance to target:" + distance);
		
		// Define movement angles needed
		// ^
		// |     X (x,y)
		// |   /
		// |  /distance
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
				if (Config.DEBUG)
					_log.fine("already in range, no movement needed.");
				
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				
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
		
		if (Config.GEODATA > 0 && !isFlying() // flying chars not checked - even canSeeTarget doesn't work yet
				&& (!isInsideZone(ZONE_WATER) || isInsideZone(ZONE_SIEGE)) // swimming also not checked unless in siege zone - but distance is limited
				&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			final boolean isInVehicle = isPlayer() && getActingPlayer().getVehicle() != null;
			if (isInVehicle)
				m.disregardingGeodata = true;
			
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			int gty = (originalY - L2World.MAP_MIN_Y) >> 4;
			
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if ((Config.GEODATA == 2 && !(isL2Attackable() && ((L2Attackable) this).isReturningToSpawnPoint())) || (isPlayer() && !(isInVehicle && distance > 1500)) || (isSummon() && !(this.getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
					|| isAfraid() || this instanceof L2RiftInvaderInstance)
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return;
						_move.onGeodataPathIndex = -1; // Set not on geodata path
					}
					catch (NullPointerException e)
					{
						// nothing
					}
				}
				
				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y || curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					_log.warning("Character " + this.getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (isPlayer())
						getActingPlayer().logout();
					else if (isSummon())
						return; // preventation when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					else
						this.onDecay();
					return;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z, getInstanceId());
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				dx = x - curX;
				dy = y - curY;
				dz = z - curZ;
				distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
			}
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if (Config.GEODATA == 2 && originalDistance - distance > 30 && distance < 2000 && !this.isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if ((isPlayable() && !isInVehicle) || this.isMinion() || this.isInCombat())
				{
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, getInstanceId(), isPlayable());
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
						if (isPlayer() || (!isPlayable() && !this.isMinion() && Math.abs(z - curZ) > 140) || (isSummon() && !((L2Summon) this).getFollowStatus()))
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
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z, getInstanceId()))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						for (int i = 0; i < m.geoPath.size() - 1; i++)
						{
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1), getInstanceId()))
							{
								m.geoPath = null;
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}
			// If no distance to go through, the movement is canceled
			if (distance < 1 && (Config.GEODATA == 2 || isPlayable() || this instanceof L2RiftInvaderInstance || this.isAfraid()))
			{
				if (isSummon())
					((L2Summon) this).setFollowStatus(false);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}
		
		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying() || isInsideZone(ZONE_WATER)) && !verticalMovementOnly)
			distance = Math.sqrt(distance * distance + dz * dz);
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		// Does not broke heading on vertical movements
		if (!verticalMovementOnly)
			setHeading(Util.calculateHeadingFrom(cos, sin));
		
		if (Config.DEBUG)
			_log.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getHeading());
		
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
		
		MoveData md = _move;
		if (md == null)
			return false;
		
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		
		if (md.onGeodataPathIndex == md.geoPath.size() - 2)
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
		// Calculate and set the heading of the L2Character
		if (distance != 0)
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		
		m._heading = 0; // initial value for coordinate sync
		
		m._moveStartTime = GameTimeController.getGameTicks();
		
		if (Config.DEBUG)
			_log.fine("time to target:" + ticksToMove);
		
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
		
		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);
		
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;
		
		if (m == null)
			return true;
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0); // initial value or false
			m._heading = heading;
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
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x   X position of the target
	 * @param y   Y position of the target
	 * @param z 
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
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
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
		
		return (dx * dx + dy * dy + dz * dz);
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
		
		return (dx * dx + dy * dy);
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
	 * @see #isInsideRadius(int, int, int, int, boolean, boolean)
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
	
	public final boolean isInsideRadius(Location loc, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZ, strictCheck);
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
		boolean isInsideRadius = false;
		if (strictCheck)
		{
			if (checkZ)
				isInsideRadius = (dx * dx + dy * dy + dz * dz) < radius * radius;
			else
				isInsideRadius = (dx * dx + dy * dy) < radius * radius;
		}
		else
		{
			if (checkZ)
				isInsideRadius = (dx * dx + dy * dy + dz * dz) <= radius * radius;
			else
				isInsideRadius = (dx * dx + dy * dy) <= radius * radius;
		}
		return isInsideRadius;
	}
	
	//	/**
	//	* event that is called when the destination coordinates are reached
	//	*/
	//	public void onTargetReached()
	//	{
	//	L2Character pawn = getPawnTarget();
	//
	//	if (pawn != null)
	//	{
	//	int x = pawn.getX(), y=pawn.getY(),z = pawn.getZ();
	//
	//	double distance = getDistance(x,y);
	//	if (getCurrentState() == STATE_FOLLOW)
	//	{
	//	calculateMovement(x,y,z,distance);
	//	return;
	//	}
	//
	//	//          takes care of moving away but distance is 0 so i won't follow problem
	//
	//
	//	if (((distance > getAttackRange()) && (getCurrentState() == STATE_ATTACKING)) || (pawn.isMoving() && getCurrentState() != STATE_ATTACKING))
	//	{
	//	calculateMovement(x,y,z,distance);
	//	return;
	//	}
	//
	//	}
	//	//       update x,y,z with the current calculated position
	//	stopMove();
	//
	//	if (Config.DEBUG)
	//	_log.fine(this.getName() +":: target reached at: x "+getX()+" y "+getY()+ " z:" + getZ());
	//
	//	if (getPawnTarget() != null)
	//	{
	//
	//	setPawnTarget(null);
	//	setMovingToPawn(false);
	//	}
	//	}
	//
	//	public void setTo(int x, int y, int z, int heading)
	//	{
	//	setX(x);
	//	setY(y);
	//	setZ(z);
	//	setHeading(heading);
	//	updateCurrentWorldRegion(); //TODO: maybe not needed here
	//	if (isMoving())
	//	{
	//	setCurrentState(STATE_IDLE);
	//	StopMove setto = new StopMove(this);
	//	broadcastPacket(setto);
	//	}
	//	else
	//	{
	//	ValidateLocation setto = new ValidateLocation(this);
	//	broadcastPacket(setto);
	//	}
	//
	//	FinishRotation fr = new FinishRotation(this);
	//	broadcastPacket(fr);
	//	}
	
	//	protected void startCombat()
	//	{
	//	if (_currentAttackTask == null )//&& !isInCombat())
	//	{
	//	_currentAttackTask = ThreadPoolManager.getInstance().scheduleMed(new AttackTask(), 0);
	//	}
	//	else
	//	{
	//	_log.info("multiple attacks want to start in parallel. prevented.");
	//	}
	//	}
	//
	
	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return True if arrows are available.
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return True if bolts are available.
	 */
	protected boolean checkAndEquipBolts()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 * @param addToExp 
	 * @param addToSp 
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return the active weapon instance (always equiped in the right hand).
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return the active weapon item (always equiped in the right hand).
	 */
	public abstract L2Weapon getActiveWeaponItem();
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return the secondary weapon instance (always equiped in the left hand).
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @return the secondary {@link L2Item} item (always equiped in the left hand).
	 */
	public abstract L2Item getSecondaryWeaponItem();
	
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
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() || (isNpc() && ((L2Npc) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if ((isNpc() && target.isAlikeDead()) || target.isDead() || (!getKnownList().knowsObject(target) && !isDoor()))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			// Notify target AI
			if (target.hasAI())
				target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
			
			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(this);
		}
		
		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss);
		
		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid() && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE)
			{
				if (getLevel() > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
					
					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this);
					}
					else
						_log.warning("Skill 4515 at level 1 is missing in DP.");
					
					damage = 0; // prevents messing up drop calculation
				}
			}
			
			// If L2Character target is a L2PcInstance, send a system message
			if (target.isPlayer())
			{
				L2PcInstance enemy = target.getActingPlayer();
				enemy.getAI().clientStartAutoAttack();
				
				/*if (shld && 100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
				{
				   if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
				   {
				             damage = 1;
				             enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS); //SHIELD_DEFENCE faultless
				   }
				    else
				      enemy.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL);
				}*/
			}
			
			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isBow = (weapon != null && (weapon.getItemType() == L2WeaponType.BOW || weapon.getItemType() == L2WeaponType.CROSSBOW));
				int reflectedDamage = 0;
				
				if (!isBow && !target.isInvul()) // Do not reflect if weapon is of type bow or target is invunlerable
				{
					// quick fix for no drop from raid if boss attack high-level char with damage reflection
					if (!target.isRaid() || getActingPlayer() == null || getActingPlayer().getLevel() <= target.getLevel() + 8)
					{
						// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
						double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
						
						if (reflectPercent > 0)
						{
							reflectedDamage = (int) (reflectPercent / 100. * damage);
							
							if (reflectedDamage > target.getMaxHp()) // to prevent extreme damage when hitting a low lvl char...
								reflectedDamage = target.getMaxHp();
						}
					}
				}
				
				// reduce targets HP
				target.reduceCurrentHp(damage, this, null);
				
				if (reflectedDamage > 0)
				{
					reduceCurrentHp(reflectedDamage, target, true, false, null);
				}
				
				if (!isBow) // Do not absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxRecoverableHp() - getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);
						
						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
							
						if (absorbDamage > 0)
							setCurrentHp(getCurrentHp() + absorbDamage);
					}
					
					// Absorb MP from the damage inflicted
					absorbPercent = getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxRecoverableMp() - getCurrentMp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);
						
						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
							
						if (absorbDamage > 0)
							setCurrentMp(getCurrentMp() + absorbDamage);
					}
					
				}
				
				// Notify AI with EVT_ATTACKED
				if (target.hasAI())
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				if (isSummon())
				{
					L2PcInstance owner = ((L2Summon) this).getOwner();
					if (owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Maybe launch chance skills on us
				if (_chanceSkills != null)
				{
					_chanceSkills.onHit(target, damage, false, crit);
					// Reflect triggers onHit
					if (reflectedDamage > 0)
						_chanceSkills.onHit(target, reflectedDamage, true, false);
				}
				
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
					target.getChanceSkills().onHit(this, damage, true, crit);
			}
			
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			if (activeWeapon != null)
				activeWeapon.getSkillEffects(this, target, crit);
			
			/* COMMENTED OUT BY nexus - 2006-08-17
			 *
			 * We must not discharge the soulshouts at the onHitTimer method,
			 * as this can cause unwanted soulshout consumption if the attacker
			 * recharges the soulshot right after an attack request but before
			 * his hit actually lands on the target.
			 *
			 * The soulshot discharging has been moved to the doAttack method:
			 * As soon as we know that we didn't missed the hit there, then we
			 * must discharge any charged soulshots.
			 */
			/*
			L2ItemInstance weapon = getActiveWeaponInstance();

			if (!miss)
			{
			    if (isSummon() && !(this instanceof L2PetInstance))
			    {
			        if (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
			            ((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			    }
			    else
			    {
			        if (weapon != null && weapon.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			            weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			    }
			}
			 */

			return;
		}
		
		if (!isCastingNow() && !isCastingSimultaneouslyNow())
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			
			if (isPlayer())
			{
				//TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(ActionFailed.STATIC_PACKET);
				
				// Send a system message
				sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical & static skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && (getLastSkillCast().isMagic() || getLastSkillCast().isStatic()))
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (isPlayer())
			{
				// Send a system message
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 * @param bolts 
	 */
	protected void reduceArrowCount(boolean bolts)
	{
		// default is to do nothing
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
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isInOlympiadMode() && player.getTarget() != null && player.getTarget().isPlayable())
		{
			L2PcInstance target = null;
			L2Object object = player.getTarget();
			if (object != null && object.isPlayable())
			{
				target = object.getActingPlayer();
			}
			
			if (target == null || (target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId())))
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		if (player.getTarget() != null && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (player.getBlockCheckerArena() != -1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	/**
	 * @param attacker 
	 * @return True if inside peace zone.
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return (!attacker.getAccessLevel().allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target));
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
			return false;
		if (!(target.isPlayable() && attacker.isPlayable()))
			return false;
		if (InstanceManager.getInstance().getInstance(this.getInstanceId()).isPvPInstance())
			return false;
		
		if (TerritoryWarManager.PLAYER_WITH_WARD_CAN_BE_KILLED_IN_PEACEZONE && TerritoryWarManager.getInstance().isTWInProgress())
		{
			if (target.isPlayer() && target.getActingPlayer().isCombatFlagEquipped())
				return false;
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0)
				return false;
			if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getKarma() > 0 && target.getActingPlayer() != null && target.getActingPlayer().getPvpFlag() > 0)
				return false;
			
			if (attacker instanceof L2Character && target instanceof L2Character)
			{
				return (((L2Character) target).isInsideZone(ZONE_PEACE) || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
			}
			if (attacker instanceof L2Character)
			{
				return (TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
			}
		}
		
		if (attacker instanceof L2Character && target instanceof L2Character)
		{
			return (((L2Character) target).isInsideZone(ZONE_PEACE) || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
		}
		if (attacker instanceof L2Character)
		{
			return (TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
		}
		return (TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null);
	}
	
	/**
	 * @return true if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return ((region != null) && (region.isActive()));
	}
	
	/**
	 * @return True if the L2Character has a Party in progress.
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * @return the L2Party object of the L2Character.
	 */
	public L2Party getParty()
	{
		return null;
	}
	
	/**
	 * @param target 
	 * @param weapon 
	 * @return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if (weapon != null && !isTransformed())
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1500 * 345 / atkSpd);
				case CROSSBOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1200 * 345 / atkSpd);
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
		
		return Formulas.calcPAtkSpd(this, target, atkSpd);
	}
	
	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null || isTransformed())
			return 0;
		
		int reuse = weapon.getReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
			return 0;
		
		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
			case CROSSBOW:
				return (int) (reuse * 345 / atkSpd);
			default:
				return (int) (reuse * 312 / atkSpd);
		}
	}
	
	/**
	 * @return True if the L2Character use a dual weapon.
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
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			if (newSkill.getDisplayId() != newSkill.getId())
			{
				_customSkills.put(Integer.valueOf(newSkill.getDisplayId()), new SkillHolder(newSkill));
			}
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if ((oldSkill.triggerAnotherSkill()))
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
			
			if (oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}
			
			// Add passive effects if there are any.
			newSkill.getEffectsPassive(this);
			
			/*if (!newSkill.isChance() && newSkill.triggerAnotherSkill() )
			{
				L2Skill bestowed = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(bestowed);
				//bestowed skills are invisible for player. Visible for gm's looking thru gm window.
				//those skills should always be chance or passive, to prevent hlapex.
			}
			            
			if(newSkill.isChance() && newSkill.triggerAnotherSkill())
			{
				L2Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(),newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}*/
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
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
			return null;
		
		return removeSkill(skill.getId(), true);
	}
	
	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		if (skill == null)
			return null;
		
		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId(), cancelEffect);
	}
	
	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}
	
	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			if (oldSkill.getDisplayId() != oldSkill.getId())
			{
				_customSkills.remove(Integer.valueOf(oldSkill.getDisplayId()));
			}
			//this is just a fail-safe againts buggers and gm dummies...
			if ((oldSkill.triggerAnotherSkill()) && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}
			
			// does not abort casting of the transformation dispell
			if (oldSkill.getSkillType() != L2SkillType.TRANSFORMDISPEL)
			{
				// Stop casting if this skill is used right now
				if (getLastSkillCast() != null && isCastingNow())
				{
					if (oldSkill.getId() == getLastSkillCast().getId())
						abortCast();
				}
				if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow())
				{
					if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
						abortCast();
				}
			}
			
			// Remove passive effects.
			_effects.removePassiveEffects(skillId);
			
			if (cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Effect e = getFirstEffect(oldSkill);
				if (e == null || e.getEffectType() != L2EffectType.TRANSFORMATION)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}
			
			if (oldSkill instanceof L2SkillAgathion && isPlayer() && getActingPlayer().getAgathionId() > 0)
			{
				getActingPlayer().setAgathionId(0);
				getActingPlayer().broadcastUserInfo();
			}
			
			if (oldSkill instanceof L2SkillMount && isPlayer() && getActingPlayer().isMounted())
				getActingPlayer().dismount();
			
			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && isPlayer() && getActingPlayer().getPet() != null && getActingPlayer().getPet().getNpcId() == 14870)
			{
				getActingPlayer().getPet().unSummon(getActingPlayer());
			}
		}
		
		return oldSkill;
	}
	
	public void removeChanceSkill(int id)
	{
		if (_chanceSkills == null)
			return;
		synchronized (_chanceSkills)
		{
			for (IChanceSkillTrigger trigger : _chanceSkills.keySet())
			{
				if (!(trigger instanceof L2Skill))
					continue;
				if (((L2Skill) trigger).getId() == id)
					_chanceSkills.remove(trigger);
			}
		}
	}
	
	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if (_chanceSkills == null)
		{
			synchronized (this)
			{
				if (_chanceSkills == null)
					_chanceSkills = new ChanceSkillList(this);
			}
		}
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}
	
	public void removeChanceEffect(IChanceSkillTrigger effect)
	{
		if (_chanceSkills == null)
			return;
		_chanceSkills.remove(effect);
	}
	
	public void onStartChanceEffect(byte element)
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onStart(element);
	}
	
	public void onActionTimeChanceEffect(byte element)
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onActionTime(element);
	}
	
	public void onExitChanceEffect(byte element)
	{
		if (_chanceSkills == null)
			return;
		
		_chanceSkills.onExit(element);
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR><BR>
	 * @return all skills own by the L2Character in a table of L2Skill.
	 */
	public final Collection<L2Skill> getAllSkills()
	{
		return new ArrayList<>(_skills.values());
	}
	
	/**
	 * @return the map containing this character skills.
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	/**
	 * @return all the custom skills (skills with different display Id than skill Id).
	 */
	public final Map<Integer, SkillHolder> getCustomSkills()
	{
		return _customSkills;
	}
	
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 *
	 * @return The level of the L2Skill identified by skillId
	 *
	 */
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		if (skill == null)
			return -1;
		
		return skill.getLevel();
	}
	
	/**
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 * @return the skill from the known skill.
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
	
	/**
	 * Return the number of buffs affecting this L2Character.<BR><BR>
	 *
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}
	
	public int getDanceCount()
	{
		return _effects.getDanceCount();
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
	 * @param mut
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		L2Object[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			abortCast();
			return;
		}
		
		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				// only AURA-type skills can be cast without target
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_CORPSE_MOB:
					break;
				default:
					abortCast();
					return;
			}
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();
		
		if (targets.length > 0 && escapeRange > 0)
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			List<L2Character> targetList = new FastList<>(targets.length);
			for (L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					if (!Util.checkIfInRange(escapeRange, this, target, true))
					{
						_skiprange++;
						continue;
					}
					if (skill.getSkillRadius() > 0 && skill.isOffensive() && Config.GEODATA > 0 && !GeoData.getInstance().canSeeTarget(this, target))
					{
						_skipgeo++;
						continue;
					}
					if (skill.isOffensive() && !skill.isNeutral())
					{
						if (isPlayer())
						{
							if (((L2Character) target).isInsidePeaceZone(getActingPlayer()))
							{
								_skippeace++;
								continue;
							}
						}
						else
						{
							if (((L2Character) target).isInsidePeaceZone(this, target))
							{
								_skippeace++;
								continue;
							}
						}
					}
					targetList.add((L2Character) target);
				}
				//else
				//{
				//	if (Config.DEBUG)
				//        _log.warning("Class cast bad: "+targets[i].getClass().toString());
				//}
			}
			if (targetList.isEmpty())
			{
				if (isPlayer())
				{
					if (_skiprange > 0)
						sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					else if (_skipgeo > 0)
						sendPacket(SystemMessageId.CANT_SEE_TARGET);
					else if (_skippeace > 0)
						sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
				}
				abortCast();
				return;
			}
			mut.targets = targetList.toArray(new L2Character[targetList.size()]);
		}
		
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Static skills can be used while faking death.
		if ((mut.simultaneously && !isCastingSimultaneouslyNow()) || (!mut.simultaneously && !isCastingNow()) || (isAlikeDead() && !skill.isStatic()))
		{
			// now cancels both, simultaneous and normal
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = getSkillLevel(skill.getId());
		
		if (level < 1)
			level = 1;
		
		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isStatic())
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
		
		mut.phase = 2;
		if (mut.hitTime == 0)
			onMagicHitTimer(mut);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
	}
	
	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		final L2Object[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			abortCast();
			return;
		}
		
		if (getFusionSkill() != null)
		{
			if (mut.simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			getFusionSkill().onCastAbort();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (mut.simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		try
		{
			// Go through targets table
			for (L2Object tgt : targets)
			{
				if (tgt.isPlayable())
				{
					L2Character target = (L2Character) tgt;
					
					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addSkillName(skill);
						target.sendPacket(smsg);
					}
					
					if (isPlayer() && target.isSummon())
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}
			
			StatusUpdate su = new StatusUpdate(this);
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);
			
			if (mpConsume > 0)
			{
				if (mpConsume > getCurrentMp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abortCast();
					return;
				}
				
				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				
				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if (consumeHp + 1 >= getCurrentHp())
					consumeHp = getCurrentHp() - 1.0;
				
				getStatus().reduceHp(consumeHp, this, true);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other L2PcInstance to inform
			if (skill.getCpConsume() > 0)
			{
				double consumeCp;
				
				consumeCp = skill.getCpConsume();
				if (consumeCp + 1 >= getCurrentHp())
					consumeCp = getCurrentHp() - 1.0;
				
				getStatus().reduceCp((int) consumeCp);
				su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
				sendPacket(su);
			
			if (isPlayer())
			{
				int charges = getActingPlayer().getCharges();
				// check for charges
				if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
					sm.addSkillName(skill);
					sendPacket(sm);
					abortCast();
					return;
				}
				// generate charges if any
				if (skill.getNumCharges() > 0)
				{
					if (skill.getMaxCharges() > 0)
						getActingPlayer().increaseCharges(skill.getNumCharges(), skill.getMaxCharges());
					else
						getActingPlayer().decreaseCharges(skill.getNumCharges());
				}
				
				// Consume Souls if necessary
				if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
				{
					if (!getActingPlayer().decreaseSouls(skill.getSoulConsumeCount() > 0 ? skill.getSoulConsumeCount() : skill.getMaxSoulConsumeCount(), skill))
					{
						abortCast();
						return;
					}
				}
			}
			
			// On each repeat restore shots before cast
			if (mut.count > 0)
			{
				final L2ItemInstance weaponInst = getActiveWeaponInstance();
				if (weaponInst != null)
				{
					if (mut.skill.useSoulShot())
						weaponInst.setChargedSoulshot(mut.shots);
					else if (mut.skill.useSpiritShot())
						weaponInst.setChargedSpiritshot(mut.shots);
				}
			}
			
			// Launch the magic skill in order to calculate its effects
			callSkill(mut.skill, mut.targets);
		}
		catch (NullPointerException e)
		{
			_log.log(Level.WARNING, "", e);
		}
		
		if (mut.hitTime > 0)
		{
			mut.count++;
			if (mut.count < skill.getHitCounts())
			{
				int hitTime = mut.hitTime * skill.getHitTimings()[mut.count] / 100;
				if (mut.simultaneously)
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				else
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				return;
			}
		}
		
		mut.phase = 3;
		if (mut.hitTime == 0 || mut.coolTime == 0)
			onMagicFinalizer(mut);
		else
		{
			if (mut.simultaneously)
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			else
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
		}
	}
	
	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.simultaneously)
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		
		_skillCast = null;
		setIsCastingNow(false);
		_castInterruptTime = 0;
		
		final L2Skill skill = mut.skill;
		final L2Object target = mut.targets.length > 0 ? mut.targets[0] : null;
		
		// Attack target after skill use
		if ((skill.nextActionIsAttack()) && (getTarget() instanceof L2Character) && (getTarget() != this) && (target != null) && (getTarget() == target) && target.isAttackable())
		{
			if (getAI() == null || getAI().getNextIntention() == null || getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		if (skill.isOffensive() && !skill.isNeutral() && !(skill.getSkillType() == L2SkillType.UNLOCK) && !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
			getAI().clientStartAutoAttack();
		
		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		
		notifyQuestEventSkillFinished(skill, target);
		/*
		 * If character is a player, then wipe their current cast state and
		 * check if a skill is queued.
		 *
		 * If there is a queued skill, launch it and wipe the queue.
		 */
		if (isPlayer())
		{
			L2PcInstance currPlayer = getActingPlayer();
			SkillDat queuedSkill = currPlayer.getQueuedSkill();
			
			currPlayer.setCurrentSkill(null, false, false);
			
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				
				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}
	
	// Quest event ON_SPELL_FNISHED
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		
	}
	
	public L2TIntObjectHashMap<Long> getDisabledSkills()
	{
		return _disabledSkills;
	}
	
	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 * @param skill the skill to enable.
	 */
	public void enableSkill(L2Skill skill)
	{
		if (skill == null || _disabledSkills == null)
			return;
		
		_disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(L2Skill skill, long delay)
	{
		if (skill == null)
			return;
		
		if (_disabledSkills == null)
			_disabledSkills = new L2TIntObjectHashMap<>();
		
		_disabledSkills.put(Integer.valueOf(skill.getReuseHashCode()), delay > 10 ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skill The L2Skill to check
	 * @return true if a skill is disabled.
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if (skill == null)
			return true;
		
		return isSkillDisabled(skill.getReuseHashCode());
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param reuseHashcode The reuse hashcode of the skillId/level to check
	 * @return true if a skill is disabled.
	 */
	public boolean isSkillDisabled(int reuseHashcode)
	{
		if (isAllSkillsDisabled())
			return true;
		
		if (_disabledSkills == null)
			return false;
		
		final Long timeStamp = _disabledSkills.get(Integer.valueOf(reuseHashcode));
		if (timeStamp == null)
			return false;
		
		if (timeStamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(Integer.valueOf(reuseHashcode));
			return false;
		}
		
		return true;
	}
	
	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG)
			_log.fine("all skills disabled");
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR><BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG)
			_log.fine("all skills enabled");
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
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			// Check if the toggle skill effects are already in progress on the L2Character
			if (skill.isToggle() && getFirstEffect(skill.getId()) != null)
				return;
			
			// Initial checks
			for (L2Object trg : targets)
			{
				if (trg instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character target = (L2Character) trg;
					
					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if (target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if (!Config.RAID_DISABLE_CURSE && ((target.isRaid() && target.giveRaidCurse() && getLevel() > target.getLevel() + 8) || (!skill.isOffensive() && targetsAttackTarget != null && targetsAttackTarget.isRaid() && targetsAttackTarget.giveRaidCurse() && targetsAttackTarget.getAttackByList().contains(target) // has attacked raid
					&& getLevel() > targetsAttackTarget.getLevel() + 8) || (!skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid() && targetsCastTarget.giveRaidCurse() && targetsCastTarget.getAttackByList().contains(target) // has attacked raid
					&& getLevel() > targetsCastTarget.getLevel() + 8)))
					{
						if (skill.isMagic())
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (_log.isLoggable(Level.WARNING))
								_log.log(Level.WARNING, "Skill 4215 at level 1 is missing in DP.");
						}
						else
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
							if (tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
							else if (_log.isLoggable(Level.WARNING))
								_log.log(Level.WARNING, "Skill 4515 at level 1 is missing in DP.");
						}
						return;
					}
					
					// Check if over-hit is possible
					if (skill.isOverhit())
					{
						if (target.isL2Attackable())
							((L2Attackable) target).overhitEnabled(true);
					}
					
					// crafting does not trigger any chance skills
					// possibly should be unhardcoded
					switch (skill.getSkillType())
					{
						case COMMON_CRAFT:
						case DWARVEN_CRAFT:
							break;
						default:
							// Launch weapon Special ability skill effect if available
							if (activeWeapon != null && !target.isDead())
							{
								if (activeWeapon.getSkillEffects(this, target, skill).length > 0 && isPlayer())
								{
									SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED);
									sm.addSkillName(skill);
									sendPacket(sm);
								}
							}
							
							// Maybe launch chance skills on us
							if (_chanceSkills != null)
								_chanceSkills.onSkillHit(target, skill, false);
							// Maybe launch chance skills on target
							if (target.getChanceSkills() != null)
								target.getChanceSkills().onSkillHit(this, skill, true);
					}
				}
			}
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);
			
			L2PcInstance player = getActingPlayer();
			if (player != null)
			{
				for (L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof L2Character)
					{
						if (skill.isNeutral())
						{
							// no flags
						}
						else if (skill.isOffensive())
						{
							if (target.isPlayer() || target.isSummon() || target.isTrap())
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									if (target.isPlayer())
									{
										target.getActingPlayer().getAI().clientStartAutoAttack();
									}
									else if (target.isSummon() && ((L2Character) target).hasAI())
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}
									// attack of the own pet does not flag player
									// triggering trap not flag trap owner
									if (player.getPet() != target && !isTrap())
										player.updatePvPStatus((L2Character) target);
								}
							}
							else if (target.isL2Attackable())
							{
								switch (skill.getId())
								{
									case 51: // Lure
									case 511: // Temptation
										break;
									default:
										// add attacker into list
										((L2Character) target).addAttackerToAttackByList(this);
								}
							}
							// notify target AI about the attack
							if (((L2Character) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if (target.isPlayer())
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) && (target.getActingPlayer().getPvpFlag() > 0 || target.getActingPlayer().getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target.isL2Attackable())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
									case UNLOCK_SPECIAL:
										break;
									default:
										player.updatePvPStatus();
								}
							}
						}
					}
				}
				
				// Mobs in range 1000 see spell
				Collection<L2Object> objs = player.getKnownList().getKnownObjects().values();
				for (L2Object spMob : objs)
				{
					if (spMob.isNpc())
					{
						L2Npc npcMob = (L2Npc) spMob;
						
						if ((npcMob.isInsideRadius(player, 1000, true, true)) && (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null))
							for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
								quest.notifySkillSee(npcMob, player, skill, targets, isSummon());
					}
				}
			}
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					default:
						for (L2Object target : targets)
						{
							if (target instanceof L2Character && ((L2Character) target).hasAI())
							{
								// notify target AI about the attack
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
							}
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": callSkill() failed.", e);
		}
	}
	
	/**
	 * @param target 
	 * @return True if the L2Character is behind the target and can't be seen.
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		
		if (target == null)
			return false;
		
		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= -360 + maxAngleDiff)
				angleDiff += 360;
			if (angleDiff >= 360 - maxAngleDiff)
				angleDiff -= 360;
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (Config.DEBUG)
					_log.info("Char " + getName() + " is behind " + target.getName());
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
	 * @param target 
	 * @return True if the target is facing the L2Character.
	 */
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 60;
		if (target == null)
			return false;
		
		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		if (Math.abs(angleDiff) <= maxAngleDiff)
			return true;
		return false;
	}
	
	/**
	 * @param target 
	 * @param maxAngle 
	 * @return true if target is in front of L2Character (shield def etc)
	 */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
			return false;
		maxAngleDiff = maxAngle / 2.;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(this.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		if (Math.abs(angleDiff) <= maxAngleDiff)
			return true;
		return false;
	}
	
	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
			return isInFrontOf((L2Character) target);
		return false;
	}
	
	/**
	 * @return the Level Modifier ((level + 89) / 100).
	 */
	public float getLevelMod()
	{
		return ((getLevel() + 89) / 100f);
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	/** 
	 * Sets _isCastingNow to true and _castInterruptTime is calculated from end time (ticks) 
	 * @param newSkillCastEndTick
	 */
	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		// for interrupt -400 ms
		_castInterruptTime = newSkillCastEndTick - 4;
	}
	
	private boolean _AIdisabled = false;
	
	public void updatePvPFlag(int value)
	{
		// Overridden in L2PcInstance
	}
	
	/**
	 * @return a multiplier based on weapon random damage
	 */
	public final double getRandomDamageMultiplier()
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		int random;
		
		if (activeWeapon != null)
			random = activeWeapon.getRandomDamage();
		else
			random = 5 + (int) Math.sqrt(getLevel());
		
		return (1 + ((double) Rnd.get(0 - random, random) / 100));
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 * @return 
	 */
	public abstract int getLevel();
	
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	public int getCON()
	{
		return getStat().getCON();
	}
	
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	public int getINT()
	{
		return getStat().getINT();
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public final int getMaxRecoverableCp()
	{
		return getStat().getMaxRecoverableCp();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxRecoverableMp()
	{
		return getStat().getMaxRecoverableMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public int getMaxRecoverableHp()
	{
		return getStat().getMaxRecoverableHp();
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public double getPAtkMagicCreatures(L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}
	
	public double getPDefAnimals(L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}
	
	public double getPDefDragons(L2Character target)
	{
		return getStat().getPDefDragons(target);
	}
	
	public double getPDefInsects(L2Character target)
	{
		return getStat().getPDefInsects(target);
	}
	
	public double getPDefMonsters(L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}
	
	public double getPDefPlants(L2Character target)
	{
		return getStat().getPDefPlants(target);
	}
	
	public double getPDefGiants(L2Character target)
	{
		return getStat().getPDefGiants(target);
	}
	
	public double getPDefMagicCreatures(L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}
	
	/**
	 * Calculated by applying non-visible HP limit
	 * getMaxHp() = getMaxVisibleHp() * limitHp
	 * @return max visible HP for display purpose.
	 */
	public int getMaxVisibleHp()
	{
		return getStat().getMaxVisibleHp();
	}
	
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	public int getWIT()
	{
		return getStat().getWIT();
	}
	
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}
	
	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && Config.L2JMOD_CHAMPION_HP != 0)
			getStatus().reduceHp(i / Config.L2JMOD_CHAMPION_HP, attacker, awake, isDOT, false);
		else
			getStatus().reduceHp(i, attacker, awake, isDOT, false);
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	/**
	 * @return the max weight that the L2Character can load.
	 */
	public int getMaxLoad()
	{
		if (isPlayer() || isPet())
		{
			// Weight Limit = (CON Modifier*69000) * Skills
			// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
			double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
			return (int) calcStat(Stats.WEIGHT_LIMIT, baseLoad, this, null);
		}
		return 0;
	}
	
	public int getBonusWeightPenalty()
	{
		if (isPlayer() || isPet())
		{
			return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
		}
		return 0;
	}
	
	/**
	 * @return the current weight of the L2Character.
	 */
	public int getCurrentLoad()
	{
		if (isPlayer() || isPet())
		{
			return getInventory().getTotalWeight();
		}
		return 0;
	}
	
	public boolean isChampion()
	{
		return _champion;
	}
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	/**
	 * Check player max buff count
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}
	
	/**
	 * Send system message about damage.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance
	 * <li> L2ServitorInstance
	 * <li> L2PetInstance</li><BR><BR>
	 * @param target 
	 * @param damage 
	 * @param mcrit 
	 * @param pcrit 
	 * @param miss 
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}
	
	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}
	
	public byte getAttackElement()
	{
		return getStat().getAttackElement();
	}
	
	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}
	
	public int getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}
	
	public final void startPhysicalAttackMuted()
	{
		abortAttack();
	}
	
	public final void stopPhysicalAttackMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PHYSICAL_ATTACK_MUTE);
		else
			removeEffect(effect);
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	/** Task for potion and herb queue */
	private static class UsePotionTask implements Runnable
	{
		private final L2Character _activeChar;
		private final L2Skill _skill;
		
		UsePotionTask(L2Character activeChar, L2Skill skill)
		{
			_activeChar = activeChar;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.doSimultaneousCast(_skill);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "", e);
			}
		}
	}
	
	/**
	 * @return true
	 */
	public boolean giveRaidCurse()
	{
		return true;
	}
	
	/**
	 * Check if target is affected with special buff
	 * @see CharEffectList#isAffected(int)
	 * @param flag int
	 * @return boolean
	 */
	public boolean isAffected(int flag)
	{
		return _effects.isAffected(flag);
	}
	
	public int getMinShopDistance()
	{
		return 0;
	}
	
	public void broadcastSocialAction(int id)
	{
		broadcastPacket(new SocialAction(getObjectId(), id));
	}
	
	
	/**
	* For Premium Service
	 * @param PS 
	*/
	public void setPremiumService(int PS)
	{
		_PremiumService=PS;
	}
	
	public int getPremiumService()
	{
		return _PremiumService;
	}
	
	public boolean isPremium()
	{
		return _PremiumService == 1;
	}
	
	public int getTeam()
	{
		return _team;
	}
	
	public void setTeam(int id)
	{
		if (id >= 0 && id <= 2)
		{
			_team = id;
		}
	}
	
	// LISTENERS
	
	/**
	 * Fires the attack listeners, if any. Returns false if the attack is to be blocked.
	 * @param target
	 * @return
	 */
	private boolean fireAttackListeners(L2Character target)
	{
		if (target != null && (!attackListeners.isEmpty() || !target.getAttackListeners().isEmpty()))
		{
			AttackEvent event = new AttackEvent();
			event.setTarget(target);
			event.setAttacker(this);
			for (AttackListener listener : attackListeners)
			{
				if (!listener.onAttack(event))
				{
					return false;
				}
			}
			for (AttackListener listener : target.getAttackListeners())
			{
				if (!listener.isAttacked(event))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Fires the skill cast listeners, if any. Returns false if the attack is to be blocked.
	 * @param skill
	 * @param simultaneously
	 * @param target
	 * @param targets
	 * @return
	 */
	private boolean fireSkillCastListeners(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if (skill != null && (target != null || targets.length != 0) && (!skillUseListeners.isEmpty() || !globalSkillUseListeners.isEmpty()))
		{
			int arraySize = (target == null ? 0 : 1) + (targets == null ? 0 : targets.length);
			L2Object[] t = new L2Object[arraySize];
			int i = 0;
			if (target != null)
			{
				t[0] = target;
				i++;
			}
			if (targets != null)
			{
				System.arraycopy(targets, 0, t, i, targets.length);
				
				if (targets.length > 0)
				{
					SkillUseEvent event = new SkillUseEvent();
					event.setCaster(this);
					event.setSkill(skill);
					event.setTargets(t);
					for (SkillUseListener listener : skillUseListeners)
					{
						int skillId = skill.getId();
						if (listener.getSkillId() == -1 || skillId == listener.getSkillId())
						{
							if (!listener.onSkillUse(event))
							{
								return false;
							}
						}
					}
					for (SkillUseListener listener : globalSkillUseListeners)
					{
						int npcId = listener.getNpcId();
						int skillId = listener.getSkillId();
						boolean skillOk = skillId == -1 || skillId == skill.getId();
						boolean charOk = (npcId == -1 && this instanceof L2NpcInstance) || (npcId == -2 && isPlayer()) || npcId == -3 || (this instanceof L2NpcInstance && ((L2NpcInstance) this).getNpcId() == npcId);
						if (skillOk && charOk)
						{
							if (!listener.onSkillUse(event))
							{
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Fires the death listeners, if any.<br>
	 * If it returns false, the doDie method will return false (and the L2Character wont die)<br>
	 * The method is public so that L2Playable can access it.
	 * @param killer
	 * @return
	 */
	public boolean fireDeathListeners(L2Character killer)
	{
		if (killer != null && (!deathListeners.isEmpty() || !globalDeathListeners.isEmpty() || !killer.getDeathListeners().isEmpty()))
		{
			DeathEvent event = new DeathEvent();
			event.setKiller(killer);
			event.setVictim(this);
			for (DeathListener listener : deathListeners)
			{
				if (!listener.onDeath(event))
				{
					return false;
				}
			}
			for (DeathListener listener : killer.getDeathListeners())
			{
				if (!listener.onKill(event))
				{
					return false;
				}
			}
			for (DeathListener listener : globalDeathListeners)
			{
				if (killer.isPlayer() || isPlayer())
				{
					if (!listener.onDeath(event))
					{
						return false;
					}
					
				}
			}
		}
		return true;
	}

	
	/**
	 * Adds an attack listener
	 * @param listener
	 */
	public void addAttackListener(AttackListener listener)
	{
		if (!attackListeners.contains(listener))
		{
			attackListeners.add(listener);
		}
	}
	
	/**
	 * Removes an attack listener
	 * @param listener
	 */
	public void removeAttackListener(AttackListener listener)
	{
		attackListeners.remove(listener);
	}
	
	/**
	 * Returns the attack listeners
	 * @return
	 */
	public List<AttackListener> getAttackListeners()
	{
		return attackListeners;
	}
	
	/**
	 * Adds a death listener.<br>
	 * Triggered when this char kills another char, and when this char gets killed by another char.
	 * @param listener
	 */
	public void addDeathListener(DeathListener listener)
	{
		if (!deathListeners.contains(listener))
		{
			deathListeners.add(listener);
		}
	}
	
	/**
	 * removes a death listener
	 * @param listener
	 */
	public void removeDeathListener(DeathListener listener)
	{
		deathListeners.remove(listener);
	}
	
	/**
	 * Returns the death listeners
	 * @return
	 */
	public List<DeathListener> getDeathListeners()
	{
		return deathListeners;
	}
	
	/**
	 * Adds a global death listener
	 * @param listener
	 */
	public static void addGlobalDeathListener(DeathListener listener)
	{
		if (!globalDeathListeners.contains(listener))
		{
			globalDeathListeners.add(listener);
		}
	}
	
	public static List<DeathListener> getGlobalDeathListeners()
	{
		return globalDeathListeners;
	}
	
	/**
	 * Removes a global death listener
	 * @param listener
	 */
	public static void removeGlobalDeathListener(DeathListener listener)
	{
		globalDeathListeners.remove(listener);
	}
	
	/**
	 * Adds a skill use listener
	 * @param listener
	 */
	public void addSkillUseListener(SkillUseListener listener)
	{
		if (!skillUseListeners.contains(listener))
		{
			skillUseListeners.add(listener);
		}
	}
	
	/**
	 * Removes a skill use listener
	 * @param listener
	 */
	public void removeSkillUseListener(SkillUseListener listener)
	{
		skillUseListeners.remove(listener);
	}
	
	/**
	 * Adds a global skill use listener
	 * @param listener
	 */
	public static void addGlobalSkillUseListener(SkillUseListener listener)
	{
		if (!globalSkillUseListeners.contains(listener))
		{
			globalSkillUseListeners.add(listener);
		}
	}
	
	/**
	 * Removes a global skill use listener
	 * @param listener
	 */
	public static void removeGlobalSkillUseListener(SkillUseListener listener)
	{
		globalSkillUseListeners.remove(listener);
	}
	
	/**
	 * Sets the character's spiritshot charge to none, if the skill allows it.
	 * @param skill 
	 */
	public void spsUncharge(L2Skill skill)
	{
		if (!skill.isStatic())
			spsUncharge();
	}
	
	/**
	 * Sets the character's spiritshot charge to none.
	 */
	public void spsUncharge()
	{	
		if (isPlayer())
		{
			L2ItemInstance weapon = getActiveWeaponInstance();
			if (weapon != null)
			{
				weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (isSummon()) // If is not player, check for summon.
		{
			((L2Summon) this).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		else if (isNpc())
		{
			((L2Npc) this)._spiritshotcharged = false;
		}
	}
	
	/**
	 * Sets the character's soulshot charge to none, if the skill allows it.
	 * @param skill 
	 */
	public void ssUncharge(L2Skill skill)
	{
		if (!skill.isStatic())
			ssUncharge();
	}
	
	/**
	 * Sets the character's soulshot charge to none.
	 */
	public void ssUncharge()
	{
		if (isPlayer())
		{
			L2ItemInstance weapon = getActiveWeaponInstance();
			if (weapon != null)
			{
				weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (isSummon()) // If is not player, check for summon.
		{
			((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
		}
		else if (isNpc())
		{
			((L2Npc) this)._soulshotcharged = false;
		}
	}
		
	public boolean isSoulshotCharged(L2Skill skill)
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if (isPlayer() && !skill.isMagic() && (weapon != null) && (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT))
		{
			return true;
		}
		else if (isNpc() && ((L2Npc) this)._soulshotcharged)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT;
	}
	
	public boolean isSpiritshotCharged(L2Skill skill)
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if (isPlayer() && skill.isMagic() && (weapon != null) && (weapon.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
		{
			return true;
		}
		else if (isNpc() && ((L2Npc) this)._spiritshotcharged)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT;
	}
	
	public boolean isBlessedSpiritshotCharged(L2Skill skill)
	{
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (isPlayer() && skill.isMagic() && weaponInst != null && weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT;
	}
	
	// Synerge - This sets that this character is no affected by lethal effects
	private boolean _isNoLethal = false;
	
	public final boolean isNoLethal()
	{
		return _isNoLethal;
	}
	
	public final void setIsNoLethal(boolean value)
	{
		_isNoLethal = value;
	}
	
	// Synerge - This sets mobs that do not attack ever
	private boolean _isAttackDisabled = false;
	
	public final boolean isAttackDisabled()
	{
		return _isAttackDisabled;
	}

	public final void setIsAttackDisabled(boolean value)
	{
		_isAttackDisabled = value;
	}
}
