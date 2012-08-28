package net.sf.l2j.gameserver.network.serverpackets;

public class ShowXMasSeal extends L2GameServerPacket
{
  private static final String _S__F2_SHOWXMASSEAL = "[S] F2 ShowXMasSeal";
  private int _item;

  public ShowXMasSeal(int item)
  {
    _item = item;
  }

  protected void writeImpl()
  {
    writeC(242);

    writeD(_item);
  }

  public String getType()
  {
    return "[S] F2 ShowXMasSeal";
  }
}