package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

public final class RequestWithdrawPartyRoom extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestWithdrawPartyRoom.class.getName());
  private static final String _C__D0_02_REQUESTWITHDRAWPARTYROOM = "[C] D0:02 RequestWithdrawPartyRoom";
  private int _data1;
  private int _data2;

  protected void readImpl()
  {
    _data1 = readD();
    _data2 = readD();
  }

  protected void runImpl()
  {
    _log.info("This packet is not well known : RequestWithdrawPartyRoom");
    _log.info("Data received: d:" + _data1 + " d:" + _data2);
  }

  public String getType()
  {
    return "[C] D0:02 RequestWithdrawPartyRoom";
  }
}