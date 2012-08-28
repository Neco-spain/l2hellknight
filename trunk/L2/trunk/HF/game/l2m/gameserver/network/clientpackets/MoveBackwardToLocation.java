package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ActionFail;
import l2m.gameserver.network.serverpackets.CharMoveToLocation;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.utils.Location;

public class MoveBackwardToLocation extends L2GameClientPacket
{
  private Location _targetLoc = new Location();
  private Location _originLoc = new Location();
  private int _moveMovement;

  protected void readImpl()
  {
    _targetLoc.x = readD();
    _targetLoc.y = readD();
    _targetLoc.z = readD();
    _originLoc.x = readD();
    _originLoc.y = readD();
    _originLoc.z = readD();
    if (_buf.hasRemaining())
      _moveMovement = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setActive();

    if (System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY)
    {
      activeChar.sendActionFailed();
      return;
    }

    activeChar.setLastMovePacket();

    if (activeChar.isTeleporting())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFrozen())
    {
      activeChar.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC });
      return;
    }

    if (activeChar.isInObserverMode())
    {
      if (activeChar.getOlympiadObserveGame() == null)
        activeChar.sendActionFailed();
      else
        activeChar.sendPacket(new CharMoveToLocation(activeChar.getObjectId(), _originLoc, _targetLoc));
      return;
    }

    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.getTeleMode() > 0)
    {
      if (activeChar.getTeleMode() == 1)
        activeChar.setTeleMode(0);
      activeChar.sendActionFailed();
      activeChar.teleToLocation(_targetLoc);
      return;
    }

    if (activeChar.isInFlyingTransform()) {
      _targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z));
    }
    activeChar.moveToLocation(_targetLoc, 0, (_moveMovement != 0) && (!activeChar.getVarB("no_pf")));
  }
}