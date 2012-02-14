package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.network.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
	private int _id;

	/**
	 * packet type id 0xB7
	 * format:		cd
	 */
	@Override
	public void readImpl()
	{
		_id = readD();
	}

	@Override
	public void runImpl()
	{
		sendPacket(new RecipeItemMakeInfo(_id, getClient().getActiveChar(), 0xffffffff));
	}
}