package net.sf.l2j.gameserver.util;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;

public final class Broadcast
{
  private static Logger _log = Logger.getLogger(Broadcast.class.getName());

  public static void toPlayersTargettingMyself(L2Character character, L2GameServerPacket mov)
  {
    if (Config.DEBUG) _log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());

    for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
    {
      if ((player == null) || (player.getTarget() != character)) {
        continue;
      }
      player.sendPacket(mov);
    }
  }

  public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
  {
    if (Config.DEBUG) _log.fine("players to notify:" + character.getKnownList().getKnownPlayers().size() + " packet:" + mov.getType());

    for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
    {
      try
      {
        player.sendPacket(mov);
        if (((mov instanceof CharInfo)) && ((character instanceof L2PcInstance)))
        {
          int relation = ((L2PcInstance)character).getRelation(player);
          if ((character.getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId())) != null) && (((Integer)character.getKnownList().getKnownRelations().get(Integer.valueOf(player.getObjectId()))).intValue() != relation))
            player.sendPacket(new RelationChanged((L2PcInstance)character, relation, player.isAutoAttackable(character)));
        }
      }
      catch (NullPointerException e)
      {
      }
    }
  }

  public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
  {
    if (radius < 0) {
      radius = 1500;
    }
    for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
    {
      if (player == null) {
        continue;
      }
      if (character.isInsideRadius(player, radius, false, false))
        player.sendPacket(mov);
    }
  }

  public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
  {
    if ((character instanceof L2PcInstance))
    {
      character.sendPacket(mov);
    }

    toKnownPlayers(character, mov);
  }

  public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
  {
    if (radiusSq < 0L) radiusSq = 360000L;

    if ((character instanceof L2PcInstance)) character.sendPacket(mov);

    for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
    {
      if ((player != null) && (character.getDistanceSq(player) <= radiusSq)) player.sendPacket(mov);
    }
  }

  public static void toAllOnlinePlayers(L2GameServerPacket mov)
  {
    if (Config.DEBUG) { L2World.getInstance(); _log.fine("Players to notify: " + L2World.getAllPlayersCount() + " (with packet " + mov.getType() + ")");
    }
    for (L2PcInstance onlinePlayer : L2World.getInstance().getAllPlayers())
    {
      if (onlinePlayer == null) {
        continue;
      }
      onlinePlayer.sendPacket(mov);
    }
  }
}