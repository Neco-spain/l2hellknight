package net.sf.l2j.gameserver.model.actor.stat;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.log.AbstractLogger;

public class PcStat extends PlayableStat
{
  private static final Logger _log = AbstractLogger.getLogger(L2PcInstance.class.getName());
  private int _oldMaxHp;
  private int _oldMaxMp;
  private int _oldMaxCp;
  L2PcInstance activeChar;

  public PcStat(L2PcInstance activeChar)
  {
    super(activeChar);
    this.activeChar = activeChar;
  }

  public boolean addExp(long value)
  {
    return addExp(value, false);
  }

  public boolean addExp(long value, boolean restore)
  {
    if ((!activeChar.isCursedWeaponEquiped()) && (activeChar.getKarma() > 0) && ((activeChar.isGM()) || (!activeChar.isInsideZone(1)))) {
      int karmaLost = activeChar.calculateKarmaLost(value);
      if (karmaLost > 0) {
        activeChar.setKarma(activeChar.getKarma() - karmaLost);
      }
    }

    if (!super.addExp(value, restore)) {
      return false;
    }

    activeChar.sendPacket(new UserInfo(activeChar));

    return true;
  }

  public boolean addExpAndSp(long addToExp, int addToSp)
  {
    if (activeChar.isNoExp()) {
      return false;
    }

    float ratioTakenByPet = 0.0F;

    if ((activeChar.getPet() != null) && (activeChar.getPet().isPet())) {
      L2PetInstance pet = (L2PetInstance)activeChar.getPet();
      ratioTakenByPet = pet.getPetData().getOwnerExpTaken();

      if ((ratioTakenByPet > 0.0F) && (!pet.isDead())) {
        pet.addExpAndSp(()((float)addToExp * ratioTakenByPet), (int)(addToSp * ratioTakenByPet));
      }

      if (ratioTakenByPet > 1.0F) {
        ratioTakenByPet = 1.0F;
      }
      addToExp = ()((float)addToExp * (1.0F - ratioTakenByPet));
      addToSp = (int)(addToSp * (1.0F - ratioTakenByPet));
    }

    if (!super.addExpAndSp(addToExp, addToSp)) {
      return false;
    }

    activeChar.sendChanges();
    activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber((int)addToExp).addNumber(addToSp));
    return true;
  }

  public boolean removeExpAndSp(long addToExp, int addToSp)
  {
    if (!super.removeExpAndSp(addToExp, addToSp)) {
      return false;
    }

    activeChar.sendPacket(SystemMessage.id(SystemMessageId.EXP_DECREASED_BY_S1).addNumber((int)addToExp));
    activeChar.sendPacket(SystemMessage.id(SystemMessageId.SP_DECREASED_S1).addNumber(addToSp));
    return true;
  }

  public boolean addLevel(byte value)
  {
    return addLevel(value, false);
  }

  public final boolean addLevel(byte value, boolean restore)
  {
    if (getLevel() + value > 80) {
      return false;
    }

    boolean levelIncreased = super.addLevel(value, restore);

    if (levelIncreased) {
      activeChar.setCurrentCp(getMaxCp());
      activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 15));
      activeChar.sendPacket(Static.YOU_INCREASED_YOUR_LEVEL);
    }

    activeChar.rewardSkills();
    if (activeChar.getClan() != null) {
      activeChar.getClan().updateClanMember(activeChar);
      activeChar.getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(activeChar));
    }
    if (activeChar.isInParty()) {
      activeChar.getParty().recalculatePartyLevel();
    }
    StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
    su.addAttribute(1, getLevel());
    su.addAttribute(34, getMaxCp());
    su.addAttribute(10, getMaxHp());
    su.addAttribute(12, getMaxMp());
    activeChar.sendPacket(su);

    activeChar.refreshOverloaded();

    activeChar.refreshExpertisePenalty();

    activeChar.sendPacket(new UserInfo(activeChar));

    if (!restore) {
      activeChar.reloadSkills(false);
    }
    return levelIncreased;
  }

  public boolean addSp(int value)
  {
    if (!super.addSp(value)) {
      return false;
    }

    StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
    su.addAttribute(13, getSp());
    activeChar.sendPacket(su);

    return true;
  }

  public final long getExpForLevel(int level)
  {
    return net.sf.l2j.gameserver.model.base.Experience.LEVEL[level];
  }

  public final L2PcInstance getActiveChar()
  {
    return activeChar;
  }

  public final long getExp()
  {
    if (activeChar.isSubClassActive()) {
      return ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).getExp();
    }

    return super.getExp();
  }

  public final void setExp(long value)
  {
    if (activeChar.isSubClassActive())
      ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).setExp(value);
    else
      super.setExp(value);
  }

  public final byte getLevel()
  {
    if (activeChar.isSubClassActive()) {
      return ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).getLevel();
    }

    return super.getLevel();
  }

  public final void setLevel(byte value)
  {
    if (value > 80) {
      value = 80;
    }

    if (activeChar.isSubClassActive())
      ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).setLevel(value);
    else
      super.setLevel(value);
  }

  public final int getMaxCp()
  {
    int val = super.getMaxCp();
    if (val != _oldMaxCp) {
      _oldMaxCp = val;
      if (activeChar.getStatus().getCurrentCp() != val) {
        activeChar.getStatus().setCurrentCp(activeChar.getStatus().getCurrentCp());
      }
    }
    return val;
  }

  public final int getMaxHp()
  {
    int val = super.getMaxHp();
    if (val != _oldMaxHp) {
      _oldMaxHp = val;

      if (activeChar.getStatus().getCurrentHp() != val) {
        activeChar.getStatus().setCurrentHp(activeChar.getStatus().getCurrentHp());
      }
    }

    return val;
  }

  public final int getMaxMp()
  {
    int val = super.getMaxMp();

    if (val != _oldMaxMp) {
      _oldMaxMp = val;

      if (activeChar.getStatus().getCurrentMp() != val) {
        activeChar.getStatus().setCurrentMp(activeChar.getStatus().getCurrentMp());
      }
    }

    return val;
  }

  public final int getSp()
  {
    if (activeChar.isSubClassActive()) {
      return ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).getSp();
    }

    return super.getSp();
  }

  public final void setSp(int value)
  {
    if (activeChar.isSubClassActive())
      ((SubClass)activeChar.getSubClasses().get(Integer.valueOf(activeChar.getClassIndex()))).setSp(value);
    else
      super.setSp(value);
  }

  public double getSkillMastery()
  {
    double val = calcStat(Stats.SKILL_MASTERY, 0.0D, null, null);

    if (activeChar.isMageClass())
      val *= Formulas.getINTBonus(activeChar);
    else {
      val *= Formulas.getSTRBonus(activeChar);
    }

    return val;
  }

  public int getWalkSpeed()
  {
    if (activeChar.isInWater()) {
      return Config.WATER_SPEED;
    }

    return super.getWalkSpeed();
  }

  public int getRunSpeed()
  {
    if (activeChar.isInWater()) {
      return Config.WATER_SPEED;
    }

    return super.getRunSpeed();
  }
}