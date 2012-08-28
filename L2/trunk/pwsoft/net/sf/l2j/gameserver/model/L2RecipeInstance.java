package net.sf.l2j.gameserver.model;

public class L2RecipeInstance
{
  private int _itemId;
  private int _quantity;

  public L2RecipeInstance(int itemId, int quantity)
  {
    _itemId = itemId;
    _quantity = quantity;
  }

  public int getItemId()
  {
    return _itemId;
  }

  public int getQuantity()
  {
    return _quantity;
  }
}