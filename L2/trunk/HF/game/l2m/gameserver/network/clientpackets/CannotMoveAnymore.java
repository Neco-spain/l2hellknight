package l2m.gameserver.clientpackets;

import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.utils.Location;

public class CannotMoveAnymore extends L2GameClientPacket
{
  private Location _loc = new Location();

  protected void readImpl()
  {
    _loc.x = readD();
    _loc.y = readD();
    _loc.z = readD();
    _loc.h = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, _loc, null);
  }
}