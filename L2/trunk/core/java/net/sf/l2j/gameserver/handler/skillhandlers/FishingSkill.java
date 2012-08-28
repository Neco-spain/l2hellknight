package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;

public class FishingSkill implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.PUMPING, SkillType.REELING};

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;

		L2Fishing fish = player.GetFishCombat();
        if (fish == null)
		{
			if (skill.getSkillType()==SkillType.PUMPING)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
			}
			else if (skill.getSkillType()==SkillType.REELING)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING));
			}
			player.sendPacket(new ActionFailed());
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if(weaponInst == null || weaponItem == null)
			return;
		int SS = 1;
		int pen = 0;
		if (weaponInst != null && weaponInst.getChargedFishshot()) SS = 2;
		double gradebonus = 1 + weaponItem.getCrystalType() * 0.1;
		int dmg = (int)(skill.getPower()*gradebonus*SS);
		if (player.getSkillLevel(1315) <= skill.getLevel()-2)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY));
            pen = 50;
			int penatlydmg = dmg - pen;
			if (player.isGM()) player.sendMessage("Dmg w/o penalty = " +dmg);
			dmg = penatlydmg;
		}
		if (SS > 1)
		{
			weaponInst.setChargedFishshot(false);
		}
		if (skill.getSkillType() == SkillType.REELING)
		{
			fish.useRealing(dmg, pen);
		}
		else
		{
			fish.usePomping(dmg, pen);
		}
    }
    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
