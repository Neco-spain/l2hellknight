package l2rt.gameserver.tables;

import javolution.text.TextBuilder;
import l2rt.Config;
import l2rt.database.*;
import l2rt.extensions.scripts.Scripts;
import l2rt.gameserver.cache.InfoCache;
import l2rt.gameserver.model.L2DropData;
import l2rt.gameserver.model.L2MinionData;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2TamedBeastInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.StatsSet;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.DropList;
import l2rt.util.GArray;
import l2rt.util.Log;

import java.io.File;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NpcTable
{
	private static final Logger _log = Logger.getLogger(NpcTable.class.getName());

	private static NpcTable _instance;

	private static L2NpcTemplate[] _npcs;
	private static HashMap<Integer, StatsSet> ai_params;
	private static GArray<L2NpcTemplate>[] _npcsByLevel;
	private static HashMap<String, L2NpcTemplate> _npcsNames;
	private static boolean _initialized = false;

	public static NpcTable getInstance()
	{
		if(_instance == null)
			_instance = new NpcTable();

		return _instance;
	}

	@SuppressWarnings("unchecked")
	private NpcTable()
	{
		_npcsByLevel = new GArray[100];
		_npcsNames = new HashMap<String, L2NpcTemplate>();
		ai_params = new HashMap<Integer, StatsSet>();
		RestoreNpcData();
	}

	private final double[] hprateskill = new double[] { 0, 1, 1.2, 1.3, 2, 2, 4, 4, 0.25, 0.5, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12 };

	private void RestoreNpcData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			try
			{
				statement = con.prepareStatement("SELECT * FROM ai_params");
				rs = statement.executeQuery();
				LoadAIParams(rs);
			}
			catch(Exception e)
			{}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}

			try
			{
				statement = con.prepareStatement("SELECT * FROM npc WHERE ai_type IS NOT NULL");
				rs = statement.executeQuery();
				fillNpcTable(rs);
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error while creating npc table ", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}

			try
			{
				statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
				rs = statement.executeQuery();
				L2NpcTemplate npcDat;
				L2Skill npcSkill;

				GArray<Integer> unimpl = new GArray<Integer>();
				int counter = 0;
				while(rs.next())
				{
					int mobId = rs.getInt("npcid");
					npcDat = _npcs[mobId];
					if(npcDat == null)
						continue;
					short skillId = rs.getShort("skillid");
					int level = rs.getByte("level");

					// Для определения расы используется скилл 4416
					if(skillId == 4416)
						npcDat.setRace(level);

					if(skillId >= 4290 && skillId <= 4302)
					{
						_log.info("Warning! Skill " + skillId + " not used, use 4416 instead.");
						continue;
					}

					if(skillId == 4408)
						npcDat.setRateHp(hprateskill[level]);

					npcSkill = SkillTable.getInstance().getInfo(skillId, level);

					if(npcSkill == null || npcSkill.getSkillType() == SkillType.NOTDONE)
						unimpl.add(Integer.valueOf(skillId));

					if(npcSkill == null)
						continue;

					npcDat.addSkill(npcSkill);
					counter++;
				}
				new File("log/game/unimplemented_npc_skills.txt").delete();
				for(Integer i : unimpl)
					Log.add("[" + i + "] " + SkillTable.getInstance().getInfo(i, 1), "unimplemented_npc_skills", "");
				_log.info("Loaded " + counter + " npc skills.");
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error while reading npcskills table ", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}

			try
			{
				statement = con.prepareStatement("SELECT mobId, itemId, min, max, sweep, chance, category FROM droplist ORDER BY mobId, category, chance DESC");
				rs = statement.executeQuery();
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;

				while(rs.next())
				{
					int mobId = rs.getInt("mobId");
					npcDat = _npcs[mobId];
					if(npcDat != null)
					{
						dropDat = new L2DropData();

						int id = rs.getShort("itemId");
						if(ItemTemplates.getInstance().getTemplate(id).isCommonItem())
						{
                            dropDat.setItemId(id);
                            dropDat.setChance(rs.getInt("chance") * Config.RATE_DROP_COMMON_ITEMS);
						}
						else
						{
							dropDat.setItemId(id);
							dropDat.setChance(rs.getInt("chance"));
						}
						dropDat.setMinDrop(rs.getInt("min"));
						dropDat.setMaxDrop(rs.getInt("max"));
						dropDat.setSweep(rs.getInt("sweep") == 1);
						if(dropDat.getItem().isArrow() || dropDat.getItemId() == 1419)
							dropDat.setGroupId(Byte.MAX_VALUE); // группа для нерейтуемых предметов, сюда же надо всякую фигню
						else
							dropDat.setGroupId(rs.getInt("category"));
						npcDat.addDropData(dropDat);
					}
				}

				for(L2NpcTemplate temp : _npcs)
					if(temp != null && temp.getDropData() != null)
						if(!temp.getDropData().validate())
							_log.warning("Problems with droplist for " + temp.toString());

				if(Config.ALT_GAME_SHOW_DROPLIST && !Config.ALT_GAME_GEN_DROPLIST_ON_DEMAND)
					FillDropList();
				else
					_log.info("Players droplist load skipped");

				loadKillCount();
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error reading npc drops ", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}

			try
			{
				statement = con.prepareStatement("SELECT boss_id, minion_id, amount FROM minions");
				rs = statement.executeQuery();
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				int cnt = 0;

				while(rs.next())
				{
					int raidId = rs.getInt("boss_id");
					npcDat = _npcs[raidId];
					minionDat = new L2MinionData();
					minionDat.setMinionId(rs.getInt("minion_id"));
					minionDat.setAmount(rs.getByte("amount"));
					npcDat.addRaidData(minionDat);
					cnt++;
				}

				_log.config("NpcTable: Loaded " + cnt + " Minions.");
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error loading minions", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}

			try
			{
				statement = con.prepareStatement("SELECT npc_id, class_id FROM skill_learn");
				rs = statement.executeQuery();
				L2NpcTemplate npcDat = null;
				int cnt = 0;

				while(rs.next())
				{
					npcDat = _npcs[rs.getInt(1)];
					npcDat.addTeachInfo(ClassId.values()[rs.getInt(2)]);
					cnt++;
				}

				_log.config("NpcTable: Loaded " + cnt + " SkillLearn entrys.");
			}
			catch(Exception e)
			{
				_log.log(Level.SEVERE, "error loading minions", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseSR(statement, rs);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Cannot find connection to database");
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}

		_initialized = true;

		Scripts.getInstance();
	}

	private static void LoadAIParams(ResultSet AIData) throws Exception
	{
		int ai_params_counter = 0;
		StatsSet set = null;
		int npc_id;
		String param, value;
		while(AIData.next())
		{
			npc_id = AIData.getInt("npc_id");
			param = AIData.getString("param");
			value = AIData.getString("value");
			if(ai_params.containsKey(npc_id))
				set = ai_params.get(npc_id);
			else
			{
				set = new StatsSet();
				ai_params.put(npc_id, set);
			}
			set.set(param, value);
			ai_params_counter++;
		}
		_log.info("NpcTable: Loaded " + ai_params_counter + " AI params for " + ai_params.size() + " NPCs.");
	}

	private static StatsSet fillNpcTable(ResultSet NpcData) throws Exception
	{
		StatsSet npcDat = null;
		GArray<L2NpcTemplate> temp = new GArray<L2NpcTemplate>(10000);
		int maxId = 0;
		while(NpcData.next())
		{
			npcDat = new StatsSet();
			int id = NpcData.getInt("id");
			int level = NpcData.getByte("level");

			if(maxId < id)
				maxId = id;

			npcDat.set("npcId", id);
			npcDat.set("displayId", NpcData.getInt("displayId"));
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("ai_type", NpcData.getString("ai_type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("revardExp", NpcData.getInt("exp"));
			npcDat.set("revardSp", NpcData.getInt("sp"));
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getShort("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));

			npcDat.set("baseHpReg", NpcData.getDouble("base_hp_regen"));
			npcDat.set("baseCpReg", 0);
			npcDat.set("baseMpReg", NpcData.getDouble("base_mp_regen"));

			npcDat.set("baseSTR", NpcData.getInt("str"));
			npcDat.set("baseCON", NpcData.getInt("con"));
			npcDat.set("baseDEX", NpcData.getInt("dex"));
			npcDat.set("baseINT", NpcData.getInt("int"));
			npcDat.set("baseWIT", NpcData.getInt("wit"));
			npcDat.set("baseMEN", NpcData.getInt("men"));

			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));

			npcDat.set("baseShldDef", NpcData.getInt("shield_defense"));
			npcDat.set("baseShldRate", NpcData.getInt("shield_defense_rate"));

			if(NpcData.getString("type").equalsIgnoreCase("L2Pet"))
				if(NpcData.getString("name").equalsIgnoreCase("Cursed Man"))
					npcDat.set("baseCritRate", 80);
				else
					npcDat.set("baseCritRate", 44);
			else
				npcDat.set("baseCritRate", Math.max(1, NpcData.getInt("base_critical")) * 10);

			String factionId = NpcData.getString("faction_id");
			if(factionId != null)
				factionId.trim();
			npcDat.set("factionId", factionId);
			npcDat.set("factionRange", factionId == null || factionId.equals("") ? 0 : NpcData.getShort("faction_range"));

			npcDat.set("isDropHerbs", NpcData.getBoolean("isDropHerbs"));
			//npcDat.set("isHideName", NpcData.getBoolean("isHideName"));

			npcDat.set("shots", NpcData.getString("shots"));

			L2NpcTemplate template = new L2NpcTemplate(npcDat, ai_params.containsKey(id) ? ai_params.get(id) : null);
			temp.add(template);
			if(_npcsByLevel[level] == null)
				_npcsByLevel[level] = new GArray<L2NpcTemplate>();
			_npcsByLevel[level].add(template);
			_npcsNames.put(NpcData.getString("name").toLowerCase(), template);
		}
		_npcs = new L2NpcTemplate[maxId + 1];
		for(L2NpcTemplate template : temp)
			_npcs[template.npcId] = template;
		_log.config("NpcTable: Loaded " + temp.size() + " Npc Templates.");

		return npcDat;
	}

	public static void reloadNpc(int id)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			HashMap<Integer, L2Skill> skills = new HashMap<Integer, L2Skill>();
			if(old.getSkills() != null)
				skills.putAll(old.getSkills());
			/*
			 Contact with Styx to understand this commenting
			 GArray<L2DropData> drops = new GArray<L2DropData>();
			 if(old.getDropData() != null)
			 drops.addAll(old.getDropData());
			 */
			ClassId[] classIds = null;
			if(old.getTeachInfo() != null)
				classIds = old.getTeachInfo().clone();
			GArray<L2MinionData> minions = new GArray<L2MinionData>();
			minions.addAll(old.getMinionData());

			// reload the NPC base data
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
			st.setInt(1, id);
			rs = st.executeQuery();
			fillNpcTable(rs);

			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);
			for(L2Skill skill : skills.values())
				created.addSkill(skill);
			/*
			 for(L2DropData drop : drops)
			 created.addDropData(drop);
			 */
			if(classIds != null)
				for(ClassId classId : classIds)
					created.addTeachInfo(classId);
			for(L2MinionData minion : minions)
				created.addRaidData(minion);
		}
		catch(Exception e)
		{
			_log.warning("cannot reload npc " + id + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, st, rs);
		}
	}

	public static StatsSet getNpcStatsSet(int id)
	{
		StatsSet dat = null;

		ThreadConnection con = null;
		FiltredPreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
			st.setInt(1, id);
			rs = st.executeQuery();
			dat = fillNpcTable(rs);
		}
		catch(Exception e)
		{
			_log.warning("cannot load npc stats for " + id + ": " + e);
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, st, rs);
		}

		return dat;
	}

	// just wrapper
	@SuppressWarnings("unchecked")
	public void reloadAllNpc()
	{
		_npcsByLevel = new GArray[100];
		_npcsNames = new HashMap<String, L2NpcTemplate>();
		ai_params = new HashMap<Integer, StatsSet>();
		RestoreNpcData();
	}

	public void saveNpc(StatsSet npc)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		String query = "";
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			HashMap<String, Object> set = npc.getSet();
			String name = "";
			String values = "";
			for(Object obj : set.keySet())
			{
				name = (String) obj;
				if(!name.equalsIgnoreCase("npcId"))
				{
					if(!values.equals(""))
						values += ", ";
					values += name + " = '" + set.get(name) + "'";
				}
			}
			query = "UPDATE npc SET " + values + " WHERE id = ?";
			statement = con.prepareStatement(query);
			statement.setInt(1, npc.getInteger("npcId"));
			statement.execute();
		}
		catch(Exception e1)
		{
			// problem with storing spawn
			_log.warning("npc data couldnt be stored in db, query is :" + query + " : " + e1);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static boolean isInitialized()
	{
		return _initialized;
	}

	public static void replaceTemplate(L2NpcTemplate npc)
	{
		_npcs[npc.npcId] = npc;
		_npcsNames.put(npc.name.toLowerCase(), npc);
	}

	public static L2NpcTemplate getTemplate(int id)
	{
		return _npcs[id];
	}

	public static L2NpcTemplate getTemplateByName(String name)
	{
		return _npcsNames.get(name.toLowerCase());
	}

	public static GArray<L2NpcTemplate> getAllOfLevel(int lvl)
	{
		return _npcsByLevel[lvl];
	}

	public static L2NpcTemplate[] getAll()
	{
		return _npcs;
	}

	public void FillDropList()
	{
		for(L2NpcTemplate npc : _npcs)
			if(npc != null)
				InfoCache.addToDroplistCache(npc.npcId, DropList.generateDroplist(npc, null, 1, null));
		_log.info("Players droplist was cached");
	}

	public void applyServerSideTitle()
	{
		if(Config.SERVER_SIDE_NPC_TITLE_WITH_LVL)
			for(L2NpcTemplate npc : _npcs)
				if(npc != null && npc.isInstanceOf(L2MonsterInstance.class) && !npc.isInstanceOf(L2TamedBeastInstance.class))
				{
					String title = "L" + npc.level;
					if(npc.aggroRange != 0 || npc.factionRange != 0)
						title += " " + (npc.aggroRange != 0 ? "A" : "") + (npc.factionRange != 0 ? "S" : "");
					title += " ";
					npc.title = title + npc.title;
				}
	}

	public static void storeKillsCount()
	{
		ThreadConnection con = null;
		FiltredStatement fs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			TextBuilder sb = TextBuilder.newInstance();
			fs = con.createStatement();

			for(L2NpcTemplate t : NpcTable.getAll())
				if(t != null && t.killscount > 0)
				{
					fs.addBatch(sb.append("REPLACE INTO `killcount` SET `npc_id`=").append(t.npcId).append(", `count`=").append(t.killscount).append(", `char_id`=-1").toString());
					sb.clear();
				}
			TextBuilder.recycle(sb);
			fs.executeBatch();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, fs);
		}
	}

	private void loadKillCount()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet list = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM `killcount` WHERE `char_id`=-1");
			list = statement.executeQuery();
			while(list.next())
			{
				L2NpcTemplate t = NpcTable.getTemplate(list.getInt("npc_id"));
				if(t != null)
					t.killscount = list.getInt("count");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, list);
		}
	}

	public static void unload()
	{
		if(_npcs != null)
			_npcs = null;
		if(ai_params != null)
		{
			ai_params.clear();
			ai_params = null;
		}
		if(_npcsByLevel != null)
			_npcsByLevel = null;
		if(_npcsNames != null)
		{
			_npcsNames.clear();
			_npcsNames = null;
		}
		if(_instance != null)
			_instance = null;
	}
}