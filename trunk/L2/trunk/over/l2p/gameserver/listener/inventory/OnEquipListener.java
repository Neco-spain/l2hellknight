package l2p.gameserver.listener.inventory;

import l2p.commons.listener.Listener;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.items.ItemInstance;

public abstract interface OnEquipListener extends Listener<Playable>
{
  public abstract void onEquip(int paramInt, ItemInstance paramItemInstance, Playable paramPlayable);

  public abstract void onUnequip(int paramInt, ItemInstance paramItemInstance, Playable paramPlayable);
}