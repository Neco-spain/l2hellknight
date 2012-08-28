package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2CustomNpcInstance;
import net.sf.l2j.util.Rnd;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CustomNpcInstanceManager
{
  private static final Log _log = LogFactory.getLog(CustomNpcInstanceManager.class.getName());
  private static CustomNpcInstanceManager _instance;
  private FastMap<Integer, customInfo> spawns;
  private FastMap<Integer, customInfo> templates;

  CustomNpcInstanceManager()
  {
    load();
  }

  public static final CustomNpcInstanceManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new CustomNpcInstanceManager();
    }
    return _instance;
  }

  public final void reload()
  {
    if (spawns != null) spawns.clear();
    if (templates != null) templates.clear();
    spawns = null;
    templates = null;

    load();

    for (L2Spawn spawn : SpawnTable.getInstance().getAllTemplates().values())
    {
      if ((spawn == null) || 
        (spawn.getLastSpawn() == null)) {
        continue;
      }
      new L2CustomNpcInstance(spawn.getLastSpawn());
    }
  }

  private final void load()
  {
    if ((spawns == null) || (templates == null))
    {
      spawns = new FastMap();
      templates = new FastMap();
    }

    String[] SQL_ITEM_SELECTS = { "SELECT spawn,template,name,title,class_id,female,hair_style,hair_color,face,name_color,title_color, noble,hero,pvp,karma,wpn_enchant,right_hand,left_hand,gloves,chest,legs,feet,hair,hair2, pledge,cw_level,clan_id,ally_id,clan_crest,ally_crest,rnd_class,rnd_appearance,rnd_weapon,rnd_armor,max_rnd_enchant FROM npc_to_pc_polymorph" };

    Connection con = null;
    try
    {
      int count = 0;
      con = L2DatabaseFactory.getInstance().getConnection();
      for (String selectQuery : SQL_ITEM_SELECTS)
      {
        PreparedStatement statement = con.prepareStatement(selectQuery);
        ResultSet rset = statement.executeQuery();

        while (rset.next())
        {
          count++;
          customInfo ci = new customInfo();
          ci.integerData[26] = rset.getInt("spawn");
          ci.integerData[25] = rset.getInt("template");
          try
          {
            ci.stringData[0] = rset.getString("name");
            ci.stringData[1] = rset.getString("title");
            ci.integerData[7] = rset.getInt("class_id");

            int PcSex = rset.getInt("female");
            switch (PcSex) {
            case 0:
              ci.booleanData[3] = false;
              break;
            case 1:
              ci.booleanData[3] = true;
              break;
            default:
              ci.booleanData[3] = (Rnd.get(100) > 50 ? 1 : false);
            }

            ci.integerData[19] = rset.getInt("hair_style");
            ci.integerData[20] = rset.getInt("hair_color");
            ci.integerData[21] = rset.getInt("face");
            ci.integerData[22] = rset.getInt("name_color");
            ci.integerData[23] = rset.getInt("title_color");
            ci.booleanData[1] = (rset.getInt("noble") > 0 ? 1 : false);
            ci.booleanData[2] = (rset.getInt("hero") > 0 ? 1 : false);
            ci.booleanData[0] = (rset.getInt("pvp") > 0 ? 1 : false);
            ci.integerData[1] = rset.getInt("karma");
            ci.integerData[8] = rset.getInt("wpn_enchant");
            ci.integerData[11] = rset.getInt("right_hand");
            ci.integerData[12] = rset.getInt("left_hand");
            ci.integerData[13] = rset.getInt("gloves");
            ci.integerData[14] = rset.getInt("chest");
            ci.integerData[15] = rset.getInt("legs");
            ci.integerData[16] = rset.getInt("feet");
            ci.integerData[17] = rset.getInt("hair");
            ci.integerData[18] = rset.getInt("hair2");
            ci.integerData[9] = rset.getInt("pledge");
            ci.integerData[10] = rset.getInt("cw_level");
            ci.integerData[2] = rset.getInt("clan_id");
            ci.integerData[3] = rset.getInt("ally_id");
            ci.integerData[4] = rset.getInt("clan_crest");
            ci.integerData[5] = rset.getInt("ally_crest");
            ci.booleanData[4] = (rset.getInt("rnd_class") > 0 ? 1 : false);
            ci.booleanData[5] = (rset.getInt("rnd_appearance") > 0 ? 1 : false);
            ci.booleanData[6] = (rset.getInt("rnd_weapon") > 0 ? 1 : false);
            ci.booleanData[7] = (rset.getInt("rnd_armor") > 0 ? 1 : false);
            ci.integerData[24] = rset.getInt("max_rnd_enchant");

            if ((ci.integerData[25] != 0) && (!templates.containsKey(Integer.valueOf(ci.integerData[25]))))
              templates.put(Integer.valueOf(ci.integerData[25]), ci);
            if ((ci.integerData[25] == 0) && (!spawns.containsKey(Integer.valueOf(ci.integerData[26]))))
              spawns.put(Integer.valueOf(ci.integerData[26]), ci);
          }
          catch (Throwable t)
          {
            _log.warn("Failed to load Npc Morph data for Object Id: " + ci.integerData[26] + " template: " + ci.integerData[25]);
          }
        }
        rset.close();
        statement.close();
      }
      _log.info("CustomNpcInstanceManager: loaded " + count + " NPC to PC polymorphs.");
    }
    catch (Exception e)
    {
      _log.warn("Failed to initialize CustomNpcInstanceManager: ", e); } finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public final boolean isThisL2CustomNpcInstance(int spwnId, int npcId)
  {
    if ((spwnId == 0) || (npcId == 0)) return false;
    if (spawns.containsKey(Integer.valueOf(spwnId))) return true;
    return templates.containsKey(Integer.valueOf(npcId));
  }

  public final customInfo getCustomData(int spwnId, int npcId)
  {
    if ((spwnId == 0) || (npcId == 0)) return null;

    for (customInfo ci : spawns.values()) {
      if ((ci != null) && (ci.integerData[26] == spwnId)) return ci;
    }

    for (customInfo ci : templates.values()) {
      if ((ci != null) && (ci.integerData[25] == npcId)) return ci;
    }
    return null;
  }

  public final FastMap<Integer, customInfo> getAllTemplates()
  {
    return templates;
  }

  public final FastMap<Integer, customInfo> getAllSpawns()
  {
    return spawns;
  }

  public final void updateRemoveInDB(customInfo ciToRemove)
  {
  }

  public final void AddInDB(customInfo ciToAdd)
  {
    String Query = "REPLACE INTO npc_to_pc_polymorph VALUES spawn,template,name,title,class_id,female,hair_style,hair_color,face,name_color,title_color, noble,hero,pvp,karma,wpn_enchant,right_hand,left_hand,gloves,chest,legs,feet,hair,hair2, pledge,cw_level,clan_id,ally_id,clan_crest,ally_crest,rnd_class,rnd_appearance,rnd_weapon,rnd_armor,max_rnd_enchant FROM npc_to_pc_polymorph";
    try
    {
      Connection con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement(Query);
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        customInfo ci = new customInfo();
        ci.integerData[26] = rset.getInt("spawn");
        ci.integerData[25] = rset.getInt("template");
      }
    }
    catch (Throwable t)
    {
      _log.warn("Could not add Npc Morph info into the DB: ", t);
    }
  }

  public final class customInfo
  {
    public String[] stringData = new String[2];
    public int[] integerData = new int[27];
    public boolean[] booleanData = new boolean[8];

    public customInfo()
    {
    }
  }
}