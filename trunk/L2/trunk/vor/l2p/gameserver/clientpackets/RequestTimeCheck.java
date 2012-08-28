package l2p.gameserver.clientpackets;

public class RequestTimeCheck extends L2GameClientPacket
{
  private int unk;
  private int unk2;

  protected void readImpl()
  {
    unk = readD();
    unk2 = readD();
  }

  protected void runImpl()
  {
  }
}