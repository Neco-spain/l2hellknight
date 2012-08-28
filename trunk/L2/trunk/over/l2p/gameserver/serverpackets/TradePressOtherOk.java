package l2p.gameserver.serverpackets;

public class TradePressOtherOk extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new TradePressOtherOk();

  protected final void writeImpl()
  {
    writeC(130);
  }
}