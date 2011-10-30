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

import l2.hellknight.gameserver.templates.chars.L2NpcTemplate;

public class L2CastleWyvernManagerInstance extends L2WyvernManagerInstance
{
	public L2CastleWyvernManagerInstance (int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public final boolean isOwnerClan(L2PcInstance player)
	{
		if (player.getClan() != null && getCastle() != null)
		{
			if (player.getClanId() == getCastle().getOwnerId() && player.isClanLeader())
				return true;
		}
		return false;
	}
	
	/**
	 * @see l2.hellknight.gameserver.model.actor.instance.L2WyvernManagerInstance#isInSiege()
	 */
	@Override
	public boolean isInSiege()
	{
		return getCastle().getZone().isActive();
	}
}
