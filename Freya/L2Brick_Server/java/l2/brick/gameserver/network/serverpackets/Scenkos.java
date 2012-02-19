package l2.brick.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2.brick.Config;
import l2.brick.gameserver.model.L2World;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.CharInfo;
import l2.brick.gameserver.network.serverpackets.CreatureSay;
import l2.brick.gameserver.network.serverpackets.L2GameServerPacket;
import l2.brick.gameserver.network.serverpackets.RelationChanged;

public final class Scenkos
{
	private static Logger _log = Logger.getLogger(Scenkos.class.getName());
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character that have the Character targetted.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 *
	 */
	public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
	{
		if (Config.DEBUG)
			_log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		// synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player.getTarget() != character)
					continue;
				
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the
	 * L2Character.<BR>
	 * <BR>
	 * 
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in
	 * <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * L2Character, server just need to go through _knownPlayers to send
	 * Server->Client Packet<BR>
	 * <BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this L2Character (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * 
	 */
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		if (Config.DEBUG)
			_log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player == null)
					continue;
				try
				{
					player.sendPacket(mov);
					if (mov instanceof CharInfo && character instanceof L2PcInstance)
					{
						int relation = ((L2PcInstance) character).getRelation(player);
						Integer oldrelation = character.getKnownList().getKnownRelations().get(player.getObjectId());
						if (oldrelation != null && oldrelation != relation)
						{
							player.sendPacket(new RelationChanged((L2PcInstance) character, relation, character.isAutoAttackable(player)));
							if (((L2PcInstance) character).getPet() != null)
								player.sendPacket(new RelationChanged(((L2PcInstance) character).getPet(), relation, character.isAutoAttackable(player)));
						}
					}
				}
				catch (NullPointerException e)
				{
					_log.log(Level.WARNING, e.getMessage(),e);
				}
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers (in the specified
	 * radius) of the L2Character.<BR>
	 * <BR>
	 * 
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in
	 * <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the
	 * L2Character, server just needs to go through _knownPlayers to send
	 * Server->Client Packet and check the distance between the targets.<BR>
	 * <BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND
	 * Server->Client packet to this L2Character (to do this use method
	 * toSelfAndKnownPlayers)</B></FONT><BR>
	 * <BR>
	 * 
	 */
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
			radius = 1500;
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (character.isInsideRadius(player, radius, false, false))
					player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance in the _KnownPlayers of the L2Character and to the specified character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.<BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
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
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
			radiusSq = 360000;
		
		if (character instanceof L2PcInstance)
			character.sendPacket(mov);
		
		Collection<L2PcInstance> plrs = character.getKnownList().getKnownPlayers().values();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance player : plrs)
			{
				if (player != null && character.getDistanceSq(player) <= radiusSq)
					player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Send a packet to all L2PcInstance present in the world.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR>
	 * In order to inform other players of state modification on the L2Character, server just need to go through _allPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packet to this L2Character (to do this use method toSelfAndKnownPlayers)</B></FONT><BR><BR>
	 *
	 */
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		if (Config.DEBUG)
			_log.fine("Players to notify: " + L2World.getInstance().getAllPlayersCount() + " (with packet " + mov.getType() + ")");
		
		L2PcInstance[] pls = L2World.getInstance().getAllPlayersArray();
		// synchronized (L2World.getInstance().getAllPlayers())
		{
			for (L2PcInstance onlinePlayer : pls)
				if (onlinePlayer != null && onlinePlayer.isOnline())
					onlinePlayer.sendPacket(mov);
		}
	}
	
	public static void announceToOnlinePlayers(String text)
	{
		CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		toAllOnlinePlayers(cs);
	}
	
	public static void toPlayersInInstance(L2GameServerPacket mov, int instanceId)
	{
		L2PcInstance[] pls = L2World.getInstance().getAllPlayersArray();
		//synchronized (character.getKnownList().getKnownPlayers())
		{
			for (L2PcInstance onlinePlayer : pls)
			{
				if (onlinePlayer != null && onlinePlayer.isOnline() && onlinePlayer.getInstanceId() == instanceId)
					onlinePlayer.sendPacket(mov);
			}
		}
	}
}
