package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2OlympiadTexture extends L2ZoneType
{
  private int _stadiumId;

  public L2OlympiadTexture(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
  }

  protected void onExit(L2Character character)
  {
  }

  protected void onDieInside(L2Character character)
  {
  }

  protected void onReviveInside(L2Character character)
  {
  }

  public int getStadiumId()
  {
    return _stadiumId;
  }
}