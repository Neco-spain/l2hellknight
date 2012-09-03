/**
 * 
 */
package l2rt.gameserver.skills;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.skills.effects.*;

import java.lang.reflect.Constructor;

public enum EffectType
{
	// Основные эффекты
	AddSkills(EffectAddSkills.class, null, false),
	Aggression(EffectAggression.class, null, true),
	TargetToMe(EffectTargetToMe.class, null, true),
	Betray(EffectBetray.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	BlessNoblesse(EffectBlessNoblesse.class, null, true),
	BlockStat(EffectBlockStat.class, null, true),
	Buff(EffectBuff.class, null, false),
	BuffImmunity(EffectBuffImmunity.class, null, true),
	CallSkills(EffectCallSkills.class, null, false),
	ClanGate(EffectClanGate.class, null, false),
	CombatPointHealOverTime(EffectCombatPointHealOverTime.class, null, true),
	ConsumeSoulsOverTime(EffectConsumeSoulsOverTime.class, null, true),
	CharmOfCourage(EffectCharmOfCourage.class, null, true),
	CPDamPercent(EffectCPDamPercent.class, null, true),
	DamOverTime(EffectDamOverTime.class, null, false),
	DamOverTimeLethal(EffectDamOverTimeLethal.class, null, false),
	DestroySummon(EffectDestroySummon.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Disarm(EffectDisarm.class, null, true),
	Discord(EffectDiscord.class, AbnormalEffect.CONFUSED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Enervation(EffectEnervation.class, null, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, false),
	FakeDeath(EffectFakeDeath.class, null, true),
	Fear(EffectFear.class, AbnormalEffect.AFFRAID, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	Grow(EffectGrow.class, null, false),
	Heal(EffectHeal.class, null, false),
	HealBlock(EffectHealBlock.class, null, true),
	HealCPPercent(EffectHealCPPercent.class, null, true),
	HealHPCP(EffectHealHPCP.class, null, false),
	HealOverTime(EffectHealOverTime.class, null, false),
	HealPercent(EffectHealPercent.class, null, false),
	ImobileBuff(EffectImobileBuff.class, null, true),
	Interrupt(EffectInterrupt.class, null, true),
	Invulnerable(EffectInvulnerable.class, null, false),
	Invisible(EffectInvisible.class, null, false),
	CurseOfLifeFlow(EffectCurseOfLifeFlow.class, null, true),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, null, true),
	ManaDamOverTime(EffectManaDamOverTime.class, null, true),
	ManaHeal(EffectManaHeal.class, null, false),
	ManaHealOverTime(EffectManaHealOverTime.class, null, false),
	ManaHealPercent(EffectManaHealPercent.class, null, false),
	Meditation(EffectMeditation.class, null, false),
	Mute(EffectMute.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MuteAll(EffectMuteAll.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MuteAttack(EffectMuteAttack.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	MutePhisycal(EffectMutePhisycal.class, AbnormalEffect.MUTED, Stats.MENTAL_RECEPTIVE, Stats.MENTAL_POWER, true),
	NegateEffects(EffectNegateEffects.class, null, false),
	NegateMusic(EffectNegateMusic.class, null, false),
	Paralyze(EffectParalyze.class, AbnormalEffect.HOLD_1, Stats.PARALYZE_RECEPTIVE, Stats.PARALYZE_POWER, true),
	Petrification(EffectPetrification.class, AbnormalEffect.HOLD_2, Stats.PARALYZE_RECEPTIVE, Stats.PARALYZE_POWER, true),
	Relax(EffectRelax.class, null, true),
	Root(EffectRoot.class, AbnormalEffect.ROOT, Stats.ROOT_RECEPTIVE, Stats.ROOT_POWER, true),
	Salvation(EffectSalvation.class, null, true),
	SilentMove(EffectSilentMove.class, AbnormalEffect.STEALTH, true),
	Sleep(EffectSleep.class, AbnormalEffect.SLEEP, Stats.SLEEP_RECEPTIVE, Stats.SLEEP_POWER, true),
	Stun(EffectStun.class, AbnormalEffect.STUN, Stats.STUN_RECEPTIVE, Stats.STUN_POWER, true),
	Symbol(EffectSymbol.class, null, false),
	Transformation(EffectTransformation.class, null, true),
	TransformationAll(EffectTransformationAll.class, null, true),
	Turner(EffectTurner.class, AbnormalEffect.STUN, Stats.STUN_RECEPTIVE, Stats.STUN_POWER, true),
	UnAggro(EffectUnAggro.class, null, true),
	Vitality(EffectBuff.class, AbnormalEffect.VITALITY, true),

	// Производные от основных эффектов
	Poison(EffectDamOverTime.class, null, Stats.POISON_RECEPTIVE, Stats.POISON_POWER, false),
	PoisonLethal(EffectDamOverTimeLethal.class, null, Stats.POISON_RECEPTIVE, Stats.POISON_POWER, false),
	Bleed(EffectDamOverTime.class, null, Stats.BLEED_RECEPTIVE, Stats.BLEED_POWER, false),
	Debuff(EffectBuff.class, null, false),
	SoulRetain(EffectBuff.class, null, false),
	WatcherGaze(EffectBuff.class, null, false),
	TransferDam(EffectBuff.class, null, false);

	private final Class<? extends L2Effect> clazz;
	private final AbnormalEffect abnormal;
	private final Stats resistType;
	private final Stats attibuteType;
	private final boolean isRaidImmune;

	private EffectType(Class<? extends L2Effect> clazz, AbnormalEffect abnormal, boolean isRaidImmune)
	{
		this(clazz, abnormal, null, null, isRaidImmune);
	}

	private EffectType(Class<? extends L2Effect> clazz, AbnormalEffect abnormal, Stats resistType, Stats attibuteType, boolean isRaidImmune)
	{
		this.clazz = clazz;
		this.abnormal = abnormal;
		this.resistType = resistType;
		this.attibuteType = attibuteType;
		this.isRaidImmune = isRaidImmune;
	}

	public AbnormalEffect getAbnormal()
	{
		return abnormal;
	}

	public Stats getResistType()
	{
		return resistType;
	}

	public Stats getAttibuteType()
	{
		return attibuteType;
	}

	public boolean isRaidImmune()
	{
		return isRaidImmune;
	}

	public L2Effect makeEffect(Env env, EffectTemplate template)
	{
		try
		{
			Constructor<? extends L2Effect> c = clazz.getConstructor(Env.class, EffectTemplate.class);
			return c.newInstance(env, template);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}