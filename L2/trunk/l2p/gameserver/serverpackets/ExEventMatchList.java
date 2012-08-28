package l2p.gameserver.serverpackets;

public class ExEventMatchList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(13);
  }
}