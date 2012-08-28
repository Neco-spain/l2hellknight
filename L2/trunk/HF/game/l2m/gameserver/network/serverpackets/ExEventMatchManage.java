package l2m.gameserver.network.serverpackets;

public class ExEventMatchManage extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(48);
  }
}