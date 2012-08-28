package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.knownlist.PlayableKnownList;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2j.gameserver.templates.L2CharTemplate;

public abstract class L2PlayableInstance extends L2Character
{
  private boolean _isNoblesseBlessed = false;
  private boolean _getCharmOfLuck = false;
  private boolean _isPhoenixBlessed = false;
  private boolean _ProtectionBlessing = false;

  private boolean _donator = false;

  public L2PlayableInstance(int objectId, L2CharTemplate template)
  {
    super(objectId, template);
    getKnownList();
    getStat();
    getStatus();
  }

  public PlayableKnownList getKnownList()
  {
    if ((super.getKnownList() == null) || (!(super.getKnownList() instanceof PlayableKnownList))) {
      setKnownList(new PlayableKnownList(this));
    }
    return (PlayableKnownList)super.getKnownList();
  }

  public PlayableStat getStat()
  {
    if ((super.getStat() == null) || (!(super.getStat() instanceof PlayableStat))) {
      setStat(new PlayableStat(this));
    }
    return (PlayableStat)super.getStat();
  }

  public PlayableStatus getStatus()
  {
    if ((super.getStatus() == null) || (!(super.getStatus() instanceof PlayableStatus))) {
      setStatus(new PlayableStatus(this));
    }
    return (PlayableStatus)super.getStatus();
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    if (killer != null) {
      killer.onKillUpdatePvPKarma(this);
    }

    return true;
  }

  public boolean checkIfPvP(L2Character target) {
    if (target == null) {
      return false;
    }
    if (target == this) {
      return false;
    }
    if (!target.isL2Playable()) {
      return false;
    }
    L2PcInstance player = getPlayer();
    if (player == null) {
      return false;
    }
    if (player.getKarma() != 0) {
      return false;
    }
    L2PcInstance targetPlayer = null;
    if (target.isPlayer())
      targetPlayer = (L2PcInstance)target;
    else if (target.isL2Summon()) {
      targetPlayer = target.getOwner();
    }

    if (targetPlayer == null) {
      return false;
    }
    if (targetPlayer == this) {
      return false;
    }
    if (targetPlayer.getKarma() != 0) {
      return false;
    }

    return targetPlayer.getPvpFlag() != 0;
  }

  public boolean isAttackable()
  {
    return true;
  }

  public final boolean isNoblesseBlessed()
  {
    return _isNoblesseBlessed;
  }

  public final void setIsNoblesseBlessed(boolean value) {
    _isNoblesseBlessed = value;
  }

  public final void startNoblesseBlessing() {
    setIsNoblesseBlessed(true);
    updateAbnormalEffect();
  }

  public final void stopNoblesseBlessing(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.NOBLESSE_BLESSING);
    else {
      removeEffect(effect);
    }

    setIsNoblesseBlessed(false);
    updateAbnormalEffect();
  }

  public final boolean isPhoenixBlessed()
  {
    return _isPhoenixBlessed;
  }

  public final void setIsPhoenixBlessed(boolean value) {
    _isPhoenixBlessed = value;
  }

  public final void startPhoenixBlessing() {
    setIsPhoenixBlessed(true);
    updateAbnormalEffect();
  }

  public final void stopPhoenixBlessing(L2Effect effect) {
    if (effect == null)
      stopEffects(L2Effect.EffectType.PHOENIX_BLESSING);
    else {
      removeEffect(effect);
    }

    setIsPhoenixBlessed(false);
    updateAbnormalEffect();
  }

  public final boolean getProtectionBlessing()
  {
    return _ProtectionBlessing;
  }

  public final void setProtectionBlessing(boolean value) {
    _ProtectionBlessing = value;
  }

  public void startProtectionBlessing() {
    setProtectionBlessing(true);
    updateAbnormalEffect();
  }

  public void stopProtectionBlessing(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.PROTECTION_BLESSING);
    else {
      removeEffect(effect);
    }

    setProtectionBlessing(false);
    updateAbnormalEffect();
  }

  public void setDonator(boolean value)
  {
    _donator = value;
  }

  public boolean isDonator()
  {
    return _donator;
  }

  public abstract boolean destroyItemByItemId(String paramString, int paramInt1, int paramInt2, L2Object paramL2Object, boolean paramBoolean);

  public abstract boolean destroyItem(String paramString, int paramInt1, int paramInt2, L2Object paramL2Object, boolean paramBoolean);

  public final boolean getCharmOfLuck()
  {
    return _getCharmOfLuck;
  }

  public final void setCharmOfLuck(boolean value) {
    _getCharmOfLuck = value;
  }

  public final void startCharmOfLuck() {
    setCharmOfLuck(true);
    updateAbnormalEffect();
  }

  public final void stopCharmOfLuck(L2Effect effect)
  {
    if (effect == null)
      stopEffects(L2Effect.EffectType.CHARM_OF_LUCK);
    else {
      removeEffect(effect);
    }

    setCharmOfLuck(false);
    updateAbnormalEffect();
  }

  public boolean isL2Playable()
  {
    return true;
  }
}