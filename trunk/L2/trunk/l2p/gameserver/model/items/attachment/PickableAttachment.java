package l2p.gameserver.model.items.attachment;

import l2p.gameserver.model.Player;

public abstract interface PickableAttachment extends ItemAttachment
{
  public abstract boolean canPickUp(Player paramPlayer);

  public abstract void pickUp(Player paramPlayer);
}