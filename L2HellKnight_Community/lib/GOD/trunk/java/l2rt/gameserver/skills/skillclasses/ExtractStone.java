package l2rt.gameserver.skills.skillclasses;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.network.serverpackets.StatusUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.StringTokenizer;

public class ExtractStone extends L2Skill
{
	private final static int ExtractScrollSkill = 2630;
	private final static int ExtractedCoarseRedStarStone = 13858;
	private final static int ExtractedCoarseBlueStarStone = 13859;
	private final static int ExtractedCoarseGreenStarStone = 13860;

	private final static int ExtractedRedStarStone = 14009;
	private final static int ExtractedBlueStarStone = 14010;
	private final static int ExtractedGreenStarStone = 14011;

	private final static int RedStarStone1 = 18684;
	private final static int RedStarStone2 = 18685;
	private final static int RedStarStone3 = 18686;

	private final static int BlueStarStone1 = 18687;
	private final static int BlueStarStone2 = 18688;
	private final static int BlueStarStone3 = 18689;

	private final static int GreenStarStone1 = 18690;
	private final static int GreenStarStone2 = 18691;
	private final static int GreenStarStone3 = 18692;

	private final static int FireEnergyCompressionStone = 14015;
	private final static int WaterEnergyCompressionStone = 14016;
	private final static int WindEnergyCompressionStone = 14017;
	private final static int EarthEnergyCompressionStone = 14018;
	private final static int DarknessEnergyCompressionStone = 14019;
	private final static int SacredEnergyCompressionStone = 14020;

	private final static int SeedFire = 18679;
	private final static int SeedWater = 18678;
	private final static int SeedWind = 18680;
	private final static int SeedEarth = 18681;
	private final static int SeedDarkness = 18683;
	private final static int SeedDivinity = 18682;

	private GArray<Integer> _npcIds = new GArray<Integer>();

	public ExtractStone(StatsSet set)
	{
		super(set);
		StringTokenizer st = new StringTokenizer(set.getString("npcIds", ""), ";");
		while(st.hasMoreTokens())
			_npcIds.add(Integer.valueOf(st.nextToken()));
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(target == null || !target.isNpc() || getItemId(target.getNpcId()) == 0)
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		if(!_npcIds.isEmpty() && !_npcIds.contains(new Integer(target.getNpcId())))
		{
			activeChar.sendPacket(Msg.INVALID_TARGET);
			return false;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	/**
	 * Возвращает ID предмета получаемого из npcId.
	 * @return
	 */
	private int getItemId(int npcId)
	{
		switch(npcId)
		{
			case RedStarStone1:
			case RedStarStone2:
			case RedStarStone3:
				if(_id == ExtractScrollSkill)
					return ExtractedCoarseRedStarStone;
				return ExtractedRedStarStone;
			case BlueStarStone1:
			case BlueStarStone2:
			case BlueStarStone3:
				if(_id == ExtractScrollSkill)
					return ExtractedCoarseBlueStarStone;
				return ExtractedBlueStarStone;
			case GreenStarStone1:
			case GreenStarStone2:
			case GreenStarStone3:
				if(_id == ExtractScrollSkill)
					return ExtractedCoarseGreenStarStone;
				return ExtractedGreenStarStone;
			case SeedFire:
				return FireEnergyCompressionStone;
			case SeedWater:
				return WaterEnergyCompressionStone;
			case SeedWind:
				return WindEnergyCompressionStone;
			case SeedEarth:
				return EarthEnergyCompressionStone;
			case SeedDarkness:
				return DarknessEnergyCompressionStone;
			case SeedDivinity:
				return SacredEnergyCompressionStone;
			default:
				return 0;
		}
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		L2Player player = activeChar.getPlayer();
		if(player == null)
			return;

		for(L2Character target : targets)
			if(target != null && getItemId(target.getNpcId()) != 0)
			{
				float rate = Config.RATE_DROP_ITEMS * player.getBonus().RATE_DROP_ITEMS;
				long count = _id == ExtractScrollSkill ? 1 : Math.min(10, Rnd.get((int) (getLevel() * rate + 1)));
				int itemId = getItemId(target.getNpcId());

				if(count > 0)
				{
					player.getInventory().addItem(itemId, count);
					player.sendPacket(new PlaySound(Quest.SOUND_ITEMGET));
					player.sendPacket(SystemMessage.obtainItems(itemId, count, 0));
					player.sendStatusUpdate(false, StatusUpdate.CUR_LOAD);
				}
				else
					player.sendPacket(Msg.THE_COLLECTION_HAS_FAILED);

				target.doDie(player);
			}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}