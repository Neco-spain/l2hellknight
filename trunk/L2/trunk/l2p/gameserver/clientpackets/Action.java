package l2p.gameserver.clientpackets;

import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class Action extends L2GameClientPacket
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
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendActionFailed();
      return;
    }

    GameObject obj = activeChar.getVisibleObject(_objectId);
    if (obj == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.setActive();

    if ((activeChar.getAggressionTarget() != null) && (activeChar.getAggressionTarget() != obj))
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isLockedTarget())
    {
      if (activeChar.isClanAirShipDriver()) {
        activeChar.sendPacket(SystemMsg.THIS_ACTION_IS_PROHIBITED_WHILE_STEERING);
      }
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFrozen())
    {
      activeChar.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC });
      return;
    }

    obj.onAction(activeChar, _actionId == 1);
  }
}