package net.sf.l2j.gameserver.skills.targets;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;

public abstract class TargetList
{
  public static TargetList create(L2Skill.SkillTargetType targetType)
  {
    switch (1.$SwitchMap$net$sf$l2j$gameserver$model$L2Skill$SkillTargetType[targetType.ordinal()])
    {
    case 1:
      return new TargetOne();
    case 2:
    case 3:
      return new TargetSelf();
    case 4:
      return new TargetHoly();
    case 5:
      return new TargetPet();
    case 6:
      return new TargetPetOwner();
    case 7:
      return new TargetPetCorpse();
    case 8:
      return new TargetAura();
    case 9:
      return new TargetArea();
    case 10:
      return new TargetMultiface();
    case 11:
      return new TargetParty();
    case 12:
      return new TargetPartyMember();
    case 13:
      return new TargetPartyOther();
    case 14:
      return new TargetAlly();
    case 15:
      return new TargetAllyCorpse();
    case 16:
      return new TargetClan();
    case 17:
      return new TargetClanCorpse();
    case 18:
      return new TargetCorpsePlayer();
    case 19:
      return new TargetMob();
    case 20:
      return new TargetMobCorpse();
    case 21:
      return new TargetMobCorpseArea();
    case 22:
      return new TargetUnlockable();
    case 23:
      return new TargetItem();
    case 24:
      return new TargetUndead();
    case 25:
      return new TargetTyrannosaurus();
    case 26:
      return new TargetAngelArea();
    case 27:
      return new TargetUndeadArea();
    case 28:
      return new TargetEnemySummon();
    }
    return new TargetDefault();
  }

  public abstract FastList<L2Object> getTargetList(FastList<L2Object> paramFastList, L2Character paramL2Character1, boolean paramBoolean, L2Character paramL2Character2, L2Skill paramL2Skill);
}