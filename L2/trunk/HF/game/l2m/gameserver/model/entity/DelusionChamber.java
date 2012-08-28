package l2m.gameserver.model.entity;

import java.util.List;
import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.xml.holder.InstantZoneHolder;
import l2m.gameserver.instancemanager.DimensionalRiftManager;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.templates.InstantZone;
import l2m.gameserver.utils.Location;

public class DelusionChamber extends DimensionalRift
{
  private Future<?> killRiftTask;

  public DelusionChamber(Party party, int type, int room)
  {
    super(party, type, room);
  }

  public synchronized void createNewKillRiftTimer()
  {
    if (killRiftTask != null)
    {
      killRiftTask.cancel(false);
      killRiftTask = null;
    }

    killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        if ((getParty() != null) && (!getParty().getPartyMembers().isEmpty()))
          for (Player p : getParty().getPartyMembers())
            if (p.getReflection() == DelusionChamber.this)
            {
              String var = p.getVar("backCoords");
              if ((var == null) || (var.equals("")))
                continue;
              p.teleToLocation(Location.parseLoc(var), ReflectionManager.DEFAULT);
              p.unsetVar("backCoords");
            }
        collapse();
      }
    }
    , 100L);
  }

  public void partyMemberExited(Player player)
  {
    if ((getPlayersInside(false) < 2) || (getPlayersInside(true) == 0))
    {
      createNewKillRiftTimer();
      return;
    }
  }

  public void manualExitRift(Player player, NpcInstance npc)
  {
    if ((!player.isInParty()) || (player.getParty().getReflection() != this)) {
      return;
    }
    if (!player.getParty().isLeader(player))
    {
      DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
      return;
    }

    createNewKillRiftTimer();
  }

  public String getName()
  {
    InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(_roomType + 120);
    return iz.getName();
  }

  protected int getManagerId()
  {
    return 32664;
  }
}