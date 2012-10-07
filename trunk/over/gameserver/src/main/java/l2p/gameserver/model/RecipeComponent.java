package l2p.gameserver.model;

/**
 * This class describes a RecipeList componant (1 line of the recipe : Item-Quantity needed).<BR><BR>
 */
public class RecipeComponent
{
	/** The Identifier of the item needed in the L2RecipeInstance */
	private final int _itemId;

	/** The item quantity needed in the L2RecipeInstance */
	private final int _quantity;

	/**
	 * Constructor<?> of L2RecipeInstance (create a new line in a RecipeList).<BR><BR>
	 */
	public RecipeComponent(int itemId, int quantity)
	{
		_itemId = itemId;
		_quantity = quantity;
	}

	/**
	 * Return the Identifier of the L2RecipeInstance Item needed.<BR><BR>
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Return the Item quantity needed of the L2RecipeInstance.<BR><BR>
	 */
	public int getQuantity()
	{
		return _quantity;
	}
}
