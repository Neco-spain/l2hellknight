package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class ItemList extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(ItemList.class.getName());
  private static final String _S__27_ITEMLIST = "[S] 1b ItemList";
  private L2ItemInstance[] _items;
  private boolean _showWindow;

  public ItemList(L2PcInstance cha, boolean showWindow)
  {
    _items = cha.getInventory().getItems();
    _showWindow = showWindow;
    if (Config.DEBUG)
    {
      showDebug();
    }
  }

  public ItemList(L2ItemInstance[] items, boolean showWindow)
  {
    _items = items;
    _showWindow = showWindow;
    if (Config.DEBUG)
    {
      showDebug();
    }
  }

  private void showDebug()
  {
    for (L2ItemInstance temp : _items)
    {
      _log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
    }
  }

  protected final void writeImpl()
  {
    writeC(27);
    writeH(_showWindow ? 1 : 0);

    int count = _items.length;
    writeH(count);

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
      else {
        writeD(0);
      }
      writeD(temp.getMana());
    }
  }

  public String getType()
  {
    return "[S] 1b ItemList";
  }
}