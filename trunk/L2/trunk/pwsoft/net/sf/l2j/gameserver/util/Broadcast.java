package net.sf.l2j.gameserver.util;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;

public final class Broadcast
{
  public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
  {
    sendToPlayersTargettingMyself(character, mov, character.getKnownList().getListKnownPlayers(), null);
  }

  private static void sendToPlayersTargettingMyself(L2Character character, L2GameServerPacket mov, FastList<L2PcInstance> players, L2PcInstance pc) {
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if ((pc == null) || 
        (!character.equals(pc.getTarget()))) continue;
      pc.sendPacket(mov);
    }

    mov.gcb();
  }

  public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
  {
    sendToPlayers(character, mov, character.getKnownList().getListKnownPlayers(), null);
  }

  private static void sendToPlayers(L2Character cha, L2GameServerPacket mov, FastList<L2PcInstance> players, L2PcInstance pc) {
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(mov);
      if ((mov.isCharInfo()) && (cha.isPlayer())) {
        int relation = cha.getRelation(pc);
        if (cha.getKnownList().updateRelationsFor(pc.getObjectId(), relation)) {
          pc.sendPacket(new RelationChanged(cha.getPlayer(), relation, pc.isAutoAttackable(cha)));
        }
      }
    }

    mov.gcb();
  }

  public static void broadcastSoulShotsPacket(L2GameServerPacket mov, FastList<L2PcInstance> players, L2PcInstance pc)
  {
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if ((pc == null) || 
        (!pc.showSoulShotsAnim())) continue;
      pc.sendPacket(mov);
    }

    mov.gcb();
  }

  public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
  {
    if (radius < 0) {
      radius = 1500;
    }
    sendToKnownPlayersInRadius(mov, character.getKnownList().getKnownPlayersInRadius(radius), null);
  }

  public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius, boolean f) {
    if (radius < 0) {
      radius = 1500;
    }
    sendToPlayers(character, mov, character.getKnownList().getKnownPlayersInRadius(radius), null);
  }

  private static void sendToKnownPlayersInRadius(L2GameServerPacket mov, FastList<L2PcInstance> players, L2PcInstance pc) {
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
      {
        continue;
      }
      pc.sendPacket(mov);
    }

    mov.gcb();
  }

  public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
  {
    if (character.isPlayer()) {
      character.sendPacket(mov);
    }

    toKnownPlayers(character, mov);
  }

  public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
  {
    if (radiusSq < 0L) {
      radiusSq = 360000L;
    }

    if (character.isPlayer()) {
      character.sendPacket(mov);
    }

    sendToSelfAndKnownPlayersInRadius(character, mov, character.getKnownList().getListKnownPlayers(), null, radiusSq);
  }

  private static void sendToSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, FastList<L2PcInstance> players, L2PcInstance pc, long radiusSq) {
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; ) {
      pc = (L2PcInstance)n.getValue();
      if ((pc == null) || 
        (character.getDistanceSq(pc) > radiusSq)) continue;
      pc.sendPacket(mov);
    }

    mov.gcb();
  }

  public static void toAllOnlinePlayers(L2GameServerPacket mov)
  {
    for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
      if ((player == null) || 
        (player.isInOfflineMode()) || (player.isFantome()))
      {
        continue;
      }
      player.sendPacket(mov);
    }
  }
}