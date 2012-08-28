package l2m.gameserver.network.serverpackets;

public class ExEventMatchObserver extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(14);
  }
}