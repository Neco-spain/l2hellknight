package l2rt.gameserver.network.serverpackets;

public class RelationChangedOld extends RelationChanged
{
	public RelationChangedOld(RelationChangedData data)
	{
		super(data);
	}

	@Override
	protected void writeImpl()
	{
		if(_data.size() != 1)
		{
			System.out.println("RelationChangedOld _data.size() != 1 // " + _data.size());
			Thread.dumpStack();
		}

		RelationChangedData data = _data.get(0);
		if(data.charObjId == 0)
			return;
		writeC(0xCE);
		writeRelationChanged(data);
	}
}
