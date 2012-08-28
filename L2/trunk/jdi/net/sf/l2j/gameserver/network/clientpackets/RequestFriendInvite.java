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
import net.sf.l2j.gameserver.network.serverpackets.AskJoinFriend;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public final class RequestFriendInvite extends L2GameClientPacket
{
  private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";
  private static Logger _log = Logger.getLogger(RequestFriendInvite.class.getName());
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

    L2PcInstance friend = L2World.getInstance().getPlayer(_name);
    _name = Util.capitalizeFirst(_name);

    if (friend == null)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }
    if (friend.isInOlympiadMode())
    {
      activeChar.sendMessage("You target in olympiad mode.");
      return;
    }
    if (friend == activeChar)
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
      activeChar.sendPacket(sm);
      sm = null;
      return;
    }

    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement statement = con.prepareStatement("SELECT char_id FROM character_friends WHERE char_id=? AND friend_id=?");
      statement.setInt(1, activeChar.getObjectId());
      statement.setInt(2, friend.getObjectId());
      ResultSet rset = statement.executeQuery();

      if (rset.next())
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
        sm.addString(_name);
      }
      else if (!friend.isProcessingRequest())
      {
        activeChar.onTransactionRequest(friend);
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUESTED_TO_BECOME_FRIENDS);
        sm.addString(_name);
        AskJoinFriend ajf = new AskJoinFriend(activeChar.getName());
        friend.sendPacket(ajf);
      }
      else
      {
        sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
      }

      friend.sendPacket(sm);
      SystemMessage sm = null;
      rset.close();
      statement.close();
    }
    catch (Exception e)
    {
      _log.log(Level.WARNING, "could not add friend objectid: ", e);
    }
    finally {
      try {
        con.close();
      } catch (Exception e) {
      }
    }
  }

  public String getType() {
    return "[C] 5E RequestFriendInvite";
  }
}