package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class ItemList extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(ItemList.class.getName());
  private FastTable<L2ItemInstance> _items;
  private boolean _showWindow;

  public ItemList(L2PcInstance cha, boolean showWindow)
  {
    _items = new FastTable();
    _items.addAll(cha.getInventory().getAllItems());
    _showWindow = showWindow;
  }

  protected final void writeImpl()
  {
    writeC(27);
    writeH(_showWindow ? 1 : 0);

    writeH(_items.size());

    for (int i = _items.size() - 1; i > -1; i--)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(i);
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
      writeD(temp.getMana());
    }
  }

  public void gc()
  {
    _items.clear();
    _items = null;
  }
}