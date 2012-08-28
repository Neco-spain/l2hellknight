package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2ClanHallBossInstance extends L2MonsterInstance
{
  private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

  public L2ClanHallBossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  protected int getMaintenanceInterval() {
    return 10000;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean isRaid()
  {
    return true;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer))
      return false;
    L2PcInstance player = null;

    if (killer.isPlayer())
      player = (L2PcInstance)killer;
    else if (killer.isL2Summon()) {
      player = killer.getOwner();
    }
    if (!getCastle().getSiege().getIsInProgress()) {
      return false;
    }
    if (player != null)
    {
      int npcId = getTemplate().npcId;
      int ClanHallId = 0;

      switch (npcId)
      {
      case 35410:
        ClanHallId = 34;
        break;
      case 35629:
        ClanHallId = 64;
        break;
      case 35368:
        ClanHallId = 21;
      }

      if (ClanHallId == 21)
      {
        if ((player.getClan() != null) && (player.getClan().getHasHideout() == 0))
        {
          if (!ClanHallManager.getInstance().isFree(ClanHallId)) {
            ClanHallManager.getInstance().setFree(ClanHallId);
          }
          ClanHallManager.getInstance().setOwner(ClanHallId, player.getClan());
        }
        getCastle().getSiege().endSiege();
        return true;
      }

      if ((player.getClan() != null) && (getCastle().getSiege().getAttackerClan(player.getClan()) != null) && (player.getClan().getHasHideout() == 0))
      {
        if (!ClanHallManager.getInstance().isFree(ClanHallId)) {
          ClanHallManager.getInstance().setFree(ClanHallId);
        }
        ClanHallManager.getInstance().setOwner(ClanHallId, player.getClan());
      }
      getCastle().getSiege().endSiege();
    }
    return true;
  }
}