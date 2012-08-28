package net.sf.l2j.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class FriendList extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(FriendList.class.getName());
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";

    private L2PcInstance _activeChar;

    public FriendList(L2PcInstance character)
    {
    	_activeChar = character;
    }

	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
			return;

        Connection con = null;

		try
		{
			String sqlQuery = "SELECT friend_id, friend_name FROM character_friends WHERE " +
                    "char_id=" + _activeChar.getObjectId() + " ORDER BY friend_name ASC";

			con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(sqlQuery);
			ResultSet rset = statement.executeQuery(sqlQuery);

			// Obtain the total number of friend entries for this player.
			rset.last();

            if (rset.getRow() > 0)
            {

            	writeC(0xfa);
    			writeH(rset.getRow());

    			rset.beforeFirst();

    			while (rset.next())
    			{
                    int friendId = rset.getInt("friend_id");
    				String friendName = rset.getString("friend_name");

    				if (friendId == _activeChar.getObjectId())
                        continue;

    				L2PcInstance friend = L2World.getInstance().getPlayer(friendName);

    				writeH(0); // ??
    				writeD(friendId);
    				writeS(friendName);

    				if (friend == null)
    					writeD(0); // offline
    				else
    					writeD(1); // online

    				writeH(0); // ??
    			}
            }
            writeC(0xfa);
			rset.close();
			statement.close();
		}
		catch (Exception e)	{
			_log.warning("Error found in " + _activeChar.getName() + "'s FriendList: " + e);
		}
		finally	{
			try {con.close();} catch (Exception e) {}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
