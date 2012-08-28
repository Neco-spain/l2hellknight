package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PackageToList extends L2GameServerPacket
{
  private static final String _S__C2_PACKAGETOLIST = "[S] C2 PackageToList";
  private Map<Integer, String> _players;

  public PackageToList(Map<Integer, String> players)
  {
    _players = players;
  }

  protected void writeImpl()
  {
    writeC(194);
    writeD(_players.size());
    for (Iterator i$ = _players.keySet().iterator(); i$.hasNext(); ) { int objId = ((Integer)i$.next()).intValue();

      writeD(objId);
      writeS((CharSequence)_players.get(Integer.valueOf(objId)));
    }
  }

  public String getType()
  {
    return "[S] C2 PackageToList";
  }
}