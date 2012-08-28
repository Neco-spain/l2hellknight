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
import net.sf.l2j.gameserver.network.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItemPoly;
import net.sf.l2j.gameserver.network.serverpackets.StaticObject;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.util.Point3D;

public class PcKnownList extends PlayableKnownList
{
  public PcKnownList(L2PcInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addKnownObject(L2Object object)
  {
    return addKnownObject(object, null);
  }

  public boolean addKnownObject(L2Object object, L2Character dropper)
  {
    if (!super.addKnownObject(object, dropper)) return false;

    if ((object.getPoly().isMorphed()) && (object.getPoly().getPolyType().equals("item")))
    {
      getActiveChar().sendPacket(new SpawnItemPoly(object));
    }
    else
    {
      if ((object instanceof L2ItemInstance))
      {
        if (dropper != null)
          getActiveChar().sendPacket(new DropItem((L2ItemInstance)object, dropper.getObjectId()));
        else
          getActiveChar().sendPacket(new SpawnItem((L2ItemInstance)object));
      }
      else if ((object instanceof L2DoorInstance))
      {
        getActiveChar().sendPacket(new DoorInfo((L2DoorInstance)object, false));
        getActiveChar().sendPacket(new DoorStatusUpdate((L2DoorInstance)object));
      }
      else if ((object instanceof L2BoatInstance))
      {
        if ((!getActiveChar().isInBoat()) && 
          (object != getActiveChar().getBoat()))
        {
          getActiveChar().sendPacket(new VehicleInfo((L2BoatInstance)object));
          ((L2BoatInstance)object).sendVehicleDeparture(getActiveChar());
        }
      }
      else if ((object instanceof L2StaticObjectInstance))
      {
        getActiveChar().sendPacket(new StaticObject((L2StaticObjectInstance)object));
      }
      else if ((object instanceof L2NpcInstance))
      {
        if (Config.CHECK_KNOWN) getActiveChar().sendMessage("Added NPC: " + ((L2NpcInstance)object).getName());
        getActiveChar().sendPacket(new NpcInfo((L2NpcInstance)object, getActiveChar()));
      }
      else if ((object instanceof L2Summon))
      {
        L2Summon summon = (L2Summon)object;

        if (getActiveChar().equals(summon.getOwner()))
        {
          getActiveChar().sendPacket(new PetInfo(summon));

          summon.updateEffectIcons(true);
          if ((summon instanceof L2PetInstance))
          {
            getActiveChar().sendPacket(new PetItemList((L2PetInstance)summon));
          }
        }
        else {
          getActiveChar().sendPacket(new NpcInfo(summon, getActiveChar()));
        }
      } else if ((object instanceof L2PcInstance))
      {
        L2PcInstance otherPlayer = (L2PcInstance)object;
        if (otherPlayer.isInBoat())
        {
          otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());

          getActiveChar().sendPacket(new CharInfo(otherPlayer));
          int relation = otherPlayer.getRelation(getActiveChar());
          if ((otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(getActiveChar().getObjectId())) != null) && (((Integer)otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(getActiveChar().getObjectId()))).intValue() != relation))
            getActiveChar().sendPacket(new RelationChanged(otherPlayer, relation, getActiveChar().isAutoAttackable(otherPlayer)));
          getActiveChar().sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
        }
        else
        {
          getActiveChar().sendPacket(new CharInfo(otherPlayer));
          int relation = otherPlayer.getRelation(getActiveChar());
          if ((otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(getActiveChar().getObjectId())) != null) && (((Integer)otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(getActiveChar().getObjectId()))).intValue() != relation)) {
            getActiveChar().sendPacket(new RelationChanged(otherPlayer, relation, getActiveChar().isAutoAttackable(otherPlayer)));
          }
        }
        if (otherPlayer.getPrivateStoreType() == 1)
          getActiveChar().sendPacket(new PrivateStoreMsgSell(otherPlayer));
        else if (otherPlayer.getPrivateStoreType() == 3)
          getActiveChar().sendPacket(new PrivateStoreMsgBuy(otherPlayer));
        else if (otherPlayer.getPrivateStoreType() == 5)
          getActiveChar().sendPacket(new RecipeShopMsg(otherPlayer));
      }
      if ((object instanceof L2Character))
      {
        L2Character obj = (L2Character)object;
        obj.getAI().describeStateToPlayer(getActiveChar());
      }
    }

    return true;
  }

  public boolean removeKnownObject(L2Object object)
  {
    if (!super.removeKnownObject(object)) return false;

    getActiveChar().sendPacket(new DeleteObject(object));
    if ((Config.CHECK_KNOWN) && ((object instanceof L2NpcInstance))) getActiveChar().sendMessage("Removed NPC: " + ((L2NpcInstance)object).getName());
    return true;
  }

  public final L2PcInstance getActiveChar()
  {
    return (L2PcInstance)super.getActiveChar();
  }

  public int getDistanceToForgetObject(L2Object object)
  {
    int knownlistSize = getKnownObjects().size();
    if (knownlistSize <= 25) return 4000;
    if (knownlistSize <= 35) return 3500;
    if (knownlistSize <= 70) return 2910;
    return 2310;
  }

  public int getDistanceToWatchObject(L2Object object)
  {
    int knownlistSize = getKnownObjects().size();

    if (knownlistSize <= 25) return 3400;
    if (knownlistSize <= 35) return 2900;
    if (knownlistSize <= 70) return 2300;
    return 1700;
  }
}