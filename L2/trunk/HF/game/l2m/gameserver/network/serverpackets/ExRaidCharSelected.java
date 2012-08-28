package l2m.gameserver.network.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(181);
  }
}