package l2m.gameserver.network.serverpackets;

public class ExBR_RecentProductListPacket extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(220);
  }
}