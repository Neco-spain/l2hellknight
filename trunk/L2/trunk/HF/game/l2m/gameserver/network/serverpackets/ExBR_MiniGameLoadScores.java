package l2m.gameserver.network.serverpackets;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import l2m.gameserver.instancemanager.games.MiniGameScoreManager;
import l2m.gameserver.model.Player;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.IntObjectMap.Entry;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

public class ExBR_MiniGameLoadScores extends L2GameServerPacket
{
  private int _place;
  private int _score;
  private int _lastScore;
  private IntObjectMap<List<Map.Entry<String, Integer>>> _entries = new TreeIntObjectMap();

  public ExBR_MiniGameLoadScores(Player player)
  {
    int lastBig = 0;
    int i = 1;

    for (Iterator i$ = MiniGameScoreManager.getInstance().getScores().entrySet().iterator(); i$.hasNext(); ) { entry = (IntObjectMap.Entry)i$.next();

      for (String name : (Set)entry.getValue())
      {
        List set = (List)_entries.get(i);
        if (set == null) {
          _entries.put(i, set = new ArrayList());
        }
        if ((name.equalsIgnoreCase(player.getName())) && 
          (entry.getKey() > lastBig))
        {
          _place = i;
          _score = (lastBig = entry.getKey());
        }

        set.add(new AbstractMap.SimpleImmutableEntry(name, Integer.valueOf(entry.getKey())));

        i++;

        _lastScore = entry.getKey();

        if (i > 100)
          break;
      } }
    IntObjectMap.Entry entry;
  }

  protected void writeImpl()
  {
    writeEx(221);
    writeD(_place);
    writeD(_score);
    writeD(0);
    writeD(_lastScore);
    for (Iterator i$ = _entries.entrySet().iterator(); i$.hasNext(); ) { entry = (IntObjectMap.Entry)i$.next();
      for (Map.Entry scoreEntry : (List)entry.getValue())
      {
        writeD(entry.getKey());
        writeS((CharSequence)scoreEntry.getKey());
        writeD(((Integer)scoreEntry.getValue()).intValue());
      }
    }
    IntObjectMap.Entry entry;
  }
}