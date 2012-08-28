package l2m.gameserver.network.serverpackets;

public class AttackOutOfRange extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(2);
  }
}