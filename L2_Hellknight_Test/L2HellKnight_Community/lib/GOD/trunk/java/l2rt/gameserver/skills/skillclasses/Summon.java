package l2rt.gameserver.skills.skillclasses;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.instances.*;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.FuncAdd;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;
import l2rt.util.Location;

public class Summon extends L2Skill
{
	private final SummonType _summonType;

	private final float _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;

	private static enum SummonType
	{
		PET,
		CUBIC,
		AGATHION,
		TRAP,
		DECOY,
		MERCHANT
	}

	public Summon(StatsSet set)
	{
		super(set);

		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getFloat("expPenalty", 0.f);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
		_lifeTime = set.getInteger("lifeTime", 1200) * 1000;
	}

	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(_summonType == SummonType.CUBIC && !target.isPlayer())
			return false;

		L2Player player = _summonType == SummonType.CUBIC ? target.getPlayer() : activeChar.getPlayer();
		if(player == null)
			return false;

		// Siege Golem, Wild Hog Cannon, Swoop Cannon
		if(_id == 13 || _id == 299 || _id == 448)
		{
			SystemMessage sm = null;
			Siege siege = SiegeManager.getSiege(player, true);
			if(siege == null)
				sm = Msg.YOU_ARE_NOT_IN_SIEGE;
			else if(player.getClanId() != 0 && siege.getAttackerClan(player.getClan()) == null)
				sm = Msg.OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE;
			if(sm != null)
			{
				player.sendPacket(sm);
				return false;
			}
		}

		switch(_summonType)
		{
			case CUBIC:
				if(_targetType == SkillTargetType.TARGET_SELF)
				{
					int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
					if(mastery < 0)
						mastery = 0;
					if(player.getCubics().size() > mastery && player.getCubic(getNpcId()) == null)
						return false;
				}
				break;
			case AGATHION:
				if(player.getAgathion() != null && getNpcId() != 0)
				{
					player.sendMessage("You may not use multiple agathions at the same time.");
					return false;
				}
				// Попытка использования скила отзыва без вызванного agathion-а.
				if(player.getAgathion() == null && getNpcId() == 0)
				{
					activeChar.sendPacket(Msg.AGATHION_SKILLS_CAN_BE_USED_ONLY_WHEN_AGATHION_IS_SUMMONED);
					return false;
				}
				break;
			case TRAP:
				if(player.isInZonePeace())
				{
					activeChar.sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
					return false;
				}
				break;
			case PET:
			case DECOY:
				if(player.getPet() != null || player.isMounted())
				{
					player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
				break;
		}

		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(L2Character caster, GArray<L2Character> targets)
	{
		L2Player activeChar = caster.getPlayer();

		if(_summonType != SummonType.CUBIC && activeChar == null)
		{
			System.out.println("Non player character has summon skill!!! skill id: " + getId());
			return;
		}

		if(getNpcId() == 0 && _summonType != SummonType.AGATHION)
		{
			caster.sendMessage("Summon skill " + getId() + " not described yet");
			return;
		}

		switch(_summonType)
		{
			case AGATHION:
				activeChar.setAgathion(getNpcId());
				break;
			case CUBIC:
				for(L2Character targ : targets)
					if(targ != null)
					{
						if(!targ.isPlayer())
							continue;
						L2Player target = (L2Player) targ;

						int mastery = target.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
						if(mastery < 0)
							mastery = 0;

						if(target.getCubics().size() > mastery && target.getCubic(getNpcId()) == null)
						{
							target.sendPacket(Msg.CUBIC_SUMMONING_FAILED);
							continue;
						}

						if(getNpcId() == 3 && _lifeTime == 3600000) // novice life cubic
							target.addCubic(3, 8, 3600000, false);
						else if(getNpcId() == 3 && _level > 7) // затычка на энчант поскольку один уровень скилла занят на novice
							target.addCubic(getNpcId(), _level + 1, _lifeTime, caster != target);
						else
							target.addCubic(getNpcId(), _level, _lifeTime, caster != target);

						target.broadcastUserInfo(true);
						getEffects(caster, target, getActivateRate() > 0, false);
					}
				break;
			case TRAP:
				L2Skill trapSkill = getFirstAddedSkill();
				if(trapSkill == null)
				{
					System.out.println("Not implemented trap skill, id = " + getId());
					return;
				}
				if(activeChar.getTrapsCount() >= 5)
				{
					activeChar.destroyFirstTrap();
					if(activeChar.getTrapsCount() >= 5)
					{
						System.out.println("Error while deleting trap!");
						return;
					}
				}
				activeChar.addTrap(new L2TrapInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(getNpcId()), activeChar, trapSkill));
				break;
			case PET:
				// Удаление трупа, если идет суммон из трупа.
				Location loc = null;
				if(_targetType == SkillTargetType.TARGET_CORPSE)
					for(L2Character target : targets)
						if(target != null && target.isDead() && target.isNpc())
						{
							activeChar.getAI().setAttackTarget(null);
							loc = target.getLoc();
							((L2NpcInstance) target).endDecayTask();
						}

				if(activeChar.getPet() != null || activeChar.isMounted())
					return;

				L2NpcTemplate summonTemplate = NpcTable.getTemplate(getNpcId());

				if(summonTemplate == null)
				{
					System.out.println("Null summon template for skill " + this);
					return;
				}

				L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay);

				summon.setTitle(activeChar.getName());
				summon.setExpPenalty(_expPenalty);
				summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
				summon.setCurrentHp(summon.getMaxHp(), false);
				summon.setCurrentMp(summon.getMaxMp());
				summon.setHeading(activeChar.getHeading());
				summon.setRunning();

				activeChar.setPet(summon);

				summon.spawnMe(loc == null ? GeoEngine.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()) : loc);

				if(summon.getSkillLevel(4140) > 0)
					summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(4140)), activeChar);

				if(summon.getName().equalsIgnoreCase("Shadow"))
					summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 0x40, this, 15));

				summon.setFollowStatus(true, true);
				break;
			case DECOY:
				if(activeChar.getPet() != null || activeChar.isMounted())
					return;

				L2NpcTemplate DecoyTemplate = NpcTable.getTemplate(getNpcId());
				L2DecoyInstance decoy = new L2DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, _lifeTime);

				decoy.setCurrentHp(decoy.getMaxHp(), false);
				decoy.setCurrentMp(decoy.getMaxMp());
				decoy.setHeading(activeChar.getHeading());
				decoy.setReflection(activeChar.getReflection());

				activeChar.setDecoy(decoy);

				decoy.spawnMe(activeChar.getLoc());
				break;
			case MERCHANT:
				if(activeChar.getPet() != null || activeChar.isMounted())
					return;

				L2NpcTemplate merchantTemplate = NpcTable.getTemplate(getNpcId());
				L2MerchantInstance merchant = new L2MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);

				merchant.setCurrentHp(merchant.getMaxHp(), false);
				merchant.setCurrentMp(merchant.getMaxMp());
				merchant.setHeading(activeChar.getHeading());
				merchant.setReflection(activeChar.getReflection());
				merchant.spawnMe(activeChar.getLoc());

				ThreadPoolManager.getInstance().scheduleAi(new DeleteMerchantTask(merchant), _lifeTime, true);
				break;
		}

		if(isSSPossible())
			caster.unChargeShots(isMagic());
	}

	public class DeleteMerchantTask implements Runnable
	{
		L2MerchantInstance _merchant;

		public DeleteMerchantTask(L2MerchantInstance merchant)
		{
			_merchant = merchant;
		}

		public void run()
		{
			if(_merchant != null)
				_merchant.deleteMe();
		}
	}

	@Override
	public boolean isOffensive()
	{
		return _targetType == SkillTargetType.TARGET_CORPSE;
	}
}