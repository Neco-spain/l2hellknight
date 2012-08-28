package l2p.gameserver.ai;

import l2p.commons.util.Rnd;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.model.instances.NpcInstance;

public class DoorAI extends CharacterAI
{
  public DoorAI(DoorInstance actor)
  {
    super(actor);
  }

  public void onEvtTwiceClick(Player player)
  {
  }

  public void onEvtOpen(Player player)
  {
  }

  public void onEvtClose(Player player)
  {
  }

  public DoorInstance getActor()
  {
    return (DoorInstance)super.getActor();
  }

  protected void onEvtAttacked(Creature attacker, int damage)
  {
    Creature actor;
    if ((attacker == null) || ((actor = getActor()) == null))
      return;
    Creature actor;
    Player player = attacker.getPlayer();
    if (player == null) {
      return;
    }
    SiegeEvent siegeEvent1 = (SiegeEvent)player.getEvent(SiegeEvent.class);
    SiegeEvent siegeEvent2 = (SiegeEvent)actor.getEvent(SiegeEvent.class);

    if ((siegeEvent1 == null) || ((siegeEvent1 == siegeEvent2) && (siegeEvent1.getSiegeClan("attackers", player.getClan()) != null)))
      for (NpcInstance npc : actor.getAroundNpc(900, 200))
      {
        if (!npc.isSiegeGuard()) {
          continue;
        }
        if (Rnd.chance(20))
          npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(10000));
        else
          npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(2000));
      }
  }
}