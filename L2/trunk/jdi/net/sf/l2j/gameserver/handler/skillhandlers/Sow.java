package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public class Sow
  implements ISkillHandler
{
  private static Logger _log = Logger.getLogger(Sow.class.getName());
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.SOW };
  private L2PcInstance _activeChar;
  private L2MonsterInstance _target;
  private int _seedId;

  public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
  {
    if (!(activeChar instanceof L2PcInstance)) {
      return;
    }
    _activeChar = ((L2PcInstance)activeChar);

    L2Object[] targetList = skill.getTargetList(activeChar);

    if (targetList == null) {
      return;
    }

    for (int index = 0; index < targetList.length; index++) {
      if (!(targetList[0] instanceof L2MonsterInstance)) {
        continue;
      }
      _target = ((L2MonsterInstance)targetList[0]);

      if (_target.isSeeded()) {
        _activeChar.sendPacket(new ActionFailed());
      }
      else if (_target.isDead()) {
        _activeChar.sendPacket(new ActionFailed());
      }
      else if (_target.getSeeder() != _activeChar) {
        _activeChar.sendPacket(new ActionFailed());
      }
      else
      {
        _seedId = _target.getSeedType();
        if (_seedId == 0) {
          _activeChar.sendPacket(new ActionFailed());
        }
        else
        {
          L2ItemInstance item = _activeChar.getInventory().getItemByItemId(_seedId);

          _activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

          SystemMessage sm = null;
          if (calcSuccess()) {
            _activeChar.sendPacket(new PlaySound("Itemsound.quest_itemget"));
            _target.setSeeded();
            sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
          } else {
            sm = new SystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
          }
          if (_activeChar.getParty() == null)
            _activeChar.sendPacket(sm);
          else {
            _activeChar.getParty().broadcastToPartyMembers(sm);
          }

          _target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
      }
    }
  }

  private boolean calcSuccess() {
    int basicSuccess = L2Manor.getInstance().isAlternative(_seedId) ? 20 : 90;
    int minlevelSeed = 0;
    int maxlevelSeed = 0;
    minlevelSeed = L2Manor.getInstance().getSeedMinLevel(_seedId);
    maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(_seedId);

    int levelPlayer = _activeChar.getLevel();
    int levelTarget = _target.getLevel();

    if (levelTarget < minlevelSeed)
      basicSuccess -= 5;
    if (levelTarget > maxlevelSeed) {
      basicSuccess -= 5;
    }

    int diff = levelPlayer - levelTarget;
    if (diff < 0)
      diff = -diff;
    if (diff > 5) {
      basicSuccess -= 5 * (diff - 5);
    }

    if (basicSuccess < 1) {
      basicSuccess = 1;
    }
    int rate = Rnd.nextInt(99);

    return rate < basicSuccess;
  }

  public L2Skill.SkillType[] getSkillIds() {
    return SKILL_IDS;
  }
}