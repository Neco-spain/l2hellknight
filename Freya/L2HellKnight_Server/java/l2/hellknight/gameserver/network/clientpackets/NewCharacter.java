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

import java.util.logging.Logger;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.CharTemplateTable;
import l2.hellknight.gameserver.model.base.ClassId;
import l2.hellknight.gameserver.network.serverpackets.NewCharacterSuccess;
import l2.hellknight.gameserver.templates.L2PcTemplate;


/**
 * This class ...
 *
 * @version $Revision: 1.3.4.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class NewCharacter extends L2GameClientPacket
{
	private static final String _C__0E_NEWCHARACTER = "[C] 0E NewCharacter";
	private static Logger _log = Logger.getLogger(NewCharacter.class.getName());
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG) _log.fine("CreateNewChar");
		
		NewCharacterSuccess ct = new NewCharacterSuccess();
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0);
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter);	// human fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.mage);	// human mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter);	// elf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage);	// elf mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter);	// dark elf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage);	// dark elf mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter);	// orc fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage);	// orc mage
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter);	// dwarf fighter
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.maleSoldier); //kamael male soldier
		ct.addChar(template);
		
		template = CharTemplateTable.getInstance().getTemplate(ClassId.femaleSoldier); // kamael female soldier
		ct.addChar(template);
		
		sendPacket(ct);
	}
	
	/* (non-Javadoc)
	 * @see l2.hellknight.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0E_NEWCHARACTER;
	}
}
