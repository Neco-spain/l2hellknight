package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2SummonAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ChairSit;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopManageList;
import net.sf.l2j.gameserver.network.serverpackets.Ride;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class RequestActionUse extends L2GameClientPacket
{
  private static final String _C__45_REQUESTACTIONUSE = "[C] 45 RequestActionUse";
  private static Logger _log = Logger.getLogger(RequestActionUse.class.getName());
  private int _actionId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _actionId = readD();
    _ctrlPressed = (readD() == 1);
    _shiftPressed = (readC() == 1);
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (Config.DEBUG) {
      _log.finest(new StringBuilder().append(activeChar.getName()).append(" request Action use: id ").append(_actionId).append(" 2:").append(_ctrlPressed).append(" 3:").append(_shiftPressed).toString());
    }

    if (activeChar.isAlikeDead())
    {
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isOutOfControl())
    {
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isCastingNow())
    {
      ((L2GameClient)getClient()).sendPacket(new ActionFailed());
      return;
    }

    L2Summon pet = activeChar.getPet();
    L2Object target = activeChar.getTarget();

    if (Config.DEBUG) {
      _log.info(new StringBuilder().append("Requested Action ID: ").append(String.valueOf(_actionId)).toString());
    }
    switch (_actionId)
    {
    case 0:
      if (activeChar.getMountType() != 0) {
        break;
      }
      if ((target != null) && (!activeChar.isSitting()) && ((target instanceof L2StaticObjectInstance)) && (((L2StaticObjectInstance)target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && (activeChar.isInsideRadius(target, 150, false, false)))
      {
        ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance)target).getStaticObjectId());
        activeChar.sendPacket(cs);
        activeChar.sitDown();
        activeChar.broadcastPacket(cs);
      }
      else
      {
        if (activeChar.isSitting())
        {
          activeChar.standUp();
        }
        else if ((activeChar.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO) || (activeChar.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST))
        {
          activeChar.getAI().setSitDownAfterAction(true);
        }
        else
        {
          activeChar.sitDown();
        }

        if (!Config.DEBUG) break;
        _log.fine(new StringBuilder().append("new wait type: ").append(activeChar.isSitting() ? "SITTING" : "STANDING").toString()); } break;
    case 1:
      if (activeChar.isRunning())
        activeChar.setWalking();
      else {
        activeChar.setRunning();
      }
      if (!Config.DEBUG) break;
      _log.fine(new StringBuilder().append("new move type: ").append(activeChar.isRunning() ? "RUNNING" : "WALKIN").toString()); break;
    case 15:
    case 21:
      if ((pet == null) || (activeChar.isBetrayed())) break;
      ((L2SummonAI)pet.getAI()).notifyFollowStatusChange(); break;
    case 16:
    case 22:
      if ((target == null) || (pet == null) || (pet == target) || (activeChar == target) || (pet.isAttackingDisabled()) || (pet.isBetrayed()))
        break;
      if ((activeChar.isInOlympiadMode()) && (!activeChar.isOlympiadStart()))
      {
        activeChar.sendPacket(new ActionFailed());
        return;
      }

      if ((activeChar.getAccessLevel() < Config.GM_PEACEATTACK) && (activeChar.isInsidePeaceZone(pet, target)))
      {
        if ((!activeChar.isInFunEvent()) || (!target.isInFunEvent()))
        {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
          return;
        }
      }

      if ((!target.isAutoAttackable(activeChar)) && (!_ctrlPressed))
        break;
      if ((target instanceof L2DoorInstance))
      {
        if ((!((L2DoorInstance)target).isAttackable(activeChar)) || (pet.getNpcId() == 14839)) break;
        pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      }
      else {
        if (pet.getNpcId() == 14737) break;
        pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target); } break;
    case 17:
    case 23:
      if ((pet == null) || (pet.isMovementDisabled()) || (activeChar.isBetrayed())) break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null); break;
    case 19:
      if ((pet == null) || (activeChar.isBetrayed())) {
        break;
      }
      if (pet.isDead())
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED));
      }
      else if ((pet.isAttackingNow()) || (pet.isRooted()))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
      }
      else
      {
        if (!(pet instanceof L2PetInstance))
          break;
        L2PetInstance petInst = (L2PetInstance)pet;

        if (petInst.getCurrentFed() > petInst.getMaxFed() * 0.4D)
          pet.unSummon(activeChar);
        else
          activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS)); 
      }
      break;
    case 38:
      if ((pet != null) && (pet.isMountable()) && (!activeChar.isMounted()) && (!activeChar.isBetrayed()))
      {
        if (activeChar.isDead())
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if (pet.isDead())
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if ((pet.isInCombat()) || (pet.isRooted()))
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if ((activeChar.isInCombat()) || (activeChar.getPvpFlag() != 0))
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if ((activeChar.isSitting()) || (activeChar.isMoving()))
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if (activeChar.isFishing())
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
          activeChar.sendPacket(msg);
          msg = null;
        }
        else if (activeChar.isCursedWeaponEquiped())
        {
          SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
          activeChar.sendPacket(msg);
        } else {
          if ((pet.isDead()) || (activeChar.isMounted()))
            break;
          if (!activeChar.disarmWeapons()) return;
          Ride mount = new Ride(activeChar.getObjectId(), 1, pet.getTemplate().npcId);
          activeChar.broadcastPacket(mount);
          activeChar.setMountType(mount.getMountType());
          activeChar.setMountObjectID(pet.getControlItemId());
          pet.unSummon(activeChar);
          activeChar.startUnmountTask();
        }
      }
      else if (activeChar.isRentedPet())
      {
        activeChar.stopRentPet();
      } else {
        if (!activeChar.isMounted())
          break;
        if (!activeChar.setMountType(0))
          break;
        if (activeChar.isFlying()) activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        Ride dismount = new Ride(activeChar.getObjectId(), 0, 0);
        activeChar.broadcastPacket(dismount);
        activeChar.setMountObjectID(0);
        activeChar.stopUnmountTask();
      }break;
    case 32:
      useSkill(4230);
      break;
    case 36:
      useSkill(4259);
      break;
    case 37:
      if (activeChar.isAlikeDead())
      {
        ((L2GameClient)getClient()).sendPacket(new ActionFailed());
        return;
      }
      if (activeChar.getPrivateStoreType() != 0) {
        activeChar.setPrivateStoreType(0);
        activeChar.broadcastUserInfo();
      }
      if (activeChar.isSitting()) {
        activeChar.standUp();
      }
      if (activeChar.getCreateList() == null)
      {
        activeChar.setCreateList(new L2ManufactureList());
      }

      activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
      break;
    case 39:
      useSkill(4138);
      break;
    case 41:
      useSkill(4230);
      break;
    case 42:
      useSkill(4378, activeChar);
      break;
    case 43:
      useSkill(4137);
      break;
    case 44:
      useSkill(4139);
      break;
    case 45:
      useSkill(4025, activeChar);
      break;
    case 46:
      useSkill(4261);
      break;
    case 47:
      useSkill(4260);
      break;
    case 48:
      useSkill(4068);
      break;
    case 51:
      if (activeChar.isAlikeDead())
      {
        ((L2GameClient)getClient()).sendPacket(new ActionFailed());
        return;
      }
      if (activeChar.getPrivateStoreType() != 0) {
        activeChar.setPrivateStoreType(0);
        activeChar.broadcastUserInfo();
      }
      if (activeChar.isSitting()) {
        activeChar.standUp();
      }
      if (activeChar.getCreateList() == null) {
        activeChar.setCreateList(new L2ManufactureList());
      }
      activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
      break;
    case 52:
      if ((pet == null) || (!(pet instanceof L2SummonInstance)))
        break;
      if ((activeChar.isBetrayed()) || (pet.isAttackingNow()) || (pet.isRooted()))
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
      }
      else pet.unSummon(activeChar); break;
    case 53:
      if ((target == null) || (pet == null) || (pet == target) || (pet.isMovementDisabled()))
        break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0)); break;
    case 54:
      if ((target == null) || (pet == null) || (pet == target) || (pet.isMovementDisabled()))
        break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0)); break;
    case 96:
      _log.info("98 Accessed");
      break;
    case 97:
      _log.info("97 Accessed");

      break;
    case 1000:
      if (!(target instanceof L2DoorInstance)) break; useSkill(4079); break;
    case 1001:
      break;
    case 1003:
      useSkill(4710);
      break;
    case 1004:
      useSkill(4711, activeChar);
      break;
    case 1005:
      useSkill(4712);
      break;
    case 1006:
      useSkill(4713, activeChar);
      break;
    case 1007:
      useSkill(4699, activeChar);
      break;
    case 1008:
      useSkill(4700, activeChar);
      break;
    case 1009:
      useSkill(4701);
      break;
    case 1010:
      useSkill(4702, activeChar);
      break;
    case 1011:
      useSkill(4703, activeChar);
      break;
    case 1012:
      useSkill(4704);
      break;
    case 1013:
      useSkill(4705);
      break;
    case 1014:
      useSkill(4706, activeChar);
      break;
    case 1015:
      useSkill(4707);
      break;
    case 1016:
      useSkill(4709);
      break;
    case 1017:
      useSkill(4708);
      break;
    case 1031:
      useSkill(5135);
      break;
    case 1032:
      useSkill(5136);
      break;
    case 1033:
      useSkill(5137);
      break;
    case 1034:
      useSkill(5138);
      break;
    case 1035:
      useSkill(5139);
      break;
    case 1036:
      useSkill(5142);
      break;
    case 1037:
      useSkill(5141);
      break;
    case 1038:
      useSkill(5140);
      break;
    case 1039:
      if ((target instanceof L2DoorInstance)) break;
      useSkill(5110); break;
    case 1040:
      if ((target instanceof L2DoorInstance)) break;
      useSkill(5111); break;
    default:
      _log.warning(new StringBuilder().append(activeChar.getName()).append(": unhandled action type ").append(_actionId).toString());
    }
  }

  private void useSkill(int skillId, L2Object target)
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2Summon activeSummon = activeChar.getPet();

    if (activeChar.getPrivateStoreType() != 0)
    {
      activeChar.sendMessage("Cannot use skills while trading");
      return;
    }

    if ((activeSummon != null) && (!activeChar.isBetrayed()))
    {
      Map _skills = activeSummon.getTemplate().getSkills();

      if (_skills == null) return;

      if (_skills.size() == 0)
      {
        activeChar.sendPacket(new SystemMessage(SystemMessageId.SKILL_NOT_AVAILABLE));
        return;
      }

      L2Skill skill = (L2Skill)_skills.get(Integer.valueOf(skillId));

      if (skill == null)
      {
        if (Config.DEBUG)
          _log.warning(new StringBuilder().append("Skill ").append(skillId).append(" missing from npcskills.sql for a summon id ").append(activeSummon.getNpcId()).toString());
        return;
      }

      if ((skill.isOffensive()) && (activeChar == target))
        return;
      activeSummon.setTarget(target);
      activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed);
    }
  }

  private void useSkill(int skillId)
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    useSkill(skillId, activeChar.getTarget());
  }

  public String getType()
  {
    return "[C] 45 RequestActionUse";
  }
}