package l2m.gameserver.network.serverpackets;

public class ExRotation extends L2GameServerPacket
{
  private int _charObjId;
  private int _degree;

  public ExRotation(int charId, int degree)
  {
    _charObjId = charId;
    _degree = degree;
  }

  protected void writeImpl()
  {
    writeEx(193);
    writeD(_charObjId);
    writeD(_degree);
  }
}