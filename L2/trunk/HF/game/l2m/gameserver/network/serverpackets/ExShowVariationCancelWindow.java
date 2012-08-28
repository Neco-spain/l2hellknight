package l2m.gameserver.network.serverpackets;

public class ExShowVariationCancelWindow extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExShowVariationCancelWindow();

  protected final void writeImpl()
  {
    writeEx(82);
  }
}