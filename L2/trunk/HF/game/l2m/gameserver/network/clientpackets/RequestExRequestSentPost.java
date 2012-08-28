package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.dao.MailDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.mail.Mail;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExReplySentPost;
import l2m.gameserver.network.serverpackets.ExShowSentPostList;

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