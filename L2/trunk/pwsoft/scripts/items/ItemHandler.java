package scripts.items;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.items.itemhandlers.BeastSoulShot;
import scripts.items.itemhandlers.BeastSpice;
import scripts.items.itemhandlers.BeastSpiritShot;
import scripts.items.itemhandlers.BlessedSpiritShot;
import scripts.items.itemhandlers.Book;
import scripts.items.itemhandlers.CharChangePotions;
import scripts.items.itemhandlers.ChestKey;
import scripts.items.itemhandlers.ChristmasTree;
import scripts.items.itemhandlers.CrystalCarol;
import scripts.items.itemhandlers.EnchantScrolls;
import scripts.items.itemhandlers.EnergyStone;
import scripts.items.itemhandlers.ExtractableItems;
import scripts.items.itemhandlers.Firework;
import scripts.items.itemhandlers.FishShots;
import scripts.items.itemhandlers.Harvester;
import scripts.items.itemhandlers.Maps;
import scripts.items.itemhandlers.MercTicket;
import scripts.items.itemhandlers.MysteryPotion;
import scripts.items.itemhandlers.PaganKeys;
import scripts.items.itemhandlers.Potions;
import scripts.items.itemhandlers.Recipes;
import scripts.items.itemhandlers.Remedy;
import scripts.items.itemhandlers.RollingDice;
import scripts.items.itemhandlers.ScrollOfEscape;
import scripts.items.itemhandlers.ScrollOfResurrection;
import scripts.items.itemhandlers.Scrolls;
import scripts.items.itemhandlers.Seed;
import scripts.items.itemhandlers.SevenSignsRecord;
import scripts.items.itemhandlers.SoulCrystals;
import scripts.items.itemhandlers.SoulShots;
import scripts.items.itemhandlers.SpecialXMas;
import scripts.items.itemhandlers.SpiritShot;
import scripts.items.itemhandlers.SummonItems;

public class ItemHandler
{
  private static final Logger _log = AbstractLogger.getLogger(ItemHandler.class.getName());
  private static ItemHandler _instance;
  private Map<Integer, IItemHandler> _datatable;

  public static ItemHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new ItemHandler();
    }
    return _instance;
  }

  public int size()
  {
    return _datatable.size();
  }

  private ItemHandler()
  {
    _datatable = new TreeMap();
    registerItemHandler(new ScrollOfEscape());
    registerItemHandler(new ScrollOfResurrection());
    registerItemHandler(new SoulShots());
    registerItemHandler(new SpiritShot());
    registerItemHandler(new BlessedSpiritShot());
    registerItemHandler(new BeastSoulShot());
    registerItemHandler(new BeastSpiritShot());
    registerItemHandler(new ChestKey());
    registerItemHandler(new PaganKeys());
    registerItemHandler(new Maps());
    registerItemHandler(new Potions());
    registerItemHandler(new Recipes());
    registerItemHandler(new RollingDice());
    registerItemHandler(new MysteryPotion());
    registerItemHandler(new EnchantScrolls());
    registerItemHandler(new EnergyStone());
    registerItemHandler(new Book());
    registerItemHandler(new Remedy());
    registerItemHandler(new Scrolls());
    registerItemHandler(new CrystalCarol());
    registerItemHandler(new SoulCrystals());
    registerItemHandler(new SevenSignsRecord());
    registerItemHandler(new CharChangePotions());
    registerItemHandler(new Firework());
    registerItemHandler(new Seed());
    registerItemHandler(new Harvester());
    registerItemHandler(new MercTicket());
    registerItemHandler(new FishShots());
    registerItemHandler(new ExtractableItems());
    registerItemHandler(new SpecialXMas());
    registerItemHandler(new SummonItems());
    registerItemHandler(new BeastSpice());
    registerItemHandler(new ChristmasTree());

    _log.config("ItemHandler: Loaded " + _datatable.size() + " handlers.");
  }

  public void registerItemHandler(IItemHandler handler)
  {
    int[] ids = handler.getItemIds();

    for (int i = 0; i < ids.length; i++)
    {
      _datatable.put(Integer.valueOf(ids[i]), handler);
    }
  }

  public IItemHandler getItemHandler(int itemId)
  {
    return (IItemHandler)_datatable.get(Integer.valueOf(itemId));
  }
}