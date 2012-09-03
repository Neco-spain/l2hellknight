package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.RecipeShopSellList;

/**
 * Возврат к списку из информации о рецепте
 */
public class RequestRecipeShopSellList extends L2GameClientPacket
{
	int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Player trader = L2ObjectsStorage.getPlayer(_objectId);
		if(trader != null)
			activeChar.sendPacket(new RecipeShopSellList(activeChar, trader));
		else
			activeChar.sendActionFailed();
	}
}