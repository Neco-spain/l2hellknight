package l2p.gameserver.model;

import java.util.Iterator;
import l2p.commons.collections.EmptyIterator;
import l2p.gameserver.serverpackets.components.IStaticPacket;

public abstract interface PlayerGroup extends Iterable<Player>
{
  public static final long serialVersionUID = 1L;
  public static final PlayerGroup EMPTY = new PlayerGroup()
  {
    public void broadCast(IStaticPacket[] packet)
    {
    }

    public Iterator<Player> iterator()
    {
      return EmptyIterator.getInstance();
    }
  };

  public abstract void broadCast(IStaticPacket[] paramArrayOfIStaticPacket);
}