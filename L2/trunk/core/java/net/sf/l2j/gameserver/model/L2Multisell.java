package net.sf.l2j.gameserver.model;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.MultiSellList;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class L2Multisell
{
    private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
    private List<MultiSellListContainer> _entries = new FastList<MultiSellListContainer>();
    private static L2Multisell _instance = new L2Multisell();
    public MultiSellListContainer getList(int id)
    {
        synchronized (_entries)
        {
            for (MultiSellListContainer list : _entries)
            {
                if (list.getListId() == id) return list;
            }
        }

        _log.warning("[L2Multisell] can't find list with id: " + id);
        return null;
    }

    public L2Multisell()
    {
        parseData();
    }

    public void reload()
    {
        parseData();
    }

    public static L2Multisell getInstance()
    {
        return _instance;
    }

    private void parseData()
    {
        _entries.clear();
        parse();
    }

    private MultiSellListContainer generateMultiSell(int listId, boolean inventoryOnly, L2PcInstance player, double taxRate)
    {
        MultiSellListContainer listTemplate = L2Multisell.getInstance().getList(listId);
        MultiSellListContainer list = new MultiSellListContainer();
        if (listTemplate == null) return list;
        list = L2Multisell.getInstance().new MultiSellListContainer();
        list.setListId(listId);

        if (inventoryOnly)
        {
        	if (player == null)
        		return list;

        	L2ItemInstance[] items;
        	if (listTemplate.getMaintainEnchantment())
        		items = player.getInventory().getUniqueItemsByEnchantLevel(false,false,false);
        	else
        		items = player.getInventory().getUniqueItems(false,false,false);

        	int enchantLevel;
            for (L2ItemInstance item : items)
            {
            	// only do the matchup on equipable items that are not currently equipped
            	// so for each appropriate item, produce a set of entries for the multisell list.
            	if (!item.isWear() && ((item.getItem() instanceof L2Armor) || (item.getItem() instanceof L2Weapon)))
            	{
            		enchantLevel = (listTemplate.getMaintainEnchantment()? item.getEnchantLevel() : 0);
            		// loop through the entries to see which ones we wish to include
	                for (MultiSellEntry ent : listTemplate.getEntries())
	                {
	                	boolean doInclude = false;

	                	// check ingredients of this entry to see if it's an entry we'd like to include.
		                for (MultiSellIngredient ing : ent.getIngredients())
		                {
		                    if (item.getItemId() == ing.getItemId())
		                    {
		                    	doInclude = true;
		                        break;
		                    }
		                }

		                // manipulate the ingredients of the template entry for this particular instance shown
		                // i.e: Assign enchant levels and/or apply taxes as needed.
		                if (doInclude)
		                	list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.getMaintainEnchantment(), enchantLevel, taxRate));
	                }
            	}
            } // end for each inventory item.
        } // end if "inventory-only"
        else  // this is a list-all type
        {
        	// if no taxes are applied, no modifications are needed
    		for (MultiSellEntry ent : listTemplate.getEntries())
    			list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0, taxRate));
        }

        return list;
    }

	// Regarding taxation, the following is the case:
	// a) The taxes come out purely from the adena TaxIngredient
	// b) If the entry has no adena ingredients other than the taxIngredient, the resulting
    //    amount of adena is appended to the entry
	// c) If the entry already has adena as an entry, the taxIngredient is used in order to increase
    //	  the count for the existing adena ingredient
    private MultiSellEntry prepareEntry(MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel, double taxRate)
    {
    	MultiSellEntry newEntry = L2Multisell.getInstance().new MultiSellEntry();
    	newEntry.setEntryId(templateEntry.getEntryId()*100000+enchantLevel);
    	int adenaAmount = 0;

        for (MultiSellIngredient ing : templateEntry.getIngredients())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
        	if ( ing.getItemId() == 57 && ing.isTaxIngredient() )
        	{
        		if (applyTaxes)
        			adenaAmount += (int)Math.round(ing.getItemCount()*taxRate);
        		continue;	// do not adena yet, as non-taxIngredient adena entries might occur next (order not guaranteed)
        	}
        	else if ( ing.getItemId() == 57 )  // && !ing.isTaxIngredient()
        	{
        		adenaAmount += ing.getItemCount();
        		continue;	// do not adena yet, as taxIngredient adena entries might occur next (order not guaranteed)
        	}
        	// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
        	else if (maintainEnchantment)
        	{
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
        	}

        	// finally, add this ingredient to the entry
        	newEntry.addIngredient(newIngredient);
        }
        // now add the adena, if any.
        if (adenaAmount > 0 )
        {
        	newEntry.addIngredient(L2Multisell.getInstance().new MultiSellIngredient(57,adenaAmount,0,false,false));
        }
        // Now modify the enchantment level of products, if necessary
        for (MultiSellIngredient ing : templateEntry.getProducts())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	if (maintainEnchantment)
            {
            	// if it is an armor/weapon, modify the enchantment level appropriately
            	// (note, if maintain enchantment is "false" this modification will result to a +0)
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
            }

        	newEntry.addProduct(newIngredient);
        }
        return newEntry;
    }

    public void SeparateAndSend(int listId, L2PcInstance player, boolean inventoryOnly, double taxRate)
    {
		MultiSellListContainer list = generateMultiSell(listId, inventoryOnly, player, taxRate);
		MultiSellListContainer temp = new MultiSellListContainer();
		int page = 1;

		temp.setListId(list.getListId());

		for (MultiSellEntry e : list.getEntries())
		{
			if (temp.getEntries().size() == 40)
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

    public class MultiSellEntry
    {
        private int _entryId;

        private List<MultiSellIngredient> _products = new FastList<MultiSellIngredient>();
        private List<MultiSellIngredient> _ingredients = new FastList<MultiSellIngredient>();

        /**
         * @param entryId The entryId to set.
         */
        public void setEntryId(int entryId)
        {
            _entryId = entryId;
        }

        /**
         * @return Returns the entryId.
         */
        public int getEntryId()
        {
            return _entryId;
        }

        /**
         * @param product The product to add.
         */
        public void addProduct(MultiSellIngredient product)
        {
            _products.add(product);
        }

        /**
         * @return Returns the products.
         */
        public List<MultiSellIngredient> getProducts()
        {
            return _products;
        }

        /**
         * @param ingredients The ingredients to set.
         */
        public void addIngredient(MultiSellIngredient ingredient)
        {
            _ingredients.add(ingredient);
        }

        /**
         * @return Returns the ingredients.
         */
        public List<MultiSellIngredient> getIngredients()
        {
            return _ingredients;
        }
    }

    public class MultiSellIngredient
    {
		private int _itemId, _itemCount, _enchantmentLevel;
        private boolean _isTaxIngredient, _mantainIngredient;

		public MultiSellIngredient(int itemId, int itemCount, boolean isTaxIngredient, boolean mantainIngredient)
        {
			this(itemId, itemCount, 0, isTaxIngredient, mantainIngredient);
        }

        public MultiSellIngredient(int itemId, int itemCount, int enchantmentLevel, boolean isTaxIngredient, boolean mantainIngredient)
        {
            setItemId(itemId);
            setItemCount(itemCount);
            setEnchantmentLevel(enchantmentLevel);
            setIsTaxIngredient(isTaxIngredient);
            setMantainIngredient(mantainIngredient);
        }

        public MultiSellIngredient(MultiSellIngredient e)
        {
        	_itemId = e.getItemId();
        	_itemCount = e.getItemCount();
        	_enchantmentLevel = e.getEnchantmentLevel();
        	_isTaxIngredient = e.isTaxIngredient();
        	_mantainIngredient = e.getMantainIngredient();
        }
        /**
         * @param itemId The itemId to set.
         */
        public void setItemId(int itemId)
        {
            _itemId = itemId;
        }

        /**
         * @return Returns the itemId.
         */
        public int getItemId()
        {
            return _itemId;
        }

        /**
         * @param itemCount The itemCount to set.
         */
        public void setItemCount(int itemCount)
        {
            _itemCount = itemCount;
        }

        /**
         * @return Returns the itemCount.
         */
        public int getItemCount()
        {
            return _itemCount;
        }

        /**
         * @param itemCount The itemCount to set.
         */
        public void setEnchantmentLevel(int enchantmentLevel)
        {
        	_enchantmentLevel = enchantmentLevel;
        }

        /**
         * @return Returns the itemCount.
         */
        public int getEnchantmentLevel()
        {
            return _enchantmentLevel;
        }

        public void setIsTaxIngredient(boolean isTaxIngredient)
        {
        	_isTaxIngredient = isTaxIngredient;
        }

        public boolean isTaxIngredient()
        {
        	return _isTaxIngredient;
        }

        public void setMantainIngredient(boolean mantainIngredient)
        {
        	_mantainIngredient = mantainIngredient;
        }

        public boolean getMantainIngredient()
        {
        	return _mantainIngredient;
        }
    }

    public class MultiSellListContainer
    {
        private int _listId;
        private boolean _applyTaxes = false;
		private String _bidNpcId;
        private boolean _maintainEnchantment = false;

        List<MultiSellEntry> _entriesC;

        public MultiSellListContainer()
        {
            _entriesC = new FastList<MultiSellEntry>();
        }

        /**
         * @param listId The listId to set.
         */
        public void setListId(int listId)
        {
            _listId = listId;
        }

        public void setApplyTaxes(boolean applyTaxes)
        {
        	_applyTaxes = applyTaxes;
        }

		public void setBidNpcId(String npcIds)
		{
			_bidNpcId = npcIds;
		}

        public void setMaintainEnchantment(boolean maintainEnchantment)
        {
        	_maintainEnchantment = maintainEnchantment;
        }

        /**
         * @return Returns the listId.
         */
        public int getListId()
        {
            return _listId;
        }

        public boolean getApplyTaxes()
        {
            return _applyTaxes;
        }

		public String getBidNpcId()
		{
			return _bidNpcId;
		}

        public boolean getMaintainEnchantment()
        {
            return _maintainEnchantment;
        }

        public void addEntry(MultiSellEntry e)
        {
            _entriesC.add(e);
        }

        public List<MultiSellEntry> getEntries()
        {
            return _entriesC;
        }
    }

    private void hashFiles(String dirname, List<File> hash)
    {
        File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
        if (!dir.exists())
        {
            _log.config("Dir " + dir.getAbsolutePath() + " not exists");
            return;
        }
        File[] files = dir.listFiles();
        for (File f : files)
        {
            if (f.getName().endsWith(".xml")) hash.add(f);
        }
    }

    private void parse()
    {
        Document doc = null;
        int id = 0;
        List<File> files = new FastList<File>();
        hashFiles("multisell", files);

        for (File f : files)
        {
            id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
            try
            {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                doc = factory.newDocumentBuilder().parse(f);
            }
            catch (Exception e)
            {
                _log.log(Level.SEVERE, "Error loading file " + f, e);
            }
            try
            {
                MultiSellListContainer list = parseDocument(doc);
                list.setListId(id);
                _entries.add(list);
            }
            catch (Exception e)
            {
                _log.log(Level.SEVERE, "Error in file " + f, e);
            }
        }
    }

    protected MultiSellListContainer parseDocument(Document doc)
    {
        MultiSellListContainer list = new MultiSellListContainer();

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {
            	Node attribute;
				attribute = n.getAttributes().getNamedItem("NpcId");
				if (attribute == null)
					list.setBidNpcId(null);
				else
					list.setBidNpcId(attribute.getNodeValue());
            	attribute = n.getAttributes().getNamedItem("applyTaxes");
            	if(attribute == null)
            		list.setApplyTaxes(false);
            	else
            		list.setApplyTaxes(Boolean.parseBoolean(attribute.getNodeValue()));
            	attribute = n.getAttributes().getNamedItem("maintainEnchantment");
            	if(attribute == null)
            		list.setMaintainEnchantment(false);
            	else
            		list.setMaintainEnchantment(Boolean.parseBoolean(attribute.getNodeValue()));

                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("item".equalsIgnoreCase(d.getNodeName()))
                    {
                        MultiSellEntry e = parseEntry(d);
                        list.addEntry(e);
                    }
                }
            }
            else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                MultiSellEntry e = parseEntry(n);
                list.addEntry(e);
            }
        }

        return list;
    }

    protected MultiSellEntry parseEntry(Node n)
    {
        int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());

        Node first = n.getFirstChild();
        MultiSellEntry entry = new MultiSellEntry();

        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("ingredient".equalsIgnoreCase(n.getNodeName()))
            {
            	Node attribute;

                int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
                int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
                boolean isTaxIngredient = false, mantainIngredient = false;

                attribute = n.getAttributes().getNamedItem("isTaxIngredient");

                if (attribute != null)
                	isTaxIngredient = Boolean.parseBoolean(attribute.getNodeValue());

                attribute = n.getAttributes().getNamedItem("mantainIngredient");

                if (attribute != null)
                	mantainIngredient = Boolean.parseBoolean(attribute.getNodeValue());

                MultiSellIngredient e = new MultiSellIngredient(id, count, isTaxIngredient, mantainIngredient);
                entry.addIngredient(e);
            }
            else if ("production".equalsIgnoreCase(n.getNodeName()))
            {
                int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
                int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());

                MultiSellIngredient e = new MultiSellIngredient(id, count, false, false);
                entry.addProduct(e);
            }
        }

        entry.setEntryId(entryId);

        return entry;
    }
}
