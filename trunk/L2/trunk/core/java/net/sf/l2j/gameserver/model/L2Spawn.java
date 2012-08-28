package net.sf.l2j.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Territory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.geodata.GeoData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2Spawn
{
    protected static final Logger _log = Logger.getLogger(L2Spawn.class.getName());

    /** The link on the L2NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...) */
	private L2NpcTemplate _template;

	/** The Identifier of this spawn in the spawn table */
	private int _id;

	// private String _location = DEFAULT_LOCATION;

	/** The identifier of the location area where L2NpcInstance can be spwaned */
	private int _location;

	/** The maximum number of L2NpcInstance that can manage this L2Spawn */
	private int _maximumCount;

	/** The current number of L2NpcInstance managed by this L2Spawn */
	private int _currentCount;

	/** The current number of SpawnTask in progress or stand by of this L2Spawn */
    protected int _scheduledCount;

	/** The X position of the spwan point */
	private int _locX;

	/** The Y position of the spwan point */
	private int _locY;

	/** The Z position of the spwan point */
	private int _locZ;

	/** The heading of L2NpcInstance when they are spawned */
	private int _heading;

	/** The delay between a L2NpcInstance remove and its re-spawn */
	private int _respawnDelay;

	/** Minimum delay RaidBoss */
	private int _respawnMinDelay;

	/** Maximum delay RaidBoss */
	private int _respawnMaxDelay;

	/** The generic constructor of L2NpcInstance managed by this L2Spawn */
	private Constructor<?> _constructor;

	/** If True a L2NpcInstance is respawned each time that another is killed */
    protected boolean _doRespawn;

	public boolean isRespawnable()
	{
		return _doRespawn;
	}

    private L2NpcInstance _lastSpawn;
    private static List<SpawnListener> _spawnListeners = new FastList<SpawnListener>();

	/** The task launching the function doSpawn() */
	class SpawnTask implements Runnable
	{
		private L2NpcInstance _oldNpc;

		public SpawnTask(L2NpcInstance pOldNpc)
		{
			_oldNpc = pOldNpc;
		}

		public void run()
		{
			try
			{
				// [L2J_JP DELETE SANDMAN]respawnNpc(oldNpc);
				if (_doRespawn)
					respawnNpc(_oldNpc);
			}
			catch (Exception e)
			{
				_log.warning(e.getMessage());
			}

			_scheduledCount--;
		}
	}


	/**
	 * Constructor of L2Spawn.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...).
	 * All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Spawn is created, server just create a link between the instance and the template.
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * Each L2NpcInstance is linked to a L2Spawn that manages its spawn and respawn (delay, location...).
	 * This link is stored in <B>_spawn</B> of the L2NpcInstance<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Spawn </li>
	 * <li>Calculate the implementationName used to generate the generic constructor of L2NpcInstance managed by this L2Spawn</li>
	 * <li>Create the generic constructor of L2NpcInstance managed by this L2Spawn</li><BR><BR>
	 *
	 * @param mobTemplate The L2NpcTemplate to link to this L2Spawn
	 *
	 */
	@SuppressWarnings("rawtypes")
	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the L2Spawn
		 _template = mobTemplate;

         if (_template == null)
             return;

		 // The Name of the L2NpcInstance type managed by this L2Spawn
		 String implementationName = _template.type; // implementing class name

		if (mobTemplate.npcId == 30995)
            implementationName = "L2RaceManager";

		// if (mobTemplate.npcId == 8050)

		if ((mobTemplate.npcId >= 31046)&&(mobTemplate.npcId <= 31053))
            implementationName = "L2SymbolMaker";

		// Create the generic constructor of L2NpcInstance managed by this L2Spawn
		Class[] parameters = {int.class, Class.forName("net.sf.l2j.gameserver.templates.L2NpcTemplate")};
		_constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
	}

	/**
	 * Return the maximum number of L2NpcInstance that this L2Spawn can manage.<BR><BR>
	 */
	public int getAmount()
	{
		return _maximumCount;
	}

	/**
	 * Return the Identifier of this L2Spwan (used as key in the SpawnTable).<BR><BR>
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Return the Identifier of the location area where L2NpcInstance can be spwaned.<BR><BR>
	 */
	public int getLocation()
	{
		return _location;
	}

	/**
	 * Return the X position of the spwan point.<BR><BR>
	 */
	public int getLocx()
	{
		return _locX;
	}

	/**
	 * Return the Y position of the spwan point.<BR><BR>
	 */
	public int getLocy()
	{
		return _locY;
	}

	/**
	 * Return the Z position of the spwan point.<BR><BR>
	 */
	public int getLocz()
	{
		return _locZ;
	}

	/**
	 * Return the Itdentifier of the L2NpcInstance manage by this L2Spwan contained in the L2NpcTemplate.<BR><BR>
	 */
	public int getNpcid()
	{
		return _template.npcId;
	}

	/**
	 * Return the heading of L2NpcInstance when they are spawned.<BR><BR>
	 */
	public int getHeading()
	{
		return _heading;
	}

	/**
	 * Return the delay between a L2NpcInstance remove and its re-spawn.<BR><BR>
	 */
    public int getRespawnDelay()
    {
        return _respawnDelay;
    }
    /**
     * Return Min RaidBoss Spawn delay.<BR><BR>
    */
    public int getRespawnMinDelay()
    {
        return _respawnMinDelay;
    }
    /**
     * Return Max RaidBoss Spawn delay.<BR><BR>
    */
    public int getRespawnMaxDelay()
    {
        return _respawnMaxDelay;
    }

	/**
	 * Set the maximum number of L2NpcInstance that this L2Spawn can manage.<BR><BR>
	 */
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}

	/**
	 * Set the Identifier of this L2Spwan (used as key in the SpawnTable).<BR><BR>
	 */
	public void setId(int id)
	{
		_id = id;
	}

	/**
	 * Set the Identifier of the location area where L2NpcInstance can be spwaned.<BR><BR>
	 */
	public void setLocation(int location)
	{
		_location = location;
	}
	/**
	 * Set Minimum Respawn Delay.<BR><BR>
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	/**
	 * Set Maximum Respawn Delay.<BR><BR>
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	/**
	 * Set the X position of the spwan point.<BR><BR>
	 */
	public void setLocx(int locx)
	{
		_locX = locx;
	}

	/**
	 * Set the Y position of the spwan point.<BR><BR>
	 */
	public void setLocy(int locy)
	{
		_locY = locy;
	}

	/**
	 * Set the Z position of the spwan point.<BR><BR>
	 */
	public void setLocz(int locz)
	{
		_locZ = locz;
	}

	/**
	 * Set the heading of L2NpcInstance when they are spawned.<BR><BR>
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * Decrease the current number of L2NpcInstance of this L2Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Decrease the current number of L2NpcInstance of this L2Spawn </li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag </li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this L2Spawn </li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A respawn is possible ONLY if _doRespawn=True and _scheduledCount + _currentCount < _maximumCount</B></FONT><BR><BR>
	 *
	 */
	public void decreaseCount(L2NpcInstance oldNpc)
	{
		// Decrease the current number of L2NpcInstance of this L2Spawn
		_currentCount--;

		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (isRespawnable() && (_doRespawn && _scheduledCount + _currentCount < _maximumCount))
		{
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			_scheduledCount++;

			// Create a new SpawnTask to launch after the respawn Delay
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), _respawnDelay);
		}
	}

	/**
	 * Create the initial spawning and set _doRespawn to True.<BR><BR>
	 *
	 * @return The number of L2NpcInstance that were spawned
	 */
	public int init()
	{
		return init(false);
	}

	public int init(boolean firstspawn)
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn(false, firstspawn);
		}
		_doRespawn = true;

		return _currentCount;
	}

	/**
	 * Create a L2NpcInstance in this L2Spawn.<BR><BR>
	 */
	public L2NpcInstance spawnOne(boolean val)
	{
		return doSpawn(val);
	}

	public L2NpcInstance doSpawn(boolean isSummonSpawn)
	{
		return doSpawn(isSummonSpawn, false);
	}

	public L2NpcInstance doSpawn()
	{
		return doSpawn(false, false);
	}

	/**
	 * Set _doRespawn to False to stop respawn in thios L2Spawn.<BR><BR>
	 */
    public void stopRespawn()
    {
        _doRespawn = false;
    }

    /**
     * Set _doRespawn to True to start or restart respawn in this L2Spawn.<BR><BR>
     */
    public void startRespawn()
    {
        _doRespawn = true;
    }

	public L2NpcInstance doSpawn(boolean isSummonSpawn, boolean firstspawn)
	{
		L2NpcInstance mob = null;
		try
		{
			// Check if the L2Spawn is not a L2Pet or L2Minion or L2Decoy spawn
			if (_template.type.equalsIgnoreCase("L2Pet") || _template.type.equalsIgnoreCase("L2Minion")
					|| _template.type.equalsIgnoreCase("L2EffectPoint"))
			{
				_currentCount++;
				return mob;
			}

			// Get L2NpcInstance Init parameters and its generate an Identifier
			Object[] parameters =
			{ IdFactory.getInstance().getNextId(), _template };

			// Call the constructor of the L2NpcInstance 
			// (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance or L2FolkInstance)
			L2Object tmp = (L2Object)_constructor.newInstance(parameters);
			// Must be done before object is spawned into visible world

			if (isSummonSpawn && tmp instanceof L2Character)
				((L2Character)tmp).setShowSummonAnimation(isSummonSpawn);
			// Check if the Instance is a L2NpcInstance
			if (!(tmp instanceof L2NpcInstance))
				return mob;
			mob = (L2NpcInstance)tmp;
			return intializeNpcInstance(mob, firstspawn);
		}
		catch (Exception e)
		{
			// Spawning failed
			
			_currentCount++;
			if (Config.DEBUG)
			{
			_log.warning("NPC " + _template.npcId + " class not found: " + _template.type);
			}
		}
		return mob;
	}

	private L2NpcInstance intializeNpcInstance(L2NpcInstance mob,boolean firstspawn)
	{
		int newlocx, newlocy, newlocz;

        if (getLocx() == 0 && getLocy() == 0)
        {
            if (getLocation() == 0)
                return mob;
            // Calculate the random position in the location area
            int p[] = Territory.getInstance().getRandomPoint(getLocation());
            // Set the calculated position of the L2NpcInstance
            newlocx = p[0];
            newlocy = p[1];
            newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, p[2], p[3], _id);
        }
        else
        {
            // The L2NpcInstance is spawned at the exact position (Lox,
            // Locy,
            // Locz)
            newlocx = getLocx();
            newlocy = getLocy();
            if (Config.GEODATA)
                newlocz = GeoData.getInstance().getSpawnHeight(newlocx, newlocy, getLocz(), getLocz(), _id);
            else
                newlocz = getLocz();
        }

		for (L2Effect f : mob.getAllEffects())
		{
			if (f != null)
				mob.removeEffect(f);
		}
		mob.setDecayed(false);
		mob.getStatus().setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
		if (getHeading() == -1)
		{
			mob.setHeading(Rnd.nextInt(61794));
		}
		else
		{
			mob.setHeading(getHeading());
		}
		
		if (mob instanceof L2Attackable)
			((L2Attackable) mob).setChampion(false);

		if (Config.CHAMPION_ENABLE)
			// Set champion on next spawn
			if
			(
					mob instanceof L2MonsterInstance && !(mob instanceof L2RaidBossInstance) 
					&& !(mob instanceof L2GrandBossInstance)
					&& !getTemplate().isQuestMonster
					&& !mob.isRaid()
					&& !mob.isRaidMinion()
					&& Config.CHAMPION_FREQUENCY > 0
					&& mob.getLevel() >= Config.CHAMPION_MIN_LVL
					&& mob.getLevel() <= Config.CHAMPION_MAX_LVL
					
			)
			{
				int random = Rnd.get(100);

				if (random < Config.CHAMPION_FREQUENCY)
					((L2Attackable) mob).setChampion(true);
			}
		
		mob.setSpawn(this);
		mob.spawnMe(newlocx, newlocy, newlocz, firstspawn);
		L2Spawn.notifyNpcSpawned(mob);
		_lastSpawn = mob;
		_currentCount++;
		return mob;
	}

    public static void addSpawnListener(SpawnListener listener)
    {
        synchronized (_spawnListeners)
        {
            _spawnListeners.add(listener);
        }
    }

    public static void removeSpawnListener(SpawnListener listener)
    {
        synchronized (_spawnListeners)
        {
            _spawnListeners.remove(listener);
        }
    }

    public static void notifyNpcSpawned(L2NpcInstance npc)
    {
        synchronized (_spawnListeners)
        {
            for (SpawnListener listener : _spawnListeners)
            {
                listener.npcSpawned(npc);
            }
        }
    }

	/**
	 * @param i delay in seconds
	 */
	public void setRespawnDelay(int i)
	{
        if (i<0)
            _log.warning("respawn delay is negative for spawnId:"+_id);

        if (i<60)
            i=60;

		_respawnDelay = i * 1000;
	}

	public L2NpcInstance getLastSpawn()
	{
        return _lastSpawn;
	}

	public void respawnNpc(L2NpcInstance oldNpc)
	{
		oldNpc.refreshID();
		intializeNpcInstance(oldNpc, false);
	}

    public L2NpcTemplate getTemplate()
    {
	    return _template;
    }
}
