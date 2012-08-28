package net.sf.l2j.gameserver.model.actor.stat;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class PcStat extends PlayableStat
{
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());

    private int _oldMaxHp;
    private int _oldMaxMp;
    private int _oldMaxCp;
    public PcStat(L2PcInstance activeChar)
    {
        super(activeChar);
    }
    @Override
	public boolean addExp(long value)
    {
    	L2PcInstance activeChar = getActiveChar();

        if (!activeChar.isCursedWeaponEquiped() && activeChar.getKarma() > 0 && (activeChar.isGM() || !activeChar.isInsideZone(L2Character.ZONE_PVP)))
        {
            int karmaLost = activeChar.calculateKarmaLost(value);
            if (karmaLost > 0) activeChar.setKarma(activeChar.getKarma() - karmaLost);
        }
        if (getActiveChar().isGM() && getActiveChar().getAccessLevel() <= Config.GM_DONT_TAKE_EXPSP && getActiveChar().isInParty())
              return false;

		if (!super.addExp(value)) return false;
        activeChar.sendPacket(new UserInfo(activeChar));

        return true;
    }
    @Override
	public boolean addExpAndSp(long addToExp, int addToSp)
    {
    	float ratioTakenByPet = 0;
    	L2PcInstance activeChar = getActiveChar();
    	if (activeChar.isGM() && activeChar.getAccessLevel() <= Config.GM_DONT_TAKE_EXPSP && activeChar.isInParty())
    	     return false;

    	if (activeChar.getPet() instanceof L2PetInstance )
    	{
    		L2PetInstance pet = (L2PetInstance) activeChar.getPet();
    		ratioTakenByPet = pet.getPetData().getOwnerExpTaken();
    		if (ratioTakenByPet > 0 && !pet.isDead())
    			pet.addExpAndSp((long)(addToExp*ratioTakenByPet), (int)(addToSp*ratioTakenByPet));
    		if (ratioTakenByPet > 1)
    			ratioTakenByPet = 1;
    		addToExp = (long)(addToExp*(1-ratioTakenByPet));
    		addToSp = (int)(addToSp*(1-ratioTakenByPet));
    	}

    	if ( !super.addExpAndSp(addToExp, addToSp) ) return false;

        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
        sm.addNumber((int)addToExp);
        sm.addNumber(addToSp);
        getActiveChar().sendPacket(sm);

        return true;
    }

    @Override
	public boolean removeExpAndSp(long addToExp, int addToSp)
    {
        if (!super.removeExpAndSp(addToExp, addToSp)) return false;

        SystemMessage sm = new SystemMessage(SystemMessageId.EXP_DECREASED_BY_S1);
        sm.addNumber((int)addToExp);
        getActiveChar().sendPacket(sm);
        sm = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
        sm.addNumber(addToSp);
        getActiveChar().sendPacket(sm);
        return true;
    }

    @Override
	public final boolean addLevel(byte value)
    {
		if (getLevel() + value > Experience.MAX_LEVEL - 1) return false;

        boolean levelIncreased = super.addLevel(value);

        if (levelIncreased)
        {
        	QuestState qs = getActiveChar().getQuestState("255_Tutorial"); 
        		if (qs != null)
        			qs.getQuest().notifyEvent("CE40", null, getActiveChar());

        	getActiveChar().setCurrentCp(getMaxCp());
            getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
            getActiveChar().sendPacket(new SystemMessage(SystemMessageId.YOU_INCREASED_YOUR_LEVEL));
        }

        getActiveChar().rewardSkills(); // Give Expertise skill of this level
        if (getActiveChar().getClan() != null)
        {
        	getActiveChar().getClan().updateClanMember(getActiveChar());
        	getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
        }
        if (getActiveChar().isInParty()) getActiveChar().getParty().recalculatePartyLevel(); // Recalculate the party level

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
        getActiveChar().sendPacket(su);

        getActiveChar().refreshOverloaded();
        getActiveChar().refreshExpertisePenalty();
        getActiveChar().sendPacket(new UserInfo(getActiveChar()));

        return levelIncreased;
    }

    @Override
	public boolean addSp(int value)
    {
        if (!super.addSp(value)) return false;

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.SP, getSp());
        getActiveChar().sendPacket(su);

        return true;
    }

    @Override
	public final long getExpForLevel(int level) { return Experience.LEVEL[level]; }

    @Override
	public final L2PcInstance getActiveChar() { return (L2PcInstance)super.getActiveChar(); }

    @Override
	public final long getExp()
    {
        if (getActiveChar().isSubClassActive()) 
	        return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getExp();
        
        return super.getExp();
    }
    
    @Override
	public final void setExp(long value)
    {
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setExp(value);
        else
            super.setExp(value);
    }

    @Override
	public final byte getLevel()
    {
        if (getActiveChar().isSubClassActive()) 
        	return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getLevel();
        	
        return super.getLevel();
    }
    @Override
	public final void setLevel(byte value)
    {
		if (value > Experience.MAX_LEVEL - 1) 
			value = (byte) (Experience.MAX_LEVEL - 1);
        	
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setLevel(value);
        else
            super.setLevel(value);
    }

    @Override
	public final int getMaxHp()
    {
        int val = super.getMaxHp();
        if (val != _oldMaxHp)
        {
            _oldMaxHp = val;
            if (getActiveChar().getStatus().getCurrentHp() != val) getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
        }

        return val;
    }

    @Override
	public final int getMaxMp()
    {
        int val = super.getMaxMp();
        
        if (val != _oldMaxMp)
        {
            _oldMaxMp = val;

            if (getActiveChar().getStatus().getCurrentMp() != val) 
            	getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp());
        }

        return val;
    }

    @Override
	public final int getSp()
    {
        if (getActiveChar().isSubClassActive()) 
        	return getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).getSp();
        	
        return super.getSp();
    }
    @Override
	public final void setSp(int value)
    {
        if (getActiveChar().isSubClassActive())
            getActiveChar().getSubClasses().get(getActiveChar().getClassIndex()).setSp(value);
        else
            super.setSp(value);
    }
    
    public int getMaxCp()  
 	{  
    	int val = super.getMaxCp();  
 	        if (val != _oldMaxCp)  
 	        {  
 	          _oldMaxCp = val;  
 	               if (getActiveChar().getStatus().getCurrentCp() != val)  
 	               {  
 		              getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp());  
 	               }  
 	        }  
 	    return val;  
    }  
}
