package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class NpcTable
{
  private static Logger _log;
  private static NpcTable _instance;
  private Map<Integer, L2NpcTemplate> _npcs;
  private boolean _initialized = false;

  public static NpcTable getInstance()
  {
    if (_instance == null) {
      _instance = new NpcTable();
    }
    return _instance;
  }

  private NpcTable()
  {
    _npcs = new FastMap();

    restoreNpcData();
  }

  private void restoreNpcData()
  {
    Connection con = null;
    try
    {
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();

        PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] { "id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "class", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI" }) + " FROM npc");
        ResultSet npcdata = statement.executeQuery();

        fillNpcTable(npcdata);
        npcdata.close();
        statement.close();
      }
      catch (Exception e)
      {
        _log.severe("NPCTable: Error creating NPC table: " + e);
      }
      finally {
        try {
          con.close(); } catch (Exception e) { e.printStackTrace();
        }
      }
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
        ResultSet npcskills = statement.executeQuery();
        L2NpcTemplate npcDat = null;
        L2Skill npcSkill = null;

        while (npcskills.next())
        {
          int mobId = npcskills.getInt("npcid");
          npcDat = (L2NpcTemplate)_npcs.get(Integer.valueOf(mobId));

          if (npcDat == null) {
            continue;
          }
          int skillId = npcskills.getInt("skillid");
          int level = npcskills.getInt("level");

          if ((npcDat.race == null) && (skillId == 4416))
          {
            npcDat.setRace(level);
            continue;
          }

          npcSkill = SkillTable.getInstance().getInfo(skillId, level);

          if (npcSkill == null) {
            continue;
          }
          npcDat.addSkill(npcSkill);
        }

        npcskills.close();
        statement.close();
      }
      catch (Exception e) {
        _log.severe("NPCTable: Error reading NPC skills table: " + e);
      }

      try
      {
        PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] { "mobId", "itemId", "min", "max", "category", "chance" }) + " FROM droplist ORDER BY mobId, chance DESC");
        ResultSet dropData = statement2.executeQuery();
        L2DropData dropDat = null;
        L2NpcTemplate npcDat = null;

        while (dropData.next())
        {
          int mobId = dropData.getInt("mobId");
          npcDat = (L2NpcTemplate)_npcs.get(Integer.valueOf(mobId));
          if (npcDat == null)
          {
            _log.severe("NPCTable: No npc correlating with id : " + mobId);
            continue;
          }
          dropDat = new L2DropData();

          dropDat.setItemId(dropData.getInt("itemId"));
          dropDat.setMinDrop(dropData.getInt("min"));
          dropDat.setMaxDrop(dropData.getInt("max"));
          dropDat.setChance(dropData.getInt("chance"));

          int category = dropData.getInt("category");

          npcDat.addDropData(dropDat, category);
        }

        dropData.close();
        statement2.close();
      }
      catch (Exception e) {
        _log.severe("NPCTable: Error reading NPC drop data: " + e);
      }

      try
      {
        PreparedStatement statement3 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] { "npc_id", "class_id" }) + " FROM skill_learn");
        ResultSet learndata = statement3.executeQuery();

        while (learndata.next())
        {
          int npcId = learndata.getInt("npc_id");
          int classId = learndata.getInt("class_id");
          L2NpcTemplate npc = getTemplate(npcId);

          if (npc == null)
          {
            _log.warning("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
            continue;
          }

          npc.addTeachInfo(ClassId.values()[classId]);
        }

        learndata.close();
        statement3.close();
      }
      catch (Exception e) {
        _log.severe("NPCTable: Error reading NPC trainer data: " + e);
      }

      try
      {
        PreparedStatement statement4 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] { "boss_id", "minion_id", "amount_min", "amount_max" }) + " FROM minions");
        ResultSet minionData = statement4.executeQuery();
        L2MinionData minionDat = null;
        L2NpcTemplate npcDat = null;
        int cnt = 0;

        while (minionData.next())
        {
          int raidId = minionData.getInt("boss_id");
          npcDat = (L2NpcTemplate)_npcs.get(Integer.valueOf(raidId));
          minionDat = new L2MinionData();
          minionDat.setMinionId(minionData.getInt("minion_id"));
          minionDat.setAmountMin(minionData.getInt("amount_min"));
          minionDat.setAmountMax(minionData.getInt("amount_max"));
          npcDat.addRaidData(minionDat);
          cnt++;
        }

        minionData.close();
        statement4.close();
        _log.config("NpcTable: Loaded " + cnt + " Minions.");
      }
      catch (Exception e) {
        _log.severe("Error loading minion data: " + e);
      }
    } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    _initialized = true;
  }

  private void fillNpcTable(ResultSet NpcData)
    throws Exception
  {
    while (NpcData.next())
    {
      StatsSet npcDat = new StatsSet();
      int id = NpcData.getInt("id");

      if ((Config.ASSERT) && 
        (!$assertionsDisabled) && (id >= 1000000)) throw new AssertionError();

      npcDat.set("npcId", id);
      npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
      int level = NpcData.getInt("level");
      npcDat.set("level", level);
      npcDat.set("jClass", NpcData.getString("class"));

      npcDat.set("baseShldDef", 0);
      npcDat.set("baseShldRate", 0);
      npcDat.set("baseCritRate", 38);

      npcDat.set("name", NpcData.getString("name"));
      npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
      npcDat.set("title", NpcData.getString("title"));
      npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
      npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
      npcDat.set("collision_height", NpcData.getDouble("collision_height"));
      npcDat.set("sex", NpcData.getString("sex"));
      npcDat.set("type", NpcData.getString("type"));
      npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
      npcDat.set("rewardExp", NpcData.getInt("exp"));
      npcDat.set("rewardSp", NpcData.getInt("sp"));
      npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
      npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
      npcDat.set("aggroRange", NpcData.getInt("aggro"));
      npcDat.set("rhand", NpcData.getInt("rhand"));
      npcDat.set("lhand", NpcData.getInt("lhand"));
      npcDat.set("armor", NpcData.getInt("armor"));
      npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
      npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
      npcDat.set("baseSTR", NpcData.getInt("str"));
      npcDat.set("baseCON", NpcData.getInt("con"));
      npcDat.set("baseDEX", NpcData.getInt("dex"));
      npcDat.set("baseINT", NpcData.getInt("int"));
      npcDat.set("baseWIT", NpcData.getInt("wit"));
      npcDat.set("baseMEN", NpcData.getInt("men"));
      npcDat.set("baseHpMax", NpcData.getInt("hp"));
      npcDat.set("baseCpMax", 0);
      npcDat.set("baseMpMax", NpcData.getInt("mp"));
      npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0.0F ? NpcData.getFloat("hpreg") : 1.5D + (level - 1) / 10.0D);
      npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0.0F ? NpcData.getFloat("mpreg") : 0.9D + 0.3D * ((level - 1) / 10.0D));
      npcDat.set("basePAtk", NpcData.getInt("patk"));
      npcDat.set("basePDef", NpcData.getInt("pdef"));
      npcDat.set("baseMAtk", NpcData.getInt("matk"));
      npcDat.set("baseMDef", NpcData.getInt("mdef"));

      npcDat.set("factionId", NpcData.getString("faction_id"));
      npcDat.set("factionRange", NpcData.getInt("faction_range"));

      npcDat.set("isUndead", NpcData.getString("isUndead"));

      npcDat.set("absorb_level", NpcData.getString("absorb_level"));
      npcDat.set("absorb_type", NpcData.getString("absorb_type"));

      npcDat.set("ss", NpcData.getInt("ss"));
      npcDat.set("bss", NpcData.getInt("bss"));
      npcDat.set("ssRate", NpcData.getInt("ss_rate"));

      npcDat.set("AI", NpcData.getString("AI"));

      L2NpcTemplate template = new L2NpcTemplate(npcDat);
      template.addVulnerability(Stats.BOW_WPN_VULN, 1.0D);
      template.addVulnerability(Stats.BLUNT_WPN_VULN, 1.0D);
      template.addVulnerability(Stats.DAGGER_WPN_VULN, 1.0D);

      _npcs.put(Integer.valueOf(id), template);
    }

    _log.config("NpcTable: Loaded " + _npcs.size() + " Npc Templates.");
  }

  public void reloadNpc(int id)
  {
    Connection con = null;
    try
    {
      L2NpcTemplate old = getTemplate(id);
      Map skills = new FastMap();

      if (old.getSkills() != null) {
        skills.putAll(old.getSkills());
      }
      FastList categories = new FastList();

      if (old.getDropData() != null) {
        categories.addAll(old.getDropData());
      }
      ClassId[] classIds = null;

      if (old.getTeachInfo() != null) {
        classIds = (ClassId[])old.getTeachInfo().clone();
      }
      List minions = new FastList();

      if (old.getMinionData() != null) {
        minions.addAll(old.getMinionData());
      }

      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] { "id", "idTemplate", "name", "serverSideName", "title", "serverSideTitle", "class", "collision_radius", "collision_height", "level", "sex", "type", "attackrange", "hp", "mp", "hpreg", "mpreg", "str", "con", "dex", "int", "wit", "men", "exp", "sp", "patk", "pdef", "matk", "mdef", "atkspd", "aggro", "matkspd", "rhand", "lhand", "armor", "walkspd", "runspd", "faction_id", "faction_range", "isUndead", "absorb_level", "absorb_type", "ss", "bss", "ss_rate", "AI" }) + " FROM npc WHERE id=?");
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      fillNpcTable(rs);
      rs.close();
      st.close();

      created = getTemplate(id);

      for (L2Skill skill : skills.values()) {
        created.addSkill(skill);
      }
      if (classIds != null) {
        for (ClassId classId : classIds)
          created.addTeachInfo(classId);
      }
      for (L2MinionData minion : minions)
        created.addRaidData(minion);
    }
    catch (Exception e)
    {
      L2NpcTemplate created;
      _log.warning("NPCTable: Could not reload data for NPC " + id + ": " + e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public void reloadAllNpc() {
    restoreNpcData();
  }

  public void saveNpc(StatsSet npc)
  {
    Connection con = null;
    String query = "";
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      Map set = npc.getSet();

      String name = "";
      String values = "";

      for (Object obj : set.keySet())
      {
        name = (String)obj;

        if (!name.equalsIgnoreCase("npcId"))
        {
          if (values != "") {
            values = values + ", ";
          }
          values = values + name + " = '" + set.get(name) + "'";
        }
      }

      query = "UPDATE npc SET " + values + " WHERE id = ?";
      PreparedStatement statement = con.prepareStatement(query);
      statement.setInt(1, npc.getInteger("npcId"));
      statement.execute();
      statement.close();
    }
    catch (Exception e)
    {
      _log.warning("NPCTable: Could not store new NPC data in database: " + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
  }

  public boolean isInitialized() {
    return _initialized;
  }

  public void replaceTemplate(L2NpcTemplate npc)
  {
    _npcs.put(Integer.valueOf(npc.npcId), npc);
  }

  public L2NpcTemplate getTemplate(int id)
  {
    return (L2NpcTemplate)_npcs.get(Integer.valueOf(id));
  }

  public L2NpcTemplate getTemplateByName(String name)
  {
    for (L2NpcTemplate npcTemplate : _npcs.values()) {
      if (npcTemplate.name.equalsIgnoreCase(name))
        return npcTemplate;
    }
    return null;
  }

  public L2NpcTemplate[] getAllOfLevel(int lvl)
  {
    List list = new FastList();

    for (L2NpcTemplate t : _npcs.values()) {
      if (t.level == lvl)
        list.add(t);
    }
    return (L2NpcTemplate[])list.toArray(new L2NpcTemplate[list.size()]);
  }

  public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
  {
    List list = new FastList();

    for (L2NpcTemplate t : _npcs.values()) {
      if ((t.level == lvl) && ("L2Monster".equals(t.type)))
        list.add(t);
    }
    return (L2NpcTemplate[])list.toArray(new L2NpcTemplate[list.size()]);
  }

  public L2NpcTemplate[] getAllNpcStartingWith(String letter)
  {
    List list = new FastList();

    for (L2NpcTemplate t : _npcs.values()) {
      if ((t.name.startsWith(letter)) && ("L2Npc".equals(t.type)))
        list.add(t);
    }
    return (L2NpcTemplate[])list.toArray(new L2NpcTemplate[list.size()]);
  }

  public Set<Integer> getAllNpcOfClassType(String classType)
  {
    return null;
  }

  public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
  {
    return null;
  }

  public Set<Integer> getAllNpcOfAiType(String aiType)
  {
    return null;
  }

  static
  {
    _log = Logger.getLogger(NpcTable.class.getName());
  }
}