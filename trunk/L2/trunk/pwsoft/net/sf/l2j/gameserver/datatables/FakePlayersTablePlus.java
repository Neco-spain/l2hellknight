package net.sf.l2j.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.util.Online;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class FakePlayersTablePlus
{
  private static final Logger _log = AbstractLogger.getLogger(FakePlayersTablePlus.class.getName());

  private static int _fakesCount = 0;
  private static int _fakesLimit = 0;
  private static String _fakeAcc = "fh4f#67$kl";
  private static FastMap<Integer, L2Fantome> _fakes = new FastMap().shared("FakePlayersTablePlus._fakes");

  private volatile int _fakesTownTotal = 0;

  private static int _locsCount = 0;
  private static FastList<Location> _fakesTownLoc = new FastList();

  private static Map<Integer, ConcurrentLinkedQueue<L2PcInstance>> _fakesTown = new ConcurrentHashMap();

  private static Map<Integer, ConcurrentLinkedQueue<L2PcInstance>> _fakesTownClan = new ConcurrentHashMap();
  private static Map<Integer, ConcurrentLinkedQueue<Integer>> _fakesTownClanList = new ConcurrentHashMap();

  private static int _setsCount = 0;
  private static FastList<L2Set> _sets = new FastList();
  private static int _setsArcherCount = 0;
  private static FastList<L2Set> _setsArcher = new FastList();

  private static ConcurrentLinkedQueue<L2PcInstance> _fakesTownRec = new ConcurrentLinkedQueue();

  private static ConcurrentLinkedQueue<L2PcInstance> _fakesOly = new ConcurrentLinkedQueue();

  private static int _setsOlyCount = 0;
  private static FastList<L2Set> _setsOly = new FastList();

  private static int _nameColCount = 0;
  private static int _titleColCount = 0;
  private static FastList<Integer> _nameColors = new FastList();
  private static FastList<Integer> _titleColors = new FastList();
  private static FakePlayersTablePlus _instance;
  private static int _fakesEnchPhsCount = 0;
  private static FastList<String> _fakesEnchPhrases = new FastList();
  private static int _fakesLastPhsCount = 0;
  private static FastList<String> _fakesLastPhrases = new FastList();

  public static FakePlayersTablePlus getInstance() {
    return _instance;
  }

  public static void init() {
    _instance = new FakePlayersTablePlus();
    _instance.load();
  }

  public void load()
  {
    parceArmors();
    parceArcherArmors();
    parceOlyArmors();
    parceColors();
    cacheLastPhrases();
    if (Config.ALLOW_FAKE_PLAYERS_PLUS) {
      parceTownLocs();
      parceTownClans();
      parceTownRecs();
      cacheFantoms();
      cacheEnchantPhrases();

      _fakesLimit = Config.FAKE_PLAYERS_PLUS_COUNT_FIRST + Config.FAKE_PLAYERS_PLUS_COUNT_NEXT + 10;
      _fakesTown.put(Integer.valueOf(1), new ConcurrentLinkedQueue());
      _fakesTown.put(Integer.valueOf(2), new ConcurrentLinkedQueue());
    }
  }

  private void cacheFantoms() {
    new Thread(new Runnable()
    {
      public void run()
      {
        String name = "";
        String new_name = "";
        Connect con = null;
        PreparedStatement st = null;

        ResultSet rs = null;

        L2PcInstance fantom = null;
        try {
          con = L2DatabaseFactory.getInstance().getConnection();
          con.setTransactionIsolation(1);
          st = con.prepareStatement("SELECT obj_Id,char_name,title,x,y,z,clanid FROM characters WHERE account_name = ?");
          st.setString(1, FakePlayersTablePlus._fakeAcc);
          rs = st.executeQuery();
          rs.setFetchSize(250);
          while (rs.next())
          {
            name = rs.getString("char_name");

            FakePlayersTablePlus._fakes.put(Integer.valueOf(rs.getInt("obj_Id")), new FakePlayersTablePlus.L2Fantome(name, rs.getString("title"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("clanid")));
          }
        } catch (Exception e) {
          FakePlayersTablePlus._log.warning("FakePlayersTablePlus: could not load chars from DB: " + e);
        } finally {
          Close.CSR(con, st, rs);
        }
        FakePlayersTablePlus._log.info("FakePlayersTablePlus: Cached " + FakePlayersTablePlus._fakes.size() + " players.");
        if (!FakePlayersTablePlus._fakes.isEmpty()) {
          FakePlayersTablePlus.access$302(FakePlayersTablePlus._fakes.size() - 1);
          ThreadPoolManager.getInstance().scheduleGeneral(new FakePlayersTablePlus.FantomTask(FakePlayersTablePlus.this, 1), Config.FAKE_PLAYERS_PLUS_DELAY_FIRST);
        }
      }
    }).start();
  }

  private String getRandomEnchantPhrase()
  {
    return (String)_fakesEnchPhrases.get(Rnd.get(_fakesEnchPhsCount));
  }

  public String getRandomLastPhrase() {
    return (String)_fakesLastPhrases.get(Rnd.get(_fakesLastPhsCount));
  }

  public void wearFantom(L2PcInstance fantom)
  {
    L2Set set = getRandomSet();

    L2ItemInstance body = ItemTable.getInstance().createDummyItem(set.body);
    L2ItemInstance gaiters = ItemTable.getInstance().createDummyItem(set.gaiters);
    L2ItemInstance gloves = ItemTable.getInstance().createDummyItem(set.gloves);
    L2ItemInstance boots = ItemTable.getInstance().createDummyItem(set.boots);
    L2ItemInstance weapon = ItemTable.getInstance().createDummyItem(set.weapon);

    fantom.getInventory().equipItemAndRecord(body);
    fantom.getInventory().equipItemAndRecord(gaiters);
    fantom.getInventory().equipItemAndRecord(gloves);
    fantom.getInventory().equipItemAndRecord(boots);

    if (set.custom > 0) {
      L2ItemInstance custom = ItemTable.getInstance().createDummyItem(set.custom);
      fantom.getInventory().equipItemAndRecord(custom);
    }

    weapon.setEnchantLevel(Rnd.get(Config.FAKE_PLAYERS_ENCHANT.nick, Config.FAKE_PLAYERS_ENCHANT.title));
    if (Rnd.get(100) < 30) {
      weapon.setAugmentation(new L2Augmentation(weapon, 1067847165, 3250, 1, false));
    }
    fantom.getInventory().equipItemAndRecord(weapon);

    fantom.spawnMe();
    fantom.setOnlineStatus(true);
    if (Rnd.get(100) < 23)
      fantom.setAlone(true);
  }

  private void parceArmors()
  {
    if (!_sets.isEmpty()) {
      _sets.clear();
    }

    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/town_sets.txt");
      if (!Data.exists()) {
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
        String[] items = line.split(",");

        int custom = 0;
        try {
          custom = Integer.parseInt(items[5]);
        } catch (Exception e) {
          custom = 0;
        }
        _sets.add(new L2Set(Integer.parseInt(items[0]), Integer.parseInt(items[1]), Integer.parseInt(items[2]), Integer.parseInt(items[3]), Integer.parseInt(items[4]), custom));
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
      catch (Exception e1) {
      }
    }
    _setsCount = _sets.size() - 1;
  }

  private void parceArcherArmors() {
    if (!_setsArcher.isEmpty()) {
      _setsArcher.clear();
    }

    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/archer_sets.txt");
      if (!Data.exists()) {
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
        String[] items = line.split(",");

        int custom = 0;
        try {
          custom = Integer.parseInt(items[5]);
        } catch (Exception e) {
          custom = 0;
        }
        _setsArcher.add(new L2Set(Integer.parseInt(items[0]), Integer.parseInt(items[1]), Integer.parseInt(items[2]), Integer.parseInt(items[3]), Integer.parseInt(items[4]), custom));
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
      catch (Exception e1) {
      }
    }
    _setsArcherCount = _setsArcher.size() - 1;
  }

  private void parceOlyArmors() {
    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/oly_sets.txt");
      if (!Data.exists()) {
        return;
      }
      fr = new FileReader(Data);
      br = new BufferedReader(fr);
      lnr = new LineNumberReader(br);
      String line;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#"))) {
          continue;
        }
        String[] items = line.split(",");

        int custom = 0;
        try {
          custom = Integer.parseInt(items[5]);
        } catch (Exception e) {
          custom = 0;
        }
        _setsOly.add(new L2Set(Integer.parseInt(items[0]), Integer.parseInt(items[1]), Integer.parseInt(items[2]), Integer.parseInt(items[3]), Integer.parseInt(items[4]), custom));
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
      catch (Exception e1) {
      }
    }
    _setsOlyCount = _setsOly.size() - 1;
  }

  private void parceTownClans() {
    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/town_clans.txt");
      if (!Data.exists()) {
        return;
      }
      fr = new FileReader(Data);
      br = new BufferedReader(fr);
      lnr = new LineNumberReader(br);
      int clanId = 0;
      String line;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#")))
        {
          continue;
        }
        String[] items = line.split(":");
        clanId = Integer.parseInt(items[0]);

        String[] pls = items[1].split(",");
        ConcurrentLinkedQueue players = new ConcurrentLinkedQueue();
        for (String plid : pls) {
          players.add(Integer.valueOf(Integer.parseInt(plid)));
        }
        _fakesTownClanList.put(Integer.valueOf(clanId), players);
      }
    } catch (Exception e1) {
      e.printStackTrace();
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

  private void parceTownRecs()
  {
  }

  private void parceTownLocs()
  {
    _fakesTownLoc.clear();

    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/town_locs.txt");
      if (!Data.exists()) {
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
        String[] items = line.split(",");
        _fakesTownLoc.add(new Location(Integer.parseInt(items[0]), Integer.parseInt(items[1]), Integer.parseInt(items[2])));
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
    _locsCount = _fakesTownLoc.size() - 1;
  }

  private void cacheEnchantPhrases() {
    _fakesEnchPhrases.clear();

    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/phrases_enchant.txt");
      if (!Data.exists()) {
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
        _fakesEnchPhrases.add(line);
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
    _fakesEnchPhsCount = _fakesEnchPhrases.size() - 1;
  }

  private void cacheLastPhrases() {
    _fakesLastPhrases.clear();

    LineNumberReader lnr = null;
    BufferedReader br = null;
    FileReader fr = null;
    try {
      File Data = new File("./config/fake/phrases_last.txt");
      if (!Data.exists()) {
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
        _fakesLastPhrases.add(line);
      }
    } catch (Exception e1) {
      e.printStackTrace();
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
    _fakesLastPhsCount = _fakesLastPhrases.size() - 1;
  }

  private L2Set getRandomSet() {
    return (L2Set)_sets.get(Rnd.get(_setsCount));
  }

  private L2Set getRandomArcherSet() {
    return (L2Set)_setsArcher.get(Rnd.get(_setsArcherCount));
  }

  private int getRandomFake() {
    return Rnd.get(511151115, 511157114);
  }

  private int getRandomFakeNext() {
    int obj = 0;
    for (int i = 6; i > 0; i--) {
      obj = Rnd.get(511151115, 511157114);

      if ((!((ConcurrentLinkedQueue)_fakesTown.get(Integer.valueOf(1))).contains(Integer.valueOf(obj))) && (!((ConcurrentLinkedQueue)_fakesTown.get(Integer.valueOf(2))).contains(Integer.valueOf(obj))))
      {
        return obj;
      }
    }
    return getRandomFakeNext();
  }

  private int getRandomClan() {
    return Rnd.get(511158000, 511158008);
  }

  private Location getRandomLoc()
  {
    Location loc = null;
    try {
      loc = (Location)_fakesTownLoc.get(_fakesTownTotal);
    }
    catch (Exception e) {
    }
    if (loc == null) {
      loc = (Location)_fakesTownLoc.get(Rnd.get(_locsCount));
    }

    return loc;
  }

  private L2PcInstance restoreFake(int objId) {
    L2Fantome fake = (L2Fantome)_fakes.get(Integer.valueOf(objId));
    if (fake == null) {
      return null;
    }

    L2PcInstance fantom = L2PcInstance.restoreFake(objId);
    fantom.setName(fake.name);
    fantom.setTitle(fake.title);

    fantom.getAppearance().setNameColor(getNameColor());
    fantom.getAppearance().setTitleColor(getTitleColor());

    if (Rnd.get(100) < 40) {
      fantom.setClan(ClanTable.getInstance().getClan(getRandomClan()));
    }

    Location loc = getRandomLoc();
    fantom.setFakeLoc(loc.x, loc.y, loc.z);
    fantom.setXYZInvisible(loc.x + Rnd.get(60), loc.y + Rnd.get(60), loc.z);
    return fantom;
  }

  private void parceColors()
  {
    _nameColors = Config.FAKE_PLAYERS_NAME_CLOLORS;
    _titleColors = Config.FAKE_PLAYERS_TITLE_CLOLORS;
    _nameColCount = _nameColors.size() - 1;
    _titleColCount = _titleColors.size() - 1;
  }

  private int getNameColor() {
    return ((Integer)_nameColors.get(Rnd.get(_nameColCount))).intValue();
  }

  private int getTitleColor() {
    return ((Integer)_titleColors.get(Rnd.get(_titleColCount))).intValue();
  }

  public void wearArcher(L2PcInstance fantom)
  {
    L2Set set = getRandomArcherSet();

    L2ItemInstance body = ItemTable.getInstance().createDummyItem(set.body);
    L2ItemInstance gaiters = ItemTable.getInstance().createDummyItem(set.gaiters);
    L2ItemInstance gloves = ItemTable.getInstance().createDummyItem(set.gloves);
    L2ItemInstance boots = ItemTable.getInstance().createDummyItem(set.boots);
    L2ItemInstance weapon = ItemTable.getInstance().createDummyItem(set.weapon);

    fantom.getInventory().equipItemAndRecord(body);
    fantom.getInventory().equipItemAndRecord(gaiters);
    fantom.getInventory().equipItemAndRecord(gloves);
    fantom.getInventory().equipItemAndRecord(boots);

    if (set.custom > 0) {
      L2ItemInstance custom = ItemTable.getInstance().createDummyItem(set.custom);
      fantom.getInventory().equipItemAndRecord(custom);
    }

    weapon.setEnchantLevel(Rnd.get(Config.FAKE_PLAYERS_ENCHANT.nick, Config.FAKE_PLAYERS_ENCHANT.title));
    if (Rnd.get(100) < 30) {
      weapon.setAugmentation(new L2Augmentation(weapon, 1067847165, 3240, 10, false));
    }
    fantom.getInventory().equipItemAndRecord(weapon);

    fantom.spawnMe();
    fantom.setOnlineStatus(true);
    if (Rnd.get(100) < 23)
      fantom.setAlone(true);
  }

  static class L2Fantome
  {
    public String name;
    public String title;
    public int x;
    public int y;
    public int z;
    public int clanId;

    L2Fantome(String name, String title, int x, int y, int z, int clanId)
    {
      this.name = name;
      this.title = title;

      this.x = x;
      this.y = y;
      this.z = z;

      this.clanId = clanId;
    }
  }

  static class L2Set
  {
    public int body;
    public int gaiters;
    public int gloves;
    public int boots;
    public int weapon;
    public int custom;

    L2Set(int bod, int gaiter, int glove, int boot, int weapon, int custom)
    {
      body = bod;
      gaiters = gaiter;
      gloves = glove;
      boots = boot;
      this.weapon = weapon;
      this.custom = custom;
    }
  }

  public class CheckCount
    implements Runnable
  {
    public CheckCount()
    {
    }

    public void run()
    {
      for (Map.Entry entry : FakePlayersTablePlus._fakesTown.entrySet()) {
        wave = (Integer)entry.getKey();
        ConcurrentLinkedQueue players = (ConcurrentLinkedQueue)entry.getValue();
        if ((wave == null) || (players == null) || 
          (players.isEmpty()))
        {
          continue;
        }
        int limit = wave.intValue() == 1 ? Config.FAKE_PLAYERS_PLUS_COUNT_FIRST : Config.FAKE_PLAYERS_PLUS_COUNT_NEXT;
        overflow = players.size() - limit;
        if (overflow < 1)
        {
          continue;
        }
        for (L2PcInstance fantom : players) {
          fantom.kick();
          fantom.setOnlineStatus(false);
          ((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(wave)).remove(fantom);
          FakePlayersTablePlus.access$710(FakePlayersTablePlus.this);

          overflow--;
          if (overflow == 0)
            break;
        }
      }
      Integer wave;
      int overflow;
      ThreadPoolManager.getInstance().scheduleGeneral(new CheckCount(FakePlayersTablePlus.this), 300000L);
    }
  }

  public class Social
    implements Runnable
  {
    public Social()
    {
    }

    public void run()
    {
      TextBuilder tb = new TextBuilder();
      for (Map.Entry entry : FakePlayersTablePlus._fakesTown.entrySet()) {
        Integer wave = (Integer)entry.getKey();
        ConcurrentLinkedQueue players = (ConcurrentLinkedQueue)entry.getValue();
        if ((wave == null) || (players == null) || 
          (players.isEmpty()))
        {
          continue;
        }
        count = 0;
        for (L2PcInstance player : players) {
          if (Rnd.get(100) < 65) {
            switch (Rnd.get(2)) {
            case 0:
            case 1:
              L2ItemInstance wpn = player.getActiveWeaponInstance();
              int enhchant = wpn.getEnchantLevel();
              int nextench = enhchant + 1;
              if ((Rnd.get(100) < Config.ENCHANT_ALT_MAGICCAHNCE) && (enhchant <= Config.ENCHANT_MAX_WEAPON)) {
                wpn.setEnchantLevel(nextench);
              } else if (Rnd.get(100) < 70) {
                wpn.setEnchantLevel(3);
                if ((nextench > 13) && (Rnd.get(100) < 2)) {
                  tb.append("!");
                  for (int i = Rnd.get(2, 13); i > 0; i--) {
                    tb.append("!");
                  }
                  player.sayString(FakePlayersTablePlus.this.getRandomEnchantPhrase() + tb.toString(), 1);
                  tb.clear();
                }
              }
              player.sendItems(true);
              player.broadcastUserInfo();
              break;
            case 2:
              if (Rnd.get(100) >= 5) break;
              player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(player.getX() + Rnd.get(30), player.getY() + Rnd.get(30), player.getZ(), 0));
            }

            try
            {
              Thread.sleep(Rnd.get(500, 1500));
            } catch (InterruptedException e) {
            }
            count++;
          }
          if (count > 55)
            break;
        }
      }
      int count;
      tb.clear();
      tb = null;
      ThreadPoolManager.getInstance().scheduleGeneral(new Social(FakePlayersTablePlus.this), 12000L);
    }
  }

  public class FantomTaskDespawn
    implements Runnable
  {
    public int task;

    public FantomTaskDespawn(int task)
    {
      this.task = task;
    }

    public void run() {
      Location loc = null;
      L2PcInstance next = null;
      ConcurrentLinkedQueue players = (ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(task));
      for (L2PcInstance fantom : players) {
        if (fantom == null)
        {
          continue;
        }
        loc = fantom.getFakeLoc();
        fantom.kick();
        fantom.setOnlineStatus(false);
        ((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(task))).remove(fantom);
        FakePlayersTablePlus.access$710(FakePlayersTablePlus.this);
        try
        {
          Thread.sleep(task == 1 ? Config.FAKE_PLAYERS_PLUS_DELAY_DESPAWN_FIRST : Config.FAKE_PLAYERS_PLUS_DELAY_DESPAWN_NEXT);
        }
        catch (InterruptedException e) {
        }
        if (_fakesTownTotal > FakePlayersTablePlus._fakesLimit)
        {
          continue;
        }
        next = FakePlayersTablePlus.this.restoreFake(FakePlayersTablePlus.access$900(FakePlayersTablePlus.this));
        next.setFakeLoc(loc.x, loc.y, loc.z);
        next.setXYZInvisible(loc.x + Rnd.get(60), loc.y + Rnd.get(60), loc.z);

        wearFantom(next);

        if ((Config.SOULSHOT_ANIM) && (Rnd.get(100) < 45)) {
          try {
            Thread.sleep(900L);
          }
          catch (InterruptedException e) {
          }
          if (Rnd.get(100) < 3) {
            next.sitDown();
          }

          next.broadcastPacket(new MagicSkillUser(next, next, 2154, 1, 0, 0));
          try
          {
            Thread.sleep(300L);
          } catch (InterruptedException e) {
          }
          next.broadcastPacket(new MagicSkillUser(next, next, 2164, 1, 0, 0));
        }

        ((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(task))).add(next);
        FakePlayersTablePlus.access$708(FakePlayersTablePlus.this);
        try {
          Thread.sleep(100L);
        } catch (InterruptedException e) {
        }
      }
      loc = null;
      next = null;
      ThreadPoolManager.getInstance().scheduleGeneral(new FantomTaskDespawn(FakePlayersTablePlus.this, 1), task == 1 ? Config.FAKE_PLAYERS_PLUS_DESPAWN_FIRST : Config.FAKE_PLAYERS_PLUS_DESPAWN_NEXT);
    }
  }

  public class FantomTask
    implements Runnable
  {
    public int task;

    public FantomTask(int task)
    {
      this.task = task;
    }

    public void run() {
      switch (task) {
      case 1:
        FakePlayersTablePlus._log.info("FakePlayersTablePlus: 1st wave, spawn started.");
        int count = 0;
        int fakeObjId = 0;
        L2PcInstance fantom = null;
        while (count < Config.FAKE_PLAYERS_PLUS_COUNT_FIRST)
        {
          fakeObjId = FakePlayersTablePlus.this.getRandomFake();

          if (((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(1))).contains(Integer.valueOf(fakeObjId)))
          {
            continue;
          }

          fantom = FakePlayersTablePlus.this.restoreFake(fakeObjId);

          wearFantom(fantom);

          if ((Config.SOULSHOT_ANIM) && (Rnd.get(100) < 45)) {
            try {
              Thread.sleep(900L);
            }
            catch (InterruptedException e) {
            }
            if (Rnd.get(100) < 3) {
              fantom.sitDown();
            }

            fantom.broadcastPacket(new MagicSkillUser(fantom, fantom, 2154, 1, 0, 0));
            try {
              Thread.sleep(300L);
            } catch (InterruptedException e) {
            }
            fantom.broadcastPacket(new MagicSkillUser(fantom, fantom, 2164, 1, 0, 0));
          }

          ((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(1))).add(fantom);
          try
          {
            Thread.sleep(Config.FAKE_PLAYERS_PLUS_DELAY_SPAWN_FIRST);
          } catch (InterruptedException e) {
          }
          count++;
          FakePlayersTablePlus.access$708(FakePlayersTablePlus.this);
        }

        FakePlayersTablePlus._log.info("FakePlayersTablePlus: 1st wave, spawned " + count + " players.");
        Online.getInstance().checkMaxOnline();
        ThreadPoolManager.getInstance().scheduleGeneral(new FakePlayersTablePlus.FantomTaskDespawn(FakePlayersTablePlus.this, 1), Config.FAKE_PLAYERS_PLUS_DESPAWN_FIRST);
        ThreadPoolManager.getInstance().scheduleGeneral(new FantomTask(FakePlayersTablePlus.this, 2), Config.FAKE_PLAYERS_PLUS_DELAY_NEXT);
        ThreadPoolManager.getInstance().scheduleGeneral(new FakePlayersTablePlus.Social(FakePlayersTablePlus.this), 12000L);
        ThreadPoolManager.getInstance().scheduleGeneral(new FakePlayersTablePlus.CheckCount(FakePlayersTablePlus.this), 300000L);
        break;
      case 2:
        FakePlayersTablePlus._log.info("FakePlayersTablePlus: 2nd wave, spawn started.");
        int count2 = 0;
        int fakeObjId2 = 0;
        L2PcInstance fantom2 = null;
        while (count2 < Config.FAKE_PLAYERS_PLUS_COUNT_NEXT) {
          fakeObjId2 = FakePlayersTablePlus.this.getRandomFake();
          if (((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(2))).contains(Integer.valueOf(fakeObjId2)))
          {
            continue;
          }
          fantom2 = FakePlayersTablePlus.this.restoreFake(fakeObjId2);
          wearFantom(fantom2);

          if ((Config.SOULSHOT_ANIM) && (Rnd.get(100) < 45)) {
            try {
              Thread.sleep(900L);
            }
            catch (InterruptedException e) {
            }
            if (Rnd.get(100) < 3) {
              fantom2.sitDown();
            }

            fantom2.broadcastPacket(new MagicSkillUser(fantom2, fantom2, 2154, 1, 0, 0));
            try {
              Thread.sleep(300L);
            } catch (InterruptedException e) {
            }
            fantom2.broadcastPacket(new MagicSkillUser(fantom2, fantom2, 2164, 1, 0, 0));
          }
          ((ConcurrentLinkedQueue)FakePlayersTablePlus._fakesTown.get(Integer.valueOf(2))).add(fantom2);
          try {
            Thread.sleep(Config.FAKE_PLAYERS_PLUS_DELAY_SPAWN_NEXT);
          } catch (InterruptedException e) {
          }
          count2++;
          FakePlayersTablePlus.access$708(FakePlayersTablePlus.this);
        }
        FakePlayersTablePlus._log.info("FakePlayersTablePlus: 2nd wave, spawned " + count2 + " players.");
        Online.getInstance().checkMaxOnline();
        ThreadPoolManager.getInstance().scheduleGeneral(new FakePlayersTablePlus.FantomTaskDespawn(FakePlayersTablePlus.this, 2), Config.FAKE_PLAYERS_PLUS_DESPAWN_NEXT);
      }
    }
  }
}