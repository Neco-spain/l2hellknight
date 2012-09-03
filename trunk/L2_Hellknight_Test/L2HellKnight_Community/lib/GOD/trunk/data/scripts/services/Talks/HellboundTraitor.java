package services.Talks;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2DoorInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.Say2;
import l2rt.gameserver.tables.DoorTable;
import l2rt.util.Files;
import l2rt.util.Location;

public class HellboundTraitor extends Functions implements ScriptFile
{
	private static String EnFilePatch = "data/html/hellbound/hellbound traitor/";
	private static String RuFilePatch = "data/html-ru/hellbound/hellbound traitor/";
    public static L2NpcInstance npc;

	private static final int Leodas = 22448;

	@Override
	public void onLoad()
	{}

	@Override
	public void onReload()
	{}

	@Override
	public void onShutdown()
	{}

	public void MarksOfBetrayal()
	{
		L2Player player = (L2Player) getSelf();
		L2NpcInstance LeodasRB = L2ObjectsStorage.getByNpcId(Leodas);
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		if(LeodasRB != null)
		{
			if(player.getVar("lang@").equalsIgnoreCase("ru"))
				show(Files.read(RuFilePatch + "32364-7.htm", player), player);
			else
				show(Files.read(EnFilePatch + "32364-7.htm", player), player);
			return;
		}
		if(hellboundLevel >= 5 && hellboundLevel <= 6)
		{
			long marksCount = player.getInventory().getItemByItemId(9676).getCount();
			if(marksCount == 0)
			{
				if(player.getVar("lang@").equalsIgnoreCase("ru"))
					show(Files.read(RuFilePatch + "32364-4.htm", player), player);
				else
					show(Files.read(EnFilePatch + "32364-4.htm", player), player);
			}
			else if(marksCount >= 1 && marksCount < 10)
			{
				if(player.getVar("lang@").equalsIgnoreCase("ru"))
					show(Files.read(RuFilePatch + "32364-6.htm", player), player);
				else
					show(Files.read(EnFilePatch + "32364-6.htm", player), player);
			}
			else if(marksCount >= 10)
			{
				final L2DoorInstance Nativedoor3 = DoorTable.getInstance().getDoor(19250003);
				final L2DoorInstance Nativedoor4 = DoorTable.getInstance().getDoor(19250004);

				removeItem(player, 9676, 10); // Marks of Betrayal
				npc.broadcastPacket(new Say2(npc.getObjectId(), 1, npc.getName(), "Brothers! This stranger wants to kill our Commander!!!"));
				Nativedoor3.openMe();
				Nativedoor3.onOpen();

				Nativedoor4.openMe();
				Nativedoor4.onOpen();
				Functions.spawn(new Location( -27807, 252740, -3520, 0), Leodas);
			}
		}
	}
}