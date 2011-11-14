/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2js.gameserver.model.multisell;

import static com.l2js.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2js.gameserver.model.L2ItemInstance;

/**
 * @author DS
 */
public class PreparedEntry extends Entry
{
	public static Logger _log = Logger.getLogger(PreparedEntry.class.getName());
	
	private long _taxAmount = 0;
	
	public PreparedEntry(Entry template, L2ItemInstance item, boolean applyTaxes, boolean maintainEnchantment, double taxRate)
	{
		try
		{
			_entryId = template.getEntryId() * 100000;
			if (maintainEnchantment && item != null)
				_entryId += item.getEnchantLevel();
			
			ItemInfo info = null;
			long adenaAmount = 0;
			
			_ingredients = new ArrayList<Ingredient>(template.getIngredients().size());
			for (Ingredient ing : template.getIngredients())
			{
				if (ing.getItemId() == ADENA_ID)
				{
					// Tax ingredients added only if taxes enabled
					if (ing.isTaxIngredient())
					{
						// if taxes are to be applied, modify/add the adena
						// count based on the template adena/ancient adena count
						if (applyTaxes)
							_taxAmount += Math.round(ing.getItemCount() * taxRate);
					}
					else
						adenaAmount += ing.getItemCount();
					
					continue; // do not yet add this adena amount to the list as
								// non-taxIngredient adena might be entered
								// later (order not guaranteed)
				}
				else if (maintainEnchantment && item != null && ing.isArmorOrWeapon())
				{
					info = new ItemInfo(item);
					final Ingredient newIngredient = ing.getCopy();
					newIngredient.setItemInfo(info);
					_ingredients.add(newIngredient);
				}
				else if (!maintainEnchantment && ing.isArmorOrWeapon() && ing.getEnchantLvl() > 0)
				{
					info = new ItemInfo(ing.getEnchantLvl());
					final Ingredient newIngredient = ing.getCopy();
					newIngredient.setItemInfo(info);
					_ingredients.add(newIngredient);
				}
				else
				{
					final Ingredient newIngredient = ing.getCopy();
					_ingredients.add(newIngredient);
				}
				
			}
			
			// now add the adena, if any.
			adenaAmount += _taxAmount; // do not forget tax
			if (adenaAmount > 0)
				_ingredients.add(new Ingredient(ADENA_ID, adenaAmount, 0, false, false));
			
			// now copy products
			_products = new ArrayList<Ingredient>(template.getProducts().size());
			for (Ingredient ing : template.getProducts())
			{
				if (!ing.isStackable())
					_stackable = false;
				
				if (maintainEnchantment && ing.isArmorOrWeapon())
				{
					final Ingredient newProduct = ing.getCopy();
					newProduct.setItemInfo(info);
					_products.add(newProduct);
				}
				else if (!maintainEnchantment && ing.isArmorOrWeapon() && ing.getEnchantLvl() > 0)
				{
					final Ingredient newProduct = ing.getCopy();
					info = new ItemInfo(ing.getEnchantLvl());
					newProduct.setItemInfo(info);
					_products.add(newProduct);
				}
				{
					final Ingredient newProduct = ing.getCopy();
					_products.add(newProduct);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, PreparedEntry.class.getName(), e);
		}
	}
	
	@Override
	public final long getTaxAmount()
	{
		return _taxAmount;
	}
}
