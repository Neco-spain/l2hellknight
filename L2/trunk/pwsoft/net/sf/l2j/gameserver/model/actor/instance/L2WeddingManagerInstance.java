package net.sf.l2j.gameserver.model.actor.instance;

import javolution.util.FastTable;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.entity.Wedding;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2NpcInstance
{
  private boolean _active = false;
  private Wedding wed = null;

  public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (!Config.L2JMOD_ALLOW_WEDDING)
    {
      player.sendHtmlMessage("\u0412 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u044F \u0432 \u043E\u0442\u043F\u0443\u0441\u043A\u0435.");
      return;
    }

    if (_active)
    {
      player.sendHtmlMessage("\u0412 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u044F \u0437\u0430\u043D\u044F\u0442\u0430.<br> \u0418\u043D\u0442\u0435\u0440\u0432\u0430\u043B \u043F\u0440\u043E\u0432\u0435\u0434\u0435\u043D\u0438\u044F \u0441\u0432\u0430\u0434\u0435\u0431: " + Config.L2JMOD_WEDDING_INTERVAL / 1000 + " \u0441\u0435\u043A\u0443\u043D\u0434.");
      return;
    }

    if (command.equalsIgnoreCase("engage"))
    {
      L2Party party = player.getParty();
      if (party == null)
      {
        player.sendHtmlMessage("\u0412\u044B \u0434\u043E\u043B\u0436\u043D\u044B \u0431\u044B\u0442\u044C \u0432\u0434\u0432\u043E\u0435\u043C \u0432 \u043F\u0430\u0442\u0438 \u0441\u043E \u0441\u0432\u043E\u0438\u043C \u043F\u0430\u0440\u0442\u043D\u0435\u0440\u043E\u043C.");
        return;
      }

      if (player.getAppearance().getSex())
      {
        party.broadcastHtmlToPartyMembers("\u042F \u0431\u0443\u0434\u0443 \u0440\u0430\u0437\u0433\u043E\u0432\u0430\u0440\u0438\u0432\u0430\u0442\u044C \u0442\u043E\u043B\u044C\u043A\u043E \u0441 \u0436\u0435\u043D\u0438\u0445\u043E\u043C.");
        return;
      }

      if (party.getMemberCount() != 2)
      {
        party.broadcastHtmlToPartyMembers("\u0422\u043E\u043B\u044C\u043A\u043E \u0436\u0435\u043D\u0438\u0445 \u0438 \u043D\u0435\u0432\u0435\u0441\u0442\u0430 \u0434\u043E\u043B\u0436\u043D\u044B \u0431\u044B\u0442\u044C \u0432 \u043F\u0430\u0442\u0438.");
        return;
      }

      if (!party.isLeader(player))
      {
        party.broadcastHtmlToPartyMembers("\u0416\u0435\u043D\u0438\u0445 \u0434\u043E\u043B\u0436\u0435\u043D \u0431\u044B\u0442\u044C \u043B\u0438\u0434\u0435\u0440\u043E\u043C \u043F\u0430\u0442\u0438.");
        return;
      }

      if (player.getAppearance().getSex() == ((L2PcInstance)party.getPartyMembers().get(1)).getAppearance().getSex())
      {
        party.broadcastHtmlToPartyMembers("\u041E\u0434\u043D\u043E\u043F\u043E\u043B\u044B\u0435 \u0431\u0440\u0430\u043A\u0438 \u0437\u0430\u043F\u0440\u0435\u0449\u0435\u043D\u044B!");
        return;
      }

      if ((player.isMarried()) || (((L2PcInstance)party.getPartyMembers().get(1)).isMarried()))
      {
        String married = ((L2PcInstance)party.getPartyMembers().get(1)).getName();
        if (player.isMarried()) {
          married = player.getName();
        }
        party.broadcastHtmlToPartyMembers(married + " \u0443\u0436\u0435 \u0432 \u0431\u0440\u0430\u043A\u0435!!");
        return;
      }

      if (Config.L2JMOD_WEDDING_COIN > 0)
      {
        L2ItemInstance coin = player.getInventory().getItemByItemId(Config.L2JMOD_WEDDING_COIN);
        if ((coin == null) || (coin.getCount() < Config.L2JMOD_WEDDING_PRICE))
        {
          party.broadcastHtmlToPartyMembers("\u0416\u0435\u043D\u0438\u0445 \u0434\u043E\u043B\u0436\u0435\u043D \u043E\u043F\u043B\u0430\u0442\u0438\u0442\u044C: " + Config.L2JMOD_WEDDING_PRICE + " " + Config.L2JMOD_WEDDING_COINNAME + ".");
          return;
        }

        if (!player.destroyItemByItemId("WEDDING", Config.L2JMOD_WEDDING_COIN, Config.L2JMOD_WEDDING_PRICE, player, true))
        {
          party.broadcastHtmlToPartyMembers("\u0416\u0435\u043D\u0438\u0445 \u0434\u043E\u043B\u0436\u0435\u043D \u043E\u043F\u043B\u0430\u0442\u0438\u0442\u044C: " + Config.L2JMOD_WEDDING_PRICE + " " + Config.L2JMOD_WEDDING_COINNAME + ".");
          return;
        }
      }

      _active = true;
      wed = new Wedding(player, (L2PcInstance)party.getPartyMembers().get(1), this);
      CoupleManager.getInstance().regWedding(player.getObjectId(), wed);

      ThreadPoolManager.getInstance().scheduleAi(new Finish(), Config.L2JMOD_WEDDING_INTERVAL, false);
    }
    else if (command.equalsIgnoreCase("divorce"))
    {
      if ((!player.isMarried()) || (player.getPartnerId() == 0))
      {
        player.sendHtmlMessage("\u0412\u044B \u043D\u0435 \u0441\u043E\u0441\u0442\u043E\u0438\u0442\u0435 \u0432 \u0431\u0440\u0430\u043A\u0435.");
        return;
      }

      L2ItemInstance coin = player.getInventory().getItemByItemId(Config.L2JMOD_WEDDING_DIVORCE_COIN);
      if ((coin == null) || (coin.getCount() < Config.L2JMOD_WEDDING_DIVORCE_PRICE))
      {
        player.sendHtmlMessage("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0440\u0430\u0437\u0432\u043E\u0434\u0430: " + Config.L2JMOD_WEDDING_DIVORCE_PRICE + " " + Config.L2JMOD_WEDDING_DIVORCE_COINNAME + ".");
        return;
      }

      if (!player.destroyItemByItemId("WEDDING", Config.L2JMOD_WEDDING_DIVORCE_COIN, Config.L2JMOD_WEDDING_DIVORCE_PRICE, player, true))
      {
        player.sendHtmlMessage("\u0421\u0442\u043E\u0438\u043C\u043E\u0441\u0442\u044C \u0440\u0430\u0437\u0432\u043E\u0434\u0430: " + Config.L2JMOD_WEDDING_DIVORCE_PRICE + " " + Config.L2JMOD_WEDDING_DIVORCE_COINNAME + ".");
        return;
      }

      CoupleManager.getInstance().deleteCouple(player.getCoupleId());
    }
  }

  public void finish(boolean no)
  {
    if (wed != null)
    {
      if ((no) && (!wed.married))
      {
        wed.broadcastHtml("\u0421\u0432\u0430\u0434\u044C\u0431\u0430 \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430.");
        sayString("\u0421\u0432\u0430\u0434\u044C\u0431\u0430 \u043E\u0442\u043C\u0435\u043D\u0435\u043D\u0430!!!", 18);
        if (Config.L2JMOD_WEDDING_COIN > 0)
        {
          L2PcInstance groom = wed.getGroom();
          if (groom != null)
            giveItem(groom, Config.L2JMOD_WEDDING_COIN, Config.L2JMOD_WEDDING_PRICE);
        }
      }
      wed.clear();
    }
    wed = null;
    _active = false;
    sayString("\u041F\u0440\u043E\u0448\u0443 \u0441\u043B\u0435\u0434\u0443\u044E\u0449\u0443\u044E \u043F\u0430\u0440\u0443 \u043F\u043E\u0434\u043E\u0439\u0442\u0438 \u043A\u043E \u043C\u043D\u0435.", 18);
  }

  private class Finish implements Runnable
  {
    Finish()
    {
    }

    public void run()
    {
      finish(true);
    }
  }
}