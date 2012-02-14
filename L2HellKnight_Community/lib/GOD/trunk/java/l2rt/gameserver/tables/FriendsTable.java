package l2rt.gameserver.tables;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.base.Transaction;
import l2rt.gameserver.model.base.Transaction.TransactionType;
import l2rt.gameserver.network.serverpackets.FriendAddRequest;
import l2rt.gameserver.network.serverpackets.L2Friend;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.util.GArray;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FriendsTable
{
	private static final Logger _log = Logger.getLogger(FriendsTable.class.getName());

	private static FriendsTable _instance;

	private HashMap<Integer, GArray<Integer>> _friends;

	public synchronized static FriendsTable getInstance()
	{
		if(_instance == null)
			_instance = new FriendsTable();
		return _instance;
	}

	private FriendsTable()
	{
		_friends = new HashMap<Integer, GArray<Integer>>();
		RestoreFriendsData();
	}

	private void RestoreFriendsData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet friendsdata = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_id, friend_id FROM character_friends");
			friendsdata = statement.executeQuery();

			int i = 0;

			while(friendsdata.next())
			{
				add(friendsdata.getInt("char_id"), friendsdata.getInt("friend_id"));
				i++;
			}

			_log.config("FriendsTable: Loaded " + i + " friends.");
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "Error while loading friends table!", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, friendsdata);
		}
	}

	private void add(int char_id, int friend_id)
	{
		GArray<Integer> friends = _friends.get(char_id);
		if(friends == null)
		{
			friends = new GArray<Integer>(1);
			_friends.put(char_id, friends);
		}
		friends.add(friend_id);
	}

	public void addFriend(L2Player player1, L2Player player2)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("replace into character_friends (char_id,friend_id) values(?,?)");
			statement.setInt(1, player1.getObjectId());
			statement.setInt(2, player2.getObjectId());
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("replace into character_friends (char_id,friend_id) values(?,?)");
			statement.setInt(1, player2.getObjectId());
			statement.setInt(2, player1.getObjectId());
			statement.execute();

			add(player1.getObjectId(), player2.getObjectId());
			add(player2.getObjectId(), player1.getObjectId());

			player1.sendPacket(Msg.YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND, new SystemMessage(SystemMessage.S1_HAS_BEEN_ADDED_TO_YOUR_FRIEND_LIST).addString(player2.getName()), new L2Friend(player2, true));
			player2.sendPacket(new SystemMessage(SystemMessage.S1_HAS_JOINED_AS_A_FRIEND).addString(player1.getName()), new L2Friend(player1, true));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public boolean TryFriendDelete(L2Player activeChar, String delFriend)
	{
		if(activeChar == null || delFriend == null || delFriend.isEmpty())
			return false;

		delFriend = delFriend.trim();

		L2Player friendChar = L2World.getPlayer(delFriend);
		if(friendChar != null)
			delFriend = friendChar.getName();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name LIKE ? LIMIT 1");
			statement.setString(1, delFriend);
			rset = statement.executeQuery();
			if(!rset.next())
			{
				System.out.println("FriendsTable: not found char to delete: " + delFriend);
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(delFriend));
				return false;
			}

			int friendId = rset.getInt("obj_Id");
			if(!checkIsFriends(activeChar.getObjectId(), friendId))
			{
				System.out.println("FriendsTable: not in friend list: " + activeChar.getObjectId() + ", " + delFriend);
				activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(delFriend));
				return false;
			}

			DatabaseUtils.closeDatabaseSR(statement, rset);
			rset = null;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id=? AND friend_id=?) OR (char_id=? AND friend_id=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, friendId);
			statement.setInt(3, friendId);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();

			GArray<Integer> friends = _friends.get(activeChar.getObjectId());
			if(friends != null)
				friends.remove(new Integer(friendId));

			friends = _friends.get(friendId);
			if(friends != null)
				friends.remove(new Integer(activeChar.getObjectId()));

			//Player deleted from your friendlist
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIEND_LIST).addString(delFriend), new L2Friend(delFriend, false, friendChar != null, friendId)); //Офф посылает 0xFB Friend, хотя тут нету разници что именно посылать
			if(friendChar != null)
				friendChar.sendPacket(new SystemMessage(SystemMessage.S1__HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(activeChar.getName()), new L2Friend(activeChar, false)); //Офф посылает 0xFB Friend, хотя тут нету разници что именно посылать
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return true;
	}

	public boolean TryFriendInvite(L2Player activeChar, String addFriend)
	{
		if(activeChar == null || addFriend == null || addFriend.isEmpty())
			return false;

		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}

		if(activeChar.getName().equalsIgnoreCase(addFriend))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST);
			return false;
		}

		L2Player friendChar = L2World.getPlayer(addFriend);
		if(friendChar == null)
		{
			activeChar.sendPacket(Msg.THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME);
			return false;
		}

		if(friendChar.isBlockAll() || friendChar.isInBlockList(activeChar) || friendChar.getMessageRefusal())
		{
			activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
			return false;
		}

		if(friendChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER);
			return false;
		}

		if(FriendsTable.getInstance().checkIsFriends(activeChar.getObjectId(), activeChar.getObjectId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_ALREADY_ON_YOUR_FRIEND_LIST).addString(friendChar.getName()));
			return false;
		}

		new Transaction(TransactionType.FRIEND, activeChar, friendChar, 10000);
		friendChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_REQUESTED_TO_BECOME_FRIENDS).addString(activeChar.getName()), new FriendAddRequest(activeChar.getName()));

		return true;
	}

	public GArray<Integer> getFriendsList(int char_id)
	{
		GArray<Integer> friends = _friends.get(char_id);
		if(friends == null)
			friends = new GArray<Integer>(0);
		return friends;
	}

	public boolean checkIsFriends(int char_id, int friend_id)
	{
		for(Integer obj_id : getFriendsList(char_id))
			if(obj_id != null && obj_id.equals(friend_id))
				return true;
		for(Integer obj_id : getFriendsList(friend_id))
			if(obj_id != null && obj_id.equals(char_id))
			{
				System.out.println("FriendsTable: corrupted friends table! " + char_id + "," + friend_id);
				return true;
			}
		return false;
	}
}