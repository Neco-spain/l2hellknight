package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;
import l2m.gameserver.data.dao.MailDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.mail.Mail;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowSentPostList;
import org.apache.commons.lang3.ArrayUtils;

public class RequestExDeleteSentPost extends L2GameClientPacket
{
  private int _count;
  private int[] _list;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 4 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _list = new int[_count];
    for (int i = 0; i < _count; i++)
      _list[i] = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (_count == 0)) {
      return;
    }
    Collection mails = MailDAO.getInstance().getSentMailByOwnerId(activeChar.getObjectId());
    if (!mails.isEmpty())
    {
      for (Mail mail : mails) {
        if ((ArrayUtils.contains(_list, mail.getMessageId())) && 
          (mail.getAttachments().isEmpty()))
        {
          MailDAO.getInstance().deleteSentMailByMailId(activeChar.getObjectId(), mail.getMessageId());
        }
      }
    }
    activeChar.sendPacket(new ExShowSentPostList(activeChar));
  }
}