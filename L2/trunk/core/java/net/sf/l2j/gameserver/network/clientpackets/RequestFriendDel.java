/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendManage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendDel extends L2GameClientPacket{

	private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";
	private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		SystemMessage sm;
		java.sql.Connection con = null;
		L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

		try
		{
		    L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
		    ResultSet rset;
		    if (friend != null)
            {
    			statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=? and friend_id=?");
    			statement.setInt(1, activeChar.getObjectId());
    			statement.setInt(2, friend.getObjectId());
    			rset = statement.executeQuery();
    			if (!rset.next())
                {
    			    statement.close();
    			    // Player is not in your friendlist
    			    sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
    			    sm.addString(_name);
    			    activeChar.sendPacket(sm);
    			    sm = null;
    			    return;
    			}
		    } else
            {
    			statement = con.prepareStatement("SELECT friend_id FROM character_friends, characters WHERE char_id=? AND friend_id=obj_id AND char_name=?");
    			statement.setInt(1, activeChar.getObjectId());
    			statement.setString(2, _name);
    			rset = statement.executeQuery();
    			if (!rset.next())
                {
    				statement.close();
    				// Player is not in your friendlist
    				sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
    				sm.addString(_name);
    				activeChar.sendPacket(sm);
    				sm = null;
    				return;
    			}
		    }

			int objectId = rset.getInt("friend_id");
			statement.close();
            rset.close();

            statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
            statement.setInt(1, activeChar.getObjectId());
            statement.setInt(2, objectId);
            statement.execute();
            statement.close();

            statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
            statement.setInt(1, objectId);
            statement.setInt(2, activeChar.getObjectId());
            statement.execute();
            statement.close();
			// Player deleted from your friendlist
			activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(this._name));
		    activeChar.sendPacket(new FriendManage(this._name, false, friend != null, objectId));
		      if (friend != null)
		      {
		        friend.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(activeChar.getName()));
		        friend.sendPacket(new FriendManage(activeChar, false));
		      }
		}
		catch (Exception e)
		{
		    _log.log(Level.WARNING, "could not del friend objectid: ", e);
		}
		finally
		{
		    try { con.close(); } catch (Exception e) {}
		}

	}


	@Override
	public String getType()
	{
		return _C__61_REQUESTFRIENDDEL;
	}
}

