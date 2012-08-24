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
package l2.hellknight.gameserver.scripting.scriptengine.listeners.player;

import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.scripting.scriptengine.events.EquipmentEvent;
import l2.hellknight.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Equip and unequip listener.<br>
 * This listener can be either global or player-based<br>
 * Use the boolean in the constructor!
 * @author TheOne
 */
public abstract class EquipmentListener extends L2JListener
{
	private boolean isGlobal = false;
	
	/**
	 * Constructor To set a global listener, set the L2PcInstance value to null
	 * @param character
	 */
	public EquipmentListener(L2PcInstance character)
	{
		player = character;
		if (character == null)
		{
			isGlobal = true;
		}
		register();
	}
	
	/**
	 * The item has just been equipped or unequipped
	 * @param event
	 * @return
	 */
	public abstract boolean onEquip(EquipmentEvent event);
	
	@Override
	public void register()
	{
		if (isGlobal)
		{
			L2PcInstance.addGlobalEquipmentListener(this);
		}
		else
		{
			player.addEquipmentListener(this);
		}
	}
	
	@Override
	public void unregister()
	{
		if (isGlobal)
		{
			L2PcInstance.removeGlobalEquipmentListener(this);
		}
		else
		{
			player.removeEquipmentListener(this);
		}
	}
	
}
