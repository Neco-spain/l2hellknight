package l2m.gameserver.skills.conditions;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.skills.Env;

public class ConditionZoneType extends Condition
{
  private final Zone.ZoneType _zoneType;

  public ConditionZoneType(String zoneType)
  {
    _zoneType = Zone.ZoneType.valueOf(zoneType);
  }

  protected boolean testImpl(Env env)
  {
    if (!env.character.isPlayer())
      return false;
    return env.character.isInZone(_zoneType);
  }
}