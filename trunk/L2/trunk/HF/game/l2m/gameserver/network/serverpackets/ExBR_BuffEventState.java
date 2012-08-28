package l2m.gameserver.network.serverpackets;

public class ExBR_BuffEventState extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(219);
  }
}