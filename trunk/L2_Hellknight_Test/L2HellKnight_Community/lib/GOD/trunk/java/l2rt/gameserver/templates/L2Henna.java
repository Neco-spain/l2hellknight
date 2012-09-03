package l2rt.gameserver.templates;

public class L2Henna
{
	private final int symbol_id;
	private final int dye;
	private final int price;
	private final int amount;
	private final int stat_INT;
	private final int stat_STR;
	private final int stat_CON;
	private final int stat_MEN;
	private final int stat_DEX;
	private final int stat_WIT;
	
	//public final int [] Elements = {0,0,0,0,0,0};

	public L2Henna(StatsSet set)
	{
		symbol_id = set.getInteger("symbol_id");
		dye = set.getInteger("dye");
		price = set.getInteger("price");
		amount = set.getInteger("amount");
		stat_INT = set.getInteger("stat_INT");
		stat_STR = set.getInteger("stat_STR");
		stat_CON = set.getInteger("stat_CON");
		stat_MEN = set.getInteger("stat_MEN");
		stat_DEX = set.getInteger("stat_DEX");
		stat_WIT = set.getInteger("stat_WIT");
		/**Elements[0] = set.getByte("fire");
		Elements[1] = set.getByte("wind");
		Elements[2] = set.getByte("water");
		Elements[3] = set.getByte("earth");
		Elements[4] = set.getByte("unholy");
		Elements[5] = set.getByte("sacred");**/
	}

	public int getSymbolId()
	{
		return symbol_id;
	}

	public int getDyeId()
	{
		return dye;
	}

	public int getPrice()
	{
		return price;
	}

	public int getAmountDyeRequire()
	{
		return amount;
	}

	public int getStatINT()
	{
		return stat_INT;
	}

	public int getStatSTR()
	{
		return stat_STR;
	}

	public int getStatCON()
	{
		return stat_CON;
	}

	public int getStatMEN()
	{
		return stat_MEN;
	}

	public int getStatDEX()
	{
		return stat_DEX;
	}

	public int getStatWIT()
	{
		return stat_WIT;
	}	
	
	/*public byte[] getStatElements()
	{
		return Elements;
	}  */  
}