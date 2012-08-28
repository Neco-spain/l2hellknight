package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
  @SuppressWarnings("unused")
private static Logger _log = Logger.getLogger(RequestDismissPartyRoom.class.getName());
  @SuppressWarnings("unused")
private static final String _C__D0_02_REQUESTDISMISSPARTYROOM = "[C] D0:02 RequestDismissPartyRoom";
  @SuppressWarnings("unused")
private int _data1;
  @SuppressWarnings("unused")
private int _data2;

  protected void readImpl()
  {
    this._data1 = readD();
    this._data2 = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    //activeChar.delete(activeChar.getName());
    //activeChar.SetPartyFind(0);
    activeChar.broadcastUserInfo();
  }

  public String getType()
  {
    return "[C] D0:02 RequestDismissPartyRoom";
  }
}