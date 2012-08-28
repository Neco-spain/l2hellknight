package l2m.gameserver.data.tables;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.triggers.TriggerInfo;
import l2m.gameserver.skills.triggers.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AugmentationData
{
  private static final Logger _log = LoggerFactory.getLogger(AugmentationData.class);
  private static AugmentationData _Instance;
  private static final int STAT_START = 1;
  private static final int STAT_END = 14560;
  private static final int STAT_BLOCKSIZE = 3640;
  private static final int STAT_SUBBLOCKSIZE = 91;
  private static final int STAT_NUM = 13;
  private static final byte[] STATS1_MAP = new byte[91];
  private static final byte[] STATS2_MAP = new byte[91];
  private static final int BLUE_START = 14561;
  private static final int SKILLS_BLOCKSIZE = 178;
  private static final int BASESTAT_STR = 16341;
  private static final int BASESTAT_CON = 16342;
  private static final int BASESTAT_INT = 16343;
  private static final int BASESTAT_MEN = 16344;
  private static final int ACC_START = 16669;
  private static final int ACC_BLOCKS_NUM = 10;
  private static final int ACC_STAT_SUBBLOCKSIZE = 21;
  private static final int ACC_STAT_NUM = 6;
  private static final int ACC_RING_START = 16669;
  private static final int ACC_RING_SKILLS = 18;
  private static final int ACC_RING_BLOCKSIZE = 102;
  private static final int ACC_RING_END = 17688;
  private static final int ACC_EAR_START = 17689;
  private static final int ACC_EAR_SKILLS = 18;
  private static final int ACC_EAR_BLOCKSIZE = 102;
  private static final int ACC_EAR_END = 18708;
  private static final int ACC_NECK_START = 18709;
  private static final int ACC_NECK_SKILLS = 24;
  private static final int ACC_NECK_BLOCKSIZE = 108;
  private static final int ACC_END = 19789;
  private static final byte[] ACC_STATS1_MAP = new byte[21];
  private static final byte[] ACC_STATS2_MAP = new byte[21];

  private List<?>[] _augStats = new ArrayList[4];
  private List<?>[] _augAccStats = new ArrayList[4];

  private List<?>[] _blueSkills = new ArrayList[10];
  private List<?>[] _purpleSkills = new ArrayList[10];
  private List<?>[] _redSkills = new ArrayList[10];
  private List<?>[] _yellowSkills = new ArrayList[10];

  private TIntObjectHashMap<TriggerInfo> _allSkills = new TIntObjectHashMap();

  public static AugmentationData getInstance()
  {
    if (_Instance == null)
      _Instance = new AugmentationData();
    return _Instance;
  }

  public AugmentationData()
  {
    _log.info("Initializing AugmentationData.");

    _augStats[0] = new ArrayList();
    _augStats[1] = new ArrayList();
    _augStats[2] = new ArrayList();
    _augStats[3] = new ArrayList();

    _augAccStats[0] = new ArrayList();
    _augAccStats[1] = new ArrayList();
    _augAccStats[2] = new ArrayList();
    _augAccStats[3] = new ArrayList();

    for (int idx = 0; idx < 13; idx++)
    {
      STATS1_MAP[idx] = (byte)idx;
      STATS2_MAP[idx] = (byte)idx;
    }

    for (int i = 0; i < 13; i++) {
      for (int j = i + 1; j < 13; j++)
      {
        STATS1_MAP[idx] = (byte)i;
        STATS2_MAP[idx] = (byte)j;

        idx++;
      }

    }

    idx = 0;

    for (int i = 0; i < 4; i++) {
      for (int j = i; j < 6; j++)
      {
        ACC_STATS1_MAP[idx] = (byte)i;
        ACC_STATS2_MAP[idx] = (byte)j;

        idx++;
      }

    }

    ACC_STATS1_MAP[idx] = 4;
    ACC_STATS2_MAP[(idx++)] = 4;
    ACC_STATS1_MAP[idx] = 5;
    ACC_STATS2_MAP[(idx++)] = 5;
    ACC_STATS1_MAP[idx] = 4;
    ACC_STATS2_MAP[idx] = 5;

    for (int i = 0; i < 10; i++)
    {
      _blueSkills[i] = new ArrayList();
      _purpleSkills[i] = new ArrayList();
      _redSkills[i] = new ArrayList();
      _yellowSkills[i] = new ArrayList();
    }

    load();

    _log.info("AugmentationData: Loaded: " + _augStats[0].size() * 4 + " augmentation stats.");
    _log.info("AugmentationData: Loaded: " + _augAccStats[0].size() * 4 + " accessory augmentation stats.");
    for (int i = 0; i < 10; i++)
      _log.info("AugmentationData: Loaded: " + _blueSkills[i].size() + " blue, " + _purpleSkills[i].size() + " purple and " + _redSkills[i].size() + " red skills for lifeStoneLevel " + i);
  }

  private final void load()
  {
    try
    {
      int badAugmantData = 0;
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT, "data/stats/augmentation/augmentation_skillmap.xml");

      Document doc = factory.newDocumentBuilder().parse(file);
      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName()))
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
            if (!"augmentation".equalsIgnoreCase(d.getNodeName()))
              continue;
            NamedNodeMap attrs = d.getAttributes();
            int skillId = 0; int augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
            int skillLvL = 0;
            String type = "blue";

            TriggerType t = null;
            double chance = 0.0D;
            for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
            {
              attrs = cd.getAttributes();
              if ("skillId".equalsIgnoreCase(cd.getNodeName()))
              {
                skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              }
              else if ("skillLevel".equalsIgnoreCase(cd.getNodeName()))
              {
                skillLvL = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
              }
              else if ("type".equalsIgnoreCase(cd.getNodeName()))
              {
                type = attrs.getNamedItem("val").getNodeValue();
              }
              else if ("trigger_type".equalsIgnoreCase(cd.getNodeName()))
              {
                t = TriggerType.valueOf(attrs.getNamedItem("val").getNodeValue());
              } else {
                if (!"trigger_chance".equalsIgnoreCase(cd.getNodeName()))
                  continue;
                chance = Double.parseDouble(attrs.getNamedItem("val").getNodeValue());
              }
            }

            if (skillId == 0)
            {
              badAugmantData++;
            }
            else if (skillLvL == 0)
            {
              badAugmantData++;
            }
            else
            {
              int k = (augmentationId - 14561) / 178;
              if (type.equalsIgnoreCase("blue"))
                _blueSkills[k].add(Integer.valueOf(augmentationId));
              else if (type.equalsIgnoreCase("purple"))
                _purpleSkills[k].add(Integer.valueOf(augmentationId));
              else if (type.equalsIgnoreCase("red")) {
                _redSkills[k].add(Integer.valueOf(augmentationId));
              }
              _allSkills.put(augmentationId, new TriggerInfo(skillId, skillLvL, t, chance));
            }
          }
      if (badAugmantData != 0)
        _log.info("AugmentationData: " + badAugmantData + " bad skill(s) were skipped.");
    }
    catch (Exception e)
    {
      _log.error("Error parsing augmentation_skillmap.xml.", e);
      return;
    }

    for (int i = 1; i < 5; i++)
    {
      try
      {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);

        File file = new File(Config.DATAPACK_ROOT, "data/stats/augmentation/augmentation_stats" + i + ".xml");
        Document doc = factory.newDocumentBuilder().parse(file);

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
          if ("list".equalsIgnoreCase(n.getNodeName()))
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
              if (!"stat".equalsIgnoreCase(d.getNodeName()))
                continue;
              NamedNodeMap attrs = d.getAttributes();
              String statName = attrs.getNamedItem("name").getNodeValue();
              double[] soloValues = null; double[] combinedValues = null;

              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if (!"table".equalsIgnoreCase(cd.getNodeName()))
                  continue;
                attrs = cd.getAttributes();
                String tableName = attrs.getNamedItem("name").getNodeValue();

                StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
                TDoubleArrayList array = new TDoubleArrayList();
                while (data.hasMoreTokens()) {
                  array.add(Double.parseDouble(data.nextToken()));
                }
                if (tableName.equalsIgnoreCase("#soloValues"))
                {
                  soloValues = new double[array.size()];
                  int x = 0;
                  for (double value : array.toNativeArray())
                    soloValues[(x++)] = value;
                }
                else
                {
                  combinedValues = new double[array.size()];
                  int x = 0;
                  for (double value : array.toNativeArray()) {
                    combinedValues[(x++)] = value;
                  }
                }
              }

              _augStats[(i - 1)].add(new augmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
            }
      }
      catch (Exception e)
      {
        _log.error("Error parsing augmentation_stats" + i + ".xml.", e);
        return;
      }

      try
      {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);

        File file = new File(Config.DATAPACK_ROOT, "data/stats/augmentation/augmentation_jewel_stats" + i + ".xml");
        Document doc = factory.newDocumentBuilder().parse(file);

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
          if ("list".equalsIgnoreCase(n.getNodeName()))
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
              if (!"stat".equalsIgnoreCase(d.getNodeName()))
                continue;
              NamedNodeMap attrs = d.getAttributes();
              String statName = attrs.getNamedItem("name").getNodeValue();
              double[] soloValues = null; double[] combinedValues = null;

              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if (!"table".equalsIgnoreCase(cd.getNodeName()))
                  continue;
                attrs = cd.getAttributes();
                String tableName = attrs.getNamedItem("name").getNodeValue();

                StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
                TDoubleArrayList array = new TDoubleArrayList();
                while (data.hasMoreTokens()) {
                  array.add(Double.parseDouble(data.nextToken()));
                }
                if (tableName.equalsIgnoreCase("#soloValues"))
                {
                  soloValues = new double[array.size()];
                  int x = 0;
                  for (double value : array.toNativeArray())
                    soloValues[(x++)] = value;
                }
                else
                {
                  combinedValues = new double[array.size()];
                  int x = 0;
                  for (double value : array.toNativeArray()) {
                    combinedValues[(x++)] = value;
                  }
                }
              }

              _augAccStats[(i - 1)].add(new augmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
            }
      }
      catch (Exception e)
      {
        _log.error("Error parsing jewel augmentation_stats" + i + ".xml.", e);
        return;
      }
    }
  }

  public int generateRandomAugmentation(int lifeStoneLevel, int lifeStoneGrade, int bodyPart)
  {
    switch (bodyPart)
    {
    case 2:
    case 4:
    case 6:
    case 8:
    case 16:
    case 32:
    case 48:
      return generateRandomAccessoryAugmentation(lifeStoneLevel, bodyPart);
    }
    return generateRandomWeaponAugmentation(lifeStoneLevel, lifeStoneGrade);
  }

  private int generateRandomWeaponAugmentation(int lifeStoneLevel, int lifeStoneGrade)
  {
    int stat12 = 0;
    int stat34 = 0;
    boolean generateSkill = false;
    boolean generateGlow = false;

    lifeStoneLevel = Math.min(lifeStoneLevel, 9);

    switch (lifeStoneGrade)
    {
    case 0:
      generateSkill = Rnd.chance(Config.AUGMENTATION_NG_SKILL_CHANCE);
      generateGlow = Rnd.chance(Config.AUGMENTATION_NG_GLOW_CHANCE);
      break;
    case 1:
      generateSkill = Rnd.chance(Config.AUGMENTATION_MID_SKILL_CHANCE);
      generateGlow = Rnd.chance(Config.AUGMENTATION_MID_GLOW_CHANCE);
      break;
    case 2:
      generateSkill = Rnd.chance(Config.AUGMENTATION_HIGH_SKILL_CHANCE);
      generateGlow = Rnd.chance(Config.AUGMENTATION_HIGH_GLOW_CHANCE);
      break;
    case 3:
      generateSkill = Rnd.chance(Config.AUGMENTATION_TOP_SKILL_CHANCE);
      generateGlow = Rnd.chance(Config.AUGMENTATION_TOP_GLOW_CHANCE);
    }

    if ((!generateSkill) && (Rnd.get(1, 100) <= Config.AUGMENTATION_BASESTAT_CHANCE)) {
      stat34 = Rnd.get(16341, 16344);
    }

    int resultColor = Rnd.get(0, 100);
    if ((stat34 == 0) && (!generateSkill)) {
      if (resultColor <= 15 * lifeStoneGrade + 40)
        resultColor = 1;
      else
        resultColor = 0;
    } else if ((resultColor <= 10 * lifeStoneGrade + 5) || (stat34 != 0))
      resultColor = 3;
    else if (resultColor <= 10 * lifeStoneGrade + 10)
      resultColor = 1;
    else {
      resultColor = 2;
    }

    if (generateSkill)
    {
      switch (resultColor)
      {
      case 1:
        stat34 = ((Integer)_blueSkills[lifeStoneLevel].get(Rnd.get(0, _blueSkills[lifeStoneLevel].size() - 1))).intValue();
        break;
      case 2:
        stat34 = ((Integer)_purpleSkills[lifeStoneLevel].get(Rnd.get(0, _purpleSkills[lifeStoneLevel].size() - 1))).intValue();
        break;
      case 3:
        stat34 = ((Integer)_redSkills[lifeStoneLevel].get(Rnd.get(0, _redSkills[lifeStoneLevel].size() - 1))).intValue();
      }
    }
    int offset;
    if (stat34 == 0)
    {
      int temp = Rnd.get(2, 3);
      int colorOffset = resultColor * 10 * 91 + temp * 3640 + 1;
      int offset = lifeStoneLevel * 91 + colorOffset;

      stat34 = Rnd.get(offset, offset + 91 - 1);
      if ((generateGlow) && (lifeStoneGrade >= 2))
        offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + lifeStoneGrade * 10 * 91 + 1;
      else
        offset = lifeStoneLevel * 91 + (temp - 2) * 3640 + Rnd.get(0, 1) * 10 * 91 + 1;
    }
    else
    {
      int offset;
      if (!generateGlow)
        offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + 1;
      else
        offset = lifeStoneLevel * 91 + Rnd.get(0, 1) * 3640 + (lifeStoneGrade + resultColor) / 2 * 10 * 91 + 1;
    }
    stat12 = Rnd.get(offset, offset + 91 - 1);

    return (stat34 << 16) + stat12;
  }

  private int generateRandomAccessoryAugmentation(int lifeStoneLevel, int bodyPart)
  {
    int stat12 = 0;
    int stat34 = 0;
    int base = 0;
    int skillsLength = 0;

    lifeStoneLevel = Math.min(lifeStoneLevel, 9);

    switch (bodyPart)
    {
    case 16:
    case 32:
    case 48:
      base = 16669 + 102 * lifeStoneLevel;
      skillsLength = 18;
      break;
    case 2:
    case 4:
    case 6:
      base = 17689 + 102 * lifeStoneLevel;
      skillsLength = 18;
      break;
    case 8:
      base = 18709 + 108 * lifeStoneLevel;
      skillsLength = 24;
      break;
    default:
      return 0;
    }

    int resultColor = Rnd.get(0, 3);
    TriggerInfo triggerInfo = null;

    stat12 = Rnd.get(21);

    if (Rnd.get(1, 100) <= Config.AUGMENTATION_ACC_SKILL_CHANCE)
    {
      stat34 = base + Rnd.get(skillsLength);
      triggerInfo = (TriggerInfo)_allSkills.get(stat34);
    }

    if (triggerInfo == null)
    {
      stat34 = (stat12 + 1 + Rnd.get(20)) % 21;

      stat34 = base + skillsLength + 21 * resultColor + stat34;
    }

    stat12 = base + skillsLength + 21 * resultColor + stat12;

    return (stat34 << 16) + stat12;
  }

  public class augmentationStat
  {
    private final Stats _stat;
    private final int _singleSize;
    private final int _combinedSize;
    private final double[] _singleValues;
    private final double[] _combinedValues;

    public augmentationStat(Stats stat, double[] sValues, double[] cValues)
    {
      _stat = stat;
      _singleSize = sValues.length;
      _singleValues = sValues;
      _combinedSize = cValues.length;
      _combinedValues = cValues;
    }

    public int getSingleStatSize()
    {
      return _singleSize;
    }

    public int getCombinedStatSize()
    {
      return _combinedSize;
    }

    public double getSingleStatValue(int i)
    {
      if ((i >= _singleSize) || (i < 0))
        return _singleValues[(_singleSize - 1)];
      return _singleValues[i];
    }

    public double getCombinedStatValue(int i)
    {
      if ((i >= _combinedSize) || (i < 0))
        return _combinedValues[(_combinedSize - 1)];
      return _combinedValues[i];
    }

    public Stats getStat()
    {
      return _stat;
    }
  }
}