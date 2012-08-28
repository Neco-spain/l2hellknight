package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
  private static final String REQUESTCIVEITEMTOPET__C__8B = "[C] 8B RequestGiveItemToPet";
  private static Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
  private int _objectId;
  private int _amount;

  protected void readImpl()
  {
    _objectId = readD();
    _amount = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if ((player == null) || (player.getPet() == null) || (!(player.getPet() instanceof L2PetInstance))) return;

    if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE) && (player.getKarma() > 0)) return;
    if (player.getPrivateStoreType() != 0)
    {
      player.sendMessage("Cannot exchange items while trading");
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
    if (item.isAugmented())
    {
      player.sendMessage("Due To Hero Weapons Protection , You Can't Use Pet's Inventory");
      return;
    }

    if (item == null) {
      return;
    }
    if (item.isHeroItem())
    {
      player.sendMessage("Duo To Hero Weapons Protection u Canot Use Pet's Inventory");
      return;
    }

    if ((!item.isDropable()) || (!item.isDestroyable()) || (!item.isTradeable()))
    {
      sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
      return;
    }

    L2PetInstance pet = (L2PetInstance)player.getPet();

    if (pet.isDead())
    {
      sendPacket(new SystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
      return;
    }

    if (item.getObjectId() == pet.getControlItemId())
    {
      return;
    }

    if (player.getActiveEnchantItem() != null)
    {
      player.sendMessage("Cannot give items to pet while enchanting");
      return;
    }

    if (_amount < 0)
    {
      return;
    }

    if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
    {
      _log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
    }
  }

  public String getType()
  {
    return "[C] 8B RequestGiveItemToPet";
  }
}