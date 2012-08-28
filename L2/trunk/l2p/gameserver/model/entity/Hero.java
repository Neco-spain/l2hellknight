package l2p.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import l2p.commons.dbutils.DbUtils;
import l2p.gameserver.Config;
import l2p.gameserver.data.StringHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.database.mysql;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.entity.olympiad.Olympiad;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hero
{
  private static final Logger _log = LoggerFactory.getLogger(Hero.class);
  private static Hero _instance;
  private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
  private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
  private static Map<Integer, StatsSet> _heroes;
  private static Map<Integer, StatsSet> _completeHeroes;
  private static Map<Integer, List<HeroDiary>> _herodiary;
  private static Map<Integer, String> _heroMessage;
  public static final String COUNT = "count";
  public static final String PLAYED = "played";
  public static final String CLAN_NAME = "clan_name";
  public static final String CLAN_CREST = "clan_crest";
  public static final String ALLY_NAME = "ally_name";
  public static final String ALLY_CREST = "ally_crest";
  public static final String ACTIVE = "active";
  public static final String MESSAGE = "message";

  public static Hero getInstance()
  {
    if (_instance == null)
      _instance = new Hero();
    return _instance;
  }

  public Hero()
  {
    init();
  }

  private static void HeroSetClanAndAlly(int charId, StatsSet hero)
  {
    Map.Entry e = ClanTable.getInstance().getClanAndAllianceByCharId(charId);
    hero.set("clan_crest", e.getKey() == null ? 0 : ((Clan)e.getKey()).getCrestId());
    hero.set("clan_name", e.getKey() == null ? "" : ((Clan)e.getKey()).getName());
    hero.set("ally_crest", e.getValue() == null ? 0 : ((Alliance)e.getValue()).getAllyCrestId());
    hero.set("ally_name", e.getValue() == null ? "" : ((Alliance)e.getValue()).getAllyName());
    e = null;
  }

  private void init()
  {
    _heroes = new ConcurrentHashMap();
    _completeHeroes = new ConcurrentHashMap();
    _herodiary = new ConcurrentHashMap();
    _heroMessage = new ConcurrentHashMap();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM heroes WHERE played = 1");
      rset = statement.executeQuery();
      while (rset.next())
      {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", Olympiad.getNobleName(charId));
        hero.set("class_id", Olympiad.getNobleClass(charId));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));
        hero.set("active", rset.getInt("active"));
        HeroSetClanAndAlly(charId, hero);
        loadDiary(charId);
        loadMessage(charId);
        _heroes.put(Integer.valueOf(charId), hero);
      }
      DbUtils.close(statement, rset);

      statement = con.prepareStatement("SELECT * FROM heroes");
      rset = statement.executeQuery();
      while (rset.next())
      {
        StatsSet hero = new StatsSet();
        int charId = rset.getInt("char_id");
        hero.set("char_name", Olympiad.getNobleName(charId));
        hero.set("class_id", Olympiad.getNobleClass(charId));
        hero.set("count", rset.getInt("count"));
        hero.set("played", rset.getInt("played"));
        hero.set("active", rset.getInt("active"));
        HeroSetClanAndAlly(charId, hero);
        _completeHeroes.put(Integer.valueOf(charId), hero);
      }
    }
    catch (SQLException e)
    {
      _log.warn("Hero System: Couldnt load Heroes", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    _log.info(new StringBuilder().append("Hero System: Loaded ").append(_heroes.size()).append(" Heroes.").toString());
    _log.info(new StringBuilder().append("Hero System: Loaded ").append(_completeHeroes.size()).append(" all time Heroes.").toString());
  }

  public Map<Integer, StatsSet> getHeroes()
  {
    return _heroes;
  }

  public synchronized void clearHeroes()
  {
    mysql.set("UPDATE heroes SET played = 0, active = 0");

    if (!_heroes.isEmpty()) {
      for (StatsSet hero : _heroes.values())
      {
        if (hero.getInteger("active") == 0) {
          continue;
        }
        String name = hero.getString("char_name");

        Player player = World.getPlayer(name);

        if (player != null)
        {
          PcInventory inventory = player.getInventory();
          inventory.writeLock();
          try
          {
            for (ItemInstance item : player.getInventory().getItems())
              if (item.isHeroWeapon())
                player.getInventory().destroyItem(item);
          }
          finally
          {
            inventory.writeUnlock();
          }

          player.setHero(false);
          player.updatePledgeClass();
          player.broadcastUserInfo(true);
        }
      }
    }
    _heroes.clear();
    _herodiary.clear();
  }

  public synchronized boolean computeNewHeroes(List<StatsSet> newHeroes)
  {
    if (newHeroes.size() == 0) {
      return true;
    }
    Map heroes = new ConcurrentHashMap();
    boolean error = false;

    for (StatsSet hero : newHeroes)
    {
      int charId = hero.getInteger("char_id");

      if ((_completeHeroes != null) && (_completeHeroes.containsKey(Integer.valueOf(charId))))
      {
        StatsSet oldHero = (StatsSet)_completeHeroes.get(Integer.valueOf(charId));
        int count = oldHero.getInteger("count");
        oldHero.set("count", count + 1);
        oldHero.set("played", 1);
        oldHero.set("active", 0);

        heroes.put(Integer.valueOf(charId), oldHero);
      }
      else
      {
        StatsSet newHero = new StatsSet();
        newHero.set("char_name", hero.getString("char_name"));
        newHero.set("class_id", hero.getInteger("class_id"));
        newHero.set("count", 1);
        newHero.set("played", 1);
        newHero.set("active", 0);

        heroes.put(Integer.valueOf(charId), newHero);
      }

      addHeroDiary(charId, 2, 0);
      loadDiary(charId);
    }

    _heroes.putAll(heroes);
    heroes.clear();

    updateHeroes(0);

    return error;
  }

  public void updateHeroes(int id)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO heroes (char_id, count, played, active) VALUES (?,?,?,?)");

      for (Integer heroId : _heroes.keySet())
      {
        if ((id > 0) && (heroId.intValue() != id))
          continue;
        StatsSet hero = (StatsSet)_heroes.get(heroId);
        statement.setInt(1, heroId.intValue());
        statement.setInt(2, hero.getInteger("count"));
        statement.setInt(3, hero.getInteger("played"));
        statement.setInt(4, hero.getInteger("active"));
        statement.execute();
        if ((_completeHeroes != null) && (!_completeHeroes.containsKey(heroId)))
        {
          HeroSetClanAndAlly(heroId.intValue(), hero);
          _completeHeroes.put(heroId, hero);
        }
      }
    }
    catch (SQLException e)
    {
      _log.warn("Hero System: Couldnt update Heroes");
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public boolean isHero(int id)
  {
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

  public void activateHero(Player player)
  {
    StatsSet hero = (StatsSet)_heroes.get(Integer.valueOf(player.getObjectId()));
    if (hero == null)
    {
      hero = new StatsSet();
      int charId = player.getObjectId();
      hero.set("char_name", player.getName());
      hero.set("class_id", player.getClassId());
      hero.set("count", 1);
      hero.set("played", 15);
    }
    hero.set("active", 1);

    _heroes.remove(Integer.valueOf(player.getObjectId()));
    _heroes.put(Integer.valueOf(player.getObjectId()), hero);

    if (player.getBaseClassId() == player.getActiveClassId()) {
      addSkills(player);
    }
    player.setHero(true);
    player.updatePledgeClass();
    player.broadcastPacket(new L2GameServerPacket[] { new SocialAction(player.getObjectId(), 20016) });
    if ((player.getClan() != null) && (player.getClan().getLevel() >= 5))
    {
      player.getClan().incReputation(1000, true, new StringBuilder().append("Hero:activateHero:").append(player).toString());
      player.getClan().broadcastToOtherOnlineMembers(new SystemMessage(1776).addString(player.getName()).addNumber(Math.round(1000.0D * Config.RATE_CLAN_REP_SCORE)), player);
    }
    player.broadcastUserInfo(true);
    updateHeroes(player.getObjectId());
  }

  public static void addSkills(Player player)
  {
    player.addSkill(SkillTable.getInstance().getInfo(395, 1));
    player.addSkill(SkillTable.getInstance().getInfo(396, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1374, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1375, 1));
    player.addSkill(SkillTable.getInstance().getInfo(1376, 1));
  }

  public static void removeSkills(Player player)
  {
    player.removeSkillById(Integer.valueOf(395));
    player.removeSkillById(Integer.valueOf(396));
    player.removeSkillById(Integer.valueOf(1374));
    player.removeSkillById(Integer.valueOf(1375));
    player.removeSkillById(Integer.valueOf(1376));
  }

  public void loadDiary(int charId)
  {
    List diary = new ArrayList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
      statement.setInt(1, charId);
      rset = statement.executeQuery();

      while (rset.next())
      {
        long time = rset.getLong("time");
        int action = rset.getInt("action");
        int param = rset.getInt("param");

        HeroDiary d = new HeroDiary(action, time, param);
        diary.add(d);
      }

      _herodiary.put(Integer.valueOf(charId), diary);

      if (Config.DEBUG)
        _log.info(new StringBuilder().append("Hero System: Loaded ").append(diary.size()).append(" diary entries for Hero(object id: #").append(charId).append(")").toString());
    }
    catch (SQLException e)
    {
      _log.warn(new StringBuilder().append("Hero System: Couldnt load Hero Diary for CharId: ").append(charId).toString(), e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public void showHeroDiary(Player activeChar, int heroclass, int charid, int page)
  {
    int perpage = 10;

    List mainlist = (List)_herodiary.get(Integer.valueOf(charid));

    if (mainlist != null)
    {
      NpcHtmlMessage html = new NpcHtmlMessage(activeChar, null);
      html.setFile("olympiad/monument_hero_info.htm");
      html.replace("%title%", StringHolder.getInstance().getNotNull(activeChar, "hero.diary"));
      html.replace("%heroname%", Olympiad.getNobleName(charid));
      html.replace("%message%", (String)_heroMessage.get(Integer.valueOf(charid)));

      List list = new ArrayList(mainlist);

      Collections.reverse(list);

      boolean color = true;
      StringBuilder fList = new StringBuilder(500);
      int counter = 0;
      int breakat = 0;
      for (int i = (page - 1) * 10; i < list.size(); i++)
      {
        breakat = i;
        HeroDiary diary = (HeroDiary)list.get(i);
        Map.Entry entry = diary.toString(activeChar);

        fList.append("<tr><td>");
        if (color)
          fList.append("<table width=270 bgcolor=\"131210\">");
        else
          fList.append("<table width=270>");
        fList.append(new StringBuilder().append("<tr><td width=270><font color=\"LEVEL\">").append((String)entry.getKey()).append("</font></td></tr>").toString());
        fList.append(new StringBuilder().append("<tr><td width=270>").append((String)entry.getValue()).append("</td></tr>").toString());
        fList.append("<tr><td>&nbsp;</td></tr></table>");
        fList.append("</td></tr>");
        color = !color;
        counter++;
        if (counter >= 10) {
          break;
        }
      }
      if (breakat < list.size() - 1)
      {
        html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
        html.replace("%prev_bypass%", new StringBuilder().append("_diary?class=").append(heroclass).append("&page=").append(page + 1).toString());
      }
      else {
        html.replace("%buttprev%", "");
      }
      if (page > 1)
      {
        html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
        html.replace("%next_bypass%", new StringBuilder().append("_diary?class=").append(heroclass).append("&page=").append(page - 1).toString());
      }
      else {
        html.replace("%buttnext%", "");
      }
      html.replace("%list%", fList.toString());

      activeChar.sendPacket(html);
    }
  }

  public void addHeroDiary(int playerId, int id, int param)
  {
    insertHeroDiary(playerId, id, param);

    List list = (List)_herodiary.get(Integer.valueOf(playerId));
    if (list != null)
      list.add(new HeroDiary(id, System.currentTimeMillis(), param));
  }

  private void insertHeroDiary(int charId, int action, int param)
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
      statement.setInt(1, charId);
      statement.setLong(2, System.currentTimeMillis());
      statement.setInt(3, action);
      statement.setInt(4, param);
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.error("SQL exception while saving DiaryData.", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void loadMessage(int charId)
  {
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      String message = null;
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT message FROM heroes WHERE char_id=?");
      statement.setInt(1, charId);
      rset = statement.executeQuery();
      rset.next();
      message = rset.getString("message");
      _heroMessage.put(Integer.valueOf(charId), message);
    }
    catch (SQLException e)
    {
      _log.error(new StringBuilder().append("Hero System: Couldnt load Hero Message for CharId: ").append(charId).toString(), e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }
  }

  public void setHeroMessage(int charId, String message)
  {
    _heroMessage.put(Integer.valueOf(charId), message);
  }

  public void saveHeroMessage(int charId)
  {
    if (_heroMessage.get(Integer.valueOf(charId)) == null) {
      return;
    }
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?;");
      statement.setString(1, (String)_heroMessage.get(Integer.valueOf(charId)));
      statement.setInt(2, charId);
      statement.execute();
      statement.close();
    }
    catch (SQLException e)
    {
      _log.error("SQL exception while saving HeroMessage.", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public void shutdown()
  {
    for (Iterator i$ = _heroMessage.keySet().iterator(); i$.hasNext(); ) { int charId = ((Integer)i$.next()).intValue();
      saveHeroMessage(charId); }
  }

  public int getHeroByClass(int classid)
  {
    if (!_heroes.isEmpty())
      for (Integer heroId : _heroes.keySet())
      {
        StatsSet hero = (StatsSet)_heroes.get(heroId);
        if (hero.getInteger("class_id") == classid)
          return heroId.intValue();
      }
    return 0;
  }

  public Map.Entry<Integer, StatsSet> getHeroStats(int classId)
  {
    if (!_heroes.isEmpty())
    {
      for (Map.Entry entry : _heroes.entrySet())
      {
        if (((StatsSet)entry.getValue()).getInteger("class_id") == classId)
          return entry;
      }
    }
    return null;
  }
}