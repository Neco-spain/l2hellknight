package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Map;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.DoorInfo;
import net.sf.l2j.gameserver.network.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.DropItem;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItemPoly;
import net.sf.l2j.gameserver.network.serverpackets.StaticObject;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.util.Point3D;

public class PcKnownList extends PlayableKnownList
{
  L2PcInstance activeChar;

  public PcKnownList(L2PcInstance activeChar)
  {
    super(activeChar);
    this.activeChar = activeChar;
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper)
  {
    if (!super.addKnownObject(object, dropper)) {
      return false;
    }

    if ((object.getPoly().isMorphed()) && (object.getPoly().getPolyType().equals("item")))
    {
      activeChar.sendPacket(new SpawnItemPoly(object));
    }
    else
    {
      if (object.isL2Item()) {
        if (dropper != null)
          activeChar.sendPacket(new DropItem((L2ItemInstance)object, dropper.getObjectId()));
        else
          activeChar.sendPacket(new SpawnItem((L2ItemInstance)object));
      }
      else if (object.isL2Door()) {
        L2DoorInstance door = (L2DoorInstance)object;

        activeChar.sendPacket(new DoorInfo(door));
        activeChar.sendPacket(new DoorStatusUpdate(door, true));
      }
      else if ((object instanceof L2BoatInstance)) {
        if ((!activeChar.isInBoat()) && 
          (object != activeChar.getBoat())) {
          activeChar.sendPacket(new VehicleInfo((L2BoatInstance)object));
          ((L2BoatInstance)object).sendVehicleDeparture(getActiveChar());
        }
      }
      else if ((object instanceof L2StaticObjectInstance)) {
        activeChar.sendPacket(new StaticObject((L2StaticObjectInstance)object));
      } else if (object.isL2Npc()) {
        if (Config.CHECK_KNOWN) {
          activeChar.sendMessage("Added NPC: " + ((L2NpcInstance)object).getName());
        }

        if (((L2NpcInstance)object).getRunSpeed() == 0)
          activeChar.sendPacket(new ServerObjectInfo((L2NpcInstance)object, getActiveChar()));
        else
          activeChar.sendPacket(new NpcInfo((L2NpcInstance)object, getActiveChar()));
      }
      else if (object.isL2Summon()) {
        L2Summon summon = (L2Summon)object;

        if ((activeChar.equals(summon.getOwner())) && (summon.getNpcId() != Config.SOB_NPC)) {
          activeChar.sendPacket(new PetInfo(summon, 0));

          summon.updateEffectIcons(true);
          if (summon.isPet())
            activeChar.sendPacket(new PetItemList((L2PetInstance)summon));
        }
        else {
          activeChar.sendPacket(new NpcInfo(summon, getActiveChar()));
        }
      } else if (object.isPlayer()) {
        L2PcInstance otherPlayer = object.getPlayer();
        if (otherPlayer.isInBoat()) {
          otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
          activeChar.sendPacket(new CharInfo(otherPlayer));
          int relation = otherPlayer.getRelation(getActiveChar());
          if (otherPlayer.getKnownList().updateRelationsFor(activeChar.getObjectId(), relation)) {
            activeChar.sendPacket(new RelationChanged(otherPlayer, relation, activeChar.isAutoAttackable(otherPlayer)));
          }
          activeChar.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
        }
        else
        {
          activeChar.sendPacket(new CharInfo(otherPlayer));
          int relation = otherPlayer.getRelation(getActiveChar());
          if (otherPlayer.getKnownList().updateRelationsFor(activeChar.getObjectId(), relation)) {
            activeChar.sendPacket(new RelationChanged(otherPlayer, relation, activeChar.isAutoAttackable(otherPlayer)));
          }
        }

        if (otherPlayer.getPrivateStoreType() == 1)
          activeChar.sendPacket(new PrivateStoreMsgSell(otherPlayer));
        else if (otherPlayer.getPrivateStoreType() == 3)
          activeChar.sendPacket(new PrivateStoreMsgBuy(otherPlayer));
        else if (otherPlayer.getPrivateStoreType() == 5) {
          activeChar.sendPacket(new RecipeShopMsg(otherPlayer));
        }
      }

      if (object.isL2Character())
      {
        L2Character obj = (L2Character)object;
        obj.getAI().describeStateToPlayer(getActiveChar());
      }
    }

    return true;
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) {
      return false;
    }

    activeChar.sendPacket(new DeleteObject(object));
    if ((Config.CHECK_KNOWN) && (object.isL2Npc())) {
      activeChar.sendMessage("Removed NPC: " + ((L2NpcInstance)object).getName());
    }
    return true;
  }

  public final L2PcInstance getActiveChar()
  {
    return activeChar;
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    int knownlistSize = getKnownObjects().size();
    if (knownlistSize <= 25) {
      return 4200;
    }
    if (knownlistSize <= 35) {
      return 3600;
    }
    if (knownlistSize <= 70) {
      return 2910;
    }
    return 2310;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    int knownlistSize = getKnownObjects().size();

    if (knownlistSize <= 25) {
      return 3500;
    }
    if (knownlistSize <= 35) {
      return 2900;
    }
    if (knownlistSize <= 70) {
      return 2300;
    }
    return 1700;
  }
}