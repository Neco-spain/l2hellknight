package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class GMViewItemList extends L2GameServerPacket
{
  private static final String _S__AD_GMVIEWITEMLIST = "[S] 94 GMViewItemList";
  private L2ItemInstance[] _items;
  private L2PcInstance _cha;
  private String _playerName;

  public GMViewItemList(L2PcInstance cha)
  {
    _items = cha.getInventory().getItems();
    _playerName = cha.getName();
    _cha = cha;
  }

  protected final void writeImpl()
  {
    writeC(148);
    writeS(_playerName);
    writeD(_cha.GetInventoryLimit());
    writeH(1);
    writeH(_items.length);

    for (L2ItemInstance temp : _items)
    {
      if ((temp == null) || (temp.getItem() == null)) {
        continue;
      }
      writeH(temp.getItem().getType1());

      writeD(temp.getObjectId());
      writeD(temp.getItemId());
      writeD(temp.getCount());
      writeH(temp.getItem().getType2());
      writeH(temp.getCustomType1());
      writeH(temp.isEquipped() ? 1 : 0);
      writeD(temp.getItem().getBodyPart());
      writeH(temp.getEnchantLevel());
      writeH(temp.getCustomType2());
      if (temp.isAugmented())
        writeD(temp.getAugmentation().getAugmentationId());
      else
        writeD(0);
      writeD(-1);
    }
  }

  public String getType()
  {
    return "[S] 94 GMViewItemList";
  }
}