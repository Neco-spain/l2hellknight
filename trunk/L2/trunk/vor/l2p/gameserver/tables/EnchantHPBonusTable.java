package l2p.gameserver.tables;

import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EnchantHPBonusTable
{
  private static Logger _log = LoggerFactory.getLogger(EnchantHPBonusTable.class);

  private final TIntObjectHashMap<Integer[]> _armorHPBonus = new TIntObjectHashMap();

  private int _onepieceFactor = 100;

  private static EnchantHPBonusTable _instance = new EnchantHPBonusTable();

  public static EnchantHPBonusTable getInstance()
  {
    if (_instance == null)
      _instance = new EnchantHPBonusTable();
    return _instance;
  }

  public void reload()
  {
    _instance = new EnchantHPBonusTable();
  }

  private EnchantHPBonusTable()
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      File file = new File(Config.DATAPACK_ROOT, "data/enchant_bonus.xml");
      Document doc = factory.newDocumentBuilder().parse(file);

      for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        if ("list".equalsIgnoreCase(n.getNodeName()))
          for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
          {
            NamedNodeMap attrs = d.getAttributes();

            if ("options".equalsIgnoreCase(d.getNodeName()))
            {
              Node att = attrs.getNamedItem("onepiece_factor");
              if (att == null)
              {
                _log.info("EnchantHPBonusTable: Missing onepiece_factor, skipping");
              }
              else
                _onepieceFactor = Integer.parseInt(att.getNodeValue());
            } else {
              if (!"enchant_bonus".equalsIgnoreCase(d.getNodeName()))
              {
                continue;
              }
              Node att = attrs.getNamedItem("grade");
              if (att == null)
              {
                _log.info("EnchantHPBonusTable: Missing grade, skipping");
              }
              else {
                Integer grade = Integer.valueOf(Integer.parseInt(att.getNodeValue()));

                att = attrs.getNamedItem("values");
                if (att == null)
                {
                  _log.info("EnchantHPBonusTable: Missing bonus id: " + grade + ", skipping");
                }
                else {
                  StringTokenizer st = new StringTokenizer(att.getNodeValue(), ",");
                  int tokenCount = st.countTokens();
                  Integer[] bonus = new Integer[tokenCount];
                  for (int i = 0; i < tokenCount; i++)
                  {
                    Integer value = Integer.decode(st.nextToken().trim());
                    if (value == null)
                    {
                      _log.info("EnchantHPBonusTable: Bad Hp value!! grade: " + grade + " token: " + i);
                      value = Integer.valueOf(0);
                    }
                    bonus[i] = value;
                  }
                  _armorHPBonus.put(grade.intValue(), bonus);
                }
              }
            }
          }
      _log.info("EnchantHPBonusTable: Loaded bonuses for " + _armorHPBonus.size() + " grades.");
    }
    catch (Exception e)
    {
      _log.warn("EnchantHPBonusTable: Lists could not be initialized.");
      e.printStackTrace();
    }
  }

  public final int getHPBonus(ItemInstance item)
  {
    if (item.getEnchantLevel() == 0) {
      return 0;
    }
    Integer[] values = (Integer[])_armorHPBonus.get(item.getTemplate().getCrystalType().externalOrdinal);

    if ((values == null) || (values.length == 0)) {
      return 0;
    }

    int buffer = Math.min(item.getEnchantLevel(), values.length);

    Player player = item.getPlayer();

    if (player != null)
    {
      int bonus = values[(buffer - 1)].intValue();
      if ((Config.OLY_ENCH_LIMIT_ENABLE) && (player.isInOlympiadMode()))
        bonus = values[(Math.min(buffer, Config.OLY_ENCHANT_LIMIT_ARMOR) - 1)].intValue();
      if (item.getTemplate().getBodyPart() == 32768)
        bonus = (int)(bonus * _onepieceFactor / 100.0D);
      return bonus;
    }

    int bonus = values[(buffer - 1)].intValue();
    if (item.getTemplate().getBodyPart() == 32768)
      bonus = (int)(bonus * _onepieceFactor / 100.0D);
    return bonus;
  }
}