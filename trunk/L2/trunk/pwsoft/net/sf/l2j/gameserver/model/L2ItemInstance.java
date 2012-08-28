package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.model.actor.knownlist.NullKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class L2ItemInstance extends L2Object
{
  private static final Logger _log;
  private static final Logger _logItems;
  private int _ownerId;
  private int _count;
  private int _initCount;
  private int _time;
  private boolean _decrease = false;
  private final int _itemId;
  private final L2Item _item;
  private ItemLocation _loc;
  private int _locData;
  private int _enchantLevel;
  private int _priceSell;
  private int _priceBuy;
  private boolean _wear;
  private L2Augmentation _augmentation = null;

  private int _mana = -1;
  private boolean _consumingMana = false;
  private static final int MANA_CONSUMPTION_RATE = 60000;
  private int _type1;
  private int _type2;
  private long _dropTime;
  public static final int CHARGED_NONE = 0;
  public static final int CHARGED_SOULSHOT = 1;
  public static final int CHARGED_SPIRITSHOT = 1;
  public static final int CHARGED_BLESSED_SOULSHOT = 2;
  public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
  private int _chargedSoulshot = 0;

  private int _chargedSpiritshot = 0;
  private boolean _chargedFishtshot = false;
  private boolean _protected;
  public static final int UNCHANGED = 0;
  public static final int ADDED = 1;
  public static final int REMOVED = 3;
  public static final int MODIFIED = 2;
  private int _lastChange = 2;
  private boolean _existsInDb;
  private boolean _storedInDb;
  private boolean _whFlag = false;
  private ScheduledFuture<?> itemLootShedule = null;
  private Future<?> _lazyUpdateInDb;
  private L2PcInstance _pickUpPriv = null;
  private long _pickUpTime = 0L;

  private long _expire = 0L;
  private long _life = 0L;

  public L2ItemInstance(int objectId, int itemId)
  {
    super(objectId);
    super.setKnownList(new NullKnownList(this));
    _itemId = itemId;
    _item = ItemTable.getInstance().getTemplate(itemId);
    if ((_itemId == 0) || (_item == null)) {
      throw new IllegalArgumentException();
    }
    _count = 1;
    _loc = ItemLocation.VOID;
    _type1 = 0;
    _type2 = 0;
    _dropTime = 0L;
    _life = _item.getExpire();
    _mana = (_life > 0L ? -1 : _item.getDuration());
  }

  public L2ItemInstance(int objectId, L2Item item)
  {
    super(objectId);
    super.setKnownList(new NullKnownList(this));
    if (item == null) {
      throw new IllegalArgumentException();
    }
    _itemId = item.getItemId();
    _item = item;
    if ((_itemId == 0) || (_item == null)) {
      throw new IllegalArgumentException();
    }
    _count = 1;
    _loc = ItemLocation.VOID;

    _life = _item.getExpire();
    _mana = (_life > 0L ? -1 : _item.getDuration());
  }

  public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference)
  {
    setOwnerId(owner_id);
  }

  public void setOwnerId(int owner_id)
  {
    if (owner_id == _ownerId) {
      return;
    }

    _ownerId = owner_id;
    _storedInDb = false;
  }

  public int getOwnerId()
  {
    return _ownerId;
  }

  public void setLocation(ItemLocation loc)
  {
    setLocation(loc, 0);
  }

  public void setLocation(ItemLocation loc, int loc_data)
  {
    if ((loc == _loc) && (loc_data == _locData)) {
      return;
    }
    _loc = loc;
    _locData = loc_data;
    _storedInDb = false;
  }

  public ItemLocation getLocation() {
    return _loc;
  }

  public int getCount()
  {
    return _count;
  }

  public void changeCount(String process, int count, L2PcInstance creator, L2Object reference)
  {
    if (count == 0) {
      return;
    }

    if ((count > 0) && (_count > 2147483647 - count))
      _count = 2147483647;
    else {
      _count += count;
    }

    if (_count < 0) {
      _count = 0;
    }

    _storedInDb = false;
  }

  public void changeCountWithoutTrace(String process, int count, L2PcInstance creator, L2Object reference)
  {
    if (count == 0) {
      return;
    }
    if ((count > 0) && (_count > 2147483647 - count))
      _count = 2147483647;
    else {
      _count += count;
    }
    if (_count < 0) {
      _count = 0;
    }
    _storedInDb = false;
  }

  public void setCount(int count)
  {
    if (_count == count) {
      return;
    }

    _count = (count >= -1 ? count : 0);
    _storedInDb = false;
  }

  public boolean isEquipable()
  {
    return (_item.getBodyPart() != 0) && (!(_item instanceof L2EtcItem));
  }

  public boolean isEquipped()
  {
    return (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP);
  }

  public int getLocationSlot()
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (_loc != ItemLocation.PAPERDOLL) && (_loc != ItemLocation.PET_EQUIP) && (_loc != ItemLocation.FREIGHT) && (_loc != ItemLocation.INVENTORY)) throw new AssertionError();

    return _locData;
  }

  public int getEquipSlot()
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (_loc != ItemLocation.PAPERDOLL) && (_loc != ItemLocation.PET_EQUIP) && (_loc != ItemLocation.FREIGHT)) throw new AssertionError();

    return _locData;
  }

  public L2Item getItem()
  {
    return _item;
  }

  public int getCustomType1() {
    return _type1;
  }

  public int getCustomType2() {
    return _type2;
  }

  public void setCustomType1(int newtype) {
    _type1 = newtype;
  }

  public void setCustomType2(int newtype) {
    _type2 = newtype;
  }

  public void setDropTime(long time) {
    _dropTime = time;
  }

  public long getDropTime() {
    return _dropTime;
  }

  public boolean isWear() {
    return _wear;
  }

  public void setWear(boolean newwear) {
    _wear = newwear;
  }

  public Enum getItemType()
  {
    return _item.getItemType();
  }

  public int getItemId()
  {
    return _itemId;
  }

  public final int getCrystalCount()
  {
    return _item.getCrystalCount(_enchantLevel);
  }

  public int getReferencePrice()
  {
    return _item.getReferencePrice();
  }

  public String getItemName()
  {
    return _item.getName();
  }

  public int getPriceToSell()
  {
    return isConsumable() ? (int)(_priceSell * Config.RATE_CONSUMABLE_COST) : _priceSell;
  }

  public void setPriceToSell(int price)
  {
    _priceSell = price;
    _storedInDb = false;
  }

  public int getPriceToBuy()
  {
    return isConsumable() ? (int)(_priceBuy * Config.RATE_CONSUMABLE_COST) : _priceBuy;
  }

  public void setPriceToBuy(int price)
  {
    _priceBuy = price;
    _storedInDb = false;
  }

  public int getLastChange()
  {
    return _lastChange;
  }

  public void setLastChange(int lastChange)
  {
    _lastChange = lastChange;
  }

  public boolean isStackable()
  {
    return _item.isStackable();
  }

  public boolean isDropable()
  {
    return isShadowItem() ? false : isAugmented() ? false : _item.isDropable();
  }

  public boolean isDestroyable()
  {
    return _item.isDestroyable();
  }

  public boolean isTradeable()
  {
    return isShadowItem() ? false : isAugmented() ? false : isEnchLimited() ? false : _item.isTradeable();
  }

  public boolean isEnchLimited() {
    return (Config.MAX_TRADE_ENCHANT > 0) && (_enchantLevel >= Config.MAX_TRADE_ENCHANT);
  }

  public boolean isConsumable()
  {
    return _item.isConsumable();
  }

  public boolean isBlessedEnchantScroll()
  {
    switch (_itemId) {
    case 6569:
    case 6570:
    case 6571:
    case 6572:
    case 6573:
    case 6574:
    case 6575:
    case 6576:
    case 6577:
    case 6578:
      return true;
    }
    return false;
  }

  public boolean isCrystallEnchantScroll() {
    switch (_itemId) {
    case 731:
    case 732:
    case 949:
    case 950:
    case 953:
    case 954:
    case 957:
    case 958:
    case 961:
    case 962:
      return true;
    }
    return false;
  }

  public short getEnchantCrystalId(L2ItemInstance scroll) {
    int scrollItemId = scroll.getItemId();
    short crystalId = 0;
    switch (_item.getCrystalType()) {
    case 4:
      crystalId = 1461;
      break;
    case 3:
      crystalId = 1460;
      break;
    case 2:
      crystalId = 1459;
      break;
    case 1:
      crystalId = 1458;
      break;
    case 5:
      crystalId = 1462;
    }

    for (short scrollId : getEnchantScrollId()) {
      if (scrollItemId == scrollId) {
        return crystalId;
      }
    }

    return 0;
  }

  public short[] getEnchantScrollId() {
    if (_item.getType2() == 0)
      switch (_item.getCrystalType()) {
      case 4:
        return new short[] { 729, 6569, 731 };
      case 3:
        return new short[] { 947, 6571, 949 };
      case 2:
        return new short[] { 951, 6573, 953 };
      case 1:
        return new short[] { 955, 6575, 957 };
      case 5:
        return new short[] { 959, 6577, 961 };
      }
    else if ((_item.getType2() == 1) || (_item.getType2() == 2)) {
      switch (_item.getCrystalType()) {
      case 4:
        return new short[] { 730, 6570, 732 };
      case 3:
        return new short[] { 948, 6572, 950 };
      case 2:
        return new short[] { 952, 6574, 954 };
      case 1:
        return new short[] { 956, 6576, 958 };
      case 5:
        return new short[] { 960, 6578, 962 };
      }
    }
    return new short[] { 0, 0, 0 };
  }

  public boolean isMagicWeapon() {
    switch (_itemId) {
    case 148:
    case 150:
    case 151:
    case 5638:
    case 5639:
    case 5640:
    case 5641:
    case 5642:
    case 5643:
    case 6366:
    case 6579:
    case 6587:
    case 6588:
    case 6589:
    case 6608:
    case 6609:
    case 6610:
    case 7722:
    case 7723:
    case 7724:
      return true;
    }
    return Config.ALT_MAGIC_WEAPONS.contains(Integer.valueOf(_itemId));
  }

  public boolean isWeddingRing()
  {
    int myid = _itemId;
    return (myid == 50001) || (myid == 50003);
  }

  public boolean isMagicShot()
  {
    switch (_itemId) {
    case 2509:
    case 2510:
    case 2511:
    case 2512:
    case 2513:
    case 2514:
    case 3947:
    case 3948:
    case 3949:
    case 3950:
    case 3951:
    case 3952:
    case 5790:
      return true;
    }
    return false;
  }

  public boolean isFighterShot() {
    switch (_itemId) {
    case 1463:
    case 1464:
    case 1465:
    case 1466:
    case 1467:
    case 1835:
    case 5789:
      return true;
    }
    return false;
  }

  public boolean isAvailable(L2PcInstance player, boolean allowAdena)
  {
    return (!isEquipped()) && (getItem().getType2() != 3) && ((getItem().getType2() != 4) || (getItem().getType1() != 1)) && ((player.getPet() == null) || (getObjectId() != player.getPet().getControlItemId())) && (player.getActiveEnchantItem() != this) && ((allowAdena) || (getItemId() != 57)) && ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != getItemId())) && (isTradeable());
  }

  public boolean isHeroItem()
  {
    return ((_itemId >= 6611) && (_itemId <= 6621)) || (_itemId == 6842);
  }

  public boolean notForOly() {
    if (_enchantLevel > getItem().maxOlyEnch()) {
      return true;
    }

    return getItem().notForOly();
  }

  public void setPickuper(L2PcInstance pickuper)
  {
    _pickUpPriv = pickuper;
    _pickUpTime = (System.currentTimeMillis() + Config.PICKUP_PENALTY);
  }

  public void onAction(L2PcInstance player)
  {
    if (getLocation() != ItemLocation.VOID)
    {
      return;
    }

    if (player.isAlikeDead()) {
      player.sendPacket(Static.CANT_LOOT_DEAD);
      return;
    }

    boolean canPickup = true;
    if ((_pickUpPriv != null) && (_pickUpTime > System.currentTimeMillis())) {
      if (!player.equals(_pickUpPriv)) {
        canPickup = false;
      }

      if (player.getParty() != null) {
        if (!player.getParty().getPartyMembers().contains(_pickUpPriv)) {
          canPickup = false;
        }

        if ((_pickUpPriv.getParty() != null) && (_pickUpPriv.getParty().isInCommandChannel()) && (!_pickUpPriv.getParty().getCommandChannel().getPartys().contains(player.getParty()))) {
          canPickup = false;
        }
      }
    }
    if (!canPickup) {
      player.sendActionFailed();
      player.sendPacket(SystemMessage.id(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(getItemId()));
      return;
    }

    if (((_itemId == 8689) || (_itemId == 8190)) && ((player.isMounted()) || (player.isInOlympiadMode()) || (player.getOlympiadGameId() > -1))) {
      player.sendActionFailed();
      return;
    }

    if (((_itemId >= 3960) && (_itemId <= 4021) && (player.isInParty())) || ((_itemId >= 3960) && (_itemId <= 3969) && (!player.isCastleLord(1))) || ((_itemId >= 3973) && (_itemId <= 3982) && (!player.isCastleLord(2))) || ((_itemId >= 3986) && (_itemId <= 3995) && (!player.isCastleLord(3))) || ((_itemId >= 3999) && (_itemId <= 4008) && (!player.isCastleLord(4))) || ((_itemId >= 4012) && (_itemId <= 4021) && (!player.isCastleLord(5))) || ((_itemId >= 5205) && (_itemId <= 5214) && (!player.isCastleLord(6))) || ((_itemId >= 6779) && (_itemId <= 6788) && (!player.isCastleLord(7))) || ((_itemId >= 7973) && (_itemId <= 7982) && (!player.isCastleLord(8))) || ((_itemId >= 7918) && (_itemId <= 7927) && (!player.isCastleLord(9))))
    {
      if (player.isInParty())
      {
        player.sendMessage("You cannot pickup mercenaries while in a party.");
      }
      else player.sendMessage("Only the castle lord can pickup mercenaries.");

      player.setTarget(this);
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

      player.sendActionFailed();
    } else {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
    }
  }

  public int getEnchantLevel()
  {
    return Math.min(_enchantLevel, getMaxEnchant());
  }

  public int getMaxEnchant() {
    return getItem().getMaxEnchant();
  }

  public void setEnchantLevel(int enchantLevel, boolean game)
  {
    _enchantLevel = enchantLevel;
    _storedInDb = false;
  }

  public void setEnchantLevel(int enchantLevel) {
    if ((_enchantLevel == enchantLevel) || (enchantLevel > getMaxEnchant())) {
      return;
    }
    setEnchantLevel(enchantLevel, true);
  }

  public int getPDef()
  {
    if ((_item instanceof L2Armor)) {
      return ((L2Armor)_item).getPDef();
    }
    return 0;
  }

  public boolean isAugmented()
  {
    return _augmentation != null;
  }

  public L2Augmentation getAugmentation()
  {
    return _augmentation;
  }

  public boolean setAugmentation(L2Augmentation augmentation)
  {
    if (_augmentation != null) {
      return false;
    }
    _augmentation = augmentation;
    return true;
  }

  public void removeAugmentation()
  {
    if (_augmentation == null) {
      return;
    }
    _augmentation.deleteAugmentationData();
    _augmentation = null;
  }

  public boolean isShadowItem()
  {
    return _mana >= 0;
  }

  public void setMana(int mana)
  {
    _mana = mana;
  }

  public int getMana()
  {
    return _mana;
  }

  public void decreaseMana(boolean resetConsumingMana)
  {
    if (!isShadowItem()) {
      return;
    }

    if (getLocation() != ItemLocation.PAPERDOLL) {
      return;
    }

    if (_mana > 0) {
      _mana -= 1;
    }

    if (_storedInDb) {
      _storedInDb = false;
    }
    if (resetConsumingMana) {
      _consumingMana = false;
    }

    L2PcInstance player = L2World.getInstance().getPlayer(getOwnerId());
    if (player != null)
    {
      switch (_mana)
      {
      case 1:
      case 3:
      case 5:
      case 8:
      case 10:
      case 20:
      case 30:
      case 40:
      case 50:
      case 60:
        player.sendCritMessage(getItemName() + ": " + _mana + " \u043C\u0438\u043D\u0443\u0442 \u0434\u043E \u0438\u0441\u0447\u0435\u0437\u043D\u043E\u0432\u0435\u043D\u0438\u044F.");
      }

      if (_mana == 0)
      {
        player.sendCritMessage(getItemName() + " \u0441\u043B\u043E\u043C\u0430\u043B\u0430\u0441\u044C");

        if (isEquipped()) {
          player.getInventory().unEquipItemInSlot(getEquipSlot());
        }

        if (getLocation() != ItemLocation.WAREHOUSE)
        {
          player.getInventory().destroyItem("Shadow", this, player, null);

          player.sendItems(false);
        } else {
          player.getWarehouse().destroyItem("Shadow", this, player, null);
        }

        player.sendChanges();
        player.broadcastUserInfo();

        L2World.getInstance().removeObject(this);
      }
      else {
        if ((!_consumingMana) && (isEquipped())) {
          scheduleConsumeManaTask();
        }
        if (getLocation() != ItemLocation.WAREHOUSE) {
          InventoryUpdate iu = new InventoryUpdate();
          iu.addModifiedItem(this);
          player.sendPacket(iu);
        }
      }
    }
  }

  private void scheduleConsumeManaTask() {
    _consumingMana = true;
    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), 60000L);
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  public int getChargedSoulshot()
  {
    return _chargedSoulshot;
  }

  public int getChargedSpiritshot()
  {
    return _chargedSpiritshot;
  }

  public boolean getChargedFishshot() {
    return _chargedFishtshot;
  }

  public void setChargedSoulshot(int type)
  {
    _chargedSoulshot = type;
  }

  public void setChargedSpiritshot(int type)
  {
    _chargedSpiritshot = type;
  }

  public void setChargedFishshot(boolean type) {
    _chargedFishtshot = type;
  }

  public Func[] getStatFuncs(L2Character player)
  {
    return getItem().getStatFuncs(this, player);
  }

  public void updateDatabase()
  {
    updateDatabase(false);
  }

  public synchronized void updateDatabase(boolean commit) {
    if (isWear())
    {
      return;
    }

    if (_existsInDb) {
      if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || ((_count == 0) && (_loc != ItemLocation.LEASE))) {
        removeFromDb();
      } else if (isStackable()) {
        if (commit)
        {
          if (stopLazyUpdateTask(true)) {
            insertIntoDb();
            return;
          }
          updateInDb();
          return;
        }
        Future lazyUpdateInDb = _lazyUpdateInDb;
        if ((lazyUpdateInDb == null) || (lazyUpdateInDb.isDone()))
          _lazyUpdateInDb = ThreadPoolManager.getInstance().scheduleGeneral(new LazyUpdateInDb(this), 60000L);
      }
      else {
        updateInDb();
      }
    } else {
      if ((_count == 0) && (_loc != ItemLocation.LEASE)) {
        return;
      }
      if ((_loc == ItemLocation.VOID) || (_ownerId == 0)) {
        return;
      }
      insertIntoDb();
    }
  }

  public boolean stopLazyUpdateTask(boolean interrupt) {
    boolean ret = false;
    if (_lazyUpdateInDb != null) {
      ret = _lazyUpdateInDb.cancel(interrupt);
      _lazyUpdateInDb = null;
    }
    return ret;
  }

  public static L2ItemInstance restoreFromDb(int objectId)
  {
    L2ItemInstance inst = null;
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, aug_id, aug_skill, aug_lvl, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left, end_time FROM items WHERE object_id = ?");
      statement.setInt(1, objectId);
      rs = statement.executeQuery();
      int owner_id;
      if (rs.next()) {
        owner_id = rs.getInt("owner_id");
        int item_id = rs.getInt("item_id");
        int count = rs.getInt("count");
        ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
        int loc_data = rs.getInt("loc_data");
        int enchant_level = rs.getInt("enchant_level");
        int aug_id = rs.getInt("aug_id");
        int aug_skill = rs.getInt("aug_skill");
        int aug_lvl = rs.getInt("aug_lvl");
        int custom_type1 = rs.getInt("custom_type1");
        int custom_type2 = rs.getInt("custom_type2");
        int price_sell = rs.getInt("price_sell");
        int price_buy = rs.getInt("price_buy");
        int manaLeft = rs.getInt("mana_left");
        long endTime = rs.getLong("end_time");
        L2Item item = ItemTable.getInstance().getTemplate(item_id);
        Object localObject1;
        if (item == null) {
          _log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
          localObject1 = null;
          return localObject1;
        }
        inst = new L2ItemInstance(objectId, item);
        inst._existsInDb = true;
        inst._storedInDb = true;
        inst._ownerId = owner_id;
        inst._count = count;
        inst._enchantLevel = enchant_level;

        if (aug_id > 0) {
          inst._augmentation = new L2Augmentation(inst, aug_id, aug_skill, aug_lvl, false);
        }

        inst._type1 = custom_type1;
        inst._type2 = custom_type2;
        inst._loc = loc;
        inst._locData = loc_data;
        inst._priceSell = price_sell;
        inst._priceBuy = price_buy;

        inst._expire = endTime;
        if (inst._expire > 0L) {
          if (System.currentTimeMillis() - inst._expire >= 0L) {
            inst.removeFromDb();
            localObject1 = null;
            return localObject1;
          }
          inst.scheduleExpireTask(System.currentTimeMillis() - inst._expire);
        }

        inst._mana = manaLeft;

        if ((inst._mana > 0) && (inst.getLocation() == ItemLocation.PAPERDOLL)) {
          inst.decreaseMana(false);
        }

        if (inst._mana == 0) {
          inst.removeFromDb();
          localObject1 = null;
          return localObject1;
        }
        if ((inst._mana > 0) && (inst.getLocation() == ItemLocation.PAPERDOLL))
          inst.scheduleConsumeManaTask();
      }
      else {
        _log.severe("Item object_id=" + objectId + " not found");
        owner_id = null;
        return owner_id;
      }
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Could not restore item " + objectId + " from DB:", e);
    } finally {
      Close.CSR(con, statement, rs);
    }
    return inst;
  }

  public final void dropMe(L2Character dropper, int x, int y, int z)
  {
    synchronized (this)
    {
      setIsVisible(true);
      getPosition().setWorldPosition(x, y, z);
      getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));

      getPosition().getWorldRegion().addVisibleObject(this);
    }
    setDropTime(System.currentTimeMillis());

    L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);
    if (Config.SAVE_DROPPED_ITEM)
      ItemsOnGroundManager.getInstance().save(this);
  }

  private void updateInDb()
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (!_existsInDb)) throw new AssertionError();

    if (_wear) {
      return;
    }
    if (_storedInDb) {
      return;
    }

    if (getCount() <= 0) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=?,mana_left=?,end_time=? WHERE object_id = ?");
      statement.setInt(1, _ownerId);
      statement.setInt(2, getCount());
      statement.setString(3, _loc.name());
      statement.setInt(4, _locData);
      statement.setInt(5, getEnchantLevel());
      statement.setInt(6, _priceSell);
      statement.setInt(7, _priceBuy);
      statement.setInt(8, getCustomType1());
      statement.setInt(9, getCustomType2());
      statement.setInt(10, getMana());
      statement.setLong(11, getExpire());
      statement.setInt(12, getObjectId());
      statement.executeUpdate();
      _existsInDb = true;
      _storedInDb = true;
    } catch (SQLException e) {
      _log.log(Level.SEVERE, "Could not update item " + getObjectId() + " in DB: Reason: Duplicate itemId");
    } finally {
      Close.CS(con, statement);
    }
  }

  public void updateAdena(int newCount) {
    if (_storedInDb) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("UPDATE `items` SET `count`=? WHERE `object_id`=?");
      statement.setInt(1, newCount);
      statement.setInt(2, getObjectId());
      statement.executeUpdate();
      _existsInDb = true;
      _storedInDb = true;
    } catch (SQLException e) {
      _log.log(Level.SEVERE, "Could not update adena in DB");
    } finally {
      Close.CS(con, statement);
    }
  }

  private void insertIntoDb()
  {
    if (_wear) {
      return;
    }
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (
      (_existsInDb) || (getObjectId() == 0))) throw new AssertionError();

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,aug_id,aug_skill,aug_lvl,price_sell,price_buy,object_id,custom_type1,custom_type2,mana_left,end_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      statement.setInt(1, _ownerId);
      statement.setInt(2, _itemId);
      statement.setInt(3, getCount());
      statement.setString(4, _loc.name());
      statement.setInt(5, _locData);
      statement.setInt(6, getEnchantLevel());
      if (isAugmented()) {
        statement.setInt(7, getAugmentation().getAugmentationId());
        if (getAugmentation().getAugmentSkill() != null) {
          statement.setInt(8, getAugmentation().getAugmentSkill().getId());
          statement.setInt(9, getAugmentation().getAugmentSkill().getLevel());
        } else {
          statement.setInt(8, -1);
          statement.setInt(9, -1);
        }
      }
      else {
        statement.setInt(7, -1);
        statement.setInt(8, -1);
        statement.setInt(9, -1);
      }
      statement.setInt(10, _priceSell);
      statement.setInt(11, _priceBuy);
      statement.setInt(12, getObjectId());
      statement.setInt(13, _type1);
      statement.setInt(14, _type2);
      statement.setInt(15, getMana());

      if (_life > 0L) {
        long expire = System.currentTimeMillis() + _life;
        statement.setLong(16, expire);
        setExpire(expire);
        scheduleExpireTask(_life);
      } else {
        statement.setLong(16, 0L);
      }

      statement.executeUpdate();

      _existsInDb = true;
      _storedInDb = true;
    } catch (SQLException e) {
      _log.log(Level.SEVERE, "Could not insert item " + getObjectId() + " into DB: Reason: " + e);
    } finally {
      Close.CS(con, statement);
    }
  }

  private void removeFromDb()
  {
    if (_wear) {
      return;
    }
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (!_existsInDb)) throw new AssertionError();

    stopLazyUpdateTask(true);

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM items WHERE object_id=?");
      statement.setInt(1, getObjectId());
      statement.executeUpdate();

      _existsInDb = false;
      _storedInDb = false;
    } catch (Exception e) {
      _log.log(Level.SEVERE, "Could not delete item " + getObjectId() + " in DB:", e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public String toString()
  {
    return "" + _item;
  }

  public void resetOwnerTimer() {
    if (itemLootShedule != null) {
      itemLootShedule.cancel(true);
    }
    itemLootShedule = null;
  }

  public void setItemLootShedule(ScheduledFuture<?> sf) {
    itemLootShedule = sf;
  }

  public ScheduledFuture<?> getItemLootShedule() {
    return itemLootShedule;
  }

  public void setProtected(boolean is_protected) {
    _protected = is_protected;
  }

  public boolean isProtected() {
    return _protected;
  }

  public boolean isNightLure() {
    return ((_itemId >= 8505) && (_itemId <= 8513)) || (_itemId == 8485);
  }

  public void setCountDecrease(boolean decrease) {
    _decrease = decrease;
  }

  public boolean getCountDecrease() {
    return _decrease;
  }

  public void setInitCount(int InitCount) {
    _initCount = InitCount;
  }

  public int getInitCount() {
    return _initCount;
  }

  public void restoreInitCount() {
    if (_decrease)
      _count = _initCount;
  }

  public void setTime(int time)
  {
    if (time > 0)
      _time = time;
    else
      _time = 0;
  }

  public int getTime()
  {
    return _time;
  }

  private boolean isLoggable(String method)
  {
    return (method.equals("Trade")) || (method.equals("WH finish")) || (method.equals("DieDrop")) || (method.equals("Pickup")) || (method.equals("depositwh")) || (method.equals("PrivateStore"));
  }

  public boolean canBeEnchanted()
  {
    if ((isHeroItem()) && (!Config.ENCHANT_HERO_WEAPONS)) {
      return false;
    }

    if (getItemType() == L2WeaponType.ROD) {
      return false;
    }

    if ((_itemId == 8190) || (_itemId == 8689)) {
      return false;
    }

    return !isShadowItem();
  }

  public void deleteMe()
  {
    removeFromDb();
    decayMe();
    L2World.getInstance().removeObject(this);
  }

  public void removeFromWorld() {
    L2World.getInstance().removeObject(this);
  }

  public void setWhFlag(boolean flag) {
    _whFlag = flag;
  }

  public boolean isLure()
  {
    switch (_itemId) {
    case 6519:
    case 6520:
    case 6521:
    case 6522:
    case 6523:
    case 6524:
    case 6525:
    case 6526:
    case 6527:
    case 7610:
    case 7611:
    case 7612:
    case 7613:
    case 7807:
    case 7808:
    case 7809:
    case 8484:
    case 8485:
    case 8486:
    case 8505:
    case 8506:
    case 8507:
    case 8508:
    case 8509:
    case 8510:
    case 8511:
    case 8512:
    case 8513:
      return true;
    }
    return false;
  }

  public boolean canBeStored(L2PcInstance player, boolean privatewh)
  {
    if (isEquipped()) {
      return false;
    }

    if ((!privatewh) && (isShadowItem())) {
      return false;
    }

    if ((!privatewh) && (isAugmented())) {
      return false;
    }

    if (getItem().getType2() == 3) {
      return false;
    }

    if (isHeroItem()) {
      return false;
    }

    if ((player.getPet() != null) && (getObjectId() == player.getPet().getControlItemId())) {
      return false;
    }

    if ((_itemId == 8190) || (_itemId == 8689)) {
      return false;
    }

    if (isWear()) {
      return false;
    }

    if (player.getActiveEnchantItem() == this) {
      return false;
    }

    if ((player.getCurrentSkill() != null) && (player.getCurrentSkill().getSkill().getItemConsumeId() == getItemId())) {
      return false;
    }

    return (privatewh) || (isTradeable());
  }

  public void setExpire(long time)
  {
    _expire = time;
  }

  public long getExpire() {
    return _expire;
  }

  private void scheduleExpireTask(long time) {
    ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleExpireTask(this), time);
  }

  public boolean canBeAugmented()
  {
    if ((getItem().getItemGrade() < 2) || (getItem().getType2() != 0)) {
      return false;
    }

    if (isShadowItem()) {
      return false;
    }

    if ((isHeroItem()) && (Config.ALT_AUGMENT_HERO)) {
      return true;
    }

    return isDestroyable();
  }

  public boolean isL2Item()
  {
    return true;
  }

  public boolean notForBossZone() {
    if (getItem().isNotForBossZone()) {
      return true;
    }
    if (Config.BOSS_ZONE_MAX_ENCH == 0) {
      return false;
    }

    return _enchantLevel > Config.BOSS_ZONE_MAX_ENCH;
  }

  static
  {
    _log = Logger.getLogger(L2ItemInstance.class.getName());
    _logItems = Logger.getLogger("item");
  }

  public static class ScheduleExpireTask
    implements Runnable
  {
    private L2ItemInstance item;

    public ScheduleExpireTask(L2ItemInstance item)
    {
      this.item = item;
    }

    public void run() {
      try {
        if (item._storedInDb) {
          L2ItemInstance.access$102(item, false);
        }

        L2PcInstance player = L2World.getInstance().getPlayer(item.getOwnerId());
        if (player == null) {
          return;
        }

        player.sendCritMessage(item.getItemName() + " \u0438\u0441\u0442\u0435\u043A\u043B\u043E.");

        if (item.isEquipped()) {
          player.getInventory().unEquipItemInSlot(item.getEquipSlot());
        }

        if (item.getLocation() != L2ItemInstance.ItemLocation.WAREHOUSE) {
          player.getInventory().destroyItem("Expire", item, player, null);
          player.sendItems(false);
        } else {
          player.getWarehouse().destroyItem("Expire", item, player, null);
        }

        player.sendChanges();
        player.broadcastUserInfo();
        L2World.getInstance().removeObject(item);
      }
      catch (Throwable t)
      {
      }
    }
  }

  public static class ScheduleConsumeManaTask
    implements Runnable
  {
    private L2ItemInstance _shadowItem;

    public ScheduleConsumeManaTask(L2ItemInstance item)
    {
      _shadowItem = item;
    }

    public void run()
    {
      try {
        if (_shadowItem != null)
          _shadowItem.decreaseMana(true);
      }
      catch (Throwable t)
      {
      }
    }
  }

  private class LazyUpdateInDb
    implements Runnable
  {
    private final int itemStoreId;

    public LazyUpdateInDb(L2ItemInstance item)
    {
      itemStoreId = item.getObjectId();
    }

    public void run() {
      L2ItemInstance item = L2World.getInstance().getItem(itemStoreId);
      if (item == null)
        return;
      try
      {
        item.updateInDb();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        item.stopLazyUpdateTask(false);
      }
    }
  }

  public static enum ItemLocation
  {
    VOID, 
    INVENTORY, 
    PAPERDOLL, 
    WAREHOUSE, 
    CLANWH, 
    PET, 
    PET_EQUIP, 
    LEASE, 
    FREIGHT;
  }
}