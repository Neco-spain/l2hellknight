package l2p.gameserver.clientpackets;

public class NetPing extends L2GameClientPacket
{
  private int unk;
  private int unk2;
  private int unk3;

  protected void runImpl()
  {
  }

  protected void readImpl()
  {
    unk = readD();
    unk2 = readD();
    unk3 = readD();
  }
}