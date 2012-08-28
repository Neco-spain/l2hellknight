package l2m.gameserver.network.serverpackets;

public class ExEventMatchList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(13);
  }
}