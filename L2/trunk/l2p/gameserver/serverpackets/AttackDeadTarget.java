package l2p.gameserver.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(4);
  }
}