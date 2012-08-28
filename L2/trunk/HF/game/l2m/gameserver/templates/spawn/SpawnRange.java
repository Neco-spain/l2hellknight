package l2m.gameserver.templates.spawn;

import l2m.gameserver.utils.Location;

public abstract interface SpawnRange
{
  public abstract Location getRandomLoc(int paramInt);
}