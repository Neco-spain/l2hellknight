package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Point3D;

public final class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
  private final Point3D _pos = new Point3D(0, 0, 0);
  private final Point3D _origin_pos = new Point3D(0, 0, 0);
  private int _boatId;

  public TaskPriority getPriority()
  {
    return TaskPriority.PR_HIGH;
  }

  protected void readImpl()
  {
    _boatId = readD();
    int _x = readD();
    int _y = readD();
    int _z = readD();
    _pos.setXYZ(_x, _y, _z);
    _x = readD();
    _y = readD();
    _z = readD();
    _origin_pos.setXYZ(_x, _y, _z);
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if ((activeChar.isAttackingNow()) && (activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW))
    {
      activeChar.sendPacket(new ActionFailed());
    }
    else
    {
      if (!activeChar.isInBoat())
      {
        activeChar.setInBoat(true);
      }
      L2BoatInstance boat = BoatManager.getInstance().GetBoat(_boatId);
      activeChar.setBoat(boat);
      activeChar.setInBoatPosition(_pos);
      activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO_IN_A_BOAT, new L2CharPosition(_pos.getX(), _pos.getY(), _pos.getZ(), 0), new L2CharPosition(_origin_pos.getX(), _origin_pos.getY(), _origin_pos.getZ(), 0));
    }
  }

  public String getType()
  {
    return null;
  }
}