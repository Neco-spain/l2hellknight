package l2m.gameserver.serverpackets;

public class WithdrawAlliance extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(171);
  }
}