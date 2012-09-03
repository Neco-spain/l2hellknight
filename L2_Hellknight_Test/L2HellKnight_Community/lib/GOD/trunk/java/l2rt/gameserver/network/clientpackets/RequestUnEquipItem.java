package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.templates.L2Item;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private int _slot;

	/**
	 * packet type id 0x16
	 * format:		cd
	 */
	@Override
	public void readImpl()
	{
		_slot = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		// Нельзя снимать проклятое оружие и флаги
		if((_slot == L2Item.SLOT_R_HAND || _slot == L2Item.SLOT_L_HAND || _slot == L2Item.SLOT_LR_HAND) && (activeChar.isCursedWeaponEquipped() || activeChar.isCombatFlagEquipped() || activeChar.isTerritoryFlagEquipped()))
			return;

		activeChar.getInventory().unEquipItemInBodySlotAndNotify(_slot, null);
	}
}