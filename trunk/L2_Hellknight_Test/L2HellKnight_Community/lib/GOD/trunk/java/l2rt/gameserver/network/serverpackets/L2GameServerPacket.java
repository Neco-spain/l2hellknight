package l2rt.gameserver.network.serverpackets;

import java.util.logging.Logger;

import l2rt.extensions.network.SendablePacket;
import l2rt.gameserver.model.TradeItem;
import l2rt.gameserver.model.base.MultiSellIngredient;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.xml.ItemTemplates;

public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	//Пускай все наследники используют этот логер
	//P.S. Можно удалить "Logger _log = Logger.getLogger" со всех ServerPacket, они будут наследовать вот этот.
	protected static final Logger _log = Logger.getLogger(L2GameServerPacket.class.getName());
	@Override
	protected void write()
	{
		try
		{
			writeImpl();
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}

	protected abstract void writeImpl();

	protected final static int EXTENDED_PACKET = 0xFE;

	@Override
	protected int getHeaderSize()
	{
		return 2;
	}

	@Override
	protected void writeHeader(int dataSize)
	{
		writeH(dataSize + getHeaderSize());
	}

	protected void writeEnchantEffect(L2ItemInstance item)
	{
        writeH(0);
        writeH(0);
        writeH(0);
	}

	protected void writeEnchantEffect(TradeItem item)
	{
        writeH(0);
        writeH(0);
        writeH(0);
	}

	protected void writeEnchantEffect()
	{
        writeH(0);
        writeH(0);
        writeH(0);
	}

	protected void writeItemElements(L2ItemInstance item)
	{
		writeH(item.getAttackElement()[0]);
		writeH(item.getAttackElement()[1]);
		writeH(item.getDefenceFire());
		writeH(item.getDefenceWater());
		writeH(item.getDefenceWind());
		writeH(item.getDefenceEarth());
		writeH(item.getDefenceHoly());
		writeH(item.getDefenceUnholy());
	}

	protected void writeItemElements(TradeItem item)
	{
		writeH(item.getAttackElement()[0]); // attack element (-2 - none)
		writeH(item.getAttackElement()[1]); // attack element value
		writeH(item.getDefenceFire()); // водная стихия (fire pdef)
		writeH(item.getDefenceWater()); // огненная стихия (water pdef)
		writeH(item.getDefenceWind()); // земляная стихия (wind pdef)
		writeH(item.getDefenceEarth()); // воздушная стихия (earth pdef)
		writeH(item.getDefenceHoly()); // темная стихия (holy pdef)
		writeH(item.getDefenceUnholy()); // светлая стихия (dark pdef)
	}

	protected void writeItemElements(MultiSellIngredient item)
	{
		if(item.getItemId() <= 0)
		{
			writeItemElements();
			return;
		}
		L2Item i = ItemTemplates.getInstance().getTemplate(item.getItemId());
		if(i.isWeapon())
		{
			writeH(item.getElement()); // attack element (-2 - none)
			writeH(item.getElementValue()); // attack element value
			writeH(0); // водная стихия (fire pdef)
			writeH(0); // огненная стихия (water pdef)
			writeH(0); // земляная стихия (wind pdef)
			writeH(0); // воздушная стихия (earth pdef)
			writeH(0); // темная стихия (holy pdef)
			writeH(0); // светлая стихия (dark pdef)
		}
		else if(i.isArmor())
		{
			writeH(-2); // attack element (-2 - none)
			writeH(0); // attack element value
			writeH(item.getElement() == L2Item.ATTRIBUTE_FIRE ? item.getElementValue() : 0); // водная стихия (fire pdef)
			writeH(item.getElement() == L2Item.ATTRIBUTE_WATER ? item.getElementValue() : 0); // огненная стихия (water pdef)
			writeH(item.getElement() == L2Item.ATTRIBUTE_WIND ? item.getElementValue() : 0); // земляная стихия (wind pdef)
			writeH(item.getElement() == L2Item.ATTRIBUTE_EARTH ? item.getElementValue() : 0); // воздушная стихия (earth pdef)
			writeH(item.getElement() == L2Item.ATTRIBUTE_HOLY ? item.getElementValue() : 0); // темная стихия (holy pdef)
			writeH(item.getElement() == L2Item.ATTRIBUTE_DARK ? item.getElementValue() : 0); // светлая стихия (dark pdef)
		}
		else
			writeItemElements();
	}

	protected void writeItemElements()
	{
		writeH(-2); // attack element (-2 - none)
		writeH(0x00); // attack element value
		writeH(0x00); // водная стихия (fire pdef)
		writeH(0x00); // огненная стихия (water pdef)
		writeH(0x00); // земляная стихия (wind pdef)
		writeH(0x00); // воздушная стихия (earth pdef)
		writeH(0x00); // темная стихия (holy pdef)
		writeH(0x00); // светлая стихия (dark pdef)
	}

	public String getType()
	{
		return "[S] " + getClass().getSimpleName();
	}

	@Override
	public String toString()
	{
		return getType() + "; buffer: " + getByteBuffer();
	}
}