package l2m.gameserver.model.items.attachment;

import l2m.gameserver.model.Player;

public abstract interface PickableAttachment extends ItemAttachment
{
  public abstract boolean canPickUp(Player paramPlayer);

  public abstract void pickUp(Player paramPlayer);
}