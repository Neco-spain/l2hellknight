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
package l2.hellknight.gameserver.network.serverpackets;

import l2.hellknight.gameserver.model.actor.instance.L2PetInstance;
import l2.hellknight.gameserver.model.items.instance.L2ItemInstance;

public class PetItemList extends L2GameServerPacket
{
	private final L2PetInstance _activeChar;
	
	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB3);
		
		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);
		
		for (L2ItemInstance temp : items)
		{
			writeD(temp.getObjectId());
			writeD(temp.getDisplayId());
			writeD(temp.getLocationSlot());
			writeQ(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
				writeD(temp.getAugmentation().getAugmentationId());
			else
				writeD(0x00);
			writeD(temp.getMana());
			writeD(temp.isTimeLimitedItem() ? (int) (temp.getRemainingTime() / 1000) : -9999);
			writeH(temp.getAttackElementType());
			writeH(temp.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				writeH(temp.getElementDefAttr(i));
			}
			// Enchant Effects
			writeH(0x00);
			writeH(0x00);
			writeH(0x00);
		}
	}
}
