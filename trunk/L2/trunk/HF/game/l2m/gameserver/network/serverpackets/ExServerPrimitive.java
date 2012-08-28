package l2m.gameserver.network.serverpackets;

public class ExServerPrimitive extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(17);
  }
}