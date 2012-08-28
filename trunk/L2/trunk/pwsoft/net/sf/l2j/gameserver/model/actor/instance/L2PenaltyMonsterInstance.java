package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2PenaltyMonsterInstance extends L2MonsterInstance
{
  private L2PcInstance _ptk;

  public L2PenaltyMonsterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public L2Character getMostHated()
  {
    return _ptk;
  }

  @Deprecated
  public void notifyPlayerDead() {
    deleteMe();

    L2Spawn spawn = getSpawn();
    if (spawn != null)
    {
      spawn.stopRespawn();
      SpawnTable.getInstance().deleteSpawn(spawn, false);
    }
  }

  public void setPlayerToKill(L2PcInstance ptk)
  {
    if (Rnd.nextInt(100) <= 80)
    {
      broadcastPacket(new CreatureSay(getObjectId(), 0, getName(), "mmm your bait was delicious"));
    }
    _ptk = ptk;
    addDamageHate(ptk, 10, 10);
    getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, ptk);
    addAttackerToAttackByList(ptk);
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }
    if (Rnd.nextInt(100) <= 75)
    {
      broadcastPacket(new CreatureSay(getObjectId(), 0, getName(), "I will tell fishes not to take your bait"));
    }
    return true;
  }

  public boolean isL2Penalty()
  {
    return true;
  }
}