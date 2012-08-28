package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendManage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendDel extends L2GameClientPacket
{
  private static final String _C__61_REQUESTFRIENDDEL = "[C] 61 RequestFriendDel";
  private static Logger _log = Logger.getLogger(RequestFriendDel.class.getName());
  private String _name;

  protected void readImpl()
  {
    _name = readS();
  }

  protected void runImpl()
  {
    Connection con = null;
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    try
    {
      L2PcInstance friend = L2World.getInstance().getPlayer(_name);
      con = L2DatabaseFactory.getInstance().getConnection();
      ResultSet rset;
      if (friend != null)
      {
        PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=? and friend_id=?");
        statement.setInt(1, activeChar.getObjectId());
        statement.setInt(2, friend.getObjectId());
        ResultSet rset = statement.executeQuery();
        if (!rset.next()) { statement.close();

          SystemMessage sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
          sm.addString(_name);
          activeChar.sendPacket(sm);
          sm = null;
          return; } } else { statement = con.prepareStatement("SELECT friend_id FROM character_friends, characters WHERE char_id=? AND friend_id=obj_id AND char_name=?");
        statement.setInt(1, activeChar.getObjectId());
        statement.setString(2, _name);
        rset = statement.executeQuery();
        if (!rset.next()) { statement.close();

          SystemMessage sm = new SystemMessage(SystemMessageId.S1_NOT_ON_YOUR_FRIENDS_LIST);
          sm.addString(_name);
          activeChar.sendPacket(sm);
          sm = null;
          return; } } int objectId = rset.getInt("friend_id");
      statement.close();
      rset.close();

      PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
      statement.setInt(1, activeChar.getObjectId());
      statement.setInt(2, objectId);
      statement.execute();
      statement.close();

      statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? AND friend_id=?");
      statement.setInt(1, objectId);
      statement.setInt(2, activeChar.getObjectId());
      statement.execute();
      statement.close();

      activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));
      activeChar.sendPacket(new FriendManage(_name, false, friend != null, objectId));
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
    finally {
      try {
        con.close();
      }
      catch (Exception e)
      {
      }
    }
  }

  public String getType() {
    return "[C] 61 RequestFriendDel";
  }
}