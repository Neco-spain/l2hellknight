package l2m.gameserver.network.serverpackets;

public class MyTargetSelected extends L2GameServerPacket
{
  private int _objectId;
  private int _color;

  public MyTargetSelected(int objectId, int color)
  {
    _objectId = objectId;
    _color = color;
  }

  protected final void writeImpl()
  {
    writeC(185);
    writeD(_objectId);
    writeH(_color);
    writeD(0);
  }
}