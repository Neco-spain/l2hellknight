package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Recall
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.RECALL };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if ((activeChar instanceof L2PcInstance))
    {
      if (!TvTEvent.onEscapeUse(((L2PcInstance)activeChar).getObjectId()))
      {
        ((L2PcInstance)activeChar).sendPacket(new ActionFailed());
        return;
      }

      if (((L2PcInstance)activeChar).isInOlympiadMode())
      {
        ((L2PcInstance)activeChar).sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
        return;
      }
    }

    try
    {
      for (int index = 0; index < targets.length; index++)
      {
        if (!(targets[index] instanceof L2Character)) {
          continue;
        }
        L2Character target = (L2Character)targets[index];

        if ((target instanceof L2PcInstance))
        {
          L2PcInstance targetChar = (L2PcInstance)target;

          if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", targetChar))
          {
            targetChar.sendPacket(SystemMessage.sendString("\u041D\u0435\u0432\u043E\u0437\u043C\u043E\u0436\u043D\u043E \u0438\u0441\u043F\u043E\u043B\u044C\u0437\u043E\u0432\u0430\u0442\u044C \u0432 \u044D\u0442\u043E\u0439 \u043C\u0435\u0441\u0442\u043D\u043E\u0441\u0442\u0438"));
            targetChar.sendPacket(new ActionFailed());
            break;
          }

          if (targetChar.isFestivalParticipant()) {
            targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
            continue;
          }

          if (targetChar._inEventCTF)
          {
            targetChar.sendMessage("You may not use an escape skill in a Event.");
            continue;
          }

          if (targetChar.isInJail())
          {
            targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
            continue;
          }

          if (targetChar.isInDuel())
          {
            targetChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
            continue;
          }
        }

        target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
      }
    }
    catch (Throwable e) {
    }
  }

  public L2Skill.SkillType[] getSkillIds() {
    return SKILL_IDS;
  }
}