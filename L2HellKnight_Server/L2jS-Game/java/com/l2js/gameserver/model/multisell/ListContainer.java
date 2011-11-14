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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author DS
 * 
 */
public class ListContainer
{
	protected int _listId;
	protected boolean _applyTaxes = false;
	protected boolean _maintainEnchantment = false;
	protected double _useRate = 1.0;
	
	protected List<Entry> _entries;
	
	public ListContainer()
	{
		_entries = new ArrayList<Entry>();
	}
	
	/**
	 * This constructor used in PreparedListContainer only
	 * ArrayList not created
	 */
	protected ListContainer(int listId)
	{
		_listId = listId;
	}
	
	public final List<Entry> getEntries()
	{
		return _entries;
	}
	
	public final void setListId(int listId)
	{
		_listId = listId;
	}
	
	public final int getListId()
	{
		return _listId;
	}
	
	public final void setApplyTaxes(boolean applyTaxes)
	{
		_applyTaxes = applyTaxes;
	}
	
	public final boolean getApplyTaxes()
	{
		return _applyTaxes;
	}
	
	public final void setMaintainEnchantment(boolean maintainEnchantment)
	{
		_maintainEnchantment = maintainEnchantment;
	}
	
	public final boolean getMaintainEnchantment()
	{
		return _maintainEnchantment;
	}
	
	public double getUseRate()
	{
		return _useRate;
	}
	
	/**
	 * Set this to create multisell with increased products, all product counts will be multiplied
	 * by the rate specified<br>
	 * 
	 * 
	 * <b>NOTE:</b> It affects only parser, it won't change values of already parsed multisell
	 * since MultiSells' parseEntry method hnadles this feature
	 * 
	 * @param _useRate
	 * @see com.l2jserver.gameserver.datatables.MultiSell#parseEntry
	 */
	public void setUseRate(double rate)
	{
		_useRate = rate;
	}
}
