package net.sf.l2j.gameserver.network.serverpackets;

public class MyTargetSelected extends L2GameServerPacket
{
  private static final String _S__BF_MYTARGETSELECTED = "[S] a6 MyTargetSelected";
  private int _objectId;
  private int _color;

  public MyTargetSelected(int objectId, int color)
  {
    _objectId = objectId;
    _color = color;
  }

  protected final void writeImpl()
  {
    writeC(166);
    writeD(_objectId);
    writeH(_color);
  }

  public String getType()
  {
    return "[S] a6 MyTargetSelected";
  }
}