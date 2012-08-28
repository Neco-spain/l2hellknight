package l2p.gameserver.serverpackets;

public class ServerObjectInfo extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(146);
  }
}