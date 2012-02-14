package l2rt.gameserver.model.entity;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.database.*;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.olympiad.Olympiad;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.ClanTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class Hero
{
	private static Logger _log = Logger.getLogger(Hero.class.getName());

	private static Hero _instance;
	private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
	private static final String GET_ALL_HEROES = "SELECT * FROM heroes";

	private static Map<Integer, StatsSet> _heroes;
	private static Map<Integer, StatsSet> _completeHeroes;

	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	public static final String ACTIVE = "active";

	public static Hero getInstance()
	{
		if(_instance == null)
			_instance = new Hero();
		return _instance;
	}

	public Hero()
	{
		init();
	}

	private static void HeroSetClanAndAlly(int charId, StatsSet hero)
	{
		Entry<L2Clan, L2Alliance> e = ClanTable.getInstance().getClanAndAllianceByCharId(charId);
		hero.set(CLAN_CREST, e.getKey() == null ? 0 : e.getKey().getCrestId());
		hero.set(CLAN_NAME, e.getKey() == null ? "" : e.getKey().getName());
		hero.set(ALLY_CREST, e.getValue() == null ? 0 : e.getValue().getAllyCrestId());
		hero.set(ALLY_NAME, e.getValue() == null ? "" : e.getValue().getAllyName());
		e = null;
	}

	private void init()
	{
		_heroes = new FastMap<Integer, StatsSet>().setShared(true);
		_completeHeroes = new FastMap<Integer, StatsSet>().setShared(true);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(GET_HEROES);
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, Olympiad.getNobleName(charId));
				hero.set(Olympiad.CLASS_ID, Olympiad.getNobleClass(charId));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				hero.set(ACTIVE, rset.getInt(ACTIVE));
				HeroSetClanAndAlly(charId, hero);
				_heroes.put(charId, hero);
			}
			DatabaseUtils.closeDatabaseSR(statement, rset);

			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();
			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, Olympiad.getNobleName(charId));
				hero.set(Olympiad.CLASS_ID, Olympiad.getNobleClass(charId));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));
				hero.set(ACTIVE, rset.getInt(ACTIVE));
				HeroSetClanAndAlly(charId, hero);
				_completeHeroes.put(charId, hero);
			}
		}
		catch(SQLException e)
		{
			_log.warning("Hero System: Couldnt load Heroes");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		_log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
		_log.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
	}

	public Map<Integer, StatsSet> getHeroes()
	{
		return _heroes;
	}

	public synchronized void clearHeroes()
	{
		mysql.set("UPDATE heroes SET played = 0, active = 0");

		if(!_heroes.isEmpty())
			for(StatsSet hero : _heroes.values())
			{
				if(hero.getInteger(ACTIVE) == 0)
					continue;

				String name = hero.getString(Olympiad.CHAR_NAME);

				L2Player player = L2World.getPlayer(name);

				if(player != null)
				{
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_L_HAND, null);
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_R_HAND, null);
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND, null);
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_HAIR, null);
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_HAIRALL, null);
					player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_DHAIR, null);

					for(L2ItemInstance item : player.getInventory().getItems())
					{
						if(item == null)
							continue;
						if(item.isHeroWeapon())
							player.getInventory().destroyItem(item, 1, true);
					}

					for(L2ItemInstance item : player.getWarehouse().listItems(ItemClass.EQUIPMENT))
					{
						if(item == null)
							continue;
						if(item.isHeroWeapon())
							player.getWarehouse().destroyItem(item.getItemId(), 1);
					}

					player.setHero(false);
					player.updatePledgeClass();
					player.broadcastUserInfo(true);
				}
			}

		_heroes.clear();
	}

	public synchronized boolean computeNewHeroes(GArray<StatsSet> newHeroes)
	{
		if(newHeroes.size() == 0)
			return true;

		Map<Integer, StatsSet> heroes = new FastMap<Integer, StatsSet>().setShared(true);
		boolean error = false;

		for(StatsSet hero : newHeroes)
		{
			int charId = hero.getInteger(Olympiad.CHAR_ID);

			if(_completeHeroes != null && _completeHeroes.containsKey(charId))
			{
				StatsSet oldHero = _completeHeroes.get(charId);
				int count = oldHero.getInteger(COUNT);
				oldHero.set(COUNT, count + 1);
				oldHero.set(PLAYED, 1);
				oldHero.set(ACTIVE, 0);

				heroes.put(charId, oldHero);
			}
			else
			{
				StatsSet newHero = new StatsSet();
				newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
				newHero.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
				newHero.set(COUNT, 1);
				newHero.set(PLAYED, 1);
				newHero.set(ACTIVE, 0);

				heroes.put(charId, newHero);
			}
		}

		_heroes.putAll(heroes);
		heroes.clear();

		updateHeroes(0);

		return error;
	}

	public void updateHeroes(int id)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO heroes VALUES (?,?,?,?)");

			for(Integer heroId : _heroes.keySet())
			{
				if(id > 0 && heroId != id)
					continue;
				StatsSet hero = _heroes.get(heroId);
				try
				{
					statement.setInt(1, heroId);
					statement.setInt(2, hero.getInteger(COUNT));
					statement.setInt(3, hero.getInteger(PLAYED));
					statement.setInt(4, hero.getInteger(ACTIVE));
					statement.execute();
					if(_completeHeroes != null && !_completeHeroes.containsKey(heroId))
					{
						HeroSetClanAndAlly(heroId, hero);
						_completeHeroes.put(heroId, hero);
					}
				}
				catch(SQLException e)
				{
					_log.warning("Hero System: Couldnt update Hero: " + heroId);
					e.printStackTrace();
				}
			}

		}
		catch(SQLException e)
		{
			_log.warning("Hero System: Couldnt update Heroes");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public boolean isHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger(ACTIVE) == 1)
			return true;
		return false;
	}

	public boolean isInactiveHero(int id)
	{
		if(_heroes == null || _heroes.isEmpty())
			return false;
		if(_heroes.containsKey(id) && _heroes.get(id).getInteger(ACTIVE) == 0)
			return true;
		return false;
	}

	public void activateHero(L2Player player)
	{
		StatsSet hero = _heroes.get(player.getObjectId());
		hero.set(ACTIVE, 1);
		_heroes.remove(player.getObjectId());
		_heroes.put(player.getObjectId(), hero);

		if(player.getBaseClassId() == player.getActiveClassId())
			addSkills(player);

		player.setHero(true);
		player.updatePledgeClass();
		player.broadcastPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
		if(player.getClan() != null && player.getClan().getLevel() >= 5)
		{
			player.getClan().incReputation(5000, true, "Hero:activateHero:" + player);
			player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addString(player.getName()).addNumber(Math.round(1000 * Config.RATE_CLAN_REP_SCORE)), player);
		}
		else
			player.broadcastUserInfo(true);
		updateHeroes(player.getObjectId());
	}

	public static void addSkills(L2Player player)
	{
		player.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HEROIC_MIRACLE, 1));
		player.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HEROIC_BERSERKER, 1));
		player.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HEROIC_VALOR, 1));
		player.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HEROIC_GRANDEUR, 1));
		player.addSkill(SkillTable.getInstance().getInfo(L2Skill.SKILL_HEROIC_DREAD, 1));
	}

	public static void removeSkills(L2Player player)
	{
		player.removeSkillById(L2Skill.SKILL_HEROIC_MIRACLE);
		player.removeSkillById(L2Skill.SKILL_HEROIC_BERSERKER);
		player.removeSkillById(L2Skill.SKILL_HEROIC_VALOR);
		player.removeSkillById(L2Skill.SKILL_HEROIC_GRANDEUR);
		player.removeSkillById(L2Skill.SKILL_HEROIC_DREAD);
	}
}