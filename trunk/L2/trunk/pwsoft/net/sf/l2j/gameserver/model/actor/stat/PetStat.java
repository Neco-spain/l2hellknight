package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class PetStat extends SummonStat
{
  public PetStat(L2PetInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addExp(int value)
  {
    if (!super.addExp(value)) {
      return false;
    }

    getActiveChar().updateAndBroadcastStatus(1);

    getActiveChar().updateEffectIcons(true);

    return true;
  }

  public boolean addExpAndSp(long addToExp, int addToSp)
  {
    if (!super.addExpAndSp(addToExp, addToSp)) {
      return false;
    }
    getActiveChar().getOwner().sendPacket(SystemMessage.id(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int)addToExp));
    return true;
  }

  public final boolean addLevel(byte value)
  {
    if (getLevel() + value > 80) {
      return false;
    }

    boolean levelIncreased = super.addLevel(value);

    if ((getExp() > getExpForLevel(getLevel() + 1)) || (getExp() < getExpForLevel(getLevel()))) {
      setExp(net.sf.l2j.gameserver.model.base.Experience.LEVEL[getLevel()]);
    }

    if (levelIncreased) {
      getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");
    }

    StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
    su.addAttribute(1, getLevel());
    su.addAttribute(10, getMaxHp());
    su.addAttribute(12, getMaxMp());
    getActiveChar().broadcastPacket(su);
    getActiveChar().broadcastPacket(new SocialAction(getActiveChar().getObjectId(), 15));

    getActiveChar().updateAndBroadcastStatus(1);

    getActiveChar().updateEffectIcons(true);
    if (getActiveChar().getControlItem() != null) {
      getActiveChar().getControlItem().setEnchantLevel(getLevel());
    }

    return levelIncreased;
  }

  public final long getExpForLevel(int level)
  {
    return L2PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level).getPetMaxExp();
  }

  public L2PetInstance getActiveChar()
  {
    return (L2PetInstance)super.getActiveChar();
  }

  public final int getFeedBattle() {
    return getActiveChar().getPetData().getPetFeedBattle();
  }

  public final int getFeedNormal() {
    return getActiveChar().getPetData().getPetFeedNormal();
  }

  public void setLevel(byte value)
  {
    getActiveChar().stopFeed();
    super.setLevel(value);

    getActiveChar().setPetData(L2PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().npcId, getLevel()));
    getActiveChar().startFeed(false);

    if (getActiveChar().getControlItem() != null)
      getActiveChar().getControlItem().setEnchantLevel(getLevel());
  }

  public final int getMaxFeed()
  {
    return getActiveChar().getPetData().getPetMaxFeed();
  }

  public int getMaxHp()
  {
    return (int)calcStat(Stats.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null);
  }

  public int getMaxMp()
  {
    return (int)calcStat(Stats.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null);
  }

  public int getMAtk(L2Character target, L2Skill skill)
  {
    double attack = getActiveChar().getPetData().getPetMAtk();
    Stats stat = skill == null ? null : skill.getStat();
    if (stat != null) {
      switch (1.$SwitchMap$net$sf$l2j$gameserver$skills$Stats[stat.ordinal()]) {
      case 1:
        attack += getActiveChar().getTemplate().baseAggression;
        break;
      case 2:
        attack += getActiveChar().getTemplate().baseBleed;
        break;
      case 3:
        attack += getActiveChar().getTemplate().basePoison;
        break;
      case 4:
        attack += getActiveChar().getTemplate().baseStun;
        break;
      case 5:
        attack += getActiveChar().getTemplate().baseRoot;
        break;
      case 6:
        attack += getActiveChar().getTemplate().baseMovement;
        break;
      case 7:
        attack += getActiveChar().getTemplate().baseConfusion;
        break;
      case 8:
        attack += getActiveChar().getTemplate().baseSleep;
        break;
      case 9:
        attack += getActiveChar().getTemplate().baseFire;
        break;
      case 10:
        attack += getActiveChar().getTemplate().baseWind;
        break;
      case 11:
        attack += getActiveChar().getTemplate().baseWater;
        break;
      case 12:
        attack += getActiveChar().getTemplate().baseEarth;
        break;
      case 13:
        attack += getActiveChar().getTemplate().baseHoly;
        break;
      case 14:
        attack += getActiveChar().getTemplate().baseDark;
      }
    }

    if (skill != null) {
      attack += skill.getPower();
    }
    return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
  }

  public int getMDef(L2Character target, L2Skill skill)
  {
    double defence = getActiveChar().getPetData().getPetMDef();
    return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
  }

  public int getPAtk(L2Character target)
  {
    return (int)calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null);
  }

  public int getPDef(L2Character target)
  {
    return (int)calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null);
  }

  public int getAccuracy()
  {
    return (int)calcStat(Stats.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null);
  }

  public int getCriticalHit(L2Character target, L2Skill skill)
  {
    return (int)calcStat(Stats.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null);
  }

  public int getEvasionRate(L2Character target)
  {
    return (int)calcStat(Stats.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null);
  }

  public int getRunSpeed()
  {
    return (int)calcStat(Stats.RUN_SPEED, getActiveChar().getPetData().getPetSpeed(), null, null);
  }

  public int getPAtkSpd()
  {
    return (int)calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null);
  }

  public int getMAtkSpd()
  {
    return (int)calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null);
  }
}