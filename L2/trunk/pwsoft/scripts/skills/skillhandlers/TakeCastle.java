package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.skills.ISkillHandler;

public class TakeCastle
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.TAKECASTLE };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId())) return;

    Castle castle = CastleManager.getInstance().getCastle(player);
    if ((castle == null) || (!checkIfOkToCastSealOfRule(player, castle, false))) {
      return;
    }
    try
    {
      if (player.getTarget().isL2Artefact())
        castle.Engrave(player.getClan(), player.getTarget().getObjectId());
    }
    catch (Exception e)
    {
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }

  public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, boolean isCheckOnly)
  {
    return checkIfOkToCastSealOfRule(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
  }

  public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, Castle castle, boolean isCheckOnly)
  {
    if ((activeChar == null) || (!activeChar.isPlayer())) {
      return false;
    }
    SystemMessage sm = SystemMessage.id(SystemMessageId.S1_S2);
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((castle == null) || (castle.getCastleId() <= 0)) {
      sm.addString("\u041D\u0435 \u043F\u043E\u0434\u0445\u043E\u0434\u044F\u0449\u0435\u0435 \u043C\u0435\u0441\u0442\u043E \u0434\u043B\u044F \u0447\u0442\u0435\u043D\u0438\u044F \u043F\u0435\u0447\u0430\u0442\u0438/");
    } else if ((player.getTarget() == null) && (!(player.getTarget() instanceof L2ArtefactInstance))) {
      sm.addString("You can only use this skill on an artifact");
    } else if (!castle.getSiege().getIsInProgress()) {
      sm.addString("You can only use this skill during a siege.");
    } else if (castle.getSiege().getAttackerClan(player.getClan()) == null) {
      sm.addString("You must be an attacker to use this skill");
    }
    else {
      if (!isCheckOnly) castle.getSiege().announceToPlayer("\u041A\u043B\u0430\u043D " + player.getClan().getName() + " \u043D\u0430\u0447\u0430\u043B \u0447\u0442\u0435\u043D\u0438\u0435 \u043F\u0435\u0447\u0430\u0442\u0438.", true);
      return true;
    }

    if (!isCheckOnly) player.sendPacket(sm);
    return false;
  }
}