package l2p.gameserver.serverpackets;

public class ExColosseumFenceInfo extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(3);
  }
}