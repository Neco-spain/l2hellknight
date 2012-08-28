package l2p.gameserver.serverpackets;

public class ExShowTerritory extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(137);
  }
}