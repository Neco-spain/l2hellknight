package zone_scripts.Hellbound.Shadai;

import l2.hellknight.Config;
import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.quest.Quest;

public class Shadai extends Quest
{
	private static final int SHADAI = 32347;
	
	private static final int[] DAY_COORDS = { 16882, 238952, 9776 };
	private static final int[] NIGHT_COORDS = { 9064, 253037, -1928 };
	
	public Shadai(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addSpawnId(SHADAI);
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (!npc.isTeleporting())
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ValidatePosition(npc), 60000, 60000);
		return super.onSpawn(npc);
	}
	
	private static void validatePosition(L2Npc npc)
	{
		int[] coords = DAY_COORDS;
		boolean mustRevalidate = false;
		if (npc.getX() != NIGHT_COORDS[0] && GameTimeController.getInstance().isNowNight())
		{
			coords = NIGHT_COORDS;
			mustRevalidate = true;
		}
		else if (npc.getX() != DAY_COORDS[0] && !GameTimeController.getInstance().isNowNight())
			mustRevalidate = true;
			
		if (mustRevalidate)
		{
			npc.getSpawn().setLocx(coords[0]);
			npc.getSpawn().setLocy(coords[1]);
			npc.getSpawn().setLocz(coords[2]);
			npc.teleToLocation(coords[0], coords[1], coords[2]);
		}
	}

	private static class ValidatePosition implements Runnable
	{
		private final L2Npc _npc;

		public ValidatePosition(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			validatePosition(_npc);
		}
	}

	public static void main(String[] args)
	{
		new Shadai(-1, Shadai.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Shadai");
	}
}
