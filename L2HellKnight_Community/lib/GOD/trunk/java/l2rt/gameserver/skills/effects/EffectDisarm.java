package l2rt.gameserver.skills.effects;

import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.skills.Env;
import l2rt.gameserver.xml.ItemTemplates;

public final class EffectDisarm extends L2Effect
{
	public EffectDisarm(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(_effected.isPlayer()) {
            L2Player player = _effected.getPlayer();
            if(player == null)
                return;
            if(player.isCursedWeaponEquipped() || player.isCombatFlagEquipped() || player.isTerritoryFlagEquipped())
                return;
            if(player.getActiveWeaponInstance() == null)
                return;
            L2ItemInstance weapon = player.getActiveWeaponInstance();
            if(weapon != null)
                player.getInventory().unEquipItemInBodySlotAndNotify(weapon.getBodyPart(), weapon);
        }
        else if(_effected.isNpc() && _effector.isPlayer()) {
            L2NpcInstance npc = (L2NpcInstance) _effected;
            if(npc.getLeftHandItem() != 0 && ItemTemplates.getInstance().createItem(npc.getLeftHandItem()).isWeapon())
                npc.setLHandId(0);
            if(npc.getRightHandItem() != 0 && ItemTemplates.getInstance().createItem(npc.getRightHandItem()).isWeapon())
                npc.setRHandId(0);
            npc.updateAbnormalEffect();
        }
	}

    @Override
    public void onExit() {
        super.onExit();
        if(_effected.isNpc() && _effector.isPlayer()) {
            L2NpcInstance npc = (L2NpcInstance) _effected;
            npc.setLHandId(npc.getTemplate().lhand);
            npc.setRHandId(npc.getTemplate().rhand);
            npc.updateAbnormalEffect();
        }
    }

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}