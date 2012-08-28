package l2p.gameserver.taskmanager.actionrunner.tasks;

import java.util.List;
import java.util.Set;
import l2p.commons.dao.JdbcEntityState;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.MailDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.model.mail.Mail;
import l2p.gameserver.model.mail.Mail.SenderType;
import l2p.gameserver.serverpackets.ExNoticePostArrived;

public class DeleteExpiredMailTask extends AutomaticTask
{
  public void doTask()
    throws Exception
  {
    int expireTime = (int)(System.currentTimeMillis() / 1000L);

    List mails = MailDAO.getInstance().getExpiredMail(expireTime);

    for (Mail mail : mails)
    {
      if (!mail.getAttachments().isEmpty())
      {
        if (mail.getType() == Mail.SenderType.NORMAL)
        {
          Player player = World.getPlayer(mail.getSenderId());

          Mail reject = mail.reject();
          mail.delete();
          reject.setExpireTime(expireTime + 1296000);
          reject.save();

          if (player != null)
          {
            player.sendPacket(ExNoticePostArrived.STATIC_TRUE);
            player.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
          }

        }
        else
        {
          mail.setExpireTime(expireTime + 86400);
          mail.setJdbcState(JdbcEntityState.UPDATED);
          mail.update();
        }
      }
      else
      {
        mail.delete();
      }
    }
  }

  public long reCalcTime(boolean start)
  {
    return System.currentTimeMillis() + 600000L;
  }
}