package l2p.gameserver.listener.actor.door;

import l2p.gameserver.listener.CharListener;
import l2p.gameserver.model.instances.DoorInstance;

public abstract interface OnOpenCloseListener extends CharListener
{
  public abstract void onOpen(DoorInstance paramDoorInstance);

  public abstract void onClose(DoorInstance paramDoorInstance);
}