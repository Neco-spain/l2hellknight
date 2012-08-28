package net.sf.l2j.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.skills.conditions.Condition;
import net.sf.l2j.gameserver.skills.conditions.ConditionElementSeed;
import net.sf.l2j.gameserver.skills.conditions.ConditionForceBuff;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameChance;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameTime;
import net.sf.l2j.gameserver.skills.conditions.ConditionGameTime.CheckGameTime;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicAnd;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicNot;
import net.sf.l2j.gameserver.skills.conditions.ConditionLogicOr;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHp;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerHpPercentage;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerLevel;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerMp;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerRace;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionSkillStats;
import net.sf.l2j.gameserver.skills.conditions.ConditionSlotItemId;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetAggro;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetClassIdRestriction;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetLevel;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetRaceId;
import net.sf.l2j.gameserver.skills.conditions.ConditionTargetUsesWeaponKind;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingSkill;
import net.sf.l2j.gameserver.skills.conditions.ConditionWithSkill;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;
import net.sf.l2j.gameserver.skills.funcs.Lambda;
import net.sf.l2j.gameserver.skills.funcs.LambdaCalc;
import net.sf.l2j.gameserver.skills.funcs.LambdaConst;
import net.sf.l2j.gameserver.skills.funcs.LambdaStats;
import net.sf.l2j.gameserver.skills.funcs.LambdaStats.StatsType;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

abstract class DocumentBase
{
  static Logger _log = Logger.getLogger(DocumentBase.class.getName());
  private File _file;
  protected Map<String, String[]> _tables;

  DocumentBase(File pFile)
  {
    _file = pFile;
    _tables = new FastMap();
  }

  Document parse()
  {
    Document doc;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      doc = factory.newDocumentBuilder().parse(_file);
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error loading file " + _file, e);
      return null;
    }
    try
    {
      parseDocument(doc);
    }
    catch (Exception e)
    {
      _log.log(Level.SEVERE, "Error in file " + _file, e);
      return null;
    }
    return doc; } 
  protected abstract void parseDocument(Document paramDocument);

  protected abstract StatsSet getStatsSet();

  protected abstract String getTableValue(String paramString);

  protected abstract String getTableValue(String paramString, int paramInt);

  protected void resetTable() { _tables = new FastMap();
  }

  protected void setTable(String name, String[] table)
  {
    _tables.put(name, table);
  }

  protected void parseTemplate(Node n, Object template)
  {
    Condition condition = null;
    n = n.getFirstChild();
    if (n == null) return;
    if ("cond".equalsIgnoreCase(n.getNodeName()))
    {
      condition = parseCondition(n.getFirstChild(), template);
      Node msg = n.getAttributes().getNamedItem("msg");
      if ((condition != null) && (msg != null)) condition.setMessage(msg.getNodeValue());
      n = n.getNextSibling();
    }
    for (; n != null; n = n.getNextSibling())
    {
      if ("add".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Add", condition);
      } else if ("sub".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Sub", condition);
      } else if ("mul".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Mul", condition);
      } else if ("basemul".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "BaseMul", condition);
      } else if ("div".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Div", condition);
      } else if ("set".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Set", condition);
      } else if ("enchant".equalsIgnoreCase(n.getNodeName())) { attachFunc(n, template, "Enchant", condition);
      }
      else if ("skill".equalsIgnoreCase(n.getNodeName())) { attachSkill(n, template, condition); } else {
        if (!"effect".equalsIgnoreCase(n.getNodeName()))
          continue;
        if ((template instanceof EffectTemplate)) throw new RuntimeException("Nested effects");
        attachEffect(n, template, condition);
      }
    }
  }

  protected void attachFunc(Node n, Object template, String name, Condition attachCond)
  {
    Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
    String order = n.getAttributes().getNamedItem("order").getNodeValue();
    Lambda lambda = getLambda(n, template);
    int ord = Integer.decode(getValue(order, template)).intValue();
    Condition applayCond = parseCondition(n.getFirstChild(), template);
    FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
    if ((template instanceof L2Item)) ((L2Item)template).attach(ft);
    else if ((template instanceof L2Skill)) ((L2Skill)template).attach(ft);
    else if ((template instanceof EffectTemplate)) ((EffectTemplate)template).attach(ft);
  }

  protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc)
  {
    String name = n.getNodeName();
    TextBuilder sb = new TextBuilder(name);
    sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
    name = sb.toString();
    Lambda lambda = getLambda(n, template);
    FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.funcs.length, lambda);
    calc.addFunc(ft.getFunc(new Env(), calc));
  }

  protected void attachEffect(Node n, Object template, Condition attachCond)
  {
    NamedNodeMap attrs = n.getAttributes();
    String name = attrs.getNamedItem("name").getNodeValue();
    int time = 0; int count = 1;

    if (attrs.getNamedItem("count") != null)
    {
      count = Integer.decode(getValue(attrs.getNamedItem("count").getNodeValue(), template)).intValue();
    }
    if (attrs.getNamedItem("time") != null)
    {
      time = Integer.decode(getValue(attrs.getNamedItem("time").getNodeValue(), template)).intValue();
      if (Config.ENABLE_MODIFY_SKILL_DURATION)
      {
        if (Config.SKILL_DURATION_LIST.containsKey(Integer.valueOf(((L2Skill)template).getId())))
        {
          if (((L2Skill)template).getLevel() < 100)
            time = ((Integer)Config.SKILL_DURATION_LIST.get(Integer.valueOf(((L2Skill)template).getId()))).intValue();
          else if ((((L2Skill)template).getLevel() >= 100) && (((L2Skill)template).getLevel() < 140))
            time += ((Integer)Config.SKILL_DURATION_LIST.get(Integer.valueOf(((L2Skill)template).getId()))).intValue();
          else if (((L2Skill)template).getLevel() > 140)
            time = ((Integer)Config.SKILL_DURATION_LIST.get(Integer.valueOf(((L2Skill)template).getId()))).intValue();
          if (Config.DEBUG)
            _log.info("*** Skill " + ((L2Skill)template).getName() + " (" + ((L2Skill)template).getLevel() + ") changed duration to " + time + " seconds.");
        }
      }
    }
    else if (((L2Skill)template).getBuffDuration() > 0) {
      time = ((L2Skill)template).getBuffDuration() / 1000 / count;
    }
    if (attrs.getNamedItem("name").getNodeValue().equalsIgnoreCase("buff")) {
      if (((L2Skill)template).isDance()) time = (int)(time * Config.DANCE_TIME_MULTIPLIER);
      else if ((1199 < time) && (time < 1501)) time = (int)(time * Config.BUFF_TIME_MULTIPLIER);
      else if ((299 < time) && (time < 481)) time = (int)(time * Config.SPIRIT_TIME_MULTIPLIER);
    }
    boolean self = false;
    if (attrs.getNamedItem("self") != null)
    {
      if (Integer.decode(getValue(attrs.getNamedItem("self").getNodeValue(), template)).intValue() == 1)
        self = true;
    }
    boolean icon = true;
    if (attrs.getNamedItem("noicon") != null)
    {
      if (Integer.decode(getValue(attrs.getNamedItem("noicon").getNodeValue(), template)).intValue() == 1)
        icon = false;
    }
    Lambda lambda = getLambda(n, template);
    Condition applayCond = parseCondition(n.getFirstChild(), template);
    int abnormal = 0;
    if (attrs.getNamedItem("abnormal") != null)
    {
      String abn = attrs.getNamedItem("abnormal").getNodeValue();
      if (abn.equals("poison")) abnormal = 2;
      else if (abn.equals("bleeding")) abnormal = 1;
      else if (abn.equals("flame")) abnormal = 16384;
      else if (abn.equals("bighead")) abnormal = 8192;
      else if (abn.equals("stealth")) abnormal = 1048576;
    }
    float stackOrder = 0.0F;
    String stackType = "none";
    if (attrs.getNamedItem("stackType") != null)
    {
      stackType = attrs.getNamedItem("stackType").getNodeValue();
    }
    if (attrs.getNamedItem("stackOrder") != null)
    {
      stackOrder = Float.parseFloat(getValue(attrs.getNamedItem("stackOrder").getNodeValue(), template));
    }
    EffectTemplate lt = new EffectTemplate(attachCond, applayCond, name, lambda, count, time, abnormal, stackType, stackOrder, icon);

    parseTemplate(n, lt);
    if ((template instanceof L2Item)) ((L2Item)template).attach(lt);
    else if (((template instanceof L2Skill)) && (!self)) ((L2Skill)template).attach(lt);
    else if (((template instanceof L2Skill)) && (self)) ((L2Skill)template).attachSelf(lt);
  }

  protected void attachSkill(Node n, Object template, Condition attachCond)
  {
    NamedNodeMap attrs = n.getAttributes();
    int id = 0; int lvl = 1;
    if (attrs.getNamedItem("id") != null)
    {
      id = Integer.decode(getValue(attrs.getNamedItem("id").getNodeValue(), template)).intValue();
    }
    if (attrs.getNamedItem("lvl") != null)
    {
      lvl = Integer.decode(getValue(attrs.getNamedItem("lvl").getNodeValue(), template)).intValue();
    }

    L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
    if (attrs.getNamedItem("chance") != null)
    {
      if (((template instanceof L2Weapon)) || ((template instanceof L2Item)))
      {
        skill.attach(new ConditionGameChance(Integer.decode(getValue(attrs.getNamedItem("chance").getNodeValue(), template)).intValue()), true);
      }
      else
      {
        skill.attach(new ConditionGameChance(Integer.decode(getValue(attrs.getNamedItem("chance").getNodeValue(), template)).intValue()), false);
      }
    }
    if ((template instanceof L2Weapon))
    {
      if ((attrs.getNamedItem("onUse") != null) || ((attrs.getNamedItem("onCrit") == null) && (attrs.getNamedItem("onCast") == null)))
      {
        ((L2Weapon)template).attach(skill);
      }if (attrs.getNamedItem("onCrit") != null) ((L2Weapon)template).attachOnCrit(skill);
      if (attrs.getNamedItem("onCast") != null) ((L2Weapon)template).attachOnCast(skill);
    }
    else if ((template instanceof L2Item))
    {
      ((L2Item)template).attach(skill);
    }
  }

  protected Condition parseCondition(Node n, Object template)
  {
    while ((n != null) && (n.getNodeType() != 1))
      n = n.getNextSibling();
    if (n == null) return null;
    if ("and".equalsIgnoreCase(n.getNodeName())) return parseLogicAnd(n, template);
    if ("or".equalsIgnoreCase(n.getNodeName())) return parseLogicOr(n, template);
    if ("not".equalsIgnoreCase(n.getNodeName())) return parseLogicNot(n, template);
    if ("player".equalsIgnoreCase(n.getNodeName())) return parsePlayerCondition(n);
    if ("target".equalsIgnoreCase(n.getNodeName())) return parseTargetCondition(n, template);
    if ("skill".equalsIgnoreCase(n.getNodeName())) return parseSkillCondition(n);
    if ("using".equalsIgnoreCase(n.getNodeName())) return parseUsingCondition(n);
    if ("game".equalsIgnoreCase(n.getNodeName())) return parseGameCondition(n);
    return null;
  }

  protected Condition parseLogicAnd(Node n, Object template)
  {
    ConditionLogicAnd cond = new ConditionLogicAnd();
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
    {
      if (n.getNodeType() != 1) continue; cond.add(parseCondition(n, template));
    }
    if ((cond.conditions == null) || (cond.conditions.length == 0))
      _log.severe("Empty <and> condition in " + _file);
    return cond;
  }

  protected Condition parseLogicOr(Node n, Object template)
  {
    ConditionLogicOr cond = new ConditionLogicOr();
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
    {
      if (n.getNodeType() != 1) continue; cond.add(parseCondition(n, template));
    }
    if ((cond.conditions == null) || (cond.conditions.length == 0))
      _log.severe("Empty <or> condition in " + _file);
    return cond;
  }

  protected Condition parseLogicNot(Node n, Object template)
  {
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
    {
      if (n.getNodeType() == 1)
      {
        return new ConditionLogicNot(parseCondition(n, template));
      }
    }
    _log.severe("Empty <not> condition in " + _file);
    return null;
  }

  protected Condition parsePlayerCondition(Node n)
  {
    Condition cond = null;
    int[] ElementSeeds = new int[5];
    byte[] forces = new byte[2];
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++)
    {
      Node a = attrs.item(i);
      if ("race".equalsIgnoreCase(a.getNodeName()))
      {
        Race race = Race.valueOf(a.getNodeValue());
        cond = joinAnd(cond, new ConditionPlayerRace(race));
      }
      else if ("level".equalsIgnoreCase(a.getNodeName()))
      {
        int lvl = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
        cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
      }
      else if ("resting".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, val));
      }
      else if ("flying".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FLYING, val));
      }
      else if ("moving".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.MOVING, val));
      }
      else if ("running".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, val));
      }
      else if ("behind".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.BEHIND, val));
      }
      else if ("front".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.FRONT, val));
      }
      else if ("hp".equalsIgnoreCase(a.getNodeName()))
      {
        int hp = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
        cond = joinAnd(cond, new ConditionPlayerHp(hp));
      }
      else if ("hprate".equalsIgnoreCase(a.getNodeName()))
      {
        double rate = Double.parseDouble(getValue(a.getNodeValue(), null));
        cond = joinAnd(cond, new ConditionPlayerHpPercentage(rate));
      }
      else if ("mp".equalsIgnoreCase(a.getNodeName()))
      {
        int hp = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
        cond = joinAnd(cond, new ConditionPlayerMp(hp));
      }
      else if ("seed_fire".equalsIgnoreCase(a.getNodeName()))
      {
        ElementSeeds[0] = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      }
      else if ("seed_water".equalsIgnoreCase(a.getNodeName()))
      {
        ElementSeeds[1] = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      }
      else if ("seed_wind".equalsIgnoreCase(a.getNodeName()))
      {
        ElementSeeds[2] = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      }
      else if ("seed_various".equalsIgnoreCase(a.getNodeName()))
      {
        ElementSeeds[3] = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      }
      else if ("seed_any".equalsIgnoreCase(a.getNodeName()))
      {
        ElementSeeds[4] = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      }
      else if ("battle_force".equalsIgnoreCase(a.getNodeName()))
      {
        forces[0] = Byte.decode(getValue(a.getNodeValue(), null)).byteValue();
      } else {
        if (!"spell_force".equalsIgnoreCase(a.getNodeName()))
          continue;
        forces[1] = Byte.decode(getValue(a.getNodeValue(), null)).byteValue();
      }
    }
    for (int i = 0; i < ElementSeeds.length; i++)
    {
      if (ElementSeeds[i] <= 0)
        continue;
      cond = joinAnd(cond, new ConditionElementSeed(ElementSeeds));
      break;
    }

    if (forces[0] + forces[1] > 0)
    {
      cond = joinAnd(cond, new ConditionForceBuff(forces));
    }

    if (cond == null) _log.severe("Unrecognized <player> condition in " + _file);
    return cond;
  }

  protected Condition parseTargetCondition(Node n, Object template)
  {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++)
    {
      Node a = attrs.item(i);
      if ("aggro".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionTargetAggro(val));
      }
      else if ("level".equalsIgnoreCase(a.getNodeName()))
      {
        int lvl = Integer.decode(getValue(a.getNodeValue(), template)).intValue();
        cond = joinAnd(cond, new ConditionTargetLevel(lvl));
      }
      else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
      {
        FastList array = new FastList();
        StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
        while (st.hasMoreTokens())
        {
          String item = st.nextToken().trim();
          array.add(Integer.decode(getValue(item, null)));
        }
        cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
      }
      else if ("race_id".equalsIgnoreCase(a.getNodeName()))
      {
        FastList array = new FastList();
        StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
        while (st.hasMoreTokens())
        {
          String item = st.nextToken().trim();
          array.add(Integer.valueOf(Integer.decode(getValue(item, null)).intValue() - 1));
        }
        cond = joinAnd(cond, new ConditionTargetRaceId(array));
      } else {
        if (!"using".equalsIgnoreCase(a.getNodeName()))
          continue;
        int mask = 0;
        StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
        while (st.hasMoreTokens())
        {
          String item = st.nextToken().trim();
          for (L2WeaponType wt : L2WeaponType.values())
          {
            if (!wt.toString().equals(item))
              continue;
            mask |= wt.mask();
            break;
          }

          for (L2ArmorType at : L2ArmorType.values())
          {
            if (!at.toString().equals(item))
              continue;
            mask |= at.mask();
            break;
          }
        }

        cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
      }
    }
    if (cond == null) _log.severe("Unrecognized <target> condition in " + _file);
    return cond;
  }

  protected Condition parseSkillCondition(Node n)
  {
    NamedNodeMap attrs = n.getAttributes();
    Stats stat = Stats.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
    return new ConditionSkillStats(stat);
  }

  protected Condition parseUsingCondition(Node n)
  {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++)
    {
      Node a = attrs.item(i);
      if ("kind".equalsIgnoreCase(a.getNodeName()))
      {
        int mask = 0;
        StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
        while (st.hasMoreTokens())
        {
          String item = st.nextToken().trim();
          for (L2WeaponType wt : L2WeaponType.values())
          {
            if (!wt.toString().equals(item))
              continue;
            mask |= wt.mask();
            break;
          }

          for (L2ArmorType at : L2ArmorType.values())
          {
            if (!at.toString().equals(item))
              continue;
            mask |= at.mask();
            break;
          }
        }

        cond = joinAnd(cond, new ConditionUsingItemType(mask));
      }
      else if ("skill".equalsIgnoreCase(a.getNodeName()))
      {
        int id = Integer.parseInt(a.getNodeValue());
        cond = joinAnd(cond, new ConditionUsingSkill(id));
      } else {
        if (!"slotitem".equalsIgnoreCase(a.getNodeName()))
          continue;
        StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
        int id = Integer.parseInt(st.nextToken().trim());
        int slot = Integer.parseInt(st.nextToken().trim());
        int enchant = 0;
        if (st.hasMoreTokens()) enchant = Integer.parseInt(st.nextToken().trim());
        cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
      }
    }
    if (cond == null) _log.severe("Unrecognized <using> condition in " + _file);
    return cond;
  }

  protected Condition parseGameCondition(Node n)
  {
    Condition cond = null;
    NamedNodeMap attrs = n.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++)
    {
      Node a = attrs.item(i);
      if ("skill".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionWithSkill(val));
      }
      if ("night".equalsIgnoreCase(a.getNodeName()))
      {
        boolean val = Boolean.valueOf(a.getNodeValue()).booleanValue();
        cond = joinAnd(cond, new ConditionGameTime(ConditionGameTime.CheckGameTime.NIGHT, val));
      }
      if (!"chance".equalsIgnoreCase(a.getNodeName()))
        continue;
      int val = Integer.decode(getValue(a.getNodeValue(), null)).intValue();
      cond = joinAnd(cond, new ConditionGameChance(val));
    }

    if (cond == null) _log.severe("Unrecognized <game> condition in " + _file);
    return cond;
  }

  protected void parseTable(Node n)
  {
    NamedNodeMap attrs = n.getAttributes();
    String name = attrs.getNamedItem("name").getNodeValue();
    if (name.charAt(0) != '#') throw new IllegalArgumentException("Table name must start with #");
    StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
    List array = new FastList();
    while (data.hasMoreTokens())
      array.add(data.nextToken());
    String[] res = new String[array.size()];
    int i = 0;
    for (String str : array)
    {
      res[(i++)] = str;
    }
    setTable(name, res);
  }

  protected void parseBeanSet(Node n, StatsSet set, Integer level)
  {
    String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
    String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
    char ch = value.length() == 0 ? ' ' : value.charAt(0);
    if ((ch == '#') || (ch == '-') || (Character.isDigit(ch))) set.set(name, String.valueOf(getValue(value, level)));
    else
    {
      set.set(name, value);
    }
  }

  protected Lambda getLambda(Node n, Object template) {
    Node nval = n.getAttributes().getNamedItem("val");
    if (nval != null)
    {
      String val = nval.getNodeValue();
      if (val.charAt(0) == '#')
      {
        return new LambdaConst(Double.parseDouble(getTableValue(val)));
      }
      if (val.charAt(0) == '$')
      {
        if (val.equalsIgnoreCase("$player_level"))
          return new LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL);
        if (val.equalsIgnoreCase("$target_level"))
          return new LambdaStats(LambdaStats.StatsType.TARGET_LEVEL);
        if (val.equalsIgnoreCase("$player_max_hp"))
          return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP);
        if (val.equalsIgnoreCase("$player_max_mp"))
          return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP);
        StatsSet set = getStatsSet();
        String field = set.getString(val.substring(1));
        if (field != null)
        {
          return new LambdaConst(Double.parseDouble(getValue(field, template)));
        }
        throw new IllegalArgumentException("Unknown value " + val);
      }

      return new LambdaConst(Double.parseDouble(val));
    }

    LambdaCalc calc = new LambdaCalc();
    n = n.getFirstChild();
    while ((n != null) && (n.getNodeType() != 1))
      n = n.getNextSibling();
    if ((n == null) || (!"val".equals(n.getNodeName()))) {
      throw new IllegalArgumentException("Value not specified");
    }
    for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
    {
      if (n.getNodeType() == 1)
        attachLambdaFunc(n, template, calc);
    }
    return calc;
  }

  protected String getValue(String value, Object template)
  {
    if (value.charAt(0) == '#')
    {
      if ((template instanceof L2Skill)) return getTableValue(value);
      if ((template instanceof Integer)) return getTableValue(value, ((Integer)template).intValue());
      throw new IllegalStateException();
    }
    return value;
  }

  protected Condition joinAnd(Condition cond, Condition c)
  {
    if (cond == null) return c;
    if ((cond instanceof ConditionLogicAnd))
    {
      ((ConditionLogicAnd)cond).add(c);
      return cond;
    }
    ConditionLogicAnd and = new ConditionLogicAnd();
    and.add(cond);
    and.add(c);
    return and;
  }
}