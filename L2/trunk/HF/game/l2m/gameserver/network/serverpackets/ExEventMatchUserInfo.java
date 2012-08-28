package l2m.gameserver.network.serverpackets;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(2);
  }
}