package net.sf.l2j.gameserver.network.clientpackets;

public final class RequestOustFromPartyRoom extends L2GameClientPacket
{
  private static final String _C__D0_01_REQUESTOUSTFROMPARTYROOM = "[C] D0:01 RequestOustFromPartyRoom";
  private int _id;

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
  }

  public String getType()
  {
    return "[C] D0:01 RequestOustFromPartyRoom";
  }
}