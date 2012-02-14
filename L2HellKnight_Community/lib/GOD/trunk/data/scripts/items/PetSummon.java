package items;

import java.util.logging.Logger;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2PetBabyInstance;
import l2rt.gameserver.model.instances.L2PetInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillLaunched;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;

public class PetSummon implements IItemHandler, ScriptFile
{
	protected static Logger _log = Logger.getLogger(PetSummon.class.getName());

	// all the items ids that this handler knowns
	private static final int[] _itemIds = PetDataTable.getPetControlItems();
	private static final int _skillId = 2046;
	private static final int MAX_RADIUS = 150;
	private static final int MIN_RADIUS = 100;

	public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return;
		L2Player player = (L2Player) playable;

		// pets resummon fast fix
		if(System.currentTimeMillis() <= player.lastDiceThrown)
			return;
		player.lastDiceThrown = System.currentTimeMillis() + 6000L;

		if(!checks(player, item, true))
			return;

		player.stopMove();
		player.block();
		player.broadcastPacket(new MagicSkillUse(player, player, _skillId, 1, 5000, 600000));
		player.sendPacket(Msg.SUMMON_A_PET);

		// continue execution in 5 seconds
		ThreadPoolManager.getInstance().scheduleAi(new SummonFinalizer(player, item), 5000, true);
	}

	static class SummonFinalizer implements Runnable
	{
		private final L2Player _player;
		private final L2ItemInstance _item;

		SummonFinalizer(L2Player player, L2ItemInstance item)
		{
			_player = player;
			_item = item;
		}

		public void run()
		{
			try
			{
				if(!checks(_player, _item, false))
					return;

				int npcId = PetDataTable.getSummonId(_item);
				if(npcId == 0)
					return;

				L2NpcTemplate petTemplate = NpcTable.getTemplate(npcId);
				if(petTemplate == null)
					return;

				L2PetInstance pet = L2PetInstance.spawnPet(petTemplate, _player, _item);
				if(pet == null)
					return;

				_player.setPet(pet);
				pet.setTitle(_player.getName());

				if(!pet.isRespawned())
					try
					{
						pet.setCurrentHp(pet.getMaxHp(), false);
						pet.setCurrentMp(pet.getMaxMp());
						pet.setExp(pet.getExpForThisLevel());
						pet.setCurrentFed(pet.getMaxFed());
						pet.store();
					}
					catch(NullPointerException e)
					{
						_log.warning("PetSummon: failed set stats for summon " + npcId + ".");
						return;
					}

				_player.sendPacket(new MagicSkillLaunched(_player.getObjectId(), 2046, 1, pet, true));
				pet.spawnMe(GeoEngine.findPointToStay(_player.getX(), _player.getY(), _player.getZ(), MIN_RADIUS, MAX_RADIUS, _player.getReflection().getGeoIndex()));
				pet.setRunning();
				pet.setFollowStatus(true, true);

				if(pet instanceof L2PetBabyInstance)
					((L2PetBabyInstance) pet).startBuffTask();
			}
			catch(Throwable e)
			{
				_log.severe(e.toString());
			}
			finally
			{
				if(_player != null)
					_player.unblock();
			}
		}
	}

	public static boolean checks(L2Player player, L2ItemInstance item, boolean first)
	{
		if(player.isInTransaction() || player.isInFlyingTransform())
			return false;

		if(player.isSitting())
		{
			player.sendPacket(Msg.A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING);
			return false;
		}

		if(player.isInOlympiadMode())
		{
			player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		if(player.isCastingNow() || player.isActionsDisabled())
			return false;

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, 1);
		if(!skill.checkCondition(player, player, false, true, true))
			return false;

		if(player.getPet() != null)
		{
			player.sendPacket(Msg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME);
			return false;
		}

		if(player.isMounted() || player.isInVehicle())
		{
			player.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
			return false;
		}

		if(player.isCursedWeaponEquipped())
		{
			// You can't mount while weilding a cursed weapon
			player.sendPacket(Msg.A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE);
			return false;
		}

		int npcId = PetDataTable.getSummonId(item);
		if(npcId == 0)
			return false;

		if(Config.ALT_DONT_ALLOW_PETS_ON_SIEGE && (PetDataTable.isBabyPet(npcId) || PetDataTable.isImprovedBabyPet(npcId)) && SiegeManager.getSiege(player, true) != null)
		{
			player.sendMessage("Этих питомцев запрещено использовать в зонах осад.");
			return false;
		}

		for(L2Object o : L2World.getAroundObjects(player, MAX_RADIUS + 50, 200))
			if(o.isDoor())
			{
				player.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}

		return true;
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