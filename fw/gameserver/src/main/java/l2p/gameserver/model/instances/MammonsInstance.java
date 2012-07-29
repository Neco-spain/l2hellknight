package l2p.gameserver.model.instances;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MammonsInstance extends NpcInstance {
    private static final Logger _log = LoggerFactory.getLogger(MammonsInstance.class);
    private static final int ANCIENT_ADENA_ID = 5575;
    public static final String MAMMONS_HTML_PATH = "mammons/";

    public MammonsInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this))
            return;

        // first do the common stuff and handle the commands that all NPC classes know
        super.onBypassFeedback(player, command);

        if (command.startsWith("bmarket")) {
            ItemInstance ancientAdena = player.getInventory().getItemByItemId(ANCIENT_ADENA_ID);
            long ancientAdenaAmount = ancientAdena == null ? 0 : ancientAdena.getCount();
            int val = Integer.parseInt(command.substring(11, 12).trim());

            if (command.length() > 12) // SevenSigns x[x] x [x..x]
                val = Integer.parseInt(command.substring(11, 13).trim());

            switch (val) {
                case 1: // Exchange Ancient Adena for Adena
                    long ancientAdenaConvert = 0;
                    try {
                        ancientAdenaConvert = Long.parseLong(command.substring(13).trim());
                    } catch (NumberFormatException e) {
                        player.sendMessage(new CustomMessage("common.IntegerAmount", player));
                        return;
                    } catch (StringIndexOutOfBoundsException e) {
                        player.sendMessage(new CustomMessage("common.IntegerAmount", player));
                        return;
                    }

                    if (ancientAdenaAmount < ancientAdenaConvert || ancientAdenaConvert < 1) {
                        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                        return;
                    }

                    if (player.getInventory().destroyItemByItemId(ANCIENT_ADENA_ID, ancientAdenaConvert)) {
                        player.addAdena(ancientAdenaConvert);
                        player.sendPacket(SystemMessage2.removeItems(ANCIENT_ADENA_ID, ancientAdenaConvert));
                        player.sendPacket(SystemMessage2.obtainItems(57, ancientAdenaConvert, 0));
                    }
                    break;
                default:
                    showChatWindow(player, "blkmrkt.htm");
                    break;
            }
        }
    }

    @Override
    public void showChatWindow(Player player, int val, Object... arg) {
        int npcId = getTemplate().npcId;

        String filename = MAMMONS_HTML_PATH;

        switch (npcId) {
            case 33511: // Priest of Mammon
                filename += "priestmam.htm";
                break;
            case 31092: // Black Marketeer of Mammon
                filename += "blkmrkt.htm";
                break;
            case 31113: // Merchant of Mammon
                filename += "merchmamm.htm";
                break;
            case 31126: // Blacksmith of Mammon
                filename += "blksmithmam.htm";
                break;
            default:
                filename = getHtmlPath(npcId, val, player);
        }

        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }
}