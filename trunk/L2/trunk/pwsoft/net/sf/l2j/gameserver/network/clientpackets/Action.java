package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class Action extends L2GameClientPacket
{
  private int _objectId;
  private int _actionId;

  protected void readImpl()
  {
    _objectId = readD();

    readD();
    readD();
    readD();
    _actionId = readC();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }

    if ((player.isOutOfControl()) || (player.isParalyzed())) {
      player.sendActionFailed();
      return;
    }

    if (player.inObserverMode()) {
      player.sendActionFailed();
      return;
    }

    if (player.getPrivateStoreType() != 0) {
      player.sendActionFailed();
      return;
    }
    L2Object obj;
    L2Object obj;
    if (player.getTargetId() == _objectId) {
      obj = player.getTarget();
    } else {
      obj = L2World.getInstance().findObject(_objectId);
      if (obj == null) {
        obj = L2World.getInstance().getPlayer(_objectId);
      }

    }

    if ((obj == null) || (!player.canTarget(obj))) {
      player.sendActionFailed();
      return;
    }

    player.clearNextLoc();
    switch (_actionId) {
    case 0:
      obj.onAction(player);
      break;
    case 1:
      if (obj.isPlayer())
        obj.getPlayer().onActionShift(player);
      else if ((obj.isL2Character()) && (obj.isAlikeDead()))
        obj.onAction(player);
      else {
        obj.onActionShift((L2GameClient)getClient());
      }
      break;
    default:
      player.sendActionFailed();
    }
  }
}