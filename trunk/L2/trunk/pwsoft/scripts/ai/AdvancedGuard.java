package scripts.ai;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.GuardKnownList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class AdvancedGuard extends L2GuardInstance
{
  public AdvancedGuard(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();
    if (getSpawn() != null) {
      return;
    }

    ThreadPoolManager.getInstance().scheduleAi(new Radar(), 1000L, false);
  }

  private class Radar implements Runnable
  {
    Radar()
    {
    }

    public void run()
    {
      L2PcInstance pc = null;
      FastList players = getKnownList().getKnownPlayersInRadius(2100);
      FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
        pc = (L2PcInstance)n.getValue();
        if ((pc == null) || (pc.getKarma() == 0))
        {
          continue;
        }

        pc.reduceCurrentHp(999999.0D, AdvancedGuard.this, true, true);
      }

      ThreadPoolManager.getInstance().scheduleAi(new Radar(AdvancedGuard.this), 10L, false);
    }
  }
}