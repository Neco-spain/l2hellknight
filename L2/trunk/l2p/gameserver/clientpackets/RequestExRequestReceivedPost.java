package l2p.gameserver.clientpackets;

import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.dao.MailDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExChangePostState;
import l2p.gameserver.serverpackets.ExReplyReceivedPost;
import l2p.gameserver.serverpackets.ExShowReceivedPostList;

public class RequestExRequestReceivedPost extends L2GameClientPacket
{
  private int postId;

  protected void readImpl()
  {
    postId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Mail mail = MailDAO.getInstance().getReceivedMailByMailId(activeChar.getObjectId(), postId);
    if (mail != null)
    {
      if (mail.isUnread())
      {
        mail.setUnread(false);
        mail.setJdbcState(JdbcEntityState.UPDATED);
        mail.update();
        activeChar.sendPacket(new ExChangePostState(true, 1, new Mail[] { mail }));
      }

      activeChar.sendPacket(new ExReplyReceivedPost(mail));
      return;
    }

    activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
  }
}