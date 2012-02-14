package events.RabbitsToRiches;

import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Drop;
import l2rt.gameserver.model.L2DropData;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.Rnd;

public class TreasureSack implements IItemHandler, ScriptFile
{
	private static final int[] PLACE_TREASURE_SACK = { 10254, 10255, 10256, 10257, 10258, 10259 };

	protected static final L2DropData[][] PLACE_TREASURE_SACK_DROP = new L2DropData[][] { {
	// Item Chance
			new L2DropData(6373, 1, 1, 250, 1), // Imperial Crusader Breastplate 0.025%
			new L2DropData(6374, 1, 1, 250, 1), // Imperial Crusader Gaiters 0.025%
			new L2DropData(6375, 1, 1, 250, 1), // Imperial Crusader Gauntlets 0.025%
			new L2DropData(6376, 1, 1, 250, 1), // Imperial Crusader Boots 0.025%
			new L2DropData(6378, 1, 1, 250, 1), // Imperial Crusader Helmet 0.025%
			new L2DropData(6379, 1, 1, 250, 1), // Draconic Leather Armor 0.025%
			new L2DropData(6380, 1, 1, 250, 1), // Draconic Leather Gloves 0.025%
			new L2DropData(6381, 1, 1, 250, 1), // Draconic Leather Boots 0.025%
			new L2DropData(6382, 1, 1, 250, 1), // Draconic Leather Helmet 0.025%
			new L2DropData(6383, 1, 1, 250, 1), // Major Arcana Robe 0.025%
			new L2DropData(6384, 1, 1, 250, 1), // Major Arcana Gloves 0.025%
			new L2DropData(6385, 1, 1, 250, 1), // Major Arcana Boots 0.025%
			new L2DropData(6386, 1, 1, 250, 1), // Major Arcana Circlet 0.025%
			new L2DropData(5908, 1, 1, 250, 1), // Red Soul Crystal: Grade 13 0.025%
			new L2DropData(5911, 1, 1, 250, 1), // Green Soul Crystal: Grade 13 0.025%
			new L2DropData(5914, 1, 1, 250, 1), // Blue Soul Crystal: Grade 13 0.025%
			new L2DropData(9570, 1, 1, 250, 1), // Red Soul Crystal - Stage 14 0.025%
			new L2DropData(9571, 1, 1, 250, 1), // Blue Soul Crystal - Stage 14 0.025%
			new L2DropData(9572, 1, 1, 250, 1) // Green Soul Crystal - Stage 14 0.025%
			},

			{
			// Item Chance
					new L2DropData(959, 1, 1, 250, 1), // Scroll: Enchant Weapon (S) 0.025%
					new L2DropData(960, 1, 1, 250, 1), // Scroll: Enchant Armor (S) 0.025%
					new L2DropData(961, 1, 1, 250, 1), // Crystal Scroll: Enchant Weapon (S) 0.025%
					new L2DropData(962, 1, 1, 250, 1), // Crystal Scroll: Enchant Armor (S) 0.025%
					new L2DropData(9625, 1, 1, 250, 1), // Giant''s Codex - Oblivion 0.025%
					new L2DropData(9626, 1, 1, 250, 1), // Giant''s Codex - Discipline 0.025%
					new L2DropData(9627, 1, 1, 250, 1), // Giant''s Codex - Mastery 0.025%
					new L2DropData(9546, 1, 1, 250, 1), // Fire Stone 0.025%
					new L2DropData(9547, 1, 1, 250, 1), // Water Stone 0.025%
					new L2DropData(9548, 1, 1, 250, 1), // Earth Stone 0.025%
					new L2DropData(9549, 1, 1, 250, 1), // Wind Stone 0.025%
					new L2DropData(9550, 1, 1, 250, 1), // Dark Stone 0.025%
					new L2DropData(9551, 1, 1, 250, 1), // Divine Stone 0.025%
					new L2DropData(9552, 1, 1, 250, 1), // Fire Crystal 0.025%
					new L2DropData(9553, 1, 1, 250, 1), // Water Crystal 0.025%
					new L2DropData(9554, 1, 1, 250, 1), // Earth Crystal 0.025%
					new L2DropData(9555, 1, 1, 250, 1), // Wind Crystal 0.025%
					new L2DropData(9556, 1, 1, 250, 1), // Dark Crystal 0.025%
					new L2DropData(9557, 1, 1, 250, 1), // Divine Crystal 0.025%
					new L2DropData(9558, 1, 1, 250, 1), // Fire Jewel 0.025%
					new L2DropData(9559, 1, 1, 250, 1), // Water Jewel 0.025%
					new L2DropData(9560, 1, 1, 250, 1), // Earth Jewel 0.025%
					new L2DropData(9561, 1, 1, 250, 1), // Wind Jewel 0.025%
					new L2DropData(9562, 1, 1, 250, 1), // Dark Jewel 0.025%
					new L2DropData(9563, 1, 1, 250, 1), // Divine Jewel 0.025%
					new L2DropData(9564, 1, 1, 250, 1), // Fire Energy 0.025%
					new L2DropData(9565, 1, 1, 250, 1), // Water Energy 0.025%
					new L2DropData(9566, 1, 1, 250, 1), // Earth Energy 0.025%
					new L2DropData(9567, 1, 1, 250, 1), // Wind Energy 0.025%
					new L2DropData(9568, 1, 1, 250, 1), // Dark Energy 0.025%
					new L2DropData(9569, 1, 1, 250, 1) // Divine Energy 0.025%
			},

			{
			// Item Chance
					new L2DropData(729, 1, 1, 250, 1), // Scroll: Enchant Weapon (A) 0.025%
					new L2DropData(730, 1, 1, 250, 1), // Scroll: Enchant Armor (A) 0.025%
					new L2DropData(731, 1, 1, 250, 1), // Crystal Scroll: Enchant Weapon (A) 0.025%
					new L2DropData(732, 1, 1, 250, 1), // Crystal Scroll: Enchant Armor (A) 0.025%
					new L2DropData(947, 1, 1, 250, 1), // Scroll: Enchant Weapon (B) 0.025%
					new L2DropData(948, 1, 1, 250, 1), // Scroll: Enchant Armor (B) 0.025%
					new L2DropData(949, 1, 1, 250, 1), // Crystal Scroll: Enchant Weapon (B) 0.025%
					new L2DropData(950, 1, 1, 250, 1), // Crystal Scroll: Enchant Armor (B) 0.025%
					new L2DropData(8723, 1, 1, 250, 1), // Life Stone: level 46 0.025%
					new L2DropData(8724, 1, 1, 250, 1), // Life Stone: level 49 0.025%
					new L2DropData(8725, 1, 1, 250, 1), // Life Stone: level 52 0.025%
					new L2DropData(8726, 1, 1, 250, 1), // Life Stone: level 55 0.025%
					new L2DropData(8727, 1, 1, 250, 1), // Life Stone: level 58 0.025%
					new L2DropData(8728, 1, 1, 250, 1), // Life Stone: level 61 0.025%
					new L2DropData(8729, 1, 1, 250, 1), // Life Stone: level 64 0.025%
					new L2DropData(8730, 1, 1, 250, 1), // Life Stone: level 67 0.025%
					new L2DropData(8731, 1, 1, 250, 1), // Life Stone: level 70 0.025%
					new L2DropData(8732, 1, 1, 250, 1), // Life Stone: level 76 0.025%
					new L2DropData(9573, 1, 1, 250, 1), // Life Stone: level 80 0.025%
					new L2DropData(10483, 1, 1, 250, 1), // Life Stone: level 82 0.025%
					new L2DropData(14166, 1, 1, 250, 1), // Life Stone: level 84 0.025%
					new L2DropData(8733, 1, 1, 250, 1), // Mid-Grade Life Stone: level 46 0.025%
					new L2DropData(8734, 1, 1, 250, 1), // Mid-Grade Life Stone: level 49 0.025%
					new L2DropData(8735, 1, 1, 250, 1), // Mid-Grade Life Stone: level 52 0.025%
					new L2DropData(8736, 1, 1, 250, 1), // Mid-Grade Life Stone: level 55 0.025%
					new L2DropData(8737, 1, 1, 250, 1), // Mid-Grade Life Stone: level 58 0.025%
					new L2DropData(8738, 1, 1, 250, 1), // Mid-Grade Life Stone: level 61 0.025%
					new L2DropData(8739, 1, 1, 250, 1), // Mid-Grade Life Stone: level 64 0.025%
					new L2DropData(8740, 1, 1, 250, 1), // Mid-Grade Life Stone: level 67 0.025%
					new L2DropData(8741, 1, 1, 250, 1), // Mid-Grade Life Stone: level 70 0.025%
					new L2DropData(8742, 1, 1, 250, 1), // Mid-Grade Life Stone: level 76 0.025%
					new L2DropData(9574, 1, 1, 250, 1), // Mid-Grade Life Stone: level 80 0.025%
					new L2DropData(10484, 1, 1, 250, 1), // Mid-Grade Life Stone: level 82 0.025%
					new L2DropData(14167, 1, 1, 250, 1), // Mid-Grade Life Stone: level 84 0.025%
					new L2DropData(8743, 1, 1, 250, 1), // High-Grade Life Stone: level 46 0.025%
					new L2DropData(8744, 1, 1, 250, 1), // High-Grade Life Stone: level 49 0.025%
					new L2DropData(8745, 1, 1, 250, 1), // High-Grade Life Stone: level 52 0.025%
					new L2DropData(8746, 1, 1, 250, 1), // High-Grade Life Stone: level 55 0.025%
					new L2DropData(8747, 1, 1, 250, 1), // High-Grade Life Stone: level 58 0.025%
					new L2DropData(8748, 1, 1, 250, 1), // High-Grade Life Stone: level 61 0.025%
					new L2DropData(8749, 1, 1, 250, 1), // High-Grade Life Stone: level 64 0.025%
					new L2DropData(8750, 1, 1, 250, 1), // High-Grade Life Stone: level 67 0.025%
					new L2DropData(8751, 1, 1, 250, 1), // High-Grade Life Stone: level 70 0.025%
					new L2DropData(8752, 1, 1, 250, 1), // High-Grade Life Stone: level 76 0.025%
					new L2DropData(9575, 1, 1, 250, 1), // High-Grade Life Stone: level 80 0.025%
					new L2DropData(10485, 1, 1, 250, 1), // High-Grade Life Stone: level 82 0.025%
					new L2DropData(14168, 1, 1, 250, 1), // High-Grade Life Stone: level 84 0.025%
					new L2DropData(8753, 1, 1, 250, 1), // Top-Grade Life Stone: level 46 0.025%
					new L2DropData(8754, 1, 1, 250, 1), // Top-Grade Life Stone: level 49 0.025%
					new L2DropData(8755, 1, 1, 250, 1), // Top-Grade Life Stone: level 52 0.025%
					new L2DropData(8756, 1, 1, 250, 1), // Top-Grade Life Stone: level 55 0.025%
					new L2DropData(8757, 1, 1, 250, 1), // Top-Grade Life Stone: level 58 0.025%
					new L2DropData(8758, 1, 1, 250, 1), // Top-Grade Life Stone: level 61 0.025%
					new L2DropData(8759, 1, 1, 250, 1), // Top-Grade Life Stone: level 64 0.025%
					new L2DropData(8760, 1, 1, 250, 1), // Top-Grade Life Stone: level 67 0.025%
					new L2DropData(8761, 1, 1, 250, 1), // Top-Grade Life Stone: level 70 0.025%
					new L2DropData(8762, 1, 1, 250, 1), // Top-Grade Life Stone: level 76 0.025%
					new L2DropData(9576, 1, 1, 250, 1), // Top-Grade Life Stone: level 80 0.025%
					new L2DropData(10486, 1, 1, 250, 1), // Top-Grade Life Stone: level 80 0.025%
					new L2DropData(14169, 1, 1, 250, 1), // Top-Grade Life Stone: level 84 0.025%
					new L2DropData(3936, 1, 1, 250, 1) // Blessed Scroll of Resurrection 0.025%
			},

			{
			// Item Chance
					new L2DropData(951, 1, 1, 250, 1), // Scroll: Enchant Weapon (C) 0.025%
					new L2DropData(952, 1, 1, 250, 1), // Scroll: Enchant Armor (C) 0.025%
					new L2DropData(953, 1, 1, 250, 1), // Crystal Scroll: Enchant Weapon (C) 0.025%
					new L2DropData(954, 1, 1, 250, 1), // Crystal Scroll: Enchant Armor (C) 0.025%
					new L2DropData(955, 1, 1, 250, 1), // Scroll: Enchant Weapon (D) 0.025%
					new L2DropData(956, 1, 1, 250, 1), // Scroll: Enchant Armor (D) 0.025%
					new L2DropData(957, 1, 1, 250, 1), // Crystal Scroll: Enchant Weapon (D) 0.025%
					new L2DropData(958, 1, 1, 250, 1), // Crystal Scroll: Enchant Armor (D) 0.025%
					new L2DropData(14104, 1, 1, 250, 1), // Shadow Item - Collection Agathion Summon Bracelet 0.025%
					new L2DropData(1538, 1, 1, 250, 1) // Blessed Scroll of Escape 0.025%
			},

			{
			// Item Chance
					new L2DropData(10178, 1, 1, 250, 1), // Sweet Fruit Cocktail 0.025%
					new L2DropData(10179, 1, 1, 250, 1), // Fresh Fruit Cocktail 0.025%
					new L2DropData(20393, 1, 1, 250, 1), // Sweet Fruit Cocktail 0.025%
					new L2DropData(20394, 1, 1, 250, 1) // Fresh Fruit Cocktail 0.025%
			},

			{
			// Item Chance
					new L2DropData(10260, 1, 1, 250, 1), // Haste Juice 0.025%
					new L2DropData(10261, 1, 1, 250, 1), // Accuracy Juice 0.025%
					new L2DropData(10262, 1, 1, 250, 1), // Critical Power Juice 0.025%
					new L2DropData(10263, 1, 1, 250, 1), // Critical Attack Juice 0.025%
					new L2DropData(10264, 1, 1, 250, 1), // Casting Spd. Juice 0.025%
					new L2DropData(10265, 1, 1, 250, 1), // Evasion Juice 0.025%
					new L2DropData(10266, 1, 1, 250, 1), // Magic Power Juice 0.025%
					new L2DropData(10267, 1, 1, 250, 1), // Power Juice 0.025%
					new L2DropData(10268, 1, 1, 250, 1), // Speed Juice 0.025%
					new L2DropData(10269, 1, 1, 250, 1), // Defense Juice 0.025%
					new L2DropData(10270, 1, 1, 250, 1), // MP Consumption Juice 0.025%
					new L2DropData(10271, 1, 1, 250, 1) // Marvelous Fruit Juice #12 0.025%
			} };

	public synchronized void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(!playable.isPlayer())
			return;
		L2Player activeChar = playable.getPlayer();

		if(!activeChar.isQuestContinuationPossible(true))
			return;

		int item_id = item.getItemId();

		activeChar.getInventory().destroyItem(item, 1, true);
		activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_DISAPPEARED).addItemName(item_id));
		getGroupItem(activeChar, PLACE_TREASURE_SACK_DROP[item_id - 10254]);
	}

	public void getGroupItem(L2Player activeChar, L2DropData[] dropData)
	{
		L2ItemInstance item;
		long count = 0;
		for(L2DropData d : dropData)
			if(Rnd.get(1, L2Drop.MAX_CHANCE) <= d.getChance() * Config.EVENT_RabbitsToRichesRewardRate)
			{
				count = Rnd.get(d.getMinDrop(), d.getMaxDrop());
				item = ItemTemplates.getInstance().createItem(d.getItemId());
				item.setCount(count);
				activeChar.getInventory().addItem(item);
				if(count > 1)
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S2_S1S).addItemName(d.getItemId()).addNumber(count));
				else
					activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1).addItemName(d.getItemId()));
			}
	}

	@Override
	public final int[] getItemIds()
	{
		return PLACE_TREASURE_SACK;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}
}