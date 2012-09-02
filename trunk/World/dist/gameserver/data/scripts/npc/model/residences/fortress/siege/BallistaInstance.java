package npc.model.residences.fortress.siege;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.templates.npc.NpcTemplate;

/**
 * Данный инстанс используется NPC Ballista на осадах фортов
 * @author SYS
 */
public class BallistaInstance extends NpcInstance
{
	public BallistaInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		super.onDeath(killer);

		if(killer == null || !killer.isPlayer())
			return;

		Player player = killer.getPlayer();
		if(player.getClan() == null)
			return;

		player.getClan().incReputation(30, false, "Ballista " + getTitle());
		player.sendPacket(new SystemMessage2(SystemMsg.THE_BALLISTA_HAS_BEEN_SUCCESSFULLY_DESTROYED));
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return true;
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}
}