package l2r.gameserver.model.items.attachment;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onLogout(Player player);
	//FIXME [VISTALL] возможно переделать на слушатели игрока
	void onDeath(Player owner, Creature killer);

	boolean canAttack(Player player);

	boolean canCast(Player player, Skill skill);
}
