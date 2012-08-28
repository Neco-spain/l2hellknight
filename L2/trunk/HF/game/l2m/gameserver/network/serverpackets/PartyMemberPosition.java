package l2m.gameserver.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import l2m.gameserver.model.Player;
import l2m.gameserver.utils.Location;

public class PartyMemberPosition extends L2GameServerPacket
{
  private final Map<Integer, Location> positions = new HashMap();

  public PartyMemberPosition add(Player actor)
  {
    positions.put(Integer.valueOf(actor.getObjectId()), actor.getLoc());
    return this;
  }

  public int size()
  {
    return positions.size();
  }

  protected final void writeImpl()
  {
    writeC(186);
    writeD(positions.size());
    for (Map.Entry e : positions.entrySet())
    {
      writeD(((Integer)e.getKey()).intValue());
      writeD(((Location)e.getValue()).x);
      writeD(((Location)e.getValue()).y);
      writeD(((Location)e.getValue()).z);
    }
  }
}