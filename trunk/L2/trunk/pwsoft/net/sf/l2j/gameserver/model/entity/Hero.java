package net.sf.l2j.gameserver.model.entity;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDiary;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class Hero
{
  private static final Logger _log = AbstractLogger.getLogger(Hero.class.getName());
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
    if (_instance == null) {
      _instance = new Hero();
    }
    return _instance;
  }

  public Hero() {
    init();
  }

  private static void HeroSetClanAndAlly(int charId, StatsSet hero) {
    L2Clan clan = ClanTable.getInstance().getClanByCharId(charId);
    hero.set("clan_crest", clan == null ? 0 : clan.getCrestId());
    hero.set("clan_name", clan == null ? "" : clan.getName());
    if (clan != null) {
      hero.set("ally_crest", clan.getAllyCrestId());
      hero.set("ally_name", clan.getAllyName() == null ? "" : clan.getAllyName());
    } else {
      hero.set("ally_crest", 0);
      hero.set("ally_name", "");
    }
  }

  private void init() {
    _heroes = new FastMap().shared("Hero._heroes");
    _completeHeroes = new FastMap().shared("Hero._completeHeroes");

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT * FROM heroes WHERE played = 1");
      rset = statement.executeQuery();
      while (rset.next()) {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", getHeroName(con, charId));
        hero.set("class_id", getHeroClass(con, charId));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));
        hero.set("active", rset.getInt("active"));
        HeroSetClanAndAlly(charId, hero);
        _heroes.put(Integer.valueOf(charId), hero);
        OlympiadDiary.write(charId);
      }
      Close.SR(statement, rset);

      statement = con.prepareStatement("SELECT * FROM heroes");
      rset = statement.executeQuery();
      while (rset.next()) {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", getHeroName(con, charId));
        hero.set("class_id", getHeroClass(con, charId));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));
        hero.set("active", rset.getInt("active"));
        HeroSetClanAndAlly(charId, hero);
        _completeHeroes.put(Integer.valueOf(charId), hero);
      }
    } catch (SQLException e) {
      _log.warning("Hero System: Couldnt load Heroes");
    } finally {
      Close.CSR(con, statement, rset);
    }

    _log.info(new StringBuilder().append("Hero System: Loaded ").append(_heroes.size()).append(" Heroes.").toString());
    _log.info(new StringBuilder().append("Hero System: Loaded ").append(_completeHeroes.size()).append(" all time Heroes.").toString());
    OlympiadDiary.close();
  }

  private String getHeroName(Connect con, int objId) {
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      st = con.prepareStatement("SELECT char_name FROM `characters` WHERE `obj_Id` = ? LIMIT 0,1");
      st.setInt(1, objId);
      rset = st.executeQuery();
      if (rset.next()) {
        String str = rset.getString("char_name");
        return str;
      }
    }
    catch (Exception e)
    {
      System.out.println(new StringBuilder().append("[ERROR] Hero System: , getHeroName() error: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
    return "???";
  }

  private int getHeroClass(Connect con, int objId) {
    PreparedStatement st = null;
    ResultSet rset = null;
    try {
      st = con.prepareStatement("SELECT base_class FROM `characters` WHERE `obj_Id` = ? LIMIT 0,1");
      st.setInt(1, objId);
      rset = st.executeQuery();
      if (rset.next()) {
        int i = rset.getInt("base_class");
        return i;
      }
    }
    catch (Exception e)
    {
      System.out.println(new StringBuilder().append("[ERROR] Hero System: , getHeroClass() error: ").append(e).toString());
    } finally {
      Close.SR(st, rset);
    }
    return 0;
  }

  public Map<Integer, StatsSet> getHeroes() {
    return _heroes;
  }

  public synchronized void clearHeroes() {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE heroes SET played = ?, active = ?");
      statement.setInt(1, 0);
      statement.setInt(2, 0);
      statement.execute();
    } catch (SQLException e) {
      _log.warning("Hero System: Couldnt clearHeroes");
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }

    if (!_heroes.isEmpty()) {
      for (StatsSet hero : _heroes.values()) {
        if (hero.getInteger("active") == 0)
        {
          continue;
        }
        String name = hero.getString("char_name");

        L2PcInstance player = L2World.getInstance().getPlayer(name);

        if (player != null) {
          for (L2ItemInstance item : player.getInventory().getItems()) {
            if (item == null) {
              continue;
            }
            if (item.isHeroItem()) {
              player.destroyItem("Hero", item, player, true);
            }
          }

          player.setHero(false);
          player.broadcastUserInfo();
        }
      }
    }

    _heroes.clear();
    OlympiadDiary.clear();
  }

  public synchronized boolean computeNewHeroes(FastList<StatsSet> newHeroes) {
    if (newHeroes.size() == 0) {
      return true;
    }

    Map heroes = new FastMap();
    boolean error = false;

    for (StatsSet hero : newHeroes) {
      int charId = hero.getInteger("char_id");

      if ((_completeHeroes != null) && (_completeHeroes.containsKey(Integer.valueOf(charId)))) {
        StatsSet oldHero = (StatsSet)_completeHeroes.get(Integer.valueOf(charId));
        int count = oldHero.getInteger("count");
        oldHero.set("count", count + 1);
        oldHero.set("played", 1);
        oldHero.set("active", 0);

        heroes.put(Integer.valueOf(charId), oldHero);
      } else {
        StatsSet newHero = new StatsSet();
        newHero.set("char_name", hero.getString("char_name"));
        newHero.set("class_id", hero.getInteger("class_id"));
        newHero.set("count", 1);
        newHero.set("played", 1);
        newHero.set("active", 0);

        heroes.put(Integer.valueOf(charId), newHero);
      }
    }

    _heroes.putAll(heroes);
    heroes.clear();

    updateHeroes(0);

    return error;
  }

  public void updateHeroes(int id) {
    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO heroes VALUES (?,?,?,?)");

      for (Integer heroId : _heroes.keySet()) {
        if ((id > 0) && (heroId.intValue() != id)) {
          continue;
        }
        StatsSet hero = (StatsSet)_heroes.get(heroId);
        try {
          statement.setInt(1, heroId.intValue());
          statement.setInt(2, hero.getInteger("count"));
          statement.setInt(3, hero.getInteger("played"));
          statement.setInt(4, hero.getInteger("active"));
          statement.execute();
          if ((_completeHeroes != null) && (!_completeHeroes.containsKey(heroId))) {
            HeroSetClanAndAlly(heroId.intValue(), hero);
            _completeHeroes.put(heroId, hero);
          }
        } catch (SQLException e) {
          _log.warning(new StringBuilder().append("Hero System: Couldnt update Hero: ").append(heroId).toString());
          e.printStackTrace();
        }
      }
    }
    catch (SQLException e) {
      _log.warning("Hero System: Couldnt update Heroes");
      e.printStackTrace();
    } finally {
      Close.CS(con, statement);
    }
  }

  public boolean isHero(int id) {
    if ((_heroes == null) || (_heroes.isEmpty())) {
      return false;
    }

    return (_heroes.containsKey(Integer.valueOf(id))) && (((StatsSet)_heroes.get(Integer.valueOf(id))).getInteger("active") == 1);
  }

  public boolean isInactiveHero(int id)
  {
    if ((_heroes == null) || (_heroes.isEmpty())) {
      return false;
    }

    return (_heroes.containsKey(Integer.valueOf(id))) && (((StatsSet)_heroes.get(Integer.valueOf(id))).getInteger("active") == 0);
  }

  public void activateHero(L2PcInstance player)
  {
    StatsSet hero = (StatsSet)_heroes.get(Integer.valueOf(player.getObjectId()));
    hero.set("active", 1);
    _heroes.remove(Integer.valueOf(player.getObjectId()));
    _heroes.put(Integer.valueOf(player.getObjectId()), hero);

    player.setHero(true);
    player.broadcastPacket(new SocialAction(player.getObjectId(), 16));

    String heroclass = CharTemplateTable.getClassNameById(player.getBaseClass());
    if (player.getClan() != null) {
      Announcements.getInstance().announceToAll(new StringBuilder().append(player.getName()).append(" \u0438\u0437 \u043A\u043B\u0430\u043D\u0430 ").append(player.getClan().getName()).append(" \u0441\u0442\u0430\u043B \u0433\u0435\u0440\u043E\u0435\u043C \u0432 \u043A\u043B\u0430\u0441\u0441\u0435 ").append(heroclass).append(". \u041F\u043E\u0437\u0434\u0440\u0430\u0432\u043B\u044F\u0435\u043C!").toString());
      if (player.getClan().getLevel() >= 5) {
        player.getClan().addPoints(1000);
        player.getClan().broadcastMessageToOnlineMembers(new StringBuilder().append("\u0427\u043B\u0435\u043D\u0430 \u043A\u043B\u0430\u043D\u0430 ").append(player.getName()).append(" \u0441\u0442\u0430\u043B \u0433\u0435\u0440\u043E\u0435\u043C. ").append(Config.ALT_CLAN_REP_MUL > 1.0F ? 1000.0F * Config.ALT_CLAN_REP_MUL : 1000.0F).append(" \u043E\u0447\u043A\u043E\u0432 \u0431\u044B\u043B\u043E \u0434\u043E\u0431\u0430\u0432\u043B\u0435\u043D\u043E \u043A \u0441\u0447\u0435\u0442\u0443 \u0440\u0435\u043F\u0443\u0442\u0430\u0446\u0438\u0438 \u0412\u0430\u0448\u0435\u0433\u043E \u043A\u043B\u0430\u043D\u0430.").toString());
      }
    } else {
      Announcements.getInstance().announceToAll(new StringBuilder().append(player.getName()).append(" \u0441\u0442\u0430\u043B \u0433\u0435\u0440\u043E\u0435\u043C \u0432 \u043A\u043B\u0430\u0441\u0441\u0435 ").append(heroclass).append(". \u041F\u043E\u0437\u0434\u0440\u0430\u0432\u043B\u044F\u0435\u043C!").toString());
    }

    player.broadcastUserInfo();
    updateHeroes(player.getObjectId());
    OlympiadDiary.addRecord(player, "\u041F\u043E\u043B\u0443\u0447\u0435\u043D\u0438\u0435 \u0433\u0435\u0440\u043E\u0439\u0441\u0442\u0432\u0430.");
  }

  public static void addSkills(L2PcInstance player) {
    for (L2Skill s : HeroSkillTable.getHeroSkills())
      player.addSkill(s, false);
  }

  public static void removeSkills(L2PcInstance player)
  {
    for (L2Skill s : HeroSkillTable.getHeroSkills())
      player.removeSkill(s);
  }
}