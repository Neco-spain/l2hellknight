package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.List;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
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

public class SiegeFlag
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SIEGEFLAG };

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if ((activeChar == null) || (!(activeChar instanceof L2PcInstance))) return;

    L2PcInstance player = (L2PcInstance)activeChar;

    if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId())) return;

    Castle castle = CastleManager.getInstance().getCastle(player);

    if ((castle == null) || (!checkIfOkToPlaceFlag(player, castle, true))) return;

    try
    {
      L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcTable.getInstance().getTemplate(35062));
      flag.setTitle(player.getClan().getName());
      flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
      flag.setHeading(player.getHeading());
      flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
      castle.getSiege().getFlag(player.getClan()).add(flag);
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
    if ((activeChar == null) || (!(activeChar instanceof L2PcInstance))) {
      return false;
    }
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((castle == null) || (castle.getCastleId() <= 0))
      sm.addString("You must be on castle ground to place a flag");
    else if (!castle.getSiege().getIsInProgress())
      sm.addString("You can only place a flag during a siege.");
    else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
      sm.addString("You must be an attacker to place a flag");
    else if ((player.getClan() == null) || (!player.isClanLeader()))
      sm.addString("You must be a clan leader to place a flag");
    else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
      sm.addString("You have already placed the maximum number of flags possible");
    else {
      return true;
    }
    if (!isCheckOnly) player.sendPacket(sm);
    return false;
  }
}