package net.sf.l2j.gameserver.model.actor.instance;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.model.entity.olympiad.OlympiadDiary;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2GrandBossInstance extends L2MonsterInstance
{
  private static final int BOSS_MAINTENANCE_INTERVAL = 10000;
  private boolean _teleportedToNest;
  private boolean _social;
  private long _lastDrop = 0L;

  public L2GrandBossInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  protected int getMaintenanceInterval()
  {
    return 10000;
  }

  public void onSpawn()
  {
    super.onSpawn();
  }

  public void setTeleported(boolean flag)
  {
    _teleportedToNest = flag;
  }

  public boolean getTeleported() {
    return _teleportedToNest;
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);

    int npcId = getTemplate().npcId;

    L2PcInstance player = attacker.getPlayer();
    if (player != null)
      switch (npcId)
      {
      case 29014:
        if (getTeleported()) {
          return;
        }

        if (getCurrentHp() >= getMaxHp() * 0.5D) break;
        clearAggroList();
        getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        teleToLocation(43577, 15985, -4396, false);
        setTeleported(true);
        setCanReturnToSpawnPoint(false);
      }
  }

  public boolean isRaid()
  {
    return true;
  }

  public final boolean isGrandRaid()
  {
    return true;
  }

  public void setIsInSocialAction(boolean flag)
  {
    _social = flag;
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    if (isTyranosurus()) {
      return true;
    }

    L2PcInstance player = killer.getPlayer();
    if (player != null) {
      player.doEpicLoot(this, getTemplate().npcId);

      if (Config.RAID_CUSTOM_DROP) {
        dropRaidCustom(player);
      }

      if ((Config.EPIC_CLANPOINTS_REWARD > 0) && 
        (player.getClan() != null)) {
        player.getClan().addPoints(Config.EPIC_CLANPOINTS_REWARD);
      }

      broadcastPacket(Static.RAID_WAS_SUCCESSFUL);
      if (player.getParty() != null) {
        for (L2PcInstance member : player.getParty().getPartyMembers()) {
          if (member == null)
          {
            continue;
          }
          RaidBossPointsManager.addPoints(member, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));
          if (member.isHero())
            OlympiadDiary.addRecord(member, "\u041F\u043E\u0431\u0435\u0434\u0430 \u0432 \u0431\u0438\u0442\u0432\u0435 \u0441 " + getTemplate().name + ".");
        }
      }
      else {
        RaidBossPointsManager.addPoints(player, getNpcId(), getLevel() / 2 + Rnd.get(-5, 5));

        if (player.isHero()) {
          OlympiadDiary.addRecord(player, "\u041F\u043E\u0431\u0435\u0434\u0430 \u0432 \u0431\u0438\u0442\u0432\u0435 \u0441 " + getTemplate().name + ".");
        }
      }
    }
    return true;
  }

  public long getLastDrop()
  {
    return _lastDrop;
  }

  public void setLastDrop() {
    _lastDrop = System.currentTimeMillis();
  }

  public void doTele() {
    FastList trgs = new FastList();
    trgs.addAll(getKnownList().getKnownPlayersInRadius(1500));
    if (!trgs.isEmpty()) {
      L2PcInstance trg = (L2PcInstance)trgs.get(Rnd.get(trgs.size() - 1));
      if (trg != null) {
        setTarget(trg);
        broadcastPacket(new MagicSkillUser(this, trg, 5015, 1, 1500, 0));
        try {
          Thread.sleep(340L);
        } catch (InterruptedException e) {
        }
        setVis(false);
        try {
          Thread.sleep(600L);
        } catch (InterruptedException e) {
        }
        getPosition().setXYZ(trg.getX(), trg.getY(), trg.getZ());
        try {
          Thread.sleep(20L);
        } catch (InterruptedException e) {
        }
        setVis(true);
        attackFirst();
      }
    }
  }

  public void attackFirst() {
    FastList trgs = new FastList();
    trgs.addAll(getKnownList().getKnownPlayersInRadius(1200));
    if (!trgs.isEmpty()) {
      L2PcInstance trg = (L2PcInstance)trgs.get(Rnd.get(trgs.size() - 1));
      if (trg != null) {
        setTarget(trg);
        addDamageHate(trg, 0, 999);
        getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
      }
    }
  }

  public boolean inLair() {
    return false;
  }

  public boolean checkRange()
  {
    return getTemplate().npcId != 29021;
  }

  public boolean isTyranosurus()
  {
    switch (getTemplate().npcId) {
    case 22215:
    case 22216:
    case 22217:
      return true;
    }
    return false;
  }
}