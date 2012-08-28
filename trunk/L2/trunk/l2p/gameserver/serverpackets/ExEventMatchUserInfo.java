package l2p.gameserver.serverpackets;

public class ExEventMatchUserInfo extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(2);
  }
}