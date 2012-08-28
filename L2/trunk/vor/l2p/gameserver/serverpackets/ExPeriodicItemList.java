package l2p.gameserver.serverpackets;

public class ExPeriodicItemList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(135);
    writeD(0);
  }
}