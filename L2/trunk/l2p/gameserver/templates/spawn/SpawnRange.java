package l2p.gameserver.templates.spawn;

import l2p.gameserver.utils.Location;

public abstract interface SpawnRange
{
  public abstract Location getRandomLoc(int paramInt);
}