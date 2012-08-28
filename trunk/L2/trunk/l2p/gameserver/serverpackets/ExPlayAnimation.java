package l2p.gameserver.serverpackets;

public class ExPlayAnimation extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(90);
  }
}