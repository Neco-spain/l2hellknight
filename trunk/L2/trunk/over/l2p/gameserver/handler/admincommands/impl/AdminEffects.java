package l2p.gameserver.handler.admincommands.impl;

import java.util.List;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.admincommands.IAdminCommandHandler;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.World;
import l2p.gameserver.model.base.InvisibleType;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.serverpackets.Earthquake;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.skills.AbnormalEffect;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Util;

public class AdminEffects
  implements IAdminCommandHandler
{
  public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
  {
    Commands command = (Commands)comm;

    if (!activeChar.getPlayerAccess().GodMode) {
      return false;
    }

    AbnormalEffect ae = AbnormalEffect.NULL;
    GameObject target = activeChar.getTarget();
    int val;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminEffects$Commands[command.ordinal()])
    {
    case 1:
    case 2:
      if (activeChar.isInvisible())
      {
        activeChar.setInvisibleType(InvisibleType.NONE);
        activeChar.broadcastCharInfo();
        if (activeChar.getPet() == null) break;
        activeChar.getPet().broadcastCharInfo();
      }
      else
      {
        activeChar.setInvisibleType(InvisibleType.NORMAL);
        activeChar.sendUserInfo(true);
        World.removeObjectFromPlayers(activeChar);
      }
      break;
    case 3:
      int val;
      if (wordList.length < 2)
        val = 0;
      else
        try
        {
          val = Integer.parseInt(wordList[1]);
        }
        catch (Exception e)
        {
          activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
          return false;
        }
      List superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
      int sh_level = superhaste.isEmpty() ? 0 : superhaste == null ? 0 : ((Effect)superhaste.get(0)).getSkill().getLevel();

      if (val == 0)
      {
        if (sh_level != 0)
          activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true);
        activeChar.unsetVar("gm_gmspeed");
      }
      else if ((val >= 1) && (val <= 4))
      {
        if (Config.SAVE_GM_EFFECTS)
          activeChar.setVar("gm_gmspeed", String.valueOf(val), -1L);
        if (val == sh_level)
          break;
        if (sh_level != 0)
          activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true);
        activeChar.doCast(SkillTable.getInstance().getInfo(7029, val), activeChar, true);
      }
      else
      {
        activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
      }break;
    case 4:
      handleInvul(activeChar, activeChar);
      if (activeChar.isInvul())
      {
        if (!Config.SAVE_GM_EFFECTS) break;
        activeChar.setVar("gm_invul", "true", -1L);
      }
      else {
        activeChar.unsetVar("gm_invul");
      }
    }

    if (!activeChar.isGM())
      return false;
    int val;
    switch (1.$SwitchMap$l2p$gameserver$handler$admincommands$impl$AdminEffects$Commands[command.ordinal()])
    {
    case 5:
      for (Player player : GameObjectsStorage.getAllPlayers())
        if ((player != null) && (player.isInOfflineMode()))
        {
          player.setInvisibleType(InvisibleType.NONE);
          player.decayMe();
          player.spawnMe();
        }
      break;
    case 6:
      for (Player player : GameObjectsStorage.getAllPlayers())
        if ((player != null) && (player.isInOfflineMode()))
        {
          player.setInvisibleType(InvisibleType.NORMAL);
          player.decayMe();
        }
      break;
    case 7:
      try
      {
        int intensity = Integer.parseInt(wordList[1]);
        int duration = Integer.parseInt(wordList[2]);
        activeChar.broadcastPacket(new L2GameServerPacket[] { new Earthquake(activeChar.getLoc(), intensity, duration) });
      }
      catch (Exception e)
      {
        activeChar.sendMessage("USAGE: //earthquake intensity duration");
        return false;
      }

    case 8:
      if ((target == null) || (!target.isCreature()))
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      if (((Creature)target).isBlocked())
        return false;
      ((Creature)target).abortAttack(true, false);
      ((Creature)target).abortCast(true, false);
      ((Creature)target).block();
      activeChar.sendMessage("Target blocked.");
      break;
    case 9:
      if ((target == null) || (!target.isCreature()))
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      if (!((Creature)target).isBlocked())
        return false;
      ((Creature)target).unblock();
      activeChar.sendMessage("Target unblocked.");
      break;
    case 10:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //changename newName");
        return false;
      }
      if (target == null)
        target = activeChar;
      if (!target.isCreature())
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      String oldName = ((Creature)target).getName();
      String newName = Util.joinStrings(" ", wordList, 1);

      ((Creature)target).setName(newName);
      ((Creature)target).broadcastCharInfo();

      activeChar.sendMessage(new StringBuilder().append("Changed name from ").append(oldName).append(" to ").append(newName).append(".").toString());
      break;
    case 11:
      if ((target == null) || (!target.isPlayer()))
      {
        activeChar.sendPacket(Msg.INVALID_TARGET);
        return false;
      }
      handleInvul(activeChar, (Player)target);
      break;
    case 12:
      if ((target == null) || (!target.isCreature())) break;
      activeChar.sendMessage(new StringBuilder().append("Target ").append(target.getName()).append("(object ID: ").append(target.getObjectId()).append(") is ").append(!((Creature)target).isInvul() ? "NOT " : "").append("invul").toString()); break;
    case 13:
      if (wordList.length < 2)
        val = Rnd.get(1, 7);
      else
        try
        {
          val = Integer.parseInt(wordList[1]);
        }
        catch (NumberFormatException nfe)
        {
          activeChar.sendMessage("USAGE: //social value");
          return false;
        }
      if ((target == null) || (target == activeChar)) {
        activeChar.broadcastPacket(new L2GameServerPacket[] { new SocialAction(activeChar.getObjectId(), val) }); } else {
        if (!target.isCreature()) break;
        ((Creature)target).broadcastPacket(new L2GameServerPacket[] { new SocialAction(target.getObjectId(), val) }); } break;
    case 14:
      try
      {
        if (wordList.length > 1)
          ae = AbnormalEffect.getByName(wordList[1]);
      }
      catch (Exception e)
      {
        activeChar.sendMessage("USAGE: //abnormal name");
        activeChar.sendMessage("//abnormal - Clears all abnormal effects");
        return false;
      }

      Creature effectTarget = target == null ? activeChar : (Creature)target;

      if (ae == AbnormalEffect.NULL)
      {
        effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
        effectTarget.sendMessage("Abnormal effects clearned by admin.");
        if (effectTarget == activeChar) break;
        effectTarget.sendMessage("Abnormal effects clearned.");
      }
      else
      {
        effectTarget.startAbnormalEffect(ae);
        effectTarget.sendMessage(new StringBuilder().append("Admin added abnormal effect: ").append(ae.getName()).toString());
        if (effectTarget == activeChar) break;
        effectTarget.sendMessage(new StringBuilder().append("Added abnormal effect: ").append(ae.getName()).toString()); } break;
    case 15:
      try
      {
        val = Integer.parseInt(wordList[1]);
      }
      catch (Exception e)
      {
        activeChar.sendMessage("USAGE: //transform transform_id");
        return false;
      }
      activeChar.setTransformation(val);
      break;
    case 16:
      if (wordList.length < 2)
      {
        activeChar.sendMessage("USAGE: //showmovie id");
        return false;
      }
      int id;
      try {
        id = Integer.parseInt(wordList[1]);
      }
      catch (NumberFormatException e)
      {
        activeChar.sendMessage("You must specify id");
        return false;
      }
      activeChar.showQuestMovie(id);
    }

    return true;
  }

  private void handleInvul(Player activeChar, Player target)
  {
    if (target.isInvul())
    {
      target.setIsInvul(false);
      target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
      if (target.getPet() != null)
      {
        target.getPet().setIsInvul(false);
        target.getPet().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
      }
      activeChar.sendMessage(new StringBuilder().append(target.getName()).append(" is now mortal.").toString());
    }
    else
    {
      target.setIsInvul(true);
      target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
      if (target.getPet() != null)
      {
        target.getPet().setIsInvul(true);
        target.getPet().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
      }
      activeChar.sendMessage(new StringBuilder().append(target.getName()).append(" is now immortal.").toString());
    }
  }

  public Enum[] getAdminCommandEnum()
  {
    return Commands.values();
  }

  private static enum Commands
  {
    admin_invis, 
    admin_vis, 
    admin_offline_vis, 
    admin_offline_invis, 
    admin_earthquake, 
    admin_block, 
    admin_unblock, 
    admin_changename, 
    admin_gmspeed, 
    admin_invul, 
    admin_setinvul, 
    admin_getinvul, 
    admin_social, 
    admin_abnormal, 
    admin_transform, 
    admin_showmovie;
  }
}