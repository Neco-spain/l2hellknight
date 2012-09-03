package l2rt.gameserver.instancemanager;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.TerritoryTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.xml.XmlUtils;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Rnd;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZoneManager
{
	protected static Logger _log = Logger.getLogger(ZoneManager.class.getName());

	private static ZoneManager _instance;
	private static FastMap<ZoneType, GArray<L2Zone>> _zonesByType;

	private final NoLandingZoneListener _noLandingZoneListener = new NoLandingZoneListener();
	private final MonsterTrapZoneListener _monsterTrapZoneListener = new MonsterTrapZoneListener();

	// zoneId, reflectionObjectId, reuseTime
	// FIXME Тут будет утечка памяти
	private FastMap<Integer, FastMap<Long, Long>> _reuseMonsterTrapZones = null;

	private static final long MONSTER_TRAP_DESPAWN_TIME = 5 * 60 * 1000L; // 5 min

	private ZoneManager()
	{
		parse();
	}

	public static ZoneManager getInstance()
	{
		if(_instance == null)
			_instance = new ZoneManager();
		return _instance;
	}

	public boolean checkIfInZone(ZoneType zoneType, L2Object object)
	{
		return checkIfInZone(zoneType, object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZone(ZoneType zoneType, int x, int y)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(L2Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y))
				return true;
		return false;
	}

	public boolean checkIfInZone(ZoneType zoneType, int x, int y, int z)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(L2Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y) && z >= zone.getLoc().getZmin() && z <= zone.getLoc().getZmax())
				return true;
		return false;
	}

	public boolean checkIfInZoneAndIndex(ZoneType zoneType, int index, L2Object object)
	{
		return checkIfInZoneAndIndex(zoneType, index, object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZoneAndIndex(ZoneType zoneType, int index, int x, int y, int z)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(L2Zone zone : list)
			if(zone.isActive() && zone.getIndex() == index && zone.getLoc() != null && zone.getLoc().isInside(x, y) && z >= zone.getLoc().getZmin() && z <= zone.getLoc().getZmax())
				return true;
		return false;
	}

	/*
	 * Возвращает первую попавшуюся, соответствующую условиям зону.
	 * Полезно, если нужна именно зона, а не факт нахождения в ней
	 */
	public L2Zone getZoneByType(ZoneType zoneType, int x, int y, boolean onlyActive)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(L2Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getLoc() != null && zone.getLoc().isInside(x, y))
				return zone;
		return null;
	}

	public GArray<L2Zone> getZoneByType(ZoneType zoneType)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		GArray<L2Zone> result = new GArray<L2Zone>();
		if(list == null)
			return result;
		for(L2Zone zone : list)
			if(zone.isActive())
				result.add(zone);
		return result;
	}

	public L2Zone getZoneByIndex(ZoneType zoneType, int index, boolean onlyActive)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(L2Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getIndex() == index)
				return zone;
		return null;
	}

	public L2Zone getZoneById(ZoneType zoneType, int id, boolean onlyActive)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(L2Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getId() == id)
				return zone;
		return null;
	}

	public L2Zone getZoneByTypeAndObject(ZoneType zoneType, L2Object object)
	{
		return getZoneByTypeAndCoords(zoneType, object.getX(), object.getY(), object.getZ());
	}

	public L2Zone getZoneByTypeAndCoords(ZoneType zoneType, int x, int y, int z)
	{
		GArray<L2Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;

		for(L2Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y, z))
				return zone;
		return null;
	}

	private void parse()
	{
		_zonesByType = new FastMap<ZoneType, GArray<L2Zone>>().setShared(true);

		GArray<File> files = new GArray<File>();
		hashFiles("zone", files);
		int count = 0;
		for(File f : files)
			count += parseFile(f);
		_log.config("ZoneManager: Loaded " + count + " zones");
		TerritoryTable.getInstance().registerZones();
	}

	private void hashFiles(String dirname, GArray<File> hash)
	{
		File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.config("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for(File f : files)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
			else if(f.isDirectory() && !f.getName().equals(".svn"))
				hashFiles(dirname + "/" + f.getName(), hash);
	}

	public int parseFile(File f)
	{
		Document doc = null;
		try
		{
            doc = XmlUtils.readFile(f);
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "zones file couldnt be initialized: " + f, e);
			return 0;
		}
		try
		{
			return parseDocument(doc);
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "zones file couldnt be initialized: " + f, e);
		}
		return 0;
	}

	protected int parseDocument(Document doc) {
		int count = 0;
		for(Element zone : doc.getRootElement().elements()) {
            L2Zone z = new L2Zone(XmlUtils.getIntValue(zone, "id", 0));
            z.setType(ZoneType.valueOf(zone.attributeValue("type")));
            z.setName(zone.attributeValue("name"));
            boolean enabled = true;
            for(Element set : zone.elements("set")) {
                String name = set.attributeValue("name");
                if("index".equalsIgnoreCase(name))
                    z.setIndex(XmlUtils.getIntValue(set, "val", 0));
                else if("taxById".equalsIgnoreCase(name))
                    z.setTaxById(XmlUtils.getIntValue(set, "val", 0));
                else if("entering_message_no".equalsIgnoreCase(name))
                    z.setEnteringMessageId(XmlUtils.getIntValue(set, "val", 0));
                else if("leaving_message_no".equalsIgnoreCase(name))
                    z.setLeavingMessageId(XmlUtils.getIntValue(set, "val", 0));
                else if("target".equalsIgnoreCase(name))
                    z.setTarget(set.attributeValue("val"));
                else if("skill_name".equalsIgnoreCase(name))
                    z.setSkill(set.attributeValue("val"));
                else if("skill_prob".equalsIgnoreCase(name))
                    z.setSkillProb(set.attributeValue("val"));
                else if("unit_tick".equalsIgnoreCase(name))
                    z.setUnitTick(set.attributeValue("val"));
                else if("initial_delay".equalsIgnoreCase(name))
                    z.setInitialDelay(set.attributeValue("val"));
                else if("restart_time".equalsIgnoreCase(name))
                    z.setRestartTime(XmlUtils.getLongValue(set, "val", 0));
                else if("blocked_actions".equalsIgnoreCase(name))
                    z.setBlockedActions(set.attributeValue("val"));
                else if("damage_on_hp".equalsIgnoreCase(name))
                    z.setDamageOnHP(set.attributeValue("val"));
                else if("damage_on_mp".equalsIgnoreCase(name))
                    z.setDamageOnМP(set.attributeValue("val"));
                else if("message_no".equalsIgnoreCase(name))
                    z.setMessageNumber(set.attributeValue("val"));
                else if("move_bonus".equalsIgnoreCase(name))
                    z.setMoveBonus(set.attributeValue("val"));
                else if("hp_regen_bonus".equalsIgnoreCase(name))
                    z.setRegenBonusHP(set.attributeValue("val"));
                else if("mp_regen_bonus".equalsIgnoreCase(name))
                    z.setRegenBonusMP(set.attributeValue("val"));
                else if("affect_race".equalsIgnoreCase(name))
                    z.setAffectRace(set.attributeValue("val"));
                else if("event".equalsIgnoreCase(name))
                    z.setEvent(set.attributeValue("val"));
                else if("reflectionId".equalsIgnoreCase(name))
                    z.setReflectionId(XmlUtils.getIntValue(set, "val", 0));
                else if("enabled".equalsIgnoreCase(name))
                    enabled = XmlUtils.getBooleanValue(set, "val", true) || z.getType() == ZoneType.water;
                else
                    z.setParam(name, set.attributeValue("val"));
            }
            loc: for(Iterator<Element> j = zone.elementIterator(); j.hasNext();) {
                Element e = j.next();
                if(!e.getName().startsWith("shape") && !e.getName().startsWith("restart_point") && !e.getName().startsWith("PKrestart_point"))
                    continue loc;
                int locId = IdFactory.getInstance().getNextId();
                boolean isRound = e.attributeValue("loc") != null;
                L2Territory territory;
                if(isRound) {
                    String[] coord = e.attributeValue("loc").replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
                    if(coord.length < 5) // Не указаны minZ и maxZ, берем граничные значения
                        territory = new L2RoundTerritory(locId, Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.MIN_VALUE, Integer.MAX_VALUE);
                    else
                        territory = new L2RoundTerritory(locId, Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.parseInt(coord[3]), Integer.parseInt(coord[4]));
                } else {
                    territory = new L2Territory(locId);
                    for(Element coords : e.elements("coords")) {
                        String[] coord = coords.attributeValue("loc").replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
                        if(coord.length < 4) // Не указаны minZ и maxZ, берем граничные значения
                            territory.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.MIN_VALUE, Integer.MAX_VALUE);
                        else
                            territory.add(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.parseInt(coord[3]));
                    }
                }
                if("shape".equalsIgnoreCase(e.getName())) {
                    z.setLoc(territory);
                    territory.setZone(z);
                    territory.validate();
                } else if("restart_point".equalsIgnoreCase(e.getName()))
                    z.setRestartPoints(territory);
                else if("PKrestart_point".equalsIgnoreCase(e.getName()))
                    z.setPKRestartPoints(territory);
                z.setActive(enabled);
                TerritoryTable.getInstance().getLocations().put(locId, territory);
            }
            if(z.getType() == ZoneType.no_landing || z.getType() == ZoneType.Siege || z.getType() == ZoneType.Castle || z.getType() == ZoneType.Fortress || z.getType() == ZoneType.OlympiadStadia)
                z.getListenerEngine().addMethodInvokedListener(_noLandingZoneListener);
            else if(z.getType() == ZoneType.monster_trap)
                z.getListenerEngine().addMethodInvokedListener(_monsterTrapZoneListener);
            if(_zonesByType.get(z.getType()) == null)
                _zonesByType.put(z.getType(), new GArray<L2Zone>());
            _zonesByType.get(z.getType()).add(z);
            count++;
        }
		return count;
	}

	public void reload()
	{
		parse();
	}

	public boolean checkIfInZoneFishing(int x, int y, int z)
	{
		return !Config.ENABLE_FISHING_CHECK_WATER || checkIfInZone(ZoneType.water, x, y, z);
	}

	private class NoLandingZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(player != null)
				if(player.isFlying() && !player.isBlocked() && player.getMountNpcId() == PetDataTable.WYVERN_ID)
				{
					Siege siege = SiegeManager.getSiege(player, false);
					if(siege != null)
					{
						Residence unit = siege.getSiegeUnit();
						if(unit != null && player.getClan() != null && player.isClanLeader() && (player.getClan().getHasCastle() == unit.getId() || player.getClan().getHasFortress() == unit.getId()))
							return;
					}

					player.stopMove();
					player.sendPacket(Msg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN_YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE);

					Integer enterCount = (Integer) player.getProperty(PropertyCollection.ZoneEnteredNoLandingFlying);
					if(enterCount == null)
						enterCount = 0;

					Location loc = player.getLastServerPosition();
					if(loc == null || enterCount >= 5)
					{
						player.setMount(0, 0, 0);
						player.addProperty(PropertyCollection.ZoneEnteredNoLandingFlying, 0);
						return;
					}

					player.teleToLocation(loc);
					player.addProperty(PropertyCollection.ZoneEnteredNoLandingFlying, enterCount + 1);
				}
				else if(Config.ALT_DONT_ALLOW_PETS_ON_SIEGE && player.getPet() != null)
				{
					int id = player.getPet().getNpcId();
					if((PetDataTable.isBabyPet(id) || PetDataTable.isImprovedBabyPet(id)) && SiegeManager.getSiege(player, true) != null)
					{
						player.getPet().unSummon();
						player.sendMessage("Этих питомцев запрещено использовать в зонах осад.");
					}
				}
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{}
	}

	private class MonsterTrapZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2Player player = object.getPlayer();
			if(player == null || zone.getEvent() == null)
				return;

			Reflection r = player.getReflection();
			if(r.getInstancedZoneId() == zone.getReflectionId())
			{
				// Структура: reuse;chance1,id11,id12,id1N;chance2,id221,id22,id2N;chanceM,idM1,idM2,idMN; .....
				String[] params = zone.getEvent().split(";");
				int reuse = Integer.parseInt(params[0]); // В секундах
				long zoneReuse = getReuseMonsterTrapZone(zone.getId(), r.getId());
				if(zoneReuse != 0 && zoneReuse + reuse * 1000L > System.currentTimeMillis())
					return;
				setReuseMonsterTrapZone(zone.getId(), r.getId(), System.currentTimeMillis());
				int[] chances = new int[params.length - 1];
				int[][] groups = new int[params.length - 1][];
				for(int i = 1; i < params.length; i++)
				{
					// Структура: chance,id1,id2,idN
					String[] group = params[i].split(",");
					chances[i - 1] = Integer.parseInt(group[0]);
					int[] mobs = new int[group.length - 1];
					for(int j = 1; j < group.length; j++)
						mobs[j - 1] = Integer.parseInt(group[j]);
					groups[i - 1] = mobs;
				}
				int[] monsters = groups[choose_group(chances)];
				for(int monster : monsters)
					try
					{
						L2NpcTemplate template = NpcTable.getTemplate(monster);
						if(template == null)
							continue;
						L2Spawn spawn = new L2Spawn(template);
						spawn.setLocation(zone.getLoc().getId());
						spawn.setHeading(-1);
						spawn.setAmount(1);
						spawn.setReflection(r.getId());
						spawn.stopRespawn();
						L2NpcInstance mob = spawn.doSpawn(true);
						if(mob != null)
						{
							ThreadPoolManager.getInstance().scheduleAi(new UnSpawnTask(mob), MONSTER_TRAP_DESPAWN_TIME, false);
							if(mob.isAggressive() && mob.getAI().canSeeInSilentMove(player))
								mob.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, player, 1);
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
			}
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{}
	}

	private int choose_group(int[] chances)
	{
		int sum = 0;

		for(int i = 0; i < chances.length; i++)
			sum += chances[i];

		int[] table = new int[sum];
		int k = 0;

		for(int i = 0; i < chances.length; i++)
			for(int j = 0; j < chances[i]; j++)
			{
				table[k] = i;
				k++;
			}

		return table[Rnd.get(table.length)];
	}

	private void setReuseMonsterTrapZone(Integer zoneId, Long refId, Long reuseTime)
	{
		if(_reuseMonsterTrapZones == null)
			_reuseMonsterTrapZones = new FastMap<Integer, FastMap<Long, Long>>();

		FastMap<Long, Long> zoneReuse = new FastMap<Long, Long>();
		zoneReuse.put(refId, reuseTime);
		_reuseMonsterTrapZones.put(zoneId, zoneReuse);
	}

	private long getReuseMonsterTrapZone(Integer zoneId, Long refId)
	{
		if(_reuseMonsterTrapZones == null)
			return 0;

		FastMap<Long, Long> reuses = _reuseMonsterTrapZones.get(zoneId);
		if(reuses == null || reuses.isEmpty())
			return 0;

		for(Entry<Long, Long> zoneReuse : reuses.entrySet())
			if(refId.longValue() == zoneReuse.getKey().longValue())
				return zoneReuse.getValue();

		return 0;
	}

	public class UnSpawnTask implements Runnable
	{
		L2NpcInstance _monster;

		public UnSpawnTask(L2NpcInstance monster)
		{
			_monster = monster;
		}

		@Override
		public void run()
		{
			if(_monster != null)
				_monster.deleteMe();
			_monster = null;
		}
	}
}