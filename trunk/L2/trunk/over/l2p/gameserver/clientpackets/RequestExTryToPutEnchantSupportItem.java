package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExPutEnchantSupportItemResult;
import l2p.gameserver.utils.ItemFunctions;

public class RequestExTryToPutEnchantSupportItem extends L2GameClientPacket
{
  private int _itemId;
  private int _catalystId;

  protected void readImpl()
  {
    _catalystId = readD();
    _itemId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    PcInventory inventory = activeChar.getInventory();
    ItemInstance itemToEnchant = inventory.getItemByObjectId(_itemId);
    ItemInstance catalyst = inventory.getItemByObjectId(_catalystId);

    if (ItemFunctions.checkCatalyst(itemToEnchant, catalyst))
      activeChar.sendPacket(new ExPutEnchantSupportItemResult(1));
    else
      activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
  }
}