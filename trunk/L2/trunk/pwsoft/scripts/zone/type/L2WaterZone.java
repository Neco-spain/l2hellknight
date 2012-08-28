package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneForm;
import scripts.zone.L2ZoneType;

public class L2WaterZone extends L2ZoneType
{
  private int id = -1;

  public L2WaterZone(int id) {
    super(id);
    this.id = id;
  }

  protected void onEnter(L2Character character)
  {
    character.setInsideZone(128, true);
    character.startWaterTask(id);
  }

  protected void onExit(L2Character character)
  {
    character.setInsideZone(128, false);
    character.stopWaterTask(id);
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }

  public int getWaterZ()
  {
    return getZone().getHighZ();
  }
}