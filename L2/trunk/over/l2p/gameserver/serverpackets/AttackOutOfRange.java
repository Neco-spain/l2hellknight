package l2p.gameserver.serverpackets;

public class AttackOutOfRange extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(2);
  }
}