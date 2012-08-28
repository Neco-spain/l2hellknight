package l2p.gameserver.model.instances;

import l2p.commons.lang.reference.HardReference;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.reference.L2Reference;
import l2p.gameserver.serverpackets.MyTargetSelected;

public class ControlKeyInstance extends GameObject
{
  protected HardReference<ControlKeyInstance> reference;

  public ControlKeyInstance()
  {
    super(IdFactory.getInstance().getNextId());
    reference = new L2Reference(this);
  }

  public HardReference<ControlKeyInstance> getRef()
  {
    return reference;
  }

  public void onAction(Player player, boolean shift)
  {
    if (player.getTarget() != this)
    {
      player.setTarget(this);
      player.sendPacket(new MyTargetSelected(getObjectId(), 0));
      return;
    }

    player.sendActionFailed();
  }
}