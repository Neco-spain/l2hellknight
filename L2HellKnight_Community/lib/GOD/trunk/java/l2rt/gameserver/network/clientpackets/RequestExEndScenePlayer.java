package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;

public class RequestExEndScenePlayer extends L2GameClientPacket
{
	private int _movieId;

	@Override
	public void runImpl()
	{
		L2Player cha = getClient().getActiveChar();
		if(cha == null)
			return;
		if(cha.getMovieId() != _movieId)
			return;
		cha.setMovieId(0);
		cha.decayMe();
		cha.spawnMe();
	}

	/**
	 * format: d
	 */
	@Override
	public void readImpl()
	{
		_movieId = readD();
	}
}