package l2r.gameserver.network.serverpackets;

import java.util.Collection;

import l2r.gameserver.model.Player;
import l2r.gameserver.model.petition.PetitionMainGroup;
import l2r.gameserver.model.petition.PetitionSubGroup;
import l2r.gameserver.utils.Language;

/**
 * @author VISTALL
 */
public class ExResponseShowStepTwo extends L2GameServerPacket
{
	private Language _language;
	private PetitionMainGroup _petitionMainGroup;

	public ExResponseShowStepTwo(Player player, PetitionMainGroup gr)
	{
		_language = player.getLanguage();
		_petitionMainGroup = gr;
	}

	@Override
	protected void writeImpl()
	{
		writeEx(0xAF);
		Collection<PetitionSubGroup> subGroups = _petitionMainGroup.getSubGroups();
		writeD(subGroups.size());
		writeS(_petitionMainGroup.getDescription(_language));
		for(PetitionSubGroup g : subGroups)
		{
			writeC(g.getId());
			writeS(g.getName(_language));
		}
	}
}