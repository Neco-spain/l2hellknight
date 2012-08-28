package l2p.gameserver.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
  private int ItemID;

  public ChooseInventoryItem(int id)
  {
    ItemID = id;
  }

  protected final void writeImpl()
  {
    writeC(124);
    writeD(ItemID);
  }
}