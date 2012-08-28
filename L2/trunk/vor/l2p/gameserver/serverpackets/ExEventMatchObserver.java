package l2p.gameserver.serverpackets;

public class ExEventMatchObserver extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(14);
  }
}