package l2m.gameserver.network.clientpackets;

public class RequestExChangeName extends L2GameClientPacket
{
  protected void readImpl()
  {
    int unk1 = readD();

    String name = readS();

    int unk2 = readD();
  }

  protected void runImpl()
  {
  }
}