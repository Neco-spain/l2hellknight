package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class CannotMoveAnymore extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(CannotMoveAnymore.class.getName());
  private int _x;
  private int _y;
  private int _z;
  private int _heading;

  protected void readImpl()
  {
    _x = readD();
    _y = readD();
    _z = readD();
    _heading = readD();
  }

  protected void runImpl()
  {
    L2Character player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (player.getAI() != null)
      player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new L2CharPosition(_x, _y, _z, _heading));
  }
}