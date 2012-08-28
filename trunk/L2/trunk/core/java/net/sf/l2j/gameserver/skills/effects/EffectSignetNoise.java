package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2EffectPointInstance;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSignetNoise extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public EffectSignetNoise(Env env, EffectTemplate template)
    {
        super(env, template);
    }
    
    @Override
    public EffectType getEffectType()
    {
        return EffectType.SIGNET_GROUND;
    }
    
    @Override
 	public boolean onStart()
 	{
 		_actor = (L2EffectPointInstance)getEffected();
		return true;
 	}
    
    @Override
    public boolean onActionTime()
    {
    	if (getCount() == getTotalCount() - 1) return true;

    	for (L2Character target : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
    	{
    		if (target == null) 
    			continue;
    		
    		L2Effect[] effects = target.getAllEffects();
    		if (effects != null)
    			for (L2Effect effect : effects)
    			{
    				if (effect.getSkill().isDance()) 
    					effect.exit();
    			}
    	}
        return true;
    }
    
    @Override
    public void onExit()
    {
    	if (_actor != null)
        {
            _actor.deleteMe();
        }
    }
}
