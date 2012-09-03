package npc.model;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2MerchantInstance;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Files;

public class AsamahInstance extends L2MerchantInstance
    implements ScriptFile
{

    public AsamahInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onLoad()
    {
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    public void showChatWindow(L2Player player, int val)
    {
        NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
        QuestState quest = player.getQuestState("_111_ElrokianHuntersProof");
        String html;
        if(player.getLang().equalsIgnoreCase("ru"))
            html = Files.read("data/html-ru/default/32115.htm", player);
        else
            html = Files.read("data/html/default/32115.htm", player);
        if(player.isGM() || quest != null && quest.isCompleted())
            if(player.getLang().equalsIgnoreCase("ru"))
                html = (new StringBuilder()).append(html).append("<br><a action=\"bypass -h scripts_services.Talks.Asamah:AsamahTrap\">\u0412\u0437\u044F\u0442\u044C \u043B\u043E\u0432\u0443\u0448\u043A\u0443.</a>").toString();
            else
                html = (new StringBuilder()).append(html).append("<br><a action=\"bypass -h scripts_services.Talks.Asamah:AsamahTrap\">Equip the trap.</a>").toString();
        msg.setHtml(html);
        player.sendPacket(new L2GameServerPacket[] {
            msg
        });
    }
}
