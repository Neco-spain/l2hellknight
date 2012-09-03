package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SSQStatus;

public class SevenSignsRecord implements IItemHandler, ScriptFile
{
	private static final int[] _itemIds = { 5707 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		/*if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		player.sendPacket(new SSQStatus(player, 1));*/
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}