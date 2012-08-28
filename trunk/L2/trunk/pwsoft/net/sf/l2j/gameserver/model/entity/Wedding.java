package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.Config.PvpColor;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2WeddingManagerInstance;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;

public class Wedding
{
  private L2PcInstance groom = null;
  private L2PcInstance bride = null;
  private L2WeddingManagerInstance prist = null;
  private long expire = 0L;

  private boolean groomYes = false;
  private boolean brideYes = false;

  public boolean married = false;

  public Wedding(L2PcInstance groom, L2PcInstance bride, L2WeddingManagerInstance prist)
  {
    this.groom = groom;
    this.bride = bride;
    this.prist = prist;
    expire = (System.currentTimeMillis() + 60000L);

    ThreadPoolManager.getInstance().scheduleAi(new Say(0), 3000L, false);
  }

  public void sayYes(L2PcInstance player)
  {
    if (player.equals(groom))
    {
      groomYes = true;
      groom.sayString(groom.getName() + ": \u042F \u0441\u043E\u0433\u043B\u0430\u0441\u0435\u043D!", 18);
      ThreadPoolManager.getInstance().scheduleAi(new Step(2), 1000L, false);
    }
    else
    {
      brideYes = true;
      bride.sayString(bride.getName() + ": \u042F \u0441\u043E\u0433\u043B\u0430\u0441\u043D\u0430!", 18);
      ThreadPoolManager.getInstance().scheduleAi(new Step(3), 1000L, false);
    }
  }

  public void sayNo(L2PcInstance player) {
    if (player.equals(groom))
    {
      if (groom != null)
        groom.sayString("\u041D\u0435\u0442!", 18);
      ThreadPoolManager.getInstance().scheduleAi(new Say(99), 100L, false);
    }
    else
    {
      if (bride != null)
        bride.sayString("\u041D\u0435\u0442!", 18);
      ThreadPoolManager.getInstance().scheduleAi(new Say(99), 100L, false);
    }
  }

  public void broadcastHtml(String text)
  {
    if ((groom != null) && (groom.getParty() != null))
      groom.getParty().broadcastHtmlToPartyMembers(text);
  }

  public L2PcInstance getGroom()
  {
    return groom;
  }

  public L2PcInstance getBride()
  {
    return bride;
  }

  public boolean isExpired()
  {
    return System.currentTimeMillis() > expire;
  }

  public void clear()
  {
    groom = null;
    bride = null;
    prist = null;
  }

  private class Step
    implements Runnable
  {
    int id;

    Step(int id)
    {
      this.id = id;
    }

    public void run()
    {
      switch (id)
      {
      case 1:
        prist.sayString(groom.getName() + "! \u0421\u043E\u0433\u043B\u0430\u0441\u0435\u043D \u043B\u0438 \u0442\u044B \u0432\u0437\u044F\u0442\u044C \u0432 \u0436\u0435\u043D\u044B " + bride.getName() + "?", 18);
        groom.setEngageRequest(true, groom.getObjectId());
        groom.sendPacket(new ConfirmDlg(614, "\u0421\u043E\u0433\u043B\u0430\u0441\u0435\u043D \u043B\u0438 \u0442\u044B \u0432\u0437\u044F\u0442\u044C \u0432 \u0436\u0435\u043D\u044B " + bride.getName() + "?"));
        break;
      case 2:
        prist.sayString(bride.getName() + "! \u0421\u043E\u0433\u043B\u0430\u0441\u043D\u0430 \u043B\u0438 \u0442\u044B \u0432\u0437\u044F\u0442\u044C \u0432 \u043C\u0443\u0436\u044C\u044F " + groom.getName() + "?", 18);
        bride.setEngageRequest(true, groom.getObjectId());
        bride.sendPacket(new ConfirmDlg(614, "\u0421\u043E\u0433\u043B\u0430\u0441\u043D\u0430 \u043B\u0438 \u0442\u044B \u0432\u0437\u044F\u0442\u044C \u0432 \u043C\u0443\u0436\u044C\u044F " + groom.getName() + "?"));
        break;
      case 3:
        if ((groomYes) && (brideYes))
          ThreadPoolManager.getInstance().scheduleAi(new Wedding.Say(Wedding.this, 8), 1000L, false);
        else
          ThreadPoolManager.getInstance().scheduleAi(new Wedding.Say(Wedding.this, 99), 1000L, false);
      }
    }
  }

  private class Say
    implements Runnable
  {
    int id;

    Say(int id)
    {
      this.id = id;
    }

    public void run()
    {
      if (married) {
        return;
      }
      int next = id + 1;
      switch (id)
      {
      case 0:
        prist.sayString("\u0423\u0432\u0430\u0436\u0430\u0435\u043C\u044B\u0435 \u0433\u043E\u0441\u0442\u0438, \u0434\u043E\u0440\u043E\u0433\u0438\u0435 \u043D\u043E\u0432\u043E\u0431\u0440\u0430\u0447\u043D\u044B\u0435!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 1:
        prist.sayString("\u041C\u044B \u0440\u0430\u0434\u044B \u0432\u0430\u0441 \u043F\u0440\u0438\u0432\u0435\u0442\u0441\u0442\u0432\u043E\u0432\u0430\u0442\u044C \u0432\u043E \u0434\u0432\u043E\u0440\u0446\u0435 \u0431\u0440\u0430\u043A\u043E\u0441\u043E\u0447\u0435\u0442\u0430\u043D\u0438\u044F \u0433\u0440\u0430\u0436\u0434\u0430\u043D\u0441\u043A\u043E\u0439 \u044D\u0440\u044B.", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 2:
        prist.sayString("\u0414\u043E\u0440\u043E\u0433\u0438\u0435 " + groom.getName() + " \u0438 " + bride.getName() + "!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 3:
        prist.sayString("\u0421\u0435\u0433\u043E\u0434\u043D\u044F, \u0441\u0430\u043C\u044B\u0439 \u043D\u0435\u0437\u0430\u0431\u044B\u0432\u0430\u0435\u043C\u044B\u0439 \u0434\u0435\u043D\u044C \u0432 \u0432\u0430\u0448\u0435\u0439 \u0436\u0438\u0437\u043D\u0438, \u0432\u044B \u0432\u0441\u0442\u0443\u043F\u0430\u0435\u0442\u0435 \u0432 \u0431\u0440\u0430\u043A!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 4:
        prist.sayString("\u0412\u044B \u0441\u0442\u043E\u0438\u0442\u0435 \u043D\u0430 \u043F\u043E\u0440\u043E\u0433\u0435 \u043D\u043E\u0432\u043E\u0433\u043E \u044D\u0442\u0430\u043F\u0430 \u0441\u0432\u043E\u0435\u0439 \u0436\u0438\u0437\u043D\u0438!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 5:
        prist.sayString("\u0427\u0435\u043B\u043E\u0432\u0435\u043A \u043E\u0431\u0440\u0435\u0442\u0430\u0435\u0442 \u043D\u043E\u0432\u044B\u0435 \u043E\u0449\u0443\u0449\u0435\u043D\u0438\u044F \u0432 \u0436\u0438\u0437\u043D\u0438, \u0432\u043E \u0432\u0441\u0435\u0439 \u0435\u0451 \u043A\u0440\u0430\u0441\u0435!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 6:
        prist.sayString("\u0421\u0442\u0430\u043D\u043E\u0432\u0438\u0442\u0441\u044F \u043C\u0443\u0434\u0440\u0435\u0435, \u043F\u0435\u0440\u0435\u0434 \u043D\u0438\u043C \u043E\u0442\u043A\u0440\u044B\u0432\u0430\u044E\u0442\u0441\u044F \u0441\u043E\u0432\u0435\u0440\u0448\u0435\u043D\u043D\u043E \u0438\u043D\u043D\u044B\u0435 \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E\u0441\u0442\u0438!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 7:
        prist.sayString("\u041F\u043E\u0437\u0432\u043E\u043B\u044C\u0442\u0435 \u0441\u043F\u0440\u043E\u0441\u0438\u0442\u044C \u0432\u0430\u0441, \u044F\u0432\u043B\u044F\u0435\u0442\u0441\u044F \u043B\u0438 \u0432\u0430\u0448\u0435 \u0436\u0435\u043B\u0430\u043D\u0438\u0435 \u0441\u0442\u0430\u0442\u044C \u043C\u0443\u0436\u0435\u043C \u0438 \u0436\u0435\u043D\u043E\u0439, \u0432\u0437\u0430\u0438\u043C\u043D\u044B\u043C \u0438 \u0438\u0441\u043A\u0440\u0435\u043D\u043D\u0438\u043C!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Wedding.Step(Wedding.this, 1), 2000L, false);
        break;
      case 8:
        prist.sayString("\u041F\u043E \u0432\u0430\u0448\u0435\u043C\u0443 \u0432\u0437\u0430\u0438\u043C\u043D\u043E\u043C\u0443 \u0436\u0435\u043B\u0430\u043D\u0438\u044E \u0438 \u0441\u043E\u0433\u043B\u0430\u0441\u0438\u044E, \u0431\u0440\u0430\u043A \u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u0443\u0435\u0442\u0441\u044F!", 18);
        CoupleManager.getInstance().createCouple(groom, bride);
        prist.broadcastPacket(new MagicSkillUser(prist, prist, 2230, 1, 1, 0));
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 2000L, false);
        break;
      case 9:
        prist.broadcastPacket(new MagicSkillUser(prist, prist, 2025, 1, 1, 0));
        prist.sayString("\u0412 \u0441\u043E\u043E\u0442\u0432\u0435\u0442\u0441\u0442\u0432\u0438\u0438 \u0441 \u0441\u0435\u043C\u0435\u0439\u043D\u044B\u043C \u043A\u043E\u0434\u0435\u043A\u0441\u043E\u043C, \u0432\u0430\u0448 \u0431\u0440\u0430\u043A \u0437\u0430\u0440\u0435\u0433\u0435\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 1000L, false);
        break;
      case 10:
        CoupleManager.getInstance().getCouple(groom.getCoupleId()).marry();
        groom.setMarried(true);
        groom.setMaryRequest(false);
        bride.setMarried(true);
        bride.setMaryRequest(false);

        if (Config.L2JMOD_WEDDING_BOW)
        {
          bride.addItem("WeddingBow", 9140, 1, bride, true);
          groom.addItem("WeddingBow", 9140, 1, groom, true);
        }

        groom.broadcastPacket(new MagicSkillUser(groom, groom, 2025, 1, 1, 0));
        bride.broadcastPacket(new MagicSkillUser(bride, bride, 2025, 1, 1, 0));
        prist.sayString("\u041E\u0431\u044A\u044F\u0432\u043B\u044F\u044E \u0432\u0430\u0441 \u043C\u0443\u0436\u0435\u043C \u0438 \u0436\u0435\u043D\u043E\u0439! \u041C\u043E\u0436\u0435\u0442\u0435 \u043F\u043E\u0437\u0434\u0440\u0430\u0432\u0438\u0442\u044C \u0434\u0440\u0443\u0433 \u0434\u0440\u0443\u0433\u0430!", 18);

        Announcements.getInstance().announceToAll("\u041F\u043E\u0437\u0434\u0440\u0430\u0432\u043B\u044F\u0435\u043C \u043C\u043E\u043B\u043E\u0434\u043E\u0436\u0435\u043D\u043E\u0432 " + groom.getName() + " \u0438 " + bride.getName() + "! \u041B\u044E\u0431\u0432\u0438 \u0438 \u0441\u0447\u0430\u0441\u0442\u044C\u044F \u0432\u0430\u043C!");
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);

        if (Config.WEDDING_COLORS.nick != 16777215)
        {
          groom.getAppearance().setNameColor(Config.WEDDING_COLORS.nick);
          groom.broadcastUserInfo();
          groom.store();
        }
        if (Config.WEDDING_COLORS.title == 16777215)
          break;
        bride.getAppearance().setNameColor(Config.WEDDING_COLORS.title);
        bride.broadcastUserInfo();
        bride.store(); break;
      case 11:
        prist.sayString("\u041F\u0440\u0438\u0434\u0430\u0434\u0438\u0442\u0435 \u043A\u0430\u0436\u0434\u043E\u043C\u0443 \u0434\u043D\u044E \u0441\u0432\u043E\u0435\u0439 \u0436\u0438\u0437\u043D\u0438 \u043D\u0435\u043F\u043E\u0432\u0442\u043E\u0440\u0438\u043C\u043E\u0441\u0442\u044C \u0438 \u0441\u043A\u0430\u0437\u043E\u0447\u043D\u043E\u0441\u0442\u044C \u043E\u0442\u043D\u043E\u0448\u0435\u043D\u0438\u0439, \u0431\u0435\u0440\u0435\u0433\u0438\u0442\u0435 \u0438\u0445!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 12:
        prist.sayString("\u0412\u043D\u0438\u043C\u0430\u043D\u0438\u0435 \u0438 \u0434\u043E\u0432\u0435\u0440\u0438\u0435 \u0432 \u0432\u0430\u0448\u0435\u043C \u0434\u043E\u043C\u0435, \u043F\u043E\u043C\u043E\u0433\u0443\u0442 \u043F\u0435\u0440\u0435\u0436\u0438\u0442\u044C \u0432\u0441\u0435 \u0442\u0440\u0443\u0434\u043D\u043E\u0441\u0442\u0438 \u0438 \u0441\u043B\u043E\u0436\u043D\u043E\u0441\u0442\u0438 \u0441\u0443\u0434\u044C\u0431\u044B!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 13:
        prist.sayString("\u0421\u0447\u0430\u0441\u0442\u044C\u044F \u0432\u0430\u043C \u0438 \u0441\u0435\u043C\u0435\u0439\u043D\u043E\u0433\u043E \u0431\u043B\u0430\u0433\u043E\u043F\u043E\u043B\u0443\u0447\u0438\u044F!", 18);
        ThreadPoolManager.getInstance().scheduleAi(new Say(Wedding.this, next), 3000L, false);
        break;
      case 14:
        married = true;
        prist.sayString("\u0411\u0435\u0440\u0435\u0433\u0438\u0442\u0435 \u0434\u0440\u0443\u0433 \u0434\u0440\u0443\u0433\u0430! \u0411\u0443\u0434\u044C\u0442\u0435 \u0441\u0447\u0430\u0441\u0442\u043B\u0438\u0432\u044B!!!", 18);
        prist.sayString("\u0421\u0432\u0430\u0434\u044C\u0431\u0430 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043D\u0430!!!", 18);
        prist.finish(false);
        break;
      case 99:
        prist.finish(true);
      }
    }
  }
}