package l2p.gameserver.model.instances;

import java.text.DateFormat;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.instancemanager.games.LotteryManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.HtmlUtils;
import l2p.gameserver.utils.ItemFunctions;
import l2p.gameserver.utils.Log;

public class LotteryManagerInstance extends NpcInstance
{
  public LotteryManagerInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (command.startsWith("Loto"))
    {
      try
      {
        int val = Integer.parseInt(command.substring(5));
        showLotoWindow(player, val);
      }
      catch (NumberFormatException e)
      {
        Log.debug("L2LotteryManagerInstance: bypass: " + command + "; player: " + player, e);
      }
    }
    else
      super.onBypassFeedback(player, command);
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom;
    String pom;
    if (val == 0)
      pom = "LotteryManager";
    else {
      pom = "LotteryManager-" + val;
    }
    return "lottery/" + pom + ".htm";
  }

  public void showLotoWindow(Player player, int val)
  {
    int npcId = getTemplate().npcId;

    NpcHtmlMessage html = new NpcHtmlMessage(player, this);

    if (val == 0)
    {
      String filename = getHtmlPath(npcId, 1, player);
      html.setFile(filename);
    }
    else if ((val >= 1) && (val <= 21))
    {
      if (!LotteryManager.getInstance().isStarted())
      {
        player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
        return;
      }
      if (!LotteryManager.getInstance().isSellableTickets())
      {
        player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
        return;
      }

      String filename = getHtmlPath(npcId, 5, player);
      html.setFile(filename);

      int count = 0;
      int found = 0;

      for (int i = 0; i < 5; i++) {
        if (player.getLoto(i) == val)
        {
          player.setLoto(i, 0);
          found = 1;
        }
        else if (player.getLoto(i) > 0) {
          count++;
        }
      }
      if ((count < 5) && (found == 0) && (val <= 20)) {
        for (int i = 0; i < 5; i++) {
          if (player.getLoto(i) != 0)
            continue;
          player.setLoto(i, val);
          break;
        }
      }

      count = 0;
      for (int i = 0; i < 5; i++) {
        if (player.getLoto(i) <= 0)
          continue;
        count++;
        String button = String.valueOf(player.getLoto(i));
        if (player.getLoto(i) < 10)
          button = "0" + button;
        String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
        String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
        html.replace(search, replace);
      }
      if (count == 5)
      {
        String search = "";
        String replace = "";
        if (player.getVar("lang@").equalsIgnoreCase("en"))
        {
          search = "0\">Return";
          replace = "22\">The winner selected the numbers above.";
        }
        else
        {
          search = "0\">\u041D\u0430\u0437\u0430\u0434";
          replace = "22\">\u0412\u044B\u0438\u0433\u0440\u044B\u0448\u043D\u044B\u0435 \u043D\u043E\u043C\u0435\u0440\u0430 \u0432\u044B\u0431\u0440\u0430\u043D\u043D\u044B\u0435 \u0432\u044B\u0448\u0435.";
        }
        html.replace(search, replace);
      }
      player.sendPacket(html);
    }

    if (val == 22)
    {
      if (!LotteryManager.getInstance().isStarted())
      {
        player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
        return;
      }
      if (!LotteryManager.getInstance().isSellableTickets())
      {
        player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
        return;
      }

      int price = Config.SERVICES_ALT_LOTTERY_PRICE;
      int lotonumber = LotteryManager.getInstance().getId();
      int enchant = 0;
      int type2 = 0;
      for (int i = 0; i < 5; i++)
      {
        if (player.getLoto(i) == 0)
          return;
        if (player.getLoto(i) < 17)
          enchant = (int)(enchant + Math.pow(2.0D, player.getLoto(i) - 1));
        else
          type2 = (int)(type2 + Math.pow(2.0D, player.getLoto(i) - 17));
      }
      if (player.getAdena() < price)
      {
        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        return;
      }
      player.reduceAdena(price, true);
      SystemMessage sm = new SystemMessage(371);
      sm.addNumber(lotonumber);
      sm.addItemName(4442);
      player.sendPacket(sm);
      ItemInstance item = ItemFunctions.createItem(4442);
      item.setCustomType1(lotonumber);
      item.setEnchantLevel(enchant);
      item.setCustomType2(type2);
      player.getInventory().addItem(item);

      String filename = getHtmlPath(npcId, 3, player);
      html.setFile(filename);
    }
    else if (val == 23)
    {
      String filename = getHtmlPath(npcId, 3, player);
      html.setFile(filename);
    }
    else if (val == 24)
    {
      String filename = getHtmlPath(npcId, 4, player);
      html.setFile(filename);

      int lotonumber = LotteryManager.getInstance().getId();
      String message = "";

      for (ItemInstance item : player.getInventory().getItems())
      {
        if (item == null)
          continue;
        if ((item.getItemId() != 4442) || (item.getCustomType1() >= lotonumber))
          continue;
        message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1();
        message = message + " " + HtmlUtils.htmlNpcString(NpcString.EVENT_NUMBER, new Object[0]) + " ";
        int[] numbers = LotteryManager.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
        for (int i = 0; i < 5; i++)
          message = message + numbers[i] + " ";
        int[] check = LotteryManager.getInstance().checkTicket(item);
        if (check[0] > 0)
        {
          message = message + "- ";
          switch (check[0])
          {
          case 1:
            message = message + HtmlUtils.htmlNpcString(NpcString.FIRST_PRIZE, new Object[0]);
            break;
          case 2:
            message = message + HtmlUtils.htmlNpcString(NpcString.SECOND_PRIZE, new Object[0]);
            break;
          case 3:
            message = message + HtmlUtils.htmlNpcString(NpcString.THIRD_PRIZE, new Object[0]);
            break;
          case 4:
            message = message + HtmlUtils.htmlNpcString(NpcString.FOURTH_PRIZE, new Object[0]);
          }

          message = message + " " + check[1] + "a.";
        }
        message = message + "</a>";
      }

      if (message.length() == 0) {
        message = message + HtmlUtils.htmlNpcString(NpcString.THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET, new Object[0]);
      }
      html.replace("%result%", message);
    }
    else if (val == 25)
    {
      String filename = getHtmlPath(npcId, 2, player);
      html.setFile(filename);
    }
    else if (val > 25)
    {
      int lotonumber = LotteryManager.getInstance().getId();
      ItemInstance item = player.getInventory().getItemByObjectId(val);
      if ((item == null) || (item.getItemId() != 4442) || (item.getCustomType1() >= lotonumber))
        return;
      int[] check = LotteryManager.getInstance().checkTicket(item);

      if (player.getInventory().destroyItem(item, 1L))
      {
        player.sendPacket(SystemMessage2.removeItems(4442, 1L));
        int adena = check[1];
        if (adena > 0) {
          player.addAdena(adena);
        }
      }
      return;
    }

    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%race%", "" + LotteryManager.getInstance().getId());
    html.replace("%adena%", "" + LotteryManager.getInstance().getPrize());
    html.replace("%ticket_price%", "" + Config.SERVICES_LOTTERY_TICKET_PRICE);
    html.replace("%prize5%", "" + Config.SERVICES_LOTTERY_5_NUMBER_RATE * 100.0D);
    html.replace("%prize4%", "" + Config.SERVICES_LOTTERY_4_NUMBER_RATE * 100.0D);
    html.replace("%prize3%", "" + Config.SERVICES_LOTTERY_3_NUMBER_RATE * 100.0D);
    html.replace("%prize2%", "" + Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE);
    html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Long.valueOf(LotteryManager.getInstance().getEndDate())));

    player.sendPacket(html);
    player.sendActionFailed();
  }
}