package l2p.gameserver.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(182);
  }
}