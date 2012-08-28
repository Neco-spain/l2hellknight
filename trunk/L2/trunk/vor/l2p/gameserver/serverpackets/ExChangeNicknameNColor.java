package l2p.gameserver.serverpackets;

public class ExChangeNicknameNColor extends L2GameServerPacket
{
  private int _itemObjId;

  public ExChangeNicknameNColor(int itemObjId)
  {
    _itemObjId = itemObjId;
  }

  protected void writeImpl()
  {
    writeEx(131);
    writeD(_itemObjId);
  }
}