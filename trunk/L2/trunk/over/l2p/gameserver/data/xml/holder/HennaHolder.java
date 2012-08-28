package l2p.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.ArrayList;
import java.util.List;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.templates.Henna;

public final class HennaHolder extends AbstractHolder
{
  private static final HennaHolder _instance = new HennaHolder();

  private TIntObjectHashMap<Henna> _hennas = new TIntObjectHashMap();

  public static HennaHolder getInstance()
  {
    return _instance;
  }

  public void addHenna(Henna h)
  {
    _hennas.put(h.getSymbolId(), h);
  }

  public Henna getHenna(int symbolId)
  {
    return (Henna)_hennas.get(symbolId);
  }

  public List<Henna> generateList(Player player)
  {
    List list = new ArrayList();
    for (TIntObjectIterator iterator = _hennas.iterator(); iterator.hasNext(); )
    {
      iterator.advance();
      Henna h = (Henna)iterator.value();
      if (h.isForThisClass(player)) {
        list.add(h);
      }
    }
    return list;
  }

  public int size()
  {
    return _hennas.size();
  }

  public void clear()
  {
    _hennas.clear();
  }
}