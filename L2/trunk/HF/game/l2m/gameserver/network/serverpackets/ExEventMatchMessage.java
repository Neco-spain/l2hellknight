package l2m.gameserver.network.serverpackets;

public class ExEventMatchMessage extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(15);
  }
}