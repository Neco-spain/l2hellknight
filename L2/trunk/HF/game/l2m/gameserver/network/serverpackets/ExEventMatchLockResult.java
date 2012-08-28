package l2m.gameserver.network.serverpackets;

public class ExEventMatchLockResult extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(11);
  }
}