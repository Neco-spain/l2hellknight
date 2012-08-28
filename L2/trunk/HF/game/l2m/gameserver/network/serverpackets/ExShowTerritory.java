package l2m.gameserver.network.serverpackets;

public class ExShowTerritory extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(137);
  }
}