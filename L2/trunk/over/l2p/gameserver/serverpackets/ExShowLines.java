package l2p.gameserver.serverpackets;

public class ExShowLines extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(165);
  }
}