package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExPutCommissionResultForVariationMake;
import l2rt.gameserver.templates.L2Item.Grade;

public class RequestConfirmGemStone extends L2GameClientPacket
{
	private static final int GEMSTONE_D = 2130;
	private static final int GEMSTONE_C = 2131;
	private static final int GEMSTONE_B = 2132;

	// format: (ch)dddd
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private long _gemstoneCount;

	@Override
	public void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readQ();
	}

	@Override
	public void runImpl()
	{
		if(_gemstoneCount <= 0)
			return;

		L2Player activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		L2ItemInstance gemstoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);

		if(targetItem == null || refinerItem == null || gemstoneItem == null)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		int gemstoneItemId = gemstoneItem.getItem().getItemId();
		if(gemstoneItemId != GEMSTONE_D && gemstoneItemId != GEMSTONE_C && gemstoneItemId != GEMSTONE_B)
		{
			activeChar.sendPacket(Msg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

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

		if(isAccessoryLifeStone)
			switch(itemGrade)
			{
				case C:
					if(_gemstoneCount != 200 || gemstoneItemId != GEMSTONE_D)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case B:
					if(_gemstoneCount != 300 || gemstoneItemId != GEMSTONE_D)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case A:
					if(_gemstoneCount != 200 || gemstoneItemId != GEMSTONE_C)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S:
					if(_gemstoneCount != 250 || gemstoneItemId != GEMSTONE_C)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S80:
					if(_gemstoneCount != 250 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S84:
					if(_gemstoneCount != 250 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R:
					if(_gemstoneCount != 500 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R95:
					if(_gemstoneCount != 1000 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R99:
					if(_gemstoneCount != 2000 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
			}
		else
			switch(itemGrade)
			{
				case C:
					if(_gemstoneCount != 20 || gemstoneItemId != GEMSTONE_D)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case B:
					if(_gemstoneCount != 30 || gemstoneItemId != GEMSTONE_D)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case A:
					if(_gemstoneCount != 20 || gemstoneItemId != GEMSTONE_C)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S:
					if(_gemstoneCount != 25 || gemstoneItemId != GEMSTONE_C)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S80:
					// Icarus
					if(targetItem.getItem().getCrystalCount() == 10394 && (_gemstoneCount != 36 || gemstoneItemId != GEMSTONE_B))
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					// Dynasty
					else if(_gemstoneCount != 28 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case S84:
					if(_gemstoneCount != 36 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R:
					if(_gemstoneCount != 72 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R95:
					if(_gemstoneCount != 144 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
				case R99:
					if(_gemstoneCount != 288 || gemstoneItemId != GEMSTONE_B)
					{
						activeChar.sendPacket(Msg.GEMSTONE_QUANTITY_IS_INCORRECT);
						return;
					}
					break;
			}

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_gemstoneItemObjId, _gemstoneCount), Msg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
}