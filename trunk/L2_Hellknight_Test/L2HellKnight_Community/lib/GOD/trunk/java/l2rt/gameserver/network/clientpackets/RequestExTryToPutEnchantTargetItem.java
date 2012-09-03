package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import l2rt.util.Log;

public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance itemToEnchant = inventory.getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getEnchantScroll();

		if(itemToEnchant == null || scroll == null)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			return;
		}

		// С помощью Master Yogi's Scroll: Enchant Weapon можно точить только Staff of Master Yogi
		if(scroll.getItemId() == 13540 && itemToEnchant.getItemId() != 13539 || itemToEnchant.getItemId() == 13539 && scroll.getItemId() != 13540)
		{
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}

		Log.add(activeChar.getName() + "|Trying to put enchant|" + itemToEnchant.getItemId() + "|+" + itemToEnchant.getEnchantLevel() + "|" + itemToEnchant.getObjectId(), "enchants");

		// Затычка, разрешающая точить Staff of Master Yogi
		if(!itemToEnchant.canBeEnchanted() && itemToEnchant.getItemId() != 13539 || itemToEnchant.isStackable())
		{
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			return;
		}

		if(itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.INVENTORY && itemToEnchant.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			return;
		}

		if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			activeChar.sendPacket(Msg.YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP);
			return;
		}

		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			activeChar.setEnchantScroll(null);
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			return;
		}

		int crystalId = itemToEnchant.getEnchantCrystalId(scroll, null);

		// Затычка, разрешающая точить Staff of Master Yogi
		if(crystalId == 0 && itemToEnchant.getItemId() != 13539)
		{
			activeChar.sendActionFailed();
			activeChar.sendPacket(Msg.DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL);
			return;
		}

		// Staff of Master Yogi можно точить до 23
		if(itemToEnchant.getItemId() != 13539 && itemToEnchant.getEnchantLevel() >= Config.ENCHANT_MAX || itemToEnchant.getItemId() == 13539 && itemToEnchant.getEnchantLevel() >= 23)
		{
			activeChar.sendActionFailed();
			activeChar.sendMessage(new CustomMessage("l2rt.gameserver.network.clientpackets.RequestEnchantItem.MaxLevel", activeChar));
			return;
		}

		// Запрет на заточку чужих вещей, баг может вылезти на серверных лагах
		if(itemToEnchant.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(new ExPutEnchantTargetItemResult(0, 0, 0));
			return;
		}

		activeChar.sendPacket(new ExPutEnchantTargetItemResult(1, 0, 0));
	}
}