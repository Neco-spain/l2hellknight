package l2p.gameserver.serverpackets;

public class ExTutorialList extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(107);
  }
}