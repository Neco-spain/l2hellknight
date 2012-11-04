package l2r.gameserver.network.clientpackets;

import l2r.gameserver.Config;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.items.ItemInstance;
import l2r.gameserver.network.serverpackets.ExUseSharedGroupItem;
import l2r.gameserver.network.serverpackets.SystemMessage2;
import l2r.gameserver.network.serverpackets.components.CustomMessage;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Events;
import l2r.gameserver.skills.TimeStamp;
import l2r.gameserver.tables.PetDataTable;

import org.apache.commons.lang3.ArrayUtils;

public class UseItem extends L2GameClientPacket
{
	private int _objectId;
	private boolean _ctrlPressed;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int itemId = item.getItemId();

		if(activeChar.isInStoreMode())
		{
			if(PetDataTable.isPetControlItem(item))
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_A_PRIVATE_STORE);
			else
				activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
			return;
		}

		if(activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
			return;
		}

		if(activeChar._event != null && activeChar._event.getName().equalsIgnoreCase("Capture The Flag") && (itemId == 13560 || itemId == 13561))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.l2.c2s.UseItem.NotUseIsEvents", activeChar));
			return;
		}
		//Запрещено использовать если чар флаганулся
		if(ArrayUtils.contains(Config.ITEM_USE_LIST_ID, itemId) && !Config.ITEM_USE_IS_COMBAT_FLAG && (activeChar.getPvpFlag() != 0 || activeChar.isInDuel() || activeChar.isInCombat()))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.l2.c2s.UseItem.NotUseIsFlag", activeChar));
			return;
		}
		
		//Запрещено использовать во время Ивентов
		if(ArrayUtils.contains(Config.ITEM_USE_LIST_ID, itemId) && !Config.ITEM_USE_IS_EVENTS && Events.onAction(activeChar, activeChar, true))
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.l2.c2s.UseItem.NotUseIsEvents", activeChar));
			return;
		}

		//Запрещено использовать если чар атакован
		if(ArrayUtils.contains(Config.ITEM_USE_LIST_ID, itemId) && !Config.ITEM_USE_IS_ATTACK && activeChar.isAttackingNow())
		{
			activeChar.sendMessage(new CustomMessage("l2r.gameserver.network.l2.c2s.UseItem.NotUseIsFlag", activeChar));
			return;
		}

		if(activeChar.isSharedGroupDisabled(item.getTemplate().getReuseGroup()))
		{
			activeChar.sendReuseMessage(item);
			return;
		}

		if(!item.getTemplate().testCondition(activeChar, item))
			return;

		if(activeChar.getInventory().isLockedItem(item))
			return;

		if(item.getTemplate().isForPet())
		{
			activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_EQUIP_A_PET_ITEM);
			return;
		}

		// Маги не могут вызывать Baby Buffalo Improved
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && activeChar.isMageClass() && item.getItemId() == 10311)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return;
		}

		// Войны не могут вызывать Improved Baby Kookaburra
		if(Config.ALT_IMPROVED_PETS_LIMITED_USE && !activeChar.isMageClass() && item.getItemId() == 10313)
		{
			activeChar.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		boolean success = item.getTemplate().getHandler().useItem(activeChar, item, _ctrlPressed);
		if(success)
		{
			long nextTimeUse = item.getTemplate().getReuseType().next(item);
			if(nextTimeUse > System.currentTimeMillis())
			{
				TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, item.getTemplate().getReuseDelay());
				activeChar.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);

				if(item.getTemplate().getReuseDelay() > 0)
					activeChar.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
			}
		}
	}
}