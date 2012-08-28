package l2m.gameserver.network.serverpackets;

public class ExShowLines extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(165);
  }
}