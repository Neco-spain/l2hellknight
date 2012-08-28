package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.util.FloodProtector;

public final class Logout extends L2GameClientPacket
{
	private static final String _C__09_LOGOUT = "[C] 09 Logout";
	private static Logger _log = Logger.getLogger(Logout.class.getName());


	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if (player == null)
			return;

		if (!(player.isGM()))
        {
            if(CustomZoneManager.getInstance().checkIfInZone("NoEscape", player)){
                player.sendPacket(SystemMessage.sendString("Not the best place to exit the game"));
                player.sendPacket(new ActionFailed());
                return;                   
            }
        }

		player.getInventory().updateDatabase();

		if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
		{
			if (Config.DEBUG) _log.fine("Player " + player.getName() + " tried to logout while fighting");

			player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
			player.sendPacket(new ActionFailed());
			return;
		}

		if(player.atEvent) 
		{
			player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event"));
			player.sendPacket(new ActionFailed());
			return;
		}
				
		//sub class exploit fix
		if(!FloodProtector.getInstance().tryPerformAction(player.getObjectId(), FloodProtector.PROTECTED_SUBCLASSPROTECT))
		{
			player.sendMessage("You can change Subclass only every " + Config.SUBCLASS_PROTECT + " Millisecond(s)");
			return;
		}
		
		if (player.isCastingNow())
	    {
	      player.sendPacket(SystemMessage.sendString("Вы не можете выйти пока кастуете!"));
	      return;
	    }
		
		if (player.isInFunEvent())
		{
			player.sendPacket(SystemMessage.sendString("You can't logout in event."));
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
	    {
	      player.sendMessage("Вы не можете выйти пока используете заточку!");
	      return;
	    }
		
		if (player.isTeleporting())
		{
        	//player.abortCast();
        	//player.setIsTeleporting(false);
			player.sendPacket(SystemMessage.sendString("Вы не можете выйти пока телепортируетесь!"));
		    return;
        }
		
		if (player.getPet() != null)
		{
			player.sendMessage("You can't logout when your pet is summoned.");
			return;
		}
		
		if (player.isMounted())
		{
			player.sendPacket(SystemMessage.sendString("Вы не можете выйти. Слезте с питомца!"));
		    return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessage.sendString("Вы не можете выйти пока торгуете!"));
		    return;
		}
			
        if (player.isInOlympiadMode() || Olympiad.getInstance().isRegisteredInComp(player) || player.getOlympiadGameId() > 0)
        {
            player.sendMessage("You cant logout in olympiad mode");
            return;
        }

		if (player.isFestivalParticipant()) 
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot log out while you are a participant in a festival.");
				return;
			}
			L2Party playerParty = player.getParty();

			if (playerParty != null)
				player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
		}
		if (player.isFlying())
		{
			player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		
		if(player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE || player.isInCraftMode() && Config.OFFLINE_CRAFT_ENABLE)
		{
			player.store();
			player.closeNetConnection(true);
			if (player.getOfflineStartTime() == 0)
				player.setOfflineStartTime(System.currentTimeMillis());
			return;
		}
		TvTEvent.onLogout(player);
		RegionBBSManager.getInstance().changeCommunityBoard();

		player.deleteMe();
		notifyFriends(player);
	}

	private void notifyFriends(L2PcInstance cha)
	{
		java.sql.Connection con = null;

		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();

			L2PcInstance friend;
			String friendName;

			while (rset.next())
			{
				friendName = rset.getString("friend_name");

				friend = L2World.getInstance().getPlayer(friendName);

				if (friend != null)
				{
					friend.sendPacket(new FriendList(friend));
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e) {
			_log.warning("could not restore friend data:"+e);
		}
		finally {
			try {con.close();} catch (Exception e){}
		}
	}

	@Override
	public String getType()
	{
		return _C__09_LOGOUT;
	}
}