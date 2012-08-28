package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import net.sf.l2j.gameserver.datatables.CrownTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CrownManager
{
  private static final Log _log = LogFactory.getLog(CrownManager.class.getName());
  private static CrownManager _instance;

  public static final CrownManager getInstance()
  {
    return _instance;
  }

  public static void init()
  {
    _instance = new CrownManager();
    _log.info("CrownManager: Loaded.");
  }

  public void checkCrowns(L2Clan clan)
  {
    if (clan == null) {
      return;
    }
    for (L2ClanMember member : clan.getMembers())
    {
      if ((member == null) || (!member.isOnline()) || (member.getPlayerInstance() == null))
        continue;
      checkCrowns(member.getPlayerInstance());
    }
  }

  public void checkCrowns(L2PcInstance activeChar)
  {
    if (activeChar == null) {
      return;
    }
    boolean isLeader = false;
    int crownId = -1;

    L2Clan activeCharClan = activeChar.getClan();
    L2ClanMember activeCharClanLeader;
    L2ClanMember activeCharClanLeader;
    if (activeCharClan != null)
      activeCharClanLeader = activeChar.getClan().getLeader();
    else {
      activeCharClanLeader = null;
    }
    if (activeCharClan != null)
    {
      Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);

      if (activeCharCastle != null)
      {
        crownId = CrownTable.getCrownId(activeCharCastle.getCastleId());
      }

      if ((activeCharClanLeader != null) && (activeCharClanLeader.getObjectId() == activeChar.getObjectId()))
      {
        isLeader = true;
      }
    }

    if (crownId > 0)
    {
      if ((isLeader) && (activeChar.getInventory().getItemByItemId(6841) == null))
      {
        activeChar.getInventory().addItem("Crown", 6841, 1, activeChar, null);
        activeChar.getInventory().updateDatabase();
      }

      if (activeChar.getInventory().getItemByItemId(crownId) == null)
      {
        activeChar.getInventory().addItem("Crown", crownId, 1, activeChar, null);
        activeChar.getInventory().updateDatabase();
      }
    }

    boolean alreadyFoundCirclet = false;
    boolean alreadyFoundCrown = false;
    for (L2ItemInstance item : activeChar.getInventory().getItems())
    {
      if (!CrownTable.getCrownList().contains(Integer.valueOf(item.getItemId())))
        continue;
      if (crownId > 0)
      {
        if (item.getItemId() == crownId)
        {
          if (!alreadyFoundCirclet)
          {
            alreadyFoundCirclet = true;
            continue;
          }
        }
        else if ((item.getItemId() == 6841) && (isLeader))
        {
          if (!alreadyFoundCrown)
          {
            alreadyFoundCrown = true;
            continue;
          }
        }
      }

      activeChar.destroyItem("Removing Crown", item, activeChar, true);
      activeChar.getInventory().updateDatabase();
    }
  }
}