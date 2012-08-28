package l2m.gameserver.data.xml.parser;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.commons.data.xml.AbstractDirParser;
import l2m.gameserver.Config;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.TeleportLocation;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.entity.residence.Residence;
import l2m.gameserver.model.entity.residence.ResidenceFunction;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.support.MerchantGuard;
import l2m.gameserver.utils.Location;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public final class ResidenceParser extends AbstractDirParser<ResidenceHolder>
{
  private static ResidenceParser _instance = new ResidenceParser();

  public static ResidenceParser getInstance()
  {
    return _instance;
  }

  private ResidenceParser()
  {
    super(ResidenceHolder.getInstance());
  }

  public File getXMLDir()
  {
    return new File(Config.DATAPACK_ROOT, "data/residences/");
  }

  public boolean isIgnored(File f)
  {
    return false;
  }

  public String getDTDFileName()
  {
    return "residence.dtd";
  }

  protected void readData(Element rootElement)
    throws Exception
  {
    String impl = rootElement.attributeValue("impl");
    Class clazz = null;

    StatsSet set = new StatsSet();
    for (Iterator iterator = rootElement.attributeIterator(); iterator.hasNext(); )
    {
      Attribute element = (Attribute)iterator.next();
      set.set(element.getName(), element.getValue());
    }

    Residence residence = null;
    try
    {
      clazz = Class.forName("l2p.gameserver.model.entity.residence." + impl);
      Constructor constructor = clazz.getConstructor(new Class[] { StatsSet.class });
      residence = (Residence)constructor.newInstance(new Object[] { set });
      ((ResidenceHolder)getHolder()).addResidence(residence);
    }
    catch (Exception e)
    {
      error("fail to init: " + getCurrentFileName(), e);
      return;
    }

    for (Iterator iterator = rootElement.elementIterator(); iterator.hasNext(); )
    {
      Element element = (Element)iterator.next();
      String nodeName = element.getName();
      int level = element.attributeValue("level") == null ? 0 : Integer.valueOf(element.attributeValue("level")).intValue();
      int lease = (int)((element.attributeValue("lease") == null ? 0 : Integer.valueOf(element.attributeValue("lease")).intValue()) * Config.RESIDENCE_LEASE_FUNC_MULTIPLIER);
      int npcId = element.attributeValue("npcId") == null ? 0 : Integer.valueOf(element.attributeValue("npcId")).intValue();
      int listId = element.attributeValue("listId") == null ? 0 : Integer.valueOf(element.attributeValue("listId")).intValue();

      ResidenceFunction function = null;
      Iterator subElementIterator;
      if (nodeName.equalsIgnoreCase("teleport"))
      {
        function = checkAndGetFunction(residence, 1);
        List targets = new ArrayList();
        for (Iterator it2 = element.elementIterator(); it2.hasNext(); )
        {
          Element teleportElement = (Element)it2.next();
          if ("target".equalsIgnoreCase(teleportElement.getName()))
          {
            int npcStringId = Integer.parseInt(teleportElement.attributeValue("name"));
            long price = Long.parseLong(teleportElement.attributeValue("price"));
            int itemId = teleportElement.attributeValue("item") == null ? 57 : Integer.parseInt(teleportElement.attributeValue("item"));
            TeleportLocation loc = new TeleportLocation(itemId, price, npcStringId, 0);
            loc.set(Location.parseLoc(teleportElement.attributeValue("loc")));
            targets.add(loc);
          }
        }
        function.addTeleports(level, (TeleportLocation[])targets.toArray(new TeleportLocation[targets.size()]));
      }
      else if (nodeName.equalsIgnoreCase("support"))
      {
        if ((level > 9) && (!Config.ALT_CH_ALLOW_1H_BUFFS))
          continue;
        function = checkAndGetFunction(residence, 6);
        function.addBuffs(level);
      }
      else if (nodeName.equalsIgnoreCase("item_create"))
      {
        function = checkAndGetFunction(residence, 2);
        function.addBuylist(level, new int[] { npcId, listId });
      }
      else if (nodeName.equalsIgnoreCase("curtain")) {
        function = checkAndGetFunction(residence, 7);
      } else if (nodeName.equalsIgnoreCase("platform")) {
        function = checkAndGetFunction(residence, 8);
      } else if (nodeName.equalsIgnoreCase("restore_exp")) {
        function = checkAndGetFunction(residence, 5);
      } else if (nodeName.equalsIgnoreCase("restore_hp")) {
        function = checkAndGetFunction(residence, 3);
      } else if (nodeName.equalsIgnoreCase("restore_mp")) {
        function = checkAndGetFunction(residence, 4);
      }
      else
      {
        Iterator nextIterator;
        if (nodeName.equalsIgnoreCase("skills"))
        {
          for (nextIterator = element.elementIterator(); nextIterator.hasNext(); )
          {
            Element nextElement = (Element)nextIterator.next();
            int id2 = Integer.parseInt(nextElement.attributeValue("id"));
            int level2 = Integer.parseInt(nextElement.attributeValue("level"));

            Skill skill = SkillTable.getInstance().getInfo(id2, level2);
            if (skill != null)
              residence.addSkill(skill);
          }
        }
        else
        {
          Iterator banishPointsIterator;
          if (nodeName.equalsIgnoreCase("banish_points"))
          {
            for (banishPointsIterator = element.elementIterator(); banishPointsIterator.hasNext(); )
            {
              Location loc = Location.parse((Element)banishPointsIterator.next());

              residence.addBanishPoint(loc);
            }
          }
          else
          {
            Iterator ownerRestartPointsIterator;
            if (nodeName.equalsIgnoreCase("owner_restart_points"))
            {
              for (ownerRestartPointsIterator = element.elementIterator(); ownerRestartPointsIterator.hasNext(); )
              {
                Location loc = Location.parse((Element)ownerRestartPointsIterator.next());

                residence.addOwnerRestartPoint(loc);
              }
            }
            else
            {
              Iterator otherRestartPointsIterator;
              if (nodeName.equalsIgnoreCase("other_restart_points"))
              {
                for (otherRestartPointsIterator = element.elementIterator(); otherRestartPointsIterator.hasNext(); )
                {
                  Location loc = Location.parse((Element)otherRestartPointsIterator.next());

                  residence.addOtherRestartPoint(loc);
                }
              }
              else
              {
                Iterator chaosRestartPointsIterator;
                if (nodeName.equalsIgnoreCase("chaos_restart_points"))
                {
                  for (chaosRestartPointsIterator = element.elementIterator(); chaosRestartPointsIterator.hasNext(); )
                  {
                    Location loc = Location.parse((Element)chaosRestartPointsIterator.next());

                    residence.addChaosRestartPoint(loc);
                  }
                }
                else
                {
                  Iterator subElementIterator;
                  if (nodeName.equalsIgnoreCase("related_fortresses"))
                  {
                    for (subElementIterator = element.elementIterator(); subElementIterator.hasNext(); )
                    {
                      Element subElement = (Element)subElementIterator.next();
                      if (subElement.getName().equalsIgnoreCase("domain"))
                        ((Castle)residence).addRelatedFortress(0, Integer.parseInt(subElement.attributeValue("fortress")));
                      else if (subElement.getName().equalsIgnoreCase("boundary"))
                        ((Castle)residence).addRelatedFortress(1, Integer.parseInt(subElement.attributeValue("fortress")));
                    }
                  }
                  else if (nodeName.equalsIgnoreCase("merchant_guards"))
                  {
                    for (subElementIterator = element.elementIterator(); subElementIterator.hasNext(); )
                    {
                      Element subElement = (Element)subElementIterator.next();

                      int itemId = Integer.parseInt(subElement.attributeValue("item_id"));
                      int npcId2 = Integer.parseInt(subElement.attributeValue("npc_id"));
                      int maxGuard = Integer.parseInt(subElement.attributeValue("max"));
                      IntSet intSet = new HashIntSet(3);
                      String[] ssq = subElement.attributeValue("ssq").split(";");
                      for (String q : ssq)
                      {
                        if (q.equalsIgnoreCase("cabal_null"))
                          intSet.add(0);
                        else if (q.equalsIgnoreCase("cabal_dusk"))
                          intSet.add(1);
                        else if (q.equalsIgnoreCase("cabal_dawn"))
                          intSet.add(2);
                        else {
                          error("Unknown ssq type: " + q + "; file: " + getCurrentFileName());
                        }
                      }
                      ((Castle)residence).addMerchantGuard(new MerchantGuard(itemId, npcId2, maxGuard, intSet));
                    }
                  }
                }
              }
            }
          }
        }
      }
      if (function != null)
        function.addLease(level, lease);
    }
  }

  private ResidenceFunction checkAndGetFunction(Residence residence, int type)
  {
    ResidenceFunction function = residence.getFunction(type);
    if (function == null)
    {
      function = new ResidenceFunction(residence.getId(), type);
      residence.addFunction(function);
    }
    return function;
  }
}