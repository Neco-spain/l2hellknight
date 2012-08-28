package l2m.gameserver.network.serverpackets;

public class AttackDeadTarget extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(4);
  }
}