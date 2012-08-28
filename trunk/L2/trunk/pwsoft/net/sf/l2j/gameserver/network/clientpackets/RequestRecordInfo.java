package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
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
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.DoorInfo;
import net.sf.l2j.gameserver.network.serverpackets.DoorStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.GetOnVehicle;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.RelationChanged;
import net.sf.l2j.gameserver.network.serverpackets.ServerObjectInfo;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItem;
import net.sf.l2j.gameserver.network.serverpackets.SpawnItemPoly;
import net.sf.l2j.gameserver.network.serverpackets.StaticObject;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.util.Point3D;

public class RequestRecordInfo extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance _activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (_activeChar == null) {
      return;
    }
    _activeChar.getKnownList().updateKnownObjects();
    _activeChar.sendPacket(new UserInfo(_activeChar));

    for (L2Object object : _activeChar.getKnownList().getKnownObjects().values())
    {
      if (object == null) {
        continue;
      }
      if ((object.getPoly().isMorphed()) && (object.getPoly().getPolyType().equals("item"))) {
        _activeChar.sendPacket(new SpawnItemPoly(object));
      }
      else {
        if (object.isL2Item()) {
          _activeChar.sendPacket(new SpawnItem((L2ItemInstance)object));
        } else if (object.isL2Door())
        {
          L2DoorInstance door = (L2DoorInstance)object;
          _activeChar.sendPacket(new DoorInfo(door));
          _activeChar.sendPacket(new DoorStatusUpdate(door, true));
        }
        else if ((object instanceof L2BoatInstance))
        {
          if ((!_activeChar.isInBoat()) && (object != _activeChar.getBoat()))
          {
            _activeChar.sendPacket(new VehicleInfo((L2BoatInstance)object));
            ((L2BoatInstance)object).sendVehicleDeparture(_activeChar);
          }
        }
        else if ((object instanceof L2StaticObjectInstance)) {
          _activeChar.sendPacket(new StaticObject((L2StaticObjectInstance)object));
        } else if (object.isL2Npc())
        {
          if (((L2NpcInstance)object).getRunSpeed() == 0)
            _activeChar.sendPacket(new ServerObjectInfo((L2NpcInstance)object, _activeChar));
          else
            _activeChar.sendPacket(new NpcInfo((L2NpcInstance)object, _activeChar));
        }
        else if (object.isL2Summon())
        {
          L2Summon summon = (L2Summon)object;

          if (_activeChar.equals(summon.getOwner()))
          {
            summon.broadcastStatusUpdate();

            if (summon.isPet())
              _activeChar.sendPacket(new PetItemList((L2PetInstance)summon));
          }
          else {
            _activeChar.sendPacket(new NpcInfo(summon, _activeChar, 1));
          }

          summon.updateEffectIcons(true);
        }
        else if (object.isPlayer())
        {
          L2PcInstance otherPlayer = object.getPlayer();

          if (otherPlayer.isInBoat())
          {
            otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition().getWorldPosition());
            _activeChar.sendPacket(new CharInfo(otherPlayer));
            int relation = otherPlayer.getRelation(_activeChar);
            if ((otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(_activeChar.getObjectId())) != null) && (((Integer)otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(_activeChar.getObjectId()))).intValue() != relation))
              _activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
            _activeChar.sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
          }
          else
          {
            _activeChar.sendPacket(new CharInfo(otherPlayer));
            int relation = otherPlayer.getRelation(_activeChar);
            if ((otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(_activeChar.getObjectId())) != null) && (((Integer)otherPlayer.getKnownList().getKnownRelations().get(Integer.valueOf(_activeChar.getObjectId()))).intValue() != relation)) {
              _activeChar.sendPacket(new RelationChanged(otherPlayer, relation, _activeChar.isAutoAttackable(otherPlayer)));
            }
          }
        }
        if (object.isL2Character())
        {
          L2Character obj = (L2Character)object;
          obj.getAI().describeStateToPlayer(_activeChar);
        }
      }
    }
  }
}