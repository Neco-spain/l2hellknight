package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestTargetCanceld extends L2GameClientPacket
{
  private static final String _C__37_REQUESTTARGETCANCELD = "[C] 37 RequestTargetCanceld";
  private int _unselect;

  protected void readImpl()
  {
    _unselect = readH();
  }

  protected void runImpl()
  {
    L2Character activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar != null)
    {
      if (_unselect == 0)
      {
        if ((activeChar.isCastingNow()) && (activeChar.canAbortCast()))
          activeChar.abortCast();
        else if (activeChar.getTarget() != null)
          activeChar.setTarget(null);
      }
      else if (activeChar.getTarget() != null)
        activeChar.setTarget(null);
    }
  }

  public String getType()
  {
    return "[C] 37 RequestTargetCanceld";
  }
}