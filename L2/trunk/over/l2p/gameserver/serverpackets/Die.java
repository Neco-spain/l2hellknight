package l2p.gameserver.serverpackets;

import java.util.HashMap;
import java.util.Map;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.GlobalEvent;
import l2p.gameserver.model.instances.MonsterInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.pledge.Clan;

public class Die extends L2GameServerPacket
{
  private int _objectId;
  private boolean _fake;
  private boolean _sweepable;
  private Map<RestartType, Boolean> _types = new HashMap(RestartType.VALUES.length);

  public Die(Creature cha)
  {
    _objectId = cha.getObjectId();
    _fake = (!cha.isDead());
    Player player;
    if (cha.isMonster()) {
      _sweepable = ((MonsterInstance)cha).isSweepActive();
    } else if (cha.isPlayer())
    {
      player = (Player)cha;
      put(RestartType.FIXED, (player.getPlayerAccess().ResurectFixed) || (((player.getInventory().getCountOf(10649) > 0L) || (player.getInventory().getCountOf(13300) > 0L)) && (!player.isOnSiegeField())));
      put(RestartType.AGATHION, player.isAgathionResAvailable());
      put(RestartType.TO_VILLAGE, true);

      Clan clan = null;
      if (get(RestartType.TO_VILLAGE))
        clan = player.getClan();
      if (clan != null)
      {
        put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
        put(RestartType.TO_CASTLE, clan.getCastle() > 0);
        put(RestartType.TO_FORTRESS, clan.getHasFortress() > 0);
      }

      for (GlobalEvent e : cha.getEvents())
        e.checkRestartLocs(player, _types);
    }
  }

  protected final void writeImpl()
  {
    if (_fake) {
      return;
    }
    writeC(0);
    writeD(_objectId);
    writeD(get(RestartType.TO_VILLAGE));
    writeD(get(RestartType.TO_CLANHALL));
    writeD(get(RestartType.TO_CASTLE));
    writeD(get(RestartType.TO_FLAG));
    writeD(_sweepable ? 1 : 0);
    writeD(get(RestartType.FIXED));
    writeD(get(RestartType.TO_FORTRESS));
    writeC(0);
    writeD(get(RestartType.AGATHION));
    writeD(0);
  }

  private void put(RestartType t, boolean b)
  {
    _types.put(t, Boolean.valueOf(b));
  }

  private boolean get(RestartType t)
  {
    Boolean b = (Boolean)_types.get(t);
    return (b != null) && (b.booleanValue());
  }
}