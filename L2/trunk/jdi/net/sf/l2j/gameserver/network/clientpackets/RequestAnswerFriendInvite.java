package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendManage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
  private static final String _C__5F_REQUESTANSWERFRIENDINVITE = "[C] 5F RequestAnswerFriendInvite";
  private static Logger _log = Logger.getLogger(RequestAnswerFriendInvite.class.getName());
  private int _response;

  protected void readImpl()
  {
    _response = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player != null)
    {
      L2PcInstance requestor = player.getActiveRequester();
      if (requestor == null) {
        return;
      }
      if (_response == 1)
      {
        Connection con = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name) VALUES (?, ?, ?), (?, ?, ?)");
          statement.setInt(1, requestor.getObjectId());
          statement.setInt(2, player.getObjectId());
          statement.setString(3, player.getName());
          statement.setInt(4, player.getObjectId());
          statement.setInt(5, requestor.getObjectId());
          statement.setString(6, requestor.getName());
          statement.execute();
          statement.close();
          SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
          requestor.sendPacket(msg);

          msg = new SystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
          msg.addString(player.getName());
          requestor.sendPacket(msg);

          msg = new SystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
          msg.addString(requestor.getName());
          player.sendPacket(msg);
          msg = null;
          requestor.sendPacket(new FriendManage(player, true));
          player.sendPacket(new FriendManage(requestor, true));
        }
        catch (Exception e)
        {
          _log.warning("could not add friend objectid: " + e);
        }
        finally {
          try {
            con.close(); } catch (Exception e) {
          }
        }
      } else {
        SystemMessage msg = new SystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
        requestor.sendPacket(msg);
        msg = null;
      }

      player.setActiveRequester(null);
      requestor.onTransactionResponse();
    }
  }

  public String getType()
  {
    return "[C] 5F RequestAnswerFriendInvite";
  }
}