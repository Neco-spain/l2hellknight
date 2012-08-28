package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.Warehouse;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
  private final ItemInstance[] _items;
  private String _charName;
  private long _charAdena;

  public GMViewWarehouseWithdrawList(Player cha)
  {
    _charName = cha.getName();
    _charAdena = cha.getAdena();
    _items = cha.getWarehouse().getItems();
  }

  protected final void writeImpl()
  {
    writeC(155);
    writeS(_charName);
    writeQ(_charAdena);
    writeH(_items.length);
    for (ItemInstance temp : _items)
    {
      writeItemInfo(temp);
      writeD(temp.getObjectId());
    }
  }
}