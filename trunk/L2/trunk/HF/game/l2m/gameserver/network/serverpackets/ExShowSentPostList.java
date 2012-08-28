package l2m.gameserver.network.serverpackets;

import java.util.List;
import java.util.Set;
import l2p.commons.collections.CollectionUtils;
import l2m.gameserver.data.dao.MailDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.mail.Mail;
import l2m.gameserver.model.mail.Mail.SenderType;

public class ExShowSentPostList extends L2GameServerPacket
{
  private final List<Mail> mails;

  public ExShowSentPostList(Player cha)
  {
    mails = MailDAO.getInstance().getSentMailByOwnerId(cha.getObjectId());
    CollectionUtils.eqSort(mails);
  }

  protected void writeImpl()
  {
    writeEx(172);
    writeD((int)(System.currentTimeMillis() / 1000L));
    writeD(mails.size());
    for (Mail mail : mails)
    {
      writeD(mail.getMessageId());
      writeS(mail.getTopic());
      writeS(mail.getReceiverName());
      writeD(mail.isPayOnDelivery() ? 1 : 0);
      writeD(mail.getExpireTime());
      writeD(mail.isUnread() ? 1 : 0);
      writeD(mail.getType() == Mail.SenderType.NORMAL ? 0 : 1);
      writeD(mail.getAttachments().isEmpty() ? 0 : 1);
    }
  }
}