package l2p.gameserver.listener.actor.door.impl;

import l2p.gameserver.listener.actor.door.OnOpenCloseListener;
import l2p.gameserver.model.instances.DoorInstance;

public class MasterOnOpenCloseListenerImpl
  implements OnOpenCloseListener
{
  private DoorInstance _door;

  public MasterOnOpenCloseListenerImpl(DoorInstance door)
  {
    _door = door;
  }

  public void onOpen(DoorInstance doorInstance)
  {
    _door.openMe();
  }

  public void onClose(DoorInstance doorInstance)
  {
    _door.closeMe();
  }
}