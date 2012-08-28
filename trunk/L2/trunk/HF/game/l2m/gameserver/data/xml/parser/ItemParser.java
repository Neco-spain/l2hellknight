package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.ItemHolder;
import l2m.gameserver.data.xml.holder.OptionDataHolder;
import l2m.gameserver.model.Skill;
import l2m.gameserver.skills.conditions.Condition;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.OptionDataTemplate;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.ArmorTemplate;
import l2m.gameserver.templates.item.Bodypart;
import l2m.gameserver.templates.item.EtcItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.templates.item.ItemTemplate.ItemClass;
import l2m.gameserver.templates.item.WeaponTemplate;

public final class ItemParser extends StatParser<ItemHolder>
{
  private static final ItemParser _instance = new ItemParser();

  public static ItemParser getInstance()
  {
    return _instance;
  }

  protected ItemParser()
  {
    super(ItemHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/items/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "item.dtd";
  }

  protected void readData(org.dom4j.Element rootElement)
    throws Exception
  {
    for (Iterator itemIterator = rootElement.elementIterator(); itemIterator.hasNext(); )
    {
      org.dom4j.Element itemElement = (org.dom4j.Element)itemIterator.next();
      StatsSet set = new StatsSet();
      set.set("item_id", itemElement.attributeValue("id"));
      set.set("name", itemElement.attributeValue("name"));
      set.set("add_name", itemElement.attributeValue("add_name", ""));

      int slot = 0;
      for (Iterator subIterator = itemElement.elementIterator(); subIterator.hasNext(); )
      {
        org.dom4j.Element subElement = (org.dom4j.Element)subIterator.next();
        String subName = subElement.getName();
        if (subName.equalsIgnoreCase("set"))
        {
          set.set(subElement.attributeValue("name"), subElement.attributeValue("value"));
        }
        else if (subName.equalsIgnoreCase("equip"))
        {
          for (slotIterator = subElement.elementIterator(); slotIterator.hasNext(); )
          {
            org.dom4j.Element slotElement = (org.dom4j.Element)slotIterator.next();
            Bodypart bodypart = Bodypart.valueOf(slotElement.attributeValue("id"));
            if (bodypart.getReal() != null)
              slot = bodypart.mask();
            else
              slot |= bodypart.mask();
          }
        }
      }
      Iterator slotIterator;
      set.set("bodypart", slot);

      ItemTemplate template = null;
      try
      {
        if (itemElement.getName().equalsIgnoreCase("weapon"))
        {
          if (!set.containsKey("class"))
          {
            if ((slot & 0x100) > 0)
              set.set("class", ItemTemplate.ItemClass.ARMOR);
            else
              set.set("class", ItemTemplate.ItemClass.WEAPON);
          }
          template = new WeaponTemplate(set);
        }
        else if (itemElement.getName().equalsIgnoreCase("armor"))
        {
          if (!set.containsKey("class"))
          {
            if ((slot & 0xBF40) > 0)
              set.set("class", ItemTemplate.ItemClass.ARMOR);
            else if ((slot & 0x3E) > 0)
              set.set("class", ItemTemplate.ItemClass.JEWELRY);
            else
              set.set("class", ItemTemplate.ItemClass.ACCESSORY);
          }
          template = new ArmorTemplate(set);
        }
        else {
          template = new EtcItemTemplate(set);
        }

      }
      catch (Exception e)
      {
        warn("Fail create item: " + set.get("item_id"), e);
      }continue;

      for (Iterator subIterator = itemElement.elementIterator(); subIterator.hasNext(); )
      {
        org.dom4j.Element subElement = (org.dom4j.Element)subIterator.next();
        String subName = subElement.getName();
        if (subName.equalsIgnoreCase("for"))
        {
          parseFor(subElement, template);
        }
        else if (subName.equalsIgnoreCase("triggers"))
        {
          parseTriggers(subElement, template);
        }
        else
        {
          Iterator nextIterator;
          if (subName.equalsIgnoreCase("skills"))
          {
            for (nextIterator = subElement.elementIterator(); nextIterator.hasNext(); )
            {
              org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();
              int id = Integer.parseInt(nextElement.attributeValue("id"));
              int level = Integer.parseInt(nextElement.attributeValue("level"));

              Skill skill = SkillTable.getInstance().getInfo(id, level);

              if (skill != null)
                template.attachSkill(skill);
              else
                info("Skill not found(" + id + "," + level + ") for item:" + set.getObject("item_id") + "; file:" + getCurrentFileName());
            }
          }
          else if (subName.equalsIgnoreCase("enchant4_skill"))
          {
            int id = Integer.parseInt(subElement.attributeValue("id"));
            int level = Integer.parseInt(subElement.attributeValue("level"));

            Skill skill = SkillTable.getInstance().getInfo(id, level);
            if (skill != null)
              template.setEnchant4Skill(skill);
          }
          else if (subName.equalsIgnoreCase("cond"))
          {
            Condition condition = parseFirstCond(subElement);
            if (condition != null)
            {
              int msgId = parseNumber(subElement.attributeValue("msgId")).intValue();
              condition.setSystemMsg(msgId);

              template.setCondition(condition);
            }
          }
          else if (subName.equalsIgnoreCase("attributes"))
          {
            int[] attributes = new int[6];
            for (Iterator nextIterator = subElement.elementIterator(); nextIterator.hasNext(); )
            {
              org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();

              if (nextElement.getName().equalsIgnoreCase("attribute"))
              {
                l2p.gameserver.model.base.Element element = l2p.gameserver.model.base.Element.getElementByName(nextElement.attributeValue("element"));
                attributes[element.getId()] = Integer.parseInt(nextElement.attributeValue("value"));
              }
            }
            template.setBaseAtributeElements(attributes);
          }
          else if (subName.equalsIgnoreCase("enchant_options"))
          {
            for (nextIterator = subElement.elementIterator(); nextIterator.hasNext(); )
            {
              org.dom4j.Element nextElement = (org.dom4j.Element)nextIterator.next();

              if (nextElement.getName().equalsIgnoreCase("level"))
              {
                int val = Integer.parseInt(nextElement.attributeValue("val"));

                int i = 0;
                int[] options = new int[3];
                for (org.dom4j.Element optionElement : nextElement.elements())
                {
                  OptionDataTemplate optionData = OptionDataHolder.getInstance().getTemplate(Integer.parseInt(optionElement.attributeValue("id")));
                  if (optionData == null)
                  {
                    error("Not found option_data for id: " + optionElement.attributeValue("id") + "; item_id: " + set.get("item_id"));
                    continue;
                  }
                  options[(i++)] = optionData.getId();
                }
                template.addEnchantOptions(val, options);
              }
            }
          }
        }
      }
      Iterator nextIterator;
      ((ItemHolder)getHolder()).addItem(template);
    }
  }

  protected Object getTableValue(String name)
  {
    return null;
  }
}