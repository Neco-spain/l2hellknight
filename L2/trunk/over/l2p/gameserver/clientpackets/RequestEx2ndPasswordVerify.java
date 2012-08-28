package l2p.gameserver.clientpackets;

public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
  private String _password;

  protected void readImpl()
  {
    _password = readS();
  }

  protected void runImpl()
  {
  }
}