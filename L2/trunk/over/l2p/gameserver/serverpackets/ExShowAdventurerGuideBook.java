package l2p.gameserver.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(56);
  }
}