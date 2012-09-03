package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.model.items.PcInventory;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.SkillTable;

public abstract class ForgottenScrolls
    implements IItemHandler, ScriptFile
{

    public ForgottenScrolls()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item)
    {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player player = (L2Player)playable;
        if(player.getTransformation() != 0)
        {
            player.sendMessage("Cannot use this item in transform!");
            return;
        }
        int itemId = item.getItemId();
        int classId = player.getClassId().getId();
        int playerLevel = player.getLevel();
        boolean skillIsAlreadyLearned = false;
        int skillIdToLearn = 0;
        boolean levelIsEnough = false;
        switch(classId)
        {
        case 2: // '\002'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 3: // '\003'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 5: // '\005'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 6: // '\006'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 8: // '\b'
            switch(itemId)
            {
            case 12770: 
                skillIdToLearn = 820;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 9: // '\t'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 12: // '\f'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 13: // '\r'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 14: // '\016'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 16: // '\020'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 17: // '\021'
            switch(itemId)
            {
            case 10579: 
                skillIdToLearn = 1499;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10581: 
                skillIdToLearn = 1501;
                levelIsEnough = playerLevel >= 70;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 20: // '\024'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 21: // '\025'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 23: // '\027'
            switch(itemId)
            {
            case 12769: 
                skillIdToLearn = 819;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 24: // '\030'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 27: // '\033'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 28: // '\034'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 30: // '\036'
            switch(itemId)
            {
            case 10583: 
                skillIdToLearn = 1503;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10584: 
                skillIdToLearn = 1504;
                levelIsEnough = playerLevel >= 70;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 33: // '!'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 34: // '"'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 36: // '$'
            switch(itemId)
            {
            case 12768: 
                skillIdToLearn = 818;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 37: // '%'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 40: // '('
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 41: // ')'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 43: // '+'
            switch(itemId)
            {
            case 10580: 
                skillIdToLearn = 1500;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10582: 
                skillIdToLearn = 1502;
                levelIsEnough = playerLevel >= 70;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 46: // '.'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 48: // '0'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 51: // '3'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14215: 
                skillIdToLearn = 1536;
                levelIsEnough = playerLevel >= 70;
                break;

            case 14216: 
                skillIdToLearn = 1537;
                levelIsEnough = playerLevel >= 74;
                break;

            case 14217: 
                skillIdToLearn = 1538;
                levelIsEnough = playerLevel >= 72;
                break;
            }
            break;

        case 52: // '4'
            switch(itemId)
            {
            case 10608: 
                skillIdToLearn = 1517;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10609: 
                skillIdToLearn = 1518;
                levelIsEnough = playerLevel >= 72;
                break;

            case 10610: 
                skillIdToLearn = 1519;
                levelIsEnough = playerLevel >= 74;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14214: 
                skillIdToLearn = 1535;
                levelIsEnough = playerLevel >= 72;
                break;
            }
            break;

        case 55: // '7'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 56: // '8'
            switch(itemId)
            {
            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 88: // 'X'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10568: 
                skillIdToLearn = 775;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14203: 
                skillIdToLearn = 775;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14202: 
                skillIdToLearn = 919;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 89: // 'Y'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10567: 
                skillIdToLearn = 774;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14181: 
                skillIdToLearn = 774;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 90: // 'Z'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10554: 
                skillIdToLearn = 760;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10591: 
                skillIdToLearn = 784;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14172: 
                skillIdToLearn = 784;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14200: 
                skillIdToLearn = 912;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 91: // '['
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10554: 
                skillIdToLearn = 760;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10555: 
                skillIdToLearn = 761;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10556: 
                skillIdToLearn = 763;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14170: 
                skillIdToLearn = 761;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14171: 
                skillIdToLearn = 763;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 92: // '\\'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10564: 
                skillIdToLearn = 771;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14178: 
                skillIdToLearn = 771;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14211: 
                skillIdToLearn = 924;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14220: 
                skillIdToLearn = 946;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 93: // ']'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10561: 
                skillIdToLearn = 768;
                levelIsEnough = playerLevel >= 81;
                break;

            case 12770: 
                skillIdToLearn = 820;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14175: 
                skillIdToLearn = 768;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14209: 
                skillIdToLearn = 922;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14210: 
                skillIdToLearn = 923;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14218: 
                skillIdToLearn = 928;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 94: // '^'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10572: 
                skillIdToLearn = 1492;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14187: 
                skillIdToLearn = 1492;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14191: 
                skillIdToLearn = 1467;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 95: // '_'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10575: 
                skillIdToLearn = 1495;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14190: 
                skillIdToLearn = 1495;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14191: 
                skillIdToLearn = 1467;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14224: 
                skillIdToLearn = 1541;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 96: // '`'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10576: 
                skillIdToLearn = 1496;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10577: 
                skillIdToLearn = 1497;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10578: 
                skillIdToLearn = 1498;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14204: 
                skillIdToLearn = 929;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 97: // 'a'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10585: 
                skillIdToLearn = 1505;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14193: 
                skillIdToLearn = 1505;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 98: // 'b'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10579: 
                skillIdToLearn = 1499;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10581: 
                skillIdToLearn = 1501;
                levelIsEnough = playerLevel >= 70;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14225: 
                skillIdToLearn = 1542;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 99: // 'c'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10554: 
                skillIdToLearn = 760;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10592: 
                skillIdToLearn = 786;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14173: 
                skillIdToLearn = 786;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 100: // 'd'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10557: 
                skillIdToLearn = 764;
                levelIsEnough = playerLevel >= 76;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14198: 
                skillIdToLearn = 914;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 101: // 'e'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10562: 
                skillIdToLearn = 769;
                levelIsEnough = playerLevel >= 81;
                break;

            case 12769: 
                skillIdToLearn = 819;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14176: 
                skillIdToLearn = 769;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14209: 
                skillIdToLearn = 922;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14210: 
                skillIdToLearn = 923;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14218: 
                skillIdToLearn = 928;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 102: // 'f'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10565: 
                skillIdToLearn = 772;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14179: 
                skillIdToLearn = 772;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14211: 
                skillIdToLearn = 924;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14220: 
                skillIdToLearn = 946;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 103: // 'g'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10573: 
                skillIdToLearn = 1493;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14188: 
                skillIdToLearn = 1493;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14192: 
                skillIdToLearn = 1468;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 104: // 'h'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10576: 
                skillIdToLearn = 1496;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10577: 
                skillIdToLearn = 1497;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10578: 
                skillIdToLearn = 1498;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14205: 
                skillIdToLearn = 931;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 105: // 'i'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10583: 
                skillIdToLearn = 1503;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10584: 
                skillIdToLearn = 1504;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10586: 
                skillIdToLearn = 1506;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14194: 
                skillIdToLearn = 1506;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 106: // 'j'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10554: 
                skillIdToLearn = 760;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10593: 
                skillIdToLearn = 788;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14174: 
                skillIdToLearn = 788;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 107: // 'k'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10558: 
                skillIdToLearn = 765;
                levelIsEnough = playerLevel >= 76;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14199: 
                skillIdToLearn = 915;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14207: 
                skillIdToLearn = 913;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 108: // 'l'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10563: 
                skillIdToLearn = 770;
                levelIsEnough = playerLevel >= 81;
                break;

            case 12768: 
                skillIdToLearn = 818;
                levelIsEnough = playerLevel >= 74;
                break;

            case 12771: 
                skillIdToLearn = 821;
                levelIsEnough = playerLevel >= 72;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14177: 
                skillIdToLearn = 770;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14209: 
                skillIdToLearn = 922;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14210: 
                skillIdToLearn = 923;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14218: 
                skillIdToLearn = 928;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 109: // 'm'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10566: 
                skillIdToLearn = 773;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14180: 
                skillIdToLearn = 773;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14211: 
                skillIdToLearn = 924;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14220: 
                skillIdToLearn = 946;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 110: // 'n'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10574: 
                skillIdToLearn = 1494;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14189: 
                skillIdToLearn = 1494;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14192: 
                skillIdToLearn = 1468;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 111: // 'o'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10576: 
                skillIdToLearn = 1496;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10577: 
                skillIdToLearn = 1497;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10578: 
                skillIdToLearn = 1498;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14206: 
                skillIdToLearn = 930;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 112: // 'p'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10580: 
                skillIdToLearn = 1500;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10582: 
                skillIdToLearn = 1502;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10587: 
                skillIdToLearn = 1507;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10588: 
                skillIdToLearn = 1508;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14195: 
                skillIdToLearn = 1507;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14196: 
                skillIdToLearn = 1508;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 113: // 'q'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10570: 
                skillIdToLearn = 777;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14183: 
                skillIdToLearn = 777;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 114: // 'r'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10569: 
                skillIdToLearn = 776;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14182: 
                skillIdToLearn = 776;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14201: 
                skillIdToLearn = 918;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 115: // 's'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10589: 
                skillIdToLearn = 1509;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14197: 
                skillIdToLearn = 1509;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14215: 
                skillIdToLearn = 1536;
                levelIsEnough = playerLevel >= 70;
                break;

            case 14216: 
                skillIdToLearn = 1537;
                levelIsEnough = playerLevel >= 74;
                break;

            case 14217: 
                skillIdToLearn = 1538;
                levelIsEnough = playerLevel >= 72;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14226: 
                skillIdToLearn = 949;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 116: // 't'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10608: 
                skillIdToLearn = 1517;
                levelIsEnough = playerLevel >= 70;
                break;

            case 10609: 
                skillIdToLearn = 1518;
                levelIsEnough = playerLevel >= 72;
                break;

            case 10610: 
                skillIdToLearn = 1519;
                levelIsEnough = playerLevel >= 74;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14212: 
                skillIdToLearn = 1532;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14213: 
                skillIdToLearn = 1533;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14214: 
                skillIdToLearn = 1535;
                levelIsEnough = playerLevel >= 72;
                break;

            case 14219: 
                skillIdToLearn = 945;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14221: 
                skillIdToLearn = 1540;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14227: 
                skillIdToLearn = 1543;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 117: // 'u'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14222: 
                skillIdToLearn = 947;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 118: // 'v'
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10571: 
                skillIdToLearn = 778;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14184: 
                skillIdToLearn = 778;
                levelIsEnough = playerLevel >= 83;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;
            }
            break;

        case 127: // '\177'
            switch(itemId)
            {
            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 128: 
            switch(itemId)
            {
            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 129: 
            switch(itemId)
            {
            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 130: 
            switch(itemId)
            {
            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 131: 
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14208: 
                skillIdToLearn = 917;
                levelIsEnough = playerLevel >= 81;
                break;

            case 14223: 
                skillIdToLearn = 948;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 132: 
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10595: 
                skillIdToLearn = 791;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14186: 
                skillIdToLearn = 791;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 133: 
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10560: 
                skillIdToLearn = 767;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10595: 
                skillIdToLearn = 791;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14186: 
                skillIdToLearn = 791;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 134: 
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10552: 
                skillIdToLearn = 758;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10553: 
                skillIdToLearn = 759;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10559: 
                skillIdToLearn = 766;
                levelIsEnough = playerLevel >= 81;
                break;

            case 10594: 
                skillIdToLearn = 790;
                levelIsEnough = playerLevel >= 81;
                break;

            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;

            case 14185: 
                skillIdToLearn = 790;
                levelIsEnough = playerLevel >= 83;
                break;
            }
            break;

        case 135: 
            switch(itemId)
            {
            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;

        case 136: 
            switch(itemId)
            {
            case 10549: 
                skillIdToLearn = 755;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10550: 
                skillIdToLearn = 756;
                levelIsEnough = playerLevel >= 82;
                break;

            case 10551: 
                skillIdToLearn = 757;
                levelIsEnough = playerLevel >= 82;
                break;

            case 13552: 
                skillIdToLearn = 840;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13553: 
                skillIdToLearn = 841;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13554: 
                skillIdToLearn = 842;
                levelIsEnough = playerLevel >= 75;
                break;

            case 13728: 
                skillIdToLearn = 932;
                levelIsEnough = playerLevel >= 75;
                break;
            }
            break;
        }
        if(skillIdToLearn == 0)
        {
            player.sendMessage("Spellbook is not for your class!");
            SystemMessage sm = new SystemMessage(113);
            sm.addItemName(Integer.valueOf(itemId));
            player.sendPacket(new L2GameServerPacket[] {
                sm
            });
            return;
        }
        skillIsAlreadyLearned = player.getSkillLevel(Integer.valueOf(skillIdToLearn)) > 0;
        L2Skill skill = SkillTable.getInstance().getInfo(skillIdToLearn, 1);
        if(skillIsAlreadyLearned)
        {
            player.sendMessage((new StringBuilder()).append("Skill ").append(skill.getName()).append(" is already learned!").toString());
            SystemMessage sm = new SystemMessage(113);
            sm.addItemName(Integer.valueOf(itemId));
            player.sendPacket(new L2GameServerPacket[] {
                sm
            });
        } else
        if(!skillIsAlreadyLearned && !levelIsEnough)
        {
            player.sendMessage((new StringBuilder()).append("Not enough level to learn skill ").append(skill.getName()).append("!").toString());
            SystemMessage sm = new SystemMessage(113);
            sm.addItemName(Integer.valueOf(itemId));
            player.sendPacket(new L2GameServerPacket[] {
                sm
            });
        } else
        if(!skillIsAlreadyLearned && levelIsEnough)
        {
            player.addSkill(skill, true);
            player.sendPacket(new L2GameServerPacket[] {
                new SkillList(player)
            });
            SystemMessage sm = new SystemMessage(54);
            sm.addSkillName(skill.getId(), skill.getLevel());
            player.sendPacket(new L2GameServerPacket[] {
                sm
            });
            PcInventory inv = player.getInventory();
            inv.destroyItem(item, 1L, false);
        }
    }

    public final int[] getItemIds()
    {
        return _itemIds;
    }

    public void onLoad()
    {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    private static final int _itemIds[] = {
        10549, 10550, 10551, 10552, 10553, 10554, 10555, 10556, 10557, 10558, 
        10559, 10560, 10561, 10562, 10563, 10564, 10565, 10566, 10567, 10568, 
        10569, 10570, 10571, 10572, 10573, 10574, 10575, 10576, 10577, 10578, 
        10579, 10580, 10581, 10582, 10583, 10584, 10585, 10586, 10587, 10588, 
        10589, 10591, 10592, 10593, 10594, 10595, 12768, 12769, 12770, 12771, 
        13552, 13553, 13554, 13728, 14170, 14171, 14172, 14173, 14174, 14175, 
        14176, 14177, 14178, 14179, 14180, 14181, 14182, 14183, 14184, 14185, 
        14186, 14187, 14188, 14189, 14190, 14191, 14192, 14193, 14194, 14195, 
        14196, 14197, 14198, 14199, 14200, 14201, 14202, 14203, 14204, 14205, 
        14206, 14207, 14208, 14209, 14210, 14211, 14212, 14213, 14214, 14215, 
        14216, 14217, 14218, 14219, 14220, 14221, 14222, 14223, 14224, 14225, 
        14226, 14227, 10608, 10609, 10610
    };

}
