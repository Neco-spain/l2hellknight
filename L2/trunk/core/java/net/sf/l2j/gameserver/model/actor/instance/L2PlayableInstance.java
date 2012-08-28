package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.knownlist.PlayableKnownList;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2j.gameserver.templates.L2CharTemplate;

public abstract class L2PlayableInstance extends L2Character
{

	private boolean _isNoblesseBlessed = false;
	private boolean _getCharmOfLuck = false;
	private boolean _isPhoenixBlessed = false;
	private boolean _isSilentMoving = false;
	private boolean _protectionBlessing = false;

	public L2PlayableInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getKnownList();
        getStat();
        getStatus();
	}

    @Override
	public PlayableKnownList getKnownList()
    {
    	if(super.getKnownList() == null || !(super.getKnownList() instanceof PlayableKnownList))
    		setKnownList(new PlayableKnownList(this));
    	return (PlayableKnownList)super.getKnownList();
    }

    @Override
	public PlayableStat getStat()
    {
    	if(super.getStat() == null || !(super.getStat() instanceof PlayableStat))
    		setStat(new PlayableStat(this));
    	return (PlayableStat)super.getStat();
    }

    @Override
	public PlayableStatus getStatus()
    {
    	if(super.getStatus() == null || !(super.getStatus() instanceof PlayableStatus))
    		setStatus(new PlayableStatus(this));
    	return (PlayableStatus)super.getStatus();
    }

    @Override
	public boolean doDie(L2Character killer)
    {
    	if (!super.doDie(killer))
    		return false;

    	if (killer != null)
        {
            L2PcInstance player = null;
            if (killer instanceof L2PcInstance)
                player = (L2PcInstance)killer;
            else if (killer instanceof L2Summon)
                player = ((L2Summon)killer).getOwner();

            if (player != null) player.onKillUpdatePvPKarma(this);
        }
    	return true;
    }

    public boolean checkIfPvP(L2Character target)
    {
        if (target == null) return false;                                               // Target is null
        if (target == this) return false;                                               // Target is self
        if (!(target instanceof L2PlayableInstance)) return false;                      // Target is not a L2PlayableInstance

        L2PcInstance player = null;
        if (this instanceof L2PcInstance)
            player = (L2PcInstance)this;
        else if (this instanceof L2Summon)
            player = ((L2Summon)this).getOwner();

        if (player == null) return false;                                               // Active player is null
        if (player.getKarma() != 0) return false;                                       // Active player has karma

        L2PcInstance targetPlayer = null;
        if (target instanceof L2PcInstance)
            targetPlayer = (L2PcInstance)target;
        else if (target instanceof L2Summon)
            targetPlayer = ((L2Summon)target).getOwner();

        if (targetPlayer == null) return false;                                         // Target player is null
        if (targetPlayer == this) return false;                                         // Target player is self
        if (targetPlayer.getKarma() != 0) return false;                                 // Target player has karma
        if (targetPlayer.getPvpFlag() == 0) return false;

        return true;
    }

    @Override
	public boolean isAttackable()
    {
        return true;
    }

    public final boolean isNoblesseBlessed() { return _isNoblesseBlessed; }
    public final void setIsNoblesseBlessed(boolean value) { _isNoblesseBlessed = value; }

    public final void startNoblesseBlessing()
    {
    	setIsNoblesseBlessed(true);
    	updateAbnormalEffect();
    }

    public final void stopNoblesseBlessing(L2Effect effect)
    {
    	if (effect == null)
    		stopEffects(L2Effect.EffectType.NOBLESSE_BLESSING);
    	else
    		removeEffect(effect);

    	setIsNoblesseBlessed(false);
    	updateAbnormalEffect();
    }
    
	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}

	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}

	public final boolean getProtectionBlessing()
	{
		return _protectionBlessing;
	}

	public final void setProtectionBlessing(boolean value)
	{
		_protectionBlessing = value;
	}

	public void startProtectionBlessing()
	{
		setProtectionBlessing(true);
		updateAbnormalEffect();
	}

	public void stopProtectionBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PROTECTION_BLESSING);
		else
			removeEffect(effect);

		setProtectionBlessing(false);
		updateAbnormalEffect();
	}

    public final boolean isPhoenixBlessed() { return _isPhoenixBlessed; }
    public final void setIsPhoenixBlessed(boolean value) { _isPhoenixBlessed = value; }
         
    public final void startPhoenixBlessing()
    {
       setIsPhoenixBlessed(true);
       updateAbnormalEffect();
    }
    
    public final void stopPhoenixBlessing(L2Effect effect)
    {
       if (effect == null)
          stopEffects(L2Effect.EffectType.PHOENIX_BLESSING);
       else
          removeEffect(effect);
                  
       setIsPhoenixBlessed(false);
       updateAbnormalEffect();
    }

	public abstract boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage);
	public abstract boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage);

	public final boolean getCharmOfLuck() { return _getCharmOfLuck; }
	public final void setCharmOfLuck(boolean value) { _getCharmOfLuck = value; }

	public final void startCharmOfLuck()
	{
		setCharmOfLuck(true);
		updateAbnormalEffect();
	}

	public final void stopCharmOfLuck(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CHARM_OF_LUCK);
		else
			removeEffect(effect);

		setCharmOfLuck(false);
		updateAbnormalEffect();
	}
}
