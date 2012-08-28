package l2p.gameserver.serverpackets;

public class TradePressOwnOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(83);
  }
}