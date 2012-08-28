package l2m.gameserver.network.serverpackets;

public class ExPVPMatchRecord extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(126);
  }
}