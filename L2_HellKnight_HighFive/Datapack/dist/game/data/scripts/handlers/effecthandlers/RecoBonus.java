package handlers.effecthandlers;

import l2.hellknight.gameserver.model.effects.L2Effect;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.stats.Env;
import l2.hellknight.gameserver.model.effects.EffectTemplate;
import l2.hellknight.gameserver.model.effects.L2EffectType;

public class RecoBonus extends L2Effect {
        
        public RecoBonus(Env env, EffectTemplate template)
        {
                super(env, template);
        }

       @Override
        public L2EffectType getEffectType()
        {
                return L2EffectType.BUFF;
        }
        
        @Override
        public boolean onStart()
        {
                if (!(getEffected() instanceof L2PcInstance))
                        return false;
                
                ((L2PcInstance) getEffected()).setRecomBonusType(1).setRecoBonusActive(true);
                return true;
        }
        
       @Override
        public void onExit()
        {
                ((L2PcInstance) getEffected()).setRecomBonusType(0).setRecoBonusActive(false);
        }
        
        @Override
        protected boolean effectCanBeStolen()
        {
                return false;
        }
        
       @Override
        public boolean onActionTime()
        {
                return false;
        }       
        
}