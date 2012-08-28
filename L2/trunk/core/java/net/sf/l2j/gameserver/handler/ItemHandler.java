//L2DDT
package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.handler.itemhandlers.*;

public class ItemHandler
{
	private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
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

	private ItemHandler()
	{
		_datatable = new TreeMap<Integer, IItemHandler>();
		registerItemHandler(new ScrollOfEscape());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new Key());
		registerItemHandler(new PaganKeys());
		registerItemHandler(new Maps());
		registerItemHandler(new CastPotions());
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
		registerItemHandler(new ItemBuff());
		_log.config("ItemHandler: Loaded " + _datatable.size() + " handlers.");
		 	}
		 
		  public int size()
		  {
		    return _datatable.size();
		  }
	

	public void registerItemHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		for (int i = 0; i < ids.length; i++)
		{
			_datatable.put(new Integer(ids[i]), handler);
		}
	}

	public IItemHandler getItemHandler(int itemId)
	{
		return _datatable.get(new Integer(itemId));
	}
}
