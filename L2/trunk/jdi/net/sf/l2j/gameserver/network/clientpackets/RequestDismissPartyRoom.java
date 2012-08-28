package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestDismissPartyRoom.class.getName());
  private static final String _C__D0_02_REQUESTDISMISSPARTYROOM = "[C] D0:02 RequestDismissPartyRoom";
  private int _data1;
  private int _data2;

  protected void readImpl()
  {
    _data1 = readD();
    _data2 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    activeChar.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] D0:02 RequestDismissPartyRoom";
  }
}