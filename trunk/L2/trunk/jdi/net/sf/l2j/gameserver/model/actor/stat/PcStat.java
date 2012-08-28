package net.sf.l2j.gameserver.model.actor.stat;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class PcStat extends PlayableStat
{
  private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
  private int _oldMaxHp;
  private int _oldMaxMp;
  private int _oldMaxCp;

  public PcStat(L2PcInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addExp(long value)
  {
    L2PcInstance activeChar = getActiveChar();

    if ((!activeChar.isCursedWeaponEquiped()) && (activeChar.getKarma() > 0) && ((activeChar.isGM()) || (!activeChar.isInsideZone(1))))
    {
      int karmaLost = activeChar.calculateKarmaLost(value);
      if (karmaLost > 0) activeChar.setKarma(activeChar.getKarma() - karmaLost);
    }
    if ((getActiveChar().isGM()) && (getActiveChar().getAccessLevel() <= Config.GM_DONT_TAKE_EXPSP) && (getActiveChar().isInParty())) {
      return false;
    }
    if (!super.addExp(value)) return false;
    activeChar.sendPacket(new UserInfo(activeChar));

    return true;
  }

  public boolean addExpAndSp(long addToExp, int addToSp)
  {
    float ratioTakenByPet = 0.0F;
    L2PcInstance activeChar = getActiveChar();
    if ((activeChar.isGM()) && (activeChar.getAccessLevel() <= Config.GM_DONT_TAKE_EXPSP) && (activeChar.isInParty())) {
      return false;
    }
    if ((activeChar.getPet() instanceof L2PetInstance))
    {
      L2PetInstance pet = (L2PetInstance)activeChar.getPet();
      ratioTakenByPet = pet.getPetData().getOwnerExpTaken();
      if ((ratioTakenByPet > 0.0F) && (!pet.isDead()))
        pet.addExpAndSp(()((float)addToExp * ratioTakenByPet), (int)(addToSp * ratioTakenByPet));
      if (ratioTakenByPet > 1.0F)
        ratioTakenByPet = 1.0F;
      addToExp = ()((float)addToExp * (1.0F - ratioTakenByPet));
      addToSp = (int)(addToSp * (1.0F - ratioTakenByPet));
    }

    if (!super.addExpAndSp(addToExp, addToSp)) return false;

    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP);
    sm.addNumber((int)addToExp);
    sm.addNumber(addToSp);
    getActiveChar().sendPacket(sm);

    return true;
  }

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

  public final boolean addLevel(byte value)
  {
    if (getLevel() + value > 80) return false;

    boolean levelIncreased = super.addLevel(value);

    if (levelIncreased)
    {
      QuestState qs = getActiveChar().getQuestState("255_Tutorial");
      if (qs != null) {
        qs.getQuest().notifyEvent("CE40", null, getActiveChar());
      }
      getActiveChar().setCurrentCp(getMaxCp());
      getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));
      getActiveChar().sendPacket(new SystemMessage(SystemMessageId.YOU_INCREASED_YOUR_LEVEL));
    }

    getActiveChar().rewardSkills();
    if (getActiveChar().getClan() != null)
    {
      getActiveChar().getClan().updateClanMember(getActiveChar());
      getActiveChar().getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(getActiveChar()));
    }
    if (getActiveChar().isInParty()) getActiveChar().getParty().recalculatePartyLevel();

    StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
    su.addAttribute(1, getLevel());
    su.addAttribute(34, getMaxCp());
    su.addAttribute(10, getMaxHp());
    su.addAttribute(12, getMaxMp());
    getActiveChar().sendPacket(su);

    getActiveChar().refreshOverloaded();
    getActiveChar().refreshExpertisePenalty();
    getActiveChar().sendPacket(new UserInfo(getActiveChar()));

    return levelIncreased;
  }

  public boolean addSp(int value)
  {
    if (!super.addSp(value)) return false;

    StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
    su.addAttribute(13, getSp());
    getActiveChar().sendPacket(su);

    return true;
  }

  public final long getExpForLevel(int level) {
    return net.sf.l2j.gameserver.model.base.Experience.LEVEL[level];
  }
  public final L2PcInstance getActiveChar() {
    return (L2PcInstance)super.getActiveChar();
  }

  public final long getExp()
  {
    if (getActiveChar().isSubClassActive()) {
      return ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).getExp();
    }
    return super.getExp();
  }

  public final void setExp(long value)
  {
    if (getActiveChar().isSubClassActive())
      ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).setExp(value);
    else
      super.setExp(value);
  }

  public final byte getLevel()
  {
    if (getActiveChar().isSubClassActive()) {
      return ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).getLevel();
    }
    return super.getLevel();
  }

  public final void setLevel(byte value)
  {
    if (value > 80) {
      value = 80;
    }
    if (getActiveChar().isSubClassActive())
      ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).setLevel(value);
    else
      super.setLevel(value);
  }

  public final int getMaxHp()
  {
    int val = super.getMaxHp();
    if (val != _oldMaxHp)
    {
      _oldMaxHp = val;
      if (getActiveChar().getStatus().getCurrentHp() != val) getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp());
    }

    return val;
  }

  public final int getMaxMp()
  {
    int val = super.getMaxMp();

    if (val != _oldMaxMp)
    {
      _oldMaxMp = val;

      if (getActiveChar().getStatus().getCurrentMp() != val) {
        getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp());
      }
    }
    return val;
  }

  public final int getSp()
  {
    if (getActiveChar().isSubClassActive()) {
      return ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).getSp();
    }
    return super.getSp();
  }

  public final void setSp(int value)
  {
    if (getActiveChar().isSubClassActive())
      ((SubClass)getActiveChar().getSubClasses().get(Integer.valueOf(getActiveChar().getClassIndex()))).setSp(value);
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