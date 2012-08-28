package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2NpcWalkerAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2NpcWalkerInstance extends L2NpcInstance
{
  public L2NpcWalkerInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
    setAI(new L2NpcWalkerAI(new L2NpcWalkerAIAccessor()));
  }

  public void setAI(L2CharacterAI newAI)
  {
    if (_ai == null)
      super.setAI(newAI);
  }

  public void onSpawn()
  {
    ((L2NpcWalkerAI)getAI()).setHomeX(getX());
    ((L2NpcWalkerAI)getAI()).setHomeY(getY());
    ((L2NpcWalkerAI)getAI()).setHomeZ(getZ());
  }

  public void broadcastChat(String chat)
  {
    Map _knownPlayers = getKnownList().getKnownPlayers();

    if (_knownPlayers == null)
    {
      if (Config.DEVELOPER)
        _log.info("broadcastChat _players == null");
      return;
    }
    CreatureSay cs;
    if (_knownPlayers.size() > 0)
    {
      cs = new CreatureSay(getObjectId(), 0, getName(), chat);

      for (L2PcInstance players : _knownPlayers.values())
      {
        players.sendPacket(cs);
      }
    }
  }

  public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
  {
  }

  public boolean doDie(L2Character killer)
  {
    return false;
  }

  public L2CharacterAI getAI()
  {
    return super.getAI();
  }
  protected class L2NpcWalkerAIAccessor extends L2Character.AIAccessor {
    protected L2NpcWalkerAIAccessor() { super();
    }

    public void detachAI()
    {
    }
  }
}