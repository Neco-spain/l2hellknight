package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.handler.items.IItemHandler;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExUseSharedGroupItem;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.TimeStamp;
import l2m.gameserver.data.tables.PetDataTable;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.ReuseType;

public class UseItem extends L2GameClientPacket
{
  private int _objectId;
  private boolean _ctrlPressed;

  protected void readImpl()
  {
    _objectId = readD();
    _ctrlPressed = (readD() == 1);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setActive();

    ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
    if (item == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    int itemId = item.getItemId();

    if (activeChar.isInStoreMode())
    {
      if (PetDataTable.isPetControlItem(item))
        activeChar.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
      else
        activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
      return;
    }

    if ((activeChar.isFishing()) && ((itemId < 6535) || (itemId > 6540)))
    {
      activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
      return;
    }

    if (activeChar.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
    {
      activeChar.sendReuseMessage(item);
      return;
    }

    if (!item.getTemplate().testCondition(activeChar, item)) {
      return;
    }
    if (activeChar.getInventory().isLockedItem(item)) {
      return;
    }
    if (item.getTemplate().isForPet())
    {
      activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
      return;
    }

    if ((Config.ALT_IMPROVED_PETS_LIMITED_USE) && (activeChar.isMageClass()) && (item.getItemId() == 10311))
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
      return;
    }

    if ((Config.ALT_IMPROVED_PETS_LIMITED_USE) && (!activeChar.isMageClass()) && (item.getItemId() == 10313))
    {
      activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
      return;
    }

    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    boolean success = item.getTemplate().getHandler().useItem(activeChar, item, _ctrlPressed);
    if (success)
    {
      long nextTimeUse = item.getTemplate().getReuseType().next(item);
      if (nextTimeUse > System.currentTimeMillis())
      {
        TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
        activeChar.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);

        if (item.getTemplate().getReuseDelay() > 0)
          activeChar.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
      }
    }
  }
}