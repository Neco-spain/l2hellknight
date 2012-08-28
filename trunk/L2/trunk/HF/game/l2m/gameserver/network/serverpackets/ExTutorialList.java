package l2m.gameserver.network.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(107);
  }
}