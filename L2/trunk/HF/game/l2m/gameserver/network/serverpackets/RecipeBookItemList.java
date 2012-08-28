package l2m.gameserver.serverpackets;

import java.util.Collection;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Recipe;

public class RecipeBookItemList extends L2GameServerPacket
{
  private Collection<Recipe> _recipes;
  private final boolean _isDwarvenCraft;
  private final int _currentMp;

  public RecipeBookItemList(Player player, boolean isDwarvenCraft)
  {
    _isDwarvenCraft = isDwarvenCraft;
    _currentMp = (int)player.getCurrentMp();
    if (isDwarvenCraft)
      _recipes = player.getDwarvenRecipeBook();
    else
      _recipes = player.getCommonRecipeBook();
  }

  protected final void writeImpl()
  {
    writeC(220);
    writeD(_isDwarvenCraft ? 0 : 1);
    writeD(_currentMp);

    writeD(_recipes.size());

    for (Recipe recipe : _recipes)
    {
      writeD(recipe.getId());
      writeD(1);
    }
  }
}