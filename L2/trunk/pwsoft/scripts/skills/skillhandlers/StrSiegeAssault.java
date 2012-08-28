package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import scripts.skills.ISkillHandler;

public class StrSiegeAssault
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.STRSIEGEASSAULT };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if (!activeChar.isRiding()) return;
    if (!player.getTarget().isL2Door()) return;

    Castle castle = CastleManager.getInstance().getCastle(player);
    if ((castle == null) || (!checkIfOkToUseStriderSiegeAssault(player, castle, true))) return;

    try
    {
      L2ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
      if (!player.destroyItem("Consume", itemToTake.getObjectId(), skill.getItemConsume(), null, true)) {
        return;
      }

      damage = 0;

      n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
      {
        L2Character target = (L2Character)n.getValue();
        L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
        if ((activeChar.isPlayer()) && (target.isPlayer()) && (target.isAlikeDead()) && (target.isFakeDeath()))
        {
          target.stopFakeDeath(null);
        }
        else if (target.isAlikeDead()) {
            continue;
          }
        boolean dual = activeChar.isUsingDualWeapon();
        boolean shld = Formulas.calcShldUse(activeChar, target);
        boolean crit = Formulas.calcCrit(activeChar.getCriticalHit(target, skill));
        boolean soul = (weapon != null) && (weapon.getChargedSoulshot() == 1) && (weapon.getItemType() != L2WeaponType.DAGGER);

        if ((!crit) && ((skill.getCondition() & 0x10) != 0))
          damage = 0;
        else {
          damage = (int)Formulas.calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
        }
        if (damage > 0)
        {
          target.reduceCurrentHp(damage, activeChar);
          if ((soul) && (weapon != null)) {
            weapon.setChargedSoulshot(0);
          }
          activeChar.sendDamageMessage(target, damage, false, false, false);
        }
        else {
          activeChar.sendPacket(SystemMessage.sendString(skill.getName() + " failed."));
        }
      }
    }
    catch (Exception e)
    {
      int damage;
      FastList.Node n;
      player.sendMessage("Error using siege assault:" + e);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }

  public static boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, boolean isCheckOnly)
  {
    return checkIfOkToUseStriderSiegeAssault(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
  }

  public static boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, Castle castle, boolean isCheckOnly)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) {
      return false;
    }
    SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((castle == null) || (castle.getCastleId() <= 0))
      sm.addString("You must be on castle ground to use strider siege assault");
    else if (!castle.getSiege().getIsInProgress())
      sm.addString("You can only use strider siege assault during a siege.");
    else if (!player.getTarget().isL2Door())
      sm.addString("You can only use strider siege assault on doors and walls.");
    else if (!activeChar.isRiding())
      sm.addString("You can only use strider siege assault when on strider.");
    else {
      return true;
    }
    if (!isCheckOnly) player.sendPacket(sm);
    return false;
  }
}