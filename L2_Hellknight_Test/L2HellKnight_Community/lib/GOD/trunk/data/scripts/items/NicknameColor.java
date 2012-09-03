package items;
 
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ExRequestChangeNicknameColor;

public class NicknameColor implements IItemHandler
	{
	private static final int[] _itemIds = { 13021 };
	public void useItem(L2Playable playable, L2ItemInstance item, Boolean forceUse)
	{
		if(playable == null || !playable.isPlayer())
			return;
		int itemId = item.getItemId();
		if (itemId == 13021)
		{
		playable.sendPacket(new ExRequestChangeNicknameColor(item.getObjectId()));
		}
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
