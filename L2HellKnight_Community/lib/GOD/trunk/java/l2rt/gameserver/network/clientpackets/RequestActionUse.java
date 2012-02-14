package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.instances.L2PetBabyInstance;
import l2rt.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2rt.gameserver.model.instances.L2StaticObjectInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.tables.PetSkillsTable;
import l2rt.gameserver.tables.SkillTable;

import java.util.logging.Logger;

/**
 * packet type id 0x56
 * format:		cddc
 */
public class RequestActionUse extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestActionUse.class.getName());

	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/* type:
	 * 0 - action
	 * 1 - pet action
	 * 2 - pet skill
	 * 3 - social
	 * 4 - dual social
	 *
	 * transform:
	 * 0 для любых разрешено
	 * 1 разрешено для некоторых
	 * 2 запрещено для всех
	 */
	public static enum Action
	{
		// Действия персонажей
		ACTION0(0, 0, 0, 1), // Сесть/встать
		ACTION1(1, 0, 0, 1), // Изменить тип передвижения, шаг/бег
		ACTION7(7, 0, 0, 1), // Next Target
		ACTION10(10, 0, 0, 1), // Запрос на создание приватного магазина продажи
		ACTION28(28, 0, 0, 1), // Запрос на создание приватного магазина покупки
		ACTION37(37, 0, 0, 1), // Создание магазина Common Craft
		ACTION38(38, 0, 0, 1), // Mount
		ACTION51(51, 0, 0, 1), // Создание магазина Dwarven Craft
		ACTION61(61, 0, 0, 1), // Запрос на создание приватного магазина продажи (Package)
		ACTION96(96, 0, 0, 1), // Quit Party Command Channel?
		ACTION97(97, 0, 0, 1), // Request Party Command Channel Info?

		// Действия петов
		ACTION15(15, 1, 0, 0), // Pet Follow
		ACTION16(16, 1, 0, 0), // Атака петом
		ACTION17(17, 1, 0, 0), // Отмена действия у пета
		ACTION19(19, 1, 0, 0), // Отзыв пета
		ACTION21(21, 1, 0, 0), // Pet Follow
		ACTION22(22, 1, 0, 0), // Атака петом
		ACTION23(23, 1, 0, 0), // Отмена действия у пета
		ACTION52(52, 1, 0, 0), // Отзыв саммона
		ACTION53(53, 1, 0, 0), // Передвинуть пета к цели
		ACTION54(54, 1, 0, 0), // Передвинуть пета к цели
		ACTION1070(1070, 1, 0, 1), // (White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar) Buff Control - Controls to prevent a buff upon the master. Lasts for 5 minutes. // нестандартная реализация, триггер

		// Действия петов со скиллами
		ACTION32(32, 2, 4230, 0), // Wild Hog Cannon - Mode Change
		ACTION36(36, 2, 4259, 0), // Soulless - Toxic Smoke
		ACTION39(39, 2, 4138, 0), // Soulless - Parasite Burst
		ACTION41(41, 2, 4230, 0), // Wild Hog Cannon - Attack
		ACTION42(42, 2, 4378, 0), // Kai the Cat - Self Damage Shield
		ACTION43(43, 2, 4137, 0), // Unicorn Merrow - Hydro Screw
		ACTION44(44, 2, 4139, 0), // Big Boom - Boom Attack
		ACTION45(45, 2, 4025, 0), // Unicorn Boxer - Master Recharge
		ACTION46(46, 2, 4261, 0), // Mew the Cat - Mega Storm Strike
		ACTION47(47, 2, 4260, 0), // Silhouette - Steal Blood
		ACTION48(48, 2, 4068, 0), // Mechanic Golem - Mech. Cannon
		ACTION1000(1000, 2, 4079, 0), // Siege Golem - Siege Hammer 
		//ACTION1001(1001, 2, , 0), // Sin Eater - Ultimate Bombastic Buster
		ACTION1003(1003, 2, 4710, 0), // Wind Hatchling/Strider - Wild Stun
		ACTION1004(1004, 2, 4711, 0), // Wind Hatchling/Strider - Wild Defense
		ACTION1005(1005, 2, 4712, 0), // Star Hatchling/Strider - Bright Burst
		ACTION1006(1006, 2, 4713, 0), // Star Hatchling/Strider - Bright Heal
		ACTION1007(1007, 2, 4699, 0), // Cat Queen - Blessing of Queen
		ACTION1008(1008, 2, 4700, 0), // Cat Queen - Gift of Queen
		ACTION1009(1009, 2, 4701, 0), // Cat Queen - Cure of Queen
		ACTION1010(1010, 2, 4702, 0), // Unicorn Seraphim - Blessing of Seraphim
		ACTION1011(1011, 2, 4703, 0), // Unicorn Seraphim - Gift of Seraphim
		ACTION1012(1012, 2, 4704, 0), // Unicorn Seraphim - Cure of Seraphim
		ACTION1013(1013, 2, 4705, 0), // Nightshade - Curse of Shade
		ACTION1014(1014, 2, 4706, 0), // Nightshade - Mass Curse of Shade
		ACTION1015(1015, 2, 4707, 0), // Nightshade - Shade Sacrifice
		ACTION1016(1016, 2, 4709, 0), // Cursed Man - Cursed Blow
		ACTION1017(1017, 2, 4708, 0), // Cursed Man - Cursed Strike/Stun
		ACTION1031(1031, 2, 5135, 0), // Feline King - Slash
		ACTION1032(1032, 2, 5136, 0), // Feline King - Spin Slash
		ACTION1033(1033, 2, 5137, 0), // Feline King - Hold of King
		ACTION1034(1034, 2, 5138, 0), // Magnus the Unicorn - Whiplash
		ACTION1035(1035, 2, 5139, 0), // Magnus the Unicorn - Tridal Wave
		ACTION1036(1036, 2, 5142, 0), // Spectral Lord - Corpse Kaboom
		ACTION1037(1037, 2, 5141, 0), // Spectral Lord - Dicing Death
		ACTION1038(1038, 2, 5140, 0), // Spectral Lord - Force Curse
		ACTION1039(1039, 2, 5110, 0), // Swoop Cannon - Cannon Fodder
		ACTION1040(1040, 2, 5111, 0), // Swoop Cannon - Big Bang
		ACTION1041(1041, 2, 5442, 0), // Great Wolf - 5442 - Bite Attack
		ACTION1042(1042, 2, 5444, 0), // Great Wolf - 5444 - Moul
		ACTION1043(1043, 2, 5443, 0), // Great Wolf - 5443 - Cry of the Wolf
		ACTION1044(1044, 2, 5445, 0), // Great Wolf - 5445 - Awakening 70
		ACTION1045(1045, 2, 5584, 0), // Wolf Howl
		ACTION1046(1046, 2, 5585, 0), // Strider - Roar // TODO скилл не отображается даже на 85 уровне, вероятно нужно корректировать поле type в PetInfo для страйдеров
		ACTION1047(1047, 2, 5580, 0), // Divine Beast - Bite
		ACTION1048(1048, 2, 5581, 0), // Divine Beast - Stun Attack
		ACTION1049(1049, 2, 5582, 0), // Divine Beast - Fire Breath
		ACTION1050(1050, 2, 5583, 0), // Divine Beast - Roar
		ACTION1051(1051, 2, 5638, 0), // Feline Queen - Bless The Body
		ACTION1052(1052, 2, 5639, 0), // Feline Queen - Bless The Soul
		ACTION1053(1053, 2, 5640, 0), // Feline Queen - Haste
		ACTION1054(1054, 2, 5643, 0), // Unicorn Seraphim - Acumen
		ACTION1055(1055, 2, 5647, 0), // Unicorn Seraphim - Clarity
		ACTION1056(1056, 2, 5648, 0), // Unicorn Seraphim - Empower
		ACTION1057(1057, 2, 5646, 0), // Unicorn Seraphim - Wild Magic
		ACTION1058(1058, 2, 5652, 0), // Nightshade - Death Whisper
		ACTION1059(1059, 2, 5653, 0), // Nightshade - Focus
		ACTION1060(1060, 2, 5654, 0), // Nightshade - Guidance
		ACTION1061(1061, 2, 5745, 0), // (Wild Beast Fighter, White Weasel) Death Blow - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1062(1062, 2, 5746, 0), // (Wild Beast Fighter) Double Attack - Rapidly attacks the enemy twice.
		ACTION1063(1063, 2, 5747, 0), // (Wild Beast Fighter) Spin Attack - Inflicts shock and damage to the enemy at the same time with a powerful spin attack.
		ACTION1064(1064, 2, 5748, 0), // (Wild Beast Fighter) Meteor Shower - Attacks nearby enemies with a doll heap attack.
		ACTION1065(1065, 2, 5753, 0), // (Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess) Awakening - Awakens a hidden ability.
		ACTION1066(1066, 2, 5749, 0), // (Fox Shaman, Spirit Shaman) Thunder Bolt - Attacks the enemy with the power of thunder.
		ACTION1067(1067, 2, 5750, 0), // (Fox Shaman, Spirit Shaman) Flash - Inflicts a swift magic attack upon contacted enemies nearby.
		ACTION1068(1068, 2, 5751, 0), // (Fox Shaman, Spirit Shaman) Lightning Wave - Attacks nearby enemies with the power of lightning.
		ACTION1069(1069, 2, 5752, 0), // (Fox Shaman, Fairy Princess) Flare - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		//ACTION1070(1070, 2, 5771, 0), // (White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar) Buff Control - Controls to prevent a buff upon the master. Lasts for 5 minutes. // TODO добавить в таблицу pet_skills
		ACTION1071(1071, 2, 5761, 0), // (Tigress) Power Striker - Powerfully attacks the target.
		ACTION1072(1072, 2, 6046, 0), // (Toy Knight) Piercing attack
		ACTION1073(1073, 2, 6047, 0), // (Toy Knight) Whirlwind
		ACTION1074(1074, 2, 6048, 0), // (Toy Knight) Lance Smash
		ACTION1075(1075, 2, 6049, 0), // (Toy Knight) Battle Cry
		ACTION1076(1076, 2, 6050, 0), // (Turtle Ascetic) Power Smash
		ACTION1077(1077, 2, 6051, 0), // (Turtle Ascetic) Energy Burst
		ACTION1078(1078, 2, 6052, 0), // (Turtle Ascetic) Shockwave
		ACTION1079(1079, 2, 6053, 0), // (Turtle Ascetic) Howl
		ACTION1080(1080, 2, 6041, 0), // Phoenix Rush
		ACTION1081(1081, 2, 6042, 0), // Phoenix Cleanse
		ACTION1082(1082, 2, 6043, 0), // Phoenix Flame Feather
		ACTION1083(1083, 2, 6044, 0), // Phoenix Flame Beak
		ACTION1084(1084, 2, 6054, 0), // (Spirit Shaman, Toy Knight, Turtle Ascetic) Switch State - Toggles you between Attack and Support modes.
		ACTION1086(1086, 2, 6094, 0), // Panther Cancel
		ACTION1087(1087, 2, 6095, 0), // Panther Dark Claw
		ACTION1088(1088, 2, 6096, 0), // Panther Fatal Claw
		ACTION1089(1089, 2, 6199, 0), // (Deinonychus) Tail Strike
		ACTION1090(1090, 2, 6205, 0), // (Guardian's Strider) Strider Bite // TODO добавить в таблицу pet_skills
		ACTION1091(1091, 2, 6206, 0), // (Guardian's Strider) Strider Fear // TODO добавить в таблицу pet_skills
		ACTION1092(1092, 2, 6207, 0), // (Guardian's Strider) Strider Dash // TODO добавить в таблицу pet_skills

		// Социальные действия
		ACTION12(12, 3, SocialAction.GREETING, 2),
		ACTION13(13, 3, SocialAction.VICTORY, 2),
		ACTION14(14, 3, SocialAction.ADVANCE, 2),
		ACTION24(24, 3, SocialAction.YES, 2),
		ACTION25(25, 3, SocialAction.NO, 2),
		ACTION26(26, 3, SocialAction.BOW, 2),
		ACTION29(29, 3, SocialAction.UNAWARE, 2),
		ACTION30(30, 3, SocialAction.WAITING, 2),
		ACTION31(31, 3, SocialAction.LAUGH, 2),
		ACTION33(33, 3, SocialAction.APPLAUD, 2),
		ACTION34(34, 3, SocialAction.DANCE, 2),
		ACTION35(35, 3, SocialAction.SORROW, 2),
		ACTION62(62, 3, SocialAction.CHARM, 2),
		ACTION66(66, 3, SocialAction.SHYNESS, 2),

        // Парные социальные действия
        ACTION71(71, 4, SocialAction.DUALBOW, 2),
        ACTION72(72, 4, SocialAction.DUALHIGHFIVE, 2),
        ACTION73(73, 4, SocialAction.DUALDANCE, 2),
		
		ACTION78(78, 0, 0, 1), // Сесть/встать
		ACTION79(79, 0, 0, 1), // Сесть/встать
		ACTION80(80, 0, 0, 1), // Сесть/встать
		ACTION81(81, 0, 0, 1); // Сесть/встать

		public int id;
		public int type;
		public int value;
		public int transform;

		private Action(int id, int type, int value, int transform)
		{
			this.id = id;
			this.type = type;
			this.value = value;
			this.transform = transform;
		}

		public static Action find(int id)
		{
			for(Action action : Action.values())
				if(action.id == id)
					return action;
			return null;
		}
	}

	@Override
	public void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		/* TODO управление летающим кораблем
		 * Возможно, пригодятся пакеты:
		 * FlySelfDestination
		 * ExMoveToTargetInAirShip
		 * ExJumpToLocation
		 * ExAttackInAirShip
		 * ExAirShipTeleportList
		 * ExAirShipInfo
		 */
		switch(_actionId)
		{
			case 67: // Steer. Allows you to control the Airship.
				L2AirShip.controlSteer(activeChar);
				activeChar.sendActionFailed();
				return;
			case 68: // Cancel Control. Relinquishes control of the Airship.
				L2AirShip.controlCancel(activeChar);
				activeChar.sendActionFailed();
				return;
			case 69: // Destination Map. Choose from pre-designated locations.
				L2AirShip.controlDestination(activeChar);
				activeChar.sendActionFailed();
				return;
			case 70: // Exit Airship. Disembarks from the Airship.
				L2AirShip.controlExit(activeChar);
				activeChar.sendActionFailed();
				return;
		}

		Action action = Action.find(_actionId);
		if(action == null)
		{
			_log.warning("unhandled action type " + _actionId + " by player " + activeChar.getName());
			activeChar.sendActionFailed();
			return;
		}

		boolean usePet = action.type == 1 || action.type == 2;

		// dont do anything if player is dead or confused
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && !(activeChar.isFakeDeath() && _actionId == 0))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTransformation() != 0 && action.transform > 0) // TODO разрешить для некоторых трансформ
		{
			activeChar.sendActionFailed();
			return;
		}

		// Социальные действия
		if(action.type == 3)
		{
			if(activeChar.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || activeChar.isInTransaction())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
				return;
			}
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), action.value));
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().scheduleAi(new SocialTask(activeChar), 2600, true);
				activeChar.block();
			}
			return;
		}

		final L2Object target = activeChar.getTarget();

		final L2Summon pet = activeChar.getPet();
		if(usePet && (pet == null || pet.isOutOfControl()))
		{
			activeChar.sendActionFailed();
			return;
		}

		// Скиллы петов
		if(action.type == 2)
		{
			// TODO перенести эти условия в скиллы
			if(action.id == 1000 && !target.isDoor()) // Siege Golem - Siege Hammer
			{
				activeChar.sendActionFailed();
				return;
			}
			if((action.id == 1039 || action.id == 1040) && (target.isDoor() || target instanceof L2SiegeHeadquarterInstance)) // Swoop Cannon (не может атаковать двери и флаги)
			{
				activeChar.sendActionFailed();
				return;
			}
			UseSkill(action.value);
			return;
		}

		switch(action.id)
		{
			// Действия с игроками:

			case 0: // Сесть/встать
				// На страйдере нельзя садиться
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}
				int distance = (int) activeChar.getDistance(activeChar.getTarget());
				if(target != null && !activeChar.isSitting() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 1 && distance <= L2Character.INTERACTION_DISTANCE)
				{
					ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getStaticObjectId());
					activeChar.sendPacket(cs);
					activeChar.sitDown();
					activeChar.broadcastPacket(cs);
					break;
				}
				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
				}
				else if(activeChar.isSitting())
					activeChar.standUp();
				else
					activeChar.sitDown();
				break;
			case 1: // Изменить тип передвижения, шаг/бег
				if(activeChar.isRunning())
					activeChar.setWalking();
				else
					activeChar.setRunning();
				break;
			case 7: // Next Target
				L2Character nearest_target = null;
				for(L2Character cha : L2World.getAroundCharacters(activeChar, 400, 200))
					if(cha != null && !cha.isAlikeDead())
						if((nearest_target == null || activeChar.getDistance3D(cha) < activeChar.getDistance3D(nearest_target)) && cha.isAutoAttackable(activeChar))
							nearest_target = cha;
				if(nearest_target != null && activeChar.getTarget() != nearest_target)
				{
					activeChar.setTarget(nearest_target);
					if(activeChar.getTarget() == nearest_target)
					{
						if(nearest_target.isNpc())
						{
							activeChar.sendPacket(new MyTargetSelected(nearest_target.getObjectId(), activeChar.getLevel() - nearest_target.getLevel()));
							activeChar.sendPacket(nearest_target.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP));
							activeChar.sendPacket(new ValidateLocation(nearest_target), Msg.ActionFail);
						}
						else
							activeChar.sendPacket(new MyTargetSelected(nearest_target.getObjectId(), 0));
					}
					return;
				}
				break;
			case 10: // Запрос на создание приватного магазина продажи
			case 61: // Запрос на создание приватного магазина продажи (Package)
			{
				if(activeChar.isInTransaction())
					activeChar.getTransaction().cancel();
				if(activeChar.getTradeList() != null)
				{
					activeChar.getTradeList().removeAll();
					activeChar.sendPacket(new SendTradeDone(0));
				}
				else
					activeChar.setTradeList(new L2TradeList(0));
				activeChar.getTradeList().updateSellList(activeChar, activeChar.getSellList());
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				if(!activeChar.checksForShop(false))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageList(activeChar, _actionId == 61));
				break;
			}
			case 28: // Запрос на создание приватного магазина покупки
			{
				if(activeChar.isInTransaction())
					activeChar.getTransaction().cancel();
				if(activeChar.getTradeList() != null)
				{
					activeChar.getTradeList().removeAll();
					activeChar.sendPacket(new SendTradeDone(0));
				}
				else
					activeChar.setTradeList(new L2TradeList(0));
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				if(!activeChar.checksForShop(false))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageListBuy(activeChar));
			}
				break;
			case 37: // Создание магазина Common Craft
			{
				if(activeChar.isInTransaction())
					activeChar.getTransaction().cancel();
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new L2ManufactureList());
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			}
			case 51: // Создание магазина Dwarven Craft
			{
				if(!activeChar.checksForShop(true))
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getCreateList() == null)
					activeChar.setCreateList(new L2ManufactureList());
				activeChar.setPrivateStoreType(L2Player.STORE_PRIVATE_NONE);
				activeChar.standUp();
				activeChar.broadcastUserInfo(true);
				activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
				break;
			}
			case 96: // Quit Party Command Channel?
				_log.info("96 Accessed");
				break;
			case 97: // Request Party Command Channel Info?
				_log.info("97 Accessed");
				break;

			// Действия с петами:	

			case 15:
			case 21: // Follow для пета
				if(pet != null)
				{
					pet.setFollowTarget(pet.getPlayer());
					pet.setFollowStatus(!pet.isFollow(), true);
				}
				break;
			case 16:
			case 22: // Атака петом
				if(target == null || pet == target || pet.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}

				// Sin Eater
				if(pet.getTemplate().getNpcId() == PetDataTable.SIN_EATER_ID)
					return;

				if(!_ctrlPressed && !target.isAutoAttackable(activeChar))
				{
					pet.setFollowTarget((L2Character) target);
					pet.setFollowStatus(true, true);
					return;
				}

				if(!target.isMonster() && (pet.isInZonePeace() || target.isInZonePeace()))
				{
					activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
					return;
				}

				if(activeChar.getLevel() + 20 <= pet.getLevel())
				{
					activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}

				if(!target.isDoor() && pet.isSiegeWeapon())
				{
					activeChar.sendPacket(Msg.INVALID_TARGET);
					return;
				}

				pet.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			case 17:
			case 23: // Отмена действия у пета
				pet.setFollowTarget(pet.getPlayer());
				pet.setFollowStatus(pet.isFollow(), true);
				break;
			case 19: // Отзыв пета
				if(pet.isDead())
				{
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_SENT_BACK, Msg.ActionFail);
					return;
				}

				if(pet.isInCombat())
				{
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE, Msg.ActionFail);
					break;
				}

				if(pet.isPet() && pet.getCurrentFed() < 0.55 * pet.getMaxFed())
				{
					activeChar.sendPacket(Msg.YOU_CANNOT_RESTORE_HUNGRY_PETS, Msg.ActionFail);
					break;
				}

				pet.unSummon();
				break;
			case 38: // Mount
				if(activeChar.getTransformation() != 0)
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(pet == null || !pet.isMountable())
				{
					if(activeChar.isMounted())
					{
						if(activeChar.isFlying() && !activeChar.checkLandingState()) // Виверна
						{
							activeChar.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION, Msg.ActionFail);
							return;
						}
						activeChar.setMount(0, 0, 0);
					}
				}
				else if(activeChar.isMounted() || activeChar.isInVehicle())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isDead())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(pet.isDead())
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_RIDDEN);
				else if(activeChar.isInDuel())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isInCombat() || pet.isInCombat())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isFishing())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isSitting())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCursedWeaponEquipped())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCombatFlagEquipped() || activeChar.isTerritoryFlagEquipped())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCastingNow())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isParalyzed())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else
				{
					activeChar.getEffectList().stopEffect(L2Skill.SKILL_EVENT_TIMER);
					activeChar.setMount(pet.getTemplate().npcId, pet.getObjectId(), pet.getLevel());
					pet.unSummon();
				}
				break;
			case 52: // Отзыв саммона
				if(pet.isInCombat())
					activeChar.sendPacket(Msg.A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE, Msg.ActionFail);
				else
					pet.unSummon();
				break;
			case 53:
			case 54: // Передвинуть пета к цели
				if(target != null && pet != target && !pet.isMovementDisabled())
				{
					pet.setFollowStatus(false, true);
					ThreadPoolManager.getInstance().executePathfind(new Runnable(){
						public void run()
						{
							pet.moveToLocation(target.getLoc(), 100, true);
						}
					});
				}
				break;
			case 1070:
				if(pet instanceof L2PetBabyInstance)
					((L2PetBabyInstance) pet).triggerBuff();
				break;
			case 71: //поклон
				tryBroadcastDualSocial(16);
				break;
			case 72://дай пять
				tryBroadcastDualSocial(17);
				break;
			case 73://танец
				tryBroadcastDualSocial(18);
				break;
			case 78: // Tactical Sign
			case 79: // Tactical Sign
			case 80: // Tactical Sign
			case 81: // Tactical Sign
				if (target instanceof L2Character && activeChar.getParty() != null) {
					L2Party party = activeChar.getParty();
					if(party != null) {
						party.IconAdd(target.getObjectId(), _actionId-77);
						party.Icon();
					}
				}
				break;

			default:
				_log.warning("unhandled action type " + _actionId + " by player " + activeChar.getName());
		}
		activeChar.sendActionFailed();
	}

    private void tryBroadcastDualSocial(int id) 
	{
        L2Player activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;

        L2Object target = activeChar.getTarget();
        if (!(target instanceof L2Player)) 
		{
            activeChar.sendPacket(new SystemMessage(109));// Incorrect Target
            return;
        }

        L2Player player = (L2Player) target;

        double distance = activeChar.getDistance(player);

        if (distance > 2000 || distance < 30)
		{
            activeChar.sendPacket(new SystemMessage(3120));// The request cannot be completed because the target does not meet location requirements.
            return;
        }

        if (player.isDead()
                || player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE
                || player.isSitting()) {
            activeChar.sendPacket(new SystemMessage(3121)); // Couple action canceled
            return;
        }

        SystemMessage sm = new SystemMessage(3150).addName(player);
        activeChar.sendPacket(sm);
        player.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), id));
    }

	private void UseSkill(int skillId)
	{
		L2Player activeChar = getClient().getActiveChar();
		L2Summon pet = activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		int skillLevel = PetSkillsTable.getInstance().getAvailableLevel(pet, skillId);
		if(skillLevel == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getLevel() + 20 <= pet.getLevel())
		{
			activeChar.sendPacket(Msg.THE_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}

		L2Character aimingTarget = skill.getAimingTarget(pet, activeChar.getTarget());
		if(skill.checkCondition(pet, aimingTarget, _ctrlPressed, _shiftPressed, true))
			pet.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		else
			activeChar.sendActionFailed();
	}

	class SocialTask implements Runnable
	{
		L2Player _player;

		SocialTask(L2Player player)
		{
			_player = player;
		}

		public void run()
		{
			_player.unblock();
		}
	}
}