package l2rt.gameserver.model.base;

public class UsablePacketItem
{
	public int itemId;
	public long count;
	public float prob;

	public UsablePacketItem(int itemId, long count, float prob)
	{
		itemId = itemId;
		count = count;
		prob = prob;
	}

	public UsablePacketItem(int itemId, long count)
	{
		itemId = itemId;
		count = count;
	}
	
    public int itemId()
    {
        return itemId;
    }

    public long count()
    {
        return count;
    }

}