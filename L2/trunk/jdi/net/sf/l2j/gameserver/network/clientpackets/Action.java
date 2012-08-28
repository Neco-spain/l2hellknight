package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
  private static final String ACTION__C__04 = "[C] 04 Action";
  private static Logger _log = Logger.getLogger(Action.class.getName());
  private int _objectId;
  private int _originX;
  private int _originY;
  private int _originZ;
  private int _actionId;

  protected void readImpl()
  {
    _objectId = readD();
    _originX = readD();
    _originY = readD();
    _originZ = readD();
    _actionId = readC();
  }

  protected void runImpl()
  {
    if (Config.DEBUG) _log.fine("Action:" + _actionId);
    if (Config.DEBUG) _log.fine("oid:" + _objectId);

    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (activeChar.inObserverMode())
    {
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      return;
    }
    L2Object obj;
    L2Object obj;
    if (activeChar.getTargetId() == _objectId)
      obj = activeChar.getTarget();
    else {
      obj = L2World.getInstance().findObject(_objectId);
    }

    if (obj == null)
    {
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      return;
    }

    if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getActiveRequester() == null));
    switch (_actionId)
    {
    case 0:
      obj.onAction(activeChar);
      break;
    case 1:
      if (((obj instanceof L2Character)) && (((L2Character)obj).isAlikeDead()))
        obj.onAction(activeChar);
      else
        obj.onActionShift((L2GameClient)getClient());
      break;
    default:
      _log.warning("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      break;

      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
    }
  }

  public String getType()
  {
    return "[C] 04 Action";
  }
}