package l2p.gameserver.serverpackets;

public class AttackinCoolTime extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(3);
  }
}