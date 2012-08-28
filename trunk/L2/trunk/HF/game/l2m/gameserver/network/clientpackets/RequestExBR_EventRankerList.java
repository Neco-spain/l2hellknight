package l2m.gameserver.network.clientpackets;

public class RequestExBR_EventRankerList extends L2GameClientPacket
{
  private int unk;
  private int unk2;
  private int unk3;

  protected void readImpl()
  {
    unk = readD();
    unk2 = readD();
    unk3 = readD();
  }

  protected void runImpl()
  {
  }
}