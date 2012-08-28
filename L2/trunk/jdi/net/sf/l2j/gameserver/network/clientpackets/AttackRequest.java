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
  private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";

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
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;
    L2Object target;
    L2Object target;
    if (activeChar.getTargetId() == _objectId)
      target = activeChar.getTarget();
    else
      target = L2World.getInstance().findObject(_objectId);
    if (target == null) return;

    if (activeChar.getTarget() != target)
    {
      target.onAction(activeChar);
    }
    else if ((target.getObjectId() != activeChar.getObjectId()) && (activeChar.getPrivateStoreType() == 0) && (activeChar.getActiveRequester() == null))
    {
      target.onForcedAttack(activeChar);
    }
  }

  public String getType()
  {
    return "[C] 0A AttackRequest";
  }
}