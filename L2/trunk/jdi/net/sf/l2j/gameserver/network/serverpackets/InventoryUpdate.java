package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemInfo;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class InventoryUpdate extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(InventoryUpdate.class.getName());
  private static final String _S__37_INVENTORYUPDATE = "[S] 27 InventoryUpdate";
  private List<ItemInfo> _items;

  public InventoryUpdate()
  {
    _items = new FastList();
    if (Config.DEBUG)
    {
      showDebug();
    }
  }

  public InventoryUpdate(List<ItemInfo> items)
  {
    _items = items;
    if (Config.DEBUG)
    {
      showDebug();
    }
  }

  public void addItem(L2ItemInstance item) {
    if (item != null) _items.add(new ItemInfo(item)); 
  }
  public void addNewItem(L2ItemInstance item) { if (item != null) _items.add(new ItemInfo(item, 1));  } 
  public void addModifiedItem(L2ItemInstance item) {
    if (item != null) _items.add(new ItemInfo(item, 2)); 
  }
  public void addRemovedItem(L2ItemInstance item) { if (item != null) _items.add(new ItemInfo(item, 3));  } 
  public void addItems(List<L2ItemInstance> items) {
    if (items != null) for (L2ItemInstance item : items) if (item != null) _items.add(new ItemInfo(item)); 
  }

  private void showDebug()
  {
    for (ItemInfo item : _items)
    {
      _log.fine("oid:" + Integer.toHexString(item.getObjectId()) + " item:" + item.getItem().getName() + " last change:" + item.getChange());
    }
  }

  protected final void writeImpl()
  {
    writeC(39);
    int count = _items.size();
    writeH(count);
    for (ItemInfo item : _items)
    {
      writeH(item.getChange());
      writeH(item.getItem().getType1());

      writeD(item.getObjectId());
      writeD(item.getItem().getItemId());
      writeD(item.getCount());
      writeH(item.getItem().getType2());
      writeH(item.getCustomType1());
      writeH(item.getEquipped());
      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchant());
      writeH(item.getCustomType2());

      writeD(item.getAugemtationBoni());
      writeD(item.getMana());
    }
  }

  public String getType()
  {
    return "[S] 27 InventoryUpdate";
  }
}