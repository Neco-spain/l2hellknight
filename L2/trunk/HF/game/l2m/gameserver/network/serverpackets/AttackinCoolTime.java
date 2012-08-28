package l2m.gameserver.network.serverpackets;

public class AttackinCoolTime extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(3);
  }
}