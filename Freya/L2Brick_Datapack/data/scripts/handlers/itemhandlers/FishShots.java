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
import l2.brick.gameserver.model.L2ItemInstance;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.actor.L2Playable;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.SystemMessageId;
import l2.brick.gameserver.network.serverpackets.MagicSkillUse;
import l2.brick.gameserver.network.serverpackets.Scenkos;
import l2.brick.gameserver.network.serverpackets.SystemMessage;
import l2.brick.gameserver.templates.L2Item;
import l2.brick.gameserver.templates.L2Weapon;
import l2.brick.gameserver.templates.L2WeaponType;

/**
 * @author -Nemesiss-
 *
 */
public class FishShots implements IItemHandler
{
	private static final int[] SKILL_IDS =
	{
		2181, 2182, 2183, 2184, 2185, 2186
	};
	
	/**
	 * 
	 * @see l2.brick.gameserver.handler.IItemHandler#useItem(l2.brick.gameserver.model.actor.L2Playable, l2.brick.gameserver.model.L2ItemInstance, boolean)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		
		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.FISHINGROD)
			return;
		
		if (weaponInst.getChargedFishshot())
			// spirit shot is already active
			return;
		
		int FishshotId = item.getItemId();
		int grade = weaponItem.getCrystalType();
		long count = item.getCount();
		
		if ((grade == L2Item.CRYSTAL_NONE && FishshotId != 6535) || (grade == L2Item.CRYSTAL_D && FishshotId != 6536) || (grade == L2Item.CRYSTAL_C && FishshotId != 6537) || (grade == L2Item.CRYSTAL_B && FishshotId != 6538)
				|| (grade == L2Item.CRYSTAL_A && FishshotId != 6539) || (FishshotId != 6540 && grade == L2Item.CRYSTAL_S ))
		{
			//1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WRONG_FISHINGSHOT_GRADE));
			return;
		}
		
		if (count < 1)
			return;
		
		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		L2Object oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		
		Scenkos.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, SKILL_IDS[grade], 1, 0, 0));
		activeChar.setTarget(oldTarget);
	}
}
