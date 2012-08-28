package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.FinishRotating;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.StartRotating;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.skills.Formulas;
import l2m.gameserver.skills.Formulas.AttackInfo;
import l2m.gameserver.templates.StatsSet;

public class PDam extends Skill
{
  private final boolean _onCrit;
  private final boolean _directHp;
  private final boolean _turner;
  private final boolean _blow;

  public PDam(StatsSet set)
  {
    super(set);
    _onCrit = set.getBool("onCrit", false);
    _directHp = set.getBool("directHp", false);
    _turner = set.getBool("turner", false);
    _blow = set.getBool("blow", false);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    boolean ss = (activeChar.getChargedSoulShot()) && (isSSPossible());

    for (Creature target : targets) {
      if ((target != null) && (!target.isDead()))
      {
        if ((_turner) && (!target.isInvul()))
        {
          target.broadcastPacket(new L2GameServerPacket[] { new StartRotating(target, target.getHeading(), 1, 65535) });
          target.broadcastPacket(new L2GameServerPacket[] { new FinishRotating(target, activeChar.getHeading(), 65535) });
          target.setHeading(activeChar.getHeading());
          target.sendPacket(new SystemMessage(110).addSkillName(_displayId, _displayLevel));
        }

        boolean reflected = target.checkReflectSkill(activeChar, this);
        Creature realTarget = reflected ? activeChar : target;

        Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, realTarget, this, false, _blow, ss, _onCrit);

        if (info.lethal_dmg > 0.0D) {
          realTarget.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
        }
        if ((!info.miss) || (info.damage >= 1.0D)) {
          realTarget.reduceCurrentHp(info.damage, activeChar, this, true, true, info.lethal ? false : _directHp, true, false, false, getPower() != 0.0D);
        }
        if (!reflected) {
          realTarget.doCounterAttack(this, activeChar, _blow);
        }
        getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
      }
    }
    if (isSuicideAttack())
      activeChar.doDie(null);
    else if (isSSPossible())
      activeChar.unChargeShots(isMagic());
  }
}