package l2p.gameserver.serverpackets;

public class ExPVPMatchRecord extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeEx(126);
  }
}