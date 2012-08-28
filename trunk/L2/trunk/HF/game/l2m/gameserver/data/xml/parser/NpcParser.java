package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import l2p.commons.data.xml.AbstractDirParser;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.TeleportLocation;
import l2m.gameserver.model.reward.RewardData;
import l2m.gameserver.model.reward.RewardGroup;
import l2m.gameserver.model.reward.RewardList;
import l2m.gameserver.model.reward.RewardType;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.npc.AbsorbInfo;
import l2m.gameserver.templates.npc.AbsorbInfo.AbsorbType;
import l2m.gameserver.templates.npc.Faction;
import l2m.gameserver.templates.npc.MinionData;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public final class NpcParser extends AbstractDirParser<NpcHolder>
{
  private static final NpcParser _instance = new NpcParser();

  public static NpcParser getInstance()
  {
    return _instance;
  }

  private NpcParser()
  {
    super(NpcHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/npc/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "npc.dtd";
  }

  protected void readData(org.dom4j.Element rootElement)
    throws Exception
  {
    for (Iterator npcIterator = rootElement.elementIterator(); npcIterator.hasNext(); )
    {
      org.dom4j.Element npcElement = (org.dom4j.Element)npcIterator.next();
      int npcId = Integer.parseInt(npcElement.attributeValue("id"));
      int templateId = npcElement.attributeValue("template_id") == null ? 0 : Integer.parseInt(npcElement.attributeValue("template_id"));
      String name = npcElement.attributeValue("name");
      String title = npcElement.attributeValue("title");

      StatsSet set = new StatsSet();
      set.set("npcId", npcId);
      set.set("displayId", templateId);
      set.set("name", name);
      set.set("title", title);
      set.set("baseCpReg", 0);
      set.set("baseCpMax", 0);

      for (Iterator firstIterator = npcElement.elementIterator(); firstIterator.hasNext(); )
      {
        org.dom4j.Element firstElement = (org.dom4j.Element)firstIterator.next();
        if (firstElement.getName().equalsIgnoreCase("set"))
        {
          set.set(firstElement.attributeValue("name"), firstElement.attributeValue("value"));
        }
        else
        {
          Iterator eIterator;
          if (firstElement.getName().equalsIgnoreCase("equip"))
          {
            for (eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
            {
              org.dom4j.Element eElement = (org.dom4j.Element)eIterator.next();
              set.set(eElement.getName(), eElement.attributeValue("item_id"));
            }
          }
          else if (firstElement.getName().equalsIgnoreCase("ai_params"))
          {
            StatsSet ai = new StatsSet();
            for (Iterator eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
            {
              org.dom4j.Element eElement = (org.dom4j.Element)eIterator.next();
              ai.set(eElement.attributeValue("name"), eElement.attributeValue("value"));
            }
            set.set("aiParams", ai);
          }
          else if (firstElement.getName().equalsIgnoreCase("attributes"))
          {
            int[] attributeAttack = new int[6];
            int[] attributeDefence = new int[6];
            for (Iterator eIterator = firstElement.elementIterator(); eIterator.hasNext(); )
            {
              org.dom4j.Element eElement = (org.dom4j.Element)eIterator.next();

              if (eElement.getName().equalsIgnoreCase("defence"))
              {
                l2p.gameserver.model.base.Element element = l2p.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
                attributeDefence[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
              }
              else if (eElement.getName().equalsIgnoreCase("attack"))
              {
                l2p.gameserver.model.base.Element element = l2p.gameserver.model.base.Element.getElementByName(eElement.attributeValue("attribute"));
                attributeAttack[element.getId()] = Integer.parseInt(eElement.attributeValue("value"));
              }
            }

            set.set("baseAttributeAttack", attributeAttack);
            set.set("baseAttributeDefence", attributeDefence);
          }
        }
      }
      NpcTemplate template = new NpcTemplate(set);

      for (Iterator secondIterator = npcElement.elementIterator(); secondIterator.hasNext(); )
      {
        org.dom4j.Element secondElement = (org.dom4j.Element)secondIterator.next();
        String nodeName = secondElement.getName();
        if (nodeName.equalsIgnoreCase("faction"))
        {
          String factionId = secondElement.attributeValue("name");
          Faction faction = new Faction(factionId);
          int factionRange = Integer.parseInt(secondElement.attributeValue("range"));
          faction.setRange(factionRange);
          for (Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
          {
            org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();
            int ignoreId = Integer.parseInt(nextElement.attributeValue("npc_id"));
            faction.addIgnoreNpcId(ignoreId);
          }
          template.setFaction(faction);
        }
        else if (nodeName.equalsIgnoreCase("rewardlist"))
        {
          RewardType type = RewardType.valueOf(secondElement.attributeValue("type"));
          boolean autoLoot = (secondElement.attributeValue("auto_loot") != null) && (Boolean.parseBoolean(secondElement.attributeValue("auto_loot")));
          RewardList list = new RewardList(type, autoLoot);

          for (Iterator nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
          {
            org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();
            String nextName = nextElement.getName();
            if (nextName.equalsIgnoreCase("group"))
            {
              double enterChance = nextElement.attributeValue("chance") == null ? 1000000.0D : Double.parseDouble(nextElement.attributeValue("chance")) * 10000.0D;

              RewardGroup group = (type == RewardType.SWEEP) || (type == RewardType.NOT_RATED_NOT_GROUPED) ? null : new RewardGroup(enterChance);
              for (Iterator rewardIterator = nextElement.elementIterator(); rewardIterator.hasNext(); )
              {
                org.dom4j.Element rewardElement = (org.dom4j.Element)rewardIterator.next();
                RewardData data = parseReward(rewardElement);
                if ((type == RewardType.SWEEP) || (type == RewardType.NOT_RATED_NOT_GROUPED))
                  warn("Can't load rewardlist from group: " + npcId + "; type: " + type);
                else {
                  group.addData(data);
                }
              }
              if (group != null)
                list.add(group);
            }
            else if (nextName.equalsIgnoreCase("reward"))
            {
              if ((type != RewardType.SWEEP) && (type != RewardType.NOT_RATED_NOT_GROUPED) && (type != RewardType.NOT_GROUPED))
              {
                warn("Reward can't be without group(and not grouped): " + npcId + "; type: " + type);
                continue;
              }

              RewardData data = parseReward(nextElement);
              RewardGroup g = new RewardGroup(1000000.0D);
              g.addData(data);
              list.add(g);
            }
          }

          if (((type == RewardType.RATED_GROUPED) || (type == RewardType.NOT_RATED_GROUPED)) && 
            (!list.validate())) {
            warn("Problems with rewardlist for npc: " + npcId + "; type: " + type);
          }
          template.putRewardList(type, list);
        }
        else
        {
          Iterator nextIterator;
          if (nodeName.equalsIgnoreCase("skills"))
          {
            for (nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
            {
              org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();
              int id = Integer.parseInt(nextElement.attributeValue("id"));
              int level = Integer.parseInt(nextElement.attributeValue("level"));

              if (id == 4416)
              {
                template.setRace(level);
              }

              Skill skill = SkillTable.getInstance().getInfo(id, level);

              if (skill == null)
              {
                continue;
              }

              template.addSkill(skill);
            }
          }
          else
          {
            Iterator nextIterator;
            if (nodeName.equalsIgnoreCase("minions"))
            {
              for (nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
              {
                org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();
                int id = Integer.parseInt(nextElement.attributeValue("npc_id"));
                int count = Integer.parseInt(nextElement.attributeValue("count"));

                template.addMinion(new MinionData(id, count));
              }
            }
            else
            {
              Iterator nextIterator;
              if (nodeName.equalsIgnoreCase("teach_classes"))
              {
                for (nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
                {
                  org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();

                  int id = Integer.parseInt(nextElement.attributeValue("id"));

                  template.addTeachInfo(l2p.gameserver.model.base.ClassId.VALUES[id]);
                }
              }
              else
              {
                Iterator nextIterator;
                if (nodeName.equalsIgnoreCase("absorblist"))
                {
                  for (nextIterator = secondElement.elementIterator(); nextIterator.hasNext(); )
                  {
                    org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();

                    int chance = Integer.parseInt(nextElement.attributeValue("chance"));
                    int cursedChance = nextElement.attributeValue("cursed_chance") == null ? 0 : Integer.parseInt(nextElement.attributeValue("cursed_chance"));
                    int minLevel = Integer.parseInt(nextElement.attributeValue("min_level"));
                    int maxLevel = Integer.parseInt(nextElement.attributeValue("max_level"));
                    boolean skill = (nextElement.attributeValue("skill") != null) && (Boolean.parseBoolean(nextElement.attributeValue("skill")));
                    AbsorbInfo.AbsorbType absorbType = AbsorbInfo.AbsorbType.valueOf(nextElement.attributeValue("type"));

                    template.addAbsorbInfo(new AbsorbInfo(skill, absorbType, chance, cursedChance, minLevel, maxLevel));
                  }
                }
                else if (nodeName.equalsIgnoreCase("teleportlist"))
                {
                  for (sublistIterator = secondElement.elementIterator(); sublistIterator.hasNext(); )
                  {
                    org.dom4j.Element subListElement = (org.dom4j.Element)sublistIterator.next();
                    int id = Integer.parseInt(subListElement.attributeValue("id"));
                    List list = new ArrayList();
                    for (Iterator targetIterator = subListElement.elementIterator(); targetIterator.hasNext(); )
                    {
                      org.dom4j.Element targetElement = (org.dom4j.Element)targetIterator.next();
                      int itemId = Integer.parseInt(targetElement.attributeValue("item_id", "57"));
                      long price = Integer.parseInt(targetElement.attributeValue("price"));
                      int npcStringId = Integer.parseInt(targetElement.attributeValue("name"));
                      int castleId = Integer.parseInt(targetElement.attributeValue("castle_id", "0"));
                      TeleportLocation loc = new TeleportLocation(itemId, price, npcStringId, castleId);
                      loc.set(Location.parseLoc(targetElement.attributeValue("loc")));
                      list.add(loc);
                    }
                    template.addTeleportList(id, (TeleportLocation[])list.toArray(new TeleportLocation[list.size()]));
                  }
                }
              }
            }
          }
        }
      }
      Iterator sublistIterator;
      ((NpcHolder)getHolder()).addTemplate(template);
    }
  }

  private RewardData parseReward(org.dom4j.Element rewardElement)
  {
    int itemId = Integer.parseInt(rewardElement.attributeValue("item_id"));
    int min = Integer.parseInt(rewardElement.attributeValue("min"));
    int max = Integer.parseInt(rewardElement.attributeValue("max"));

    int chance = (int)(Double.parseDouble(rewardElement.attributeValue("chance")) * 10000.0D);

    RewardData data = new RewardData(itemId);
    if (data.getItem().isCommonItem())
    {
      data.setChance(chance * Config.RATE_DROP_COMMON_ITEMS);
    }
    else if (Config.RATE_DROP_ITEMS_BY_ID.containsKey(Integer.valueOf(itemId)))
    {
      data.setChance(chance * ((Double)Config.RATE_DROP_ITEMS_BY_ID.get(Integer.valueOf(itemId))).doubleValue());
    }
    else
    {
      data.setChance(chance);
    }

    data.setMinDrop(min);
    data.setMaxDrop(max);

    return data;
  }
}