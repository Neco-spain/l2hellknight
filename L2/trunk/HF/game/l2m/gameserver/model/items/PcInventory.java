package l2m.gameserver.model.items;

import java.util.Collection;
import java.util.List;
import l2p.commons.collections.CollectionUtils;
import l2p.commons.dao.JdbcEntityState;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.data.dao.ItemsDAO;
import l2m.gameserver.instancemanager.CursedWeaponsManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.items.listeners.AccessoryListener;
import l2m.gameserver.model.items.listeners.ArmorSetListener;
import l2m.gameserver.model.items.listeners.BowListener;
import l2m.gameserver.model.items.listeners.ItemAugmentationListener;
import l2m.gameserver.model.items.listeners.ItemEnchantOptionsListener;
import l2m.gameserver.model.items.listeners.ItemSkillsListener;
import l2m.gameserver.network.serverpackets.ExBR_AgathionEnergyInfo;
import l2m.gameserver.network.serverpackets.InventoryUpdate;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.taskmanager.DelayedItemsManager;
import l2m.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

public class PcInventory extends Inventory
{
  private final Player _owner;
  private LockType _lockType = LockType.NONE;
  private int[] _lockItems = ArrayUtils.EMPTY_INT_ARRAY;

  public boolean isRefresh = false;

  private static final int[][] arrows = { { 17 }, { 1341, 22067 }, { 1342, 22068 }, { 1343, 22069 }, { 1344, 22070 }, { 1345, 22071 } };

  private static final int[][] bolts = { { 9632 }, { 9633, 22144 }, { 9634, 22145 }, { 9635, 22146 }, { 9636, 22147 }, { 9637, 22148 } };

  public PcInventory(Player owner)
  {
    super(owner.getObjectId());
    _owner = owner;

    addListener(ItemSkillsListener.getInstance());
    addListener(ItemAugmentationListener.getInstance());
    addListener(ItemEnchantOptionsListener.getInstance());
    addListener(ArmorSetListener.getInstance());
    addListener(BowListener.getInstance());
    addListener(AccessoryListener.getInstance());
  }

  public Player getActor()
  {
    return _owner;
  }

  protected ItemInstance.ItemLocation getBaseLocation()
  {
    return ItemInstance.ItemLocation.INVENTORY;
  }

  protected ItemInstance.ItemLocation getEquipLocation()
  {
    return ItemInstance.ItemLocation.PAPERDOLL;
  }

  public long getAdena()
  {
    ItemInstance _adena = getItemByItemId(57);
    if (_adena == null)
      return 0L;
    return _adena.getCount();
  }

  public ItemInstance addAdena(long amount)
  {
    return addItem(57, amount);
  }

  public boolean reduceAdena(long adena)
  {
    return destroyItemByItemId(57, adena);
  }

  public int getPaperdollAugmentationId(int slot)
  {
    ItemInstance item = _paperdoll[slot];
    if ((item != null) && (item.isAugmented()))
      return item.getAugmentationId();
    return 0;
  }

  public int getPaperdollItemId(int slot)
  {
    Player player = getActor();

    int itemId = super.getPaperdollItemId(slot);

    if ((slot == 7) && (itemId == 0) && (player.isClanAirShipDriver())) {
      itemId = 13556;
    }
    return itemId;
  }

  protected void onRefreshWeight()
  {
    getActor().refreshOverloaded();
  }

  public void validateItems()
  {
    for (ItemInstance item : _paperdoll) {
      if ((item == null) || ((ItemFunctions.checkIfCanEquip(getActor(), item) == null) && (item.getTemplate().testCondition(getActor(), item))))
        continue;
      unEquipItem(item);
      getActor().sendDisarmMessage(item);
    }
  }

  public void validateItemsSkills()
  {
    for (ItemInstance item : _paperdoll)
    {
      if ((item == null) || (item.getTemplate().getType2() != 0)) {
        continue;
      }
      boolean needUnequipSkills = getActor().getWeaponsExpertisePenalty() > 0;

      if (item.getTemplate().getAttachedSkills().length > 0)
      {
        boolean has = getActor().getSkillLevel(Integer.valueOf(item.getTemplate().getAttachedSkills()[0].getId())) > 0;
        if ((needUnequipSkills) && (has))
          ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
        else if ((!needUnequipSkills) && (!has))
          ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
      }
      else if (item.getTemplate().getEnchant4Skill() != null)
      {
        boolean has = getActor().getSkillLevel(Integer.valueOf(item.getTemplate().getEnchant4Skill().getId())) > 0;
        if ((needUnequipSkills) && (has))
          ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
        else if ((!needUnequipSkills) && (!has))
          ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
      } else {
        if (item.getTemplate().getTriggerList().isEmpty())
          continue;
        if (needUnequipSkills)
          ItemSkillsListener.getInstance().onUnequip(item.getEquipSlot(), item, getActor());
        else
          ItemSkillsListener.getInstance().onEquip(item.getEquipSlot(), item, getActor());
      }
    }
  }

  public void refreshEquip()
  {
    isRefresh = true;
    for (ItemInstance item : getItems())
    {
      if (item.isEquipped())
      {
        int slot = item.getEquipSlot();
        _listeners.onUnequip(slot, item);
        _listeners.onEquip(slot, item);
      } else {
        if (item.getItemType() != EtcItemTemplate.EtcItemType.RUNE)
          continue;
        _listeners.onUnequip(-1, item);
        _listeners.onEquip(-1, item);
      }
    }
    isRefresh = false;
  }

  public void sort(int[][] order)
  {
    boolean needSort = false;
    for (int[] element : order)
    {
      ItemInstance item = getItemByObjectId(element[0]);
      if (item == null)
        continue;
      if (item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
        continue;
      if (item.getLocData() == element[1])
        continue;
      item.setLocData(element[1]);
      item.setJdbcState(JdbcEntityState.UPDATED);
      needSort = true;
    }
    if (needSort)
      CollectionUtils.eqSort(_items, Inventory.ItemOrderComparator.getInstance());
  }

  public ItemInstance findArrowForBow(ItemTemplate bow)
  {
    int[] arrowsId = arrows[bow.getCrystalType().externalOrdinal];
    ItemInstance ret = null;
    for (int id : arrowsId)
      if ((ret = getItemByItemId(id)) != null)
        return ret;
    return null;
  }

  public ItemInstance findArrowForCrossbow(ItemTemplate xbow)
  {
    int[] boltsId = bolts[xbow.getCrystalType().externalOrdinal];
    ItemInstance ret = null;
    for (int id : boltsId)
      if ((ret = getItemByItemId(id)) != null)
        return ret;
    return null;
  }

  public ItemInstance findEquippedLure()
  {
    ItemInstance res = null;
    int last_lure = 0;
    Player owner = getActor();
    String LastLure = owner.getVar("LastLure");
    if ((LastLure != null) && (!LastLure.isEmpty()))
      last_lure = Integer.valueOf(LastLure).intValue();
    for (ItemInstance temp : getItems())
      if (temp.getItemType() == EtcItemTemplate.EtcItemType.BAIT) {
        if ((temp.getLocation() == ItemInstance.ItemLocation.PAPERDOLL) && (temp.getEquipSlot() == 8))
          return temp;
        if ((last_lure > 0) && (res == null) && (temp.getObjectId() == last_lure))
          res = temp; 
      }
    return res;
  }

  public void lockItems(LockType lock, int[] items)
  {
    if (_lockType != LockType.NONE) {
      return;
    }
    _lockType = lock;
    _lockItems = items;

    getActor().sendItemList(false);
  }

  public void unlock()
  {
    if (_lockType == LockType.NONE) {
      return;
    }
    _lockType = LockType.NONE;
    _lockItems = ArrayUtils.EMPTY_INT_ARRAY;

    getActor().sendItemList(false);
  }

  public boolean isLockedItem(ItemInstance item)
  {
    switch (1.$SwitchMap$l2p$gameserver$model$items$LockType[_lockType.ordinal()])
    {
    case 1:
      return ArrayUtils.contains(_lockItems, item.getItemId());
    case 2:
      return !ArrayUtils.contains(_lockItems, item.getItemId());
    }
    return false;
  }

  public LockType getLockType()
  {
    return _lockType;
  }

  public int[] getLockItems()
  {
    return _lockItems;
  }

  protected void onRestoreItem(ItemInstance item)
  {
    super.onRestoreItem(item);

    if (item.getItemType() == EtcItemTemplate.EtcItemType.RUNE) {
      _listeners.onEquip(-1, item);
    }
    if (item.isTemporalItem()) {
      item.startTimer(new LifeTimeTask(item));
    }
    if (item.isCursed())
      CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
  }

  protected void onAddItem(ItemInstance item)
  {
    super.onAddItem(item);

    if (item.getItemType() == EtcItemTemplate.EtcItemType.RUNE)
    {
      _listeners.onEquip(-1, item);
    }

    if (item.isTemporalItem()) {
      item.startTimer(new LifeTimeTask(item));
    }
    if (item.isCursed())
      CursedWeaponsManager.getInstance().checkPlayer(getActor(), item);
  }

  protected void onRemoveItem(ItemInstance item)
  {
    super.onRemoveItem(item);

    getActor().removeItemFromShortCut(item.getObjectId());

    if (item.getItemType() == EtcItemTemplate.EtcItemType.RUNE) {
      _listeners.onUnequip(-1, item);
    }
    if (item.isTemporalItem())
      item.stopTimer();
  }

  protected void onEquip(int slot, ItemInstance item)
  {
    super.onEquip(slot, item);

    if (item.isShadowItem())
      item.startTimer(new ShadowLifeTimeTask(item));
  }

  protected void onUnequip(int slot, ItemInstance item)
  {
    super.onUnequip(slot, item);

    if (item.isShadowItem())
      item.stopTimer();
  }

  public void restore()
  {
    int ownerId = getOwnerId();

    writeLock();
    try
    {
      Collection items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getBaseLocation());

      for (ItemInstance item : items)
      {
        _items.add(item);
        onRestoreItem(item);
      }
      CollectionUtils.eqSort(_items, Inventory.ItemOrderComparator.getInstance());

      items = _itemsDAO.getItemsByOwnerIdAndLoc(ownerId, getEquipLocation());

      for (ItemInstance item : items)
      {
        _items.add(item);
        onRestoreItem(item);
        if (item.getEquipSlot() >= 26)
        {
          item.setLocation(getBaseLocation());
          item.setLocData(0);
          item.setEquipped(false);
          continue;
        }
        setPaperdollItem(item.getEquipSlot(), item);
      }
    }
    finally
    {
      writeUnlock();
    }

    DelayedItemsManager.getInstance().loadDelayed(getActor(), false);

    refreshWeight();
  }

  public void store()
  {
    writeLock();
    try
    {
      _itemsDAO.update(_items);
    }
    finally
    {
      writeUnlock();
    }
  }

  protected void sendAddItem(ItemInstance item)
  {
    Player actor = getActor();

    actor.sendPacket(new InventoryUpdate().addNewItem(item));
    if (item.getTemplate().getAgathionEnergy() > 0)
      actor.sendPacket(new ExBR_AgathionEnergyInfo(1, new ItemInstance[] { item }));
  }

  protected void sendModifyItem(ItemInstance item)
  {
    Player actor = getActor();

    actor.sendPacket(new InventoryUpdate().addModifiedItem(item));
    if (item.getTemplate().getAgathionEnergy() > 0)
      actor.sendPacket(new ExBR_AgathionEnergyInfo(1, new ItemInstance[] { item }));
  }

  protected void sendRemoveItem(ItemInstance item)
  {
    getActor().sendPacket(new InventoryUpdate().addRemovedItem(item));
  }

  public void startTimers()
  {
  }

  public void stopAllTimers()
  {
    for (ItemInstance item : getItems())
    {
      if ((item.isShadowItem()) || (item.isTemporalItem()))
        item.stopTimer();
    }
  }

  protected class LifeTimeTask extends RunnableImpl
  {
    private ItemInstance item;

    LifeTimeTask(ItemInstance item)
    {
      this.item = item;
    }

    public void runImpl()
      throws Exception
    {
      Player player = getActor();
      int left;
      synchronized (item)
      {
        left = item.getTemporalLifeTime();
        if (left <= 0) {
          destroyItem(item);
        }
      }
      if (left <= 0)
        player.sendPacket(new SystemMessage(2366).addItemName(item.getItemId()));
    }
  }

  protected class ShadowLifeTimeTask extends RunnableImpl
  {
    private ItemInstance item;

    ShadowLifeTimeTask(ItemInstance item)
    {
      this.item = item;
    }

    public void runImpl()
      throws Exception
    {
      Player player = getActor();

      if (!item.isEquipped())
        return;
      int mana;
      synchronized (item)
      {
        item.setLifeTime(item.getLifeTime() - 1);
        mana = item.getShadowLifeTime();
        if (mana <= 0) {
          destroyItem(item);
        }
      }
      SystemMessage sm = null;
      if (mana == 10)
        sm = new SystemMessage(1979);
      else if (mana == 5)
        sm = new SystemMessage(1980);
      else if (mana == 1)
        sm = new SystemMessage(1981);
      else if (mana <= 0)
        sm = new SystemMessage(1982);
      else {
        player.sendPacket(new InventoryUpdate().addModifiedItem(item));
      }
      if (sm != null)
      {
        sm.addItemName(item.getItemId());
        player.sendPacket(sm);
      }
    }
  }
}