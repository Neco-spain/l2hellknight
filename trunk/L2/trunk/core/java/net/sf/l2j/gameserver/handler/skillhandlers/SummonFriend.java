package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public class SummonFriend
  implements ISkillHandler
{
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SUMMON_FRIEND };

  public static boolean checkSummonerStatus(L2PcInstance summonerChar)
  {
    if (summonerChar == null) {
      return false;
    }
    if (summonerChar.isInOlympiadMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return false;
    }

    if ((summonerChar._inEventCTF))
    {
      summonerChar.sendMessage("You cannot use this skill in a Event.");
      return false;
    }

    if (TvTEvent.isPlayerParticipant(summonerChar.getObjectId()))
    {
      summonerChar.sendMessage("You cannot use this skill in a Event.");
      return false;
    }

    if (summonerChar.inObserverMode())
    {
      return false;
    }
    if (summonerChar.isInsideZone(4096))
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }
    return true;
  }

  public static boolean checkTargetStatus(L2PcInstance targetChar, L2PcInstance summonerChar)
  {
    if (targetChar == null) {
      return false;
    }
    if (targetChar.isAlikeDead())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }

    if (TvTEvent.isPlayerParticipant(targetChar.getObjectId()))
    {
      targetChar.sendMessage("You cannot use this skill in a Event.");
      return false;
    }

    if ((targetChar._inEventCTF))
    {
      targetChar.sendMessage("You cannot use this skill in a Event.");
      return false;
    }
    if (targetChar.isInStoreMode())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }
    if (targetChar.isRooted())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }
    if (targetChar.isInCombat())
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }
    if (targetChar.isInOlympiadMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
      return false;
    }
    if (targetChar.isFestivalParticipant())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }
    if (targetChar.inObserverMode())
    {
      summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
      return false;
    }
    if (targetChar.isInsideZone(4096))
    {
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA);
      sm.addString(targetChar.getName());
      summonerChar.sendPacket(sm);
      return false;
    }

    if (summonerChar.isIn7sDungeon())
    {
      int targetCabal = SevenSigns.getInstance().getPlayerCabal(targetChar);
      if (SevenSigns.getInstance().isSealValidationPeriod())
      {
        if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
        {
          summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
          return false;
        }

      }
      else if (targetCabal == 0)
      {
        summonerChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
        return false;
      }
    }

    return true;
  }

  public static void teleToTarget(L2PcInstance targetChar, L2PcInstance summonerChar, L2Skill summonSkill)
  {
    if ((targetChar == null) || (summonerChar == null) || (summonSkill == null)) {
      return;
    }
    if (!checkSummonerStatus(summonerChar)) {
      return;
    }
    if (!checkTargetStatus(targetChar, summonerChar)) {
      return;
    }
    int itemConsumeId = summonSkill.getTargetConsumeId();
    int itemConsumeCount = summonSkill.getTargetConsume();
    if ((itemConsumeId != 0) && (itemConsumeCount != 0))
    {
      String ItemName = ItemTable.getInstance().getTemplate(itemConsumeId).getName();
      if (targetChar.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
        sm.addString(ItemName);
        targetChar.sendPacket(sm);
        return;
      }
      targetChar.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summonerChar, targetChar);
      SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
      sm.addString(ItemName);
      targetChar.sendPacket(sm);
    }
    targetChar.teleToLocation(summonerChar.getX(), summonerChar.getY(), summonerChar.getZ(), true);
  }

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance activePlayer = (L2PcInstance)activeChar;
    if (!checkSummonerStatus(activePlayer)) {
      return;
    }
    try
    {
      for (L2Character target : (L2Character[])(L2Character[])targets)
      {
        if (activeChar == target) {
          continue;
        }
        if (!(target instanceof L2PcInstance))
          continue;
        L2PcInstance targetPlayer = (L2PcInstance)target;
        if (!checkTargetStatus(targetPlayer, activePlayer)) {
          continue;
        }
        if (Util.checkIfInRange(0, activeChar, target, false))
          continue;
        if (!targetPlayer.teleportRequest(activePlayer, skill))
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_SUMMONED);
          sm.addString(target.getName());
          activePlayer.sendPacket(sm);
        }
        else if (skill.getId() == 1403)
        {
          ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());

          confirm.addString(activeChar.getName());

          confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
          confirm.addTime(30000);
          confirm.addRequesterId(activePlayer.getCharId());
          target.sendPacket(confirm);
        }
        else
        {
          teleToTarget(targetPlayer, activePlayer, skill);
          targetPlayer.teleportRequest(null, null);
        }

      }

    }
    catch (Exception e)
    {
      if (Config.DEBUG)
      {
        e.printStackTrace();
      }
    }
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}