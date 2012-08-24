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

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.CharTemplateTable;
import l2.hellknight.gameserver.model.base.ClassId;
import l2.hellknight.gameserver.network.serverpackets.NewCharacterSuccess;

/**
 * @author Zoey76
 */
public final class NewCharacter extends L2GameClientPacket
{
	private static final String _C__13_NEWCHARACTER = "[C] 13 NewCharacter";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine(_C__13_NEWCHARACTER);
		}
		
		final NewCharacterSuccess ct = new NewCharacterSuccess();
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.fighter)); // Human Figther
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.mage)); // Human Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter)); // Elven Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage)); // Elven Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter)); // Dark Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.darkMage)); // Dark Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter)); // Orc Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.orcMage)); // Orc Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter)); // Dwarf Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.maleSoldier)); // Male Kamael Soldier
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.femaleSoldier)); // Female Kamael Soldier
		sendPacket(ct);
	}
	
	@Override
	public String getType()
	{
		return _C__13_NEWCHARACTER;
	}
}
