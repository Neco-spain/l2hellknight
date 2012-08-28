package l2m.gameserver.network.clientpackets;

public class SnoopQuit extends L2GameClientPacket
{
  private int _snoopID;

  protected void readImpl()
  {
    _snoopID = readD();
  }

  protected void runImpl()
  {
  }
}