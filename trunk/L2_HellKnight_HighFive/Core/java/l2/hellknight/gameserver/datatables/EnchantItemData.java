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
package l2.hellknight.gameserver.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2.hellknight.gameserver.engines.DocumentParser;
import l2.hellknight.gameserver.model.EnchantItem;
import l2.hellknight.gameserver.model.EnchantScroll;
import l2.hellknight.gameserver.model.StatsSet;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author UnAfraid
 */
public class EnchantItemData extends DocumentParser
{
	public static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
	public static final Map<Integer, EnchantItem> _supports = new HashMap<>();
	
	/**
	 * Instantiates a new enchant item data.
	 */
	public EnchantItemData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_scrolls.clear();
		_supports.clear();
		parseDatapackFile("data/enchantData.xml");
		_log.info(getClass().getSimpleName() + ": Loaded " + _scrolls.size() + " Enchant Scrolls.");
		_log.info(getClass().getSimpleName() + ": Loaded " + _supports.size() + " Support Items.");
	}
	
	@Override
	protected void parseDocument()
	{
		StatsSet set;
		Node att;
		for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchant".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						List<Integer> items = new ArrayList<>();
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								items.add(parseInt(cd.getAttributes(), "id"));
							}
						}
						EnchantScroll item = new EnchantScroll(set, items);
						_scrolls.put(item.getScrollId(), item);
					}
					else if ("support".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						
						List<Integer> items = new ArrayList<>();
						
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("item".equalsIgnoreCase(cd.getNodeName()))
							{
								items.add(parseInt(cd.getAttributes(), "id"));
							}
						}
						EnchantItem item = new EnchantItem(set, items);
						_supports.put(item.getScrollId(), item);
					}
				}
			}
		}
	}
	
	/**
	 * Gets the enchant scroll.
	 * @param scroll the scroll
	 * @return enchant template for scroll
	 */
	public final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return _scrolls.get(scroll.getItemId());
	}
	
	/**
	 * Gets the support item.
	 * @param item the item
	 * @return enchant template for support item
	 */
	public final EnchantItem getSupportItem(L2ItemInstance item)
	{
		return _supports.get(item.getItemId());
	}
	
	/**
	 * Gets the single instance of EnchantItemData.
	 * @return single instance of EnchantItemData
	 */
	public static final EnchantItemData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EnchantItemData _instance = new EnchantItemData();
	}
}
