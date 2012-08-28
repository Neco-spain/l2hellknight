package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

public final class RequestCrystallizeItem extends L2GameClientPacket
{
  private static final String _C__72_REQUESTDCRYSTALLIZEITEM = "[C] 72 RequestCrystallizeItem";
  private static Logger _log = Logger.getLogger(RequestCrystallizeItem.class.getName());
  private int _objectId;
  private int _count;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null)
    {
      _log.fine("RequestCrystalizeItem: activeChar was null");
      return;
    }

    if (_count <= 0)
    {
      Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), 2);

      return;
    }

    if ((activeChar.getPrivateStoreType() != 0) || (activeChar.isInCrystallize()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));

      return;
    }

    int skillLevel = activeChar.getSkillLevel(248);
    if (skillLevel <= 0)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);

      activeChar.sendPacket(sm);
      sm = null;
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    PcInventory inventory = activeChar.getInventory();
    if (inventory != null)
    {
      L2ItemInstance item = inventory.getItemByObjectId(_objectId);
      if ((item == null) || (item.isWear()))
      {
        ActionFailed af = new ActionFailed();
        activeChar.sendPacket(af);
        return;
      }

      int itemId = item.getItemId();
      if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842)) {
        return;
      }
      if (_count > item.getCount())
      {
        _count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
      }

    }

    L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);

    if ((itemToRemove == null) || (itemToRemove.isWear()))
    {
      return;
    }
    if ((!itemToRemove.getItem().isCrystallizable()) || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == 0))
    {
      _log.warning("" + activeChar.getObjectId() + " tried to crystallize " + itemToRemove.getItem().getItemId());

      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 2) && (skillLevel <= 1))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);

      activeChar.sendPacket(sm);
      sm = null;
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 3) && (skillLevel <= 2))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);

      activeChar.sendPacket(sm);
      sm = null;
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 4) && (skillLevel <= 3))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);

      activeChar.sendPacket(sm);
      sm = null;
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    if ((itemToRemove.getItem().getCrystalType() == 5) && (skillLevel <= 4))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);

      activeChar.sendPacket(sm);
      sm = null;
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    activeChar.setInCrystallize(true);

    if (itemToRemove.isEquipped())
    {
      L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot());

      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
      {
        iu.addModifiedItem(unequiped[i]);
      }
      activeChar.sendPacket(iu);
    }

    L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);

    int crystalId = itemToRemove.getItem().getCrystalItemId();
    int crystalAmount = itemToRemove.getCrystalCount();
    L2ItemInstance createditem = activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, itemToRemove);

    SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
    sm.addItemName(crystalId);
    sm.addNumber(crystalAmount);
    activeChar.sendPacket(sm);
    sm = null;

    if (!Config.FORCE_INVENTORY_UPDATE)
    {
      InventoryUpdate iu = new InventoryUpdate();
      if (removedItem.getCount() == 0)
        iu.addRemovedItem(removedItem);
      else {
        iu.addModifiedItem(removedItem);
      }
      if (createditem.getCount() != crystalAmount)
        iu.addModifiedItem(createditem);
      else {
        iu.addNewItem(createditem);
      }
      activeChar.sendPacket(iu);
    } else {
      activeChar.sendPacket(new ItemList(activeChar, false));
    }

    StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
    su.addAttribute(14, activeChar.getCurrentLoad());
    activeChar.sendPacket(su);

    activeChar.broadcastUserInfo();

    L2World world = L2World.getInstance();
    world.removeObject(removedItem);

    activeChar.setInCrystallize(false);
  }

  public String getType()
  {
    return "[C] 72 RequestCrystallizeItem";
  }
}