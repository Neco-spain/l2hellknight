package l2p.gameserver.serverpackets;

public class ExPlayScene extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(92);
    writeD(0);
  }
}