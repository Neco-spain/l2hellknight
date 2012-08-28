package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public final class RequestDropItem extends L2GameClientPacket
{
  private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
  private static Logger _log = Logger.getLogger(RequestDropItem.class.getName());
  private int _objectId;
  private int _count;
  private int _x;
  private int _y;
  private int _z;

  protected void readImpl()
  {
    _objectId = readD();
    _count = readD();
    _x = readD();
    _y = readD();
    _z = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), 5))
    {
      return;
    }

    L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

    if ((item == null) || (_count == 0) || (!activeChar.validateItemManipulation(_objectId, "drop")) || ((!Config.ALLOW_DISCARDITEM) && (!activeChar.isGM())) || (!item.isDropable()))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
      return;
    }
    if (item.getItemType() == L2EtcItemType.QUEST)
    {
      return;
    }
    int itemId = item.getItemId();

    if (CursedWeaponsManager.getInstance().isCursed(itemId)) {
      return;
    }
    if (_count > item.getCount())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
      return;
    }

    if (_count < 0)
    {
      Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), 2);
      return;
    }

    if ((!item.isStackable()) && (_count > 1))
    {
      Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + activeChar.getName(), 2);
      return;
    }

    if ((Config.GM_DISABLE_TRANSACTION) && (activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
    {
      activeChar.sendMessage("Transactions are disable for your Access Level");
      activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
      return;
    }

    if ((activeChar.isProcessingTransaction()) || (activeChar.getPrivateStoreType() != 0))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
      return;
    }
    if (activeChar.isFishing())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2));
      return;
    }

    if (activeChar.isCastingNow())
    {
      if ((activeChar.getCurrentSkill() != null) && (activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId()))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
        return;
      }
    }

    if ((3 == item.getItem().getType2()) && (!activeChar.isGM()))
    {
      if (Config.DEBUG) _log.finest(activeChar.getObjectId() + ":player tried to drop quest item");
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM));
      return;
    }

    if ((!activeChar.isInsideRadius(_x, _y, 150, false)) || (Math.abs(_z - activeChar.getZ()) > 50))
    {
      if (Config.DEBUG) _log.finest(activeChar.getObjectId() + ": trying to drop too far away");
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR));
      return;
    }

    if (Config.DEBUG) _log.fine("requested drop item " + _objectId + "(" + item.getCount() + ") at " + _x + "/" + _y + "/" + _z);

    if (item.isEquipped())
    {
      if (item.isAugmented()) {
        item.getAugmentation().removeBoni(activeChar);
      }
      L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
      InventoryUpdate iu = new InventoryUpdate();
      for (int i = 0; i < unequiped.length; i++)
      {
        activeChar.checkSSMatch(null, unequiped[i]);

        iu.addModifiedItem(unequiped[i]);
      }
      activeChar.sendPacket(iu);
      activeChar.broadcastUserInfo();

      ItemList il = new ItemList(activeChar, true);
      activeChar.sendPacket(il);
    }

    L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false);

    if (Config.DEBUG) _log.fine("dropping " + _objectId + " item(" + _count + ") at: " + _x + " " + _y + " " + _z);

    if (activeChar.isGM())
    {
      String target = activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target";
      GMAudit.auditGMAction(activeChar.getName(), "drop", target, dropedItem.getItemId() + " - " + dropedItem.getName());
    }

    if ((dropedItem != null) && (dropedItem.getItemId() == 57) && (dropedItem.getCount() >= 1000000))
    {
      String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
      _log.warning(msg);
      GmListTable.broadcastMessageToGMs(msg);
    }
  }

  public String getType()
  {
    return "[C] 12 RequestDropItem";
  }
}