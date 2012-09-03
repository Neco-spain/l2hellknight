package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.L2Item.Grade;

public class RequestConfirmRefinerItem extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	private static final int GEMSTONE_B = 2132;

	// format: (ch)dd
	private int _targetItemObjId;
	private int _refinerItemObjId;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);

		if(targetItem == null || refinerItem == null)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		int refinerItemId = refinerItem.getItem().getItemId();

		boolean isAccessoryLifeStone = refinerItem.isAccessoryLifeStone();
		if(!targetItem.canBeAugmented(activeChar, isAccessoryLifeStone))
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(!isAccessoryLifeStone && !refinerItem.isLifeStone())
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		Grade itemGrade = targetItem.getItem().getItemGrade();

		int gemstoneCount = 0;
		int gemstoneItemId = 0;

		if(isAccessoryLifeStone)
			switch(itemGrade)
			{
				case C:
					gemstoneCount = 200;
					gemstoneItemId = GEMSTONE_D;
					break;
				case B:
					gemstoneCount = 300;
					gemstoneItemId = GEMSTONE_D;
					break;
				case A:
					gemstoneCount = 200;
					gemstoneItemId = GEMSTONE_C;
					break;
				case S:
					gemstoneCount = 250;
					gemstoneItemId = GEMSTONE_C;
					break;
				case S80:
					gemstoneCount = 250;
					gemstoneItemId = GEMSTONE_B;
					break;
				case S84:
					gemstoneCount = 250;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R:
					gemstoneCount = 500;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R95:
					gemstoneCount = 1000;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R99:
					gemstoneCount = 2000;
					gemstoneItemId = GEMSTONE_B;
					break;
			}
		else
			switch(itemGrade)
			{
				case C:
					gemstoneCount = 20;
					gemstoneItemId = GEMSTONE_D;
					break;
				case B:
					gemstoneCount = 30;
					gemstoneItemId = GEMSTONE_D;
					break;
				case A:
					gemstoneCount = 20;
					gemstoneItemId = GEMSTONE_C;
					break;
				case S:
					gemstoneCount = 25;
					gemstoneItemId = GEMSTONE_C;
					break;
				case S80:
					if(targetItem.getItem().getCrystalCount() == 10394) // Icarus
						gemstoneCount = 36;
					else
						gemstoneCount = 28;
					gemstoneItemId = GEMSTONE_B;
					break;
				case S84:
					gemstoneCount = 36;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R:
					gemstoneCount = 72;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R95:
					gemstoneCount = 144;
					gemstoneItemId = GEMSTONE_B;
					break;
				case R99:
					gemstoneCount = 288;
					gemstoneItemId = GEMSTONE_B;
					break;
			}

		SystemMessage sm = new SystemMessage(SystemMessage.REQUIRES_S1_S2).addNumber(gemstoneCount).addItemName(gemstoneItemId);
		activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(_refinerItemObjId, refinerItemId, gemstoneItemId, gemstoneCount), sm);
	}
}