package l2p.gameserver.serverpackets;

public class ExDuelEnemyRelation extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(89);
  }
}