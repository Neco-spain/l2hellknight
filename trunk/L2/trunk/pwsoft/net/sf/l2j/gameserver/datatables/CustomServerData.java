package net.sf.l2j.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.ItemsAutoDestroy;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.EventTerritory;
import net.sf.l2j.gameserver.model.entity.EventTerritoryRound;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import org.mmocore.network.MMOConnection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CustomServerData
{
  private static final Logger _log = AbstractLogger.getLogger(CustomServerData.class.getName());
  private static CustomServerData _instance;
  public static FastTable<DonateItem> _donateItems = new FastTable();

  public static FastMap<Integer, FastTable<DonateSkill>> _cachedSkills = new FastMap().shared("CustomServerData._cachedSkills");

  public static FastMap<Integer, int[]> _shdSets = new FastMap().shared("CustomServerData._shdSets");

  public static FastMap<Integer, ChinaItem> _chinaItems = new FastMap().shared("CustomServerData._chinaItems");

  public static FastTable<Location> _zakenPoints = new FastTable();

  public static FastTable<Integer> _whiteBuffs = new FastTable();

  public static FastMap<Integer, String> _customMessages = new FastMap().shared("CustomServerData._customMessages");

  public static FastMap<Integer, Riddle> _riddles = new FastMap().shared("CustomServerData._riddles");

  public static FastMap<Integer, FastTable<DonateSkill>> _donateSkills = new FastMap().shared("CustomServerData._donateSkills");

  public static FastTable<EventTerritory> _eventZones = new FastTable();

  public static FastTable<L2Skill> _clanSkills = new FastTable();

  public static FastMap<Integer, NpcChat> _cachedNpcChat = new FastMap().shared("CustomServerData._cachedNpcChat");

  public static FastTable<StatPlayer> _statPvp = new FastTable();
  public static FastTable<StatPlayer> _statPk = new FastTable();
  public static FastTable<StatClan> _statClans = new FastTable();
  public static FastTable<StatCastle> _statCastles = new FastTable();

  private EventTerritoryRound ttw = new EventTerritoryRound();
  private static String statHome;
  public final String EMPTY = "";

  private static FastMap<Integer, FastList<Config.EventReward>> _chestDrop = new FastMap().shared("CustomServerData._chestDrop");

  private static FastMap<SpecialDrop, FastList<SpecialDropReward>> _specialDrop = new FastMap().shared("CustomServerData._specialDrop");

  private static FastMap<FastList<Integer>, NpcPenalty> _npcPenaltyItems = new FastMap().shared("CustomServerData._npcPenaltyItems");

  public static CustomServerData getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new CustomServerData();
    _instance.load();
  }

  private void load()
  {
    if (Config.ALLOW_DSHOP) {
      loadDonateShop();
    }

    if (Config.ALLOW_DSKILLS) {
      loadDonateSkills();
    }

    if (Config.ALLOW_CSHOP) {
      cacheChinaShop();
    }

    if (Config.REWARD_SHADOW) {
      cacheShadowRewards();
    }

    if (Config.ALLOW_NPC_CHAT) {
      cacheNpcChat();
    }

    if (Config.PROTECT_MOBS_ITEMS) {
      cacheNpcPenaltyItems();
    }

    cacheSoldSkills();
    cacheZakenPoints();
    cacheWhiteBuffs();
    parceCustomMessages();
    cacheRiddles();
    cacheEventZones();
    cacheCustomZones();
    cacheClanSkills();
    cacheChestsDrop();
    cacheSpecialDrop();
  }

  private void cacheSoldSkills()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      st = con.prepareStatement("DELETE FROM z_donate_skills WHERE expire > ? AND expire < ?");
      st.setInt(1, 1);
      st.setLong(2, System.currentTimeMillis());
      st.execute();
      Close.S(st);

      FastTable dst = new FastTable();
      st = con.prepareStatement("SELECT char_id, class_id, skill_id, skill_lvl, expire FROM `z_donate_skills`");
      rs = st.executeQuery();
      while (rs.next()) {
        int charId = rs.getInt("char_id");
        int classId = rs.getInt("class_id");
        int skillId = rs.getInt("skill_id");
        int skillLvl = rs.getInt("skill_lvl");
        long expire = rs.getLong("expire");
        DonateSkill ds = new DonateSkill(classId, skillId, skillLvl, expire);
        if (_cachedSkills.get(Integer.valueOf(charId)) == null) {
          dst.clear();
          dst.add(ds);
          _cachedSkills.put(Integer.valueOf(charId), dst);
        } else {
          ((FastTable)_cachedSkills.get(Integer.valueOf(charId))).add(ds);
        }
      }
    } catch (Exception e) {
      _log.warning("CustomServerData: cacheDonateSkills() error: " + e);
    } finally {
      Close.CSR(con, st, rs);
    }
  }

  public FastTable<DonateSkill> getDonateSkills(int charId)
  {
    return (FastTable)_cachedSkills.get(Integer.valueOf(charId));
  }

  public void addDonateSkill(int charId, int cls, int id, int lvl, long expire) {
    DonateSkill ds = new DonateSkill(cls, id, lvl, expire);
    if (_cachedSkills.get(Integer.valueOf(charId)) == null) {
      FastTable dst = new FastTable();
      dst.add(ds);
      _cachedSkills.put(Integer.valueOf(charId), dst);
    } else {
      ((FastTable)_cachedSkills.get(Integer.valueOf(charId))).add(ds);
    }

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO `z_donate_skills` (`char_id`, `class_id`, `skill_id`, `skill_lvl`, `expire`) VALUES (?, ?, ?, ?, ?)");
      st.setInt(1, charId);
      st.setInt(2, cls);
      st.setInt(3, id);
      st.setInt(4, lvl);
      st.setLong(5, expire);
      st.execute();
    } catch (Exception e) {
      _log.warning("CustomServerData [ERROR]: addDonateSkill() ->" + e);
    } finally {
      Close.CS(con, st);
    }
  }

  public FastTable<DonateItem> getDonateShop() {
    return _donateItems;
  }

  public DonateItem getDonateItem(int saleId) {
    return (DonateItem)_donateItems.get(saleId);
  }

  private void loadDonateShop() {
    try {
      File file = new File(Config.DATAPACK_ROOT, "data/donate_shop.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/donate_shop.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          int itemId = 0;
          int itemCount = 0;
          String itemInfoRu = "";
          String itemInfoDesc = "";

          int priceId = 0;
          int priceCount = 0;
          String priceName = "";
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("sale".equalsIgnoreCase(d.getNodeName())) {
              NamedNodeMap attrs = d.getAttributes();
              int saleId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("item".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  itemCount = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                }
                if ("info".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  itemInfoRu = attrs.getNamedItem("ru").getNodeValue();
                  itemInfoDesc = attrs.getNamedItem("description").getNodeValue();
                }
                if ("price".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  priceId = Integer.parseInt(attrs.getNamedItem("coin").getNodeValue());
                  priceCount = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                  priceName = attrs.getNamedItem("name").getNodeValue();
                }
              }
              _donateItems.add(new DonateItem(itemId, itemCount, itemInfoRu, itemInfoDesc, priceId, priceCount, priceName));
            }
        }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: " + e.toString());
    }
    _log.config("CustomServerData: Donate Shop, loaded " + _donateItems.size() + " items.");
  }

  private void loadDonateSkills() {
    try {
      File file = new File(Config.DATAPACK_ROOT, "data/donate_skills.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/donate_skills.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          int cls = 0;
          int id = 0;
          int lvl = 0;
          long expire = 0L;

          int priceId = 0;
          int priceCount = 0;
          String priceName = "";
          String icon = "";
          String info = "";
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("class".equalsIgnoreCase(d.getNodeName())) {
              FastTable skills = new FastTable();
              NamedNodeMap attrs = d.getAttributes();
              cls = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("skill".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String[] skill = attrs.getNamedItem("id").getNodeValue().split(",");
                  id = Integer.parseInt(skill[0]);
                  lvl = Integer.parseInt(skill[1]);

                  String[] price = attrs.getNamedItem("price").getNodeValue().split(",");
                  priceId = Integer.parseInt(price[0]);
                  priceCount = Integer.parseInt(price[1]);
                  priceName = price[2];

                  expire = Integer.parseInt(attrs.getNamedItem("period").getNodeValue());
                  icon = attrs.getNamedItem("icon").getNodeValue();
                  info = attrs.getNamedItem("info").getNodeValue();
                  skills.add(new DonateSkill(cls, id, lvl, expire, priceId, priceCount, priceName, icon, info));
                }
              }
              _donateSkills.put(Integer.valueOf(cls), skills);
            }
        }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: " + e.toString());
    }
    _log.config("CustomServerData: Donate Skills Shop, loaded " + _donateSkills.size() + " classes.");
  }

  private void cacheShadowRewards() {
    _shdSets.put(Integer.valueOf(1), new int[] { 2415, 2406, 5716, 5732 });
    _shdSets.put(Integer.valueOf(2), new int[] { 2417, 2392, 5723, 5739 });
    _shdSets.put(Integer.valueOf(3), new int[] { 2417, 2381, 5722, 5738 });
    _shdSets.put(Integer.valueOf(4), new int[] { 358, 2380, 2416, 5718, 5734 });

    _shdSets.put(Integer.valueOf(5), new int[] { 10673, 10680, 10681, 10682, 10683, 920, 889, 889, 858, 858 });
    _shdSets.put(Integer.valueOf(6), new int[] { 10673, 10677, 10678, 10679, 920, 889, 889, 858, 858 });
    _shdSets.put(Integer.valueOf(7), new int[] { 10673, 10674, 10675, 10676, 920, 889, 889, 858, 858 });
  }

  private void cacheWhiteBuffs() {
    int[] buffs = { 1068, 1388, 1086, 1077, 1242, 1240, 4352, 1085, 1059, 1303, 1043, 1356, 1355, 1357, 1040, 1389, 1036, 1035, 1243, 1304, 1078, 1087, 1006, 1009, 1007, 1002, 1252, 1253, 1309, 1251, 1308, 1391, 1310, 1390, 1362, 1413, 1363, 1003, 1005, 1008, 1260, 1004, 1250, 1261, 1249, 1282, 1364, 1365, 1414, 267, 270, 268, 269, 265, 264, 266, 306, 304, 308, 363, 349, 364, 274, 277, 272, 273, 276, 271, 275, 311, 307, 310, 365, 1073, 4342, 1044, 4347, 4348, 1257, 1397, 1268, 4554, 1032, 1392, 1393, 1259, 1354, 1353, 1352, 1191, 1182, 1189, 1033, 4700, 4702, 4703, 4699 };

    int i = 0;
    for (int id : buffs) {
      _whiteBuffs.add(Integer.valueOf(id));
    }

    _whiteBuffs.addAll(Config.C_ALLOWED_BUFFS);
  }

  public boolean isWhiteBuff(int id) {
    if ((id >= 3100) && (id <= 3299)) {
      return false;
    }
    if ((Config.F_BUFF.containsKey(Integer.valueOf(id))) || (Config.M_BUFF.containsKey(Integer.valueOf(id)))) {
      return true;
    }
    if (_whiteBuffs.contains(Integer.valueOf(id))) {
      return true;
    }

    return !Config.F_PROFILE_BUFFS.contains(Integer.valueOf(id));
  }

  public int[] getShadeItems(int set)
  {
    return (int[])_shdSets.get(Integer.valueOf(set));
  }

  private void cacheChinaShop() {
    _chinaItems.put(Integer.valueOf(14000), new ChinaItem(4037, 5000, 36, "\u0411\u0435\u043B\u044B\u0435 \u043A\u0440\u044B\u043B\u044C\u044F", "pDef/mDef +200; MP +3000; HP +500."));
    _chinaItems.put(Integer.valueOf(26116), new ChinaItem(4037, 5000, 36, "\u0427\u0435\u0440\u043D\u044B\u0435 \u043A\u0440\u044B\u043B\u044C\u044F", "pDef/mDef +200; MP +3000; HP +500."));
    _chinaItems.put(Integer.valueOf(50000), new ChinaItem(4037, 6000, 36, "\u0424\u043B\u0430\u0433", "pDef + 300; mDef +200; MP +3000; HP +500."));
  }

  public FastMap<Integer, ChinaItem> getChinaShop() {
    return _chinaItems;
  }

  public ChinaItem getChinaItem(int id) {
    return (ChinaItem)_chinaItems.get(Integer.valueOf(id));
  }

  private void cacheZakenPoints()
  {
    _zakenPoints.add(new Location(53950, 219860, -3488));
    _zakenPoints.add(new Location(55980, 219820, -3488));
    _zakenPoints.add(new Location(54950, 218790, -3488));
    _zakenPoints.add(new Location(55970, 217770, -3488));
    _zakenPoints.add(new Location(53930, 217760, -3488));
    _zakenPoints.add(new Location(55970, 217770, -3216));
    _zakenPoints.add(new Location(55980, 219920, -3216));
    _zakenPoints.add(new Location(54960, 218790, -3216));
    _zakenPoints.add(new Location(53950, 219860, -3216));
    _zakenPoints.add(new Location(53930, 217760, -3216));
    _zakenPoints.add(new Location(55970, 217770, -2944));
    _zakenPoints.add(new Location(55980, 219920, -2944));
    _zakenPoints.add(new Location(54960, 218790, -2944));
    _zakenPoints.add(new Location(53950, 219860, -2944));
    _zakenPoints.add(new Location(53930, 217760, -2944));
  }

  public Location getZakenPoint() {
    return (Location)_zakenPoints.get(Rnd.get(_zakenPoints.size() - 1));
  }

  public String getCharName(int objId)
  {
    return getCharName(objId, null);
  }

  public String getCharName(int objId, Connect excon) {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      if (excon == null) {
        con = L2DatabaseFactory.getInstance().getConnection();
        con.setTransactionIsolation(1);
      } else {
        con = excon;
      }

      st = con.prepareStatement("SELECT char_name FROM `characters` WHERE `obj_Id` = ? LIMIT 0,1");
      st.setInt(1, objId);
      rs = st.executeQuery();
      if (rs.next()) {
        String str = rs.getString("char_name");
        return str;
      }
    }
    catch (Exception e)
    {
      _log.warning("[ERROR] CustomServerData, getCharName() error: " + e);
    } finally {
      if (excon == null)
        Close.CSR(con, st, rs);
      else {
        Close.SR(st, rs);
      }
    }
    return "n?f";
  }

  private void parceCustomMessages() {
    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./data/static_messages.txt");
      if (!Data.exists()) { _log.warning("[ERROR] CustomServerData, parceCustomMessages() '/data/static_messages.txt' not founded. ");
        return;
      }
      fr = new FileReader(Data);
      br = new BufferedReader(fr);
      lnr = new LineNumberReader(br);
      String line;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
        {
          continue;
        }
        String[] msgs = line.split("=");
        _customMessages.put(Integer.valueOf(Integer.parseInt(msgs[0])), msgs[1]);
      }
    } catch (Exception e1) {
      _log.warning("[ERROR] CustomServerData, parceCustomMessages() error: " + e);
    } finally {
      try {
        if (fr != null) {
          fr.close();
        }
        if (br != null) {
          br.close();
        }
        if (lnr != null)
          lnr.close();
      }
      catch (Exception e1)
      {
      }
    }
  }

  public String getMessage(int msg) {
    return (String)_customMessages.get(Integer.valueOf(msg));
  }

  private void cacheRiddles() {
    _riddles.put(Integer.valueOf(99900), new Riddle("\u0441\u043E\u043B\u043D\u0446\u0435", "\u0416\u0430\u0440\u043A\u0438\u0439 \u0448\u0430\u0440 \u043D\u0430 \u043D\u0435\u0431\u0435 \u0441\u0432\u0435\u0442\u0438\u0442.<br1>\u042D\u0442\u043E\u0442 \u0448\u0430\u0440 \u043B\u044E\u0431\u043E\u0439 \u0437\u0430\u043C\u0435\u0442\u0438\u0442.<br1>\u0423\u0442\u0440\u043E\u043C \u0441\u043C\u043E\u0442\u0440\u0438\u0442 \u043A \u043D\u0430\u043C \u0432 \u043E\u043A\u043E\u043D\u0446\u0435,<br1>\u0420\u0430\u0434\u043E\u0441\u0442\u043D\u043E \u0441\u0438\u044F\u044F, ...<br>"));
    _riddles.put(Integer.valueOf(99901), new Riddle("\u0440\u0435\u043A\u0430", "\u041C\u0447\u0438\u0442\u0441\u044F \u043F\u043E \u0445\u043E\u043B\u043C\u0430\u043C \u0437\u043C\u0435\u044F,<br1>\u0412\u043B\u0430\u0433\u0443 \u0434\u0435\u0440\u0435\u0432\u0446\u0430\u043C \u043D\u0435\u0441\u044F.<br1>\u041E\u043C\u044B\u0432\u0430\u044F \u0431\u0435\u0440\u0435\u0433\u0430,<br1>\u041F\u043E \u043F\u043E\u043B\u044F\u043C \u0442\u0435\u0447\u0435\u0442 ...<br>"));
    _riddles.put(Integer.valueOf(99902), new Riddle("\u043E\u0431\u043B\u0430\u043A\u0430", "\u041E\u043D\u0438 \u043B\u0435\u0433\u043A\u0438\u0435, \u043A\u0430\u043A \u0432\u0430\u0442\u0430,<br1>\u041F\u043E \u043D\u0435\u0431\u0443 \u043F\u043B\u044B\u0432\u0443\u0442 \u043A\u0443\u0434\u0430-\u0442\u043E.<br1>\u0414\u0435\u0440\u0436\u0430\u0442 \u043F\u0443\u0442\u044C \u0438\u0437\u0434\u0430\u043B\u0435\u043A\u0430<br1>\u041A\u0430\u0440\u0430\u0432\u0435\u043B\u043B\u044B-...<br>"));
    _riddles.put(Integer.valueOf(99903), new Riddle("\u0440\u043E\u0441\u0430", "\u0412\u043E\u0442 \u0431\u0440\u0438\u043B\u044C\u044F\u043D\u0442\u044B \u043D\u0430 \u043B\u0438\u0441\u0442\u043E\u0447\u043A\u0430\u0445,<br1>\u0412\u0434\u043E\u043B\u044C \u0434\u043E\u0440\u043E\u0436\u0435\u043A \u0438 \u043D\u0430 \u043A\u043E\u0447\u043A\u0430\u0445 -<br1>\u042D\u0442\u043E \u0447\u0442\u043E \u0437\u0430 \u0447\u0443\u0434\u0435\u0441\u0430?<br1>\u041F\u043E \u0443\u0442\u0440\u0443 \u0431\u043B\u0435\u0441\u0442\u0438\u0442 ...<br>"));
    _riddles.put(Integer.valueOf(99904), new Riddle("\u0433\u0440\u043E\u0437\u0430", "\u0417\u0430\u0441\u043B\u043E\u043D\u0438\u043B\u0438 \u0442\u0443\u0447\u0438 \u0441\u043E\u043B\u043D\u0446\u0435,<br1>\u0413\u0440\u043E\u043C \u0440\u0430\u0441\u043A\u0430\u0442\u0438\u0441\u0442\u043E \u0441\u043C\u0435\u0435\u0442\u0441\u044F.<br1>\u0412 \u043D\u0435\u0431\u0435 \u043C\u043E\u043B\u043D\u0438\u0439 \u043F\u043E\u043B\u043E\u0441\u0430 -<br1>\u0417\u043D\u0430\u0447\u0438\u0442, \u043D\u0430\u0447\u0430\u043B\u0430\u0441\u044C ...<br>"));
    _riddles.put(Integer.valueOf(99905), new Riddle("\u0433\u0440\u0430\u0434", "\u0421\u044B\u043F\u043B\u0435\u0442\u0441\u044F \u0438\u0437 \u0442\u0443\u0447 \u0433\u043E\u0440\u043E\u0445,<br1>\u041F\u0440\u044B\u0433\u0430\u0435\u0442 \u043A \u043D\u0430\u043C \u043D\u0430 \u043F\u043E\u0440\u043E\u0433.<br1>\u0421 \u043A\u0440\u044B\u0448\u0438 \u043A\u0430\u0442\u0438\u0442\u0441\u044F \u043E\u043D \u0432 \u0441\u0430\u0434.<br1>\u0427\u0442\u043E \u0442\u0430\u043A\u043E\u0435? \u042D\u0442\u043E - ...<br>"));
    _riddles.put(Integer.valueOf(99906), new Riddle("\u043F\u0443\u0445", "\u0410 \u0432 \u0438\u044E\u043D\u0435 \u0431\u0435\u043B\u044B\u0439 \u0441\u043D\u0435\u0433<br1>\u0412\u043D\u043E\u0432\u044C \u043F\u043E\u0440\u0430\u0434\u043E\u0432\u0430\u043B \u043D\u0430\u0441 \u0432\u0441\u0435\u0445. -<br1>\u0411\u0443\u0434\u0442\u043E \u0440\u043E\u0439 \u043B\u0435\u043D\u0438\u0432\u044B\u0445 \u043C\u0443\u0445,<br1>\u0421 \u0442\u043E\u043F\u043E\u043B\u0435\u0439 \u0441\u043B\u0435\u0442\u0430\u0435\u0442 ...<br>"));
    _riddles.put(Integer.valueOf(99907), new Riddle("\u0434\u043E\u0436\u0434\u0438\u043A", "\u041E\u043D \u043F\u043E\u043F\u043B\u0430\u0447\u0435\u0442 \u043D\u0430\u0434 \u0441\u0430\u0434\u0430\u043C\u0438  -<br1>\u0421\u0430\u0434 \u043D\u0430\u043F\u043E\u043B\u043D\u0438\u0442\u0441\u044F \u043F\u043B\u043E\u0434\u0430\u043C\u0438.<br1>\u0414\u0430\u0436\u0435 \u043F\u044B\u043B\u044C\u043D\u044B\u0439 \u043F\u043E\u0434\u043E\u0440\u043E\u0436\u043D\u0438\u043A<br1>\u0420\u0430\u0434 \u0443\u043C\u044B\u0442\u044C\u0441\u044F \u0432 \u043B\u0435\u0442\u043D\u0438\u0439 ...<br>"));
    _riddles.put(Integer.valueOf(99908), new Riddle("\u043A\u043E\u0440\u043E\u0432\u044B", "\u041A\u0442\u043E \u043F\u0430\u0441\u0435\u0442\u0441\u044F \u043D\u0430 \u043B\u0443\u0433\u0443?<br>\u0414\u0430\u043B\u0435\u043A\u043E, \u0434\u0430\u043B\u0435\u043A\u043E<br1>\u041D\u0430 \u043B\u0443\u0433\u0443 \u043F\u0430\u0441\u0443\u0442\u0441\u044F \u043A\u043E:<br1>\u041A\u043E\u043D\u0438? \u041D\u0435\u0442, \u043D\u0435 \u043A\u043E\u043D\u0438!<br1>\u0414\u0430\u043B\u0435\u043A\u043E, \u0434\u0430\u043B\u0435\u043A\u043E<br1>\u041D\u0430 \u043B\u0443\u0433\u0443 \u043F\u0430\u0441\u0443\u0442\u0441\u044F \u043A\u043E:<br1>\u041A\u043E\u0437\u044B? \u041D\u0435\u0442, \u043D\u0435 \u043A\u043E\u0437\u044B!<br>\u0414\u0430\u043B\u0435\u043A\u043E, \u0434\u0430\u043B\u0435\u043A\u043E<br1>\u041D\u0430 \u043B\u0443\u0433\u0443 \u043F\u0430\u0441\u0443\u0442\u0441\u044F \u043A\u043E:<br>"));
    _riddles.put(Integer.valueOf(99909), new Riddle("\u043A\u0430\u0440\u0442\u043E\u0448\u043A\u0430", "\u041F\u043E\u0434 \u0437\u0435\u043C\u043B\u0435\u0439 \u0436\u0438\u0432\u0451\u0442 \u0441\u0435\u043C\u044C\u044F: <br1>\u041F\u0430\u043F\u0430, \u043C\u0430\u043C\u0430, \u0434\u0435\u0442\u043E\u043A \u0442\u044C\u043C\u0430. <br1>\u041B\u0438\u0448\u044C \u043A\u043E\u043F\u043D\u0438 \u0435\u0451 \u043D\u0435\u043C\u043D\u043E\u0436\u043A\u043E - <br1>\u0412\u043C\u0438\u0433 \u043F\u043E\u044F\u0432\u0438\u0442\u0441\u044F ...<br>"));
    _riddles.put(Integer.valueOf(99910), new Riddle("\u0431\u0430\u043A\u043B\u0430\u0436\u0430\u043D", "\u041D\u0430\u0448 \u043B\u0438\u043B\u043E\u0432\u044B\u0439 \u0433\u043E\u0441\u043F\u043E\u0434\u0438\u043D <br1>\u0421\u0440\u0435\u0434\u0438 \u043E\u0432\u043E\u0449\u0435\u0439 \u043E\u0434\u0438\u043D. <br1>\u041E\u043D \u0444\u0440\u0430\u043D\u0446\u0443\u0437\u0441\u043A\u0438\u0439 \u0433\u0440\u0430\u0444 \u0414\u0435 \u0416\u0430\u043D <br1>\u0410 \u043F\u043E-\u0440\u0443\u0441\u0441\u043A\u0438 - ...<br>"));
    _riddles.put(Integer.valueOf(99911), new Riddle("\u043A\u0430\u0431\u0430\u0447\u043E\u043A", "\u041A\u0442\u043E \u0440\u0430\u0437\u043B\u0451\u0433\u0441\u044F \u0441\u0440\u0435\u0434\u0438 \u0433\u0440\u044F\u0434\u043A\u0438, <br1>\u041A\u0442\u043E \u0438\u0433\u0440\u0430\u0442\u044C \u043D\u0435 \u043B\u044E\u0431\u0438\u0442 \u0432 \u043F\u0440\u044F\u0442\u043A\u0438? <br1>\u0412\u043E\u0442 \u0415\u043C\u0435\u043B\u044F-\u043F\u0440\u043E\u0441\u0442\u0430\u0447\u043E\u043A, <br1>\u0411\u0435\u043B\u043E\u0431\u043E\u043A\u0438\u0439 ...<br>"));
    _riddles.put(Integer.valueOf(99912), new Riddle("\u043B\u0438\u043C\u043E\u043D", "\u0416\u0435\u043B\u0442\u044B\u0439 \u0446\u0438\u0442\u0440\u0443\u0441\u043E\u0432\u044B\u0439 \u043F\u043B\u043E\u0434<br1>\u0412 \u0441\u0442\u0440\u0430\u043D\u0430\u0445 \u0441\u043E\u043B\u043D\u0435\u0447\u043D\u044B\u0445 \u0440\u0430\u0441\u0442\u0451\u0442.<br1>\u041D\u043E \u043D\u0430 \u0432\u043A\u0443\u0441 \u043A\u0438\u0441\u043B\u0435\u0439\u0448\u0438\u0439 \u043E\u043D,<br11>\u0410 \u0437\u043E\u0432\u0443\u0442 \u0435\u0433\u043E ...<br1>"));
    _riddles.put(Integer.valueOf(99913), new Riddle("\u0433\u0440\u0443\u0448\u0430", "\u0412\u0441\u0435 \u043E \u043D\u0435\u0439 \u0431\u043E\u043A\u0441\u0435\u0440\u044B \u0437\u043D\u0430\u044E\u0442  <br1>\u0421 \u043D\u0435\u0439 \u0443\u0434\u0430\u0440 \u0441\u0432\u043E\u0439 \u0440\u0430\u0437\u0432\u0438\u0432\u0430\u044E\u0442.<br1>\u0425\u043E\u0442\u044C \u043E\u043D\u0430 \u0438 \u043D\u0435\u0443\u043A\u043B\u044E\u0436\u0430,<br1>\u041D\u043E \u043D\u0430 \u0444\u0440\u0443\u043A\u0442 \u043F\u043E\u0445\u043E\u0436\u0430 ...<br1>"));
    _riddles.put(Integer.valueOf(99914), new Riddle("\u0431\u0430\u043D\u0430\u043D", "\u0417\u043D\u0430\u044E\u0442 \u044D\u0442\u043E\u0442 \u0444\u0440\u0443\u043A\u0442 \u0434\u0435\u0442\u0438\u0448\u043A\u0438,<br1>\u041B\u044E\u0431\u044F\u0442 \u0435\u0441\u0442\u044C \u0435\u0433\u043E \u043C\u0430\u0440\u0442\u044B\u0448\u043A\u0438.<br1>\u0420\u043E\u0434\u043E\u043C \u043E\u043D \u0438\u0437 \u0436\u0430\u0440\u043A\u0438\u0445 \u0441\u0442\u0440\u0430\u043D<br1>\u0412 \u0442\u0440\u043E\u043F\u0438\u043A\u0430\u0445 \u0440\u0430\u0441\u0442\u0435\u0442 ...<br1>"));
    _riddles.put(Integer.valueOf(99915), new Riddle("\u0430\u0440\u0431\u0443\u0437", "\u041E\u043D \u0442\u044F\u0436\u0435\u043B\u044B\u0439 \u0438 \u043F\u0443\u0437\u0430\u0442\u044B\u0439,<br1>\u041D\u043E\u0441\u0438\u0442 \u0444\u0440\u0430\u043A \u0441\u0432\u043E\u0439 \u043F\u043E\u043B\u043E\u0441\u0430\u0442\u044B\u0439.<br1>\u041D\u0430 \u043C\u0430\u043A\u0443\u0448\u043A\u0435 \u0445\u0432\u043E\u0441\u0442\u0438\u043A-\u0443\u0441,<br1>\u0421\u043F\u0435\u043B\u044B\u0439 \u0438\u0437\u043D\u0443\u0442\u0440\u0438 ...<br1>"));
    _riddles.put(Integer.valueOf(99916), new Riddle("\u043A\u0430\u0440\u0442\u043E\u0448\u043A\u0430", "\u0422\u044B \u0432\u0441\u0435 \u043B\u0435\u0442\u043E \u0437\u0435\u043B\u0435\u043D\u0435\u0435\u0448\u044C,<br1>\u0421\u043F\u0440\u044F\u0442\u0430\u0432 \u044F\u0433\u043E\u0434\u044B \u0432 \u0437\u0435\u043C\u043B\u0435,<br1>\u0411\u043B\u0438\u0436\u0435 \u043A \u043E\u0441\u0435\u043D\u0438 \u0441\u043E\u0437\u0440\u0435\u0435\u0448\u044C -<br1>\u0421\u0440\u0430\u0437\u0443 \u043F\u0440\u0430\u0437\u0434\u043D\u0438\u043A \u043D\u0430 \u0441\u0442\u043E\u043B\u0435!<br1>\u0421\u0443\u043F \u0438 \u0449\u0438, \u043F\u044E\u0440\u0435, \u043E\u043A\u0440\u043E\u0448\u043A\u0430,<br1>\u041D\u0430\u043C \u0432\u0435\u0437\u0434\u0435 \u043D\u0443\u0436\u043D\u0430...<br1>"));
    _riddles.put(Integer.valueOf(99917), new Riddle("\u0430\u0440\u0431\u0443\u0437", "\u0421 \u0432\u0438\u0434\u0443 \u043E\u043D \u0437\u0435\u043B\u0435\u043D\u044B\u0439 \u043C\u044F\u0447\u0438\u043A,<br1>\u041D\u043E \u0437\u043E\u0432\u0443\u0442 \u0435\u0433\u043E \u0438\u043D\u0430\u0447\u0435,<br1>\u041D\u0430 \u043D\u0435\u043C \u0431\u0430\u0440\u0445\u0430\u0442\u043D\u044B\u0439 \u043A\u0430\u0440\u0442\u0443\u0437.<br1>\u042D\u0442\u043E \u0441\u043B\u0430\u0434\u043A\u0438\u0439 \u043D\u0430\u0448...<br1>"));
    _riddles.put(Integer.valueOf(99918), new Riddle("\u0441\u0432\u0435\u043A\u043B\u0430", "\u0422\u044B \u043A\u0440\u0443\u0433\u043B\u0430, \u0432\u043A\u0443\u0441\u043D\u0430, \u043A\u0440\u0430\u0441\u0438\u0432\u0430!<br1>\u0422\u044B \u0441\u043E\u0447\u043D\u0430, \u043D\u0443 \u043F\u0440\u043E\u0441\u0442\u043E \u0434\u0438\u0432\u043E!<br1>\u0411\u043E\u0440\u0449, \u0441\u0432\u0435\u043A\u043E\u043B\u044C\u043D\u0438\u043A, \u0432\u0438\u043D\u0435\u0433\u0440\u0435\u0442...<br1>\u0411\u0435\u0437 \u0442\u0435\u0431\u044F \u0443\u0436 \u043D\u0435 \u043E\u0431\u0435\u0434!<br1>\u0422\u044B \u0432\u043E \u0432\u0441\u0435\u043C \u043D\u0430\u043C \u043F\u043E\u043C\u043E\u0433\u043B\u0430,<br1>\u0410 \u0437\u043E\u0432\u0443\u0442 \u0442\u0435\u0431\u044F...<br1>"));
    _riddles.put(Integer.valueOf(99919), new Riddle("\u0447\u0435\u0441\u043D\u043E\u043A", "\u041E\u0442 \u043F\u0440\u043E\u0441\u0442\u0443\u0434\u044B \u043D\u0430\u0441 \u0438\u0437\u0431\u0430\u0432\u0438\u043B,<br1>\u0412\u0438\u0442\u0430\u043C\u0438\u043D\u043E\u0432 \u043D\u0430\u043C \u0434\u043E\u0431\u0430\u0432\u0438\u043B<br1>\u0418 \u043E\u0442 \u0433\u0440\u0438\u043F\u043F\u0430 \u043E\u043D \u043F\u043E\u043C\u043E\u0433,<br1>\u0413\u043E\u0440\u044C\u043A\u0438\u0439 \u0434\u043E\u043A\u0442\u043E\u0440 \u043D\u0430\u0448...<br1>"));
    _riddles.put(Integer.valueOf(99920), new Riddle("\u0433\u043B\u043E\u0431\u0443\u0441", "\u041A\u0440\u0443\u0433\u043B\u044B\u0439 \u043C\u044F\u0447 \u043D\u0430 \u0442\u043E\u043D\u043A\u043E\u0439 \u043D\u043E\u0436\u043A\u0435<br1>\u041C\u044B \u0432\u0440\u0430\u0449\u0430\u0435\u043C \u0443 \u043E\u043A\u043E\u0448\u043A\u0430.<br1>\u041D\u0430 \u043C\u044F\u0447\u0435 \u043C\u044B \u0432\u0438\u0434\u0438\u043C \u0441\u0442\u0440\u0430\u043D\u044B,<br1>\u0413\u043E\u0440\u043E\u0434\u0430 \u0438 \u043E\u043A\u0435\u0430\u043D\u044B.<br1>"));
    _riddles.put(Integer.valueOf(99921), new Riddle("\u0430\u0439\u0431\u043E\u043B\u0438\u0442", "\u041B\u0435\u0447\u0438\u0442 \u043E\u043D \u043C\u044B\u0448\u0435\u0439 \u0438 \u043A\u0440\u044B\u0441,<br1>\u041A\u0440\u043E\u043A\u043E\u0434\u0438\u043B\u043E\u0432, \u0437\u0430\u0439\u0446\u0435\u0432, \u043B\u0438\u0441,<br1>\u041F\u0435\u0440\u0435\u0432\u044F\u0437\u044B\u0432\u0430\u0435\u0442  \u0440\u0430\u043D\u043A\u0438<br1>\u0410\u0444\u0440\u0438\u043A\u0430\u043D\u0441\u043A\u043E\u0439 \u043E\u0431\u0435\u0437\u044C\u044F\u043D\u043A\u0435. <br1>\u0418 \u043B\u044E\u0431\u043E\u0439 \u043D\u0430\u043C \u043F\u043E\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u0442: <br1>\u042D\u0442\u043E - \u0434\u043E\u043A\u0442\u043E\u0440 ...<br1>"));
  }

  public Riddle getRiddle(int i) {
    return (Riddle)_riddles.get(Integer.valueOf(i));
  }

  public void restoreOfflineTraders()
  {
    new Thread(new Runnable()
    {
      public void run()
      {
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
          con = L2DatabaseFactory.getInstance().getConnection();

          st = con.prepareStatement("DELETE FROM character_offline WHERE `name` = ? AND `value` < ?");
          st.setString(1, "offline");
          st.setLong(2, System.currentTimeMillis());
          st.execute();
          Close.S(st);

          con.setTransactionIsolation(1);

          int count = 0;
          st = con.prepareStatement("SELECT obj_id FROM `character_offline` WHERE `name` = ?");
          st.setString(1, "offline");
          rs = st.executeQuery();
          while (rs.next()) {
            int objid = rs.getInt("obj_id");
            if (objid == 0)
            {
              continue;
            }
            L2GameClient client = new L2GameClient(new MMOConnection(null), true);
            client.setCharSelection(objid);
            L2PcInstance p = client.loadCharFromDisk(0);
            if ((p == null) || (p.isDead())) {
              continue;
            }
            client.setAccountName(p.getAccountName());

            p.spawnMe();
            p.revalidateZone(true);
            p.setOnlineStatus(true);
            p.setOfflineMode(true);
            p.setConnected(false);
            p.broadcastUserInfo();
            count++;
          }
          CustomServerData._log.info("Restored " + count + " offline traders");
        } catch (Exception e) {
          CustomServerData._log.severe("GameServer [ERROR]: Failed to restore offline traders. Reason: " + e.getMessage());
          e.printStackTrace();
        } finally {
          Close.CSR(con, st, rs);
        }
      }
    }).start();
  }

  public void cacheEventZones()
  {
    if ((Config.TVT_EVENT_ENABLED) && (Config.TVT_POLY != null)) {
      _eventZones.add(Config.TVT_POLY);
    }

    if ((Config.ELH_ENABLE) && (Config.LASTHERO_POLY != null)) {
      _eventZones.add(Config.LASTHERO_POLY);
    }

    if ((Config.MASS_PVP) && (Config.MASSPVP_POLY != null)) {
      _eventZones.add(Config.MASSPVP_POLY);
    }

    if ((Config.EBC_ENABLE) && (Config.BASECAPTURE_POLY != null))
      _eventZones.add(Config.BASECAPTURE_POLY);
  }

  public void cacheCustomZones()
  {
    EventTerritoryRound baiumLair = new EventTerritoryRound();
    baiumLair.addPoint(113144, 14072);
    baiumLair.addPoint(113288, 14008);
    baiumLair.addPoint(118328, 11464);
    baiumLair.addPoint(119000, 20824);
    baiumLair.addPoint(112600, 17430);
    baiumLair.addPoint(112232, 16536);
    baiumLair.addPoint(112248, 15576);
    baiumLair.addPoint(112632, 14584);
    baiumLair.setZ(9960, 16301);
    _eventZones.add(baiumLair);
  }

  public boolean intersectEventZone(int x, int y, int z, int tx, int ty, int tz) {
    if (_eventZones.isEmpty()) {
      return false;
    }

    EventTerritory zone = null;
    int i = 0; for (int n = _eventZones.size(); i < n; i++) {
      zone = (EventTerritory)_eventZones.get(i);
      if (zone == null)
      {
        continue;
      }
      if ((zone.contains(x, y, z)) && (zone.contains(tx, ty, tz)))
      {
        continue;
      }
      if (zone.intersectsLine(x, y, z, tx, ty, tz)) {
        return true;
      }

    }

    zone = null;
    return false;
  }

  public void cacheStat() {
    _statPvp.clear();
    _statPk.clear();
    _statClans.clear();
    _statCastles.clear();

    L2Clan clan = null;
    String clan_name = "";
    String owner = "";
    int clan_id = -1;
    String siegeDate = "";
    CastleManager cm = CastleManager.getInstance();
    ClanTable ct = ClanTable.getInstance();

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT obj_Id,char_name,pvpkills,clanid,online FROM `characters` WHERE `pvpkills` >= ? AND `accesslevel` = ? ORDER BY `pvpkills` DESC LIMIT 0, 100");
      st.setInt(1, 1);
      st.setInt(2, 0);
      rs = st.executeQuery();
      rs.setFetchSize(20);
      while (rs.next()) {
        clan = ct.getClan(rs.getInt("clanid"));
        if (clan != null) {
          clan_name = clan.getName();
        }

        _statPvp.add(new StatPlayer(rs.getInt("obj_Id"), Util.htmlSpecialChars(rs.getString("char_name")), Util.htmlSpecialChars(clan_name), rs.getInt("online"), rs.getInt("pvpkills")));
      }
      Close.SR(st, rs);

      st = con.prepareStatement("SELECT obj_Id,char_name,pkkills,clanid,online FROM `characters` WHERE `pkkills` >= ? AND `accesslevel` = ? ORDER BY `pkkills` DESC LIMIT 0, 100");
      st.setInt(1, 1);
      st.setInt(2, 0);
      rs = st.executeQuery();
      rs.setFetchSize(20);
      while (rs.next()) {
        clan = ct.getClan(rs.getInt("clanid"));
        if (clan != null) {
          clan_name = clan.getName();
        }

        _statPk.add(new StatPlayer(rs.getInt("obj_Id"), Util.htmlSpecialChars(rs.getString("char_name")), Util.htmlSpecialChars(clan_name), rs.getInt("online"), rs.getInt("pkkills")));
      }
      Close.SR(st, rs);

      st = con.prepareStatement("SELECT clan_id,clan_name,clan_level,reputation_score,ally_name,leader_id FROM `clan_data` ORDER BY `clan_level` DESC, `reputation_score` DESC LIMIT 0, 100");
      rs = st.executeQuery();
      rs.setFetchSize(20);
      while (rs.next()) {
        clan = ct.getClan(rs.getInt("clan_id"));
        if (clan == null)
        {
          continue;
        }
        _statClans.add(new StatClan(Util.htmlSpecialChars(rs.getString("clan_name")), clan.getLeaderName(), rs.getInt("clan_level"), rs.getInt("reputation_score"), clan.getMembersCount(), rs.getString("ally_name")));
      }
      Close.SR(st, rs);

      st = con.prepareStatement("SELECT id,name,siegeDate FROM `castle` WHERE `id` <= ?");
      st.setInt(1, 9);
      rs = st.executeQuery();
      rs.setFetchSize(20);
      while (rs.next()) {
        clan_id = rs.getInt("id");
        siegeDate = String.valueOf(cm.getCastleById(clan_id).getSiegeDate().getTime());
        owner = "NPC";
        clan = ct.getClan(cm.getCastleById(clan_id).getOwnerId());
        if (clan != null) {
          owner = clan.getName();
        }
        _statCastles.add(new StatCastle(rs.getString("name"), owner, siegeDate));
      }
      Close.SR(st, rs);
    } catch (SQLException e) {
      _log.warning("[ERROR] CustomServerData, cacheStat() error: " + e);
    } finally {
      clan = null;
      Close.CSR(con, st, rs);
    }
    cacheStatHome();
  }

  private void cacheStatHome()
  {
    TextBuilder htm = new TextBuilder();

    int count = 0;
    StatPlayer pc = null;
    int i = 0; for (int n = _statPvp.size(); i < n; i++) {
      pc = (StatPlayer)_statPvp.get(i);
      if (pc == null)
      {
        continue;
      }
      htm.append("<font color=CCCC33>" + pc.name + "</font> <font color=999966>" + pc.kills + "</font><br1>");
      count++;
      if (count > 10) {
        break;
      }
    }
    htm.append("</td><td valign=top>");

    count = 0;
    int i = 0; for (int n = _statPk.size(); i < n; i++) {
      pc = (StatPlayer)_statPk.get(i);
      if (pc == null)
      {
        continue;
      }
      htm.append("<font color=CCCC33>" + pc.name + "</font> <font color=999966>" + pc.kills + "</font><br1>");
      count++;
      if (count > 10) {
        break;
      }
    }
    pc = null;

    statHome = htm.toString();
    htm.clear();
    htm = null;
  }

  public String getStatHome() {
    return statHome;
  }

  public FastTable<StatPlayer> getStatPvp() {
    return _statPvp;
  }

  public FastTable<StatPlayer> getStatPk() {
    return _statPk;
  }

  public FastTable<StatClan> getStatClans() {
    return _statClans;
  }

  public FastTable<StatCastle> getStatCastles() {
    return _statCastles;
  }

  private void cacheClanSkills()
  {
    SkillTable st = SkillTable.getInstance();
    _clanSkills.add(st.getInfo(370, 3));
    _clanSkills.add(st.getInfo(371, 3));
    _clanSkills.add(st.getInfo(372, 3));
    _clanSkills.add(st.getInfo(373, 3));
    _clanSkills.add(st.getInfo(374, 3));
    _clanSkills.add(st.getInfo(375, 3));
    _clanSkills.add(st.getInfo(376, 3));
    _clanSkills.add(st.getInfo(377, 3));
    _clanSkills.add(st.getInfo(378, 3));
    _clanSkills.add(st.getInfo(379, 3));
    _clanSkills.add(st.getInfo(380, 3));
    _clanSkills.add(st.getInfo(381, 3));
    _clanSkills.add(st.getInfo(382, 3));
    _clanSkills.add(st.getInfo(383, 3));
    _clanSkills.add(st.getInfo(384, 3));
    _clanSkills.add(st.getInfo(385, 3));
    _clanSkills.add(st.getInfo(386, 3));
    _clanSkills.add(st.getInfo(387, 3));
    _clanSkills.add(st.getInfo(388, 3));
    _clanSkills.add(st.getInfo(389, 3));
    _clanSkills.add(st.getInfo(390, 3));
    _clanSkills.add(st.getInfo(391, 1));
  }

  public void addClanSkills(L2PcInstance player, L2Clan clan) {
    if (clan == null) {
      return;
    }

    L2Skill skill = null;
    int i = 0; for (int n = _clanSkills.size(); i < n; i++) {
      skill = (L2Skill)_clanSkills.get(i);
      if (skill == null)
      {
        continue;
      }
      clan.addNewSkill(skill);
      player.sendPacket(SystemMessage.id(SystemMessageId.CLAN_SKILL_S1_ADDED).addSkillName(skill.getId()));
    }
    skill = null;

    clan.broadcastToOnlineMembers(new PledgeSkillList(clan));
    for (L2PcInstance member : clan.getOnlineMembers("")) {
      if (member == null)
      {
        continue;
      }
      member.sendSkillList();
    }
  }

  private void cacheNpcChat()
  {
    try
    {
      File file = new File(Config.DATAPACK_ROOT, "data/npc_chat.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/npc_chat.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          int type = 0;
          int npcId = 0;
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("npc".equalsIgnoreCase(d.getNodeName())) {
              FastTable spawn = new FastTable();
              FastTable attack = new FastTable();
              FastTable death = new FastTable();
              FastTable kill = new FastTable();

              NamedNodeMap attrs = d.getAttributes();
              npcId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("onSpawn".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String[] text = attrs.getNamedItem("chat").getNodeValue().split(";");
                  for (String phrase : text) {
                    if (phrase.length() == 0)
                    {
                      continue;
                    }
                    spawn.add(phrase);
                  }
                }
                if ("onAttack".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String[] text = attrs.getNamedItem("chat").getNodeValue().split(";");
                  for (String phrase : text) {
                    if (phrase.length() == 0)
                    {
                      continue;
                    }
                    attack.add(phrase);
                  }
                }
                if ("onDeath".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String[] text = attrs.getNamedItem("chat").getNodeValue().split(";");
                  for (String phrase : text) {
                    if (phrase.length() == 0)
                    {
                      continue;
                    }
                    death.add(phrase);
                  }
                }
                if ("onKill".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  String[] text = attrs.getNamedItem("chat").getNodeValue().split(";");
                  for (String phrase : text) {
                    if (phrase.length() == 0)
                    {
                      continue;
                    }
                    kill.add(phrase);
                  }
                }
              }
              _cachedNpcChat.put(Integer.valueOf(npcId), new NpcChat(spawn, attack, death, kill, type));
            }
        }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: " + e.toString());
    }
    _log.config("CustomServerData: Npc Chat, cached " + _cachedNpcChat.size() + " npcs.");
  }

  public NpcChat getNpcChat(int id) {
    return (NpcChat)_cachedNpcChat.get(Integer.valueOf(id));
  }

  public void cacheChestsDrop()
  {
    try
    {
      File file = new File(Config.DATAPACK_ROOT, "data/chests_drop.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/chests_drop.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          int type = 0;
          int item_id = 0;
          int item_count = 0;
          int item_chance = 0;
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("chest".equalsIgnoreCase(d.getNodeName())) {
              FastList reward = new FastList();
              NamedNodeMap attrs = d.getAttributes();
              type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("item".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  item_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  item_count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                  item_chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
                  reward.add(new Config.EventReward(item_id, item_count, item_chance));
                }
              }
              _chestDrop.put(Integer.valueOf(type), reward);
            }
        }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: cacheChestsDrop " + e.toString());
    }
  }

  public FastList<Config.EventReward> getChestDrop(int cat)
  {
    return (FastList)_chestDrop.get(Integer.valueOf(cat));
  }

  private void cacheSpecialDrop()
  {
    try
    {
      File file = new File(Config.DATAPACK_ROOT, "data/special_drop.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/special_drop.xml doesn't exist");
        return;
      }

      DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          String name = "";
          String time = "";
          String welcome = "";

          Date start = null;
          Date finish = null;

          int item_id = 0;
          int item_count = 0;
          int item_chance = 0;
          int autoloot = 0;
          int announce = 0;
          String announce_text = "";

          SpecialDrop sDrop = null;
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("drop".equalsIgnoreCase(d.getNodeName())) {
              FastList reward = new FastList();
              NamedNodeMap attrs = d.getAttributes();
              name = attrs.getNamedItem("name").getNodeValue();
              time = attrs.getNamedItem("time").getNodeValue();
              welcome = attrs.getNamedItem("welcome").getNodeValue();
              try {
                String[] period = time.split("~");
                start = formatter.parse(period[0]);
                finish = formatter.parse(period[1]);
              } catch (ParseException e) {
                _log.warning("CustomServerData [ERROR]: cacheSpecialDrop: time " + time);
                _log.warning("TRACE: " + e.toString());
                continue;
              }
              long begin = start.getTime();
              long end = finish.getTime();

              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("item".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  item_id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                  item_count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
                  item_chance = Integer.parseInt(attrs.getNamedItem("chance").getNodeValue());
                  try {
                    autoloot = Integer.parseInt(attrs.getNamedItem("autoloot").getNodeValue());
                  } catch (Exception e) {
                    autoloot = 0;
                  }
                  try {
                    announce_text = attrs.getNamedItem("text").getNodeValue();
                    if (announce_text != null)
                      announce = 1;
                  }
                  catch (Exception e) {
                    announce = 0;
                  }
                  reward.add(new SpecialDropReward(item_id, item_count, item_chance, autoloot, announce, announce_text));
                }
              }
              if (!reward.isEmpty()) {
                sDrop = new SpecialDrop(name, welcome, begin, end);
              }

              if (sDrop != null)
                _specialDrop.put(sDrop, reward);
            }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: cacheSpecialDrop " + e.toString());
    }
    _log.config("CustomServerData: Special Drop, cached " + _specialDrop.size() + " event.");
  }

  public void showSpecialDropWelcome(L2PcInstance player) {
    SpecialDrop key = null;
    FastList value = null;
    long now = System.currentTimeMillis();
    FastMap.Entry e = _specialDrop.head(); for (FastMap.Entry end = _specialDrop.tail(); (e = e.getNext()) != end; ) {
      key = (SpecialDrop)e.getKey();
      value = (FastList)e.getValue();
      if ((key == null) || (value == null) || 
        (now < key.begin) || (now > key.end))
      {
        continue;
      }
      player.sendUserPacket(key.welcome);
    }
    key = null;
    value = null;
  }

  public void manageSpecialDrop(L2PcInstance player, L2Attackable mob) {
    SpecialDrop key = null;
    L2ItemInstance item = null;
    FastList value = null;
    long now = System.currentTimeMillis();
    FastMap.Entry e = _specialDrop.head(); for (FastMap.Entry end = _specialDrop.tail(); (e = e.getNext()) != end; ) {
      key = (SpecialDrop)e.getKey();
      value = (FastList)e.getValue();
      if ((key == null) || (value == null) || 
        (now < key.begin) || (now > key.end))
      {
        continue;
      }
      SpecialDropReward reward = null;
      FastList.Node n = value.head(); for (FastList.Node endv = value.tail(); (n = n.getNext()) != endv; ) {
        reward = (SpecialDropReward)n.getValue();
        if ((reward == null) || 
          (Rnd.get(100) >= reward.chance)) continue;
        if (reward.autoloot > 0) {
          player.addItem("SpecialDrop", reward.id, reward.getCount(), player, true);
        } else {
          int x = mob.getX() + Rnd.get(30);
          int y = mob.getY() + Rnd.get(30);
          int z = GeoData.getInstance().getSpawnHeight(x, y, mob.getZ(), mob.getZ());

          item = ItemTable.getInstance().createItem("Loot", reward.id, reward.getCount(), player, mob);
          item.setProtected(false);
          item.setPickuper(player);
          item.dropMe(mob, x, y, z);

          if ((Config.AUTODESTROY_ITEM_AFTER > 0) && (!Config.LIST_PROTECTED_ITEMS.contains(Integer.valueOf(item.getItemId())))) {
            ItemsAutoDestroy.getInstance().addItem(item);
          }
        }

        if (reward.announce > 0) {
          Announcements.getInstance().announceToAll(reward.text.replace("%player%", player.getName()));
        }
      }

      reward = null;
    }
    key = null;
    item = null;
    value = null;
  }

  private void cacheNpcPenaltyItems()
  {
    try
    {
      File file = new File(Config.DATAPACK_ROOT, "data/npc_penalty_items.xml");
      if (!file.exists()) {
        _log.config("CustomServerData [ERROR]: data/npc_penalty_items.xml doesn't exist");
        return;
      }

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      Document doc = factory.newDocumentBuilder().parse(file);
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          String name = "";
          String coord = "";
          String[] coords = null;

          String npc = "";
          String item = "";
          FastList npc_list = null;
          FastList item_list = null;

          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("penalty".equalsIgnoreCase(d.getNodeName())) {
              FastList reward = new FastList();
              NamedNodeMap attrs = d.getAttributes();
              name = attrs.getNamedItem("name").getNodeValue();

              Location loc = null;
              coord = attrs.getNamedItem("loc").getNodeValue();
              coords = coord.split(",");
              loc = new Location(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));

              npc_list = new FastList();
              item_list = new FastList();
              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("npc".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  npc = attrs.getNamedItem("list").getNodeValue();
                  if (npc.isEmpty()) {
                    continue;
                  }
                  String[] npcs = npc.split(",");

                  for (String npcId : npcs) {
                    if (npcId.isEmpty()) {
                      continue;
                    }
                    npc_list.add(Integer.valueOf(Integer.parseInt(npcId)));
                  }
                }

                if ("item".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  item = attrs.getNamedItem("list").getNodeValue();
                  if (item.isEmpty()) {
                    continue;
                  }
                  String[] items = item.split(",");

                  for (String itemId : items) {
                    if (itemId.isEmpty()) {
                      continue;
                    }
                    item_list.add(Integer.valueOf(Integer.parseInt(itemId)));
                  }
                }

              }

              if ((!npc_list.isEmpty()) && (!item_list.isEmpty()))
                _npcPenaltyItems.put(npc_list, new NpcPenalty(loc, item_list));
            }
        }
      }
    }
    catch (Exception e)
    {
      _log.warning("CustomServerData [ERROR]: cacheNpcPenaltyItems " + e.toString());
    }
    _log.config("CustomServerData: Npc Penalty Items, cached " + _npcPenaltyItems.size() + " penaltyes.");
  }

  public void setPenaltyItems(int id, L2NpcTemplate template) {
    NpcPenalty penalty = null;
    FastList list = null;
    FastMap.Entry e = _npcPenaltyItems.head(); for (FastMap.Entry end = _npcPenaltyItems.tail(); (e = e.getNext()) != end; ) {
      list = (FastList)e.getKey();
      penalty = (NpcPenalty)e.getValue();
      if ((list == null) || (penalty == null) || 
        (!list.contains(Integer.valueOf(id))))
      {
        continue;
      }
      template.setPenaltyLoc(penalty.loc);
      template.setPenaltyItems(penalty.item_list);
    }
    penalty = null;
    list = null;
  }

  public static class NpcPenalty
  {
    public Location loc = null;
    public FastList<Integer> item_list = null;

    public NpcPenalty(Location loc, FastList<Integer> item_list) {
      this.loc = loc;
      this.item_list = item_list;
    }
  }

  public static class SpecialDropReward
  {
    int id = 0;
    int count = 0;
    int chance = 0;
    int autoloot = 0;
    int announce = 0;
    String text = "";

    public SpecialDropReward(int id, int count, int chance, int autoloot, int announce, String text) {
      this.id = id;
      this.count = count;
      this.chance = chance;
      this.autoloot = autoloot;
      this.announce = announce;
      this.text = text;
    }

    public int getCount() {
      if (count > 1) {
        return Rnd.get(1, count);
      }

      return 1;
    }
  }

  public static class SpecialDrop
  {
    String name;
    CreatureSay welcome;
    long begin;
    long end;

    public SpecialDrop(String name, String welcome, long begin, long end)
    {
      this.name = name;
      this.welcome = new CreatureSay(0, 18, "", welcome);
      this.begin = begin;
      this.end = end;
    }
  }

  public static class NpcChat
  {
    public FastTable<String> onSpawn = new FastTable();
    public FastTable<String> onAttack = new FastTable();
    public FastTable<String> onDeath = new FastTable();
    public FastTable<String> onKill = new FastTable();
    private int onSpawnSize;
    private int onAttackSize;
    private int onDeathSize;
    private int onKillSize;
    private int type;

    public NpcChat(FastTable<String> onSpawn, FastTable<String> onAttack, FastTable<String> onDeath, FastTable<String> onKill, int type)
    {
      this.onSpawn = onSpawn;
      this.onAttack = onAttack;
      this.onDeath = onDeath;
      this.onKill = onKill;

      onSpawnSize = (onSpawn.size() - 1);
      onAttackSize = (onAttack.size() - 1);
      onDeathSize = (onDeath.size() - 1);
      onKillSize = (onKill.size() - 1);

      this.type = type;
    }

    public void chatSpawn(L2NpcInstance npc) {
      if (onSpawnSize < 0) {
        return;
      }

      String txt = (String)onSpawn.get(Rnd.get(onSpawnSize));
      txt = txt.replaceAll("%npc%", npc.getName());
      sayString(npc, txt);
    }

    public void chatAttack(L2NpcInstance npc) {
      if (onAttackSize < 0) {
        return;
      }

      npc.sayString((String)onAttack.get(Rnd.get(onAttackSize)));
    }

    public void chatDeath(L2NpcInstance npc, String name) {
      if (onDeathSize < 0) {
        return;
      }

      String txt = (String)onDeath.get(Rnd.get(onDeathSize));
      txt = txt.replaceAll("%npc%", npc.getName());
      txt = txt.replaceAll("%player%", name);
      sayString(npc, txt);
    }

    public void chatKill(L2NpcInstance npc, String name) {
      if (onKillSize < 0) {
        return;
      }

      String txt = (String)onKill.get(Rnd.get(onKillSize));
      txt = txt.replaceAll("%npc%", npc.getName());
      txt = txt.replaceAll("%player%", name);
      sayString(npc, txt);
    }

    public void sayString(L2NpcInstance npc, String txt) {
      npc.sayString(txt, type);
    }
  }

  public static class StatCastle
  {
    public String name;
    public String owner;
    public String siege;

    public StatCastle(String name, String owner, String siege)
    {
      this.name = name;
      this.owner = owner;
      this.siege = siege;
    }
  }

  public static class StatClan
  {
    public String name;
    public String owner;
    public int level;
    public int rep;
    public int count;
    public String ally;

    public StatClan(String name, String owner, int level, int rep, int count, String ally)
    {
      this.name = name;
      this.owner = owner;
      this.level = level;
      this.rep = rep;
      this.count = count;
      this.ally = ally;
    }
  }

  public static class StatPlayer
  {
    public int id;
    public String name;
    public String clan;
    public int online;
    public int kills;

    public StatPlayer(int id, String name, String clan, int online, int kills)
    {
      this.id = id;
      this.name = name;
      this.clan = clan;
      this.online = online;
      this.kills = kills;
    }
  }

  public static class ChinaItem
  {
    public int coin;
    public int price;
    public int days;
    public String name;
    public String info;

    public ChinaItem(int coin, int price, int days, String name, String info)
    {
      this.coin = coin;
      this.price = price;
      this.days = days;
      this.name = name;
      this.info = info;
    }
  }

  public static class DonateItem
  {
    public int itemId;
    public int itemCount;
    public String itemInfoRu;
    public String itemInfoDesc;
    public int priceId;
    public int priceCount;
    public String priceName;

    public DonateItem(int itemId, int itemCount, String itemInfoRu, String itemInfoDesc, int priceId, int priceCount, String priceName)
    {
      this.itemId = itemId;
      this.itemCount = itemCount;
      this.itemInfoRu = itemInfoRu;
      this.itemInfoDesc = itemInfoDesc;

      this.priceId = priceId;
      this.priceCount = priceCount;
      this.priceName = priceName;
    }
  }

  public static class DonateSkill
  {
    public int cls;
    public int id;
    public int lvl;
    public long expire;
    public int priceId;
    public int priceCount;
    public String priceName;
    public String icon;
    public String info;

    public DonateSkill(int cls, int id, int lvl, long expire)
    {
      this.cls = cls;
      this.id = id;
      this.lvl = lvl;
      this.expire = expire;
    }

    public DonateSkill(int cls, int id, int lvl, long expire, int priceId, int priceCount, String priceName, String icon, String info) {
      this.cls = cls;
      this.id = id;
      this.lvl = lvl;
      this.expire = expire;
      this.priceId = priceId;
      this.priceCount = priceCount;
      this.priceName = priceName;
      this.icon = icon;
      this.info = info;
    }
  }

  public static class Riddle
  {
    public String answer;
    public String question;

    public Riddle(String answer, String question)
    {
      this.answer = answer;
      this.question = question;
    }
  }
}