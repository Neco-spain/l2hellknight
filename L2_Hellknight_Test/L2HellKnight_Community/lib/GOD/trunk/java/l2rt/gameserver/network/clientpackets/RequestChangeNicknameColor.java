package l2rt.gameserver.network.clientpackets;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;

public class RequestChangeNicknameColor extends L2GameClientPacket
{
	private static final int[] COLORS = { 9671679, 8145404, 9959676, 16423662, 16735635, 64672, 10528257, 7903407, 4743829, 10066329 };
	private int _colorNum;
	private int _itemObjectId;
	private String _title;

	protected void readImpl()
	{
     this._colorNum = readD();
     this._title = readS();
     this._itemObjectId = readD();
	}

	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if (activeChar == null) 
		{
			return;
		}
		if ((this._colorNum < 0) || (this._colorNum >= COLORS.length)) {
			return;
		}
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(this._itemObjectId);
		if ((item == null) || (item.getItem() == null))
		{
			return;
		}
		if (activeChar.getInventory().destroyItem(item, 0, false) != null)
		{
			activeChar.setTitle(this._title);
			activeChar.setTitleColor(COLORS[this._colorNum]);
			activeChar.broadcastUserInfo(true);
			Functions.removeItem(activeChar, item.getItemId(), 1);
		}
 }

  public String getType()
  {
	  return "[C] D0:4F RequestChangeNicknameColor";
  }
}