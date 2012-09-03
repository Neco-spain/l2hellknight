package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExChangeAttributeInfo;

public final class SendChangeAttributeTargetItem extends L2GameClientPacket
{
	int _crystalItemId,_objId,att;
	
	//2045372F   PUSH Engine.205A65B8                      ASCII "chdd"
	protected void readImpl()
	{
		_crystalItemId = readD(); 
		_objId = readD();
	}

	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objId);
		att = item.getAttackAttributeElement();
		switch (att)
		{
		case 0:
			att = 62;
			break;
		case 1:
			att = 61;
			break;
		case 2:
			att = 59;
			break;
		case 3:
			att = 55;
			break;
		case 4:
			att = 47;
			break;
		case 5:
			att = 31;
			break;
		}
		activeChar.sendPacket(new ExChangeAttributeInfo(item.getObjectId(),att));

	}
}