package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.PreparedStatement;
import java.util.logging.Logger;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TransactionType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
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
      if (System.currentTimeMillis() - player.gCPBC() < 100L)
        return;
      player.sCPBC();

      L2PcInstance requestor = player.getTransactionRequester();

      player.setTransactionRequester(null);

      if (requestor == null) {
        return;
      }
      requestor.setTransactionRequester(null);

      if ((player.getTransactionType() != L2PcInstance.TransactionType.FRIEND) || (player.getTransactionType() != requestor.getTransactionType())) {
        return;
      }
      if (_response == 1)
      {
        Connect con = null;
        PreparedStatement statement = null;
        try
        {
          con = L2DatabaseFactory.getInstance().getConnection();
          statement = con.prepareStatement("INSERT INTO character_friends (char_id, friend_id, friend_name) VALUES (?, ?, ?), (?, ?, ?)");
          statement.setInt(1, requestor.getObjectId());
          statement.setInt(2, player.getObjectId());
          statement.setString(3, player.getName());
          statement.setInt(4, player.getObjectId());
          statement.setInt(5, requestor.getObjectId());
          statement.setString(6, requestor.getName());
          statement.execute();
          Close.S(statement);
          requestor.sendPacket(Static.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);

          requestor.sendPacket(SystemMessage.id(SystemMessageId.S1_ADDED_TO_FRIENDS).addString(player.getName()));

          player.sendPacket(SystemMessage.id(SystemMessageId.S1_JOINED_AS_FRIEND).addString(requestor.getName()));

          player.sendPacket(new FriendList(player));
          requestor.sendPacket(new FriendList(requestor));

          player.storeFriend(requestor.getObjectId(), requestor.getName());
          requestor.storeFriend(player.getObjectId(), player.getName());
        }
        catch (Exception e)
        {
          _log.warning("could not add friend objectid: " + e);
        }
        finally
        {
          Close.CS(con, statement);
        }
      }
      else {
        requestor.sendPacket(Static.FAILED_TO_INVITE_A_FRIEND);
      }
      requestor.setTransactionType(L2PcInstance.TransactionType.NONE);
      player.setTransactionType(L2PcInstance.TransactionType.NONE);
    }
  }
}