package l2m.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.Zone;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public final class ObservationInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;

  public ObservationInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (checkForDominionWard(player)) {
      return;
    }
    if (player.getOlympiadGame() != null) {
      return;
    }
    if (command.startsWith("observeSiege"))
    {
      String val = command.substring(13);
      StringTokenizer st = new StringTokenizer(val);
      st.nextToken();

      List zones = new ArrayList();
      World.getZones(zones, new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())), ReflectionManager.DEFAULT);
      for (Zone z : zones)
      {
        if ((z.getType() == Zone.ZoneType.SIEGE) && (z.isActive()))
        {
          doObserve(player, val);
          return;
        }
      }

      player.sendPacket(SystemMsg.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE);
    }
    else if (command.startsWith("observe")) {
      doObserve(player, command.substring(8));
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  public String getHtmlPath(int npcId, int val, Player player)
  {
    String pom = "";
    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }
    return "observation/" + pom + ".htm";
  }

  private void doObserve(Player player, String val)
  {
    StringTokenizer st = new StringTokenizer(val);
    int cost = Integer.parseInt(st.nextToken());
    int x = Integer.parseInt(st.nextToken());
    int y = Integer.parseInt(st.nextToken());
    int z = Integer.parseInt(st.nextToken());

    if (!player.reduceAdena(cost, true))
    {
      player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
      player.sendActionFailed();
      return;
    }

    player.enterObserverMode(new Location(x, y, z));
  }
}