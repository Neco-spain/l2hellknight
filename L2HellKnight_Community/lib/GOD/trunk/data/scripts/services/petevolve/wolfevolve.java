package services.petevolve;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Summon;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.PetDataTable.L2Pet;
import l2rt.util.Files;

/**
 * User: darkevil
 * Date: 16.05.2008
 * Time: 12:19:36
 */
public class wolfevolve extends Functions implements ScriptFile
{
	private static final int WOLF = PetDataTable.PET_WOLF_ID; //Чтоб было =), проверка на Wolf
	private static final int WOLF_COLLAR = L2Pet.WOLF.getControlItemId(); // Ошейник Wolf
	private static final int GREAT_WOLF_NECKLACE = L2Pet.GREAT_WOLF.getControlItemId(); // Ожерелье Great Wolf

	public void evolve()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance npc = getNpc();
		if(player == null || npc == null)
			return;
		L2Summon pl_pet = player.getPet();
		if(player.getInventory().getItemByItemId(WOLF_COLLAR) == null)
		{
			show(Files.read("data/scripts/services/petevolve/no_item.htm", player), player, npc);
			return;
		}
		if(pl_pet == null || pl_pet.isDead())
		{
			show(Files.read("data/scripts/services/petevolve/evolve_no.htm", player), player, npc);
			return;
		}
		if(pl_pet.getNpcId() != WOLF)
		{
			show(Files.read("data/scripts/services/petevolve/no_wolf.htm", player), player, npc);
			return;
		}
		if(pl_pet.getLevel() < 55)
		{
			show(Files.read("data/scripts/services/petevolve/no_level.htm", player), player, npc);
			return;
		}

		L2ItemInstance control = player.getInventory().getItemByObjectId(player.getPet().getControlItemObjId());
		control.setItemId(GREAT_WOLF_NECKLACE);
		control.updateDatabase(true, true);
		player.sendPacket(new ItemList(player, false));
		player.getPet().unSummon();

		show(Files.read("data/scripts/services/petevolve/yes_wolf.htm", player), player, npc);
	}

	public void onLoad()
	{
		System.out.println("Loaded Service: Evolve Wolf");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}