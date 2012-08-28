package net.sf.l2j.gameserver.model;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MultiSellList;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.util.log.AbstractLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class L2Multisell
{
  private static Logger _log = AbstractLogger.getLogger(L2Multisell.class.getName());
  private Map<Integer, MultiSellListContainer> _entries = new ConcurrentHashMap();
  private static L2Multisell _instance = new L2Multisell();

  public MultiSellListContainer getList(int id) {
    return (MultiSellListContainer)_entries.get(Integer.valueOf(id));
  }

  public L2Multisell() {
    parseData();
  }

  public void reload() {
    parseData();
  }

  public static L2Multisell getInstance() {
    return _instance;
  }

  private void parseData() {
    _entries.clear();
    parse();
  }

  private MultiSellListContainer generateMultiSell(int listId, boolean inventoryOnly, L2PcInstance player, double taxRate)
  {
    if (player == null) {
      return null;
    }

    MultiSellListContainer listTemplate = getInstance().getList(listId);
    MultiSellListContainer list = new MultiSellListContainer();
    if (listTemplate == null) {
      return list;
    }

    list.setListId(listId);

    if (inventoryOnly)
    {
      L2ItemInstance[] items;
      L2ItemInstance[] items;
      if (listTemplate.saveEnchantment())
        items = player.getInventory().getUniqueItemsByEnchantLevel(false, false, false);
      else
        items = player.getInventory().getUniqueItems(false, false, false);
      L2ItemInstance item;
      int enchantLevel;
      for (item : items)
      {
        if ((!item.isWear()) && (((item.getItem() instanceof L2Armor)) || ((item.getItem() instanceof L2Weapon)))) {
          enchantLevel = listTemplate.saveEnchantment() ? item.getEnchantLevel() : 0;

          for (MultiSellEntry ent : listTemplate.getEntries()) {
            boolean doInclude = false;

            for (MultiSellIngredient ing : ent.getIngredients()) {
              if (item.getItemId() == ing.getItemId()) {
                doInclude = true;
                break;
              }

            }

            if (doInclude) {
              list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.saveEnchantment(), enchantLevel, taxRate));
            }
          }
        }
      }

    }
    else
    {
      for (MultiSellEntry ent : listTemplate.getEntries()) {
        list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0, taxRate));
      }
    }

    return list;
  }

  private MultiSellEntry prepareEntry(MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel, double taxRate)
  {
    MultiSellEntry newEntry = new MultiSellEntry();
    newEntry.setEntryId(templateEntry.getEntryId() * 100000 + enchantLevel);
    int adenaAmount = 0;

    ItemTable it = ItemTable.getInstance();
    for (MultiSellIngredient ing : templateEntry.getIngredients())
    {
      MultiSellIngredient newIngredient = new MultiSellIngredient(ing);

      if ((ing.getItemId() == 57) && (ing.isTaxIngredient())) {
        if (applyTaxes) {
          adenaAmount += (int)Math.round(ing.getItemCount() * taxRate); continue;
        }
      }
      if (ing.getItemId() == 57)
      {
        adenaAmount += ing.getItemCount();
        continue;
      }

      if ((maintainEnchantment) && (newIngredient.getItemId() > 0)) {
        L2Item tempItem = it.createDummyItem(ing.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          newIngredient.setEnchantmentLevel(enchantLevel);
        }

      }

      newEntry.addIngredient(newIngredient);
    }

    if (adenaAmount > 0) {
      newEntry.addIngredient(new MultiSellIngredient(57, adenaAmount, 0, false, false));
    }

    for (MultiSellIngredient ing : templateEntry.getProducts())
    {
      MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
      if (maintainEnchantment)
      {
        L2Item tempItem = it.createDummyItem(ing.getItemId()).getItem();
        if (((tempItem instanceof L2Armor)) || ((tempItem instanceof L2Weapon))) {
          newIngredient.setEnchantmentLevel(enchantLevel);
        }
      }
      newEntry.addProduct(newIngredient);
    }
    return newEntry;
  }

  public void SeparateAndSend(int listId, L2PcInstance player, boolean inventoryOnly, double taxRate) {
    MultiSellListContainer list = generateMultiSell(listId, inventoryOnly, player, taxRate);
    MultiSellListContainer temp = new MultiSellListContainer();
    int page = 1;
    temp.setListId(list.getListId());
    for (MultiSellEntry e : list.getEntries()) {
      if (temp.getEntries().size() == 40) {
        player.sendPacket(new MultiSellList(temp, page, 0, true));
        page++;
        temp = new MultiSellListContainer();
        temp.setListId(list.getListId());
      }
      temp.addEntry(e);
    }
    player.sendPacket(new MultiSellList(temp, page, 1, true));
  }

  private void hashFiles(String dirname, List<File> hash)
  {
    File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
    if (!dir.exists()) {
      _log.config("Dir " + dir.getAbsolutePath() + " not exists");
      return;
    }
    File[] files = dir.listFiles();
    for (File f : files)
      if (f.getName().endsWith(".xml"))
        hash.add(f);
  }

  private void parse()
  {
    Document doc = null;
    int id = 0;
    List files = new FastList();
    hashFiles("multisell", files);

    for (File f : files) {
      id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
      try
      {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        doc = factory.newDocumentBuilder().parse(f);
      } catch (Exception e) {
        _log.log(Level.SEVERE, "Error loading file " + f, e);
      }
      try {
        MultiSellListContainer list = parseDocument(doc, id);
        list.setListId(id);
        _entries.put(Integer.valueOf(id), list);
      } catch (Exception e) {
        _log.log(Level.SEVERE, "Error in file " + f, e);
      }
    }
  }

  protected MultiSellListContainer parseDocument(Document doc, int id) {
    MultiSellListContainer list = new MultiSellListContainer();

    for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
      if ("list".equalsIgnoreCase(n.getNodeName()))
      {
        Node attribute = n.getAttributes().getNamedItem("applyTaxes");
        if (attribute == null)
          list.setApplyTaxes(false);
        else {
          list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
        }

        attribute = n.getAttributes().getNamedItem("maintainEnchantment");
        if (attribute == null)
          list.setMaintainEnchantment(false);
        else {
          list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
        }

        attribute = n.getAttributes().getNamedItem("saveEnchantment");
        if (attribute == null)
          list.setSaveEnchantment(false);
        else {
          list.setSaveEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));
        }

        attribute = n.getAttributes().getNamedItem("npcId");
        if (attribute == null) {
          list.addNpc(0);
        } else {
          String[] npcList = attribute.getNodeValue().split(",");
          for (String npcId : npcList) {
            if (npcId.equals(""))
            {
              continue;
            }
            list.addNpc(Integer.parseInt(npcId));
          }
        }

        for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
          if ("item".equalsIgnoreCase(d.getNodeName())) {
            MultiSellEntry e = parseEntry(d, id);
            if (e != null)
              list.addEntry(e);
          }
        }
      }
      else if ("item".equalsIgnoreCase(n.getNodeName())) {
        MultiSellEntry e = parseEntry(n, id);
        if (e != null) {
          list.addEntry(e);
        }
      }

    }

    Integer ench = (Integer)Config.MULT_ENCHS.get(Integer.valueOf(id));
    if (ench != null) {
      list.setEnchant(ench.intValue());
    }

    Integer ticket = (Integer)Config.MULTVIP_CARDS.get(Integer.valueOf(id));
    if (ticket != null) {
      list.setTicketId(ticket.intValue());
    }

    return list;
  }

  protected MultiSellEntry parseEntry(Node n, int listId) {
    int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());

    Node first = n.getFirstChild();
    MultiSellEntry entry = new MultiSellEntry();
    ItemTable itemTable = ItemTable.getInstance();

    for (n = first; n != null; n = n.getNextSibling()) {
      if ("ingredient".equalsIgnoreCase(n.getNodeName())) {
        int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        if ((id < 65300) && (itemTable.getTemplate(id) == null)) {
          _log.warning("L2Multisell [WARNING], list " + listId + ": ingredient itemID " + id + " not known.");
          return null;
        }

        int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
        boolean isTaxIngredient = false; boolean mantainIngredient = false;

        Node attribute = n.getAttributes().getNamedItem("isTaxIngredient");

        if (attribute != null) {
          isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());
        }

        attribute = n.getAttributes().getNamedItem("mantainIngredient");

        if (attribute != null) {
          mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());
        }

        entry.addIngredient(new MultiSellIngredient(id, count, isTaxIngredient, mantainIngredient));
      } else if ("production".equalsIgnoreCase(n.getNodeName())) {
        int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        if ((id < 65300) && (itemTable.getTemplate(id) == null)) {
          _log.warning("L2Multisell [WARNING], list " + listId + ": production itemID " + id + " not known.");
          return null;
        }
        int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
        entry.addProduct(new MultiSellIngredient(id, count, false, false));
      }
    }

    entry.setEntryId(entryId);

    return entry;
  }

  public static class MultiSellListContainer
  {
    private int _listId;
    private boolean _applyTaxes = false;
    private boolean _maintainEnchantment = false;
    private boolean _saveEnchantment = false;
    private int _enchLvl = Config.MULT_ENCH;
    private int _ticket = 0;
    private ConcurrentLinkedQueue<Integer> _npcList = new ConcurrentLinkedQueue();
    private ConcurrentLinkedQueue<L2Multisell.MultiSellEntry> _entriesC = new ConcurrentLinkedQueue();

    public void setListId(int listId)
    {
      _listId = listId;
    }

    public void setApplyTaxes(boolean applyTaxes) {
      _applyTaxes = applyTaxes;
    }

    public void setMaintainEnchantment(boolean maintainEnchantment) {
      _maintainEnchantment = maintainEnchantment;
    }

    public void setEnchant(int ench) {
      _enchLvl = ench;
    }

    public int getListId()
    {
      return _listId;
    }

    public boolean getApplyTaxes() {
      return _applyTaxes;
    }

    public boolean getMaintainEnchantment() {
      return _maintainEnchantment;
    }

    public int getEnchant() {
      return _enchLvl;
    }

    public void addEntry(L2Multisell.MultiSellEntry e) {
      _entriesC.add(e);
    }

    public ConcurrentLinkedQueue<L2Multisell.MultiSellEntry> getEntries() {
      return _entriesC;
    }

    public void setTicketId(int ticket) {
      _ticket = ticket;
    }

    public int getTicketId() {
      return _ticket;
    }

    public void addNpc(int npcId)
    {
      _npcList.add(Integer.valueOf(npcId));
    }

    public boolean containsNpc(int npcId) {
      return _npcList.contains(Integer.valueOf(npcId));
    }

    public void setSaveEnchantment(boolean f)
    {
      _saveEnchantment = f;
    }

    public boolean saveEnchantment() {
      return _saveEnchantment;
    }
  }

  public static class MultiSellIngredient
  {
    private int _itemId;
    private int _itemCount;
    private int _enchantmentLevel;
    private boolean _isTaxIngredient;
    private boolean _mantainIngredient;

    public MultiSellIngredient(int itemId, int itemCount, boolean isTaxIngredient, boolean mantainIngredient)
    {
      this(itemId, itemCount, 0, isTaxIngredient, mantainIngredient);
    }

    public MultiSellIngredient(int itemId, int itemCount, int enchantmentLevel, boolean isTaxIngredient, boolean mantainIngredient) {
      setItemId(itemId);
      setItemCount(itemCount);
      setEnchantmentLevel(enchantmentLevel);
      setIsTaxIngredient(isTaxIngredient);
      setMantainIngredient(mantainIngredient);
    }

    public MultiSellIngredient(MultiSellIngredient e) {
      _itemId = e.getItemId();
      _itemCount = e.getItemCount();
      _enchantmentLevel = e.getEnchantmentLevel();
      _isTaxIngredient = e.isTaxIngredient();
      _mantainIngredient = e.getMantainIngredient();
    }

    public void setItemId(int itemId)
    {
      _itemId = itemId;
    }

    public int getItemId()
    {
      return _itemId;
    }

    public void setItemCount(int itemCount)
    {
      _itemCount = itemCount;
    }

    public int getItemCount()
    {
      return _itemCount;
    }

    public void setEnchantmentLevel(int enchantmentLevel)
    {
      _enchantmentLevel = enchantmentLevel;
    }

    public int getEnchantmentLevel()
    {
      return _enchantmentLevel;
    }

    public void setIsTaxIngredient(boolean isTaxIngredient) {
      _isTaxIngredient = isTaxIngredient;
    }

    public boolean isTaxIngredient() {
      return _isTaxIngredient;
    }

    public void setMantainIngredient(boolean mantainIngredient) {
      _mantainIngredient = mantainIngredient;
    }

    public boolean getMantainIngredient() {
      return _mantainIngredient;
    }
  }

  public static class MultiSellEntry
  {
    private int _entryId;
    private List<L2Multisell.MultiSellIngredient> _products = new FastList();
    private List<L2Multisell.MultiSellIngredient> _ingredients = new FastList();

    public void setEntryId(int entryId)
    {
      _entryId = entryId;
    }

    public int getEntryId()
    {
      return _entryId;
    }

    public void addProduct(L2Multisell.MultiSellIngredient product)
    {
      _products.add(product);
    }

    public List<L2Multisell.MultiSellIngredient> getProducts()
    {
      return _products;
    }

    public void addIngredient(L2Multisell.MultiSellIngredient ingredient)
    {
      _ingredients.add(ingredient);
    }

    public List<L2Multisell.MultiSellIngredient> getIngredients()
    {
      return _ingredients;
    }
  }
}