package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;
import scripts.skills.ISkillHandler;

public class Unlock
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.UNLOCK };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    FastList.Node n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
    {
      L2Object target = (L2Object)n.getValue();

      boolean success = Formulas.calculateUnlockChance(skill);
      if (target.isL2Door())
      {
        L2DoorInstance door = (L2DoorInstance)target;

        if ((success) && (!door.getOpen()))
        {
          door.openMe();
          door.onOpen();
          SystemMessage systemmessage = SystemMessage.id(SystemMessageId.S1_S2);

          systemmessage.addString("Unlock the door!");
          activeChar.sendPacket(systemmessage);
        }
        else {
          activeChar.sendPacket(Static.FAILED_TO_UNLOCK_DOOR);
        }
      } else if ((target instanceof L2ChestInstance))
      {
        L2ChestInstance chest = (L2ChestInstance)target;
        if ((chest.getCurrentHp() <= 0.0D) || (chest.isInteracted()))
        {
          activeChar.sendPacket(new ActionFailed());
          return;
        }

        int chestChance = 0;
        int chestGroup = 0;
        int chestTrapLimit = 0;

        if (chest.getLevel() > 60) chestGroup = 4;
        else if (chest.getLevel() > 40) chestGroup = 3;
        else if (chest.getLevel() > 30) chestGroup = 2; else {
          chestGroup = 1;
        }
        switch (chestGroup)
        {
        case 1:
          if (skill.getLevel() > 10) chestChance = 100;
          else if (skill.getLevel() >= 3) chestChance = 50;
          else if (skill.getLevel() == 2) chestChance = 45;
          else if (skill.getLevel() == 1) chestChance = 40;

          chestTrapLimit = 10;

          break;
        case 2:
          if (skill.getLevel() > 12) chestChance = 100;
          else if (skill.getLevel() >= 7) chestChance = 50;
          else if (skill.getLevel() == 6) chestChance = 45;
          else if (skill.getLevel() == 5) chestChance = 40;
          else if (skill.getLevel() == 4) chestChance = 35;
          else if (skill.getLevel() == 3) chestChance = 30;

          chestTrapLimit = 30;

          break;
        case 3:
          if (skill.getLevel() >= 14) chestChance = 50;
          else if (skill.getLevel() == 13) chestChance = 45;
          else if (skill.getLevel() == 12) chestChance = 40;
          else if (skill.getLevel() == 11) chestChance = 35;
          else if (skill.getLevel() == 10) chestChance = 30;
          else if (skill.getLevel() == 9) chestChance = 25;
          else if (skill.getLevel() == 8) chestChance = 20;
          else if (skill.getLevel() == 7) chestChance = 15;
          else if (skill.getLevel() == 6) chestChance = 10;

          chestTrapLimit = 50;

          break;
        case 4:
          if (skill.getLevel() >= 14) chestChance = 50;
          else if (skill.getLevel() == 13) chestChance = 45;
          else if (skill.getLevel() == 12) chestChance = 40;
          else if (skill.getLevel() == 11) chestChance = 35;

          chestTrapLimit = 80;
        }

        if (Rnd.get(100) <= chestChance)
        {
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
          chest.setSpecialDrop();
          chest.setMustRewardExpSp(false);
          chest.setInteracted();
          chest.reduceCurrentHp(99999999.0D, activeChar);
        }
        else
        {
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          if (Rnd.get(100) < chestTrapLimit) chest.chestTrap(activeChar);
          chest.setInteracted();
          chest.addDamageHate(activeChar, 0, 1);
          chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
        }
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}