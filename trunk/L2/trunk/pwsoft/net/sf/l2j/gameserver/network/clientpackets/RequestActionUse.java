package net.sf.l2j.gameserver.network.clientpackets;

import java.util.Map;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2StaticObjectInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ChairSit;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopManageList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.PeaceZone;

public final class RequestActionUse extends L2GameClientPacket
{
  private static final Logger _log = Logger.getLogger(RequestActionUse.class.getName());
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
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPAN() < 200L) {
      player.sendActionFailed();
      return;
    }

    player.sCPAN();

    if (player.isAlikeDead()) {
      player.sendActionFailed();
      return;
    }

    if ((player.isOutOfControl()) || (player.isParalyzed())) {
      player.sendActionFailed();
      return;
    }

    if (player.isCastingNow()) {
      player.sendActionFailed();
      return;
    }

    L2Summon pet = player.getPet();
    L2Object target = player.getTarget();

    switch (_actionId) {
    case 0:
      if (player.getMountType() != 0)
      {
        break;
      }
      if ((target != null) && (!player.isSitting()) && ((target instanceof L2StaticObjectInstance)) && (((L2StaticObjectInstance)target).getType() == 1) && (CastleManager.getInstance().getCastle(target) != null) && (player.isInsideRadius(target, 150, false, false)))
      {
        player.sitDown();
        player.broadcastPacket(new ChairSit(player, ((L2StaticObjectInstance)target).getStaticObjectId()));
      }
      else if (player.isSitting()) {
        player.standUp();
      } else {
        player.sitDown();
      }

      break;
    case 1:
      if (player.isRunning())
        player.setWalking();
      else {
        player.setRunning();
      }

      break;
    case 15:
    case 21:
      if ((pet == null) || (pet.isMovementDisabled()) || (player.isBetrayed())) break;
      pet.setFollowStatus(!pet.getFollowStatus()); break;
    case 16:
    case 22:
      if ((target == null) || (pet == null) || (pet == target) || (pet.isDead())) {
        player.sendActionFailed();
        return;
      }

      if ((player.isBetrayed()) || (PeaceZone.getInstance().inPeace(player, target)))
      {
        player.sendActionFailed();
        return;
      }

      if ((player.isInOlympiadMode()) && (!player.isOlympiadCompStart())) {
        player.sendActionFailed();
        return;
      }

      if ((target.isL2Door()) && (!target.isAutoAttackable(pet))) {
        player.sendActionFailed();
        return;
      }

      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
      break;
    case 17:
    case 23:
      if ((pet == null) || (pet.isMovementDisabled()) || (player.isBetrayed())) break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null); break;
    case 19:
      if ((pet == null) || (player.isBetrayed()))
        break;
      if (pet.isDead()) {
        player.sendPacket(Static.DEAD_PET_CANNOT_BE_RETURNED);
      } else if ((pet.isAttackingNow()) || (pet.isRooted())) {
        player.sendPacket(Static.PET_CANNOT_SENT_BACK_DURING_BATTLE);
      }
      else {
        if (!pet.isPet()) break;
        L2PetInstance petInst = (L2PetInstance)pet;

        if (petInst.getCurrentFed() > petInst.getMaxFed() * 0.4D)
          pet.unSummon(player);
        else
          player.sendPacket(Static.YOU_CANNOT_RESTORE_HUNGRY_PETS);
      }
      break;
    case 38:
      player.tryMountPet(pet);
      break;
    case 32:
      useSkill(4230);
      break;
    case 36:
      useSkill(4259);
      break;
    case 37:
      if (player.isAlikeDead()) {
        player.sendActionFailed();
        return;
      }
      if (player.getPrivateStoreType() != 0) {
        player.setPrivateStoreType(0);
        player.broadcastUserInfo();
      }
      if (player.isSitting()) {
        player.standUp();
      }

      if (player.getCreateList() == null) {
        player.setCreateList(new L2ManufactureList());
      }

      player.sendPacket(new RecipeShopManageList(player, true));
      break;
    case 39:
      useSkill(4138);
      break;
    case 41:
      useSkill(4230);
      break;
    case 42:
      useSkill(4378, player);
      break;
    case 43:
      useSkill(4137);
      break;
    case 44:
      useSkill(4139);
      break;
    case 45:
      useSkill(4025, player);
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
      if (player.isAlikeDead()) {
        player.sendActionFailed();
        return;
      }
      if (player.getPrivateStoreType() != 0) {
        player.setPrivateStoreType(0);
        player.broadcastUserInfo();
      }
      if (player.isSitting()) {
        player.standUp();
      }

      if (player.getCreateList() == null) {
        player.setCreateList(new L2ManufactureList());
      }

      player.sendPacket(new RecipeShopManageList(player, false));
      break;
    case 52:
      if ((pet == null) || (!pet.isSummon())) break;
      pet.unSummon(player); break;
    case 53:
      if ((target == null) || (pet == null) || (pet == target) || (pet.isMovementDisabled())) break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), pet.calcHeading(target.getX(), target.getY()))); break;
    case 54:
      if ((target == null) || (pet == null) || (pet == target) || (pet.isMovementDisabled())) break;
      pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), pet.calcHeading(target.getX(), target.getY()))); break;
    case 96:
      _log.info("98 Accessed");
      break;
    case 97:
      _log.info("97 Accessed");

      break;
    case 1000:
      if (!target.isL2Door()) break;
      useSkill(4079); break;
    case 1001:
      break;
    case 1003:
      useSkill(4710);
      break;
    case 1004:
      useSkill(4711, player);
      break;
    case 1005:
      useSkill(4712);
      break;
    case 1006:
      useSkill(4713, player);
      break;
    case 1007:
      useSkill(4699, player);
      break;
    case 1008:
      useSkill(4700, player);
      break;
    case 1009:
      useSkill(4701);
      break;
    case 1010:
      useSkill(4702, player);
      break;
    case 1011:
      useSkill(4703, player);
      break;
    case 1012:
      useSkill(4704);
      break;
    case 1013:
      useSkill(4705);
      break;
    case 1014:
      useSkill(4706, player);
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
      if (target.isL2Door()) break;
      useSkill(5110); break;
    case 1040:
      if (target.isL2Door()) break;
      useSkill(5111); break;
    case 61:
      player.sendMessage("\u041D\u0435 \u0440\u0430\u0431\u043E\u0442\u0430\u0435\u0442");
      break;
    case 63:
      player.sendMessage("\u041D\u0435 \u0440\u0430\u0431\u043E\u0442\u0430\u0435\u0442");
      break;
    case 64:
      player.sendMessage("\u041D\u0435 \u0440\u0430\u0431\u043E\u0442\u0430\u0435\u0442");
      break;
    default:
      _log.warning(player.getName() + ": unhandled action type " + _actionId);
    }
  }

  private void useSkill(int skillId, L2Object target)
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    L2Summon activeSummon = player.getPet();

    if (player.getPrivateStoreType() != 0) {
      player.sendMessage("Cannot use skills while trading");
      return;
    }

    if ((activeSummon != null) && (!player.isBetrayed())) {
      Map _skills = activeSummon.getTemplate().getSkills();

      if (_skills == null) {
        return;
      }

      if (_skills.size() == 0) {
        player.sendPacket(Static.SKILL_NOT_AVAILABLE);
        return;
      }

      L2Skill skill = (L2Skill)_skills.get(Integer.valueOf(skillId));

      if (skill == null)
      {
        return;
      }

      activeSummon.setTarget(target);
      activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed);
    }
  }

  private void useSkill(int skillId)
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    useSkill(skillId, player.getTarget());
  }
}