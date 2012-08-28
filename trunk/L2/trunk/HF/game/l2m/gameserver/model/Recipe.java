package l2m.gameserver.model;

public class Recipe
{
  private RecipeComponent[] _recipes;
  private int _id;
  private int _level;
  private int _recipeId;
  private String _recipeName;
  private int _successRate;
  private int _mpCost;
  private int _itemId;
  private int _foundation;
  private int _count;
  private boolean _isdwarvencraft;
  private long _exp;
  private long _sp;

  public Recipe(int id, int level, int recipeId, String recipeName, int successRate, int mpCost, int itemId, int foundation, int count, long exp, long sp, boolean isdwarvencraft)
  {
    _id = id;
    _recipes = new RecipeComponent[0];
    _level = level;
    _recipeId = recipeId;
    _recipeName = recipeName;
    _successRate = successRate;
    _mpCost = mpCost;
    _itemId = itemId;
    _foundation = foundation;
    _count = count;
    _exp = exp;
    _sp = sp;
    _isdwarvencraft = isdwarvencraft;
  }

  public void addRecipe(RecipeComponent recipe)
  {
    int len = _recipes.length;
    RecipeComponent[] tmp = new RecipeComponent[len + 1];
    System.arraycopy(_recipes, 0, tmp, 0, len);
    tmp[len] = recipe;
    _recipes = tmp;
  }

  public int getId()
  {
    return _id;
  }

  public int getLevel()
  {
    return _level;
  }

  public int getRecipeId()
  {
    return _recipeId;
  }

  public String getRecipeName()
  {
    return _recipeName;
  }

  public int getSuccessRate()
  {
    return _successRate;
  }

  public int getMpCost()
  {
    return _mpCost;
  }

  public int getItemId()
  {
    return _itemId;
  }

  public int getCount()
  {
    return _count;
  }

  public RecipeComponent[] getRecipes()
  {
    return _recipes;
  }

  public boolean isDwarvenRecipe()
  {
    return _isdwarvencraft;
  }

  public long getExp()
  {
    return _exp;
  }

  public long getSp()
  {
    return _sp;
  }

  public int getFoundation()
  {
    return _foundation;
  }
}