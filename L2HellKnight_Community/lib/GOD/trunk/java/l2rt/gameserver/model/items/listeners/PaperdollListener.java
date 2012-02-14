package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.model.items.L2ItemInstance;

public interface PaperdollListener
{
	public void notifyEquipped(int slot, L2ItemInstance inst);

	public void notifyUnequipped(int slot, L2ItemInstance inst);
}
