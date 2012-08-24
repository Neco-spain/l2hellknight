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
package l2.hellknight.gameserver.skills.l2skills;

import l2.hellknight.gameserver.datatables.NpcTable;
import l2.hellknight.gameserver.idfactory.IdFactory;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2BirthdayCakeInstance;
import l2.hellknight.gameserver.model.actor.instance.L2NpcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2TotemInstance;
import l2.hellknight.gameserver.model.actor.instance.L2XmassTreeInstance;
import l2.hellknight.gameserver.templates.StatsSet;
import l2.hellknight.gameserver.templates.chars.L2NpcTemplate;
import l2.hellknight.util.Rnd;

public class L2SkillSpawn extends L2Skill
{
	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	private final int _skillToCast;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_summonSpawn = set.getBool("isSummonSpawn", false);
		_randomOffset = set.getBool("randomOffset", true);
		_skillToCast = set.getInteger("skillToCast", 0);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		int x, y;
		
		if (caster.isAlikeDead())
			return;
		
		if (_npcId == 0)
		{
			_log.warning("NPC ID not defined for skill ID:" + getId());
			return;
		}
		
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		if (template == null)
		{
			_log.warning("Spawn of the nonexisting NPC ID:" + _npcId + ", skill ID:" + getId());
			return;
		}
		
		final int id = IdFactory.getInstance().getNextId();
		final L2Npc npc;
		if (template.isType("L2XmassTree"))
		{
			npc = new L2XmassTreeInstance(id, template);
		}
		else if (template.isType("L2BirthdayCake"))
		{
			npc = new L2BirthdayCakeInstance(id, template, caster.getObjectId());
		}
		else if (template.isType("L2Totem"))
		{
			npc = new L2TotemInstance(id, template, _skillToCast);
		}
		else if (template.isType("L2WeddingCake"))
		{
			 // TODO: npc = new L2WeddingCakeInstance(id, template);
			npc = new L2NpcInstance(id, template);
		}
		else
		{
			npc = new L2NpcInstance(id, template);
		}
		
		npc.setName(template.getName());
		npc.setTitle(caster.getName());
		npc.setHeading(-1);
		npc.setShowSummonAnimation(_summonSpawn);
		
		if (_randomOffset)
		{
			x = caster.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
			y = caster.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
		}
		else
		{
			x = caster.getX();
			y = caster.getY();
		}
		
		npc.spawnMe(x, y, caster.getZ() + 20);
		if (_despawnDelay > 0)
			npc.scheduleDespawn(_despawnDelay);
	}
}
