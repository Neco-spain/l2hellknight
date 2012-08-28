package l2p.gameserver.clientpackets;

public class RequestCreatePledge extends L2GameClientPacket
{
  private String _pledgename;

  protected void readImpl()
  {
    _pledgename = readS(64);
  }

  protected void runImpl()
  {
  }
}