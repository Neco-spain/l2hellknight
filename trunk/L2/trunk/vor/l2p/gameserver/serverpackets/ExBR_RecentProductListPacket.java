package l2p.gameserver.serverpackets;

public class ExBR_RecentProductListPacket extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(220);
  }
}