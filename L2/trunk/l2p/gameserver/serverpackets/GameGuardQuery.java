package l2p.gameserver.serverpackets;

public class GameGuardQuery extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(116);
    writeD(0);
    writeD(0);
    writeD(0);
    writeD(0);
  }
}