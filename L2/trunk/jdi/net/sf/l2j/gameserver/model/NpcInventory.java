package net.sf.l2j.gameserver.model;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class NpcInventory extends Inventory
{
  public static final int ADENA_ID = 57;
  public static final int ANCIENT_ADENA_ID = 5575;
  private final L2NpcInstance _owner;
  public boolean sshotInUse = false;
  public boolean bshotInUse = false;

  public NpcInventory(L2NpcInstance owner)
  {
    _owner = owner;
  }

  public void reset()
  {
    destroyAllItems("Reset", null, null);
    if (_owner.getTemplate().ss > 0)
      addItem("Reset", 1835, _owner.getTemplate().ss, null, null);
    if (_owner.getTemplate().bss > 0)
      addItem("Reset", 3947, _owner.getTemplate().bss, null, null);
  }

  public L2NpcInstance getOwner() {
    return _owner;
  }
  protected L2ItemInstance.ItemLocation getBaseLocation() { return L2ItemInstance.ItemLocation.NPC; } 
  protected L2ItemInstance.ItemLocation getEquipLocation() {
    return L2ItemInstance.ItemLocation.NPC;
  }

  public L2ItemInstance[] getAllItemsByItemId(int itemId)
  {
    List list = new FastList();
    for (L2ItemInstance item : _items)
    {
      if (item.getItemId() == itemId) {
        list.add(item);
      }
    }
    return (L2ItemInstance[])list.toArray(new L2ItemInstance[list.size()]);
  }

  public void refreshWeight()
  {
  }

  public void restore()
  {
  }
}