package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ItemInfo;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PetInventoryUpdate extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(InventoryUpdate.class.getName());
  private static final String _S__37_INVENTORYUPDATE = "[S] b3 InventoryUpdate";
  private List<ItemInfo> _items;

  public PetInventoryUpdate(List<ItemInfo> items)
  {
    _items = items;
    if (Config.DEBUG)
    {
      showDebug();
    }
  }

  public PetInventoryUpdate()
  {
    this(new FastList());
  }
  public void addItem(L2ItemInstance item) {
    _items.add(new ItemInfo(item)); } 
  public void addNewItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 1)); } 
  public void addModifiedItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 2)); } 
  public void addRemovedItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 3));
  }

  public void addItems(List<L2ItemInstance> items)
  {
    L2ItemInstance item;
    for (Iterator i$ = items.iterator(); i$.hasNext(); _items.add(new ItemInfo(item))) item = (L2ItemInstance)i$.next(); 
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
    writeC(179);
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
      writeH(0);
      writeH(item.getEquipped());

      writeD(item.getItem().getBodyPart());
      writeH(item.getEnchant());
      writeH(0);
    }
  }

  public String getType()
  {
    return "[S] b3 InventoryUpdate";
  }
}