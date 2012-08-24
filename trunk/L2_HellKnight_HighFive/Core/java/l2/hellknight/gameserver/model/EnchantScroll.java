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
package l2.hellknight.gameserver.model;

import java.util.List;

import l2.hellknight.Config;
import l2.hellknight.gameserver.model.items.L2Item;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author UnAfraid
 */
public class EnchantScroll extends EnchantItem
{
	private final boolean _isBlessed;
	private final boolean _isCrystal;
	private final boolean _isSafe;
	
	/**
	 * @param set
	 * @param items
	 */
	public EnchantScroll(StatsSet set, List<Integer> items)
	{
		super(set, items);
		
		_isBlessed = set.getBool("isBlessed", false);
		_isCrystal = set.getBool("isCrystal", false);
		_isSafe = set.getBool("isSafe", false);
	}
	
	/**
	 * @return true for blessed scrolls
	 */
	public final boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return true for crystal scrolls
	 */
	public final boolean isCrystal()
	{
		return _isCrystal;
	}
	
	/**
	 * @return true for safe-enchant scrolls (enchant level will remain on failure)
	 */
	public final boolean isSafe()
	{
		return _isSafe;
	}
	
	/**
	 * @param enchantItem
	 * @param supportItem
	 * @return
	 */
	public final boolean isValid(L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		// blessed scrolls can't use support items
		if (supportItem != null && (!supportItem.isValid(enchantItem) || isBlessed()))
			return false;
		
		return super.isValid(enchantItem);
	}
	
	/**
	 * @param enchantItem
	 * @param supportItem
	 * @return
	 */
	public final double getChance(L2ItemInstance enchantItem, EnchantItem supportItem)
	{
		if (!isValid(enchantItem, supportItem))
			return -1;
		
		boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
		if (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX || (fullBody && enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))
			return 100;
		
		double chance = _chanceAdd;
		
		if (supportItem != null && !_isBlessed)
			chance *= supportItem.getChanceAdd();
		
		return chance;
	}
}
