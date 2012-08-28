package net.sf.l2j.gameserver.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.templates.L2Item;

public class L2Manor
{
  private static Logger _log = Logger.getLogger(L2Manor.class.getName());
  private static L2Manor _instance;
  private static FastMap<Integer, SeedData> _seeds;

  public L2Manor()
  {
    _seeds = new FastMap().setShared(true);
    parseData();
  }

  public static L2Manor getInstance() {
    if (_instance == null) {
      _instance = new L2Manor();
    }
    return _instance;
  }

  public FastList<Integer> getAllCrops() {
    FastList crops = new FastList();

    for (SeedData seed : _seeds.values()) {
      if ((!crops.contains(Integer.valueOf(seed.getCrop()))) && (seed.getCrop() != 0) && (!crops.contains(Integer.valueOf(seed.getCrop())))) {
        crops.add(Integer.valueOf(seed.getCrop()));
      }
    }

    return crops;
  }

  public int getSeedBasicPrice(int seedId) {
    L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);

    if (seedItem != null) {
      return seedItem.getReferencePrice();
    }
    return 0;
  }

  public int getSeedBasicPriceByCrop(int cropId)
  {
    for (SeedData seed : _seeds.values()) {
      if (seed.getCrop() == cropId)
        return getSeedBasicPrice(seed.getId());
    }
    return 0;
  }

  public int getCropBasicPrice(int cropId) {
    L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);

    if (cropItem != null) {
      return cropItem.getReferencePrice();
    }
    return 0;
  }

  public int getMatureCrop(int cropId) {
    for (SeedData seed : _seeds.values()) {
      if (seed.getCrop() == cropId)
        return seed.getMature();
    }
    return 0;
  }

  public int getSeedBuyPrice(int seedId)
  {
    int buyPrice = getSeedBasicPrice(seedId) / 10;
    return buyPrice > 0 ? buyPrice : 1;
  }

  public int getSeedMinLevel(int seedId)
  {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null)
      return seed.getLevel() - 5;
    return -1;
  }

  public int getSeedMaxLevel(int seedId) {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null)
      return seed.getLevel() + 5;
    return -1;
  }

  public int getSeedLevelByCrop(int cropId) {
    for (SeedData seed : _seeds.values()) {
      if (seed.getCrop() == cropId) {
        return seed.getLevel();
      }
    }
    return 0;
  }

  public int getSeedLevel(int seedId) {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null) {
      return seed.getLevel();
    }
    return -1;
  }

  public boolean isAlternative(int seedId) {
    for (SeedData seed : _seeds.values()) {
      if (seed.getId() == seedId) {
        return seed.isAlternative();
      }
    }
    return false;
  }

  public int getCropType(int seedId) {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null)
      return seed.getCrop();
    return -1;
  }

  public synchronized int getRewardItem(int cropId, int type) {
    for (SeedData seed : _seeds.values()) {
      if (seed.getCrop() == cropId) {
        return seed.getReward(type);
      }

    }

    return -1;
  }

  public synchronized int getRewardItemBySeed(int seedId, int type) {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null) {
      return seed.getReward(type);
    }
    return 0;
  }

  public FastList<Integer> getCropsForCastle(int castleId)
  {
    FastList crops = new FastList();

    for (SeedData seed : _seeds.values()) {
      if ((seed.getManorId() == castleId) && (!crops.contains(Integer.valueOf(seed.getCrop())))) {
        crops.add(Integer.valueOf(seed.getCrop()));
      }
    }

    return crops;
  }

  public FastList<Integer> getSeedsForCastle(int castleId)
  {
    FastList seedsID = new FastList();

    for (SeedData seed : _seeds.values()) {
      if ((seed.getManorId() == castleId) && (!seedsID.contains(Integer.valueOf(seed.getId())))) {
        seedsID.add(Integer.valueOf(seed.getId()));
      }
    }

    return seedsID;
  }

  public int getCastleIdForSeed(int seedId)
  {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null) {
      return seed.getManorId();
    }
    return 0;
  }

  public int getSeedSaleLimit(int seedId) {
    SeedData seed = (SeedData)_seeds.get(Integer.valueOf(seedId));

    if (seed != null) {
      return seed.getSeedLimit();
    }
    return 0;
  }

  public int getCropPuchaseLimit(int cropId) {
    for (SeedData seed : _seeds.values()) {
      if (seed.getCrop() == cropId) {
        return seed.getCropLimit();
      }
    }
    return 0;
  }

  private void parseData()
  {
    LineNumberReader lnr = null;
    try {
      File seedData = new File(Config.DATAPACK_ROOT, "data/csv/seeds.csv");
      lnr = new LineNumberReader(new BufferedReader(new FileReader(seedData)));

      String line = null;
      while ((line = lnr.readLine()) != null) {
        if ((line.trim().length() == 0) || (line.startsWith("#"))) {
          continue;
        }
        SeedData seed = parseList(line);
        _seeds.put(Integer.valueOf(seed.getId()), seed);
      }

      _log.info("ManorManager: Loaded " + _seeds.size() + " seeds");
    } catch (FileNotFoundException e1) {
      _log.info("seeds.csv is missing in data folder");
    } catch (Exception e1) {
      _log.info("error while loading seeds: " + e.getMessage());
    } finally {
      try {
        lnr.close();
      } catch (Exception e1) {
      }
    }
  }

  private SeedData parseList(String line) {
    StringTokenizer st = new StringTokenizer(line, ";");

    int seedId = Integer.parseInt(st.nextToken());
    int level = Integer.parseInt(st.nextToken());
    int cropId = Integer.parseInt(st.nextToken());
    int matureId = Integer.parseInt(st.nextToken());
    int type1R = Integer.parseInt(st.nextToken());
    int type2R = Integer.parseInt(st.nextToken());
    int manorId = Integer.parseInt(st.nextToken());
    int isAlt = Integer.parseInt(st.nextToken());
    int limitSeeds = Integer.parseInt(st.nextToken());
    int limitCrops = Integer.parseInt(st.nextToken());

    SeedData seed = new SeedData(level, cropId, matureId);
    seed.setData(seedId, type1R, type2R, manorId, isAlt, limitSeeds, limitCrops);

    return seed;
  }

  private class SeedData
  {
    private int _id;
    private int _level;
    private int _crop;
    private int _mature;
    private int _type1;
    private int _type2;
    private int _manorId;
    private int _isAlternative;
    private int _limitSeeds;
    private int _limitCrops;

    public SeedData(int level, int crop, int mature)
    {
      _level = level;
      _crop = crop;
      _mature = mature;
    }

    public void setData(int id, int t1, int t2, int manorId, int isAlt, int lim1, int lim2) {
      _id = id;
      _type1 = t1;
      _type2 = t2;
      _manorId = manorId;
      _isAlternative = isAlt;
      _limitSeeds = lim1;
      _limitCrops = lim2;
    }

    public int getManorId() {
      return _manorId;
    }

    public int getId() {
      return _id;
    }

    public int getCrop()
    {
      return _crop;
    }

    public int getMature() {
      return _mature;
    }

    public int getReward(int type) {
      return type == 1 ? _type1 : _type2;
    }

    public int getLevel() {
      return _level;
    }

    public boolean isAlternative() {
      return _isAlternative == 1;
    }

    public int getSeedLimit() {
      return _limitSeeds * Config.RATE_DROP_MANOR;
    }

    public int getCropLimit() {
      return _limitCrops * Config.RATE_DROP_MANOR;
    }
  }
}