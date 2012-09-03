package l2rt.gameserver.network.serverpackets;

public class ExRequestChangeNicknameColor extends L2GameServerPacket
{
  private int _itemObjectId;

  public ExRequestChangeNicknameColor(int itemObjectId)
  {
	  this._itemObjectId = itemObjectId;
  }

  protected final void writeImpl()
  {
	  writeC(254);
	  writeH(131);
	  writeD(this._itemObjectId);
  }

  public String getType()
  {
	  return "[S] FE:83 ExRequestChangeNicknameColor";
  }
}