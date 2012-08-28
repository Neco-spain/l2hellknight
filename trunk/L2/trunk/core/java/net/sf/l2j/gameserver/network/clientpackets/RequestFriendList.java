package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestFriendList.class.getName());
	private static final String _C__60_REQUESTFRIENDLIST = "[C] 60 RequestFriendList";

	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		SystemMessage sm;
		java.sql.Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT friend_id, friend_name FROM character_friends WHERE char_id=?");
			statement.setInt(1, activeChar.getObjectId());

			ResultSet rset = statement.executeQuery();

			//======<Friend List>======
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_HEAD));

            L2PcInstance friend = null;
			while (rset.next())
			{
				// int friendId = rset.getInt("friend_id");
				String friendName = rset.getString("friend_name");
				friend = L2World.getInstance().getPlayer(friendName);

				if (friend == null)
				{
				    //	(Currently: Offline)
				    sm = new SystemMessage(SystemMessageId.S1_OFFLINE);
				    sm.addString(friendName);
				}
				else
				{
				    //(Currently: Online)
				    sm = new SystemMessage(SystemMessageId.S1_ONLINE);
				    sm.addString(friendName);
				}

				activeChar.sendPacket(sm);
			}

			//=========================
			activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
			sm = null;
			rset.close();
			statement.close();
		}
		catch (Exception e) {
			_log.warning("Error in /friendlist for " + activeChar + ": " + e);
		}
		finally	{
			try {con.close();} catch (Exception e) {}
		}
	}

	@Override
	public String getType()
	{
		return _C__60_REQUESTFRIENDLIST;
	}
}
