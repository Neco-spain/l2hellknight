package l2p.gameserver.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(15);
  }
}