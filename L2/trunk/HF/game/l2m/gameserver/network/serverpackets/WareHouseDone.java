package l2m.gameserver.serverpackets;

public class WareHouseDone extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(67);
    writeD(0);
  }
}