package l2m.gameserver.ai;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.entity.boat.Boat;

public class BoatAI extends CharacterAI
{
  public BoatAI(Creature actor)
  {
    super(actor);
  }

  protected void onEvtArrived()
  {
    Boat actor = (Boat)getActor();
    if (actor == null) {
      return;
    }
    actor.onEvtArrived();
  }

  public boolean isGlobalAI()
  {
    return true;
  }
}