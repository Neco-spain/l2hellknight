package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class RequestFriendDel extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    int charId = player.getObjectId();

    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      L2PcInstance friend = L2World.getInstance().getPlayer(_name);
      int objectId = 0;
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      if (friend != null)
      {
        if (player.haveFriend(friend.getObjectId()))
        {
          objectId = friend.getObjectId();
        }
      }
      else
      {
        statement = con.prepareStatement("SELECT friend_id FROM character_friends, characters WHERE char_id=? AND friend_id=obj_Id AND char_name=?");
        statement.setInt(1, charId);
        statement.setString(2, _name);
        rset = statement.executeQuery();
        if (rset.next())
        {
          objectId = rset.getInt("friend_id");
        }
        Close.SR(statement, rset);
      }

      if (objectId == 0) {
        player.sendPacket(SystemMessage.id(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST).addString(_name));
        return;
      }
      con.setAutoCommit(false);
      statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
      statement.setInt(1, charId);
      statement.setInt(2, objectId);
      statement.addBatch();

      statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
      statement.setInt(1, objectId);
      statement.setInt(2, charId);
      statement.addBatch();
      statement.executeBatch();
      con.commit();

      player.sendPacket(SystemMessage.id(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));

      player.deleteFriend(objectId);
      player.sendPacket(new FriendList(player));
      if (friend != null)
      {
        friend.deleteFriend(charId);
        friend.sendPacket(new FriendList(friend));
      }
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not del friend objectid: ", e);
    }
    finally
    {
      Close.CSR(con, statement, rset);
    }
  }
}