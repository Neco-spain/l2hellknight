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
package com.l2js.gameserver.network.clientpackets;

import com.l2js.gameserver.datatables.PetNameTable;
import com.l2js.gameserver.model.actor.L2Summon;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.network.SystemMessageId;

public final class RequestChangePetName extends L2GameClientPacket
{
	private static final String	_C__93_REQUESTCHANGEPETNAME	= "[C] 93 RequestChangePetName";

	private String				_name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final L2Summon pet = activeChar.getPet();
		if (pet == null)
		{
			return;
		}

		if (pet.getName() != null)
		{
			activeChar.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
			return;
		}
		else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().npcId))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
			return;
		}
		else if ((_name.length() < 3) || (_name.length() > 16))
		{
			//activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_UP_TO_8CHARS);
			activeChar.sendMessage("Your pet's name can be up to 16 characters in length.");
			return;
		}
		else if (!PetNameTable.getInstance().isValidPetName(_name))
		{
			activeChar.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
			return;
		}

		pet.setName(_name);
		pet.updateAndBroadcastStatus(1);
	}

	@Override
	public String getType()
	{
		return _C__93_REQUESTCHANGEPETNAME;
	}
}
