package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import l2p.gameserver.dao.MailDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExShowReceivedPostList;
import org.apache.commons.lang3.ArrayUtils;

public class RequestExDeleteReceivedPost extends L2GameClientPacket
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
    List mails = MailDAO.getInstance().getReceivedMailByOwnerId(activeChar.getObjectId());
    if (!mails.isEmpty())
    {
      for (Mail mail : mails) {
        if ((ArrayUtils.contains(_list, mail.getMessageId())) && 
          (mail.getAttachments().isEmpty()))
        {
          MailDAO.getInstance().deleteReceivedMailByMailId(activeChar.getObjectId(), mail.getMessageId());
        }
      }
    }
    activeChar.sendPacket(new ExShowReceivedPostList(activeChar));
  }
}