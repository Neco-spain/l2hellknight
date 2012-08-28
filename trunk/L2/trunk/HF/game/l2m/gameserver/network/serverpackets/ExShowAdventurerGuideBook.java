package l2m.gameserver.network.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeEx(56);
  }
}