package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.MinionList;
import net.sf.l2j.util.Rnd;

public final class L2RaidBossInstance extends L2MonsterInstance
{
  private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 30000;
  private int countTimer = 0;
  private RaidBossSpawnManager.StatusEnum _raidStatus;

  public L2RaidBossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    setIsRaid(true);
    super.onSpawn();
  }

  protected int getMaintenanceInterval()
  {
    return 30000;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    L2PcInstance player = null;

    if ((killer instanceof L2PcInstance))
      player = (L2PcInstance)killer;
    else if ((killer instanceof L2Summon)) {
      player = ((L2Summon)killer).getOwner();
    }
    if (player != null)
    {
      SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
      broadcastPacket(msg);
      msg = null;

      if (player.getParty() != null)
      {
        for (L2PcInstance member : player.getParty().getPartyMembers())
        {
          if (Config.NOBLESSE_BY_KILLING_RB)
          {
            if (getNpcId() == 25325)
            {
              if (!member.isNoble())
              {
                member.setNoble(true);
                member.addItem("Tiara", 7694, 1, null, true);
                member.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0434\u0432\u043E\u0440\u044F\u043D\u0438\u043D\u0430 \u0437\u0430 \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u043E Barakiel'\u044F");
              }
            }
          }
          RaidBossPointsManager.addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
        }
      }
      else
      {
        if (Config.NOBLESSE_BY_KILLING_RB)
        {
          if (getNpcId() == 25325)
          {
            if (!player.isNoble())
            {
              player.setNoble(true);
              player.addItem("Tiara", 7694, 1, null, true);
              player.sendMessage("\u0412\u044B \u043F\u043E\u043B\u0443\u0447\u0438\u043B\u0438 \u0441\u0442\u0430\u0442\u0443\u0441 \u0434\u0432\u043E\u0440\u044F\u043D\u0438\u043D\u0430 \u0437\u0430 \u0443\u0431\u0438\u0439\u0441\u0442\u0432\u043E Barakiel'\u044F");
            }
          }
        }
        RaidBossPointsManager.addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
      }
    }

    RaidBossSpawnManager.getInstance().updateStatus(this, true);
    return true;
  }

  protected void manageMinions()
  {
    L2RaidBossInstance tempRaid = this;
    _minionList.spawnMinions();
    _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable(tempRaid)
    {
      public void run()
      {
        L2Spawn bossSpawn = getSpawn();
        if (!isInsideRadius(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), 3000, true, false))
        {
          MagicSkillUser msu = new MagicSkillUser(val$tempRaid, val$tempRaid, 2036, 1, 1000, 0);
          broadcastPacket(msu);
          teleToLocation(bossSpawn.getLocx(), bossSpawn.getLocy(), bossSpawn.getLocz(), true);
          healFull();
        }
        if (countTimer > 25)
        {
          _minionList.maintainMinions();
          L2RaidBossInstance.access$002(L2RaidBossInstance.this, 0);
        }
        else {
          L2RaidBossInstance.access$012(L2RaidBossInstance.this, 5);
        }
      }
    }
    , 60000L, 5000L);
  }

  public void setRaidStatus(RaidBossSpawnManager.StatusEnum status)
  {
    _raidStatus = status;
  }

  public RaidBossSpawnManager.StatusEnum getRaidStatus()
  {
    return _raidStatus;
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public void healFull()
  {
    super.setCurrentHp(super.getMaxHp());
    super.setCurrentMp(super.getMaxMp());
  }
}