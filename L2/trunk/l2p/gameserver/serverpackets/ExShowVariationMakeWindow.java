package l2p.gameserver.serverpackets;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExShowVariationMakeWindow();

  protected final void writeImpl()
  {
    writeEx(81);
  }
}