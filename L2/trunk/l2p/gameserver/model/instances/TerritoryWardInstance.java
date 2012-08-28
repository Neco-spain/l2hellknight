package l2p.gameserver.model.instances;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2p.gameserver.model.entity.events.objects.TerritoryWardObject;
import l2p.gameserver.model.entity.residence.Dominion;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.templates.npc.NpcTemplate;

public class TerritoryWardInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  private final TerritoryWardObject _territoryWard;

  public TerritoryWardInstance(int objectId, NpcTemplate template, TerritoryWardObject territoryWardObject)
  {
    super(objectId, template);
    setHasChatWindow(false);
    _territoryWard = territoryWardObject;
  }

  public void onDeath(Creature killer)
  {
    super.onDeath(killer);
    Player player = killer.getPlayer();
    if (player == null) {
      return;
    }
    if (_territoryWard.canPickUp(player))
    {
      _territoryWard.pickUp(player);
      decayMe();
    }
  }

  protected void onDecay()
  {
    decayMe();

    _spawnAnimation = 2;
  }

  public boolean isAttackable(Creature attacker)
  {
    return isAutoAttackable(attacker);
  }

  public boolean isAutoAttackable(Creature attacker)
  {
    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)getEvent(DominionSiegeEvent.class);
    if (siegeEvent == null)
      return false;
    DominionSiegeEvent siegeEvent2 = (DominionSiegeEvent)attacker.getEvent(DominionSiegeEvent.class);
    if (siegeEvent2 == null)
      return false;
    if (siegeEvent == siegeEvent2) {
      return false;
    }
    return ((Dominion)siegeEvent2.getResidence()).getOwner() == attacker.getClan();
  }

  public boolean isInvul()
  {
    return false;
  }

  public Clan getClan()
  {
    return null;
  }
}