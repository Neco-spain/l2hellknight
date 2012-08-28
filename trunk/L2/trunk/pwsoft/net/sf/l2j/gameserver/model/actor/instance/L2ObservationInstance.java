package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2ObservationInstance extends L2FolkInstance
{
  public L2ObservationInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("observeSiege")) {
      String val = command.substring(13);
      StringTokenizer st = new StringTokenizer(val);
      st.nextToken();

      if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
        doObserve(player, val);
      else
        player.sendPacket(Static.ONLY_VIEW_SIEGE);
    }
    else if (command.startsWith("observe")) {
      doObserve(player, command.substring(8));
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }

    return "data/html/observation/" + pom + ".htm";
  }

  private void doObserve(L2PcInstance player, String val) {
    if (player.isInEvent()) {
      player.sendHtmlMessage("\u0418\u0433\u0440\u043E\u043A\u0438, \u0443\u0447\u0430\u0432\u0441\u0442\u0432\u0443\u044E\u0449\u0438\u0435 \u0432 \u0438\u0432\u0435\u043D\u0442\u0430\u0445 \u043D\u0435 \u043C\u043E\u0433\u0443\u0442 \u043F\u0440\u043E\u0441\u043C\u0430\u0442\u0440\u0438\u0432\u0430\u0442\u044C.");
      player.sendActionFailed();
      return;
    }

    StringTokenizer st = new StringTokenizer(val);
    int cost = Integer.parseInt(st.nextToken());
    int x = Integer.parseInt(st.nextToken());
    int y = Integer.parseInt(st.nextToken());
    int z = Integer.parseInt(st.nextToken());
    if (player.reduceAdena("Broadcast", cost, this, true))
    {
      player.enterObserverMode(x, y, z);
      player.sendItems(false);
    }
    player.sendActionFailed();
  }
}