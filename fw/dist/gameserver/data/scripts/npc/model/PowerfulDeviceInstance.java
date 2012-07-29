package npc.model;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.ExChangeToAwakenedClass;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.templates.npc.NpcTemplate;

/**
 * @author : Ragnarok
 * @date : 16.04.12  23:02
 */
public class PowerfulDeviceInstance extends NpcInstance {
    private final int MY_CLASS_ID;

    public PowerfulDeviceInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
        MY_CLASS_ID = getParameter("MyClassId", -1);
    }

    @Override
    public void onBypassFeedback(Player player, String command) {
        if (!canBypassCheck(player, this))
            return;

        if (command.equalsIgnoreCase("Awaken")) {
            NpcHtmlMessage htmlMessage = new NpcHtmlMessage(getObjectId());
            // TODO: Restore SP Count
            htmlMessage.replace("%SP%", "0");
            // TODO: Essences Count
            htmlMessage.replace("%ESSENCES%", "0");

            htmlMessage.setFile("default/" + getNpcId() + "-4.htm");
            player.sendPacket(htmlMessage);
        } else if (command.equalsIgnoreCase("Awaken1")) {
            NpcHtmlMessage htmlMessage = new NpcHtmlMessage(getObjectId());
            // TODO: Deleted Skill list
            /*
                <table><tr><td width=40 height=40><img src="icon.skill0164" width=32 height=32></td><td width=200>Quick Recycle</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1139" width=32 height=32></td><td width=200>Servitor Magic Shield</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1141" width=32 height=32></td><td width=200>Servitor Haste</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1145" width=32 height=32></td><td width=200>Bright Servitor</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1403" width=32 height=32></td><td width=200>Summon Friend</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1140" width=32 height=32></td><td width=200>Servitor Physical Shield</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0143" width=32 height=32></td><td width=200>Cubic Mastery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0228" width=32 height=32></td><td width=200>Fast Spell Casting</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1301" width=32 height=32></td><td width=200>Servitor Blessing</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1299" width=32 height=32></td><td width=200>Servitor Empowerment</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1262" width=32 height=32></td><td width=200>Transfer Pain</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0213" width=32 height=32></td><td width=200>Boost Mana</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0234" width=32 height=32></td><td width=200>Robe Mastery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1384" width=32 height=32></td><td width=200>Mass Surrender to Water</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1277" width=32 height=32></td><td width=200>Summon Merrow the Unicorn</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1227" width=32 height=32></td><td width=200>Summon Mirage the Unicorn</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1226" width=32 height=32></td><td width=200>Summon Boxer the Unicorn</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1332" width=32 height=32></td><td width=200>Summon Seraphim the Unicorn</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1126" width=32 height=32></td><td width=200>Servitor Recharge</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1127" width=32 height=32></td><td width=200>Servitor Heal</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0146" width=32 height=32></td><td width=200>Anti Magic</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0249" width=32 height=32></td><td width=200>Weapon Mastery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1206" width=32 height=32></td><td width=200>Wind Shackle</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0229" width=32 height=32></td><td width=200>Fast Mana Recovery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0212" width=32 height=32></td><td width=200>Fast HP Recovery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0925" width=32 height=32></td><td width=200>Sigil Mastery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0328" width=32 height=32></td><td width=200>Wisdom</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0330" width=32 height=32></td><td width=200>Skill Mastery</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1349" width=32 height=32></td><td width=200>Final Servitor</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1407" width=32 height=32></td><td width=200>Summon Magnus the Unicorn</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill0779" width=32 height=32></td><td width=200>Summon Smart Cubic</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1557" width=32 height=32></td><td width=200>Servitor Share</td></tr></table>
                <table><tr><td width=40 height=40><img src="icon.skill1558" width=32 height=32></td><td width=200>Dimension Spiral</td></tr></table>
            * */
            htmlMessage.replace("%SKILLIST%", "skill list");

            htmlMessage.setFile("default/" + getNpcId() + "-5.htm");
            player.sendPacket(htmlMessage);
        } else if (command.equalsIgnoreCase("Awaken2")) {
            player.setVar("AwakenPrepared", "true", -1);
            player.setVar("AwakenedID", MY_CLASS_ID, -1);
            player.sendPacket(new ExChangeToAwakenedClass(MY_CLASS_ID));
        }
    }


    @Override
    public void showChatWindow(Player player, int val, Object... replace) {
        String htmlpath;
        if (val == 0) {
            if (player.getClassId().getLevel() == 4 && player.getInventory().getCountOf(17600) > 0) {
                int classId = 0;
                for (ClassId classId1 : ClassId.VALUES) {
                    if (classId1.getLevel() == 5 && classId1.childOf(player.getClassId())) {
                        classId = classId1.getId();
                        break;
                    }
                }

                if (player.getSummonList().size() > 0) {
                    htmlpath = getHtmlPath(getNpcId(), 1, player);
                } else if (classId != MY_CLASS_ID) {
                    htmlpath = getHtmlPath(getNpcId(), 2, player);
                } else {
                    htmlpath = getHtmlPath(getNpcId(), 3, player);
                }

                if (player.getVarB("AwakenPrepared", false)) {
                    player.setVar("AwakenedID", MY_CLASS_ID, -1);
                    player.sendPacket(new ExChangeToAwakenedClass(MY_CLASS_ID));
                    return;
                }
            } else {
                htmlpath = getHtmlPath(getNpcId(), val, player);
            }
        } else
            htmlpath = getHtmlPath(getNpcId(), val, player);
        showChatWindow(player, htmlpath, replace);
    }
}
