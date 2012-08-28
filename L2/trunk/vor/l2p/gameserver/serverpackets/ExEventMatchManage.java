package l2p.gameserver.serverpackets;

public class ExEventMatchManage extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(48);
  }
}