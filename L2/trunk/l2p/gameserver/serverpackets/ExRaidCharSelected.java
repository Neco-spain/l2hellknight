package l2p.gameserver.serverpackets;

public class ExRaidCharSelected extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(181);
  }
}