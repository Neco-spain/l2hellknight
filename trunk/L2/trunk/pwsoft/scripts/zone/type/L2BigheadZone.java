package scripts.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import scripts.zone.L2ZoneType;

public class L2BigheadZone extends L2ZoneType
{
  public L2BigheadZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if (character.isPlayer())
      character.startAbnormalEffect(8192);
  }

  protected void onExit(L2Character character)
  {
    if (character.isPlayer())
      character.stopAbnormalEffect(8192);
  }

  protected void onDieInside(L2Character character)
  {
    onExit(character);
  }

  protected void onReviveInside(L2Character character)
  {
    onEnter(character);
  }
}