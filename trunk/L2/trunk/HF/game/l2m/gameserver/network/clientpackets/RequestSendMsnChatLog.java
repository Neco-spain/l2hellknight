package l2m.gameserver.network.clientpackets;

public class RequestSendMsnChatLog extends L2GameClientPacket
{
  private int unk3;
  private String unk;
  private String unk2;

  protected void runImpl()
  {
  }

  protected void readImpl()
  {
    unk = readS();
    unk2 = readS();
    unk3 = readD();
  }
}