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
package l2.hellknight.gameserver.network.clientpackets;

import java.util.List;

import javolution.util.FastList;

import l2.hellknight.gameserver.instancemanager.CursedWeaponsManager;
import l2.hellknight.gameserver.model.CursedWeapon;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.network.serverpackets.ExCursedWeaponLocation;
import l2.hellknight.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import l2.hellknight.gameserver.util.Point3D;

/**
 * Format: (ch)
 * @author  -Wooden-
 */
public final class RequestCursedWeaponLocation extends L2GameClientPacket
{
	private static final String _C__D0_2B_REQUESTCURSEDWEAPONLOCATION = "[C] D0:2B RequestCursedWeaponLocation";
	
	@Override
	protected void readImpl()
	{
		//nothing to read it's just a trigger
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		List<CursedWeaponInfo> list = new FastList<>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
				continue;
			
			Point3D pos = cw.getWorldPosition();
			if (pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}
		
		//send the ExCursedWeaponLocation
		if (!list.isEmpty())
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
	
	/**
	 * @see l2.hellknight.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2B_REQUESTCURSEDWEAPONLOCATION;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
