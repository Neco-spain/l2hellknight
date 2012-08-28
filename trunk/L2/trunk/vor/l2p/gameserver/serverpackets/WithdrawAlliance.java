package l2p.gameserver.serverpackets;

public class WithdrawAlliance extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(171);
  }
}