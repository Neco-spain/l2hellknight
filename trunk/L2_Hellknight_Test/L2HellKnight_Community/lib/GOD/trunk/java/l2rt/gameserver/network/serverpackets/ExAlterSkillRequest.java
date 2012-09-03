package l2rt.gameserver.network.serverpackets;

// Это кнопка связана с комбо ударами!или ударами в прыжке!судя по мувикам
public class ExAlterSkillRequest extends L2GameServerPacket
{
	public ExAlterSkillRequest()
	{
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x113);
		//ddd
		writeD(0);//id скила (вроде как связано с ид сикла)
		writeD(0); //хз 
		writeD(0);//Время показа кнопки в сек
	}
}