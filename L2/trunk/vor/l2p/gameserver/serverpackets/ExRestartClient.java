package l2p.gameserver.serverpackets;

public class ExRestartClient extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(72);
  }
}