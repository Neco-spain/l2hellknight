package l2r.gameserver.handler.admincommands.impl;

import java.util.List;

import l2r.commons.util.Rnd;
import l2r.gameserver.Config;
import l2r.gameserver.handler.admincommands.IAdminCommandHandler;
import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Effect;
import l2r.gameserver.model.GameObject;
import l2r.gameserver.model.GameObjectsStorage;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.World;
import l2r.gameserver.model.base.InvisibleType;
import l2r.gameserver.network.serverpackets.Earthquake;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.skills.AbnormalEffect;
import l2r.gameserver.tables.SkillTable;
import l2r.gameserver.utils.Util;


public class AdminEffects implements IAdminCommandHandler
{
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
        admin_callskill,
		admin_showmovie
	}

	@Override
	public boolean useAdminCommand(Enum comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().GodMode)
			return false;

		int val,id,lvl;
		AbnormalEffect ae = AbnormalEffect.NULL;
		GameObject target = activeChar.getTarget();

		switch(command)
		{
			case admin_invis:
			case admin_vis:
				if(activeChar.isInvisible())
				{
					activeChar.setInvisibleType(InvisibleType.NONE);
					activeChar.broadcastCharInfo();
					if(activeChar.getPet() != null)
						activeChar.getPet().broadcastCharInfo();
				}
				else
				{
					activeChar.setInvisibleType(InvisibleType.NORMAL);
					activeChar.sendUserInfo(true);
					World.removeObjectFromPlayers(activeChar);
				}
				break;
			case admin_gmspeed:
				if(wordList.length < 2)
					val = 0;
				else
					try
				{
						val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
					return false;
				}
				List<Effect> superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
				int sh_level = superhaste == null ? 0 : superhaste.isEmpty() ? 0 : superhaste.get(0).getSkill().getLevel();

				if(val == 0)
				{
					if(sh_level != 0)
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
					activeChar.unsetVar("gm_gmspeed");
				}
				else if(val >= 1 && val <= 4)
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_gmspeed", String.valueOf(val), -1);
					if(val != sh_level)
					{
						if(sh_level != 0)
							activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true); //снимаем еффект
						activeChar.doCast(SkillTable.getInstance().getInfo(7029, val), activeChar, true);
					}
				}
				else
					activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
				break;
			case admin_invul:
				handleInvul(activeChar, activeChar);
				if(activeChar.isInvul())
				{
					if(Config.SAVE_GM_EFFECTS)
						activeChar.setVar("gm_invul", "true", -1);
				}
				else
					activeChar.unsetVar("gm_invul");
				break;
		}

		if(!activeChar.isGM())
			return false;

		switch(command)
		{
			case admin_offline_vis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NONE);
						player.decayMe();
						player.spawnMe();
					}
				break;
			case admin_offline_invis:
				for(Player player : GameObjectsStorage.getAllPlayers())
					if(player != null && player.isInOfflineMode())
					{
						player.setInvisibleType(InvisibleType.NORMAL);
						player.decayMe();
					}
				break;
			case admin_earthquake:
				try
				{
					int intensity = Integer.parseInt(wordList[1]);
					int duration = Integer.parseInt(wordList[2]);
					activeChar.broadcastPacket(new Earthquake(activeChar.getLoc(), intensity, duration));
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //earthquake intensity duration");
					return false;
				}
				break;
			case admin_block:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(((Creature) target).isBlocked())
					return false;
				((Creature) target).abortAttack(true, false);
				((Creature) target).abortCast(true, false);
				((Creature) target).block();
				activeChar.sendMessage("Target blocked.");
				break;
			case admin_unblock:
				if(target == null || !target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				if(!((Creature) target).isBlocked())
					return false;
				((Creature) target).unblock();
				activeChar.sendMessage("Target unblocked.");
				break;
			case admin_changename:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("USAGE: //changename newName");
					return false;
				}
				if(target == null)
					target = activeChar;
				if(!target.isCreature())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				String oldName = ((Creature) target).getName();
				String newName = Util.joinStrings(" ", wordList, 1);

				((Creature) target).setName(newName);
				((Creature) target).broadcastCharInfo();

				activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
				break;
			case admin_setinvul:
				if(target == null || !target.isPlayer())
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return false;
				}
				handleInvul(activeChar, (Player) target);
				break;
			case admin_getinvul:
				if(target != null && target.isCreature())
					activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (!((Creature) target).isInvul() ? "NOT " : "") + "invul");
				break;
			case admin_social:
				if(wordList.length < 2)
					val = Rnd.get(1, 7);
				else
					try
				{
						val = Integer.parseInt(wordList[1]);
				}
				catch(NumberFormatException nfe)
				{
					activeChar.sendMessage("USAGE: //social value");
					return false;
				}
				if(target == null || target == activeChar)
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), val));
				else if(target.isCreature())
					((Creature) target).broadcastPacket(new SocialAction(target.getObjectId(), val));
				break;
			case admin_abnormal:
				try
				{
					if(wordList.length > 1)
						ae = AbnormalEffect.getByName(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //abnormal name");
					activeChar.sendMessage("//abnormal - Clears all abnormal effects");
					return false;
				}

				Creature effectTarget = target == null ? activeChar : (Creature) target;

				if(ae == AbnormalEffect.NULL)
				{
					effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
					effectTarget.sendMessage("Abnormal effects clearned by admin.");
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Abnormal effects clearned.");
				}
				else
				{
					effectTarget.startAbnormalEffect(ae);
					effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
					if(effectTarget != activeChar)
						effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
				}
				break;
			case admin_transform:
				try
				{
					val = Integer.parseInt(wordList[1]);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //transform transform_id");
					return false;
				}
				activeChar.setTransformation(val);
				break;
            case admin_callskill:
                try
				{
					id = Integer.parseInt(wordList[1]);
                    lvl = Integer.parseInt(wordList[2]);
                    List<Effect> trasform = activeChar.getEffectList().getEffectsBySkillId(id);
				}
				catch(Exception e)
				{
					activeChar.sendMessage("USAGE: //transform transform_id");
					return false;
				}
                activeChar.doCast(SkillTable.getInstance().getInfo(id, lvl), activeChar, true);
				break;
			case admin_showmovie:
        if(wordList.length < 2)
        {
            activeChar.sendMessage("USAGE: //showmovie id");
            return false;
        }
        try
        {
            id = Integer.parseInt(wordList[1]);
        }
        catch(NumberFormatException e)
        {
            activeChar.sendMessage("You must specify id");
            return false;
        }
        activeChar.showQuestMovie(id);
        break;				
		}

		return true;
	}

	private void handleInvul(Player activeChar, Player target)
	{
		if(target.isInvul())
		{
			target.setIsInvul(false);
			target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(false);
				target.getPet().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now mortal.");
		}
		else
		{
			target.setIsInvul(true);
			target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			if(target.getPet() != null)
			{
				target.getPet().setIsInvul(true);
				target.getPet().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
			}
			activeChar.sendMessage(target.getName() + " is now immortal.");
		}
	}

	@Override
	public Enum[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}