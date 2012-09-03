package l2rt.gameserver.ai;

import javolution.util.FastMap;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author ~ExTaZy~
 */
public class Instance extends Functions
{
	public static boolean enterInstance(L2Player player, int id, boolean party, boolean msg)
	{
		if(player == null)
			return false;

		if(party)
		{
	    	if(!player.isInParty())
			{
	    		player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
	    		return false;
			}
	    	if(!player.getParty().isLeader(player))
			{
	    		player.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
	    		return false;
			}
		}
		else
		{
	    	if(player.isInParty())
			{
	    		player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
	    		return false;
			}
		}

		InstancedZoneManager izm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> izs = izm.getById(id);
		if(izs == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return false;
		}
		InstancedZone iz = izs.get(0);
		if(iz == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return false;
		}
		String name = iz.getName();
		if(izm.getTimeToNextEnterInstance(name, player) > 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
			return false;
		}
		/*Reflection old_ref = player.getReflection();
		if(old_ref != null)
		{
			if(!iz.equals(old_ref.getInstancedZone()))
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_ENTERED_ANOTHER_INSTANCE_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return false;
			}
		}*/
		int timelimit = iz.getTimelimit();
		if(party)
		{
			int minMembers = iz.getMinParty();
			int maxMembers = iz.getMaxParty();
			int min_level = iz.getMinLevel();
			int max_level = iz.getMaxLevel();

			/**if(player.getParty().getMemberCount() < minMembers || player.getParty().getMemberCount() > maxMembers)
			{
	    		player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
	    		return false;
			}*/
			if(player.getParty().getMemberCount() > maxMembers)
			{
	    		player.sendPacket(Msg.YOU_CANNOT_ENTER_DUE_TO_THE_PARTY_HAVING_EXCEEDED_THE_LIMIT);
	    		return false;
			}

    		for(L2Player member : player.getParty().getPartyMembers())
    		{
    			if(member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead())
    			{
    				player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
    				return false;
    			}
    			if(member.getLevel() < min_level || member.getLevel() > max_level)
    			{
    				SystemMessage sm = new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member);
    				member.sendPacket(sm);
    				player.sendPacket(sm);
    				return false;
    			}
    			if(!player.isInRange(member, 500))
    			{
    				member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
    				player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
    				return false;
    			}
    			if(izm.getTimeToNextEnterInstance(name, member) > 0)
    			{
    				player.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
    				return false;
    			}
    		}
		}

		Reflection r = new Reflection(iz);
		r.setInstancedZoneId(id);

		for(InstancedZone i : izs.values())
		{
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
			r.FillDoors(i.getDoors());
		}
		r.setCoreLoc(r.getReturnLoc());
		r.setReturnLoc(player.getLoc());
		if(party)
		{
    		for(L2Player member : player.getParty().getPartyMembers())
    		{
    			member.setVar(name, String.valueOf(System.currentTimeMillis()));
				member.setVar("backCoords", r.getReturnLoc().toXYZString());
				member.teleToLocation(iz.getTeleportCoords(), r.getId());
    		}
			player.getParty().setReflection(r);
			r.setParty(player.getParty());
		}
		else
		{
    		player.setVar(name, String.valueOf(System.currentTimeMillis()));
    		player.setVar("backCoords", r.getReturnLoc().toXYZString());
    		player.teleToLocation(iz.getTeleportCoords(), r.getId());
    		player.setReflection(r);
		}
		if(timelimit > 0)
		{
	    	r.startCollapseTimer(timelimit * 60 * 1000L);
			if(msg)
	    		player.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
		}
		return true;
	}
}