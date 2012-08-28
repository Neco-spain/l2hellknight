package l2p.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.EnchantItemHolder;
import l2p.gameserver.templates.item.support.EnchantScroll;
import l2p.gameserver.templates.item.support.FailResultType;
import org.dom4j.Element;

public class EnchantItemParser extends AbstractFileParser<EnchantItemHolder>
{
  private static EnchantItemParser _instance = new EnchantItemParser();

  public static EnchantItemParser getInstance()
  {
    return _instance;
  }

  private EnchantItemParser()
  {
    super(EnchantItemHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/enchant_items.xml");
  }

  public String getDTDFileName()
  {
    return "enchant_items.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    int defaultMaxEnchant = 0;
    int defaultChance = 0;
    int defaultMagicChance = 0;
    boolean defaultVisualEffect = false;

    Element defaultElement = rootElement.element("default");
    if (defaultElement != null)
    {
      defaultMaxEnchant = Integer.parseInt(defaultElement.attributeValue("max_enchant"));
      defaultChance = Integer.parseInt(defaultElement.attributeValue("chance"));
      defaultMagicChance = Integer.parseInt(defaultElement.attributeValue("magic_chance"));
      defaultVisualEffect = Boolean.parseBoolean(defaultElement.attributeValue("visual_effect"));
    }

    for (Iterator iterator = rootElement.elementIterator("enchant_scroll"); iterator.hasNext(); )
    {
      Element enchantItemElement = (Element)iterator.next();
      int itemId = Integer.parseInt(enchantItemElement.attributeValue("id"));
      int chance = enchantItemElement.attributeValue("chance") == null ? defaultChance : Integer.parseInt(enchantItemElement.attributeValue("chance"));

      int magicChance = enchantItemElement.attributeValue("magic_chance") == null ? defaultMagicChance : Integer.parseInt(enchantItemElement.attributeValue("magic_chance"));
      int maxEnchant = enchantItemElement.attributeValue("max_enchant") == null ? defaultMaxEnchant : Integer.parseInt(enchantItemElement.attributeValue("max_enchant"));
      FailResultType resultType = FailResultType.valueOf(enchantItemElement.attributeValue("on_fail"));
      boolean visualEffect = enchantItemElement.attributeValue("visual_effect") == null ? defaultVisualEffect : Boolean.parseBoolean(enchantItemElement.attributeValue("visual_effect"));

      item = new EnchantScroll(itemId, chance, maxEnchant, resultType, visualEffect);
      ((EnchantItemHolder)getHolder()).addEnchantScroll(item);

      for (iterator2 = enchantItemElement.elementIterator(); iterator2.hasNext(); )
      {
        Element element2 = (Element)iterator2.next();
        if (element2.getName().equals("item_list"))
        {
          for (Element e : element2.elements()) {
            item.addItemId(Integer.parseInt(e.attributeValue("id")));
          }
        }
        else
          info("Not supported for now.2");
      }
    }
    EnchantScroll item;
    Iterator iterator2;
  }
}