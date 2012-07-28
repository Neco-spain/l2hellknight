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
package handlers.itemhandlers;

import l2.brick.gameserver.handler.IItemHandler;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.item.L2Item;
import l2.brick.gameserver.model.item.L2Weapon;
import l2.brick.gameserver.model.item.instance.L2ItemInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.MagicSkillUse;
import l2.brick.gameserver.skills.Stats;
import l2.brick.gameserver.util.Broadcast;

public class SoulShots implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		// Check if Soul shot can be used
		if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return;
		}
		
		boolean gradeCheck = true;
		final int weaponGrade = weaponItem.getCrystalType();
		switch (weaponGrade)
		{
			case L2Item.CRYSTAL_NONE:
				if ((itemId != 5789) && (itemId != 1835))
				{
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_D:
				if ((itemId != 1463) && (itemId != 22082))
				{
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_C:
				if ((itemId != 1464) && (itemId != 22083))
				{
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_B:
				if ((itemId != 1465) && (itemId != 22084))
				{
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_A:
				if ((itemId != 1466) && (itemId != 22085))
				{
					gradeCheck = false;
				}
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
			case L2Item.CRYSTAL_S84:
				if ((itemId != 1467) && (itemId != 22086))
				{
					gradeCheck = false;
				}
				break;
		}
		
		if (!gradeCheck)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			}
			return;
		}
		
		activeChar.soulShotLock.lock();
		try
		{
			// Check if Soul shot is already active
			if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			{
				return;
			}
			
			// Consume Soul shots if player has enough of them
			final int saSSCount = (int) activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
			final int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
			
			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
			{
				if (!activeChar.disableAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return;
			}
			// Charge soul shot
			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		}
		finally
		{
			activeChar.soulShotLock.unlock();
		}
		int skillId = 0;
		switch (itemId)
		{
			case 1835:
			case 5789:
				skillId = 2039;
				break;
			case 1463:
				skillId = 2150;
				break;
			case 1464:
				skillId = 2151;
				break;
			case 1465:
				skillId = 2152;
				break;
			case 1466:
				skillId = 2153;
				break;
			case 1467:
				skillId = 2154;
				break;
			case 22082:
				skillId = 26060;
				break;
			case 22083:
				skillId = 26061;
				break;
			case 22084:
				skillId = 26062;
				break;
			case 22085:
				skillId = 26063;
				break;
			case 22086:
				skillId = 26064;
				break;
		}
		// Send message to client
		activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skillId, 1, 0, 0), 360000);
	}
}
