package l2p.gameserver.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import l2p.commons.time.cron.SchedulingPattern;
import l2p.gameserver.data.xml.holder.InstantZoneHolder;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.model.CommandChannel;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.entity.Reflection;
import l2p.gameserver.model.instances.DoorInstance;
import l2p.gameserver.templates.InstantZone;

public class ReflectionUtils
{
  public static DoorInstance getDoor(int id)
  {
    return ReflectionManager.DEFAULT.getDoor(id);
  }

  public static Zone getZone(String name)
  {
    return ReflectionManager.DEFAULT.getZone(name);
  }

  public static List<Zone> getZonesByType(Zone.ZoneType zoneType)
  {
    Collection zones = ReflectionManager.DEFAULT.getZones();
    if (zones.isEmpty()) {
      return Collections.emptyList();
    }
    List zones2 = new ArrayList(5);
    for (Zone z : zones) {
      if (z.getType() == zoneType)
        zones2.add(z);
    }
    return zones2;
  }

  public static Reflection enterReflection(Player invoker, int instancedZoneId)
  {
    InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
    return enterReflection(invoker, new Reflection(), iz);
  }

  public static Reflection enterReflection(Player invoker, Reflection r, int instancedZoneId)
  {
    InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(instancedZoneId);
    return enterReflection(invoker, r, iz);
  }

  public static Reflection enterReflection(Player invoker, Reflection r, InstantZone iz)
  {
    r.init(iz);

    if (r.getReturnLoc() == null) {
      r.setReturnLoc(invoker.getLoc());
    }
    switch (1.$SwitchMap$l2p$gameserver$templates$InstantZoneEntryType[iz.getEntryType().ordinal()])
    {
    case 1:
      if (iz.getRemovedItemId() > 0)
        ItemFunctions.removeItem(invoker, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
      if (iz.getGiveItemId() > 0)
        ItemFunctions.addItem(invoker, iz.getGiveItemId(), iz.getGiveItemCount(), true);
      if (iz.isDispelBuffs())
        invoker.dispelBuffs();
      if ((iz.getSetReuseUponEntry()) && (iz.getResetReuse().next(System.currentTimeMillis()) > System.currentTimeMillis()))
        invoker.setInstanceReuse(iz.getId(), System.currentTimeMillis());
      invoker.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
      invoker.teleToLocation(iz.getTeleportCoord(), r);
      break;
    case 2:
      Party party = invoker.getParty();

      party.setReflection(r);
      r.setParty(party);

      for (Player member : party.getPartyMembers())
      {
        if (iz.getRemovedItemId() > 0)
          ItemFunctions.removeItem(member, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
        if (iz.getGiveItemId() > 0)
          ItemFunctions.addItem(member, iz.getGiveItemId(), iz.getGiveItemCount(), true);
        if (iz.isDispelBuffs())
          member.dispelBuffs();
        if (iz.getSetReuseUponEntry())
          member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
        member.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
        member.teleToLocation(iz.getTeleportCoord(), r);
      }
      break;
    case 3:
      Party commparty = invoker.getParty();
      CommandChannel cc = commparty.getCommandChannel();

      cc.setReflection(r);
      r.setCommandChannel(cc);

      for (Player member : cc)
      {
        if (iz.getRemovedItemId() > 0)
          ItemFunctions.removeItem(member, iz.getRemovedItemId(), iz.getRemovedItemCount(), true);
        if (iz.getGiveItemId() > 0)
          ItemFunctions.addItem(member, iz.getGiveItemId(), iz.getGiveItemCount(), true);
        if (iz.isDispelBuffs())
          member.dispelBuffs();
        if (iz.getSetReuseUponEntry())
          member.setInstanceReuse(iz.getId(), System.currentTimeMillis());
        member.setVar("backCoords", invoker.getLoc().toXYZString(), -1L);
        member.teleToLocation(iz.getTeleportCoord(), r);
      }

    }

    return r;
  }
}