package l2m.gameserver.network.serverpackets;

public class ExColosseumFenceInfo extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(3);
  }
}