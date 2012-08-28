package l2p.gameserver.model.instances;

import l2p.commons.lang.reference.HardReference;
import l2p.commons.lang.reference.HardReferences;
import l2p.gameserver.data.xml.holder.AirshipDockHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.boat.ClanAirShip;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.ChatType;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.templates.AirshipDock;
import l2p.gameserver.templates.AirshipDock.AirshipPlatform;
import l2p.gameserver.templates.StatsSet;
import l2p.gameserver.templates.npc.NpcTemplate;

public class ClanAirShipControllerInstance extends AirShipControllerInstance
{
  public static final long serialVersionUID = 1L;
  protected static final int ENERGY_STAR_STONE = 13277;
  protected static final int AIRSHIP_SUMMON_LICENSE = 13559;
  private HardReference<ClanAirShip> _dockedShipRef = HardReferences.emptyRef();
  private final AirshipDock _dock;
  private final AirshipDock.AirshipPlatform _platform;

  public ClanAirShipControllerInstance(int objectID, NpcTemplate template)
  {
    super(objectID, template);
    int dockId = template.getAIParams().getInteger("dockId", 0);
    int platformId = template.getAIParams().getInteger("platformId", 0);
    _dock = AirshipDockHolder.getInstance().getDock(dockId);
    _platform = _dock.getPlatform(platformId);
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (command.equalsIgnoreCase("summon"))
    {
      if ((player.getClan() == null) || (player.getClan().getLevel() < 5))
      {
        player.sendPacket(SystemMsg.IN_ORDER_TO_ACQUIRE_AN_AIRSHIP_THE_CLANS_LEVEL_MUST_BE_LEVEL_5_OR_HIGHER);
        return;
      }

      if ((player.getClanPrivileges() & 0x400) != 1024)
      {
        player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
        return;
      }

      if (!player.getClan().isHaveAirshipLicense())
      {
        player.sendPacket(SystemMsg.AN_AIRSHIP_CANNOT_BE_SUMMONED_BECAUSE_EITHER_YOU_HAVE_NOT_REGISTERED_YOUR_AIRSHIP_LICENSE_OR_THE_AIRSHIP_HAS_NOT_YET_BEEN_SUMMONED);
        return;
      }

      ClanAirShip dockedAirShip = getDockedAirShip();
      ClanAirShip clanAirship = player.getClan().getAirship();

      if (clanAirship != null)
      {
        if (clanAirship == dockedAirShip)
          player.sendPacket(SystemMsg.THE_CLAN_OWNED_AIRSHIP_ALREADY_EXISTS);
        else
          player.sendPacket(SystemMsg.YOUR_CLANS_AIRSHIP_IS_ALREADY_BEING_USED_BY_ANOTHER_CLAN_MEMBER);
        return;
      }

      if (dockedAirShip != null)
      {
        Functions.npcSay(this, NpcString.IN_AIR_HARBOR_ALREADY_AIRSHIP_DOCKED_PLEASE_WAIT_AND_TRY_AGAIN, ChatType.SHOUT, 5000, new String[0]);
        return;
      }

      if (Functions.removeItem(player, 13277, 5L) != 5L)
      {
        player.sendPacket(new SystemMessage2(SystemMsg.AN_AIRSHIP_CANNOT_BE_SUMMONED_BECAUSE_YOU_DONT_HAVE_ENOUGH_S1).addItemName(13277));
        return;
      }

      ClanAirShip dockedShip = new ClanAirShip(player.getClan());
      dockedShip.setDock(_dock);
      dockedShip.setPlatform(_platform);

      dockedShip.setHeading(0);
      dockedShip.spawnMe(_platform.getSpawnLoc());
      dockedShip.startDepartTask();

      Functions.npcSay(this, NpcString.AIRSHIP_IS_SUMMONED_IS_DEPART_IN_5_MINUTES, ChatType.SHOUT, 5000, new String[0]);
    }
    else if (command.equalsIgnoreCase("register"))
    {
      if ((player.getClan() == null) || (!player.isClanLeader()) || (player.getClan().getLevel() < 5))
      {
        player.sendPacket(SystemMsg.IN_ORDER_TO_ACQUIRE_AN_AIRSHIP_THE_CLANS_LEVEL_MUST_BE_LEVEL_5_OR_HIGHER);
        return;
      }

      if (player.getClan().isHaveAirshipLicense())
      {
        player.sendPacket(SystemMsg.THE_AIRSHIP_SUMMON_LICENSE_HAS_ALREADY_BEEN_ACQUIRED);
        return;
      }

      if (Functions.getItemCount(player, 13559) == 0L)
      {
        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
        return;
      }

      Functions.removeItem(player, 13559, 1L);
      player.getClan().setAirshipLicense(true);
      player.getClan().setAirshipFuel(600);
      player.getClan().updateClanInDB();
      player.sendPacket(SystemMsg.THE_AIRSHIP_SUMMON_LICENSE_HAS_BEEN_ENTERED);
    }
    else {
      super.onBypassFeedback(player, command);
    }
  }

  protected ClanAirShip getDockedAirShip()
  {
    ClanAirShip ship = (ClanAirShip)_dockedShipRef.get();
    if ((ship != null) && (ship.isDocked())) {
      return ship;
    }
    return null;
  }

  public void setDockedShip(ClanAirShip dockedShip)
  {
    ClanAirShip old = (ClanAirShip)_dockedShipRef.get();
    if (old != null)
    {
      old.setDock(null);
      old.setPlatform(null);
    }

    if (dockedShip != null)
    {
      boolean alreadyEnter = dockedShip.getDock() != null;
      dockedShip.setDock(_dock);
      dockedShip.setPlatform(_platform);
      if (!alreadyEnter) {
        dockedShip.startArrivalTask();
      }
    }
    if (dockedShip == null)
      _dockedShipRef = HardReferences.emptyRef();
    else
      _dockedShipRef = dockedShip.getRef();
  }
}