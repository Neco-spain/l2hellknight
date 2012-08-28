package l2m.gameserver.network.clientpackets;

public class RequestSEKCustom extends L2GameClientPacket
{
  private int SlotNum;
  private int Direction;

  protected void readImpl()
  {
    SlotNum = readD();
    Direction = readD();
  }

  protected void runImpl()
  {
  }
}