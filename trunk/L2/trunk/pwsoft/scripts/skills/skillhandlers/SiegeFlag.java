package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeFlagInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.skills.ISkillHandler;

public class SiegeFlag
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SIEGEFLAG };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId())) return;

    Castle castle = CastleManager.getInstance().getCastle(player);

    if ((castle == null) || (!checkIfOkToPlaceFlag(player, castle, true))) {
      return;
    }
    try
    {
      L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
      flag.setTitle(player.getClan().getName());
      if (skill.getId() == 326)
        flag.setCurrentHpMp(flag.getMaxHp() * 2, flag.getMaxMp());
      else
        flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
      flag.setHeading(player.getHeading());
      flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
      castle.getSiege().addFlag(player.getClan(), flag);
    }
    catch (Exception e)
    {
      player.sendMessage("Error placing flag:" + e);
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }

  public static boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
  {
    return checkIfOkToPlaceFlag(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
  }

  public static boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) {
      return false;
    }
    SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((castle == null) || (castle.getCastleId() <= 0))
      sm.addString("\u041D\u0435 \u043F\u043E\u0434\u0445\u043E\u0434\u044F\u0449\u0435\u0435 \u043C\u0435\u0441\u0442\u043E \u0434\u043B\u044F \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0438 \u0444\u043B\u0430\u0433\u0430");
    else if (!castle.getSiege().getIsInProgress())
      sm.addString("\u0423\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0430 \u0444\u043B\u0430\u0433\u0430 \u0432\u043E\u0437\u043C\u043E\u0436\u043D\u0430 \u0442\u043E\u043B\u044C\u043A\u043E \u0432\u043E \u0432\u0440\u0435\u043C\u044F \u043E\u0441\u0430\u0434\u044B");
    else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
      sm.addString("\u0422\u043E\u043B\u044C\u043A\u043E \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043E\u0432\u0430\u043D\u043D\u044B\u0435 \u043D\u0430 \u0430\u0442\u0430\u043A\u0443 \u043C\u043E\u0433\u0443\u0442 \u0441\u0442\u0430\u0432\u0438\u0442\u044C \u0444\u043B\u0430\u0433");
    else if ((player.getClan() == null) || (!player.isClanLeader()))
      sm.addString("\u0422\u043E\u043B\u044C\u043A\u043E \u043A\u043B\u0430\u043D\u043B\u0438\u0434\u0435\u0440\u044B \u043C\u043E\u0433\u0443\u0442 \u0441\u0442\u0430\u0432\u0438\u0442\u044C \u0444\u043B\u0430\u0433");
    else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
      sm.addString("\u0412\u044B \u043C\u043E\u0436\u0435\u0442\u0435 \u043F\u043E\u0441\u0442\u0430\u0432\u0438\u0442\u044C \u0442\u043E\u043B\u044C\u043A\u043E 1 \u0444\u043B\u0430\u0433");
    else if (!player.isInSiegeFlagArea())
      sm.addString("\u041D\u0435 \u043F\u043E\u0434\u0445\u043E\u0434\u044F\u0449\u0435\u0435 \u043C\u0435\u0441\u0442\u043E \u0434\u043B\u044F \u0443\u0441\u0442\u0430\u043D\u043E\u0432\u043A\u0438 \u0444\u043B\u0430\u0433\u0430");
    else {
      return true;
    }
    if (!isCheckOnly) player.sendPacket(sm);
    return false;
  }
}