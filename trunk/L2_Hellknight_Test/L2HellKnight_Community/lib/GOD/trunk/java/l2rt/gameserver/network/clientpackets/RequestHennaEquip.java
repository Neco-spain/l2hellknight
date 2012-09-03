package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2HennaInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.HennaTable;
import l2rt.gameserver.tables.HennaTreeTable;
import l2rt.gameserver.templates.L2Henna;

public class RequestHennaEquip extends L2GameClientPacket
{
	private int _symbolId;

	/**
	 * packet type id 0x6F
	 * format:		cd
	 */
	@Override
	public void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if(template == null)
			return;

		L2HennaInstance temp = new L2HennaInstance(template);

		boolean cheater = true;
		for(L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId(), activeChar.getSex()))
			if(h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}

		if(cheater)
		{
			activeChar.sendPacket(Msg.THE_SYMBOL_CANNOT_BE_DRAWN);
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		L2ItemInstance item = inventory.getItemByItemId(temp.getItemIdDye());
		if(item != null && item.getCount() >= temp.getAmountDyeRequire() && activeChar.getAdena() >= temp.getPrice() && activeChar.addHenna(temp))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addString(temp.getName()), Msg.THE_SYMBOL_HAS_BEEN_ADDED);
			inventory.reduceAdena(temp.getPrice());
			if(inventory.destroyItemByItemId(temp.getItemIdDye(), temp.getAmountDyeRequire(), true) == null)
				_log.info("RequestHennaEquip[50]: Item not found!!!");
		}
		else
			activeChar.sendPacket(Msg.THE_SYMBOL_CANNOT_BE_DRAWN);
	}
}