package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.templates.item.ItemTemplate;

public class ExBR_AgathionEnergyInfo extends L2GameServerPacket
{
  private int _size;
  private ItemInstance[] _itemList = null;

  public ExBR_AgathionEnergyInfo(int size, ItemInstance[] item)
  {
    _itemList = item;
    _size = size;
  }

  protected void writeImpl()
  {
    writeEx(222);
    writeD(_size);
    for (ItemInstance item : _itemList)
    {
      if (item.getTemplate().getAgathionEnergy() == 0)
        continue;
      writeD(item.getObjectId());
      writeD(item.getItemId());
      writeD(2097152);
      writeD(item.getAgathionEnergy());
      writeD(item.getTemplate().getAgathionEnergy());
    }
  }
}