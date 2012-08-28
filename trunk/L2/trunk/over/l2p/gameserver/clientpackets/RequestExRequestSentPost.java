package l2p.gameserver.clientpackets;

import l2p.gameserver.dao.MailDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExReplySentPost;
import l2p.gameserver.serverpackets.ExShowSentPostList;

public class RequestExRequestSentPost extends L2GameClientPacket
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
    Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
    if (mail != null)
    {
      activeChar.sendPacket(new ExReplySentPost(mail));
      return;
    }

    activeChar.sendPacket(new ExShowSentPostList(activeChar));
  }
}