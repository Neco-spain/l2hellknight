package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2PeaceZone extends L2ZoneType
{
  private int id = 0;

  public L2PeaceZone(int id) {
    super(id);
    this.id = id;
  }

  protected void onEnter(L2Character character)
  {
    character.setInZonePeace(true);
    if (id == 17000)
      character.setEventWait(true);
  }

  protected void onExit(L2Character character)
  {
    character.setInZonePeace(false);
    if (id == 17000)
      character.setEventWait(false);
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }
}