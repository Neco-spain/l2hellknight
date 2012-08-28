package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.templates.L2Item;

public final class RequestGiveItemToPet extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
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
    if ((player == null) || (player.getPet() == null) || (!player.getPet().isPet())) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPT() < 400L) {
      player.sendActionFailed();
      return;
    }

    player.sCPT();

    if (_amount < 0) {
      return;
    }

    if (!Config.GIVE_ITEM_PET) {
      player.sendMessage("\u041E\u0442\u043A\u043B\u044E\u0447\u0435\u043D\u043E.");
      player.sendActionFailed();
      return;
    }

    if (player.getActiveEnchantItem() != null) {
      player.setActiveEnchantItem(null);
      player.sendPacket(new EnchantResult(0, true));
      player.sendActionFailed();
      return;
    }

    if (player.getActiveTradeList() != null) {
      player.cancelActiveTrade();
      player.sendActionFailed();
      return;
    }

    if (player.getActiveWarehouse() != null) {
      player.cancelActiveWarehouse();
      player.sendActionFailed();
      return;
    }

    if (player.getPrivateStoreType() != 0) {
      player.sendMessage("Cannot exchange items while trading");
      return;
    }

    L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
    if (item == null) {
      player.sendActionFailed();
      return;
    }

    if (item.isAugmented()) {
      return;
    }

    if ((!item.isDropable()) || (!item.isDestroyable()) || (!item.isTradeable())) {
      player.sendPacket(Static.ITEM_NOT_FOR_PETS);
      return;
    }

    L2PetInstance pet = (L2PetInstance)player.getPet();
    if (pet.isDead()) {
      player.sendPacket(Static.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
      return;
    }

    if ((item.getItem().isForWolf()) || (item.getItem().isForHatchling()) || (item.getItem().isForStrider()) || (item.getItem().isForBabyPet())) {
      if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
        _log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
    }
    else {
      player.sendPacket(Static.ITEM_NOT_FOR_PETS);
      return;
    }
  }
}