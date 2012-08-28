package l2m.gameserver.network.serverpackets;

public class DeleteRadar extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(184);
  }
}