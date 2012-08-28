package l2m.gameserver.network.serverpackets;

public class ExGoodsInventoryChangedNotify extends L2GameServerPacket
{
  public static final L2GameServerPacket STATIC = new ExGoodsInventoryChangedNotify();

  protected void writeImpl()
  {
    writeEx(226);
  }
}