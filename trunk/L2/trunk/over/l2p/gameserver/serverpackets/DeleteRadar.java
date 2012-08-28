package l2p.gameserver.serverpackets;

public class DeleteRadar extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(184);
  }
}