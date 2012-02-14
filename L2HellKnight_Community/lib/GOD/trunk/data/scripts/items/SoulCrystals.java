package items;

import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.SetupGauge;
import l2rt.gameserver.tables.SkillTable;

public class SoulCrystals implements IItemHandler, ScriptFile
{
	// First line is for Red Soul Crystals, second is Green and third is Blue Soul
	// Crystals, ordered by ascending level, from 0 to 14
	public static final int[] _itemIds = { 4629, 4640, 4651, 4630, 4641, 4652, 4631, 4642, 4653, 4632, 4643, 4654, 4633,
			4644, 4655, 4634, 4645, 4656, 4635, 4646, 4657, 4636, 4647, 4658, 4637, 4648, 4659, 4638, 4649, 4660, 4639, 4650,
			4661, 5577, 5578, 5579, 5580, 5581, 5582, 5908, 5911, 5914, 9570, 9571, 9572 };

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = playable.getPlayer();

		if(player.getTarget() == null || !player.getTarget().isMonster())
		{
			player.sendPacket(Msg.INVALID_TARGET, Msg.ActionFail);
			return;
		}

		if(player.isImobilised() || player.isCastingNow())
		{
			player.sendActionFailed();
			return;
		}

		L2MonsterInstance target = (L2MonsterInstance) player.getTarget();

		// u can use soul crystal only when target hp goes to <50%
		if(target.getCurrentHpPercents() >= 50)
		{
			player.sendPacket(Msg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL, Msg.ActionFail);
			return;
		}

		// Soul Crystal Casting section
		int skillHitTime = SkillTable.getInstance().getInfo(2096, 1).getHitTime();
		player.broadcastPacket(new MagicSkillUse(player, 2096, 1, skillHitTime, 0));
		player.sendPacket(new SetupGauge(0, skillHitTime));
		// End Soul Crystal Casting section

		// Continue execution later
		player._skillTask = ThreadPoolManager.getInstance().scheduleAi(new CrystalFinalizer(player, target), skillHitTime, true);
	}

	static class CrystalFinalizer implements Runnable
	{
		private L2Player _activeChar;
		private L2MonsterInstance _target;

		CrystalFinalizer(L2Player activeChar, L2MonsterInstance target)
		{
			_activeChar = activeChar;
			_target = target;
		}

		public void run()
		{
			_activeChar.sendActionFailed();
			_activeChar.clearCastVars();
			if(_activeChar.isDead() || _target.isDead())
				return;
			_target.addAbsorber(_activeChar);
		}
	}

	public final int[] getItemIds()
	{
		return _itemIds;
	}

	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	public void onReload()
	{}

	public void onShutdown()
	{}
}