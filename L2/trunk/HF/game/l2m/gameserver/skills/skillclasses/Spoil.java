package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.MonsterInstance;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.skills.Formulas;
import l2m.gameserver.skills.Formulas.AttackInfo;
import l2m.gameserver.templates.StatsSet;

public class Spoil extends Skill
{
  public Spoil(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (!activeChar.isPlayer()) {
      return;
    }
    int ss = isSSPossible() ? 0 : activeChar.getChargedSoulShot() ? 2 : isMagic() ? activeChar.getChargedSpiritShot() : 0;
    if ((ss > 0) && (getPower() > 0.0D)) {
      activeChar.unChargeShots(false);
    }
    for (Creature target : targets)
      if ((target != null) && (!target.isDead()))
      {
        if (target.isMonster()) {
          if (((MonsterInstance)target).isSpoiled()) {
            activeChar.sendPacket(Msg.ALREADY_SPOILED);
          }
          else {
            MonsterInstance monster = (MonsterInstance)target;
            boolean success;
            boolean success;
            if (!Config.ALT_SPOIL_FORMULA)
            {
              int monsterLevel = monster.getLevel();
              int modifier = Math.abs(monsterLevel - activeChar.getLevel());
              double rateOfSpoil = Config.BASE_SPOIL_RATE;

              if (modifier > 8) {
                rateOfSpoil -= rateOfSpoil * (modifier - 8) * 9.0D / 100.0D;
              }
              rateOfSpoil = rateOfSpoil * getMagicLevel() / monsterLevel;

              if (rateOfSpoil < Config.MINIMUM_SPOIL_RATE)
                rateOfSpoil = Config.MINIMUM_SPOIL_RATE;
              else if (rateOfSpoil > 99.0D) {
                rateOfSpoil = 99.0D;
              }
              if (((Player)activeChar).isGM())
                activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Spoil.Chance", (Player)activeChar, new Object[0]).addNumber(()rateOfSpoil));
              success = Rnd.chance(rateOfSpoil);
            }
            else {
              success = Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate());
            }
            if ((success) && (monster.setSpoiled((Player)activeChar)))
              activeChar.sendPacket(Msg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
            else
              activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
          }
        }
        if (getPower() > 0.0D)
        {
          double damage;
          double damage;
          if (isMagic()) {
            damage = Formulas.calcMagicDam(activeChar, target, this, ss);
          }
          else {
            Formulas.AttackInfo info = Formulas.calcPhysDam(activeChar, target, this, false, false, ss > 0, false);
            damage = info.damage;

            if (info.lethal_dmg > 0.0D) {
              target.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
            }
          }
          target.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
          target.doCounterAttack(this, activeChar, false);
        }

        getEffects(activeChar, target, false, false);

        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Integer.valueOf(Math.max(_effectPoint, 1)));
      }
  }
}