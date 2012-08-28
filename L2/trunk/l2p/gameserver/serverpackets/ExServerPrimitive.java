package l2p.gameserver.serverpackets;

public class ExServerPrimitive extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(17);
  }
}