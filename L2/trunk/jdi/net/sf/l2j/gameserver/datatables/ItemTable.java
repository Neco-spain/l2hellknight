package net.sf.l2j.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Item;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

public class ItemTable
{
  private static Logger _log;
  private static Logger _logItems;
  private static final Map<String, Integer> _materials;
  private static final Map<String, Integer> _crystalTypes;
  private static final Map<String, L2WeaponType> _weaponTypes;
  private static final Map<String, L2ArmorType> _armorTypes;
  private static final Map<String, Integer> _slots;
  private L2Item[] _allTemplates;
  private Map<Integer, L2EtcItem> _etcItems;
  private Map<Integer, L2Armor> _armors;
  private Map<Integer, L2Weapon> _weapons;
  private boolean _initialized = true;
  private static ItemTable _instance;
  private static final String[] SQL_ITEM_SELECTS;
  private static final Map<Integer, Item> itemData;
  private static final Map<Integer, Item> weaponData;
  private static final Map<Integer, Item> armorData;

  public static ItemTable getInstance()
  {
    if (_instance == null)
    {
      _instance = new ItemTable();
    }
    return _instance;
  }

  public Item newItem()
  {
    return new Item();
  }

  public ItemTable()
  {
    _etcItems = new FastMap();
    _armors = new FastMap();
    _weapons = new FastMap();

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      for (String selectQuery : SQL_ITEM_SELECTS)
      {
        PreparedStatement statement = con.prepareStatement(selectQuery);
        ResultSet rset = statement.executeQuery();

        while (rset.next())
        {
          if (selectQuery.endsWith("etcitem"))
          {
            Item newItem = readItem(rset);
            itemData.put(Integer.valueOf(newItem.id), newItem);
            continue;
          }if (selectQuery.endsWith("armor"))
          {
            Item newItem = readArmor(rset);
            armorData.put(Integer.valueOf(newItem.id), newItem);
            continue;
          }if (!selectQuery.endsWith("weapon"))
            continue;
          Item newItem = readWeapon(rset);
          weaponData.put(Integer.valueOf(newItem.id), newItem);
        }

        rset.close();
        statement.close();
      }
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "data error on item: ", e); } finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    for (L2Armor armor : SkillsEngine.getInstance().loadArmors(armorData))
    {
      _armors.put(Integer.valueOf(armor.getItemId()), armor);
    }
    _log.config("ItemTable: Loaded " + _armors.size() + " Armors.");

    for (L2EtcItem item : SkillsEngine.getInstance().loadItems(itemData))
    {
      _etcItems.put(Integer.valueOf(item.getItemId()), item);
    }
    _log.config("ItemTable: Loaded " + _etcItems.size() + " Items.");

    for (L2Weapon weapon : SkillsEngine.getInstance().loadWeapons(weaponData))
    {
      _weapons.put(Integer.valueOf(weapon.getItemId()), weapon);
    }
    _log.config("ItemTable: Loaded " + _weapons.size() + " Weapons.");

    buildFastLookupTable();
  }

  private Item readWeapon(ResultSet rset)
    throws SQLException
  {
    Item item = new Item();
    item.set = new StatsSet();
    item.type = ((Enum)_weaponTypes.get(rset.getString("weaponType")));
    item.id = rset.getInt("item_id");
    item.name = rset.getString("name");

    item.set.set("item_id", item.id);
    item.set.set("name", item.name);

    if (item.type == L2WeaponType.NONE)
    {
      item.set.set("type1", 1);
      item.set.set("type2", 1);
    }
    else
    {
      item.set.set("type1", 0);
      item.set.set("type2", 0);
    }

    item.set.set("bodypart", ((Integer)_slots.get(rset.getString("bodypart"))).intValue());
    item.set.set("material", ((Integer)_materials.get(rset.getString("material"))).intValue());
    item.set.set("crystal_type", ((Integer)_crystalTypes.get(rset.getString("crystal_type"))).intValue());
    item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
    item.set.set("weight", rset.getInt("weight"));
    item.set.set("soulshots", rset.getInt("soulshots"));
    item.set.set("spiritshots", rset.getInt("spiritshots"));
    item.set.set("p_dam", rset.getInt("p_dam"));
    item.set.set("rnd_dam", rset.getInt("rnd_dam"));
    item.set.set("critical", rset.getInt("critical"));
    item.set.set("hit_modify", rset.getDouble("hit_modify"));
    item.set.set("avoid_modify", rset.getInt("avoid_modify"));
    item.set.set("shield_def", rset.getInt("shield_def"));
    item.set.set("shield_def_rate", rset.getInt("shield_def_rate"));
    item.set.set("atk_speed", rset.getInt("atk_speed"));
    item.set.set("mp_consume", rset.getInt("mp_consume"));
    item.set.set("m_dam", rset.getInt("m_dam"));
    item.set.set("duration", rset.getInt("duration"));
    item.set.set("price", rset.getInt("price"));
    item.set.set("crystal_count", rset.getInt("crystal_count"));
    item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")).booleanValue());
    item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")).booleanValue());
    item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")).booleanValue());
    item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")).booleanValue());

    item.set.set("item_skill_id", rset.getInt("item_skill_id"));
    item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));

    item.set.set("enchant4_skill_id", rset.getInt("enchant4_skill_id"));
    item.set.set("enchant4_skill_lvl", rset.getInt("enchant4_skill_lvl"));

    item.set.set("onCast_skill_id", rset.getInt("onCast_skill_id"));
    item.set.set("onCast_skill_lvl", rset.getInt("onCast_skill_lvl"));
    item.set.set("onCast_skill_chance", rset.getInt("onCast_skill_chance"));

    item.set.set("onCrit_skill_id", rset.getInt("onCrit_skill_id"));
    item.set.set("onCrit_skill_lvl", rset.getInt("onCrit_skill_lvl"));
    item.set.set("onCrit_skill_chance", rset.getInt("onCrit_skill_chance"));

    if (item.type == L2WeaponType.PET)
    {
      item.set.set("type1", 0);
      if (item.set.getInteger("bodypart") == 131072)
        item.set.set("type2", 6);
      else if (item.set.getInteger("bodypart") == 1048576)
        item.set.set("type2", 7);
      else if (item.set.getInteger("bodypart") == 4194304)
        item.set.set("type2", 9);
      else {
        item.set.set("type2", 8);
      }
      item.set.set("bodypart", 128);
    }

    return item;
  }

  private Item readArmor(ResultSet rset)
    throws SQLException
  {
    Item item = new Item();
    item.set = new StatsSet();
    item.type = ((Enum)_armorTypes.get(rset.getString("armor_type")));
    item.id = rset.getInt("item_id");
    item.name = rset.getString("name");

    item.set.set("item_id", item.id);
    item.set.set("name", item.name);
    int bodypart = ((Integer)_slots.get(rset.getString("bodypart"))).intValue();
    item.set.set("bodypart", bodypart);
    item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
    item.set.set("crystal_count", rset.getInt("crystal_count"));
    item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")).booleanValue());
    item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")).booleanValue());
    item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")).booleanValue());
    item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")).booleanValue());
    item.set.set("item_skill_id", rset.getInt("item_skill_id"));
    item.set.set("item_skill_lvl", rset.getInt("item_skill_lvl"));

    if ((bodypart == 8) || (bodypart == 65536) || (bodypart == 262144) || (bodypart == 524288) || ((bodypart & 0x4) != 0) || ((bodypart & 0x20) != 0))
    {
      item.set.set("type1", 0);
      item.set.set("type2", 2);
    }
    else
    {
      item.set.set("type1", 1);
      item.set.set("type2", 1);
    }

    item.set.set("weight", rset.getInt("weight"));
    item.set.set("material", ((Integer)_materials.get(rset.getString("material"))).intValue());
    item.set.set("crystal_type", ((Integer)_crystalTypes.get(rset.getString("crystal_type"))).intValue());
    item.set.set("avoid_modify", rset.getInt("avoid_modify"));
    item.set.set("duration", rset.getInt("duration"));
    item.set.set("p_def", rset.getInt("p_def"));
    item.set.set("m_def", rset.getInt("m_def"));
    item.set.set("mp_bonus", rset.getInt("mp_bonus"));
    item.set.set("price", rset.getInt("price"));

    if (item.type == L2ArmorType.PET)
    {
      item.set.set("type1", 1);
      if (item.set.getInteger("bodypart") == 131072)
        item.set.set("type2", 6);
      else if (item.set.getInteger("bodypart") == 1048576)
        item.set.set("type2", 7);
      else if (item.set.getInteger("bodypart") == 4194304)
        item.set.set("type2", 9);
      else {
        item.set.set("type2", 8);
      }
      item.set.set("bodypart", 1024);
    }

    return item;
  }

  private Item readItem(ResultSet rset)
    throws SQLException
  {
    Item item = new Item();
    item.set = new StatsSet();
    item.id = rset.getInt("item_id");

    item.set.set("item_id", item.id);
    item.set.set("crystallizable", Boolean.valueOf(rset.getString("crystallizable")).booleanValue());
    item.set.set("type1", 4);
    item.set.set("type2", 5);
    item.set.set("bodypart", 0);
    item.set.set("crystal_count", rset.getInt("crystal_count"));
    item.set.set("sellable", Boolean.valueOf(rset.getString("sellable")).booleanValue());
    item.set.set("dropable", Boolean.valueOf(rset.getString("dropable")).booleanValue());
    item.set.set("destroyable", Boolean.valueOf(rset.getString("destroyable")).booleanValue());
    item.set.set("tradeable", Boolean.valueOf(rset.getString("tradeable")).booleanValue());
    String itemType = rset.getString("item_type");
    if (itemType.equals("none")) { item.type = L2EtcItemType.OTHER;
    } else if (itemType.equals("castle_guard")) { item.type = L2EtcItemType.SCROLL;
    } else if (itemType.equals("material")) { item.type = L2EtcItemType.MATERIAL;
    } else if (itemType.equals("pet_collar")) { item.type = L2EtcItemType.PET_COLLAR;
    } else if (itemType.equals("potion")) { item.type = L2EtcItemType.POTION;
    } else if (itemType.equals("recipe")) { item.type = L2EtcItemType.RECEIPE;
    } else if (itemType.equals("scroll")) { item.type = L2EtcItemType.SCROLL;
    } else if (itemType.equals("seed")) { item.type = L2EtcItemType.SEED;
    } else if (itemType.equals("shot")) { item.type = L2EtcItemType.SHOT;
    } else if (itemType.equals("spellbook")) { item.type = L2EtcItemType.SPELLBOOK;
    } else if (itemType.equals("herb")) { item.type = L2EtcItemType.HERB;
    } else if (itemType.equals("arrow"))
    {
      item.type = L2EtcItemType.ARROW;
      item.set.set("bodypart", 256);
    }
    else if (itemType.equals("quest"))
    {
      item.type = L2EtcItemType.QUEST;
      item.set.set("type2", 3);
    }
    else if (itemType.equals("lure"))
    {
      item.type = L2EtcItemType.OTHER;
      item.set.set("bodypart", 256);
    }
    else
    {
      _log.fine("unknown etcitem type:" + itemType);
      item.type = L2EtcItemType.OTHER;
    }

    String consume = rset.getString("consume_type");
    if (consume.equals("asset"))
    {
      item.type = L2EtcItemType.MONEY;
      item.set.set("stackable", true);
      item.set.set("type2", 4);
    }
    else if (consume.equals("stackable"))
    {
      item.set.set("stackable", true);
    }
    else
    {
      item.set.set("stackable", false);
    }

    int material = ((Integer)_materials.get(rset.getString("material"))).intValue();
    item.set.set("material", material);

    int crystal = ((Integer)_crystalTypes.get(rset.getString("crystal_type"))).intValue();
    item.set.set("crystal_type", crystal);

    int weight = rset.getInt("weight");
    item.set.set("weight", weight);
    item.name = rset.getString("name");
    item.set.set("name", item.name);

    item.set.set("duration", rset.getInt("duration"));
    item.set.set("price", rset.getInt("price"));

    return item;
  }

  public boolean isInitialized()
  {
    return _initialized;
  }

  private void buildFastLookupTable()
  {
    int highestId = 0;

    for (Iterator iter = _armors.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2Armor item = (L2Armor)_armors.get(id);
      if (item.getItemId() > highestId)
      {
        highestId = item.getItemId();
      }
    }
    for (Iterator iter = _weapons.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2Weapon item = (L2Weapon)_weapons.get(id);
      if (item.getItemId() > highestId)
      {
        highestId = item.getItemId();
      }
    }
    for (Iterator iter = _etcItems.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2EtcItem item = (L2EtcItem)_etcItems.get(id);
      if (item.getItemId() > highestId)
      {
        highestId = item.getItemId();
      }

    }

    if (Config.DEBUG) _log.fine("highest item id used:" + highestId);
    _allTemplates = new L2Item[highestId + 1];

    for (Iterator iter = _armors.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2Armor item = (L2Armor)_armors.get(id);
      assert (_allTemplates[id.intValue()] == null);
      _allTemplates[id.intValue()] = item;
    }

    for (Iterator iter = _weapons.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2Weapon item = (L2Weapon)_weapons.get(id);
      assert (_allTemplates[id.intValue()] == null);
      _allTemplates[id.intValue()] = item;
    }

    for (Iterator iter = _etcItems.keySet().iterator(); iter.hasNext(); )
    {
      Integer id = (Integer)iter.next();
      L2EtcItem item = (L2EtcItem)_etcItems.get(id);
      assert (_allTemplates[id.intValue()] == null);
      _allTemplates[id.intValue()] = item;
    }
  }

  public L2Item getTemplate(int id)
  {
    if (id > _allTemplates.length) {
      return null;
    }
    return _allTemplates[id];
  }

  public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);

    if ((process.equalsIgnoreCase("loot")) && (!actor.isAutoLoot()))
    {
      long delay = 0L;

      if (((reference != null) && ((reference instanceof L2GrandBossInstance))) || ((reference instanceof L2RaidBossInstance)))
      {
        if ((((L2Attackable)reference).getFirstCommandChannelAttacked() != null) && (((L2Attackable)reference).getFirstCommandChannelAttacked().meetRaidWarCondition(reference)))
        {
          item.setOwnerId(((L2Attackable)reference).getFirstCommandChannelAttacked().getChannelLeader().getObjectId());
          delay = 300000L;
        }
        else
        {
          delay = 15000L;
          item.setOwnerId(actor.getObjectId());
        }
      }
      else
      {
        item.setOwnerId(actor.getObjectId());
        delay = 15000L;
      }
      ScheduledFuture itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new resetOwner(item), delay);
      item.setItemLootShedule(itemLootShedule);
    }

    if (Config.DEBUG) _log.fine("ItemTable: Item created  oid:" + item.getObjectId() + " itemid:" + itemId);

    L2World.getInstance().storeObject(item);

    if ((item.isStackable()) && (count > 1)) item.setCount(count);

    if (Config.LOG_ITEMS)
    {
      LogRecord record = new LogRecord(Level.INFO, "CREATE:" + process);
      record.setLoggerName("item");
      record.setParameters(new Object[] { item, actor, reference });
      _logItems.log(record);
    }

    return item;
  }

  public L2ItemInstance createItem(String process, int itemId, int count, L2PcInstance actor)
  {
    return createItem(process, itemId, count, actor, null);
  }

  public L2ItemInstance createDummyItem(int itemId)
  {
    L2Item item = getTemplate(itemId);
    if (item == null)
      return null;
    L2ItemInstance temp = new L2ItemInstance(0, item);
    try
    {
      temp = new L2ItemInstance(0, itemId);
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
    }

    if (temp.getItem() == null)
    {
      _log.warning("ItemTable: Item Template missing for Id: " + itemId);
    }

    return temp;
  }

  public void destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    synchronized (item)
    {
      item.setCount(0);
      item.setOwnerId(0);
      item.setLocation(L2ItemInstance.ItemLocation.VOID);
      item.setLastChange(3);

      L2World.getInstance().removeObject(item);
      IdFactory.getInstance().releaseId(item.getObjectId());

      if (Config.LOG_ITEMS)
      {
        LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
        record.setLoggerName("item");
        record.setParameters(new Object[] { item, actor, reference });
        _logItems.log(record);
      }

      if (L2PetDataTable.isPetItem(item.getItemId()))
      {
        Connection con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
          statement.setInt(1, item.getObjectId());
          statement.execute();
          statement.close();
        }
        catch (Exception e)
        {
          _log.log(Level.WARNING, "could not delete pet objectid:", e);
        }
        finally {
          try {
            con.close(); } catch (Exception e) {
          }
        }
      }
    }
  }

  public void reload() {
    synchronized (_instance)
    {
      _instance = null;
      _instance = new ItemTable();
    }
  }

  static
  {
    _log = Logger.getLogger(ItemTable.class.getName());
    _logItems = Logger.getLogger("item");

    _materials = new FastMap();
    _crystalTypes = new FastMap();
    _weaponTypes = new FastMap();
    _armorTypes = new FastMap();
    _slots = new FastMap();

    _materials.put("paper", Integer.valueOf(8));
    _materials.put("wood", Integer.valueOf(9));
    _materials.put("liquid", Integer.valueOf(18));
    _materials.put("cloth", Integer.valueOf(10));
    _materials.put("leather", Integer.valueOf(11));
    _materials.put("horn", Integer.valueOf(13));
    _materials.put("bone", Integer.valueOf(12));
    _materials.put("bronze", Integer.valueOf(3));
    _materials.put("fine_steel", Integer.valueOf(1));
    _materials.put("cotton", Integer.valueOf(1));
    _materials.put("mithril", Integer.valueOf(6));
    _materials.put("silver", Integer.valueOf(4));
    _materials.put("gold", Integer.valueOf(5));
    _materials.put("adamantaite", Integer.valueOf(15));
    _materials.put("steel", Integer.valueOf(0));
    _materials.put("oriharukon", Integer.valueOf(7));
    _materials.put("blood_steel", Integer.valueOf(2));
    _materials.put("crystal", Integer.valueOf(17));
    _materials.put("damascus", Integer.valueOf(14));
    _materials.put("chrysolite", Integer.valueOf(16));
    _materials.put("scale_of_dragon", Integer.valueOf(19));
    _materials.put("dyestuff", Integer.valueOf(20));
    _materials.put("cobweb", Integer.valueOf(21));
    _materials.put("seed", Integer.valueOf(21));

    _crystalTypes.put("s", Integer.valueOf(5));
    _crystalTypes.put("a", Integer.valueOf(4));
    _crystalTypes.put("b", Integer.valueOf(3));
    _crystalTypes.put("c", Integer.valueOf(2));
    _crystalTypes.put("d", Integer.valueOf(1));
    _crystalTypes.put("none", Integer.valueOf(0));

    _weaponTypes.put("blunt", L2WeaponType.BLUNT);
    _weaponTypes.put("bow", L2WeaponType.BOW);
    _weaponTypes.put("dagger", L2WeaponType.DAGGER);
    _weaponTypes.put("dual", L2WeaponType.DUAL);
    _weaponTypes.put("dualfist", L2WeaponType.DUALFIST);
    _weaponTypes.put("etc", L2WeaponType.ETC);
    _weaponTypes.put("fist", L2WeaponType.FIST);
    _weaponTypes.put("none", L2WeaponType.NONE);
    _weaponTypes.put("pole", L2WeaponType.POLE);
    _weaponTypes.put("sword", L2WeaponType.SWORD);
    _weaponTypes.put("bigsword", L2WeaponType.BIGSWORD);
    _weaponTypes.put("pet", L2WeaponType.PET);
    _weaponTypes.put("rod", L2WeaponType.ROD);
    _weaponTypes.put("bigblunt", L2WeaponType.BIGBLUNT);
    _armorTypes.put("none", L2ArmorType.NONE);
    _armorTypes.put("light", L2ArmorType.LIGHT);
    _armorTypes.put("heavy", L2ArmorType.HEAVY);
    _armorTypes.put("magic", L2ArmorType.MAGIC);
    _armorTypes.put("pet", L2ArmorType.PET);

    _slots.put("chest", Integer.valueOf(1024));
    _slots.put("fullarmor", Integer.valueOf(32768));
    _slots.put("head", Integer.valueOf(64));
    _slots.put("hair", Integer.valueOf(65536));
    _slots.put("face", Integer.valueOf(262144));
    _slots.put("dhair", Integer.valueOf(524288));
    _slots.put("underwear", Integer.valueOf(1));
    _slots.put("back", Integer.valueOf(8192));
    _slots.put("neck", Integer.valueOf(8));
    _slots.put("legs", Integer.valueOf(2048));
    _slots.put("feet", Integer.valueOf(4096));
    _slots.put("gloves", Integer.valueOf(512));
    _slots.put("chest,legs", Integer.valueOf(3072));
    _slots.put("rhand", Integer.valueOf(128));
    _slots.put("lhand", Integer.valueOf(256));
    _slots.put("lrhand", Integer.valueOf(16384));
    _slots.put("rear,lear", Integer.valueOf(6));
    _slots.put("rfinger,lfinger", Integer.valueOf(48));
    _slots.put("none", Integer.valueOf(0));
    _slots.put("wolf", Integer.valueOf(131072));
    _slots.put("hatchling", Integer.valueOf(1048576));
    _slots.put("strider", Integer.valueOf(2097152));
    _slots.put("babypet", Integer.valueOf(4194304));

    SQL_ITEM_SELECTS = new String[] { "SELECT item_id, name, crystallizable, item_type, weight, consume_type, material, crystal_type, duration, price, crystal_count, sellable, dropable, destroyable, tradeable FROM etcitem", "SELECT item_id, name, bodypart, crystallizable, armor_type, weight, material, crystal_type, avoid_modify, duration, p_def, m_def, mp_bonus, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl FROM armor", "SELECT item_id, name, bodypart, crystallizable, weight, soulshots, spiritshots, material, crystal_type, p_dam, rnd_dam, weaponType, critical, hit_modify, avoid_modify, shield_def, shield_def_rate, atk_speed, mp_consume, m_dam, duration, price, crystal_count, sellable, dropable, destroyable, tradeable, item_skill_id, item_skill_lvl,enchant4_skill_id,enchant4_skill_lvl, onCast_skill_id, onCast_skill_lvl, onCast_skill_chance, onCrit_skill_id, onCrit_skill_lvl, onCrit_skill_chance FROM weapon" };

    itemData = new FastMap();

    weaponData = new FastMap();

    armorData = new FastMap();
  }

  protected class resetOwner
    implements Runnable
  {
    L2ItemInstance _item;

    public resetOwner(L2ItemInstance item)
    {
      _item = item;
    }

    public void run()
    {
      _item.setOwnerId(0);
      _item.setItemLootShedule(null);
    }
  }
}