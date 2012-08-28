package l2p.gameserver.clientpackets;

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