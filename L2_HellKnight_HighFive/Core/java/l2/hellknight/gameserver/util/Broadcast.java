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
package l2.hellknight.gameserver.util;

import gnu.trove.procedure.TObjectProcedure;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CharInfo;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.L2GameServerPacket;
import l2.hellknight.gameserver.network.serverpackets.RelationChanged;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public final class Broadcast
{
	private static Logger _log = Logger.getLogger(Broadcast.class.getName());
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targeted.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 */
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player.getTarget() != character)
			{
				continue;
			}
			
			player.sendPacket(mov);
		}
		
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 */
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player == null)
			{
				continue;
			}
			try
			{
				player.sendPacket(mov);
				if ((mov instanceof CharInfo) && (character instanceof L2PcInstance))
				{
					int relation = ((L2PcInstance) character).getRelation(player);
					Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
					if ((oldrelation != null) && (oldrelation != relation))
					{
						player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
						if (((L2PcInstance) character).getPet() != null)
						{
							player.sendPacket(new RelationChanged(((L2PcInstance) character).getPet(), relation, character.isAutoAttackable(player)));
						}
					}
				}
			}
			catch (NullPointerException e)
			{
				_log.log(Level.WARNING, e.getMessage(), e);
			}
		}
		
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified radius) of the L2Character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just needs to go through _knownPlayers to send Server->Client Packet and check the distance between the targets.<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param character
	 * @param mov
	 * @param radius
	 */
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			radius = 1500;
		}
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (character.isInsideRadius(player, radius, false, false))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * @param character
	 * @param mov
	 */
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (character instanceof L2PcInstance)
		{
			character.sendPacket(mov);
		}
		
		toKnownPlayers(character, mov);
	}
	
	// To improve performance we are comparing values of radius^2 instead of calculating sqrt all the time
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 600;
		
		if (character instanceof L2PcInstance)
			character.sendPacket(mov);
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs)
		{
			if (player != null && Util.checkIfInRange(radius, character, player, false))
				player.sendPacket(mov);
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance present in the world.<BR>
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR>
	 * @param mov
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		L2World.getInstance().forEachPlayer(new ForEachPlayerBroadcast(mov));
	}
	
	public static void announceToOnlinePlayers(String text, boolean isCritical)
	{
		CreatureSay cs;
		
		if (isCritical)
		{
			cs = new CreatureSay(0, Say2.CRITICAL_ANNOUNCE, "", text);
		}
		else
		{
			cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		}
		
		toAllOnlinePlayers(cs);
	}
	
	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId)
	{
		L2World.getInstance().forEachPlayer(new ForEachPlayerInInstanceBroadcast(mov, instanceId));
	}
	
	private static final class ForEachPlayerBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;
		
		protected ForEachPlayerBroadcast(L2GameServerPacket packet)
		{
			_packet = packet;
		}
		
		@Override
		public final boolean execute(final L2PcInstance onlinePlayer)
		{
			if ((onlinePlayer != null) && onlinePlayer.isOnline())
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}
	
	private static final class ForEachPlayerInInstanceBroadcast implements TObjectProcedure<L2PcInstance>
	{
		L2GameServerPacket _packet;
		int _instanceId;
		
		protected ForEachPlayerInInstanceBroadcast(L2GameServerPacket packet, int instanceId)
		{
			_packet = packet;
			_instanceId = instanceId;
		}
		
		@Override
		public final boolean execute(final L2PcInstance onlinePlayer)
		{
			if ((onlinePlayer != null) && onlinePlayer.isOnline() && (onlinePlayer.getInstanceId() == _instanceId))
			{
				onlinePlayer.sendPacket(_packet);
			}
			return true;
		}
	}
}
