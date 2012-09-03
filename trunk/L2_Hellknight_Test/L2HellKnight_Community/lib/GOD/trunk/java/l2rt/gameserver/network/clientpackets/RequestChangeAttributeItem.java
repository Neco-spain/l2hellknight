package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExChangeAttributeFail;
import l2rt.gameserver.network.serverpackets.ExChangeAttributeOk;

public class RequestChangeAttributeItem extends L2GameClientPacket
{
	private int _consumeItemId, _itemObjId, _newElementId;

	@Override
	public void readImpl()
	{
		_consumeItemId = readD(); 
		_itemObjId = readD(); 
		_newElementId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (_consumeItemId != 0 && _itemObjId != 0)
		{
			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
			if(item.getOwnerId() == activeChar.getObjectId() && activeChar.getInventory().getItemByItemId(_consumeItemId) != null)
			{
				activeChar.getInventory().destroyItem(_consumeItemId, 1, true);
				item.setAttributeElement((byte)_newElementId, item.getAttackElementValue(), new int[] {0,0,0,0,0,0}, true);
				activeChar.sendPacket(new ExChangeAttributeOk());
				return;
			}
		}
		activeChar.sendPacket(new ExChangeAttributeFail());
		
	}
}