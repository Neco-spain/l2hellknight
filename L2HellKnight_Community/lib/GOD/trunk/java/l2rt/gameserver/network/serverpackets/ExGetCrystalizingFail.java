package l2rt.gameserver.network.serverpackets;

public class ExGetCrystalizingFail extends L2GameServerPacket
{
  private int reason;

  public ExGetCrystalizingFail(int id)
  {
    this.reason = id;
  }

  protected final void writeImpl()
  {
    writeC(EXTENDED_PACKET);
    writeH(0xe1);
    writeD(reason);
  }
}