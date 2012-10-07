package l2p.gameserver.serverpackets;

import java.util.Collection;

import l2p.gameserver.model.Recipe;
import l2p.gameserver.model.Player;


public class RecipeBookItemList extends L2GameServerPacket
{
	private Collection<Recipe> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _currentMp;

	public RecipeBookItemList(Player player, boolean isDwarvenCraft)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_currentMp = (int) player.getCurrentMp();
		if(isDwarvenCraft)
			_recipes = player.getDwarvenRecipeBook();
		else
			_recipes = player.getCommonRecipeBook();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xdc);
		writeD(_isDwarvenCraft ? 0x00 : 0x01);
		writeD(_currentMp);

		writeD(_recipes.size());

		for(Recipe recipe : _recipes)
		{
			writeD(recipe.getId());
			writeD(1); //??
		}
	}
}