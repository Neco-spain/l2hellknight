package l2p.gameserver.serverpackets;

public class ExEventMatchCreate extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(29);
  }
}