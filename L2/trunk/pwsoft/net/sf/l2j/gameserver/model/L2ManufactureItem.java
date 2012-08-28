package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.RecipeController;

public class L2ManufactureItem
{
  private int _recipeId;
  private int _cost;
  private boolean _isDwarven;

  public L2ManufactureItem(int recipeId, int cost)
  {
    _recipeId = recipeId;
    _cost = cost;

    _isDwarven = RecipeController.getInstance().getRecipeById(_recipeId).isDwarvenRecipe();
  }

  public int getRecipeId()
  {
    return _recipeId;
  }

  public int getCost()
  {
    return _cost;
  }

  public boolean isDwarven()
  {
    return _isDwarven;
  }
}