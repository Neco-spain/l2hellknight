package l2m.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import l2p.commons.dao.JdbcDAO;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.dao.JdbcEntityStats;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.items.ItemAttributes;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.ItemInstance.ItemLocation;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemsDAO
  implements JdbcDAO<Integer, ItemInstance>
{
  private static final Logger _log = LoggerFactory.getLogger(ItemsDAO.class);
  private static final String RESTORE_ITEM = "SELECT object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_id, attribute_fire, attribute_water, attribute_wind, attribute_earth, attribute_holy, attribute_unholy, agathion_energy FROM items WHERE object_id = ?";
  private static final String RESTORE_OWNER_ITEMS = "SELECT object_id FROM items WHERE owner_id = ? AND loc = ?";
  private static final String STORE_ITEM = "INSERT INTO items (object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_id, attribute_fire, attribute_water, attribute_wind, attribute_earth, attribute_holy, attribute_unholy, agathion_energy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  private static final String UPDATE_ITEM = "UPDATE items SET owner_id = ?, item_id = ?, count = ?, enchant_level = ?, loc = ?, loc_data = ?, custom_type1 = ?, custom_type2 = ?, life_time = ?, custom_flags = ?, augmentation_id = ?, attribute_fire = ?, attribute_water = ?, attribute_wind = ?, attribute_earth = ?, attribute_holy = ?, attribute_unholy = ?, agathion_energy=? WHERE object_id = ?";
  private static final String REMOVE_ITEM = "DELETE FROM items WHERE object_id = ?";
  private static final ItemsDAO instance = new ItemsDAO();

  private AtomicLong load = new AtomicLong();
  private AtomicLong insert = new AtomicLong();
  private AtomicLong update = new AtomicLong();
  private AtomicLong delete = new AtomicLong();
  private final Cache cache;
  private final JdbcEntityStats stats = new JdbcEntityStats()
  {
    public long getLoadCount()
    {
      return load.get();
    }

    public long getInsertCount()
    {
      return insert.get();
    }

    public long getUpdateCount()
    {
      return update.get();
    }

    public long getDeleteCount()
    {
      return delete.get();
    }
  };

  public static final ItemsDAO getInstance()
  {
    return instance;
  }

  private ItemsDAO()
  {
    cache = CacheManager.getInstance().getCache(ItemInstance.class.getName());
  }

  public Cache getCache()
  {
    return cache;
  }

  public JdbcEntityStats getStats()
  {
    return stats;
  }

  private ItemInstance load0(int objectId) throws SQLException
  {
    ItemInstance item = null;
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_id, attribute_fire, attribute_water, attribute_wind, attribute_earth, attribute_holy, attribute_unholy, agathion_energy FROM items WHERE object_id = ?");
      statement.setInt(1, objectId);
      rset = statement.executeQuery();
      item = load0(rset);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    load.incrementAndGet();
    return item;
  }

  private ItemInstance load0(ResultSet rset) throws SQLException
  {
    ItemInstance item = null;

    if (rset.next())
    {
      int objectId = rset.getInt(1);
      item = new ItemInstance(objectId);

      item.setOwnerId(rset.getInt(2));
      item.setItemId(rset.getInt(3));
      item.setCount(rset.getLong(4));
      item.setEnchantLevel(rset.getInt(5));
      item.setLocName(rset.getString(6));
      item.setLocData(rset.getInt(7));
      item.setCustomType1(rset.getInt(8));
      item.setCustomType2(rset.getInt(9));
      item.setLifeTime(rset.getInt(10));
      item.setCustomFlags(rset.getInt(11));
      item.setAugmentationId(rset.getInt(12));
      item.getAttributes().setFire(rset.getInt(13));
      item.getAttributes().setWater(rset.getInt(14));
      item.getAttributes().setWind(rset.getInt(15));
      item.getAttributes().setEarth(rset.getInt(16));
      item.getAttributes().setHoly(rset.getInt(17));
      item.getAttributes().setUnholy(rset.getInt(18));
      item.setAgathionEnergy(rset.getInt(19));
    }

    return item;
  }

  private void save0(ItemInstance item, PreparedStatement statement) throws SQLException
  {
    statement.setInt(1, item.getObjectId());
    statement.setInt(2, item.getOwnerId());
    statement.setInt(3, item.getItemId());
    statement.setLong(4, item.getCount());
    statement.setInt(5, item.getEnchantLevel());
    statement.setString(6, item.getLocName());
    statement.setInt(7, item.getLocData());
    statement.setInt(8, item.getCustomType1());
    statement.setInt(9, item.getCustomType2());
    statement.setInt(10, item.getLifeTime());
    statement.setInt(11, item.getCustomFlags());
    statement.setInt(12, item.getAugmentationId());
    statement.setInt(13, item.getAttributes().getFire());
    statement.setInt(14, item.getAttributes().getWater());
    statement.setInt(15, item.getAttributes().getWind());
    statement.setInt(16, item.getAttributes().getEarth());
    statement.setInt(17, item.getAttributes().getHoly());
    statement.setInt(18, item.getAttributes().getUnholy());
    statement.setInt(19, item.getAgathionEnergy());
  }

  private void save0(ItemInstance item) throws SQLException
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO items (object_id, owner_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, life_time, custom_flags, augmentation_id, attribute_fire, attribute_water, attribute_wind, attribute_earth, attribute_holy, attribute_unholy, agathion_energy) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      save0(item, statement);
      statement.execute();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    insert.incrementAndGet();
  }

  private void delete0(ItemInstance item, PreparedStatement statement) throws SQLException
  {
    statement.setInt(1, item.getObjectId());
  }

  private void delete0(ItemInstance item) throws SQLException
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM items WHERE object_id = ?");
      delete0(item, statement);
      statement.execute();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    delete.incrementAndGet();
  }

  private void update0(ItemInstance item, PreparedStatement statement) throws SQLException
  {
    statement.setInt(19, item.getObjectId());
    statement.setInt(1, item.getOwnerId());
    statement.setInt(2, item.getItemId());
    statement.setLong(3, item.getCount());
    statement.setInt(4, item.getEnchantLevel());
    statement.setString(5, item.getLocName());
    statement.setInt(6, item.getLocData());
    statement.setInt(7, item.getCustomType1());
    statement.setInt(8, item.getCustomType2());
    statement.setInt(9, item.getLifeTime());
    statement.setInt(10, item.getCustomFlags());
    statement.setInt(11, item.getAugmentationId());
    statement.setInt(12, item.getAttributes().getFire());
    statement.setInt(13, item.getAttributes().getWater());
    statement.setInt(14, item.getAttributes().getWind());
    statement.setInt(15, item.getAttributes().getEarth());
    statement.setInt(16, item.getAttributes().getHoly());
    statement.setInt(17, item.getAttributes().getUnholy());
    statement.setInt(18, item.getAgathionEnergy());
  }

  private void update0(ItemInstance item) throws SQLException
  {
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE items SET owner_id = ?, item_id = ?, count = ?, enchant_level = ?, loc = ?, loc_data = ?, custom_type1 = ?, custom_type2 = ?, life_time = ?, custom_flags = ?, augmentation_id = ?, attribute_fire = ?, attribute_water = ?, attribute_wind = ?, attribute_earth = ?, attribute_holy = ?, attribute_unholy = ?, agathion_energy=? WHERE object_id = ?");
      update0(item, statement);
      statement.execute();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }

    update.incrementAndGet();
  }

  public ItemInstance load(Integer objectId)
  {
    Element ce = cache.get(objectId);
    if (ce != null)
    {
      ItemInstance item = (ItemInstance)ce.getObjectValue();
      return item;
    }
    ItemInstance item;
    try {
      item = load0(objectId.intValue());
      if (item == null) {
        return null;
      }
      item.setJdbcState(JdbcEntityState.STORED);
    }
    catch (SQLException e)
    {
      _log.error("Error while restoring item : " + objectId, e);
      return null;
    }

    cache.put(new Element(Integer.valueOf(item.getObjectId()), item));

    return item;
  }

  public Collection<ItemInstance> load(Collection<Integer> objectIds)
  {
    Collection list = Collections.emptyList();

    if (objectIds.isEmpty()) {
      return list;
    }
    list = new ArrayList(objectIds.size());

    for (Integer objectId : objectIds)
    {
      ItemInstance item = load(objectId);
      if (item != null) {
        list.add(item);
      }
    }
    return list;
  }

  public void save(ItemInstance item)
  {
    if (!item.getJdbcState().isSavable()) {
      return;
    }
    try
    {
      save0(item);
      item.setJdbcState(JdbcEntityState.STORED);
    }
    catch (SQLException e)
    {
      _log.error("Error while saving item : " + item, e);
      return;
    }

    cache.put(new Element(Integer.valueOf(item.getObjectId()), item));
  }

  public void save(Collection<ItemInstance> items)
  {
    if (items.isEmpty()) {
      return;
    }
    for (ItemInstance item : items)
      save(item);
  }

  public void update(ItemInstance item)
  {
    if (!item.getJdbcState().isUpdatable()) {
      return;
    }
    try
    {
      update0(item);
      item.setJdbcState(JdbcEntityState.STORED);
    }
    catch (SQLException e)
    {
      _log.error("Error while updating item : " + item, e);
      return;
    }

    cache.putIfAbsent(new Element(Integer.valueOf(item.getObjectId()), item));
  }

  public void update(Collection<ItemInstance> items)
  {
    if (items.isEmpty()) {
      return;
    }
    for (ItemInstance item : items)
      update(item);
  }

  public void saveOrUpdate(ItemInstance item)
  {
    if (item.getJdbcState().isSavable())
      save(item);
    else if (item.getJdbcState().isUpdatable())
      update(item);
  }

  public void saveOrUpdate(Collection<ItemInstance> items)
  {
    if (items.isEmpty()) {
      return;
    }
    for (ItemInstance item : items)
      saveOrUpdate(item);
  }

  public void delete(ItemInstance item)
  {
    if (!item.getJdbcState().isDeletable()) {
      return;
    }
    try
    {
      delete0(item);
      item.setJdbcState(JdbcEntityState.DELETED);
    }
    catch (SQLException e)
    {
      _log.error("Error while deleting item : " + item, e);
      return;
    }

    cache.remove(Integer.valueOf(item.getObjectId()));
  }

  public void delete(Collection<ItemInstance> items)
  {
    if (items.isEmpty()) {
      return;
    }
    for (ItemInstance item : items)
      delete(item);
  }

  public Collection<ItemInstance> getItemsByOwnerIdAndLoc(int ownerId, ItemInstance.ItemLocation loc)
  {
    Collection objectIds = Collections.emptyList();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id = ? AND loc = ?");
      statement.setInt(1, ownerId);
      statement.setString(2, loc.name());
      rset = statement.executeQuery();
      objectIds = new ArrayList();
      while (rset.next())
        objectIds.add(Integer.valueOf(rset.getInt(1)));
    }
    catch (SQLException e)
    {
      _log.error("Error while restore items of owner : " + ownerId, e);
      objectIds.clear();
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    return load(objectIds);
  }
}