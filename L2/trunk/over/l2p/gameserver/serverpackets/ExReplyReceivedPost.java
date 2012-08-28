package l2p.gameserver.serverpackets;

import java.util.Set;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.model.mail.Mail.SenderType;

public class ExReplyReceivedPost extends L2GameServerPacket
{
  private final Mail mail;

  public ExReplyReceivedPost(Mail mail)
  {
    this.mail = mail;
  }

  protected void writeImpl()
  {
    writeEx(171);

    writeD(mail.getMessageId());
    writeD(mail.isPayOnDelivery() ? 1 : 0);
    writeD(mail.getType() == Mail.SenderType.NORMAL ? 0 : 1);

    writeS(mail.getSenderName());
    writeS(mail.getTopic());
    writeS(mail.getBody());

    writeD(mail.getAttachments().size());
    for (ItemInstance item : mail.getAttachments())
    {
      writeItemInfo(item);
      writeD(item.getObjectId());
    }

    writeQ(mail.getPrice());
    writeD(mail.getAttachments().size() > 0 ? 1 : 0);
    writeD(mail.getType().ordinal());
  }
}