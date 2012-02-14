import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.model.L2Drop;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.ShowXMasSeal;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class ItemHandlers extends Functions
{
	// Newspaper
	public void ItemHandler_19999(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		show("data/html/newspaper/00000000.htm", player);
	}

	public void ItemHandler_5555(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		player.sendPacket(new ShowXMasSeal(5555));
	}

	public void ItemHandler_8060(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(8060, player))
			return;

		if(Functions.getItemCount(player, 8058) > 0)
		{
			removeItem(player, 8058, 1);
			addItem(player, 8059, 1);
		}
	}

	// ------ Adventurer's Boxes ------

	// Adventurer's Box: C-Grade Accessory (Low Grade)
	public void ItemHandler_8534(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8534, player))
			return;
		int[] list = new int[] { 853, 884, 916 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 14, 21, 10 };
		removeItem(player, 8534, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: C-Grade Accessory (Medium Grade)
	public void ItemHandler_8535(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8535, player))
			return;
		int[] list = new int[] { 854, 885, 917 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 14, 21, 10 };
		removeItem(player, 8535, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: C-Grade Accessory (High Grade)
	public void ItemHandler_8536(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8536, player))
			return;
		int[] list = new int[] { 119, 855, 886 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 9, 13, 19 };
		removeItem(player, 8536, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: B-Grade Accessory (Low Grade)
	public void ItemHandler_8537(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8537, player))
			return;
		int[] list = new int[] { 856, 887, 918 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 13, 20, 10 };
		removeItem(player, 8537, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: B-Grade Accessory (High Grade)
	public void ItemHandler_8538(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8538, player))
			return;
		int[] list = new int[] { 864, 895, 926 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 12, 18, 9 };
		removeItem(player, 8538, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: Hair Accessory
	public void ItemHandler_8539(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8539, player))
			return;
		int[] list = new int[] { 8177, 8178, 8179 };
		int[] counts = new int[] { 1, 1, 1 };
		int[] chances = new int[] { 22, 18, 15 };
		removeItem(player, 8539, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Adventurer's Box: Cradle of Creation
	public void ItemHandler_8540(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(8540, player))
			return;
		removeItem(player, 8540, 1);
		if(Rnd.chance(16))
			addItem(player, 8175, 1);
	}

	// Quest 370: A Wiseman Sows Seeds
	public void ItemHandler_5916(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5916, player))
			return;
		int[] list = new int[] { 5917, 5918, 5919, 5920, 736 };
		int[] counts = new int[] { 1, 1, 1, 1, 1 };
		removeItem(player, 5916, 1);
		extract_item(list, counts, player);
	}

	// Quest 376: Giants Cave Exploration, Part 1
	public void ItemHandler_5944(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5944, player))
			return;
		int[] list = { 5922, 5923, 5924, 5925, 5926, 5927, 5928, 5929, 5930, 5931, 5932, 5933, 5934, 5935, 5936, 5937,
				5938, 5939, 5940, 5941, 5942, 5943 };
		int[] counts = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = removeItem(player, 5944, Long.MAX_VALUE);
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 5944, 1);
			extract_item(list, counts, player);
		}
	}

	// Quest 376: Giants Cave Exploration, Part 1
	public void ItemHandler_14841(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14841, player))
			return;

		int[] list = { 14836, 14837, 14838, 14839, 14840 };
		int[] counts = { 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = removeItem(player, 14841, Long.MAX_VALUE);
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 14841, 1);
			extract_item(list, counts, player);
		}
	}

	// Quest 377: Giants Cave Exploration, Part 2, old
	public void ItemHandler_5955(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5955, player))
			return;
		int[] list = { 5942, 5943, 5945, 5946, 5947, 5948, 5949, 5950, 5951, 5952, 5953, 5954 };
		int[] counts = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = removeItem(player, 5955, Long.MAX_VALUE);
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 5955, 1);
			extract_item(list, counts, player);
		}
	}

	// Quest 377: Giants Cave Exploration, Part 2, new
	public void ItemHandler_14847(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14847, player))
			return;
		int[] list = { 14842, 14843, 14844, 14845, 14846 };
		int[] counts = { 1, 1, 1, 1, 1 };

		if(ctrl)
		{
			long item_count = removeItem(player, 14847, Long.MAX_VALUE);
			for(int[] res : mass_extract_item(item_count, list, counts, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 14847, 1);
			extract_item(list, counts, player);
		}
	}

	// Quest 372: Legacy of Insolence
	public void ItemHandler_5966(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5966, player))
			return;
		int[] list = new int[] { 5970, 5971, 5977, 5978, 5979, 5986, 5993, 5994, 5995, 5997, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		removeItem(player, 5966, 1);
		extract_item(list, counts, player);
	}

	// Quest 372: Legacy of Insolence
	public void ItemHandler_5967(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5967, player))
			return;
		int[] list = new int[] { 5970, 5971, 5975, 5976, 5980, 5985, 5993, 5994, 5995, 5997, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		removeItem(player, 5967, 1);
		extract_item(list, counts, player);
	}

	// Quest 372: Legacy of Insolence
	public void ItemHandler_5968(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5968, player))
			return;
		int[] list = new int[] { 5973, 5974, 5981, 5984, 5989, 5990, 5991, 5992, 5996, 5998, 5999, 6000, 5988, 5983, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		removeItem(player, 5968, 1);
		extract_item(list, counts, player);
	}

	// Quest 372: Legacy of Insolence
	public void ItemHandler_5969(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(5969, player))
			return;
		int[] list = new int[] { 5970, 5971, 5982, 5987, 5989, 5990, 5991, 5992, 5996, 5998, 5999, 6000, 5972, 6001 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		removeItem(player, 5969, 1);
		extract_item(list, counts, player);
	}

	/**
	 * Quest 373: Supplier of Reagents, from Hallate's Maid, Reagent Pouch (Gray)
	 * 2x Quicksilver (6019) 30%
	 * 2x Moonstone Shard (6013) 30%
	 * 1x Rotten Bone Piece (6014) 20%
	 * 1x Infernium Ore (6016) 20%
	 */
	public void ItemHandler_6007(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(6007, player))
			return;

		int[] list = new int[] { 6019, 6013, 6014, 6016 };
		int[] counts = new int[] { 2, 2, 1, 1 };
		int[] chances = new int[] { 30, 30, 20, 20 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(6007);
			removeItem(player, 6007, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 6007, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	/**
	 * Quest 373: Supplier of Reagents, from Platinum Tribe Shaman, Reagent Pouch (Yellow)
	 * 2x Blood Root (6017) 10%
	 * 2x Sulfur (6020) 20%
	 * 1x Rotten Bone Piece (6014) 35%
	 * 1x Infernium Ore (6016) 35%
	 */
	public void ItemHandler_6008(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(6008, player))
			return;

		int[] list = new int[] { 6017, 6020, 6014, 6016 };
		int[] counts = new int[] { 2, 2, 1, 1 };
		int[] chances = new int[] { 10, 20, 35, 35 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(6008);
			removeItem(player, 6008, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 6008, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	/**
	 * Quest 373: Supplier of Reagents, from Hames Orc Shaman, Reagent Pouch (Brown)
	 * 1x Lava Stone (6012) 20%
	 * 2x Volcanic Ash (6018) 20%
	 * 2x Quicksilver (6019) 20%
	 * 1x Moonstone Shard (6013) 40%
	 */
	public void ItemHandler_6009(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(6009, player))
			return;

		int[] list = new int[] { 6012, 6018, 6019, 6013 };
		int[] counts = new int[] { 1, 2, 2, 1 };
		int[] chances = new int[] { 20, 20, 20, 40 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(6009);
			removeItem(player, 6009, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 6009, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	/**
	 * Quest 373: Supplier of Reagents, from Platinum Guardian Shaman, Reagent Box
	 * 2x Blood Root (6017) 20%
	 * 2x Sulfur (6020) 20%
	 * 1x Infernium Ore (6016) 35%
	 * 2x Demon's Blood (6015) 25%
	 */
	public void ItemHandler_6010(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(6010, player))
			return;

		int[] list = new int[] { 6017, 6020, 6016, 6015 };
		int[] counts = new int[] { 2, 2, 1, 2 };
		int[] chances = new int[] { 20, 20, 35, 25 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(6010);
			removeItem(player, 6010, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 6010, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	// Quest 628: Hunt of Golden Ram
	public void ItemHandler_7725(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(7725, player))
			return;

		int[] list = new int[] { 6035, 1060, 735, 1540, 1061, 1539 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1 };
		int[] chances = new int[] { 7, 39, 7, 3, 12, 32 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(7725);
			removeItem(player, 7725, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 7725, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	// Quest 628: Hunt of Golden Ram
	public void ItemHandler_7637(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(7637, player))
			return;

		int[] list = new int[] { 4039, 4041, 4043, 4044, 4042, 4040 };
		int[] counts = new int[] { 4, 1, 4, 4, 2, 2 };
		int[] chances = new int[] { 20, 10, 20, 20, 15, 15 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(7637);
			removeItem(player, 7637, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 7637, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	// Quest 628: Hunt of Golden Ram
	public void ItemHandler_7636(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(7636, player))
			return;

		int[] list = new int[] { 1875, 1882, 1880, 1874, 1877, 1881, 1879, 1876 };
		int[] counts = new int[] { 3, 3, 4, 1, 3, 1, 3, 6 };
		int[] chances = new int[] { 10, 20, 10, 10, 10, 12, 12, 16 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(7636);
			removeItem(player, 7636, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 7636, 1);
			extract_item_r(list, counts, chances, player);
		}
	}

	// Looted Goods - White Cargo box
	public void ItemHandler_7629(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7629, player))
			return;
		int[] list = new int[] { 6688, 6689, 6690, 6691, 6693, 6694, 6695, 6696, 6697, 7579, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 330000 };
		int[] chances = new int[] { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10 };
		removeItem(player, 7629, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Blue Cargo box #All chances of 8 should be 8.5, must be fixed if possible!!
	public void ItemHandler_7630(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7630, player))
			return;
		int[] list = new int[] { 6703, 6704, 6705, 6706, 6708, 6709, 6710, 6712, 6713, 6714, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 292000 };
		int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 20 };
		removeItem(player, 7630, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Yellow Cargo box
	public void ItemHandler_7631(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7631, player))
			return;
		int[] list = new int[] { 6701, 6702, 6707, 6711, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 930000 };
		int[] chances = new int[] { 20, 20, 20, 20, 20 };
		removeItem(player, 7631, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Red Filing Cabinet
	public void ItemHandler_7632(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7632, player))
			return;

		int[] list;
		if(Config.ALT_100_RECIPES_S)
			list = new int[] { 6858, 6860, 6862, 6864, 6868, 6870, 6872, 6876, 6878, 6880, 13101, 57 };
		else
			list = new int[] { 6857, 6859, 6861, 6863, 6867, 6869, 6871, 6875, 6877, 6879, 13100, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 340000 };
		int[] chances = new int[] { 8, 9, 8, 9, 8, 9, 8, 9, 8, 9, 8, 7 };
		removeItem(player, 7632, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Purple Filing Cabinet
	public void ItemHandler_7633(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7633, player))
			return;
		int[] list;
		if(Config.ALT_100_RECIPES_S)
			list = new int[] { 6854, 6856, 6866, 6874, 57 };
		else
			list = new int[] { 6853, 6855, 6865, 6873, 57 };
		int[] counts = new int[] { 1, 1, 1, 1, 850000 };
		int[] chances = new int[] { 20, 20, 20, 20, 20 };
		removeItem(player, 7633, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Brown Pouch
	public void ItemHandler_7634(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7634, player))
			return;
		int[] list = new int[] { 1874, 1875, 1876, 1877, 1879, 1880, 1881, 1882, 57 };
		int[] counts = new int[] { 20, 20, 20, 20, 20, 20, 20, 20, 150000 };
		int[] chances = new int[] { 10, 10, 16, 11, 10, 5, 10, 18, 10 };
		removeItem(player, 7634, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Looted Goods - Gray Pouch
	public void ItemHandler_7635(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(7635, player))
			return;
		int[] list = new int[] { 4039, 4040, 4041, 4042, 4043, 4044, 57 };
		int[] counts = new int[] { 4, 4, 4, 4, 4, 4, 160000 };
		int[] chances = new int[] { 20, 10, 10, 10, 20, 20, 10 };
		removeItem(player, 7635, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Old Agathion
	public void ItemHandler_10408(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(10408, player))
			return;
		removeItem(player, 10408, 1);
		addItem(player, 6471, 20);
		addItem(player, 5094, 40);
		addItem(player, 9814, 3);
		addItem(player, 9816, 4);
		addItem(player, 9817, 4);
		addItem(player, 9815, 2);
		addItem(player, 57, 6000000);
	}

	// Magic Armor Set
	public void ItemHandler_10473(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(10473, player))
			return;
		removeItem(player, 10473, 1);
		addItem(player, 10470, 2); // Shadow Item - Red Crescent
		addItem(player, 10471, 2); // Shadow Item - Ring of Devotion
		addItem(player, 10472, 1); // Shadow Item - Necklace of Devotion
	}

	private final int[] sweet_list = {
			// Sweet Fruit Cocktail 
			2404, // Might
			2405, // Shield
			2406, // Wind Walk
			2407, // Focus
			2408, // Death Whisper
			2409, // Guidance
			2410, // Bless Shield
			2411, // Bless Body
			2412, // Haste
			2413, // Vampiric Rage
	};

	// Sweet Fruit Cocktail 
	public void ItemHandler_10178(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 10178, 1);
		for(int skill : sweet_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	private final int[] fresh_list = {
			// Fresh Fruit Cocktail 
			2414, // Berserker Spirit
			2411, // Bless Body
			2415, // Magic Barrier
			2405, // Shield
			2406, // Wind Walk
			2416, // Bless Soul
			2417, // Empower
			2418, // Acumen
			2419, // Clarity
	};

	// Fresh Fruit Cocktail 
	public void ItemHandler_10179(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 10179, 1);
		for(int skill : fresh_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Shield Master
	public void ItemHandler_10143(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10143, 1);
		for(int skill : new int[] { 2379, 2380, 2381, 2382, 2383 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Wizard
	public void ItemHandler_10144(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10144, 1);
		for(int skill : new int[] { 2379, 2380, 2381, 2384, 2385 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Healer
	public void ItemHandler_10145(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10145, 1);
		for(int skill : new int[] { 2379, 2380, 2381, 2384, 2386 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Dagger Master
	public void ItemHandler_10146(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10146, 1);
		for(int skill : new int[] { 2379, 2380, 2381, 2388, 2383 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Bow Master
	public void ItemHandler_10147(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10147, 1);
		for(int skill : new int[] { 2379, 2380, 2381, 2389, 2383 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Battleground Spell - Berserker
	public void ItemHandler_10148(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!player.isInZone(ZoneType.Siege))
			return;
		removeItem(player, 10148, 1);
		for(int skill : new int[] { 2390, 2391 })
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Wondrous Cubic
	public void ItemHandler_10632(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(10632, player))
			return;

		int lastuse = 0;
		try
		{
			String var = player.getVar("WondrousCubic");
			if(var != null && !var.equals("null"))
				lastuse = Integer.parseInt(var);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return;
		}

		long now = System.currentTimeMillis() / 1000;
		if(lastuse == 0 || now - lastuse > 86400)
			player.setVar("WondrousCubic", String.valueOf(now));
		else
		{
			long timeleft = 86400 - (now - lastuse);
			long hours = timeleft / 3600;
			long minutes = (timeleft - hours * 3600) / 60;
			long seconds = timeleft - hours * 3600 - minutes * 60;
			if(hours > 0)
				player.sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_HOURS_S3_MINUTES_AND_S4_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addItemName(10632).addNumber(hours).addNumber(minutes).addNumber(seconds));
			else if(minutes > 0)
				player.sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_MINUTES_S3_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addItemName(10632).addNumber(minutes).addNumber(seconds));
			else
				player.sendPacket(new SystemMessage(SystemMessage.THERE_ARE_S2_SECONDS_REMAINING_IN_S1S_REUSE_TIME).addItemName(10632).addNumber(seconds));
			return;
		}

		int chance = Rnd.get(L2Drop.MAX_CHANCE);

		if(chance < 350000) // Rough Blue Cubic Piece            35%
			addItem(player, 10633, 1);
		else if(chance < 550000) // Rough Yellow Cubic Piece     20%
			addItem(player, 10634, 1);
		else if(chance < 650000) // Rough Green Cubic Piece      10%
			addItem(player, 10635, 1);
		else if(chance < 730000) // Rough Red Cubic Piece        8%
			addItem(player, 10636, 1);
		else if(chance < 750000) // Rough White Cubic Piece      2%
			addItem(player, 10637, 1);

		else if(chance < 890000) // Fine Blue Cubic Piece        14%
			addItem(player, 10642, 1);
		else if(chance < 960000) // Fine Yellow Cubic Piece      7%
			addItem(player, 10643, 1);
		else if(chance < 985000) // Fine Green Cubic Piece       2.5%
			addItem(player, 10644, 1);
		else if(chance < 995000) // Fine Red Cubic Piece         1%
			addItem(player, 10645, 1);
		else if(chance <= 1000000) // Fine White Cubic Piece     0.5%
			addItem(player, 10646, 1);
	}

	// Ancient Tome of the Demon
	public void ItemHandler_9599(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(9599, player))
			return;

		int[] list = new int[] { 9600, 9601, 9602 };
		int[] count_min = new int[] { 1, 1, 1 };
		int[] count_max = new int[] { 2, 2, 1 };
		int[] chances = new int[] { 4, 10, 1 };

		if(ctrl)
		{
			long item_count = player.getInventory().getCountOf(9599);
			removeItem(player, 9599, item_count);
			for(int[] res : mass_extract_item_r(item_count, list, count_min, count_max, chances, player))
				addItem(player, res[0], res[1]);
		}
		else
		{
			removeItem(player, 9599, 1);
			extract_item_r(list, count_min, count_max, chances, player);
		}
	}

	public void ItemHandler_13010(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(InstancedZoneManager.getInstance().getTimeToNextEnterInstance("Kamaloka, Hall of the Abyss", player) > 0)
		{
			removeItem(player, 13010, 1);
			player.unsetVar("Kamaloka, Hall of the Abyss");
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(13010));
	}

	public void ItemHandler_13011(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(InstancedZoneManager.getInstance().getTimeToNextEnterInstance("Kamaloka, Hall of the Nightmares", player) > 0)
		{
			removeItem(player, 13011, 1);
			player.unsetVar("Kamaloka, Hall of the Nightmares");
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(13011));
	}

	public void ItemHandler_13012(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(InstancedZoneManager.getInstance().getTimeToNextEnterInstance("Kamaloka, Labyrinth of the Abyss", player) > 0)
		{
			removeItem(player, 13012, 1);
			player.unsetVar("Kamaloka, Labyrinth of the Abyss");
		}
		else
			player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(13012));
	}

	// Baby Panda Agathion Pack
	public void ItemHandler_20069(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20069, player))
			return;
		removeItem(player, 20069, 1);
		addItem(player, 20063, 1);
	}

	// Bamboo Panda Agathion Pack
	public void ItemHandler_20070(L2Player player, Boolean ctrl)
	{
		if(!canBeExtracted(20070, player))
			return;
		removeItem(player, 20070, 1);
		addItem(player, 20064, 1);
	}

	// Sexy Panda Agathion Pack
	public void ItemHandler_20071(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20071, player))
			return;
		removeItem(player, 20071, 1);
		addItem(player, 20065, 1);
	}

	// Agathion of Baby Panda 15 Day Pack
	public void ItemHandler_20072(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20072, player))
			return;
		removeItem(player, 20072, 1);
		addItem(player, 20066, 1);
	}

	// Bamboo Panda Agathion 15 Day Pack
	public void ItemHandler_20073(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20073, player))
			return;
		removeItem(player, 20073, 1);
		addItem(player, 20067, 1);
	}

	// Agathion of Sexy Panda 15 Day Pack
	public void ItemHandler_20074(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20074, player))
			return;
		removeItem(player, 20074, 1);
		addItem(player, 20068, 1);
	}

	// Charming Valentine Gift Set
	public void ItemHandler_20210(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20210, player))
			return;
		removeItem(player, 20210, 1);
		addItem(player, 20212, 1);
	}

	// Naughty Valentine Gift Set
	public void ItemHandler_20211(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20211, player))
			return;
		removeItem(player, 20211, 1);
		addItem(player, 20213, 1);
	}

	// White Maneki Neko Agathion Pack
	public void ItemHandler_20215(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20215, player))
			return;
		removeItem(player, 20215, 1);
		addItem(player, 20221, 1);
	}

	// Black Maneki Neko Agathion Pack
	public void ItemHandler_20216(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20216, player))
			return;
		removeItem(player, 20216, 1);
		addItem(player, 20222, 1);
	}

	// Brown Maneki Neko Agathion Pack
	public void ItemHandler_20217(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20217, player))
			return;
		removeItem(player, 20217, 1);
		addItem(player, 20223, 1);
	}

	// White Maneki Neko Agathion 7-Day Pack
	public void ItemHandler_20218(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20218, player))
			return;
		removeItem(player, 20218, 1);
		addItem(player, 20224, 1);
	}

	// Black Maneki Neko Agathion 7-Day Pack
	public void ItemHandler_20219(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20219, player))
			return;
		removeItem(player, 20219, 1);
		addItem(player, 20225, 1);
	}

	// Brown Maneki Neko Agathion 7-Day Pack
	public void ItemHandler_20220(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20220, player))
			return;
		removeItem(player, 20220, 1);
		addItem(player, 20226, 1);
	}

	// One-Eyed Bat Drove Agathion Pack
	public void ItemHandler_20227(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20227, player))
			return;
		removeItem(player, 20227, 1);
		addItem(player, 20230, 1);
	}

	// One-Eyed Bat Drove Agathion 7-Day Pack
	public void ItemHandler_20228(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20228, player))
			return;
		removeItem(player, 20228, 1);
		addItem(player, 20231, 1);
	}

	// One-Eyed Bat Drove Agathion 7-Day Pack
	public void ItemHandler_20229(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20229, player))
			return;
		removeItem(player, 20229, 1);
		addItem(player, 20232, 1);
	}

	// Pegasus Agathion Pack
	public void ItemHandler_20233(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20233, player))
			return;
		removeItem(player, 20233, 1);
		addItem(player, 20236, 1);
	}

	// Pegasus Agathion 7-Day Pack
	public void ItemHandler_20234(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20234, player))
			return;
		removeItem(player, 20234, 1);
		addItem(player, 20237, 1);
	}

	// Pegasus Agathion 7-Day Pack
	public void ItemHandler_20235(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20235, player))
			return;
		removeItem(player, 20235, 1);
		addItem(player, 20238, 1);
	}

	// Yellow-Robed Tojigong Pack
	public void ItemHandler_20239(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20239, player))
			return;
		removeItem(player, 20239, 1);
		addItem(player, 20245, 1);
	}

	// Blue-Robed Tojigong Pack
	public void ItemHandler_20240(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20240, player))
			return;
		removeItem(player, 20240, 1);
		addItem(player, 20246, 1);
	}

	// Green-Robed Tojigong Pack
	public void ItemHandler_20241(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20241, player))
			return;
		removeItem(player, 20241, 1);
		addItem(player, 20247, 1);
	}

	// Yellow-Robed Tojigong 7-Day Pack
	public void ItemHandler_20242(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20242, player))
			return;
		removeItem(player, 20242, 1);
		addItem(player, 20248, 1);
	}

	// Blue-Robed Tojigong 7-Day Pack
	public void ItemHandler_20243(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20243, player))
			return;
		removeItem(player, 20243, 1);
		addItem(player, 20249, 1);
	}

	// Green-Robed Tojigong 7-Day Pack
	public void ItemHandler_20244(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20244, player))
			return;
		removeItem(player, 20244, 1);
		addItem(player, 20250, 1);
	}

	// Bugbear Agathion Pack
	public void ItemHandler_20251(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20251, player))
			return;
		removeItem(player, 20251, 1);
		addItem(player, 20252, 1);
	}

	// Agathion of Love Pack (Event)
	public void ItemHandler_20254(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20254, player))
			return;
		removeItem(player, 20254, 1);
		addItem(player, 20253, 1);
	}

	// Gold Afro Hair Pack
	public void ItemHandler_20278(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20278, player))
			return;
		removeItem(player, 20278, 1);
		addItem(player, 20275, 1);
	}

	// Pink Afro Hair Pack
	public void ItemHandler_20279(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20279, player))
			return;
		removeItem(player, 20279, 1);
		addItem(player, 20276, 1);
	}

	// Plaipitak Agathion Pack
	public void ItemHandler_20041(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20041, player))
			return;
		removeItem(player, 20041, 1);
		addItem(player, 20012, 1);
	}

	// Plaipitak Agathion 30-Day Pack
	public void ItemHandler_20042(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20042, player))
			return;
		removeItem(player, 20042, 1);
		addItem(player, 20013, 1);
	}

	// Plaipitak Agathion 30-Day Pack
	public void ItemHandler_20043(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20043, player))
			return;
		removeItem(player, 20043, 1);
		addItem(player, 20014, 1);
	}

	// Plaipitak Agathion 30-Day Pack
	public void ItemHandler_20044(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20044, player))
			return;
		removeItem(player, 20044, 1);
		addItem(player, 20015, 1);
	}

	// Majo Agathion Pack
	public void ItemHandler_20035(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20035, player))
			return;
		removeItem(player, 20035, 1);
		addItem(player, 20006, 1);
	}

	// Gold Crown Majo Agathion Pack
	public void ItemHandler_20036(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20036, player))
			return;
		removeItem(player, 20036, 1);
		addItem(player, 20007, 1);
	}

	// Black Crown Majo Agathion Pack
	public void ItemHandler_20037(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20037, player))
			return;
		removeItem(player, 20037, 1);
		addItem(player, 20008, 1);
	}

	// Majo Agathion 30-Day Pack
	public void ItemHandler_20038(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20038, player))
			return;
		removeItem(player, 20038, 1);
		addItem(player, 20009, 1);
	}

	// Gold Crown Majo Agathion 30-Day Pack
	public void ItemHandler_20039(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20039, player))
			return;
		removeItem(player, 20039, 1);
		addItem(player, 20010, 1);
	}

	// Black Crown Majo Agathion 30-Day Pack
	public void ItemHandler_20040(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20040, player))
			return;
		removeItem(player, 20040, 1);
		addItem(player, 20011, 1);
	}

	// Kat the Cat Hat Pack
	public void ItemHandler_20060(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20060, player))
			return;
		removeItem(player, 20060, 1);
		addItem(player, 20031, 1);
	}

	// Skull Hat Pack
	public void ItemHandler_20061(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20061, player))
			return;
		removeItem(player, 20061, 1);
		addItem(player, 20032, 1);
	}

	// ****** Start Item Mall ******
	// Small fortuna box
	public void ItemHandler_22000(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22000, player))
			return;
		int[] list = new int[] { 22006, 22007, 22008, 22014, 22022, 22023, 22024, 8743, 8744, 8745, 8753, 8754, 8755, 22025 };
		int[] counts = new int[] { 3, 2, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 5 };
		int[] chances = new int[] { 20, 14, 6, 1, 3, 6, 5, 10, 8, 7, 2, 1, 1, 12 };
		removeItem(player, 22000, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Middle fortuna box
	public void ItemHandler_22001(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22001, player))
			return;
		int[] list = new int[] { 22007, 22008, 22009, 22014, 22015, 22022, 22023, 22024, 8746, 8747, 8748, 8756, 8757, 8758, 22025 };
		int[] counts = new int[] { 3, 2, 1, 1, 1, 5, 5, 2, 1, 1, 1, 1, 1, 1, 10 };
		int[] chances = new int[] { 27, 9, 5, 1, 1, 3, 7, 5, 9, 7, 6, 2, 2, 1, 12 };
		removeItem(player, 22001, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Large fortuna box
	public void ItemHandler_22002(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22002, player))
			return;
		int[] list = new int[] { 22008, 22009, 22014, 22015, 22018, 22019, 22022, 22023, 22024, 8749, 8750, 8751, 8759, 8760, 8761, 22025 };
		int[] counts = new int[] { 2, 1, 1, 1, 1, 1, 10, 10, 5, 1, 1, 1, 1, 1, 1, 20 };
		int[] chances = new int[] { 27, 15, 1, 1, 1, 1, 3, 7, 4, 9, 8, 6, 2, 1, 1, 12 };
		removeItem(player, 22002, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Small fortuna cube
	public void ItemHandler_22003(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22003, player))
			return;
		int[] list = new int[] { 22010, 22011, 22012, 22016, 22022, 22023, 22024, 8743, 8744, 8745, 8753, 8754, 8755, 22025 };
		int[] counts = new int[] { 3, 2, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 5 };
		int[] chances = new int[] { 20, 13, 6, 1, 3, 7, 6, 9, 8, 6, 2, 1, 1, 13 };
		removeItem(player, 22003, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Middle fortuna cube
	public void ItemHandler_22004(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22004, player))
			return;
		int[] list = new int[] { 22011, 22012, 22013, 22016, 22017, 22022, 22023, 22024, 8746, 8747, 8748, 8756, 8757, 8758, 22025 };
		int[] counts = new int[] { 3, 2, 1, 1, 1, 5, 5, 2, 1, 1, 1, 1, 1, 1, 10 };
		int[] chances = new int[] { 26, 8, 4, 1, 1, 3, 7, 5, 9, 8, 6, 2, 1, 1, 12 };
		removeItem(player, 22004, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Large fortuna cube
	public void ItemHandler_22005(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22005, player))
			return;
		int[] list = new int[] { 22012, 22013, 22016, 22017, 22020, 22021, 22022, 22023, 22024, 8749, 8750, 8751, 8759, 8760, 8761, 22025 };
		int[] counts = new int[] { 2, 1, 1, 1, 1, 1, 10, 10, 5, 1, 1, 1, 1, 1, 1, 20 };
		int[] chances = new int[] { 26, 14, 1, 1, 1, 1, 4, 8, 4, 10, 8, 6, 2, 2, 1, 13 };
		removeItem(player, 22005, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Beast Soulshot Pack
	public void ItemHandler_20326(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20326, player))
			return;
		removeItem(player, 20326, 1);
		addItem(player, 20332, 5000);
	}

	// Beast Spiritshot Pack
	public void ItemHandler_20327(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20327, player))
			return;
		removeItem(player, 20327, 1);
		addItem(player, 20333, 5000);
	}

	// Blessed Beast Spiritshot Pack
	public void ItemHandler_20328(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20328, player))
			return;
		removeItem(player, 20328, 1);
		addItem(player, 6647, 5000);
	}
		
	// Beast Soulshot Large Pack
	public void ItemHandler_20329(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20329, player))
			return;
		removeItem(player, 20329, 1);
		addItem(player, 20332, 10000);
	}
	
	// Beast Spiritshot Large Pack
	public void ItemHandler_20330(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20330, player))
			return;
		removeItem(player, 20330, 1);
		addItem(player, 20333, 10000);
	}

	// Blessed Beast Spiritshot Large Pack
	public void ItemHandler_20331(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20331, player))
			return;
		removeItem(player, 20331, 1);
		addItem(player, 6647, 10000);
	}
	
	// Light Purple Maned Horse Bracelet 30-Day Pack
	public void ItemHandler_20059(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20059, player))
			return;
		removeItem(player, 20059, 1);
		addItem(player, 20030, 1);
	}

	// Steam Beatle Mounting Bracelet 7 Day Pack
	public void ItemHandler_20494(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20494, player))
			return;
		removeItem(player, 20494, 1);
		addItem(player, 20449, 1);
	}

	// Light Purple Maned Horse Mounting Bracelet 7 Day Pack
	public void ItemHandler_20493(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20493, player))
			return;
		removeItem(player, 20493, 1);
		addItem(player, 20448, 1);
	}

	// Steam Beatle Mounting Bracelet Pack
	public void ItemHandler_20395(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20395, player))
			return;
		removeItem(player, 20395, 1);
		addItem(player, 20396, 1);
	}

	// Pumpkin Transformation Stick 7 Day Pack
	public void ItemHandler_13281(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13281, player))
			return;
		removeItem(player, 13281, 1);
		addItem(player, 13253, 1);
	}

	// Kat the Cat Hat 7-Day Pack
	public void ItemHandler_13282(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13282, player))
			return;
		removeItem(player, 13282, 1);
		addItem(player, 13239, 1);
	}

	// Feline Queen Hat 7-Day Pack
	public void ItemHandler_13283(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13283, player))
			return;
		removeItem(player, 13283, 1);
		addItem(player, 13240, 1);
	}

	// Monster Eye Hat 7-Day Pack
	public void ItemHandler_13284(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13284, player))
			return;
		removeItem(player, 13284, 1);
		addItem(player, 13241, 1);
	}

	// Brown Bear Hat 7-Day Pack
	public void ItemHandler_13285(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13285, player))
			return;
		removeItem(player, 13285, 1);
		addItem(player, 13242, 1);
	}

	// Fungus Hat 7-Day Pack
	public void ItemHandler_13286(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13286, player))
			return;
		removeItem(player, 13286, 1);
		addItem(player, 13243, 1);
	}

	// Skull Hat 7-Day Pack
	public void ItemHandler_13287(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13287, player))
			return;
		removeItem(player, 13287, 1);
		addItem(player, 13244, 1);
	}

	// Ornithomimus Hat 7-Day Pack
	public void ItemHandler_13288(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13288, player))
			return;
		removeItem(player, 13288, 1);
		addItem(player, 13245, 1);
	}

	// Feline King Hat 7-Day Pack
	public void ItemHandler_13289(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13289, player))
			return;
		removeItem(player, 13289, 1);
		addItem(player, 13246, 1);
	}

	// Kai the Cat Hat 7-Day Pack
	public void ItemHandler_13290(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13290, player))
			return;
		removeItem(player, 13290, 1);
		addItem(player, 13247, 1);
	}

	// Sudden Agathion 7 Day Pack
	public void ItemHandler_14267(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14267, player))
			return;
		removeItem(player, 14267, 1);
		addItem(player, 14093, 1);
	}

	// Shiny Agathion 7 Day Pack
	public void ItemHandler_14268(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14268, player))
			return;
		removeItem(player, 14268, 1);
		addItem(player, 14094, 1);
	}

	// Sobbing Agathion 7 Day Pack
	public void ItemHandler_14269(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14269, player))
			return;
		removeItem(player, 14269, 1);
		addItem(player, 14095, 1);
	}

	// Agathion of Love 7-Day Pack
	public void ItemHandler_13280(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13280, player))
			return;
		removeItem(player, 13280, 1);
		addItem(player, 20201, 1);
	}

	// A Scroll Bundle of Fighter
	public void ItemHandler_22087(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22087, player))
			return;
		removeItem(player, 22087, 1);
		addItem(player, 22039, 1);
		addItem(player, 22040, 1);
		addItem(player, 22041, 1);
		addItem(player, 22042, 1);
		addItem(player, 22043, 1);
		addItem(player, 22044, 1);
		addItem(player, 22047, 1);
		addItem(player, 22048, 1);
	}

	// A Scroll Bundle of Mage
	public void ItemHandler_22088(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22088, player))
			return;
		removeItem(player, 22088, 1);
		addItem(player, 22045, 1);
		addItem(player, 22046, 1);
		addItem(player, 22048, 1);
		addItem(player, 22049, 1);
		addItem(player, 22050, 1);
		addItem(player, 22051, 1);
		addItem(player, 22052, 1);
		addItem(player, 22053, 1);
	}
	
	// Hunting Helper Exchange Coupon 1-Sheet Pack
	public void ItemHandler_13276(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13276, player))
			return;
		removeItem(player, 13276, 1);
		addItem(player, 13273, 1);
	}
	
	// Hunting Helper Exchange Coupon - 3 sheet
	public void ItemHandler_13275(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13275, player))
			return;
		removeItem(player, 13275, 1);
		addItem(player, 13273, 3);
	}
	
	// Wrapped daisy hairpin
	public void ItemHandler_22124(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22124, player))
			return;
		removeItem(player, 22124, 1);
		addItem(player, 22156, 1);
	}
	
	// Wrapped forget me not hairpin
	public void ItemHandler_22125(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22125, player))
			return;
		removeItem(player, 22125, 1);
		addItem(player, 22157, 1);
	}
	
	// Wrapped outlaw eyepatch
	public void ItemHandler_22126(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22126, player))
			return;
		removeItem(player, 22126, 1);
		addItem(player, 22158, 1);
	}
	
	// Wrapped pirate eyepatch
	public void ItemHandler_22127(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22127, player))
			return;
		removeItem(player, 22127, 1);
		addItem(player, 22159, 1);
	}
	
	// Wrapped Monocle
	public void ItemHandler_22128(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22128, player))
			return;
		removeItem(player, 22128, 1);
		addItem(player, 22160, 1);
	}
	
	// Wrapped Red Mask of Victory
	public void ItemHandler_22129(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22129, player))
			return;
		removeItem(player, 22129, 1);
		addItem(player, 22161, 1);
	}
	
	// Wrapped Red Horn of Victory
	public void ItemHandler_22130(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22130, player))
			return;
		removeItem(player, 22130, 1);
		addItem(player, 22162, 1);
	}
	
	// Wrapped Party Mask
	public void ItemHandler_22131(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22131, player))
			return;
		removeItem(player, 22131, 1);
		addItem(player, 22163, 1);
	}
	
	// Wrapped Red Party Mask
	public void ItemHandler_22132(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22132, player))
			return;
		removeItem(player, 22132, 1);
		addItem(player, 22164, 1);
	}
	
	// Wrapped Cat Ear
	public void ItemHandler_22133(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22133, player))
			return;
		removeItem(player, 22133, 1);
		addItem(player, 22165, 1);
	}
	
	// Wrapped Noblewoman's Hairpin
	public void ItemHandler_22134(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22134, player))
			return;
		removeItem(player, 22134, 1);
		addItem(player, 22166, 1);
	}
	
	// Wrapped Raccoon Ear
	public void ItemHandler_22135(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22135, player))
			return;
		removeItem(player, 22135, 1);
		addItem(player, 22167, 1);
	}
	
	// Wrapped Rabbit Ear
	public void ItemHandler_22136(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22136, player))
			return;
		removeItem(player, 22136, 1);
		addItem(player, 22168, 1);
	}
	
	// Wrapped Little Angel's Wings
	public void ItemHandler_22137(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22137, player))
			return;
		removeItem(player, 22137, 1);
		addItem(player, 22169, 1);
	}
	
	// Wrapped Fairy's Tentacle
	public void ItemHandler_22138(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22138, player))
			return;
		removeItem(player, 22138, 1);
		addItem(player, 22170, 1);
	}
	
	// Wrapped Dandy's Chapeau
	public void ItemHandler_22139(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22139, player))
			return;
		removeItem(player, 22139, 1);
		addItem(player, 22171, 1);
	}
	
	// Wrapped Artisan's Goggles
	public void ItemHandler_22140(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22140, player))
			return;
		removeItem(player, 22140, 1);
		addItem(player, 22172, 1);
	}
	
	// Sudden Agathion 7 Day Pack
	public void ItemHandler_139(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(139, player))
			return;
		removeItem(player, 139, 1);
		addItem(player, 14093, 1);
	}
	
	// Shiny Agathion 7 Day Pack
	public void ItemHandler_140(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(140, player))
			return;
		removeItem(player, 140, 1);
		addItem(player, 14094, 1);
	}
	
	// Sobbing Agathion 7 Day Pack
	public void ItemHandler_141(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(141, player))
			return;
		removeItem(player, 141, 1);
		addItem(player, 14095, 1);
	}
	
	
	
	// Free Teleport Spellbook 5 Sheet Bundle
	public void ItemHandler_20181(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20181, player))
			return;
		removeItem(player, 20181, 1);
		addItem(player, 20025, 5);
	}
	
	// Free Teleport Spellbook 10 Sheet Bundle
	public void ItemHandler_20182(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20182, player))
			return;
		removeItem(player, 20182, 1);
		addItem(player, 20025, 10);
	}
	
	// Free Teleport Flag 5 Sheet Bundle
	public void ItemHandler_20183(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20183, player))
			return;
		removeItem(player, 20183, 1);
		addItem(player, 20033, 5);
	}
	
	// Free Teleport Flag 10 Sheet Bundle
	public void ItemHandler_20184(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20184, player))
			return;
		removeItem(player, 20184, 1);
		addItem(player, 20033, 10);
	}
	
	// Extra Entrance Pass - Kamaloka (Hall of the Abyss) 5 Sheet Bundle
	public void ItemHandler_20185(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20185, player))
			return;
		removeItem(player, 20185, 1);
		addItem(player, 20026, 5);
	}
	
	// Extra Entrance Pass - Kamaloka (Hall of the Abyss) 10 Sheet Bundle
	public void ItemHandler_20187(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20187, player))
			return;
		removeItem(player, 20187, 1);
		addItem(player, 20026, 10);
	}
	
	// Extra Entrance Pass - Near Kamaloka 5 Sheet Bundle
	public void ItemHandler_20179(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20179, player))
			return;
		removeItem(player, 20179, 1);
		addItem(player, 20027, 5);
	}
	
	// Extra Entrance Pass - Near Kamaloka 10 Sheet Bundle
	public void ItemHandler_20180(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20180, player))
			return;
		removeItem(player, 20180, 1);
		addItem(player, 20027, 10);
	}
	
	// Extra Entrance Pass - Kamaloka (Labyrinth of the Abyss) 5 Sheet Bundle
	public void ItemHandler_20186(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20186, player))
			return;
		removeItem(player, 20186, 1);
		addItem(player, 20028, 5);
	}
	
	// Extra Entrance Pass - Kamaloka (Labyrinth of the Abyss) 10 Sheet Bundle
	public void ItemHandler_20188(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20188, player))
			return;
		removeItem(player, 20188, 1);
		addItem(player, 20028, 10);
	}
	
	// Sweet Fruit Cocktail 
	public void ItemHandler_20393(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 20393, 1);
		for(int skill : sweet_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Fresh Fruit Cocktail 
	public void ItemHandler_20394(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 20394, 1);
		for(int skill : fresh_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}
	// ****** End Item Mall ******

	// Pathfinder's Reward - D-Grade
	public void ItemHandler_13003(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13003, player))
			return;
		removeItem(player, 13003, 1);
		if(Rnd.chance(50))
			addItem(player, 955, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - C-Grade
	public void ItemHandler_13004(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13004, player))
			return;
		removeItem(player, 13004, 1);
		if(Rnd.chance(50))
			addItem(player, 951, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - B-Grade
	public void ItemHandler_13005(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13005, player))
			return;
		removeItem(player, 13005, 1);
		if(Rnd.chance(50))
			addItem(player, 947, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - A-Grade
	public void ItemHandler_13006(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13006, player))
			return;
		removeItem(player, 13006, 1);
		if(Rnd.chance(50))
			addItem(player, 729, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - S-Grade
	public void ItemHandler_13007(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13007, player))
			return;
		removeItem(player, 13007, 1);
		if(Rnd.chance(50))
			addItem(player, 959, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - AU Karm
	public void ItemHandler_13270(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13270, player))
			return;
		removeItem(player, 13270, 1);
		if(Rnd.chance(50))
			addItem(player, 13236, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - AR Karm
	public void ItemHandler_13271(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13271, player))
			return;
		removeItem(player, 13271, 1);
		if(Rnd.chance(50))
			addItem(player, 13237, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Pathfinder's Reward - AE Karm
	public void ItemHandler_13272(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13272, player))
			return;
		removeItem(player, 13272, 1);
		if(Rnd.chance(50))
			addItem(player, 13238, 1);
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// ****** Belts ******
	// Gludio Supply Box - Belt: Grade B, C
	public void ItemHandler_13713(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13713, player))
			return;
		removeItem(player, 13713, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Belt: Grade B, C
	public void ItemHandler_13714(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13714, player))
			return;
		removeItem(player, 13714, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Belt: Grade B, C
	public void ItemHandler_13715(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13715, player))
			return;
		removeItem(player, 13715, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Belt: Grade B, C
	public void ItemHandler_13716(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13716, player))
			return;
		removeItem(player, 13716, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Belt: Grade B, C
	public void ItemHandler_13717(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13717, player))
			return;
		removeItem(player, 13717, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Belt: Grade B, C
	public void ItemHandler_13718(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13718, player))
			return;
		removeItem(player, 13718, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Belt: Grade B, C
	public void ItemHandler_13719(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13719, player))
			return;
		removeItem(player, 13719, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Belt: Grade B, C
	public void ItemHandler_13720(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13720, player))
			return;
		removeItem(player, 13720, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Belt: Grade B, C
	public void ItemHandler_13721(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13721, player))
			return;
		removeItem(player, 13721, 1);
		if(Rnd.chance(50))
			addItem(player, 13894, 1); // Cloth Belt
		if(Rnd.chance(50))
			addItem(player, 13895, 1); // Leather Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Gludio Supply Box - Belt: Grade S, A
	public void ItemHandler_14549(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14549, player))
			return;
		removeItem(player, 14549, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Belt: Grade S, A
	public void ItemHandler_14550(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14550, player))
			return;
		removeItem(player, 14550, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Belt: Grade S, A
	public void ItemHandler_14551(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14551, player))
			return;
		removeItem(player, 14551, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Belt: Grade S, A
	public void ItemHandler_14552(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14552, player))
			return;
		removeItem(player, 14552, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Belt: Grade S, A
	public void ItemHandler_14553(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14553, player))
			return;
		removeItem(player, 14553, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Belt: Grade S, A
	public void ItemHandler_14554(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14554, player))
			return;
		removeItem(player, 14554, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Belt: Grade S, A
	public void ItemHandler_14555(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14555, player))
			return;
		removeItem(player, 14555, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Belt: Grade S, A
	public void ItemHandler_14556(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14556, player))
			return;
		removeItem(player, 14556, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Belt: Grade S, A
	public void ItemHandler_14557(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14557, player))
			return;
		removeItem(player, 14557, 1);
		if(Rnd.chance(50))
			addItem(player, 13896, 1); // Iron Belt
		if(Rnd.chance(50))
			addItem(player, 13897, 1); // Mithril Belt
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// ****** Magic Pins ******
	// Gludio Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13695(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13695, player))
			return;
		removeItem(player, 13695, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13696(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13696, player))
			return;
		removeItem(player, 13696, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13697(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13697, player))
			return;
		removeItem(player, 13697, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13698(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13698, player))
			return;
		removeItem(player, 13698, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13699(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13699, player))
			return;
		removeItem(player, 13699, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13700(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13700, player))
			return;
		removeItem(player, 13700, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13701(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13701, player))
			return;
		removeItem(player, 13701, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13702(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13702, player))
			return;
		removeItem(player, 13702, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Pin: Grade B, C
	public void ItemHandler_13703(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13703, player))
			return;
		removeItem(player, 13703, 1);
		if(Rnd.chance(50))
			addItem(player, 13898, 1); // Sealed Magic Pin (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13899, 1); // Sealed Magic Pin (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Gludio Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14531(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14531, player))
			return;
		removeItem(player, 14531, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14532(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14532, player))
			return;
		removeItem(player, 14532, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14533(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14533, player))
			return;
		removeItem(player, 14533, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14534(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14534, player))
			return;
		removeItem(player, 14534, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14535(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14535, player))
			return;
		removeItem(player, 14535, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14536(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14536, player))
			return;
		removeItem(player, 14536, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14537(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14537, player))
			return;
		removeItem(player, 14537, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14538(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14538, player))
			return;
		removeItem(player, 14538, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Pin: Grade S, A
	public void ItemHandler_14539(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14539, player))
			return;
		removeItem(player, 14539, 1);
		if(Rnd.chance(50))
			addItem(player, 13900, 1); // Sealed Magic Pin (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13901, 1); // Sealed Magic Pin (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// ****** Magic Pouchs ******
	// Gludio Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13704(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13704, player))
			return;
		removeItem(player, 13704, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13705(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13705, player))
			return;
		removeItem(player, 13705, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13706(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13706, player))
			return;
		removeItem(player, 13706, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13707(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13707, player))
			return;
		removeItem(player, 13707, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13708(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13708, player))
			return;
		removeItem(player, 13708, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13709(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13709, player))
			return;
		removeItem(player, 13709, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13710(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13710, player))
			return;
		removeItem(player, 13710, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13711(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13711, player))
			return;
		removeItem(player, 13711, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Pouch: Grade B, C
	public void ItemHandler_13712(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13712, player))
			return;
		removeItem(player, 13712, 1);
		if(Rnd.chance(50))
			addItem(player, 13918, 1); // Sealed Magic Pouch (C-Grade)
		if(Rnd.chance(50))
			addItem(player, 13919, 1); // Sealed Magic Pouch (B-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Gludio Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14540(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14540, player))
			return;
		removeItem(player, 14540, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14541(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14541, player))
			return;
		removeItem(player, 14541, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14542(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14542, player))
			return;
		removeItem(player, 14542, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14543(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14543, player))
			return;
		removeItem(player, 14543, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14544(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14544, player))
			return;
		removeItem(player, 14544, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14545(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14545, player))
			return;
		removeItem(player, 14545, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14546(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14546, player))
			return;
		removeItem(player, 14546, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14547(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14547, player))
			return;
		removeItem(player, 14547, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Pouch: Grade S, A
	public void ItemHandler_14548(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14548, player))
			return;
		removeItem(player, 14548, 1);
		if(Rnd.chance(50))
			addItem(player, 13920, 1); // Sealed Magic Pouch (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 13921, 1); // Sealed Magic Pouch (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// ****** Magic Rune Clip ******
	// Gludio Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14884(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14884, player))
			return;
		removeItem(player, 14884, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14885(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14885, player))
			return;
		removeItem(player, 14885, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14886(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14886, player))
			return;
		removeItem(player, 14886, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14887(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14887, player))
			return;
		removeItem(player, 14887, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14888(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14888, player))
			return;
		removeItem(player, 14888, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14889(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14889, player))
			return;
		removeItem(player, 14889, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14890(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14890, player))
			return;
		removeItem(player, 14890, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14891(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14891, player))
			return;
		removeItem(player, 14891, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Rune Clip: Grade S, A
	public void ItemHandler_14892(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14892, player))
			return;
		removeItem(player, 14892, 1);
		if(Rnd.chance(50))
			addItem(player, 14902, 1); // Sealed Magic Rune Clip (A-Grade)
		if(Rnd.chance(50))
			addItem(player, 14903, 1); // Sealed Magic Rune Clip (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// ****** Magic Ornament ******
	// Gludio Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14893(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14893, player))
			return;
		removeItem(player, 14893, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Dion Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14894(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14894, player))
			return;
		removeItem(player, 14894, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Giran Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14895(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14895, player))
			return;
		removeItem(player, 14895, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Oren Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14896(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14896, player))
			return;
		removeItem(player, 14896, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Aden Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14897(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14897, player))
			return;
		removeItem(player, 14897, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Innadril Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14898(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14898, player))
			return;
		removeItem(player, 14898, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Goddard Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14899(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14899, player))
			return;
		removeItem(player, 14899, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Rune Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14900(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14900, player))
			return;
		removeItem(player, 14900, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Schuttgart Supply Box - Magic Ornament: Grade S, A
	public void ItemHandler_14901(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14901, player))
			return;
		removeItem(player, 14901, 1);
		if(Rnd.chance(20))
			addItem(player, 14904, 1); // Sealed Magic Ornament (A-Grade)
		if(Rnd.chance(20))
			addItem(player, 14905, 1); // Sealed Magic Ornament (S-Grade)
		else
			player.sendPacket(Msg.THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT);
	}

	// Gift from Santa Claus
	public void ItemHandler_14616(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14616, player))
			return;
		removeItem(player, 14616, 1);

		// Santa Claus' Weapon Exchange Ticket - 12 Hour Expiration Period
		addItem(player, 20107, 1);

		// Christmas Red Sock
		addItem(player, 14612, 1);

		// Special Christmas Tree
		if(Rnd.chance(30))
			addItem(player, 5561, 1);

		// Christmas Tree
		if(Rnd.chance(50))
			addItem(player, 5560, 1);

		// Agathion Seal Bracelet - Rudolph (постоянный предмет)
		if(getItemCount(player, 10606) == 0 && Rnd.chance(5))
			addItem(player, 10606, 1);

		// Agathion Seal Bracelet: Rudolph - 30 дней со скилом на виталити
		if(getItemCount(player, 20094) == 0 && Rnd.chance(3))
			addItem(player, 20094, 1);

		// Chest of Experience (Event)
		if(Rnd.chance(30))
			addItem(player, 20575, 1);
	}

	// Christmas Red Sock
	public void ItemHandler_14612(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 14612, 1);
		player.broadcastPacket(new MagicSkillUse(player, player, 23017, 1, 0, 0));
		player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(23017, 1));
	}

	// Chest of Experience (Event)
	public void ItemHandler_20575(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20575, player))
			return;
		removeItem(player, 20575, 1);
		addItem(player, 20335, 1); // Rune of Experience: 30% - 5 hour limited time
		addItem(player, 20341, 1); // Rune of SP 30% - 5 Hour Expiration Period
	}

	// Nepal Snow Agathion Pack
	public void ItemHandler_20804(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20804, player))
			return;
		removeItem(player, 20804, 1);
		addItem(player, 20782, 1);
	}

	// Nepal Snow Agathion 7-Day Pack - Snow's Haste
	public void ItemHandler_20807(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20807, player))
			return;
		removeItem(player, 20807, 1);
		addItem(player, 20785, 1);
	}

	// Round Ball Snow Agathion Pack
	public void ItemHandler_20805(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20805, player))
			return;
		removeItem(player, 20805, 1);
		addItem(player, 20783, 1);
	}

	// Round Ball Snow Agathion 7-Day Pack - Snow's Acumen
	public void ItemHandler_20808(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20808, player))
			return;
		removeItem(player, 20808, 1);
		addItem(player, 20786, 1);
	}

	// Ladder Snow Agathion Pack
	public void ItemHandler_20806(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20806, player))
			return;
		removeItem(player, 20806, 1);
		addItem(player, 20784, 1);
	}

	// Ladder Snow Agathion 7-Day Pack - Snow's Wind Walk
	public void ItemHandler_20809(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20809, player))
			return;
		removeItem(player, 20809, 1);
		addItem(player, 20787, 1);
	}

	// Iken Agathion Pack
	public void ItemHandler_20842(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20842, player))
			return;
		removeItem(player, 20842, 1);
		addItem(player, 20818, 1);
	}

	// Iken Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20843(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20843, player))
			return;
		removeItem(player, 20843, 1);
		addItem(player, 20819, 1);
	}

	// Lana Agathion Pack
	public void ItemHandler_20844(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20844, player))
			return;
		removeItem(player, 20844, 1);
		addItem(player, 20820, 1);
	}

	// Lana Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20845(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20845, player))
			return;
		removeItem(player, 20845, 1);
		addItem(player, 20821, 1);
	}

	// Gnocian Agathion Pack
	public void ItemHandler_20846(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20846, player))
			return;
		removeItem(player, 20846, 1);
		addItem(player, 20822, 1);
	}

	// Gnocian Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20847(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20847, player))
			return;
		removeItem(player, 20847, 1);
		addItem(player, 20823, 1);
	}

	// Orodriel Agathion Pack
	public void ItemHandler_20848(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20848, player))
			return;
		removeItem(player, 20848, 1);
		addItem(player, 20824, 1);
	}

	// Orodriel Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20849(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20849, player))
			return;
		removeItem(player, 20849, 1);
		addItem(player, 20825, 1);
	}

	// Lakinos Agathion Pack
	public void ItemHandler_20850(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20850, player))
			return;
		removeItem(player, 20850, 1);
		addItem(player, 20826, 1);
	}

	// Lakinos Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20851(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20851, player))
			return;
		removeItem(player, 20851, 1);
		addItem(player, 20827, 1);
	}

	// Mortia Agathion Pack
	public void ItemHandler_20852(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20852, player))
			return;
		removeItem(player, 20852, 1);
		addItem(player, 20828, 1);
	}

	// Mortia Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20853(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20853, player))
			return;
		removeItem(player, 20853, 1);
		addItem(player, 20829, 1);
	}

	// Hayance Agathion Pack
	public void ItemHandler_20854(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20854, player))
			return;
		removeItem(player, 20854, 1);
		addItem(player, 20830, 1);
	}

	// Hayance Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20855(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20855, player))
			return;
		removeItem(player, 20855, 1);
		addItem(player, 20831, 1);
	}

	// Meruril Agathion Pack
	public void ItemHandler_20856(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20856, player))
			return;
		removeItem(player, 20856, 1);
		addItem(player, 20832, 1);
	}

	// Meruril Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20857(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20857, player))
			return;
		removeItem(player, 20857, 1);
		addItem(player, 20833, 1);
	}

	// Taman ze Lapatui Agathion Pack
	public void ItemHandler_20858(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20858, player))
			return;
		removeItem(player, 20858, 1);
		addItem(player, 20834, 1);
	}

	// Taman ze Lapatui Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20859(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20859, player))
			return;
		removeItem(player, 20859, 1);
		addItem(player, 20835, 1);
	}

	// Kaurin Agathion Pack
	public void ItemHandler_20860(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20860, player))
			return;
		removeItem(player, 20860, 1);
		addItem(player, 20836, 1);
	}

	// Kaurin Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20861(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20861, player))
			return;
		removeItem(player, 20861, 1);
		addItem(player, 20837, 1);
	}

	// Ahertbein Agathion Pack
	public void ItemHandler_20862(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20862, player))
			return;
		removeItem(player, 20862, 1);
		addItem(player, 20838, 1);
	}

	// Ahertbein Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20863(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20863, player))
			return;
		removeItem(player, 20863, 1);
		addItem(player, 20839, 1);
	}

	// Naonin Agathion Pack
	public void ItemHandler_20864(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20864, player))
			return;
		removeItem(player, 20864, 1);
		addItem(player, 20840, 1);
	}

	// Rocket Gun Hat Pack Continuous Fireworks
	public void ItemHandler_20811(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20811, player))
			return;
		removeItem(player, 20811, 1);
		addItem(player, 20789, 1);
	}

	// Yellow Paper Hat 7-Day Pack Bless the Body
	public void ItemHandler_20812(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20812, player))
			return;
		removeItem(player, 20812, 1);
		addItem(player, 20790, 1);
	}

	// Pink Paper Mask Set 7-Day Pack Bless the Soul
	public void ItemHandler_20813(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20813, player))
			return;
		removeItem(player, 20813, 1);
		addItem(player, 20791, 1);
	}

	// Flavorful Cheese Hat Pack
	public void ItemHandler_20814(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20814, player))
			return;
		removeItem(player, 20814, 1);
		addItem(player, 20792, 1);
	}

	// Sweet Cheese Hat Pack
	public void ItemHandler_20815(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20815, player))
			return;
		removeItem(player, 20815, 1);
		addItem(player, 20793, 1);
	}

	// Flavorful Cheese Hat 7-Day Pack Scent of Flavorful Cheese
	public void ItemHandler_20816(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20816, player))
			return;
		removeItem(player, 20816, 1);
		addItem(player, 20794, 1);
	}

	// Sweet Cheese Hat 7-Day Pack Scent of Sweet Cheese
	public void ItemHandler_20817(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20817, player))
			return;
		removeItem(player, 20817, 1);
		addItem(player, 20795, 1);
	}

	// Flame Box Pack
	public void ItemHandler_20810(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20810, player))
			return;
		removeItem(player, 20810, 1);
		addItem(player, 20725, 1);
	}

	// Naonin Agathion 7-Day Pack Prominent Outsider Adventurer's Ability
	public void ItemHandler_20865(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20865, player))
			return;
		removeItem(player, 20865, 1);
		addItem(player, 20841, 1);
	}

	// Shiny Mask of Giant Hercules 7 day Pack
	public void ItemHandler_20748(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20748, player))
			return;
		removeItem(player, 20748, 1);
		addItem(player, 20743, 1);
	}

	// Shiny Mask of Silent Scream 7 day Pack
	public void ItemHandler_20749(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20749, player))
			return;
		removeItem(player, 20749, 1);
		addItem(player, 20744, 1);
	}

	// Shiny Spirit of Wrath Mask 7 day Pack
	public void ItemHandler_20750(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20750, player))
			return;
		removeItem(player, 20750, 1);
		addItem(player, 20745, 1);
	}

	// Shiny Undecaying Corpse Mask 7 Day Pack
	public void ItemHandler_20751(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20751, player))
			return;
		removeItem(player, 20751, 1);
		addItem(player, 20746, 1);
	}

	// Shiny Planet X235 Alien Mask 7 day Pack
	public void ItemHandler_20752(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20752, player))
			return;
		removeItem(player, 20752, 1);
		addItem(player, 20747, 1);
	}

	// Bone Quiver Pack
	public void ItemHandler_22089(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22089, player))
			return;
		removeItem(player, 22089, 1);
		addItem(player, 1341, 10000);
	}
	
	// Steel Quiver Pack
	public void ItemHandler_22090(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22090, player))
			return;
		removeItem(player, 22090, 1);
		addItem(player, 1342, 10000);
	}
	
	// Silver Quiver Pack
	public void ItemHandler_22091(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22091, player))
			return;
		removeItem(player, 22091, 1);
		addItem(player, 1343, 10000);
	}
	
	// Mithril Quiver Pack
	public void ItemHandler_22092(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22092, player))
			return;
		removeItem(player, 22092, 1);
		addItem(player, 1344, 10000);
	}
	
	// Quiver of Light Pack
	public void ItemHandler_22093(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(22093, player))
			return;
		removeItem(player, 22093, 1);
		addItem(player, 1345, 10000);
	}
	
	// Simple Valentine Cake
	public void ItemHandler_20195(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20195, player))
			return;
		int[] list = new int[] { 10134, 10138, 1539, 1540, 20196 };
		int[] counts = new int[] { 1, 1, 4, 3, 1 };
		int[] chances = new int[] { 9, 9, 41, 16, 25 };
		removeItem(player, 20195, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Velvety Valentine Cake
	public void ItemHandler_20196(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20196, player))
			return;
		int[] list = new int[] { 948, 952, 1538, 3936, 20200, 20197 };
		int[] counts = new int[] { 2, 3, 1, 1, 1, 1 };
		int[] chances = new int[] { 14, 19, 11, 6, 20, 30 };
		removeItem(player, 20195, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Delectable Valentine Cake
	public void ItemHandler_20197(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20197, player))
			return;
		int[] list = new int[] { 947, 951, 20201, 20198 };
		int[] counts = new int[] { 2, 3, 1, 1 };
		int[] chances = new int[] { 14, 39, 17, 30 };
		removeItem(player, 20195, 1);
		extract_item_r(list, counts, chances, player);
	}

	// Decadent Valentine Cake
	public void ItemHandler_20198(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20198, player))
			return;
		int[] list = new int[] { 959, 729, 20202, 20203 };
		int[] counts = new int[] { 1, 2, 1, 1 };
		int[] chances = new int[] { 14, 26, 35, 25 };
		removeItem(player, 20195, 1);
		extract_item_r(list, counts, chances, player);
	}

	private static final int[] SOI_books = { 14209, // Forgotten Scroll - Hide
			14212, // Forgotten Scroll - Enlightenment - Wizard
			14213, // Forgotten Scroll - Enlightenment - Healer
			10554, //Forgotten Scroll - Anti-Magic Armor
			14208, // Forgotten Scroll - Final Secret
			10577 // Forgotten Scroll - Excessive Loyalty
	};

	// Jewel Ornamented Duel Supplies
	public void ItemHandler_13777(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13777, player))
			return;
		removeItem(player, 13777, 1);

		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			addItem(player, 9630, 3); // 3 Orichalcum
			addItem(player, 9629, 3); // 3 Adamantine
			addItem(player, 9628, 4); // 4 Leonard
			addItem(player, 8639, 6); // 6 Elixir of CP (S-Grade)
			addItem(player, 8627, 6); // 6 Elixir of Life (S-Grade)
			addItem(player, 8633, 6); // 6 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			addItem(player, 14027, 1); // Collection Agathion Summon Bracelet
	}

	// Mother-of-Pearl Ornamented Duel Supplies
	public void ItemHandler_13778(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13778, player))
			return;
		removeItem(player, 13778, 1);

		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			addItem(player, 9630, 2); // 3 Orichalcum
			addItem(player, 9629, 2); // 3 Adamantine
			addItem(player, 9628, 3); // 4 Leonard
			addItem(player, 8639, 5); // 5 Elixir of CP (S-Grade)
			addItem(player, 8627, 5); // 5 Elixir of Life (S-Grade)
			addItem(player, 8633, 5); // 5 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			addItem(player, 14027, 1); // Collection Agathion Summon Bracelet
	}

	// Gold-Ornamented Duel Supplies
	public void ItemHandler_13779(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13779, player))
			return;
		removeItem(player, 13779, 1);

		int rnd = Rnd.get(100);
		if(rnd <= 65)
		{
			addItem(player, 9630, 1); // 1 Orichalcum
			addItem(player, 9629, 1); // 1 Adamantine
			addItem(player, 9628, 2); // 2 Leonard
			addItem(player, 8639, 4); // 4 Elixir of CP (S-Grade)
			addItem(player, 8627, 4); // 4 Elixir of Life (S-Grade)
			addItem(player, 8633, 4); // 4 Elixir of Mental Strength (S-Grade)
		}
		else if(rnd <= 95)
			addItem(player, SOI_books[Rnd.get(SOI_books.length)], 1);
		else
			addItem(player, 14027, 1); // Collection Agathion Summon Bracelet
	}

	// Silver-Ornamented Duel Supplies
	public void ItemHandler_13780(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13780, player))
			return;
		removeItem(player, 13780, 1);

		addItem(player, 8639, 4); // 4 Elixir of CP (S-Grade)
		addItem(player, 8627, 4); // 4 Elixir of Life (S-Grade)
		addItem(player, 8633, 4); // 4 Elixir of Mental Strength (S-Grade)
	}

	// Bronze-Ornamented Duel Supplies
	public void ItemHandler_13781(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13781, player))
			return;
		removeItem(player, 13781, 1);

		addItem(player, 8639, 4); // 4 Elixir of CP (S-Grade)
		addItem(player, 8627, 4); // 4 Elixir of Life (S-Grade)
		addItem(player, 8633, 4); // 4 Elixir of Mental Strength (S-Grade)
	}

	// Non-Ornamented Duel Supplies
	public void ItemHandler_13782(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13782, player))
			return;
		removeItem(player, 13782, 1);

		addItem(player, 8639, 3); // 3 Elixir of CP (S-Grade)
		addItem(player, 8627, 3); // 3 Elixir of Life (S-Grade)
		addItem(player, 8633, 3); // 3 Elixir of Mental Strength (S-Grade)
	}

	// Weak-Looking Duel Supplies
	public void ItemHandler_13783(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13783, player))
			return;
		removeItem(player, 13783, 1);

		addItem(player, 8639, 3); // 3 Elixir of CP (S-Grade)
		addItem(player, 8627, 3); // 3 Elixir of Life (S-Grade)
		addItem(player, 8633, 3); // 3 Elixir of Mental Strength (S-Grade)
	}

	// Sad-Looking Duel Supplies
	public void ItemHandler_13784(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13784, player))
			return;
		removeItem(player, 13784, 1);

		addItem(player, 8639, 3); // 3 Elixir of CP (S-Grade)
		addItem(player, 8627, 3); // 3 Elixir of Life (S-Grade)
		addItem(player, 8633, 3); // 3 Elixir of Mental Strength (S-Grade)
	}

	// Poor-Looking Duel Supplies
	public void ItemHandler_13785(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13785, player))
			return;
		removeItem(player, 13785, 1);

		addItem(player, 8639, 2); // 2 Elixir of CP (S-Grade)
		addItem(player, 8627, 2); // 2 Elixir of Life (S-Grade)
		addItem(player, 8633, 2); // 2 Elixir of Mental Strength (S-Grade)
	}

	// Worthless Duel Supplies
	public void ItemHandler_13786(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13786, player))
			return;
		removeItem(player, 13786, 1);

		addItem(player, 8639, 1); // 1 Elixir of CP (S-Grade)
		addItem(player, 8627, 1); // 1 Elixir of Life (S-Grade)
		addItem(player, 8633, 1); // 1 Elixir of Mental Strength (S-Grade)
	}

	// S-Grade Accessory Chest(MasterOfEnchanting Event)
	private static final int[] SAccessoryChest = { 6724, 6725, 6726 };

	public void ItemHandler_13992(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13992, player))
			return;
		removeItem(player, 13992, 1);
		addItem(player, SAccessoryChest[Rnd.get(SAccessoryChest.length)], 1);
	}

	// S-Grade Armor Chest(MasterOfEnchanting Event)
	private static final int[] SArmorChest = { 6674, 6675, 6679, 6683, 6687, 6678, 6677, 6682, 6686, 6676, 6681, 6685,
			9582, 10500, 10501, 10502, 6680, 6684 };

	public void ItemHandler_13991(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13991, player))
			return;
		removeItem(player, 13991, 1);
		addItem(player, SArmorChest[Rnd.get(SArmorChest.length)], 1);
	}

	// S-Grade Weapon Chest(MasterOfEnchanting Event)
	private static final int[] SWeaponChest = { 6364, 6372, 6365, 6579, 6369, 6367, 6370, 6371, 7575, 6580 };

	public void ItemHandler_13990(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13990, player))
			return;
		removeItem(player, 13990, 1);
		addItem(player, SWeaponChest[Rnd.get(SWeaponChest.length)], 1);
	}

	// S80-Grade Armor Chest(MasterOfEnchanting Event)
	private static final int[] S80ArmorChest = { 9514, 9519, 9515, 9520, 9525, 9516, 9521, 9526, 9529, 9518, 9523, 9528,
			9517, 9522, 9527 };

	public void ItemHandler_13989(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13989, player))
			return;
		removeItem(player, 13989, 1);
		addItem(player, S80ArmorChest[Rnd.get(S80ArmorChest.length)], 1);
	}

	// S80-Grade Weapon Chest(MasterOfEnchanting Event)
	private static final int[] S80WeaponChest = { 9444, 9442, 9449, 9448, 9446, 9447, 9450, 9445, 10004 };

	public void ItemHandler_13988(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(13988, player))
			return;
		removeItem(player, 13988, 1);
		addItem(player, S80WeaponChest[Rnd.get(S80WeaponChest.length)], 1);
	}

	//Scarecrow Jack Transformation Stick 30 Day Pack
	public void ItemHandler_14235(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14235, player))
			return;
		removeItem(player, 14235, 1);
		addItem(player, 12799, 1);
	}

	//Stakato Quest Reward
	public void ItemHandler_14833(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14833, player))
			return;
		removeItem(player, 14833, 1);

		if(Rnd.chance(10))
			addItem(player, 14209, 1);

		if(Rnd.chance(10))
			addItem(player, 14208, 1);

		if(Rnd.chance(10))
			addItem(player, 14212, 1);

		if(Rnd.chance(10))
			addItem(player, 10577, 1);

		if(Rnd.chance(10))
			addItem(player, 959, 1);

		if(Rnd.chance(15))
			addItem(player, 960, 2);

		if(Rnd.chance(25))
			addItem(player, 9573, 1);

		if(Rnd.chance(25))
			addItem(player, 10483, 1);

		if(Rnd.chance(5))
		{
			int[] list = new int[] { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
			int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };

			if(ctrl)
			{
				long item_count = player.getInventory().getCountOf(14833);
				removeItem(player, 14833, item_count);
				for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
					addItem(player, res[0], res[1]);
			}
			else
			{
				removeItem(player, 14833, 1);
				extract_item_r(list, counts, chances, player);
			}
		}
	}

	public void ItemHandler_14834(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14834, player))
			return;
		removeItem(player, 14834, 1);

		if(Rnd.chance(15))
			addItem(player, 14209, 1);

		if(Rnd.chance(15))
			addItem(player, 14208, 1);

		if(Rnd.chance(15))
			addItem(player, 14212, 1);

		if(Rnd.chance(15))
			addItem(player, 10577, 1);

		if(Rnd.chance(15))
			addItem(player, 959, 1);

		if(Rnd.chance(20))
			addItem(player, 960, 2);

		if(Rnd.chance(25))
			addItem(player, 9573, 1);

		if(Rnd.chance(25))
			addItem(player, 10483, 1);

		if(Rnd.chance(10))
		{
			int[] list = new int[] { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
			int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };

			if(ctrl)
			{
				long item_count = player.getInventory().getCountOf(14834);
				removeItem(player, 14834, item_count);
				for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
					addItem(player, res[0], res[1]);
			}
			else
			{
				removeItem(player, 14834, 1);
				extract_item_r(list, counts, chances, player);
			}
		}
	}

	// Kahman's Supply Box
	public void ItemHandler_14849(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14849, player))
			return;
		removeItem(player, 14849, 1);

		if(Rnd.chance(15))
			addItem(player, 14209, 1);

		if(Rnd.chance(15))
			addItem(player, 14208, 1);

		if(Rnd.chance(15))
			addItem(player, 14212, 1);

		if(Rnd.chance(15))
			addItem(player, 10577, 1);

		if(Rnd.chance(15))
			addItem(player, 959, 1);

		if(Rnd.chance(20))
			addItem(player, 960, 2);

		if(Rnd.chance(25))
			addItem(player, 9573, 1);

		if(Rnd.chance(25))
			addItem(player, 10483, 1);

		if(Rnd.chance(10))
			addItem(player, 9625, 1);

		if(Rnd.chance(10))
			addItem(player, 9626, 1);

		if(Rnd.chance(9))
		{
			int[] list = new int[] { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
			int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };

			if(ctrl)
			{
				long item_count = player.getInventory().getCountOf(14849);
				removeItem(player, 14849, item_count);
				for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
					addItem(player, res[0], res[1]);
			}
			else
			{
				removeItem(player, 14849, 1);
				extract_item_r(list, counts, chances, player);
			}
		}
	}

	// Droph's Support Items
	public void ItemHandler_14850(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(14850, player))
			return;
		removeItem(player, 14850, 1);

		if(Rnd.chance(15))
			addItem(player, 14209, 1);

		if(Rnd.chance(15))
			addItem(player, 14208, 1);

		if(Rnd.chance(15))
			addItem(player, 14212, 1);

		if(Rnd.chance(15))
			addItem(player, 10577, 1);

		if(Rnd.chance(15))
			addItem(player, 959, 1);

		if(Rnd.chance(20))
			addItem(player, 960, 2);

		if(Rnd.chance(25))
			addItem(player, 9573, 1);

		if(Rnd.chance(25))
			addItem(player, 10483, 1);

		if(Rnd.chance(10))
			addItem(player, 9625, 1);

		if(Rnd.chance(10))
			addItem(player, 9626, 1);

		if(Rnd.chance(9))
		{
			int[] list = new int[] { 10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381 };
			int[] counts = new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
			int[] chances = new int[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 };

			if(ctrl)
			{
				long item_count = player.getInventory().getCountOf(14850);
				removeItem(player, 14850, item_count);
				for(int[] res : mass_extract_item_r(item_count, list, counts, chances, player))
					addItem(player, res[0], res[1]);
			}
			else
			{
				removeItem(player, 14850, 1);
				extract_item_r(list, counts, chances, player);
			}
		}
	}
	
	// No Grade Beginner's Adventurer Support Pack
	public void ItemHandler_20635(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20635, player))
			return;
		removeItem(player, 20635, 1);
		addItem(player, 8973, 1);
		addItem(player, 8977, 1);
		addItem(player, 9030, 1);
		addItem(player, 9031, 1);
		addItem(player, 9032, 1);
		addItem(player, 9033, 1);
		addItem(player, 9034, 1);
		addItem(player, 9035, 1);
		addItem(player, 21093, 3);
		addItem(player, 21094, 3);
	}

	// D Grade Fighter Support Pack
	public void ItemHandler_20636(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20636, player))
			return;
		removeItem(player, 20636, 1);
		addItem(player, 20639, 1);
		addItem(player, 20640, 1);
		addItem(player, 20641, 1);
		addItem(player, 20642, 1);
		addItem(player, 20643, 1);
		addItem(player, 20644, 1);
		addItem(player, 20645, 1);
		addItem(player, 20646, 1);
		addItem(player, 20647, 1);
		addItem(player, 20648, 1);
	}
	
	// D Grade Mage Support Pack
	public void ItemHandler_20637(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20637, player))
			return;
		removeItem(player, 20637, 1);
		addItem(player, 20649, 1);
		addItem(player, 20650, 1);
		addItem(player, 20651, 1);
		addItem(player, 20652, 1);
		addItem(player, 20653, 1);
		addItem(player, 20645, 1);
	}
	
	// Beginner's Adventurer Reinforcement Pack
	public void ItemHandler_20638(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(20638, player))
			return;
		removeItem(player, 20638, 1);
		addItem(player, 20415, 1);
		addItem(player, 21091, 1);
		addItem(player, 21092, 1);
	}
	
	// Rune of Exp. Points 50% 7 day pack
	public void ItemHandler_21091(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(21091, player))
			return;
		removeItem(player, 21091, 1);
		addItem(player, 20340, 1);
	}
	
	// Rune of SP 50% 7 day pack
	public void ItemHandler_21092(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(21092, player))
			return;
		removeItem(player, 21092, 1);
		addItem(player, 20346, 1);
	}
	
	// Sweet Fruit Cocktail 
	public void ItemHandler_21093(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 21093, 1);
		for(int skill : sweet_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}

	// Fresh Fruit Cocktail 
	public void ItemHandler_21094(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(player.isInZone(ZoneType.OlympiadStadia))
			return;
		removeItem(player, 21094, 1);
		for(int skill : fresh_list)
		{
			player.broadcastPacket(new MagicSkillUse(player, player, skill, 1, 0, 0));
			player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(skill, 1));
		}
	}
	
	// Circlet of Freeze Pack
	public void ItemHandler_21095(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(21095, player))
			return;
		removeItem(player, 21095, 1);
		addItem(player, 17033, 1);
	}
	
	// Fortuna of Chaos
	public void ItemHandler_21041(L2Player player, Boolean ctrl)
	{
		if(player == null)
			return;
		if(!canBeExtracted(21041, player))
			return;
		int[] list = new int[] { 8618, 8619, 8620, 8621, 14700, 14701, 21038, 9627, 6622, 21030, 21031, 21032, 21033, 21037 };
		int[] counts = new int[] { 3, 1, 3, 1, 25, 12, 6, 1, 3, 4, 4, 8, 8, 5 };
		int[] chances = new int[] { 14, 26, 17, 1, 4, 4, 5, 5, 19, 2, 2, 1, 1, 1 };
		removeItem(player, 21041, 1);
		extract_item_r(list, counts, chances, player);
	}
	
	public static void extract_item(int[] list, int[] counts, L2Player player)
	{
		if(player == null)
			return;
		int index = Rnd.get(list.length);
		int id = list[index];
		int count = counts[index];
		addItem(player, id, count);
	}

	public static GArray<int[]> mass_extract_item(long source_count, int[] list, int[] counts, L2Player player)
	{
		if(player == null)
			return new GArray<int[]>(0);

		GArray<int[]> result = new GArray<int[]>((int) Math.min(list.length, source_count));

		for(int n = 1; n <= source_count; n++)
		{
			int index = Rnd.get(list.length);
			int item = list[index];
			int count = counts[index];

			int[] old = null;
			for(int[] res : result)
				if(res[0] == item)
					old = res;

			if(old == null)
				result.add(new int[] { item, count });
			else
				old[1] += count;
		}

		return result;
	}

	public static void extract_item_r(int[] list, int[] count_min, int[] count_max, int[] chances, L2Player player)
	{
		int[] counts = count_min;
		for(int i = 0; i < count_min.length; i++)
			counts[i] = Rnd.get(count_min[i], count_max[i]);
		extract_item_r(list, counts, chances, player);
	}

	public static void extract_item_r(int[] list, int[] counts, int[] chances, L2Player player)
	{
		if(player == null)
			return;

		int sum = 0;

		for(int i = 0; i < list.length; i++)
			sum += chances[i];

		int[] table = new int[sum];
		int k = 0;

		for(int i = 0; i < list.length; i++)
			for(int j = 0; j < chances[i]; j++)
			{
				table[k] = i;
				k++;
			}

		int i = table[Rnd.get(table.length)];
		int item = list[i];
		int count = counts[i];

		addItem(player, item, count);
	}

	public static GArray<int[]> mass_extract_item_r(long source_count, int[] list, int[] count_min, int[] count_max, int[] chances, L2Player player)
	{
		int[] counts = count_min;
		for(int i = 0; i < count_min.length; i++)
			counts[i] = Rnd.get(count_min[i], count_max[i]);
		return mass_extract_item_r(source_count, list, counts, chances, player);
	}

	public static GArray<int[]> mass_extract_item_r(long source_count, int[] list, int[] counts, int[] chances, L2Player player)
	{
		if(player == null)
			return new GArray<int[]>(0);

		GArray<int[]> result = new GArray<int[]>((int) Math.min(list.length, source_count));

		int sum = 0;
		for(int i = 0; i < list.length; i++)
			sum += chances[i];

		int[] table = new int[sum];
		int k = 0;

		for(int i = 0; i < list.length; i++)
			for(int j = 0; j < chances[i]; j++)
			{
				table[k] = i;
				k++;
			}

		for(int n = 1; n <= source_count; n++)
		{
			int i = table[Rnd.get(table.length)];
			int item = list[i];
			int count = counts[i];

			int[] old = null;
			for(int[] res : result)
				if(res[0] == item)
					old = res;

			if(old == null)
				result.add(new int[] { item, count });
			else
				old[1] += count;
		}

		return result;
	}

	public static boolean canBeExtracted(int itemId, L2Player player)
	{
		if(player == null)
			return false;
		if(player.getWeightPenalty() >= 3 || player.getInventory().getSize() > player.getInventoryLimit() - 10)
		{
			player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL, new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(itemId));
			return false;
		}
		return true;
	}
}