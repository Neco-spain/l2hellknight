package l2rt.gameserver.model.items.listeners;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.items.Inventory;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.skills.funcs.Func;

public final class StatsListener implements PaperdollListener
{
	private Inventory _inv;

	public StatsListener(Inventory inv)
	{
		_inv = inv;
	}

	public void notifyUnequipped(int slot, L2ItemInstance item)
	{
		L2Character owner = _inv.getOwner();
		owner.removeStatsOwner(item);
		owner.updateStats();
		if(owner.getPet() != null)
		{
			owner.getPet().removeStatsOwner(item);
			owner.getPet().updateStats();
		}
	}

	public void notifyEquipped(int slot, L2ItemInstance item)
	{
		L2Character owner = _inv.getOwner();
		owner.addStatFuncs(item.getStatFuncs());
		owner.updateStats();
		if(owner.getPet() != null)
		{
            if(item.isWeapon() && item.getAttributeFuncTemplate().get(0) != null) {
                Func f = item.getAttributeFuncTemplate().get(0).getFunc(item);
                owner.getPet().addStatFunc(f);
            } else if (item.isArmor()) {
                for(int i=0; i<6; i++) {
                    if(item.getAttributeFuncTemplate().get(i) != null) {
                        Func f = item.getAttributeFuncTemplate().get(i).getFunc(item);
                        owner.getPet().addStatFunc(f);
                    }
                }
            }
            owner.getPet().updateStats();
		}
	}
}