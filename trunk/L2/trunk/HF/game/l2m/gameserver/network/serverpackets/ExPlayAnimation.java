package l2m.gameserver.network.serverpackets;

public class ExPlayAnimation extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(90);
  }
}