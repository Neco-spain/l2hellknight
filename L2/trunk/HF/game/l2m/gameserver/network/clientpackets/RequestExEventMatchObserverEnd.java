package l2m.gameserver.network.clientpackets;

public class RequestExEventMatchObserverEnd extends L2GameClientPacket
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