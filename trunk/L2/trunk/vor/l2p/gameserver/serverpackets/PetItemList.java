package l2p.gameserver.serverpackets;

import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PetInventory;

public class PetItemList extends L2GameServerPacket
{
  private ItemInstance[] items;

  public PetItemList(PetInstance cha)
  {
    items = cha.getInventory().getItems();
  }

  protected final void writeImpl()
  {
    writeC(179);
    writeH(items.length);

    for (ItemInstance item : items)
      writeItemInfo(item);
  }
}