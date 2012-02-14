package l2rt.gameserver.tables;

import l2rt.config.ConfigSystem;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2rt.gameserver.skills.DocumentSkill;
import l2rt.gameserver.skills.SkillsEngine;
import l2rt.util.GArray;
import l2rt.util.Log;

import java.io.File;
import java.sql.ResultSet;

public class SkillTable
{
	public static enum SubclassSkills
	{
		EmergentAbilityAttack(631),
		EmergentAbilityDefense(632),
		EmergentAbilityEmpower(633),
		EmergentAbilityMagicDefense(634),

		MasterAbilityAttack(637),
		MasterAbilityEmpower(638),
		MasterAbilityCasting(639),
		MasterAbilityFocus(640),
		MasterAbilityDefense(799),
		MasterAbilityMagicDefense(800),

		KnightAbilityBoostHP(641),
		KnightAbilityDefense(652),
		KnightAbilityResistCritical(804),

		EnchanterAbilityBoostMana(642),
		EnchanterAbilityManaRecycle(647),
		EnchanterAbilityBarrier(655),

		SummonerAbilityBoostHPMP(643),
		SummonerAbilityResistAttribute(1489),
		SummonerAbilitySpirit(1491),

		RogueAbilityEvasion(644),
		RogueAbilityLongShot(645),
		RogueAbilityCriticalChance(653),

		WizardAbilityManaGain(646),
		WizardAbilityManaSteal(654),
		WizardAbilityAntimagic(802),

		HealerAbilityPrayer(648),
		HealerAbilityHeal(1490),
		HealerAbilityDivineProtection(803),

		WarriorAbilityResistTrait(650),
		WarriorAbilityHaste(651),
		WarriorAbilityBoostCP(801),

		TransformDivineWarrior(656),
		TransformDivineKnight(657),
		TransformDivineRogue(658),
		TransformDivineWizard(659),
		TransformDivineSummoner(660),
		TransformDivineHealer(661),
		TransformDivineEnchanter(662);

		private int _id;

		private SubclassSkills(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}

		public static boolean isSubclassSkill(int id)
		{
			for(SubclassSkills value : values())
				if(value.getId() == id)
					return true;
			return false;
		}
	}

	private static SkillTable _instance;

	private L2Skill[][] skills;
	private int[] _baseLevels = new int[MAX_SKILL_COUNT];
	private int[] _maxSQLLevels = new int[MAX_SKILL_COUNT];

	public static SkillTable getInstance()
	{
		if(_instance == null)
			_instance = new SkillTable();
		return _instance;
	}

	//TODO если происходит ArrayIndexOutOfBounds то поднять лимит(ы)
	public static final int MAX_SKILL_COUNT = 26100;
	public static final int MAX_SKILL_LEVELS = 259;

	private SkillTable()
	{
		skills = SkillsEngine.getInstance().loadAllSkills(MAX_SKILL_COUNT, MAX_SKILL_LEVELS);
		loadBaseLevels();
		loadSqlSkills();
	}

	public void reload()
	{
		_instance = new SkillTable();
	}

	public L2Skill getInfo(int magicId, int level)
	{
		magicId--;
		level--;
		return magicId < 0 || level < 0 || magicId >= skills.length || skills[magicId] == null || level >= skills[magicId].length ? null : skills[magicId][level];
	}

	public int getMaxLevel(int magicId)
	{
		magicId--;
		return skills[magicId] == null ? 0 : skills[magicId].length;
	}

	public L2Skill[] getAllLevels(int magicId)
	{
		magicId--;
		return skills[magicId];
	}

	public int getBaseLevel(int magicId)
	{
		return _baseLevels[magicId];
	}

	private void loadBaseLevels()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id, MAX(level) AS level FROM skills WHERE level < 100 GROUP BY id");
			rset = statement.executeQuery();
			while(rset.next())
				_baseLevels[rset.getInt("id")] = rset.getInt("level");

			DatabaseUtils.closeDatabaseSR(statement, rset);

			statement = con.prepareStatement("SELECT id, MAX(level) AS level FROM skills GROUP BY id");
			rset = statement.executeQuery();
			while(rset.next())
				_maxSQLLevels[rset.getInt("id")] = rset.getInt("level");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private void loadSqlSkills()
	{
		new File("log/game/sql_skill_levels.txt").delete();
		new File("log/game/sql_skill_enchant_levels.txt").delete();
		new File("log/game/sql_skill_display_levels.txt").delete();
		new File("log/game/skills_not_standart.txt").delete();

		GArray<Integer> _incorrectSkills = new GArray<Integer>();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM skills ORDER BY id, level ASC");
			rset = statement.executeQuery();

			int lastid = 0, lastmaxlearn = 0;
			while(rset.next())
			{
				int id = rset.getInt("id");
				int display_level = rset.getInt("level");
				String name = rset.getString("name");
				//int operate_type = rset.getInt("operate_type");
				boolean is_magic = rset.getInt("is_magic") == 1;
				int mp_consume = rset.getInt("mp_consume");
				int hp_consume = rset.getInt("hp_consume");
				int cast_range = rset.getInt("cast_range");
				int hit_time = rset.getInt("hit_time");
				int power = rset.getInt("power");
				int learn = rset.getInt("learn");
				int reuse = rset.getInt("reuse");
				boolean is_enchant = !rset.getString("enchant").isEmpty();

				if(lastid != id)
				{
					lastid = id;
					lastmaxlearn = learn;
				}
				lastmaxlearn = Math.max(lastmaxlearn, learn);

				int baseLevel = _baseLevels[id];
				L2Skill base = getInfo(id, 1);
				if(base == null)
				{
					_incorrectSkills.add(id);
					Log.add("Incorrect skill base for id: " + id, "sql_skill_levels", "");
					continue;
				}

				int level = SkillTreeTable.convertEnchantLevel(baseLevel, display_level, base.getEnchantLevelCount());
				L2Skill skill = getInfo(id, level);

				if(skill == null)
				{
					if(!_incorrectSkills.contains(id))
					{
						_incorrectSkills.add(id);
						if(display_level < 100)
							Log.add("Incorrect skill levels for id: " + id + ", level = " + level + ", display_level = " + display_level, "sql_skill_levels", "");
						else
							Log.add("Not found enchant for skill id: " + id + ", level = " + level + ", display_level = " + display_level, "sql_skill_enchant_levels", "");
					}
					continue;
				}
				//mysql.set("update skills set reuse_final=" + skill.getReuseDelay() + " where id=" + id + " and level=" + display_level);

				skill._isStandart = true;
				int maxSQL = _maxSQLLevels[id];

				for(int i = level; i < MAX_SKILL_LEVELS && (i > maxSQL || i == level); i++)
				{
					skill = getInfo(id, i);
					if(skill == null)
						continue;

					// Загружаем реюз
					if(reuse > -1)
						skill.setReuseDelay(reuse);

					// Корректируем уровни скиллов, в основном для энчантов
					if(skill.getDisplayLevel() != display_level)
						Log.add("Incorrect display level: id = " + id + ", level = " + level, "sql_skill_display_levels", "");

					if(skill.getPower() == 0 && power > 0)
					{
						skill.setPower(power);
						Log.add("Not found power for skill id: " + id + ", level = " + level, "sql_skill_without_power", "");
					}
					else if(skill.getBaseValues() != null && skill.getBaseValues().contains("power"))
						if(power <= 0)
							Log.add("Incorrect power for skill id: " + id + ", level = " + level, "sql_skill_base_power", "");
						else
							skill.setPower(power);
					else if(power > 0 && skill.getPower() != power)
						Log.add("Incorrect power for skill id: " + id + ", level = " + level, "sql_skill_power", "");

					skill.setBaseLevel((short) baseLevel);

					if(skill.getMagicLevel() == 0)
						skill.setMagicLevel(lastmaxlearn);

					if(is_enchant)
						if(skill.getEnchantLevelCount() == 15)
							skill.setMagicLevel(skill.getMagicLevel() + DocumentSkill.elevels15[skill.getDisplayLevel() % 100]);
						else
							skill.setMagicLevel(DocumentSkill.elevels30[skill.getDisplayLevel() % 100]);

					skill.setCastRange(cast_range);

					if(skill.getFlyType() != FlyType.NONE && skill.getFlyRadius() > 0 && skill.getCastRange() > 0 && skill.getCastRange() < skill.getFlyRadius())
						skill.setCastRange(skill.getFlyRadius());

					skill.setName(name);

					skill.setHitTime(hit_time);

					if(skill.getSkillInterruptTime() == 0)
						skill.setSkillInterruptTime(skill.getHitTime() * 3 / 4);

					skill.setIsMagic(is_magic);
					skill.setOverhit(skill.isOverhit() || !is_magic && ConfigSystem.getBoolean("AltAllPhysSkillsOverhit"));

					//skill.setDisplayLevel((short) display_level);

					skill.setHpConsume(hp_consume);
					if(mp_consume > 0)
						if(mp_consume / 4 >= 1 && is_magic)
						{
							skill.setMpConsume1(mp_consume * 1. / 4);
							skill.setMpConsume2(mp_consume * 3. / 4);
						}
						else
							skill.setMpConsume2(mp_consume);
				}
			}

			for(L2Skill[] sl : skills)
				if(sl != null)
					for(L2Skill s : sl)
						if(s != null)
							if(!s._isStandart)
								Log.add("Not found SQL skill id: " + s.getId() + ", level = " + s.getLevel() + ", display " + s.getDisplayLevel(), "skills_not_standart", "");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/*
	operate_type
	0 - в основном физ.
	1 - в основном маг.
	2 - в основном селфы, иногда бафы.
	3 - дебафы.
	4 - герои, нублесы, скиллы захвата замка.
	5 - рыбные, предметные, аугментация, трансформация, SA.
	6 - ауры
	7 - трансформации
	11 - пассивки игроков
	12 - еще пассивки игроков, и мобов
	13 - спец. пассивки игроков, характерные только для конкретной расы. Расы мобов.
	14 - аналогично, только еще Divine Inspiration, пассивные Final скиллы, и всякая фигня намешана
	15 - клановые скиллы, скиллы фортов
	16 - сеты, эпики, SA, аугментация, все пассивное
	
	OP_PASSIVE: 11, 12, 13, 14, 15, 16
	OP_ACTIVE: 0, 1, 2, 3, 4, 5, 7
	OP_TOGGLE: 6
	OP_ON_ATTACK: 5
	OP_ON_CRIT: 5
	OP_ON_MAGIC_ATTACK: 5
	OP_ON_UNDER_ATTACK: 5
	OP_ON_MAGIC_SUPPORT: 5
	*/

	public static void unload()
	{
		if(_instance != null)
			_instance = null;
	}
}