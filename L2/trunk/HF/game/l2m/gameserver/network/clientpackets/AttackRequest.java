package l2m.gameserver.clientpackets;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.PlayerAccess;
import l2m.gameserver.network.GameClient;

public class AttackRequest extends L2GameClientPacket
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
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setActive();

    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (!activeChar.getPlayerAccess().CanAttack)
    {
      activeChar.sendActionFailed();
      return;
    }

    GameObject target = activeChar.getVisibleObject(_objectId);
    if (target == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    if ((activeChar.getAggressionTarget() != null) && (activeChar.getAggressionTarget() != target) && (!activeChar.getAggressionTarget().isDead()))
    {
      activeChar.sendActionFailed();
      return;
    }

    if ((target.isPlayer()) && ((activeChar.isInBoat()) || (target.isInBoat())))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.getTarget() != target)
    {
      target.onAction(activeChar, _attackId == 1);
      return;
    }

    if ((target.getObjectId() != activeChar.getObjectId()) && (!activeChar.isInStoreMode()) && (!activeChar.isProcessingRequest()))
      target.onForcedAttack(activeChar, _attackId == 1);
  }
}