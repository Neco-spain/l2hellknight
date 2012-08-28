package scripts.skills.skillhandlers;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.skills.ISkillHandler;

public class Recall
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.RECALL };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if (activeChar.isPlayer())
    {
      L2PcInstance player = (L2PcInstance)activeChar;

      if (!TvTEvent.onEscapeUse(player.getName()))
      {
        player.sendActionFailed();
        return;
      }

      if (player.isInOlympiadMode())
      {
        player.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        return;
      }
      if ((player.isEventWait()) || (player.getChannel() > 1))
      {
        player.sendPacket(Static.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
        return;
      }
    }

    try
    {
      n = targets.head(); for (FastList.Node end = targets.tail(); (n = n.getNext()) != end; )
      {
        L2Object obj = (L2Object)n.getValue();
        if ((obj == null) || (!obj.isL2Character())) {
          continue;
        }
        L2Character target = (L2Character)obj;

        if (target.isPlayer())
        {
          L2PcInstance targetChar = (L2PcInstance)target;

          if (targetChar.isFestivalParticipant()) {
            targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
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
          if ((targetChar.isEventWait()) || (targetChar.getChannel() > 1))
          {
            targetChar.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u043F\u0440\u0438\u0437\u044B\u0432\u0430\u0442\u044C \u043D\u0430 \u044D\u0432\u0435\u043D\u0442\u0435");
            return;
          }
        }

        target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
      }
    }
    catch (Throwable e)
    {
      FastList.Node n;
      if (Config.DEBUG) e.printStackTrace();
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}