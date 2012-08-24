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
package l2.hellknight.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.templates.L2NpcTemplate;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;

/**
 * @author UnAfraid
 */
public class L2TotemInstance extends L2Npc
{
	protected ScheduledFuture<?> _aiTask;
	
	protected L2Skill _skill;
	
	private class TotemAI implements Runnable
	{
		private final L2TotemInstance _caster;
		
		protected TotemAI(L2TotemInstance caster)
		{
			_caster = caster;
		}
		
		@Override
		public void run()
		{
			if (_skill == null)
			{
				if (_caster._aiTask != null)
				{
					_caster._aiTask.cancel(false);
					_caster._aiTask = null;
				}
				return;
			}
			
			for (L2PcInstance player : getKnownList().getKnownPlayersInRadius(_skill.getSkillRadius()))
			{
				if (player.getFirstEffect(_skill.getId()) == null)
				{
					_skill.getEffects(player, player);
				}
			}
		}
	}
	
	public L2TotemInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TotemInstance);
	}
	
	public L2TotemInstance(int objectId, L2NpcTemplate template, int skillId)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TotemInstance);
		_skill = SkillTable.getInstance().getInfo(skillId, 1);
		_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TotemAI(this), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(true);
		}
		super.deleteMe();
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 900;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void setAITask()
	{
		if (_aiTask == null)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TotemAI(this), 3000, 3000);
		}
	}
	
	/**
	 * @param skillId the _skill to set
	 */
	public void setSkill(int skillId)
	{
		_skill = SkillTable.getInstance().getInfo(skillId, 1);
	}
}
