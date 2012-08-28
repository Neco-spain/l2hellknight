package l2m.gameserver.network.serverpackets;

public class ExRaidReserveResult extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(182);
  }
}