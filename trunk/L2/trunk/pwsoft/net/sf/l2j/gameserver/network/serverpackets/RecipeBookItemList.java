package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2RecipeList;

public class RecipeBookItemList extends L2GameServerPacket
{
  private L2RecipeList[] _recipes;
  private boolean _isDwarvenCraft;
  private int _maxMp;

  public RecipeBookItemList(boolean isDwarvenCraft, int maxMp)
  {
    _isDwarvenCraft = isDwarvenCraft;
    _maxMp = maxMp;
  }

  public void addRecipes(L2RecipeList[] recipeBook)
  {
    _recipes = recipeBook;
  }

  protected final void writeImpl()
  {
    writeC(214);

    writeD(_isDwarvenCraft ? 0 : 1);
    writeD(_maxMp);

    if (_recipes == null)
    {
      writeD(0);
    }
    else
    {
      writeD(_recipes.length);

      for (int i = 0; i < _recipes.length; i++)
      {
        L2RecipeList temp = _recipes[i];
        writeD(temp.getId());
        writeD(i + 1);
      }
    }
  }

  public String getType()
  {
    return "S.RecipeBookItemList";
  }
}