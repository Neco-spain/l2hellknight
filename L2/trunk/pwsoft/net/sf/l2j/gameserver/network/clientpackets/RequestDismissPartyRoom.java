package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestDismissPartyRoom.class.getName());
  private int _data1;
  private int _data2;

  protected void readImpl()
  {
    _data1 = readD();
    _data2 = readD();
  }

  protected void runImpl()
  {
    _log.info("This packet is not well known : RequestDismissPartyRoom");
  }
}