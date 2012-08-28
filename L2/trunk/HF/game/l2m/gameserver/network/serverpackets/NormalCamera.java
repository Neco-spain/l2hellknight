package l2m.gameserver.serverpackets;

public class NormalCamera extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(215);
  }
}