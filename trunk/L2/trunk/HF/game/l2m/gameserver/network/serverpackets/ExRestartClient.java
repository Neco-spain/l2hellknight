package l2m.gameserver.network.serverpackets;

public class ExRestartClient extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(72);
  }
}