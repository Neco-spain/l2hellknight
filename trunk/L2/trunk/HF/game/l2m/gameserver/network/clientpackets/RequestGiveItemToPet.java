package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.PetInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.items.PetInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.templates.item.ItemTemplate;

public class RequestGiveItemToPet extends L2GameClientPacket
{
  private int _objectId;
  private long _amount;

  protected void readImpl()
  {
    _objectId = readD();
    _amount = readQ();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_amount < 1L)) {
      return;
    }
    PetInstance pet = (PetInstance)activeChar.getPet();
    if (pet == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    if (pet.isDead())
    {
      sendPacket(Msg.CANNOT_GIVE_ITEMS_TO_A_DEAD_PET);
      return;
    }

    if (_objectId == pet.getControlItemObjId())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendActionFailed();
      return;
    }

    PetInventory petInventory = pet.getInventory();
    PcInventory playerInventory = activeChar.getInventory();

    ItemInstance item = playerInventory.getItemByObjectId(_objectId);
    if ((item == null) || (item.getCount() < _amount) || (!item.canBeDropped(activeChar, false)))
    {
      activeChar.sendActionFailed();
      return;
    }

    int slots = 0;
    long weight = item.getTemplate().getWeight() * _amount;
    if ((!item.getTemplate().isStackable()) || (pet.getInventory().getItemByItemId(item.getItemId()) == null)) {
      slots = 1;
    }
    if (!pet.getInventory().validateWeight(weight))
    {
      sendPacket(Msg.EXCEEDED_PET_INVENTORYS_WEIGHT_LIMIT);
      return;
    }

    if (!pet.getInventory().validateCapacity(slots))
    {
      sendPacket(Msg.DUE_TO_THE_VOLUME_LIMIT_OF_THE_PETS_INVENTORY_NO_MORE_ITEMS_CAN_BE_PLACED_THERE);
      return;
    }

    petInventory.addItem(playerInventory.removeItemByObjectId(_objectId, _amount));

    pet.sendChanges();
    activeChar.sendChanges();
  }
}