package l2m.gameserver.network.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(89);
  }
}