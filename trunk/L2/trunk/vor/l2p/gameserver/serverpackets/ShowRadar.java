package l2p.gameserver.serverpackets;

public class ShowRadar extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(170);
  }
}