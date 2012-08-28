package scripts.zone.type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import scripts.zone.L2ZoneType;

public class L2BossZone extends L2ZoneType
{
  private String _zoneName;
  private int _timeInvade;
  private boolean _enabled = true;
  private Map<Integer, Long> _raiders;
  private int[] _oustLoc = { 0, 0, 0 };

  public L2BossZone(int id)
  {
    super(id);
    _raiders = new ConcurrentHashMap();
    _oustLoc = new int[3];
  }

  public void setParameter(String name, String value)
  {
    if (name.equals("name"))
      _zoneName = value;
    else if (name.equals("InvadeTime"))
      _timeInvade = Integer.parseInt(value);
    else if (name.equals("EnabledByDefault"))
      _enabled = Boolean.parseBoolean(value);
    else if (name.equals("oustX"))
      _oustLoc[0] = Integer.parseInt(value);
    else if (name.equals("oustY"))
      _oustLoc[1] = Integer.parseInt(value);
    else if (name.equals("oustZ"))
      _oustLoc[2] = Integer.parseInt(value);
    else
      super.setParameter(name, value);
  }

  protected void onEnter(L2Character character)
  {
    if (Config.ALLOW_RAID_PVP) {
      character.setInsideZone(1, true);
    }

    if (character.isPlayer()) {
      character.setInsideSilenceZone(true);
      character.setChannel(67);

      if (Config.ALLOW_RAID_PVP) {
        character.setInHotZone(true);
      }

      if (_zoneName.equalsIgnoreCase("Lair of Frintezza")) {
        return;
      }

      if (!Config.NOEPIC_QUESTS) {
        return;
      }

      if (character.isGM()) {
        character.sendAdmResultMessage("You entered " + _zoneName);
        return;
      }

      if (character.isMounted()) {
        character.teleToLocation(MapRegionTable.TeleportWhereType.Town);
        return;
      }

      Long until = (Long)_raiders.get(Integer.valueOf(character.getObjectId()));
      if ((until == null) || (until.longValue() < System.currentTimeMillis()))
      {
        character.teleToLocation(82737, 148571, -3470);
        _raiders.remove(Integer.valueOf(character.getObjectId()));
        return;
      }

      for (L2ItemInstance item : character.getPcInventory().getItems()) {
        if (item == null)
        {
          continue;
        }
        if (item.notForBossZone())
          character.getPcInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
      }
    }
  }

  protected void onExit(L2Character character)
  {
    if (Config.ALLOW_RAID_PVP) {
      character.setInsideZone(1, false);
    }

    if ((_enabled) && 
      (character.isPlayer()))
    {
      character.setInsideSilenceZone(false);
      character.setChannel(1);

      if (Config.ALLOW_RAID_PVP) {
        character.setInHotZone(false);
      }

      if (character.isGM()) {
        character.sendAdmResultMessage("You left " + _zoneName);
        return;
      }

      if (_zoneName.equals("Lair of Frintezza")) {
        sheckSpecialItems(character.getPlayer());
      }

      Long until = (Long)_raiders.get(Integer.valueOf(character.getObjectId()));
      if ((until != null) && (until.longValue() < System.currentTimeMillis()))
        _raiders.remove(Integer.valueOf(character.getObjectId()));
    }
  }

  private void sheckSpecialItems(L2PcInstance player)
  {
    L2ItemInstance coin = player.getInventory().getItemByItemId(8192);
    if ((coin == null) || (coin.getCount() <= 0)) {
      return;
    }

    player.destroyItemByItemId("ClearSpecialItems", 8192, coin.getCount(), player, true);
  }

  public void setZoneEnabled(boolean flag) {
    if (_enabled != flag) {
      oustAllPlayers();
    }

    _enabled = flag;
  }

  public String getZoneName() {
    return _zoneName;
  }

  public int getTimeInvade() {
    return _timeInvade;
  }

  public int getPlayersCount()
  {
    return _raiders.size();
  }

  public boolean isPlayerAllowed(L2PcInstance player) {
    if (player.isGM())
      return true;
    if (_raiders.containsKey(Integer.valueOf(player.getObjectId()))) {
      return true;
    }

    player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    return false;
  }

  public void movePlayersTo(int x, int y, int z) {
    if (_characterList == null) {
      return;
    }
    if (_characterList.isEmpty()) {
      return;
    }
    for (L2Character character : _characterList.values()) {
      if (character == null) {
        continue;
      }
      if ((character.isPlayer()) && 
        (character.isOnline() == 1))
        character.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
  }

  public void oustAllPlayers()
  {
    if (_characterList == null) {
      return;
    }
    if (_characterList.isEmpty()) {
      return;
    }
    for (L2Character character : _characterList.values()) {
      if (character == null) {
        continue;
      }
      if ((character.isPlayer()) && 
        (character.isOnline() == 1)) {
        character.teleToLocation(MapRegionTable.TeleportWhereType.Town);
      }
    }

    _raiders.clear();
  }

  public void allowPlayerEntry(L2PcInstance player, int minutes)
  {
    _raiders.put(Integer.valueOf(player.getObjectId()), Long.valueOf(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes)));
  }

  public void onDieInside(L2Character character)
  {
  }

  public void onReviveInside(L2Character character)
  {
  }
}