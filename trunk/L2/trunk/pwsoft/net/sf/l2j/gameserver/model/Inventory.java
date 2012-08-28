package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public abstract class Inventory extends ItemContainer
{
  public static final int PAPERDOLL_UNDER = 0;
  public static final int PAPERDOLL_LEAR = 1;
  public static final int PAPERDOLL_REAR = 2;
  public static final int PAPERDOLL_NECK = 3;
  public static final int PAPERDOLL_LFINGER = 4;
  public static final int PAPERDOLL_RFINGER = 5;
  public static final int PAPERDOLL_HEAD = 6;
  public static final int PAPERDOLL_RHAND = 7;
  public static final int PAPERDOLL_LHAND = 8;
  public static final int PAPERDOLL_GLOVES = 9;
  public static final int PAPERDOLL_CHEST = 10;
  public static final int PAPERDOLL_LEGS = 11;
  public static final int PAPERDOLL_FEET = 12;
  public static final int PAPERDOLL_BACK = 13;
  public static final int PAPERDOLL_LRHAND = 14;
  public static final int PAPERDOLL_FACE = 15;
  public static final int PAPERDOLL_HAIR = 16;
  public static final int PAPERDOLL_DHAIR = 17;
  public static final double MAX_ARMOR_WEIGHT = 12000.0D;
  private final L2ItemInstance[] _paperdoll;
  private final List<PaperdollListener> _paperdollListeners;
  protected int _totalWeight;
  private int _wearedMask;

  protected Inventory()
  {
    _paperdoll = new L2ItemInstance[18];
    _paperdollListeners = new FastList();
    addPaperdollListener(new ArmorSetListener());
    addPaperdollListener(new BowListener());
    addPaperdollListener(new ItemPassiveSkillsListener());
    addPaperdollListener(new StatsListener());
  }

  protected abstract L2ItemInstance.ItemLocation getEquipLocation();

  public ChangeRecorder newRecorder()
  {
    return new ChangeRecorder(this);
  }

  public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
  {
    synchronized (item) {
      if (!_items.contains(item)) {
        return null;
      }

      removeItem(item);
      item.setOwnerId(process, 0, actor, reference);
      item.setLocation(L2ItemInstance.ItemLocation.VOID);
      item.setLastChange(3);

      item.updateDatabase();
      refreshWeight();
    }
    return item;
  }

  public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
  {
    L2ItemInstance item = getItemByObjectId(objectId);
    if (item == null) {
      return null;
    }

    if (item.getCount() > count) {
      item.changeCount(process, -count, actor, reference);
      item.setLastChange(2);
      item.updateDatabase();

      item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);

      item.updateDatabase();
      refreshWeight();
      return item;
    }

    return dropItem(process, item, actor, reference);
  }

  public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference, boolean whFlag)
  {
    L2ItemInstance item = getItemByObjectId(objectId);
    if (item == null) {
      return null;
    }

    item.setWhFlag(true);
    return dropItem(process, objectId, count, actor, reference);
  }

  protected void addItem(L2ItemInstance item)
  {
    super.addItem(item);
    if (item.isEquipped())
      equipItem(item);
  }

  protected void removeItem(L2ItemInstance item)
  {
    for (int i = 0; i < _paperdoll.length; i++) {
      if (_paperdoll[i] == item) {
        unEquipItemInSlot(i);
      }
    }

    super.removeItem(item);
  }

  public L2ItemInstance getPaperdollItem(int slot)
  {
    return _paperdoll[slot];
  }

  public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
  {
    switch (slot) {
    case 1:
      return _paperdoll[0];
    case 4:
      return _paperdoll[1];
    case 2:
      return _paperdoll[2];
    case 8:
      return _paperdoll[3];
    case 32:
      return _paperdoll[4];
    case 16:
      return _paperdoll[5];
    case 64:
      return _paperdoll[6];
    case 128:
      return _paperdoll[7];
    case 256:
      return _paperdoll[8];
    case 512:
      return _paperdoll[9];
    case 1024:
      return _paperdoll[10];
    case 2048:
      return _paperdoll[11];
    case 4096:
      return _paperdoll[12];
    case 8192:
      return _paperdoll[13];
    case 16384:
      return _paperdoll[14];
    case 262144:
      return _paperdoll[15];
    case 65536:
      return _paperdoll[16];
    case 524288:
      return _paperdoll[17];
    }
    return null;
  }

  public int getPaperdollItemId(int slot)
  {
    L2ItemInstance item = _paperdoll[slot];
    if (item != null)
      return item.getItemId();
    if (slot == 16) {
      item = _paperdoll[17];
      if (item != null) {
        return item.getItemId();
      }
    }
    return 0;
  }

  public int getPaperdollAugmentationId(int slot) {
    L2ItemInstance item = _paperdoll[slot];
    if (item != null) {
      if (item.getAugmentation() != null) {
        return item.getAugmentation().getAugmentationId();
      }
      return 0;
    }

    return 0;
  }

  public int getPaperdollObjectId(int slot)
  {
    L2ItemInstance item = _paperdoll[slot];
    if (item != null)
      return item.getObjectId();
    if (slot == 16) {
      item = _paperdoll[17];
      if (item != null) {
        return item.getObjectId();
      }
    }
    return 0;
  }

  public synchronized void addPaperdollListener(PaperdollListener listener)
  {
    if ((Config.ASSERT) && 
      (!$assertionsDisabled) && (_paperdollListeners.contains(listener))) throw new AssertionError();

    _paperdollListeners.add(listener);
  }

  public synchronized void removePaperdollListener(PaperdollListener listener)
  {
    _paperdollListeners.remove(listener);
  }

  public L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
  {
    L2ItemInstance old = _paperdoll[slot];
    if (old != item) {
      if (old != null) {
        _paperdoll[slot] = null;

        old.setLocation(getBaseLocation());
        old.setLastChange(2);

        int mask = 0;
        for (int i = 0; i < 14; i++) {
          L2ItemInstance pi = _paperdoll[i];
          if (pi != null) {
            mask |= pi.getItem().getItemMask();
          }
        }
        _wearedMask = mask;

        for (PaperdollListener listener : _paperdollListeners) {
          if (listener == null) {
            continue;
          }
          listener.notifyUnequiped(slot, old);
          manageHippy(old.getItem().isHippy(), false);
        }
        old.updateDatabase();
      }

      if (item != null) {
        _paperdoll[slot] = item;
        item.setLocation(getEquipLocation(), slot);
        item.setLastChange(2);
        _wearedMask |= item.getItem().getItemMask();
        for (PaperdollListener listener : _paperdollListeners) {
          listener.notifyEquiped(slot, item);
          manageHippy(item.getItem().isHippy(), true);
        }
        item.updateDatabase();
      }
    }
    return old;
  }

  public int getWearedMask()
  {
    return _wearedMask;
  }

  public int getSlotFromItem(L2ItemInstance item) {
    int slot = -1;
    int location = item.getEquipSlot();

    switch (location) {
    case 0:
      slot = 1;
      break;
    case 1:
      slot = 4;
      break;
    case 2:
      slot = 2;
      break;
    case 3:
      slot = 8;
      break;
    case 5:
      slot = 16;
      break;
    case 4:
      slot = 32;
      break;
    case 16:
      slot = 65536;
      break;
    case 15:
      slot = 262144;
      break;
    case 17:
      slot = 524288;
      break;
    case 6:
      slot = 64;
      break;
    case 7:
      slot = 128;
      break;
    case 8:
      slot = 256;
      break;
    case 9:
      slot = 512;
      break;
    case 10:
      slot = item.getItem().getBodyPart();
      break;
    case 11:
      slot = 2048;
      break;
    case 13:
      slot = 8192;
      break;
    case 12:
      slot = 4096;
      break;
    case 14:
      slot = 16384;
    }

    return slot;
  }

  public void unEquipItem(L2ItemInstance item) {
    unEquipItemInBodySlot(item.getItem().getBodyPart());
  }

  public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
  {
    ChangeRecorder recorder = newRecorder();
    try {
      unEquipItemInBodySlot(slot);
    } finally {
      removePaperdollListener(recorder);
    }
    return recorder.getChangedItems();
  }

  public synchronized L2ItemInstance unEquipItemInSlot(int pdollSlot)
  {
    return setPaperdollItem(pdollSlot, null);
  }

  public synchronized L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
  {
    ChangeRecorder recorder = newRecorder();
    try {
      unEquipItemInSlot(slot);
      if (getOwner().isPlayer())
        getOwner().getPlayer().refreshExpertisePenalty();
    }
    finally {
      removePaperdollListener(recorder);
    }
    return recorder.getChangedItems();
  }

  private void unEquipItemInBodySlot(int slot)
  {
    int pdollSlot = -1;

    switch (slot) {
    case 4:
      pdollSlot = 1;
      break;
    case 2:
      pdollSlot = 2;
      break;
    case 8:
      pdollSlot = 3;
      break;
    case 16:
      pdollSlot = 5;
      break;
    case 32:
      pdollSlot = 4;
      break;
    case 65536:
      pdollSlot = 16;
      break;
    case 262144:
      pdollSlot = 15;
      break;
    case 524288:
      setPaperdollItem(16, null);
      setPaperdollItem(15, null);
      pdollSlot = 17;
      break;
    case 64:
      pdollSlot = 6;
      break;
    case 128:
      pdollSlot = 7;
      break;
    case 256:
      pdollSlot = 8;
      break;
    case 512:
      pdollSlot = 9;
      break;
    case 1024:
    case 32768:
      pdollSlot = 10;
      break;
    case 2048:
      pdollSlot = 11;
      break;
    case 8192:
      pdollSlot = 13;
      break;
    case 4096:
      pdollSlot = 12;
      break;
    case 1:
      pdollSlot = 0;
      break;
    case 16384:
      setPaperdollItem(8, null);
      setPaperdollItem(7, null);
      pdollSlot = 14;
    }

    if (pdollSlot >= 0)
      setPaperdollItem(pdollSlot, null);
  }

  public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
  {
    ChangeRecorder recorder = newRecorder();
    try
    {
      equipItem(item);
    } finally {
      removePaperdollListener(recorder);
    }

    return recorder.getChangedItems();
  }

  public synchronized void equipItem(L2ItemInstance item)
  {
    if ((getOwner().isPlayer()) && (getOwner().getName() != null))
    {
      L2PcInstance player = getOwner().getPlayer();
      if ((item.isHeroItem()) && (!player.isHero()) && (Config.HERO_ITEMS_PENALTY)) {
        return;
      }

      L2Clan cl = player.getClan();
      int itemId = item.getItemId();

      if (((cl == null) || (cl.getHasCastle() == 0)) && (itemId == 7015) && (Config.CASTLE_SHIELD))
      {
        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        return;
      }

      if (((cl == null) || (cl.getHasHideout() == 0)) && (itemId == 6902) && (Config.CLANHALL_SHIELD))
      {
        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        return;
      }

      if ((itemId >= 7860) && (itemId <= 7879) && (Config.APELLA_ARMORS) && ((cl == null) || (player.getPledgeClass() < 5)))
      {
        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        return;
      }

      if ((itemId >= 7850) && (itemId <= 7859) && (Config.OATH_ARMORS) && (cl == null))
      {
        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        return;
      }

      if ((itemId == 6841) && (Config.CASTLE_CROWN) && ((cl == null) || (cl.getHasCastle() == 0) || (!player.isClanLeader())))
      {
        player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
        return;
      }

      if ((Config.CASTLE_CIRCLETS) && (((itemId >= 6834) && (itemId <= 6840)) || (itemId == 8182) || (itemId == 8183))) {
        if (cl == null) {
          player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
          return;
        }
        int circletId = CastleManager.getInstance().getCircletByCastleId(cl.getHasCastle());
        if ((player.getPledgeType() == -1) || (circletId != itemId)) {
          player.sendPacket(Static.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
          return;
        }

      }

    }

    int targetSlot = item.getItem().getBodyPart();

    switch (targetSlot) {
    case 16384:
      if (setPaperdollItem(8, null) != null)
      {
        setPaperdollItem(7, null);
        setPaperdollItem(8, null);
      } else {
        setPaperdollItem(7, null);
      }

      setPaperdollItem(7, item);
      setPaperdollItem(14, item);
      break;
    case 256:
      if ((!(item.getItem() instanceof L2EtcItem)) || (item.getItem().getItemType() != L2EtcItemType.ARROW)) {
        L2ItemInstance old1 = setPaperdollItem(14, null);

        if (old1 != null) {
          setPaperdollItem(7, null);
        }
      }

      setPaperdollItem(8, null);
      setPaperdollItem(8, item);
      break;
    case 128:
      if (_paperdoll[14] != null) {
        setPaperdollItem(14, null);
        setPaperdollItem(8, null);
        setPaperdollItem(7, null);
      } else {
        setPaperdollItem(7, null);
      }

      setPaperdollItem(7, item);
      break;
    case 2:
    case 4:
    case 6:
      if (_paperdoll[1] == null) {
        setPaperdollItem(1, item);
      } else if (_paperdoll[2] == null) {
        setPaperdollItem(2, item);
      } else {
        setPaperdollItem(1, null);
        setPaperdollItem(1, item);
      }

      break;
    case 16:
    case 32:
    case 48:
      if (_paperdoll[4] == null) {
        setPaperdollItem(4, item);
      } else if (_paperdoll[5] == null) {
        setPaperdollItem(5, item);
      } else {
        setPaperdollItem(4, null);
        setPaperdollItem(4, item);
      }

      break;
    case 8:
      setPaperdollItem(3, item);
      break;
    case 32768:
      setPaperdollItem(10, null);
      setPaperdollItem(11, null);
      setPaperdollItem(10, item);
      break;
    case 1024:
      setPaperdollItem(10, item);
      break;
    case 2048:
      L2ItemInstance chest = getPaperdollItem(10);
      if ((chest != null) && (chest.getItem().getBodyPart() == 32768)) {
        setPaperdollItem(10, null);
      }

      setPaperdollItem(11, null);
      setPaperdollItem(11, item);
      break;
    case 4096:
      setPaperdollItem(12, item);
      break;
    case 512:
      setPaperdollItem(9, item);
      break;
    case 64:
      setPaperdollItem(6, item);
      break;
    case 65536:
      if (setPaperdollItem(17, null) != null) {
        setPaperdollItem(17, null);
        setPaperdollItem(16, null);
        setPaperdollItem(15, null);
      } else {
        setPaperdollItem(16, null);
      }
      setPaperdollItem(16, item);
      break;
    case 262144:
      if (setPaperdollItem(17, null) != null) {
        setPaperdollItem(17, null);
        setPaperdollItem(16, null);
        setPaperdollItem(15, null);
      } else {
        setPaperdollItem(15, null);
      }
      setPaperdollItem(15, item);
      break;
    case 524288:
      if (setPaperdollItem(16, null) != null) {
        setPaperdollItem(16, null);
        setPaperdollItem(15, null);
      } else {
        setPaperdollItem(15, null);
      }
      setPaperdollItem(17, item);
      break;
    case 1:
      setPaperdollItem(0, item);
      break;
    case 8192:
      setPaperdollItem(13, item);
      break;
    default:
      _log.warning("unknown body slot:" + targetSlot);
    }
  }

  protected void refreshWeight()
  {
    int weight = 0;

    for (L2ItemInstance item : _items) {
      if ((item != null) && (item.getItem() != null)) {
        weight += item.getItem().getWeight() * item.getCount();
      }
    }

    _totalWeight = weight;
  }

  public int getTotalWeight()
  {
    return _totalWeight;
  }

  public L2ItemInstance findArrowForBow(L2Item bow)
  {
    if (bow == null) {
      return null;
    }

    int arrowsId = 0;
    switch (bow.getCrystalType()) {
    case 0:
    default:
      arrowsId = 17;
      break;
    case 1:
      arrowsId = 1341;
      break;
    case 2:
      arrowsId = 1342;
      break;
    case 3:
      arrowsId = 1343;
      break;
    case 4:
      arrowsId = 1344;
      break;
    case 5:
      arrowsId = 1345;
    }

    return getItemByItemId(arrowsId);
  }

  public void restore()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);
      st = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC");
      st.setInt(1, getOwner().getObjectId());
      st.setString(2, getBaseLocation().name());
      st.setString(3, getEquipLocation().name());
      rs = st.executeQuery();

      while (rs.next()) {
        int objectId = rs.getInt(1);
        L2ItemInstance item = L2ItemInstance.restoreFromDb(objectId);
        if (item == null)
        {
          continue;
        }
        if ((getOwner().isPlayer()) && 
          (!getOwner().getPlayer().isGM()) && 
          (!getOwner().getPlayer().isHero())) {
          int itemId = item.getItemId();
          if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842)) {
            item.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
          }

        }

        L2World.getInstance().storeObject(item);

        if ((item.isStackable()) && (getItemByItemId(item.getItemId()) != null))
          addItem("Restore", item, null, getOwner());
        else {
          addItem(item);
        }
      }
      refreshWeight();
    } catch (SQLException e) {
      _log.warning("Could not restore inventory : " + e);
    } finally {
      Close.CSR(con, st, rs);
    }
  }

  public void reloadEquippedItems()
  {
    L2ItemInstance item;
    int slot;
    for (int i = 0; i < _paperdoll.length; i++) {
      item = _paperdoll[i];
      if (item == null) {
        continue;
      }
      slot = item.getEquipSlot();

      for (PaperdollListener listener : _paperdollListeners) {
        if (listener == null) {
          continue;
        }
        listener.notifyUnequiped(slot, item);
        listener.notifyEquiped(slot, item);
      }
    }
  }

  private void manageHippy(boolean hippy, boolean b)
  {
    if (!hippy) {
      return;
    }
    if (b) {
      getOwner().setHippy(b);
      return;
    }

    getOwner().setHippy(false);
  }

  final class ArmorSetListener
    implements Inventory.PaperdollListener
  {
    ArmorSetListener()
    {
    }

    public void notifyEquiped(int slot, L2ItemInstance item)
    {
      if (!getOwner().isPlayer()) {
        return;
      }

      L2ItemInstance chestItem = getPaperdollItem(10);
      if (chestItem == null) {
        return;
      }

      L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
      if (armorSet == null) {
        return;
      }

      L2PcInstance player = getOwner().getPlayer();

      if (armorSet.containItem(slot, item.getItemId())) {
        if (armorSet.containAll(player)) {
          L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
          if (skill != null) {
            player.addSkill(skill, false);
            player.sendSkillList();
          } else {
            ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
          }

          if (armorSet.containShield(player))
          {
            L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
            if (skills != null) {
              player.addSkill(skills, false);
              player.sendSkillList();
            } else {
              ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
            }
          }
          if (armorSet.isEnchanted6(player))
          {
            int skillId = armorSet.getEnchant6skillId();
            if (skillId > 0) {
              L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
              if (skille != null) {
                player.addSkill(skille, false);
                player.sendSkillList();
              } else {
                ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
              }
            }
          }
        }
      } else if ((armorSet.containShield(item.getItemId())) && 
        (armorSet.containAll(player))) {
        L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
        if (skills != null) {
          player.addSkill(skills, false);
          player.sendSkillList();
        } else {
          ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
        }
      }
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      if (!getOwner().isPlayer()) {
        return;
      }

      boolean remove = false;
      int removeSkillId1 = 0;
      int removeSkillId2 = 0;
      int removeSkillId3 = 0;

      if (slot == 10) {
        L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
        if (armorSet == null) {
          return;
        }

        remove = true;
        removeSkillId1 = armorSet.getSkillId();
        removeSkillId2 = armorSet.getShieldSkillId();
        removeSkillId3 = armorSet.getEnchant6skillId();
      } else {
        L2ItemInstance chestItem = getPaperdollItem(10);
        if (chestItem == null) {
          return;
        }

        L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
        if (armorSet == null) {
          return;
        }

        if (armorSet.containItem(slot, item.getItemId()))
        {
          remove = true;
          removeSkillId1 = armorSet.getSkillId();
          removeSkillId2 = armorSet.getShieldSkillId();
          removeSkillId3 = armorSet.getEnchant6skillId();
        } else if (armorSet.containShield(item.getItemId()))
        {
          remove = true;
          removeSkillId2 = armorSet.getShieldSkillId();
        }
      }

      L2PcInstance player = getOwner().getPlayer();
      if (remove) {
        if (removeSkillId1 != 0) {
          L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
          if (skill != null)
            player.removeSkill(skill);
          else {
            ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
          }
        }
        if (removeSkillId2 != 0) {
          L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
          if (skill != null)
            player.removeSkill(skill);
          else {
            ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
          }
        }
        if (removeSkillId3 != 0) {
          L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
          if (skill != null)
            player.removeSkill(skill);
          else {
            ItemContainer._log.warning("Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
          }
        }
        player.sendSkillList();
      }
    }
  }

  final class ItemPassiveSkillsListener
    implements Inventory.PaperdollListener
  {
    ItemPassiveSkillsListener()
    {
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      L2PcInstance player;
      if (getOwner().isPlayer())
        player = getOwner().getPlayer();
      else
        return;
      L2PcInstance player;
      L2Skill passiveSkill = null;
      L2Skill enchant4Skill = null;

      L2Item it = item.getItem();

      if ((it instanceof L2Weapon))
      {
        if (item.isAugmented()) {
          item.getAugmentation().removeBoni(player);
        }

        passiveSkill = ((L2Weapon)it).getSkill();
        enchant4Skill = ((L2Weapon)it).getEnchant4Skill();
      } else if ((it instanceof L2Armor)) {
        passiveSkill = ((L2Armor)it).getSkill();
      }

      if (passiveSkill != null) {
        player.removeSkill(passiveSkill, false);
        player.sendSkillList();
      }
      if (enchant4Skill != null) {
        player.removeSkill(enchant4Skill, false);
        player.sendSkillList();
      }
    }

    public void notifyEquiped(int slot, L2ItemInstance item)
    {
      L2PcInstance player;
      if (getOwner().isPlayer())
        player = getOwner().getPlayer();
      else
        return;
      L2PcInstance player;
      L2Skill passiveSkill = null;
      L2Skill enchant4Skill = null;

      L2Item it = item.getItem();

      if ((it instanceof L2Weapon))
      {
        if ((item.isAugmented()) && (getOwner().isPlayer())) {
          if ((player.isInOlympiadMode()) && (!Config.ALT_ALLOW_AUGMENT_ON_OLYMP))
            item.getAugmentation().removeBoni(getOwner().getPlayer());
          else {
            item.getAugmentation().applyBoni(getOwner().getPlayer());
          }
        }

        passiveSkill = ((L2Weapon)it).getSkill();
        if (item.getEnchantLevel() >= 4)
          enchant4Skill = ((L2Weapon)it).getEnchant4Skill();
      }
      else if ((it instanceof L2Armor)) {
        passiveSkill = ((L2Armor)it).getSkill();
      }

      if (passiveSkill != null) {
        player.addSkill(passiveSkill, false);
        player.sendSkillList();
      }
      if (enchant4Skill != null) {
        player.addSkill(enchant4Skill, false);
        player.sendSkillList();
      }
    }
  }

  final class StatsListener
    implements Inventory.PaperdollListener
  {
    StatsListener()
    {
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      if (slot == 14) {
        return;
      }
      getOwner().removeStatsOwner(item);
    }

    public void notifyEquiped(int slot, L2ItemInstance item) {
      if (slot == 14) {
        return;
      }
      getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
    }
  }

  final class BowListener
    implements Inventory.PaperdollListener
  {
    BowListener()
    {
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      if (slot != 14) {
        return;
      }
      if ((Config.ASSERT) && 
        (!$assertionsDisabled) && (null != getPaperdollItem(14))) throw new AssertionError();

      if (item.getItemType() == L2WeaponType.BOW) {
        L2ItemInstance arrow = getPaperdollItem(8);
        if (arrow != null)
          setPaperdollItem(8, null);
      }
    }

    public void notifyEquiped(int slot, L2ItemInstance item)
    {
      if (slot != 14) {
        return;
      }
      if ((Config.ASSERT) && 
        (!$assertionsDisabled) && (item != getPaperdollItem(14))) throw new AssertionError();

      if (item.getItemType() == L2WeaponType.BOW) {
        L2ItemInstance arrow = findArrowForBow(item.getItem());
        if (arrow != null)
          setPaperdollItem(8, arrow);
      }
    }
  }

  public static final class ChangeRecorder
    implements Inventory.PaperdollListener
  {
    private final Inventory _inventory;
    private final List<L2ItemInstance> _changed;

    ChangeRecorder(Inventory inventory)
    {
      _inventory = inventory;
      _changed = new FastList();
      _inventory.addPaperdollListener(this);
    }

    public void notifyEquiped(int slot, L2ItemInstance item)
    {
      if (!_changed.contains(item))
        _changed.add(item);
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      if (!_changed.contains(item))
        _changed.add(item);
    }

    public L2ItemInstance[] getChangedItems()
    {
      return (L2ItemInstance[])_changed.toArray(new L2ItemInstance[_changed.size()]);
    }
  }

  final class FormalWearListener
    implements Inventory.PaperdollListener
  {
    FormalWearListener()
    {
    }

    public void notifyUnequiped(int slot, L2ItemInstance item)
    {
      if ((getOwner() == null) || (!getOwner().isPlayer()))
      {
        return;
      }

      if (item.getItemId() == 6408)
        getOwner().getPlayer().setIsWearingFormalWear(false);
    }

    public void notifyEquiped(int slot, L2ItemInstance item)
    {
      if ((getOwner() == null) || (!getOwner().isPlayer()))
      {
        return;
      }

      if (item.getItemId() == 6408) {
        getOwner().getPlayer().setIsWearingFormalWear(true);
      }
      else if (!getOwner().getPlayer().isWearingFormalWear())
        return;
    }
  }

  public static abstract interface PaperdollListener
  {
    public abstract void notifyEquiped(int paramInt, L2ItemInstance paramL2ItemInstance);

    public abstract void notifyUnequiped(int paramInt, L2ItemInstance paramL2ItemInstance);
  }
}