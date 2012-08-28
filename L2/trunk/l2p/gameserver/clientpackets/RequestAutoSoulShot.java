package l2p.gameserver.clientpackets;

import l2p.gameserver.handler.items.IItemHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExAutoSoulShot;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.templates.item.ItemTemplate;

public class RequestAutoSoulShot extends L2GameClientPacket
{
  private int _itemId;
  private boolean _type;

  protected void readImpl()
  {
    _itemId = readD();
    _type = (readD() == 1);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if ((activeChar.getPrivateStoreType() != 0) || (activeChar.isDead())) {
      return;
    }
    ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);

    if (item == null) {
      return;
    }
    if (_type)
    {
      activeChar.addAutoSoulShot(Integer.valueOf(_itemId));
      activeChar.sendPacket(new ExAutoSoulShot(_itemId, true));
      activeChar.sendPacket(new SystemMessage(1433).addString(item.getName()));
      IItemHandler handler = item.getTemplate().getHandler();
      handler.useItem(activeChar, item, false);
      return;
    }

    activeChar.removeAutoSoulShot(Integer.valueOf(_itemId));
    activeChar.sendPacket(new ExAutoSoulShot(_itemId, false));
    activeChar.sendPacket(new SystemMessage(1434).addString(item.getName()));
  }
}