package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class AttackRequest extends L2GameClientPacket
{
  private int _objectId;
  private int _originX;
  private int _originY;
  private int _originZ;
  private int _attackId;

  protected void readImpl()
  {
    _objectId = readD();
    _originX = readD();
    _originY = readD();
    _originZ = readD();
    _attackId = readC();
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

    if ((player.isDead()) || (player.isAlikeDead()) || (player.isFakeDeath())) {
      player.sendActionFailed();
      return;
    }
    L2Object target;
    L2Object target;
    if (player.getTargetId() == _objectId) {
      target = player.getTarget();
    } else {
      target = L2World.getInstance().findObject(_objectId);
      if (target == null) {
        target = L2World.getInstance().getPlayer(_objectId);
      }

    }

    if ((target == null) || (!player.canTarget(target))) {
      player.sendActionFailed();
      return;
    }

    if (player.getTarget() != target) {
      target.onAction(player);
      return;
    }

    if (target.isInZonePeace()) {
      player.sendActionFailed();
      return;
    }

    if ((target.getObjectId() != player.getObjectId()) && (player.getPrivateStoreType() == 0) && (player.getTransactionRequester() == null))
    {
      target.onForcedAttack(player);
    }
    else player.sendActionFailed();
  }
}