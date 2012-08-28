package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AugmentationData
{
  private static Logger _log = AbstractLogger.getLogger(AugmentationData.class.getName());
  private static AugmentationData _instance;
  private static int CHANCE_SKILL = 11;
  private static int CHANCE_BASESTAT = 1;
  private static final int STAT_START = 1;
  private static final int STAT_END = 14560;
  private static final int STAT_BLOCKSIZE = 3640;
  private static final int STAT_SUBBLOCKSIZE = 91;
  private static final int BASESTAT_STR = 16341;
  private static final int BASESTAT_CON = 16342;
  private static final int BASESTAT_INT = 16343;
  private static final int BASESTAT_MEN = 16344;
  private static FastList<?>[] _augmentationStats;
  private static FastList<AugmentationSkill> _activeSkills;
  private static FastList<AugmentationSkill> _passiveSkills;
  private static FastList<AugmentationSkill> _chanceSkills;

  public static AugmentationData getInstance()
  {
    if (_instance == null) {
      _instance = new AugmentationData();
    }
    return _instance;
  }

  public AugmentationData()
  {
    _augmentationStats = new FastList[4];
    _augmentationStats[0] = new FastList();
    _augmentationStats[1] = new FastList();
    _augmentationStats[2] = new FastList();
    _augmentationStats[3] = new FastList();

    _activeSkills = new FastList();
    _passiveSkills = new FastList();
    _chanceSkills = new FastList();

    load();

    _log.info("Loading AugmentationData... total " + _augmentationStats[0].size() * 4 + " augmentation stats.");
    _log.info("Loading AugmentationData... total " + _activeSkills.size() + " active, " + _passiveSkills.size() + " passive and " + _chanceSkills.size() + " chance skills.");
  }

  private final void load()
  {
    try
    {
      if (Config.AUGMENT_BASESTAT > 0) {
        CHANCE_BASESTAT = Config.AUGMENT_BASESTAT;
      }
      SkillTable st = SkillTable.getInstance();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);

      File file = new File(Config.DATAPACK_ROOT + "/data/stats/augmentation/augmentation_skillmap.xml");
      if (!file.exists()) {
        _log.warning("The augmentation skillmap file is missing.");
        return;
      }

      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
        if ("list".equalsIgnoreCase(n.getNodeName())) {
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
            if ("augmentation".equalsIgnoreCase(d.getNodeName())) {
              NamedNodeMap attrs = d.getAttributes();
              int skillId = 0; int augmentationId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
              String type = "passive";

              for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                if ("skillId".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  skillId = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                } else if ("type".equalsIgnoreCase(cd.getNodeName())) {
                  attrs = cd.getAttributes();
                  type = attrs.getNamedItem("val").getNodeValue();
                }
              }

              AugmentationSkill temp = new AugmentationSkill(skillId, st.getMaxLevel(skillId, 1), augmentationId);

              if ((temp == null) || (temp.getSkill(1) == null) || ((Config.AUGMENT_EXCLUDE_NOTDONE) && (temp.getSkill(1).getSkillType() == L2Skill.SkillType.NOTDONE) && (temp.getSkill(1).getTargetType() == L2Skill.SkillTargetType.TARGET_NONE)))
              {
                continue;
              }

              if (type.equalsIgnoreCase("active"))
                _activeSkills.add(temp);
              else if (type.equalsIgnoreCase("passive"))
                _passiveSkills.add(temp);
              else
                _chanceSkills.add(temp);
            }
        }
      }
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error parsing augmentation_skillmap.xml.", e);
      return;
    }

    for (int i = 1; i < 5; i++)
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);

        File file = new File(Config.DATAPACK_ROOT + "/data/stats/augmentation/augmentation_stats" + i + ".xml");
        if (!file.exists()) {
          _log.warning("The augmentation stat data file " + i + " is missing.");
          return;
        }

        Document doc = factory.newDocumentBuilder().parse(file);

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
          if ("list".equalsIgnoreCase(n.getNodeName()))
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
              if ("stat".equalsIgnoreCase(d.getNodeName())) {
                NamedNodeMap attrs = d.getAttributes();
                String statName = attrs.getNamedItem("name").getNodeValue();
                float[] soloValues = null; float[] combinedValues = null;
                int x;
                Iterator i$;
                for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                  if ("table".equalsIgnoreCase(cd.getNodeName())) {
                    attrs = cd.getAttributes();
                    String tableName = attrs.getNamedItem("name").getNodeValue();

                    StringTokenizer data = new StringTokenizer(cd.getFirstChild().getNodeValue());
                    List array = new FastList();
                    while (data.hasMoreTokens())
                      array.add(Float.valueOf(Float.parseFloat(data.nextToken())));
                    int x;
                    Iterator i$;
                    if (tableName.equalsIgnoreCase("#soloValues")) {
                      soloValues = new float[array.size()];
                      x = 0;
                      for (i$ = array.iterator(); i$.hasNext(); ) { float value = ((Float)i$.next()).floatValue();
                        soloValues[(x++)] = value; }
                    }
                    else {
                      combinedValues = new float[array.size()];
                      x = 0;
                      for (i$ = array.iterator(); i$.hasNext(); ) { float value = ((Float)i$.next()).floatValue();
                        combinedValues[(x++)] = value;
                      }
                    }
                  }
                }

                _augmentationStats[(i - 1)].add(new AugmentationStat(Stats.valueOfXml(statName), soloValues, combinedValues));
              }
        }
      }
      catch (Exception e)
      {
        _log.log(Level.SEVERE, "Error parsing augmentation_stats" + i + ".xml.", e);
        return;
      }
  }

  public L2Augmentation generateRandomAugmentation(L2ItemInstance item, int lifeStoneLevel, int lifeStoneGrade, boolean premium)
  {
    int resultColor = 0;

    resultColor = Rnd.get(0, 100);
    if ((lifeStoneGrade == 3) || (resultColor <= 15 * lifeStoneGrade + 10))
      resultColor = 3;
    else if ((lifeStoneGrade == 2) || (resultColor <= 15 * lifeStoneGrade + 20))
      resultColor = 2;
    else if ((lifeStoneGrade == 1) || (resultColor <= 15 * lifeStoneGrade + 30))
      resultColor = 1;
    else {
      resultColor = 0;
    }

    int colorOffset = resultColor * 910 + (lifeStoneLevel - 1) * 91;

    int offset = (3 - lifeStoneGrade) * 3640 + colorOffset;

    int stat12 = Rnd.get(offset, offset + 91);
    int stat34 = 0;
    int skilllChance = 0;

    switch (lifeStoneGrade) {
    case 0:
      skilllChance = Config.AUGMENT_SKILL_NORM;
      break;
    case 1:
      skilllChance = Config.AUGMENT_SKILL_MID;
      break;
    case 2:
      skilllChance = Config.AUGMENT_SKILL_HIGH;
      break;
    case 3:
      skilllChance = Config.AUGMENT_SKILL_TOP;
    }

    boolean generateSkill = Rnd.calcEnchant(skilllChance, premium);

    if ((!generateSkill) && (Rnd.get(1, 100) <= CHANCE_BASESTAT)) {
      stat34 = Rnd.get(16341, 16344);
    }

    if ((stat34 == 0) && (!generateSkill)) {
      offset = lifeStoneGrade * 3640 + colorOffset;

      stat34 = Rnd.get(offset, offset + 91);
    }

    L2Skill skill = null;
    if (generateSkill) {
      AugmentationSkill temp = null;
      switch (Rnd.get(1, 3)) {
      case 1:
        temp = (AugmentationSkill)_chanceSkills.get(Rnd.get(0, _chanceSkills.size() - 1));
        skill = temp.getSkill(lifeStoneLevel);
        stat34 = temp.getAugmentationSkillId();
        break;
      case 2:
        temp = (AugmentationSkill)_activeSkills.get(Rnd.get(0, _activeSkills.size() - 1));
        skill = temp.getSkill(lifeStoneLevel);
        stat34 = temp.getAugmentationSkillId();
        break;
      case 3:
        temp = (AugmentationSkill)_passiveSkills.get(Rnd.get(0, _passiveSkills.size() - 1));
        skill = temp.getSkill(lifeStoneLevel);
        stat34 = temp.getAugmentationSkillId();
      }

    }

    return new L2Augmentation(item, (stat34 << 16) + stat12, skill, true);
  }

  public L2Augmentation generateAugmentation(L2ItemInstance item, int skillId, int skillLvl, int type)
  {
    int lifeStoneLevel = 10;
    int lifeStoneGrade = 3;

    int resultColor = Rnd.get(0, 3);

    int colorOffset = resultColor * 910 + (lifeStoneLevel - 1) * 91;
    int offset = (3 - lifeStoneGrade) * 3640 + colorOffset;
    int stat12 = Rnd.get(offset, offset + 91);
    int stat34 = 0;

    L2Skill skill = null;
    AugmentationSkill temp = null;
    FastList.Node n;
    FastList.Node n;
    FastList.Node n;
    switch (type) {
    case 1:
      n = _chanceSkills.head(); for (FastList.Node end = _chanceSkills.tail(); (n = n.getNext()) != end; ) {
        AugmentationSkill value = (AugmentationSkill)n.getValue();
        if (value._skillId == skillId) {
          temp = value;
          skill = temp.getSkill(skillLvl);
          stat34 = temp.getAugmentationSkillId();
        }
      }
      break;
    case 2:
      n = _activeSkills.head(); for (FastList.Node end = _activeSkills.tail(); (n = n.getNext()) != end; ) {
        AugmentationSkill value = (AugmentationSkill)n.getValue();
        if (value._skillId == skillId) {
          temp = value;
          skill = temp.getSkill(skillLvl);
          stat34 = temp.getAugmentationSkillId();
        }
      }
      break;
    case 3:
      n = _passiveSkills.head(); for (FastList.Node end = _passiveSkills.tail(); (n = n.getNext()) != end; ) {
        AugmentationSkill value = (AugmentationSkill)n.getValue();
        if (value._skillId == skillId) {
          temp = value;
          skill = temp.getSkill(skillLvl);
          stat34 = temp.getAugmentationSkillId();
        }
      }
    }

    return new L2Augmentation(item, (stat34 << 16) + stat12, skill, true);
  }

  public FastList<AugStat> getAugStatsById(int augmentationId)
  {
    FastList temp = new FastList();

    int[] stats = new int[2];
    stats[0] = (0xFFFF & augmentationId);
    stats[1] = (augmentationId >> 16);

    for (int i = 0; i < 2; i++)
    {
      if ((stats[i] >= 1) && (stats[i] <= 14560)) {
        int block = 0;
        while (stats[i] > 3640) {
          stats[i] -= 3640;
          block++;
        }

        int subblock = 0;
        while (stats[i] > 91) {
          stats[i] -= 91;
          subblock++;
        }

        if (stats[i] < 14)
        {
          AugmentationStat as = (AugmentationStat)_augmentationStats[block].get(stats[i] - 1);
          temp.add(new AugStat(as.getStat(), as.getSingleStatValue(subblock)));
        }
        else {
          stats[i] -= 13;
          int x = 12;
          int rescales = 0;

          while (stats[i] > x) {
            stats[i] -= x;
            x--;
            rescales++;
          }

          AugmentationStat as = (AugmentationStat)_augmentationStats[block].get(rescales);
          if (rescales == 0)
            temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
          else {
            temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2 + 1)));
          }

          as = (AugmentationStat)_augmentationStats[block].get(rescales + stats[i]);
          if (as.getStat() == Stats.CRITICAL_DAMAGE)
            temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock)));
          else {
            temp.add(new AugStat(as.getStat(), as.getCombinedStatValue(subblock * 2)));
          }
        }
      }
      else if ((stats[i] >= 16341) && (stats[i] <= 16344)) {
        switch (stats[i]) {
        case 16341:
          temp.add(new AugStat(Stats.STAT_STR, 1.0F));
          break;
        case 16342:
          temp.add(new AugStat(Stats.STAT_CON, 1.0F));
          break;
        case 16343:
          temp.add(new AugStat(Stats.STAT_INT, 1.0F));
          break;
        case 16344:
          temp.add(new AugStat(Stats.STAT_MEN, 1.0F));
        }
      }

    }

    return temp;
  }

  public static class AugStat
  {
    private Stats _stat;
    private float _value;

    public AugStat(Stats stat, float value)
    {
      _stat = stat;
      _value = value;
    }

    public Stats getStat() {
      return _stat;
    }

    public float getValue() {
      return _value;
    }
  }

  public static class AugmentationStat
  {
    private Stats _stat;
    private int _singleSize;
    private int _combinedSize;
    private float[] _singleValues;
    private float[] _combinedValues;

    public AugmentationStat(Stats stat, float[] sValues, float[] cValues)
    {
      _stat = stat;
      _singleSize = sValues.length;
      _singleValues = sValues;
      _combinedSize = cValues.length;
      _combinedValues = cValues;
    }

    public int getSingleStatSize() {
      return _singleSize;
    }

    public int getCombinedStatSize() {
      return _combinedSize;
    }

    public float getSingleStatValue(int i) {
      if ((i >= _singleSize) || (i < 0)) {
        return _singleValues[(_singleSize - 1)];
      }
      return _singleValues[i];
    }

    public float getCombinedStatValue(int i) {
      if ((i >= _combinedSize) || (i < 0)) {
        return _combinedValues[(_combinedSize - 1)];
      }
      return _combinedValues[i];
    }

    public Stats getStat() {
      return _stat;
    }
  }

  public static class AugmentationSkill
  {
    private int _skillId;
    private int _maxSkillLevel;
    private int _augmentationSkillId;

    public AugmentationSkill(int skillId, int maxSkillLevel, int augmentationSkillId)
    {
      _skillId = skillId;
      _maxSkillLevel = maxSkillLevel;
      _augmentationSkillId = augmentationSkillId;
    }

    public L2Skill getSkill(int level) {
      if (level > _maxSkillLevel) {
        return SkillTable.getInstance().getInfo(_skillId, _maxSkillLevel);
      }
      return SkillTable.getInstance().getInfo(_skillId, level);
    }

    public int getAugmentationSkillId() {
      return _augmentationSkillId;
    }
  }
}