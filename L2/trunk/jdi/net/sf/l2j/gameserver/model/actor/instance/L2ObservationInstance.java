package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2ObservationInstance extends L2FolkInstance
{
  public L2ObservationInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("observeSiege"))
    {
      String val = command.substring(13);
      StringTokenizer st = new StringTokenizer(val);
      st.nextToken();

      if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
      {
        doObserve(player, val);
      }
      else player.sendPacket(new SystemMessage(SystemMessageId.ONLY_VIEW_SIEGE));
    }
    else if (command.startsWith("observe"))
    {
      doObserve(player, command.substring(8));
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";
    if (val == 0)
    {
      pom = "" + npcId;
    }
    else
    {
      pom = npcId + "-" + val;
    }

    return "data/html/observation/" + pom + ".htm";
  }

  private void doObserve(L2PcInstance player, String val)
  {
    StringTokenizer st = new StringTokenizer(val);
    int cost = Integer.parseInt(st.nextToken());
    int x = Integer.parseInt(st.nextToken());
    int y = Integer.parseInt(st.nextToken());
    int z = Integer.parseInt(st.nextToken());
    if (player.reduceAdena("Broadcast", cost, this, true))
    {
      player.enterObserverMode(x, y, z);
      ItemList il = new ItemList(player, false);
      player.sendPacket(il);
    }
    player.sendPacket(new ActionFailed());
  }
}