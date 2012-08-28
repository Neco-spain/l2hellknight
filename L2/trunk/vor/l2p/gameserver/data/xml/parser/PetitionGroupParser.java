package l2p.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2p.commons.data.xml.AbstractFileParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.PetitionGroupHolder;
import l2p.gameserver.model.petition.PetitionMainGroup;
import l2p.gameserver.model.petition.PetitionSubGroup;
import l2p.gameserver.utils.Language;
import org.dom4j.Element;

public class PetitionGroupParser extends AbstractFileParser<PetitionGroupHolder>
{
  private static PetitionGroupParser _instance = new PetitionGroupParser();

  public static PetitionGroupParser getInstance()
  {
    return _instance;
  }

  private PetitionGroupParser()
  {
    super(PetitionGroupHolder.getInstance());
  }

  public File getXMLFile()
  {
    return new File(Config.DATAPACK_ROOT, "data/petition_group.xml");
  }

  public String getDTDFileName()
  {
    return "petition_group.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element groupElement = (Element)iterator.next();
      group = new PetitionMainGroup(Integer.parseInt(groupElement.attributeValue("id")));
      ((PetitionGroupHolder)getHolder()).addPetitionGroup(group);

      for (subIterator = groupElement.elementIterator(); subIterator.hasNext(); )
      {
        Element subElement = (Element)subIterator.next();
        if ("name".equals(subElement.getName())) {
          group.setName(Language.valueOf(subElement.attributeValue("lang")), subElement.getText());
        } else if ("description".equals(subElement.getName())) {
          group.setDescription(Language.valueOf(subElement.attributeValue("lang")), subElement.getText());
        } else if ("sub_group".equals(subElement.getName()))
        {
          subGroup = new PetitionSubGroup(Integer.parseInt(subElement.attributeValue("id")), subElement.attributeValue("handler"));
          group.addSubGroup(subGroup);
          for (sub2Iterator = subElement.elementIterator(); sub2Iterator.hasNext(); )
          {
            Element sub2Element = (Element)sub2Iterator.next();
            if ("name".equals(sub2Element.getName()))
              subGroup.setName(Language.valueOf(sub2Element.attributeValue("lang")), sub2Element.getText());
            else if ("description".equals(sub2Element.getName()))
              subGroup.setDescription(Language.valueOf(sub2Element.attributeValue("lang")), sub2Element.getText());
          }
        }
      }
    }
    PetitionMainGroup group;
    Iterator subIterator;
    PetitionSubGroup subGroup;
    Iterator sub2Iterator;
  }
}