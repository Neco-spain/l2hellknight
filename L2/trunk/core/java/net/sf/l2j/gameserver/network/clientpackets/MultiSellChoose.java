package net.sf.l2j.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.Util;

public class MultiSellChoose extends L2GameClientPacket
{
    private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
    private static Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
    private int _listId;
    private int _entryId;
    private int _amount;
    private int _enchantment;
    private int _transactionTax;	// local handling of taxation
	private int _enchantCheck = 0;
	
    @Override
	protected void readImpl()
    {
        _listId = readD();
        _entryId = readD();
        _amount = readD();
        // _enchantment = readH();  // <---commented this line because it did NOT work!
        _enchantment = _entryId % 100000;
        _entryId = _entryId / 100000;
        _transactionTax = 0;	// initialize tax amount to 0...
    }

    @Override
	public void runImpl()
    {
    	if(_amount < 1 || _amount > Config.MAX_MULTISELL)
    		return;

        MultiSellListContainer list = L2Multisell.getInstance().getList(_listId);
        if(list == null) return;

        L2PcInstance player = getClient().getActiveChar();
        if(player == null) return;

        for(MultiSellEntry entry : list.getEntries())
        {
            if(entry.getEntryId() == _entryId)
            {
            	doExchange(player,entry,list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment, list.getBidNpcId(), list.getListId());
            	return;
            }
        }
    }

    @SuppressWarnings("deprecation")
	private void doExchange(L2PcInstance player, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment, String bidNpcId, int bbmult)
    {
    	PcInventory inv = player.getInventory();
    	boolean maintainItemFound = false;  
    	if (!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_MULTISELL))
    	{
    		player.sendMessage("You can buy every 1 Second!.");
    		return;
    	}
    	boolean _bbmult = Config.COMMUN_MULT_LIST.contains(bbmult);
        // given the template entry and information about maintaining enchantment and applying taxes
        // re-create the instance of the entry that will be used for this exchange
    	// i.e. change the enchantment level of select ingredient/products and adena amount appropriately.
    	 L2NpcInstance merchant = null;
    	    if (!_bbmult)
    	    {
    	      merchant = (player.getTarget() instanceof L2NpcInstance)? (L2NpcInstance) player.getTarget() : null;
    	      if (merchant == null)
    	        return;
    	    }

        MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);

		for(MultiSellIngredient e : entry.getProducts())
    	{
			if (!ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable() && _amount > (player.GetInventoryLimit() - player.getInventory().getSize()))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
    	}

		if (bidNpcId != null)
		{
			List<Integer> Result = new FastList<Integer>();
			for (String id : bidNpcId.split(","))
			{
				Result.add(Integer.parseInt(id));
			}
			if(!Result.contains(merchant.getNpcId()))
			{
				Util.handleIllegalPlayerAction(player,"Player "+player.getName()+" tried to use multisell Exploit!", Config.DEFAULT_PUNISH);
				return;
			
			}
		}

        // Generate a list of distinct ingredients and counts in order to check if the correct item-counts
        // are possessed by the player
    	FastList<MultiSellIngredient> _ingredientsList = new FastList<MultiSellIngredient>();
    	boolean newIng = true;
    	for(MultiSellIngredient e: entry.getIngredients())
    	{
    		newIng = true;

    		// at this point, the template has already been modified so that enchantments are properly included
    		// whenever they need to be applied.  Uniqueness of items is thus judged by item id AND enchantment level
    		for(MultiSellIngredient ex: _ingredientsList)
    		{
    			// if the item was already added in the list, merely increment the count
    			// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
    			if( (ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel()) )
    			{
				if ((double)ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE) {
            				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                			_ingredientsList.clear();
                			_ingredientsList = null;
                			return;
				}
    				ex.setItemCount(ex.getItemCount() + e.getItemCount());
    				newIng = false;
    			}
    		}
    		if(newIng)
    		{
    			
    			if (maintainEnchantment || e.getMantainIngredient()) 
    				 { 
    				    maintainItemFound = true; 
    				 } 
    			// if it's a new ingredient, just store its info directly (item id, count, enchantment)
    			_ingredientsList.add(L2Multisell.getInstance().new MultiSellIngredient(e));
    		}
    	}
    	if (!maintainItemFound) 
    	   { 
    	           for (MultiSellIngredient product : entry.getProducts()) 
                { 
    	              product.setEnchantmentLevel(0); 
    	        } 
           } 
    	// now check if the player has sufficient items in the inventory to cover the ingredients' expences
    	for(MultiSellIngredient e : _ingredientsList)
    	{
    		if((double)e.getItemCount() * _amount > Integer.MAX_VALUE )
            {
            	player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                _ingredientsList.clear();
                _ingredientsList = null;
                return;
            }

            if(e.getItemId() !=65336 && e.getItemId() !=65436)
            {
	            // if this is not a list that maintains enchantment, check the count of all items that have the given id.
	            // otherwise, check only the count of items with exactly the needed enchantment level
	    		if( inv.getInventoryItemCount(e.getItemId(), maintainEnchantment? e.getEnchantmentLevel() : -1) < ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient()) ? (e.getItemCount() * _amount) : e.getItemCount()) )
	    		{
	    			player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
	    			_ingredientsList.clear();
	    			_ingredientsList = null;
	    			return;
	    		}
            }

            if(e.getItemId() == 65336)
            {
                if(player.getClan() == null)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
                    return;
                }
                if(!player.isClanLeader())
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED));
                    return;
                }
                if(player.getClan().getReputationScore() < e.getItemCount() * _amount)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
                    return;
                }
            }

			if(e.getItemId() == 65436)
			{
				if((e.getItemCount()*_amount) > player.getPcCafeScore())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
			}
		}

    	_ingredientsList.clear();
    	_ingredientsList = null;
    	FastList<L2Augmentation> augmentation = new FastList<L2Augmentation>();
    	/** All ok, remove items and add final product */

    	for(MultiSellIngredient e : entry.getIngredients())
    	{
			if(e.getItemId()!=65336 && e.getItemId()!=65436)
			{
				L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());		// initialize and initial guess for the item to take.
				if (itemToTake == null)
				{
					_log.severe("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + merchant.getNpcId());
					return;
				}
				
				if(itemToTake.isWear())
				{//Player trying to buy something from the Multisell store with an item that's just being used from the Wear option from merchants.
					_log.severe("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + merchant.getNpcId());
					return;
				}

				{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(e.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		_enchantCheck = e.getEnchantmentLevel();
				}

				if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient())
				{
					// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
					if (itemToTake.isStackable())
					{
		                if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
		                	return;
					}
					else
					{
						// for non-stackable items, one of two scenaria are possible:
						// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
						// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level

						// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
						if (maintainEnchantment)
						{
							L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
				                for (int i = 0; i < (e.getItemCount() * _amount); i++)
				                {
				                	if (inventoryContents[i].isAugmented())
				                		augmentation.add(inventoryContents[i].getAugmentation());
									if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
								    	return;
					            }
						}
						else	// b) enchantment is not maintained.  Get the instances with the LOWEST enchantment level
						{
							// choice 1.  Small number of items exchanged.  No sorting.
			                for (int i = 1; i <= (e.getItemCount() * _amount); i++)
							{
								L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());

								itemToTake = inventoryContents[0];
								// get item with the LOWEST enchantment level  from the inventory...
								// +0 is lowest by default...
								if (itemToTake.getEnchantLevel() > 0)
								{
									for (int j = 0; j < inventoryContents.length; j++)
									{
										if (inventoryContents[j].getEnchantLevel() < itemToTake.getEnchantLevel())
										{
											itemToTake = inventoryContents[j];
											// nothing will have enchantment less than 0. If a zero-enchanted
											// item is found, just take it
											if (itemToTake.getEnchantLevel() == 0)
												break;
										}
									}
								}
								if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
									return;
							}
						}
					}
				}
			}
			else
			{
				if (e.getItemId() == 65336)
				{
					int repCost = player.getClan().getReputationScore() - e.getItemCount() * _amount;
					player.getClan().setReputationScore(repCost, true);
					SystemMessage smsg = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					smsg.addNumber(e.getItemCount());
					player.sendPacket(smsg);
					player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				}
				else
				{
					player.reducePcCafeScore(e.getItemCount()*_amount);
                    player.sendPacket((new SystemMessage(1709)).addNumber(e.getItemCount()));
				}
			}
        }
    	// Generate the appropriate items
    	for(MultiSellIngredient e : entry.getProducts())
    	{
			if (_enchantCheck < e.getEnchantmentLevel())
			{
				L2Item tempItem = ItemTable.getInstance().createDummyItem(e.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
				{
					player.sendMessage("Ohh Cheat dont work? You have a problem now!");
					Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" trying to enchant cheat.", Config.DEFAULT_PUNISH);
					return;
				}
			}

	    	if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
	    	{
		    	inv.addItem("Multisell", e.getItemId(), (e.getItemCount() * _amount), player, player.getTarget());
	    	} else
	    	{
	    		L2ItemInstance product = null;
	            for (int i = 0; i < (e.getItemCount() * _amount); i++)
	            {
	            	product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());

					if (Config.ENABLE_MODIFY_ENCHANT_MULTISELL)
					{
						if (Config.ENCHANT_MULTISELL_LIST.containsKey(_listId))
						{
							product.setEnchantLevel(Config.ENCHANT_MULTISELL_LIST.get(_listId));
						}
					}

			    	if (maintainEnchantment)
			    	{
			    		if (i < augmentation.size())
			    		{
			    			product.setAugmentation(new L2Augmentation(product, augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill(), true));
			    		}
			    		product.setEnchantLevel(e.getEnchantmentLevel());
			    	}
	            }
	    	}
	         // msg part
	        SystemMessage sm;

	        if (e.getItemCount() * _amount > 1)
	        {
	        	sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
	            sm.addItemName(e.getItemId());
	            sm.addNumber(e.getItemCount() * _amount);
	            player.sendPacket(sm);
	            sm = null;
	        }
	        else
	        {
	            if(_enchantment > 0)
	            {
	                sm = new SystemMessage(SystemMessageId.ACQUIRED);
	                sm.addNumber(_enchantment);
	                sm.addItemName(e.getItemId());
	            }
	            else
	            {
	                sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
	                sm.addItemName(e.getItemId());
	            }
	            player.sendPacket(sm);
	            sm = null;
	        }
    	}
        player.sendPacket(new ItemList(player, false));

        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
        player.sendPacket(su);
        su = null;

        // finally, give the tax to the castle...
		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		    merchant.getCastle().addToTreasury(_transactionTax * _amount);
    }

    private MultiSellEntry prepareEntry(L2NpcInstance merchant, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
    {
    	MultiSellEntry newEntry = L2Multisell.getInstance().new MultiSellEntry();
    	newEntry.setEntryId(templateEntry.getEntryId());
    	int totalAdenaCount = 0;

        for (MultiSellIngredient ing : templateEntry.getIngredients())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	if (newIngredient.getItemId() == 57 && newIngredient.isTaxIngredient())
        	{
            	double taxRate = 0.0;
        		if (applyTaxes)
        		{
                	if (merchant != null && merchant.getIsInTown())
                		taxRate = merchant.getCastle().getTaxRate();
        		}

               	_transactionTax = (int)Math.round(newIngredient.getItemCount()*taxRate);
               	totalAdenaCount += _transactionTax;
        		continue;	// do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
        	}
        	else if (ing.getItemId() == 57) // && !ing.isTaxIngredient()
        	{
        		totalAdenaCount += newIngredient.getItemCount();
        		continue;	// do not yet add this adena amount to the list as taxIngredient adena might be entered later (order not guaranteed)
        	}
        	// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
        	else if (maintainEnchantment)
        	{
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
        	}

        	// finally, add this ingredient to the entry
        	newEntry.addIngredient(newIngredient);
        }
        // Next add the adena amount, if any
        if (totalAdenaCount > 0)
        	newEntry.addIngredient(L2Multisell.getInstance().new MultiSellIngredient(57, totalAdenaCount, false, false));

        // Now modify the enchantment level of products, if necessary
        for (MultiSellIngredient ing : templateEntry.getProducts())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	if (maintainEnchantment)
            {
            	// if it is an armor/weapon, modify the enchantment level appropriately
            	// (note, if maintain enchantment is "false" this modification will result to a +0)
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
            }
        	newEntry.addProduct(newIngredient);
        }
        return newEntry;
    }

    @Override
	public String getType()
    {
        return _C__A7_MULTISELLCHOOSE;
    }
}
