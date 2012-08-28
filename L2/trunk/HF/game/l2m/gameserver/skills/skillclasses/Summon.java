package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.dao.EffectsDAO;
import l2m.gameserver.data.xml.holder.NpcHolder;
import l2m.gameserver.idfactory.IdFactory;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.GameObjectTasks.DeleteTask;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillTargetType;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.instances.MerchantInstance;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.model.instances.SummonInstance;
import l2m.gameserver.model.instances.TrapInstance;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.funcs.FuncAdd;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.npc.NpcTemplate;
import l2m.gameserver.utils.Location;

public class Summon extends Skill
{
  private final SummonType _summonType;
  private final double _expPenalty;
  private final int _itemConsumeIdInTime;
  private final int _itemConsumeCountInTime;
  private final int _itemConsumeDelay;
  private final int _lifeTime;

  public Summon(StatsSet set)
  {
    super(set);

    _summonType = ((SummonType)Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase()));
    _expPenalty = set.getDouble("expPenalty", 0.0D);
    _itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
    _itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
    _itemConsumeDelay = (set.getInteger("itemConsumeDelay", 240) * 1000);
    _lifeTime = (set.getInteger("lifeTime", 1200) * 1000);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    Player player = activeChar.getPlayer();
    if (player == null) {
      return false;
    }
    if (player.isProcessingRequest())
    {
      player.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
      return false;
    }

    switch (1.$SwitchMap$l2p$gameserver$skills$skillclasses$Summon$SummonType[_summonType.ordinal()])
    {
    case 1:
      if (!player.isInZonePeace())
        break;
      activeChar.sendPacket(Msg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
      return false;
    case 2:
    case 3:
      if ((player.getPet() == null) && (!player.isMounted()))
        break;
      player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
      return false;
    case 4:
      if ((player.getAgathionId() <= 0) || (_npcId == 0))
        break;
      player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature caster, List<Creature> targets)
  {
    Player activeChar = caster.getPlayer();

    switch (1.$SwitchMap$l2p$gameserver$skills$skillclasses$Summon$SummonType[_summonType.ordinal()])
    {
    case 4:
      activeChar.setAgathion(getNpcId());
      break;
    case 1:
      Skill trapSkill = getFirstAddedSkill();

      if (activeChar.getTrapsCount() >= 5)
        activeChar.destroyFirstTrap();
      TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkill);
      activeChar.addTrap(trap);
      trap.spawnMe();
      break;
    case 2:
    case 3:
      Location loc = null;
      if (_targetType == Skill.SkillTargetType.TARGET_CORPSE) {
        for (Creature target : targets)
          if ((target != null) && (target.isDead()))
          {
            activeChar.getAI().setAttackTarget(null);
            loc = target.getLoc();
            if (target.isNpc())
              ((NpcInstance)target).endDecayTask();
            else if (target.isSummon())
              ((SummonInstance)target).endDecayTask();
            else
              return;
          }
      }
      if ((activeChar.getPet() != null) || (activeChar.isMounted())) {
        return;
      }
      NpcTemplate summonTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
      SummonInstance summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, this);
      activeChar.setPet(summon);

      summon.setTitle(activeChar.getName());
      summon.setExpPenalty(_expPenalty);
      summon.setExp(l2p.gameserver.model.base.Experience.LEVEL[java.lang.Math.min(summon.getLevel(), l2p.gameserver.model.base.Experience.LEVEL.length - 1)]);
      summon.setHeading(activeChar.getHeading());
      summon.setReflection(activeChar.getReflection());
      summon.spawnMe(loc == null ? Location.findAroundPosition(activeChar, 50, 70) : loc);
      summon.setRunning();
      summon.setFollowMode(true);

      if (summon.getSkillLevel(Integer.valueOf(4140)) > 0) {
        summon.altUseSkill(SkillTable.getInstance().getInfo(4140, summon.getSkillLevel(Integer.valueOf(4140))), activeChar);
      }
      if (summon.getName().equalsIgnoreCase("Shadow")) {
        summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 64, this, 15.0D));
      }
      EffectsDAO.getInstance().restoreEffects(summon);
      if (activeChar.isInOlympiadMode()) {
        summon.getEffectList().stopAllEffects();
      }
      summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);

      if (_summonType != SummonType.SIEGE_SUMMON)
        break;
      SiegeEvent siegeEvent = (SiegeEvent)activeChar.getEvent(SiegeEvent.class);

      siegeEvent.addSiegeSummon(summon);
      break;
    case 5:
      if ((activeChar.getPet() != null) || (activeChar.isMounted())) {
        return;
      }
      NpcTemplate merchantTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
      MerchantInstance merchant = new MerchantInstance(IdFactory.getInstance().getNextId(), merchantTemplate);

      merchant.setCurrentHp(merchant.getMaxHp(), false);
      merchant.setCurrentMp(merchant.getMaxMp());
      merchant.setHeading(activeChar.getHeading());
      merchant.setReflection(activeChar.getReflection());
      merchant.spawnMe(activeChar.getLoc());

      ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(merchant), _lifeTime);
    }

    if (isSSPossible())
      caster.unChargeShots(isMagic());
  }

  public boolean isOffensive()
  {
    return _targetType == Skill.SkillTargetType.TARGET_CORPSE;
  }

  private static enum SummonType
  {
    PET, 
    SIEGE_SUMMON, 
    AGATHION, 
    TRAP, 
    MERCHANT;
  }
}