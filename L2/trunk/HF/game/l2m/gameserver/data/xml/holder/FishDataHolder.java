package l2m.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2p.commons.data.xml.AbstractHolder;
import l2m.gameserver.templates.item.support.FishGroup;
import l2m.gameserver.templates.item.support.FishTemplate;
import l2m.gameserver.templates.item.support.LureTemplate;
import l2m.gameserver.templates.item.support.LureType;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

public class FishDataHolder extends AbstractHolder
{
  private static final FishDataHolder _instance = new FishDataHolder();

  private List<FishTemplate> _fishes = new ArrayList();
  private IntObjectMap<LureTemplate> _lures = new HashIntObjectMap();
  private IntObjectMap<Map<LureType, Map<FishGroup, Integer>>> _distributionsForZones = new HashIntObjectMap();

  public static FishDataHolder getInstance()
  {
    return _instance;
  }

  public void addFish(FishTemplate fishTemplate)
  {
    _fishes.add(fishTemplate);
  }

  public void addLure(LureTemplate template)
  {
    _lures.put(template.getItemId(), template);
  }

  public void addDistribution(int id, LureType lureType, Map<FishGroup, Integer> map)
  {
    Map byLureType = (Map)_distributionsForZones.get(id);
    if (byLureType == null) {
      _distributionsForZones.put(id, byLureType = new HashMap());
    }
    byLureType.put(lureType, map);
  }

  public void log()
  {
    info("load " + _fishes.size() + " fish(es).");
    info("load " + _lures.size() + " lure(s).");
    info("load " + _distributionsForZones.size() + " distribution(s).");
  }

  public int size()
  {
    return 0;
  }

  public void clear()
  {
    _fishes.clear();
    _lures.clear();
    _distributionsForZones.clear();
  }
}