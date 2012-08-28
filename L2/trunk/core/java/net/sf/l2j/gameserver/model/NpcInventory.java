package net.sf.l2j.gameserver.model;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

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
		this.destroyAllItems("Reset", null, null);
		if (_owner.getTemplate().ss > 0)
			this.addItem("Reset", 1835, _owner.getTemplate().ss, null, null);
		if (_owner.getTemplate().bss > 0)
			this.addItem("Reset", 3947, _owner.getTemplate().bss, null, null);
	}
	
	@Override
	public L2NpcInstance getOwner() { return _owner; }
	@Override
	protected ItemLocation getBaseLocation() { return ItemLocation.NPC; }
	@Override
	protected ItemLocation getEquipLocation() { return ItemLocation.NPC; }

	/**
	 * Returns the list of all items in inventory that have a given item id.
	 * @return L2ItemInstance[] : matching items from inventory
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		List<L2ItemInstance> list = new FastList<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
				list.add(item);
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	public void refreshWeight()
	{
		// not needed
	}

	/**
	 * Get back items in inventory from database
	 */
    @Override
	public void restore()
    {
    	// not needed
    }
	
}
