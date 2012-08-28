package l2p.gameserver.serverpackets;

public class ExEventMatchLockResult extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(11);
  }
}