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

import java.util.Collection;
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
 * @author Drunkard Zabb0x
 * Lets drink2code!
 */
public class L2XmassTreeInstance extends L2Npc
{
	public static final int SPECIAL_TREE_ID = 13007;
	protected ScheduledFuture<?> _aiTask;
	
	private final class XmassAI implements Runnable
	{
		private final L2XmassTreeInstance _caster;
		private final L2Skill _skill;
		
		protected XmassAI(L2XmassTreeInstance caster, L2Skill skill)
		{
			_caster = caster;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			if (_skill == null || _caster.isInsideZone(ZONE_PEACE))
			{
				_caster._aiTask.cancel(false);
				_caster._aiTask = null;
				return;
			}
			
			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayersInRadius(_skill.getSkillRadius());
			for (L2PcInstance player : plrs)
			{
				if (player.getFirstEffect(_skill.getId()) == null)
				{
					_skill.getEffects(player, player);
				}
			}
		}
	}
	
	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2XmassTreeInstance);
		if (template.getNpcId() == SPECIAL_TREE_ID)
			_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this,SkillTable.getInstance().getInfo(2139, 1)), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null) _aiTask.cancel(true);
		
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
}
