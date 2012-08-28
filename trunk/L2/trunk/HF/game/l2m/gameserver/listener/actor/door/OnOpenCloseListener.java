package l2m.gameserver.listener.actor.door;

import l2m.gameserver.listener.CharListener;
import l2m.gameserver.model.instances.DoorInstance;

public abstract interface OnOpenCloseListener extends CharListener
{
  public abstract void onOpen(DoorInstance paramDoorInstance);

  public abstract void onClose(DoorInstance paramDoorInstance);
}