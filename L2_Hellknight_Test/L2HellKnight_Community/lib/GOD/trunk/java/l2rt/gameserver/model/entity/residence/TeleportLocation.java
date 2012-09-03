package l2rt.gameserver.model.entity.residence;

import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

public class TeleportLocation
{
	public final long _price;
	public final L2Item _item;
	public final String _name;
	public final String _target;

	public TeleportLocation(String target, int item, long price, String name)
	{
		_target = target;
		_price = price;
		_name = name;
		_item = ItemTemplates.getInstance().getTemplate(item);
	}
}