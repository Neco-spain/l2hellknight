package l2m.gameserver.network.clientpackets;

public class RequestPrivateStoreList extends L2GameClientPacket
{
  private int unk;

  protected void readImpl()
  {
    unk = readD();
  }

  protected void runImpl()
  {
  }
}