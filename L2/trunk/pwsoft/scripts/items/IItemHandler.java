package scripts.items;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public abstract interface IItemHandler
{
  public abstract void useItem(L2PlayableInstance paramL2PlayableInstance, L2ItemInstance paramL2ItemInstance);

  public abstract int[] getItemIds();
}