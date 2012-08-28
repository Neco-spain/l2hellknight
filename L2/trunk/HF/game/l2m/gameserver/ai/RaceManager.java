package l2m.gameserver.ai;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.instances.RaceManagerInstance;
import l2m.gameserver.network.serverpackets.MonRaceInfo;

public class RaceManager extends DefaultAI
{
  private boolean thinking = false;
  private List<Player> _knownPlayers = new ArrayList();

  public RaceManager(NpcInstance actor)
  {
    super(actor);
    AI_TASK_ATTACK_DELAY = 5000L;
  }

  public void runImpl()
    throws Exception
  {
    onEvtThink();
  }

  protected void onEvtThink()
  {
    RaceManagerInstance actor = getActor();
    if (actor == null) {
      return;
    }
    MonRaceInfo packet = actor.getPacket();
    if (packet == null) {
      return;
    }
    synchronized (this)
    {
      if (thinking)
        return;
      thinking = true;
    }

    try
    {
      List newPlayers = new ArrayList();
      for (Player player : World.getAroundPlayers(actor, 1200, 200))
      {
        if (player == null)
          continue;
        newPlayers.add(player);
        if (!_knownPlayers.contains(player))
          player.sendPacket(packet);
        _knownPlayers.remove(player);
      }

      for (Player player : _knownPlayers) {
        actor.removeKnownPlayer(player);
      }
      _knownPlayers = newPlayers;
    }
    finally
    {
      thinking = false;
    }
  }

  public RaceManagerInstance getActor()
  {
    return (RaceManagerInstance)super.getActor();
  }
}