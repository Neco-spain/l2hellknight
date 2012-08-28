package net.sf.l2j.gameserver.skills.effects;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2EffectPointInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Point3D;

public class EffectSignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;
	
	public EffectSignetMDam(Env env, EffectTemplate template)
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
    	L2NpcTemplate template;
    	if (getSkill() instanceof L2SkillSignetCasttime)
		{
 	    	template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime)getSkill())._effectNpcId);
		}
    	else return false;
        
        L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(),  template,  getEffector());
        effectPoint.setCurrentHp(effectPoint.getMaxHp());
        effectPoint.setCurrentMp(effectPoint.getMaxMp());
        L2World.getInstance().storeObject(effectPoint);
        
        int x = getEffector().getX();
        int y = getEffector().getY();
        int z = getEffector().getZ();
        
        if (getEffector() instanceof L2PcInstance && getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
        {
            Point3D wordPosition = ((L2PcInstance)getEffector()).getCurrentSkillWorldPosition();
            
            if (wordPosition != null)
            {
                x = wordPosition.getX();
                y = wordPosition.getY();
                z = wordPosition.getZ();
            }
        }
        effectPoint.setIsInvul(true);
        effectPoint.spawnMe(x, y, z);
    	
    	_actor = effectPoint;
		return true;
 	}

    @Override
    public boolean onActionTime()
    {
    	if (getCount() >= getTotalCount() - 2) return true;
        int mpConsume = getSkill().getMpConsume();
        
        L2PcInstance caster = (L2PcInstance)getEffector();
        
        boolean ss = false;
        boolean bss = false;
        
        L2ItemInstance weaponInst = caster.getActiveWeaponInstance();
        
        FastList<L2Character> targets = new FastList<L2Character>();
        
        for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
        {
            if (cha == null || cha == caster)
                continue;
            
            if (cha instanceof L2Attackable || cha instanceof L2PlayableInstance)
            {
                if (cha.isAlikeDead())
                    continue;
                
                if (mpConsume > caster.getCurrentMp())
                {
                    caster.sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
                    return false;
                }
                else
                    caster.reduceCurrentMp(mpConsume);
                
                if (cha instanceof L2PcInstance || cha instanceof L2Summon)
                	caster.updatePvPStatus(cha);
                
                targets.add(cha);
            }
        }
        
		if (weaponInst != null)
        {
            switch (weaponInst.getChargedSpiritshot())
            {
                case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
                        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                        bss = true;
                    break;
                case L2ItemInstance.CHARGED_SPIRITSHOT:
                        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                        ss = true;
                    break;
            }
        }

		if (!bss && !ss)
			caster.rechargeAutoSoulShot(false, true, false);

        if (targets.size() > 0)
        {
            caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
            for (L2Character target : targets)
            {
                boolean mcrit = Formulas.getInstance().calcMCrit(caster.getMCriticalHit(target, getSkill()));
                int mdam = (int)Formulas.getInstance().calcMagicDam(caster, target, getSkill(), ss, bss, mcrit);
                
                if (target instanceof L2Summon)
                {
                    if (caster.equals(((L2Summon)target).getOwner()))
                        caster.sendPacket(new PetInfo((L2Summon)target));
                    else
                        caster.sendPacket(new NpcInfo((L2Summon)target, caster));
                }
                
                if (mdam > 0)
                {
                    if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, mdam))
                    {
                        target.breakAttack();
                        target.breakCast();
                    }
                    caster.sendDamageMessage(target, mdam, mcrit, false, false);
                    target.reduceCurrentHp(mdam, caster);
                }
                target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
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
