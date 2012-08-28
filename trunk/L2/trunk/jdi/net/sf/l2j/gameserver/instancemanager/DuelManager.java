package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Duel;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public class DuelManager
{
  private static final Logger _log = Logger.getLogger(DuelManager.class.getName());
  private static DuelManager _instance;
  private FastList<Duel> _duels;
  private int _currentDuelId = 144;

  public static final DuelManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new DuelManager();
    }
    return _instance;
  }

  private DuelManager()
  {
    _log.info("Initializing DuelManager");
    _duels = new FastList();
  }

  private int getNextDuelId()
  {
    _currentDuelId += 1;

    if (_currentDuelId >= 2147483640) _currentDuelId = 1;
    return _currentDuelId;
  }

  public Duel getDuel(int duelId)
  {
    FastList.Node e = _duels.head(); for (FastList.Node end = _duels.tail(); (e = e.getNext()) != end; )
    {
      if (((Duel)e.getValue()).getId() == duelId) return (Duel)e.getValue();
    }
    return null;
  }

  public void addDuel(L2PcInstance playerA, L2PcInstance playerB, int partyDuel)
  {
    if ((playerA == null) || (playerB == null)) return;

    String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
    if (partyDuel == 1)
    {
      boolean playerInPvP = false;
      for (L2PcInstance temp : playerA.getParty().getPartyMembers())
      {
        if (temp.getPvpFlag() != 0) { playerInPvP = true; break; }
      }
      if (!playerInPvP)
      {
        for (L2PcInstance temp : playerB.getParty().getPartyMembers())
        {
          if (temp.getPvpFlag() != 0) { playerInPvP = true; break;
          }
        }
      }
      if (playerInPvP)
      {
        for (L2PcInstance temp : playerA.getParty().getPartyMembers())
        {
          temp.sendMessage(engagedInPvP);
        }
        for (L2PcInstance temp : playerB.getParty().getPartyMembers())
        {
          temp.sendMessage(engagedInPvP);
        }
        return;
      }

    }
    else if ((playerA.getPvpFlag() != 0) || (playerB.getPvpFlag() != 0))
    {
      playerA.sendMessage(engagedInPvP);
      playerB.sendMessage(engagedInPvP);
      return;
    }

    Duel duel = new Duel(playerA, playerB, partyDuel, getNextDuelId());
    _duels.add(duel);
  }

  public void removeDuel(Duel duel)
  {
    _duels.remove(duel);
  }

  public void doSurrender(L2PcInstance player)
  {
    if ((player == null) || (!player.isInDuel())) return;
    Duel duel = getDuel(player.getDuelId());
    duel.doSurrender(player);
  }

  public void onPlayerDefeat(L2PcInstance player)
  {
    if ((player == null) || (!player.isInDuel())) return;
    Duel duel = getDuel(player.getDuelId());
    if (duel != null) duel.onPlayerDefeat(player);
  }

  public void onBuff(L2PcInstance player, L2Effect buff)
  {
    if ((player == null) || (!player.isInDuel()) || (buff == null)) return;
    Duel duel = getDuel(player.getDuelId());
    if (duel != null) duel.onBuff(player, buff);
  }

  public void onRemoveFromParty(L2PcInstance player)
  {
    if ((player == null) || (!player.isInDuel())) return;
    Duel duel = getDuel(player.getDuelId());
    if (duel != null) duel.onRemoveFromParty(player);
  }

  public void broadcastToOppositTeam(L2PcInstance player, L2GameServerPacket packet)
  {
    if ((player == null) || (!player.isInDuel())) return;
    Duel duel = getDuel(player.getDuelId());
    if (duel == null) return;
    if ((duel.getPlayerA() == null) || (duel.getPlayerB() == null)) return;

    if (duel.getPlayerA() == player)
    {
      duel.broadcastToTeam2(packet);
    }
    else if (duel.getPlayerB() == player)
    {
      duel.broadcastToTeam1(packet);
    }
    else if (duel.isPartyDuel())
    {
      if ((duel.getPlayerA().getParty() != null) && (duel.getPlayerA().getParty().getPartyMembers().contains(player)))
      {
        duel.broadcastToTeam2(packet);
      }
      else if ((duel.getPlayerB().getParty() != null) && (duel.getPlayerB().getParty().getPartyMembers().contains(player)))
      {
        duel.broadcastToTeam1(packet);
      }
    }
  }
}