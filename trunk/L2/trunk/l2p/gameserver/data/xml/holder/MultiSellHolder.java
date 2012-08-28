package l2p.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.MultiSellEntry;
import l2p.gameserver.model.base.MultiSellIngredient;
import l2p.gameserver.model.items.ItemAttributes;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.MultiSellList;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.utils.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class MultiSellHolder
{
  private static final Logger _log = LoggerFactory.getLogger(MultiSellHolder.class);

  private static MultiSellHolder _instance = new MultiSellHolder();
  private static final String NODE_PRODUCTION = "production";
  private static final String NODE_INGRIDIENT = "ingredient";
  private TIntObjectHashMap<MultiSellListContainer> entries = new TIntObjectHashMap();

  public static MultiSellHolder getInstance()
  {
    return _instance;
  }

  public MultiSellListContainer getList(int id)
  {
    return (MultiSellListContainer)entries.get(id);
  }

  public MultiSellHolder()
  {
    parseData();
  }

  public void reload()
  {
    parseData();
  }

  private void parseData()
  {
    entries.clear();
    parse();
  }

  private void hashFiles(String dirname, List<File> hash)
  {
    File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
    if (!dir.exists())
    {
      _log.info("Dir " + dir.getAbsolutePath() + " not exists");
      return;
    }
    File[] files = dir.listFiles();
    for (File f : files)
      if (f.getName().endsWith(".xml"))
        hash.add(f);
      else if ((f.isDirectory()) && (!f.getName().equals(".svn")))
        hashFiles(dirname + "/" + f.getName(), hash);
  }

  public void addMultiSellListContainer(int id, MultiSellListContainer list)
  {
    if (entries.containsKey(id)) {
      _log.warn("MultiSell redefined: " + id);
    }
    list.setListId(id);
    entries.put(id, list);
  }

  public MultiSellListContainer remove(String s)
  {
    return remove(new File(s));
  }

  public MultiSellListContainer remove(File f)
  {
    return remove(Integer.parseInt(f.getName().replaceAll(".xml", "")));
  }

  public MultiSellListContainer remove(int id)
  {
    return (MultiSellListContainer)entries.remove(id);
  }

  public void parseFile(File f)
  {
    int id = 0;
    try
    {
      id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
    }
    catch (Exception e)
    {
      _log.error("Error loading file " + f, e);
      return;
    }
    Document doc = null;
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setIgnoringComments(true);
      doc = factory.newDocumentBuilder().parse(f);
    }
    catch (Exception e)
    {
      _log.error("Error loading file " + f, e);
      return;
    }
    try
    {
      addMultiSellListContainer(id, parseDocument(doc, id));
    }
    catch (Exception e)
    {
      _log.error("Error in file " + f, e);
    }
  }

  private void parse()
  {
    List files = new ArrayList();
    hashFiles("multisell", files);
    for (File f : files)
      parseFile(f);
  }

  protected MultiSellListContainer parseDocument(Document doc, int id)
  {
    MultiSellListContainer list = new MultiSellListContainer();
    int entId = 1;

    for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
      if ("list".equalsIgnoreCase(n.getNodeName()))
        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
          if ("item".equalsIgnoreCase(d.getNodeName()))
          {
            MultiSellEntry e = parseEntry(d, id);
            if (e != null)
            {
              e.setEntryId(entId++);
              list.addEntry(e);
            }
          } else {
            if (!"config".equalsIgnoreCase(d.getNodeName()))
              continue;
            list.setShowAll(XMLUtil.getAttributeBooleanValue(d, "showall", true));
            list.setNoTax(XMLUtil.getAttributeBooleanValue(d, "notax", false));
            list.setKeepEnchant(XMLUtil.getAttributeBooleanValue(d, "keepenchanted", false));
            list.setNoKey(XMLUtil.getAttributeBooleanValue(d, "nokey", false));
          }
    }
    return list;
  }

  protected MultiSellEntry parseEntry(Node n, int multiSellId)
  {
    MultiSellEntry entry = new MultiSellEntry();

    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
      if ("ingredient".equalsIgnoreCase(d.getNodeName()))
      {
        int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
        long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
        MultiSellIngredient mi = new MultiSellIngredient(id, count);
        if (d.getAttributes().getNamedItem("enchant") != null)
          mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
        if (d.getAttributes().getNamedItem("mantainIngredient") != null) {
          mi.setMantainIngredient(Boolean.parseBoolean(d.getAttributes().getNamedItem("mantainIngredient").getNodeValue()));
        }
        if (d.getAttributes().getNamedItem("fireAttr") != null)
          mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("waterAttr") != null)
          mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("earthAttr") != null)
          mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("windAttr") != null)
          mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("holyAttr") != null)
          mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("unholyAttr") != null) {
          mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
        }
        entry.addIngredient(mi);
      } else {
        if (!"production".equalsIgnoreCase(d.getNodeName()))
          continue;
        int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
        long count = Long.parseLong(d.getAttributes().getNamedItem("count").getNodeValue());
        MultiSellIngredient mi = new MultiSellIngredient(id, count);
        if (d.getAttributes().getNamedItem("enchant") != null) {
          mi.setItemEnchant(Integer.parseInt(d.getAttributes().getNamedItem("enchant").getNodeValue()));
        }
        if (d.getAttributes().getNamedItem("fireAttr") != null)
          mi.getItemAttributes().setFire(Integer.parseInt(d.getAttributes().getNamedItem("fireAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("waterAttr") != null)
          mi.getItemAttributes().setWater(Integer.parseInt(d.getAttributes().getNamedItem("waterAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("earthAttr") != null)
          mi.getItemAttributes().setEarth(Integer.parseInt(d.getAttributes().getNamedItem("earthAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("windAttr") != null)
          mi.getItemAttributes().setWind(Integer.parseInt(d.getAttributes().getNamedItem("windAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("holyAttr") != null)
          mi.getItemAttributes().setHoly(Integer.parseInt(d.getAttributes().getNamedItem("holyAttr").getNodeValue()));
        if (d.getAttributes().getNamedItem("unholyAttr") != null) {
          mi.getItemAttributes().setUnholy(Integer.parseInt(d.getAttributes().getNamedItem("unholyAttr").getNodeValue()));
        }
        if ((!Config.ALT_ALLOW_SHADOW_WEAPONS) && (id > 0))
        {
          ItemTemplate item = ItemHolder.getInstance().getTemplate(id);
          if ((item != null) && (item.isShadowItem()) && (item.isWeapon()) && (!Config.ALT_ALLOW_SHADOW_WEAPONS)) {
            return null;
          }
        }
        entry.addProduct(mi);
      }
    }
    if ((entry.getIngredients().isEmpty()) || (entry.getProduction().isEmpty()))
    {
      _log.warn("MultiSell [" + multiSellId + "] is empty!");
      return null;
    }

    if ((entry.getIngredients().size() == 1) && (entry.getProduction().size() == 1) && (((MultiSellIngredient)entry.getIngredients().get(0)).getItemId() == 57))
    {
      ItemTemplate item = ItemHolder.getInstance().getTemplate(((MultiSellIngredient)entry.getProduction().get(0)).getItemId());
      if (item == null)
      {
        _log.warn("MultiSell [" + multiSellId + "] Production [" + ((MultiSellIngredient)entry.getProduction().get(0)).getItemId() + "] not found!");
        return null;
      }
      if (((multiSellId < 70000) || (multiSellId > 70010)) && 
        (item.getReferencePrice() > ((MultiSellIngredient)entry.getIngredients().get(0)).getItemCount())) {
        _log.warn("MultiSell [" + multiSellId + "] Production '" + item.getName() + "' [" + ((MultiSellIngredient)entry.getProduction().get(0)).getItemId() + "] price is lower than referenced | " + item.getReferencePrice() + " > " + ((MultiSellIngredient)entry.getIngredients().get(0)).getItemCount());
      }
    }
    return entry;
  }

  private static long[] parseItemIdAndCount(String s)
  {
    if ((s == null) || (s.isEmpty()))
      return null;
    String[] a = s.split(":");
    try
    {
      long id = Integer.parseInt(a[0]);
      long count = a.length > 1 ? Long.parseLong(a[1]) : 1L;
      return new long[] { id, count };
    }
    catch (Exception e)
    {
      _log.error("", e);
    }return null;
  }

  public static MultiSellEntry parseEntryFromStr(String s)
  {
    if ((s == null) || (s.isEmpty())) {
      return null;
    }
    String[] a = s.split("->");
    if (a.length != 2)
      return null;
    long[] ingredient;
    long[] production;
    if (((ingredient = parseItemIdAndCount(a[0])) == null) || ((production = parseItemIdAndCount(a[1])) == null))
      return null;
    long[] production;
    MultiSellEntry entry = new MultiSellEntry();
    entry.addIngredient(new MultiSellIngredient((int)ingredient[0], ingredient[1]));
    entry.addProduct(new MultiSellIngredient((int)production[0], production[1]));
    return entry;
  }

  public void SeparateAndSend(int listId, Player player, double taxRate)
  {
    for (int i : Config.ALT_DISABLED_MULTISELL) {
      if (i != listId)
        continue;
      player.sendMessage(new CustomMessage("common.Disabled", player, new Object[0]));
      return;
    }

    MultiSellListContainer list = getList(listId);
    if (list == null)
    {
      player.sendMessage(new CustomMessage("common.Disabled", player, new Object[0]));
      return;
    }

    SeparateAndSend(list, player, taxRate);
  }

  public void SeparateAndSend(MultiSellListContainer list, Player player, double taxRate)
  {
    list = generateMultiSell(list, player, taxRate);

    MultiSellListContainer temp = new MultiSellListContainer();
    int page = 1;

    temp.setListId(list.getListId());

    player.setMultisell(list);

    for (MultiSellEntry e : list.getEntries())
    {
      if (temp.getEntries().size() == Config.MULTISELL_SIZE)
      {
        player.sendPacket(new MultiSellList(temp, page, 0));
        page++;
        temp = new MultiSellListContainer();
        temp.setListId(list.getListId());
      }
      temp.addEntry(e);
    }

    player.sendPacket(new MultiSellList(temp, page, 1));
  }

  private MultiSellListContainer generateMultiSell(MultiSellListContainer container, Player player, double taxRate)
  {
    MultiSellListContainer list = new MultiSellListContainer();
    list.setListId(container.getListId());

    boolean enchant = container.isKeepEnchant();
    boolean notax = container.isNoTax();
    boolean showall = container.isShowAll();
    boolean nokey = container.isNoKey();

    list.setShowAll(showall);
    list.setKeepEnchant(enchant);
    list.setNoTax(notax);
    list.setNoKey(nokey);

    ItemInstance[] items = player.getInventory().getItems();
    for (MultiSellEntry origEntry : container.getEntries())
    {
      ent = origEntry.clone();

      if ((!notax) && (taxRate > 0.0D))
      {
        double tax = 0.0D;
        long adena = 0L;
        List ingridients = new ArrayList(ent.getIngredients().size() + 1);
        for (MultiSellIngredient i : ent.getIngredients())
        {
          if (i.getItemId() == 57)
          {
            adena += i.getItemCount();
            tax += i.getItemCount() * taxRate;
            continue;
          }
          ingridients.add(i);
          if (i.getItemId() == -200)
          {
            tax += i.getItemCount() / 120L * 1000L * taxRate * 100.0D;
          }if (i.getItemId() < 1) {
            continue;
          }
          ItemTemplate item = ItemHolder.getInstance().getTemplate(i.getItemId());
          if (item.isStackable()) {
            tax += item.getReferencePrice() * i.getItemCount() * taxRate;
          }
        }
        adena = Math.round(adena + tax);
        if (adena > 0L) {
          ingridients.add(new MultiSellIngredient(57, adena));
        }
        ent.setTax(Math.round(tax));

        ent.getIngredients().clear();
        ent.getIngredients().addAll(ingridients);
      }
      else {
        ingridients = ent.getIngredients();
      }

      if (showall) {
        list.entries.add(ent);
      }
      else {
        itms = new ArrayList();

        for (MultiSellIngredient ingredient : ingridients)
        {
          ItemTemplate template = ingredient.getItemId() <= 0 ? null : ItemHolder.getInstance().getTemplate(ingredient.getItemId());
          if ((ingredient.getItemId() <= 0) || (nokey) || (template.isEquipment()))
          {
            if (ingredient.getItemId() == 12374)
            {
              continue;
            }
            if (ingredient.getItemId() == -200)
            {
              if ((!itms.contains(Integer.valueOf(ingredient.getItemId()))) && (player.getClan() != null) && (player.getClan().getReputationScore() >= ingredient.getItemCount())) {
                itms.add(Integer.valueOf(ingredient.getItemId())); continue;
              }
            }
            if (ingredient.getItemId() == -100)
            {
              if ((!itms.contains(Integer.valueOf(ingredient.getItemId()))) && (player.getPcBangPoints() >= ingredient.getItemCount())) {
                itms.add(Integer.valueOf(ingredient.getItemId())); continue;
              }
            }
            if (ingredient.getItemId() == -300)
            {
              if ((!itms.contains(Integer.valueOf(ingredient.getItemId()))) && (player.getFame() >= ingredient.getItemCount())) {
                itms.add(Integer.valueOf(ingredient.getItemId())); continue;
              }
            }

            for (ItemInstance item : items) {
              if ((item.getItemId() != ingredient.getItemId()) || (!item.canBeExchanged(player))) {
                continue;
              }
              if (itms.contains(Long.valueOf(ingredient.getItemId()))) {
                continue;
              }
              if (item.getEnchantLevel() < ingredient.getItemEnchant()) {
                continue;
              }
              if ((item.isStackable()) && (item.getCount() < ingredient.getItemCount())) {
                break;
              }
              itms.add(Integer.valueOf(enchant ? ingredient.getItemId() + ingredient.getItemEnchant() * 100000 : ingredient.getItemId()));
              MultiSellEntry possibleEntry = new MultiSellEntry(enchant ? ent.getEntryId() + item.getEnchantLevel() * 100000 : ent.getEntryId());

              for (MultiSellIngredient p : ent.getProduction())
              {
                if ((enchant) && (template.canBeEnchanted(true)))
                {
                  p.setItemEnchant(item.getEnchantLevel());
                  p.setItemAttributes(item.getAttributes().clone());
                }
                possibleEntry.addProduct(p);
              }

              for (MultiSellIngredient ig : ingridients)
              {
                if ((enchant) && (ig.getItemId() > 0) && (ItemHolder.getInstance().getTemplate(ig.getItemId()).canBeEnchanted(true)))
                {
                  ig.setItemEnchant(item.getEnchantLevel());
                  ig.setItemAttributes(item.getAttributes().clone());
                }
                possibleEntry.addIngredient(ig);
              }

              list.entries.add(possibleEntry);
              break;
            }
          }
        }
      }
    }
    MultiSellEntry ent;
    List ingridients;
    List itms;
    return list;
  }

  public static class MultiSellListContainer
  {
    private int _listId;
    private boolean _showall = true;
    private boolean keep_enchanted = false;
    private boolean is_dutyfree = false;
    private boolean nokey = false;
    private List<MultiSellEntry> entries = new ArrayList();

    public void setListId(int listId)
    {
      _listId = listId;
    }

    public int getListId()
    {
      return _listId;
    }

    public void setShowAll(boolean bool)
    {
      _showall = bool;
    }

    public boolean isShowAll()
    {
      return _showall;
    }

    public void setNoTax(boolean bool)
    {
      is_dutyfree = bool;
    }

    public boolean isNoTax()
    {
      return is_dutyfree;
    }

    public void setNoKey(boolean bool)
    {
      nokey = bool;
    }

    public boolean isNoKey()
    {
      return nokey;
    }

    public void setKeepEnchant(boolean bool)
    {
      keep_enchanted = bool;
    }

    public boolean isKeepEnchant()
    {
      return keep_enchanted;
    }

    public void addEntry(MultiSellEntry e)
    {
      entries.add(e);
    }

    public List<MultiSellEntry> getEntries()
    {
      return entries;
    }

    public boolean isEmpty()
    {
      return entries.isEmpty();
    }
  }
}