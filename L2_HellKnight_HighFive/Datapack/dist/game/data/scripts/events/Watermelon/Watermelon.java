/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.Watermelon;

import java.util.Arrays;
import java.util.Comparator;

import javolution.util.FastMap;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2ChronoMonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.model.items.L2Weapon;
import l2.hellknight.util.Rnd;

/**
 * @author JOJO
 *
 * Original: "SquashEvent" Gnacik version 1.0
 * Update By pmq 04-09-2010
 */
public class Watermelon extends Quest
{
	private static final String qn = "Watermelon";
/**
 * ���ʰ���� ������ Npc ID (31227) Original: ID (32727) For Freya New Npc
 */
	private static final int MANAGER = 31227;
/**
 * ���s Skill ID (2005)
 */
	private static final int NECTAR_SKILL = 2005;

	private static final long DESPAWN_FIRST = 180000;
	private static final long DESPAWN_NEXT = 90000;

	private static final int DAMAGE_MAX = 12;
	private static final int DAMAGE_DEFAULT = 5;
/**
 * �쳹�����־� �J�|�ջ����   Item ID (4202) * �L�������־� �J�|�չa��   Item ID (5133)
 * �ѳ������־� �J�|�է���     Item ID (5817) * �v�������־� �J�|�չF���d Item ID (7058)
 * ������־� �J�|�պ��ԥd�� Item ID (8350)
 */
	private static final int[] CHRONO_LIST =
	{
		4202,5133,5817,7058,8350
	};
/**
 * �����������     Mob ID (13271) * �u�}�����       Mob ID (13273) * ���}�����     Mob ID (13272)
 * ��������������� Mob ID (13275) * �u�}���������   Mob ID (13277) * ���}��������� Mob ID (13276)
 * �u�}����ʤ�     Mob ID (13274) * �u�}��������ʤ� Mob ID (13278)
 */
	private static final int[] WATERMELON_LIST =
	{
		13271,13273,13272,
		13275,13277,13276,
		13274,13278
	};

/*
1800903	�x~�I�o�O���̰�~ �O����s�ڪ��r~�H
1800904	�K��~�I��ʵn����~�I
1800905	>_<...�s�ڤF��...�H
1800906	��ʵn���F~�I�{�b�}�l�|�v���������I
1800907	����A�o�O�h�[�S�������H�ڡH
1800908	���ߵo�]�I�L�ѳ̴Ϊ����ݩ��ʧr~�I
1800909	�s�ڰ�~�H�I�I�I�Q���|������n�F���~�H
1800910	�Q�ݧڬ��R�����A��~�H
1800911	�I�I�I�I�ڭ̤@�_�ӧa~�I
1800912	�n�n������ܡA�|�O�Ӥj���~ �_�h�|�O�Ӥj���~�I
1800913	�K��~ ��~ ��~ �F~�I
1800914	�z�q�ڬO�u�}��ʡH�٬O���}��ʡH
1800915	���I���j�o~�I�����񰨹L�ӡI
1800916	�n�ܤj~ �٬O�n�ܱj~�H�I���X�z���@��a~~�I
1800917	�������ʡI...���L�A�w�g�����F�C�H
1800918	���ߤj��ʡI...�n�n���}�ݬݧa�I
1800919	�w�g�����F~�I�ڭn�k���o~^^
1800920	��ڨ����ܡA�ڷ|���z�@�d�U�������I
1800921	����~�I�zı�o�ڸ̭��˵ۤ���H�H
1800922	�ܦn~ �ܦn~ ���o~ �ܦn�C���U�Ӫ��D�n������F�a�H
1800923	�ޡI���I�߰��I���y�X�ӤF���I���M��o��öQ��...
1800924	��~ �u�ΪA�I�A~����~�I
1800925	�B�P�B�P~ �ܦn�I�٦��S���ڡH
1800926	�˷ǳ����|�ܡH��L�L�S����~�H
1800927	�o�O���O�U����~�H���D���|�o�˩O~�H
1800928	���I�ܦn~ �~��A�u�ΪA~
1800929	�����I���O����~ �O�o�̰�~ �o�̡I���ڤp�A����夣�ǰڡH
1800930	�c�A�A�A�I�o�O����~�H�I�o�T��O���s�ܡH
1800931	�n�n���~�I�u�n�������~���ܡA�N�|�����j��ʤF~�I
1800932	�G�M~�I���s���̦n���G�M�O��ʯ��s�I������~�I
1800933	�r�I�ܦn~ >_< �л����a�祴��~
1800934	�z�I���M�{�b���ڶå��H���گ��s��~
1800935	��~�I�I�o�ˬO�|�Q���a��~�I
1800936	�z�o�O�I�٨S����N�Q�Y�աH�n�a~ �H�K�n�F~ �����گ��s���ܡA�ڴN�|�Q���z��I
1800937	�A���r�I�A���r�I
1800938	�o�ˬO�|�\����~ �ڥi���޳�~�I
1800939	��~ �p�G�ڴN�o��\���F�A���N�o�����_���o�I���s������öQ�ܡH
1800940	�A���A�F�I�I�I�[�o��~�I
1800941	�ϱϧڧa~ �s�@�w���s���S�|�L�N�n����~~�I
1800942	�p�G�ڴN�o�򦺤F�A���N�|�����ʤF~�I
1800943	�}�l���믫�F~�H�I�A��30��A�N�i�H�}�ȤF~ �I�I�I
1800944	20���A�N�n�������F~�I
1800945	�����A�N��10��F~�I9...8...7...�I
1800946	�n�O�������s���ܡA2������N�|�]���o~�I
1800947	�n�O�������s���ܡA1������N�������o~�I
1800948	�ڭn���F~�I����A�O����~�I
1800949	�ܥi���A���ڳo�Ӥj��ʴN���i��F~�I
1800950	���ɤw��~�I�j�a�n�n���a~�I
1800951	��������~ �����F�C���_�Ҧ��Z���A�D�ԤU�Ӿ��|�a~�I
1800952	�u�i��~�I���W����F�A��~�IT.T
1800953	��~ �n��������H�I
1800954	�־������A���S���q���C�C�n�ڨӰۤ@���ܡH
1800955	�u�Ϊ����֡I
1800956	�Pı�u�n~ �~��V�V�ݡI
1800957	�J�|�ժ��۫߯u�O�H�ɤߡI
1800958	�n�n�t�t��~�I���䨺�Ӥj��~ �z���F�I
1800959	�D�`�n~�I
1800960	�ڰ�~~ ���l�N�n���}�F�I
1800961	�@�A�o�X���I�u�O���b�F�I�A�V�V�ݡI
1800962	�N�O�o�ӡI�o�N�O�ڭn���۫ߡI�z�Q���Q��q�P�ڡH
1800963	�A���V�o���N��A�]�o����n�F�誺��~�I
1800964	�ܵh�C�I�D�A�u�μ־��ӺV���I
1800965	�u���n���֤~�യ�}�ڪ����l�I
1800966	�o�Ӥ���A���O�����ӶܡH�N�O���ӷ�C�@�������~���A�N�Ψ��ӺV�a�I
1800967	����~ �S�����֡H�n�L���...�ڭn���o~
1800968	���O�o�ب�ժ�����~�I�ά������۫߰աI
1800969	�o�Ӥj��ʥu��έ��֤~���o�}~ �ΪZ���O���檺~�I
1800970	�μ־��V��~�I�N��A�����O�o�ӡA�μ־��V��~�I
1800971	�ܦ���O�@�I���o�����O�նO�u��~�I
1800972	���O�쳥�~���y�Ǫ��ɨϥΪ��ܡH�I�ڭn���O�J�|��~�I
1800973	�U��A��ʭn�z�}�աI�I�I
1800974	�ڬݱz�O�|�������ʤF~�I
1800975	�q�̭����ӥu�|�z�X�ǧ��ǧa�]�H�^�I
1800976	�j��ʡI�I���ߵo�]�I�I
1800977	�O��ڪ����T�y�ǩ�~~
1800978	�c��~ �����⪺���b�y�O�I
1800979	�o�ǬO�����ܡH
1800980	�c�ڰڰڡI�I�I�u���ڮ�...�I�I
1800981	�U��~ �o�̪���ʭn�z�}�աI�I���_�����U�Ӯ@�I
1800982	�u��I�z�}�աI�̭����F��M�ռM��~
1800983	������~ �V�n�@�I�աI
1800984	�z�I�I�z�u�O�@��㦳��O���H��~�H�I
1800985	�~��V�ڡI�~��I�~��I�~��I
1800986	�ڬO�n�������~�ন�����I
1800987	�[�o��~ �S�ɶ��F~
1800988	�~�o���I�O�N�H���ڷ|�z�}��~�H
1800989	����������~ �ٺ�ॴ�����a��~
1800990	���̨��̡I�k��@�I~�I��~ �n�ΪA�C
1800991	���]���O�V���ܡH��ӧ󦳹�O���B�ͨӧa�I
1800992	���Υh�Q�I�u�ޥ��I���a�I
1800993	�ݭn���s~ ��ʯ��s�I
1800994	�u���ܯ��s�A�ڤ~����j�@~
1800995	�ӡA���֮���ݬݧa�I�i�o�n�N�ܤj��ʡA�_�h�N������~�I
1800996	���ڤ@�I���s�a~ �{�l�n�j~
1800997	�л��֮����s���ڧa...�I�Ӥ��O��s~...�]��p�I�^
1800998	�Y�����s�Ӫ��ܡA�ܤF����A�ڴN���z�ֳt�����I
1800999	�o�˪��p��ʤ]�n�Y�H�����I���s�a�A�ڥi�H����j��~�I
1801000	�H�H�H�H�A�i�n�F�N�|�o�]~ �i���n���ܡ]�H�^�]�����ڪ��Ƴ�~
1801001	�Q�n�j��ʶܡH�i�ڳ��w���O����~
1801002	�۫H�ڡA���ޱN���s��W�I�I�ڷ|���z�o�j�]��~�I
*/
	private static final String[] SPAWN_TEXT =
	{
		"����A�o�O�h�[�S�������H�ڡH",
		"���ߵo�]�I�L�ѳ̴Ϊ����ݩ��ʧr~�I",
		"�Q�ݧڬ��R�����A��~�H",
		"�x~�I�o�O���̰�~�O����s�ڪ��r~�H",
		"�K��~�I��ʵn����~�I"
	};
	private static final String[] GROWUP_TEXT =
	{
		"�n�ܤj~�٬O�n�ܱj~�H�I���X�z���@��a~~�I",
		"�������ʡI...���L�A�w�g�����F�C�H",
		"���I���j�o~�I�����񰨹L�ӡI",
		"�K��~��~��~�F~�I",
		"�ܦn~�ܦn~���o~�ܦn�C���U�Ӫ��D�n������F�a�H",
		"�w�g�����F~�I�ڭn�k���o~^^"
	};
	// ���z��Ī�����
	private static final String[] KILL_TEXT =
	{
		"�c��~�����⪺���b�y�O�I",
		"�o�ǬO�����ܡH",
		"�u��I�z�}�աI�̭����F��M�ռM��~",
		"�U��~�o�̪���ʭn�z�}�աI�I���_�����U�Ӯ@�I",
		"�U��A��ʭn�z�}�աI�I�I",
		"�O��ڪ����T�y�ǩ�~~",
		"�c�ڰڰڡI�I�I�u���ڮ�...�I�I",
		"���ߵo�]�I",
		"���۳o�ӵ��ںu�J~",
		"�A�h�����ܤ^���a�I"
	};
	// �S�Ϋ��w�Z�����Ǫ����
	private static final String[] NOCHRONO_TEXT =
	{
		"�o�Ӥj��ʥu��έ��֤~���o�}~�ΪZ���O���檺~�I",
		"���O�o�ب�ժ�����~�I�ά������۫߰աI",
		"�A���r�I�A���r�I",
		"���r�H���ڡH���ڬO�ܡH",
		"�޳ޡA���F�a�H�o�˷|�\�@�H",
		"�o�˷|�\��~�ڤ����o~",
		"�A�N�n�n~���նO�u�ҧa�I",
		"����I���M�{�b���ڶå��H���O�n�A�寫�s�ܡH",
		"�ޡA�ڳo�˦��F�A�����_�����S���F�@�H���s������öQ�ܡH",
		"�o�˦��F�N�ܦ��^����~",
		"���i�ڤϦӷQ�Y���ڬO�ܡH�n�A�H�A�K~�������s�N�����A�ݡI"
	};
	// �Ϋ��w�Z�����Ǫ����
	private static final String[] CHRONO_TEXT =
	{
		"�n�n�t�t��~�I���䨺�Ӥj��~�z���F�I",
		"�N�O�o�ӡI�o�N�O�ڭn���۫ߡI�z�Q���Q��q�P�ڡH",
		"�Pı�u�n~�~��V�V�ݡI",
		"�J�|�ժ��۫߯u�O�H�ɤߡI",
		"�@�A�o�X���I�u�O���b�F�I�A�V�V�ݡI",
		"�ڰ�~~���l�N�n���}�F�I",
		"�D�`�n~�I",
		"��~�n��������H�I",
		"�u�Ϊ����֡I",
		"�־������A���S���q���C�C�n�ڨӰۤ@���ܡH",
		"���̨��̡I�k�@�I~�I��~�n�ΪA�C",
		"�z�I�I�z�u�O�@��㦳��O���H��~�H�I",
		"���]���O�V���ܡH��ӧ󦳹�O���B�ͨӧa�I",
		"�ڬO�n�������~�ন�����I",
		"�[�o��~�S�ɶ��F~",
		"�~��V�ڡI�~��I�~��I�~��I",
		"���Υh�Q�I�u�ޥ��I���a�I"
	};
	// �ί��s������Ī�����
	private static final String[] NECTAR_TEXT =
	{
		"���I�ܦn~�~��A�u�ΪA~",
		"�c�A�A�A�I�o�O����~�H�I�o�T��O���s�ܡH",
		"�ޡI���I�߰��I���y�X�ӤF���I���M��o��öQ��...",
		"�۫H�ڡA���ޱN���s��W�I�I�ڷ|���z�o�j�]��~�I",
		"�ӡA���֮���ݬݧa�I�i�o�n�N�ܤj��ʡA�_�h�N������~�I",
		"�˷ǳ����|�ܡH��L�L�S���ڡH",
		"�B�P�B�P~�I�ܦn�I�٦��S���ڡH",
		"�n�n���~�I�u�n�������~���ܡA�N�|�����j��ʤF~�I",
		"�����I���O����~�O�o�̰�~�o�̡I���ڤp�A����夣�ǰڡH",
		"�ݭn���s~��ʯ��s�I",
		"��~�u�ΪA�I�A~����~�I",
		"�o�˪��p��ʤ]�n�Y�H�����I���s�a�A�ڥi�H����j��~�I",
		"�H�H�H�H�A,�i�n�F�N�|�o�]~�i���n�����Y�]�H�^�]�����ڪ��Ƴ�~",
		"�G�M~�I���s���̦n���G�M�O��ʯ��s�I������~�I",
		"�ܦn~�ܦn~���o~�ܦn�C���U�Ӫ��D�n������F�a�H",
		"�л��֮����s���ڧa...�I�Ӥ��O��s~...�]��p�I�^",
		"�o�O���O�U����~�H���D���|�o�˩O~�H",
		"�n�n������ܡA�|�ҭӤj���~�_�h�|�O�Ӥj���~�I",
		"�Q�n�j��ʶܡH�i�ڳ��w���O����~",
		"���ڤ@�I���s�a~�{�l�n�j~"
	};
	// ���~ �i ID �Ǫ� , NO ���v , ID ���~, NO �ƶq �j
	private static final int[][] DROPLIST =
	{
		/**
		 * must be sorted by npcId !
		 * npcId, chance, itemId,qty [,itemId,qty...]
		 *
		 * Young Watermelon
		 * �����������
		 */
		{ 13271,100,  6391,2 },		// Nectar
		/**
		 * Defective Watermelon
		 * ���}�����
		 */
		{ 13272,100,  6391,10 },	// Nectar
		/**
		 * Rain Watermelon
		 * �u�}�����
		 */
		{ 13273,100,  6391,30 },	// Nectar
		/**
		 * Large Rain Watermelon
		 * �u�}����ʤ�
		 */
		{ 13274,100,  6391,50 },	// Nectar
		/**
		 * Young Honey Watermelon
		 * ���������������
		 */
		{ 13275,100, 14701,2,		// �W�j�O������O�v¡�Ĥ�
		             14700,2 },		// �W�j�O��O�v¡�Ĥ�
		/**
		 * Defective Honey Watermelon
		 * ���}���������
		 */
		{ 13276, 50,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2 },		// ���֪�����j�ƨ��b-A��
		{ 13276, 30,  6622,1 },		// ���H���g��
		{ 13276, 10,  8750,1 },		// ���ťͩR��-67��
		{ 13276, 10,  8751,1 },		// ���ťͩR��-70��
		{ 13276, 99, 14701,4,		// �W�j�O������O�v¡�Ĥ�
		             14700,4 },		// �W�j�O��O�v¡�Ĥ�
		{ 13276, 50,  1461,4 },		// ����-A��
		{ 13276, 30,  1462,3 },		// ����-S��
		{ 13276, 50,  2133,4 },		// �_��-A��
		{ 13276, 30,  2134,3 },		// �_��-S��
		/**
		 * Rain Honey Watermelon
		 * �u�}���������
		 */
		{ 13277,  7,  9570,1,		// ����j���-���q14
		              9571,1,		// �Ŧ�j���-���q14
		              9572,1,		// ���j���-���q14
		             10480,1,		// ����j���-���q15
		             10481,1,		// �Ŧ�j���-���q15
		             10482,1,		// ���j���-���q15
		             13071,1,		// ����j���-���q16
		             13072,1,		// �Ŧ�j���-���q16
		             13073,1 },		// ���j���-���q16
		{ 13277, 35,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		               959,3,		// �Z���j�ƨ��b-S��
		               960,3,		// ����j�ƨ��b-S��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2,		// ���֪�����j�ƨ��b-A��
		              6577,1,		// ���֪��Z���j�ƨ��b-S��
		              6578,1 },		// ���֪�����j�ƨ��b-S��
		{ 13277, 28,  6622,3,		// ���H���g��
		              9625,2,		// ���H���g��-��ѽg
		              9626,2,		// ���H���g��-�V�m�g
		              9627,2 },		// ���H���g��-���m�g
		{ 13277, 14,  8750,10 },	// ���ťͩR��-67��
		{ 13277, 14,  8751,8 },		// ���ťͩR��-70��
		{ 13277, 14,  8752,6 },		// ���ťͩR��-76��
		{ 13277, 14,  9575,4 },		// ���ťͩR��-80��
		{ 13277, 14, 10485,2 },		// ���ťͩR��-82��
		{ 13277, 14, 14168,1 },		// ���ťͩR��-84��
		{ 13277, 21,  8760,1,		// �S�ťͩR��-67��
		              8761,1,		// �S�ťͩR��-70��
		              8762,1,		// �S�ťͩR��-76��
		              9576,1,		// �S�ťͩR��-80��
		             10486,1,		// �S�ťͩR��-82��
		             14169,1 },		// �S�ťͩR��-84��
		{ 13277, 21, 14683,1,		// �W�j�O�ͩR�F��-D��
		             14684,1,		// �W�j�O�ͩR�F��-C��
		             14685,1,		// �W�j�O�ͩR�F��-B��
		             14686,1,		// �W�j�O�ͩR�F��-A��
		             14687,1,		// �W�j�O�ͩR�F��-S��
		             14689,1,		// �W�j�O�믫�F��-D��
		             14690,1,		// �W�j�O�믫�F��-C��
		             14691,1,		// �W�j�O�믫�F��-B��
		             14692,1,		// �W�j�O�믫�F��-A��
		             14693,1,		// �W�j�O�믫�F��-S��
		             14695,1,		// �W�j�O�����F��-D��
		             14696,1,		// �W�j�O�����F��-C��
		             14697,1,		// �W�j�O�����F��-B��
		             14698,1,		// �W�j�O�����F��-A��
		             14699,1 },		// �W�j�O�����F��-S��
		{ 13277, 99, 14701,9,		// �W�j�O������O�v¡�Ĥ�
		             14700,9 },		// �W�j�O��O�v¡�Ĥ�
		{ 13277, 63,  1461,8 },		// ����-A��
		{ 13277, 49,  1462,5 },		// ����-S��
		{ 13277, 63,  2133,6 },		// �_��-A��
		{ 13277, 49,  2134,4 },		// �_��-S��
		/**
		 * Large Rain Honey Watermelon
		 * �u�}��������ʤ�
		 */
		{ 13278, 10,  9570,1,		// ����j���-���q14
		              9571,1,		// �Ŧ�j���-���q14
		              9572,1,		// ���j���-���q14
		             10480,1,		// ����j���-���q15
		             10481,1,		// �Ŧ�j���-���q15
		             10482,1,		// ���j���-���q15
		             13071,1,		// ����j���-���q16
		             13072,1,		// �Ŧ�j���-���q16
		             13073,1 },		// ���j���-���q16
		{ 13278, 50,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		               959,3,		// �Z���j�ƨ��b-S��
		               960,3,		// ����j�ƨ��b-S��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2,		// ���֪�����j�ƨ��b-A��
		              6577,1,		// ���֪��Z���j�ƨ��b-S��
		              6578,1 },		// ���֪�����j�ƨ��b-S��
		{ 13278, 40,  6622,3,		// ���H���g��
		              9625,2,		// ���H���g��-��ѽg
		              9626,2,		// ���H���g��-�V�m�g
		              9627,2 },		// ���H���g��-���m�g
		{ 13278, 20,  8750,10 },	// ���ťͩR��-67��
		{ 13278, 20,  8751,8 },		// ���ťͩR��-70��
		{ 13278, 20,  8752,6 },		// ���ťͩR��-76��
		{ 13278, 20,  9575,4 },		// ���ťͩR��-80��
		{ 13278, 20, 10485,2 },		// ���ťͩR��-82��
		{ 13278, 20, 14168,1 },		// ���ťͩR��-84��
		{ 13278, 30,  8760,1,		// �S�ťͩR��-67��
		              8761,1,		// �S�ťͩR��-70��
		              8762,1,		// �S�ťͩR��-76��
		              9576,1,		// �S�ťͩR��-80��80
		             10486,1,		// �S�ťͩR��-82��
		             14169,1 },		// �S�ťͩR��-84��
		{ 13278, 30, 14683,1,		// �W�j�O�ͩR�F��-D��
		             14684,1,		// �W�j�O�ͩR�F��-C��
		             14685,1,		// �W�j�O�ͩR�F��-B��
		             14686,1,		// �W�j�O�ͩR�F��-A��
		             14687,1,		// �W�j�O�ͩR�F��-S��
		             14689,1,		// �W�j�O�믫�F��-D��
		             14690,1,		// �W�j�O�믫�F��-C��
		             14691,1,		// �W�j�O�믫�F��-B��
		             14692,1,		// �W�j�O�믫�F��-A��
		             14693,1,		// �W�j�O�믫�F��-S��
		             14695,1,		// �W�j�O�����F��-D��
		             14696,1,		// �W�j�O�����F��-C��
		             14697,1,		// �W�j�O�����F��-B��
		             14698,1,		// �W�j�O�����F��-A��
		             14699,1 },		// �W�j�O�����F��-S��
		{ 13278, 99, 14701,12,		// �W�j�O������O�v¡�Ĥ�
		             14700,12 },	// �W�j�O��O�v¡�Ĥ�
		{ 13278, 90,  1461,8 },		// ����-A��
		{ 13278, 70,  1462,5 },		// ����-S��
		{ 13278, 90,  2133,6 },		// �_��-A��
		{ 13278, 70,  2134,4 },		// �_��-S��
	};

	private int _numAtk = 0;
	private int w_nectar = 0;
	
	class TheInstance
	{
		int nectar;
		//int numatk;
		//int tmpatk;
		long despawnTime;
	}
	FastMap<L2ChronoMonsterInstance, TheInstance> _monsterInstances = new FastMap<L2ChronoMonsterInstance, TheInstance>().shared();
	private TheInstance create(L2ChronoMonsterInstance mob)
	{
		TheInstance mons = new TheInstance();
		_monsterInstances.put(mob, mons);
		return mons;
	}
	private TheInstance get(L2ChronoMonsterInstance mob)
	{
		return _monsterInstances.get(mob);
	}
	private void remove(L2ChronoMonsterInstance mob)
	{
		cancelQuestTimer("countdown", mob, null);
		cancelQuestTimer("despawn", mob, null);
		_monsterInstances.remove(mob);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event == "countdown")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			final TheInstance self = get(mob);
			int timeLeft = (int)((self.despawnTime - System.currentTimeMillis()) / 1000);
			if (timeLeft == 30)
				autoChat(mob, "�}�l���믫�F~�H�I�A��30��A�N�i�H�}�ȤF~�I�I�I");
			else if (timeLeft == 20)
				autoChat(mob, "�}�l���믫�F~�H�I�A��20��A�N�i�H�}�ȤF~�I�I�I");
			else if (timeLeft == 10)
				autoChat(mob, "�@�A10��ɶ��I 9 ... 8 ... 7 ...");
			else if (timeLeft == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "�ޡA�ڳo�˦��F�A�����_�����S���F�@�H���s������öQ�ܡH");
				else
					autoChat(mob, "�D�H�ϱϧڧa~�s�@�w���s���S�|�L�N�n����~");
			}
			else if ((timeLeft % 60) == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "�n�O�������s����" + timeLeft / 60 + "������N�|�]���o~");
			}
		}
		else if (event == "despawn")
		{
			remove((L2ChronoMonsterInstance)npc);
			npc.deleteMe();
		}
		else if (event == "sound")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
		}
		return "";
		//return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		L2Weapon weapon;
		final boolean isChronoAttack
			= !isPet
			&& (weapon = attacker.getActiveWeaponItem()) != null && contains(CHRONO_LIST, weapon.getItemId());
		switch (mob.getNpcId())
		{
			case 13271:
			case 13273:
			case 13272:
			case 13274:
				if (isChronoAttack)
				{
					chronoText(mob);
				}
				else
				{
					noChronoText(mob);
				}
				break;
			case 13275:
			case 13277:
			case 13276:
			case 13278:
				if (isChronoAttack)
				{
					mob.setIsInvul(false);
					if (damage == 0)
						mob.getStatus().reduceHp(DAMAGE_DEFAULT, attacker);
					else if (damage > DAMAGE_MAX)
						mob.getStatus().setCurrentHp(mob.getStatus().getCurrentHp() + damage - DAMAGE_MAX);
					chronoText(mob);
				}
				else
				{
					mob.setIsInvul(true);
					mob.setCurrentHp(mob.getMaxHp());
					noChronoText(mob);
				}
				break;
			default:
				throw new RuntimeException();
		}
		mob.getStatus().stopHpMpRegeneration();
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getId() == NECTAR_SKILL && targets[0] == npc)
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			switch(mob.getNpcId())
			{
				case 13271:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(13273, 13273, 13274, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(13272, 13272, 13272, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(13272, 13273, 13274, mob);
					break;
				case 13275:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(13277, 13277, 13278, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(13276, 13276, 13276, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(13276, 13277, 13278, mob);
					break;
				case 13273:
					npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13274, mob);
					break;
				case 13277:
					npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13278, mob);
					break;
				case 13272:
				case 13276:
					autoChat(mob, "�����j�F�I���N�Ө��H�o~");
					break;
			}
		}
		return null;
		//return super.onSkillSee(npc,caster,skill,targets,isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		remove(mob);
		autoChat(mob, KILL_TEXT[Rnd.get(KILL_TEXT.length)]);
		dropItem(mob, killer);
		w_nectar = 0;
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		assert npc instanceof L2ChronoMonsterInstance;
		
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		mob.setOnKillDelay(1500);	//Default 5000ms.
		final TheInstance self = create(mob);
		switch(mob.getNpcId())
		{
			case 13271:
			case 13275:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_FIRST, mob, null);
				self.nectar = 0;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_FIRST;
				autoChat(mob, SPAWN_TEXT[Rnd.get(SPAWN_TEXT.length)]);
				break;
			case 13272:
			case 13273:
			case 13274:
			case 13276:
			case 13277:
			case 13278:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_NEXT, mob, null);
				startQuestTimer("sound",100, mob, null);
				self.nectar = 5;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_NEXT;
				autoChat(mob, GROWUP_TEXT[Rnd.get(GROWUP_TEXT.length)]);
				break;
			default:
				throw new RuntimeException();
		}
		return super.onSpawn(npc);
	}

	static {
		Arrays.sort(DROPLIST, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) { return a[0] - b[0]; }
		});
	}
	private static final void dropItem(L2ChronoMonsterInstance mob, L2PcInstance player)
	{
		final int npcId = mob.getNpcId();
		for (int[] drop : DROPLIST)
		{
			/**
			 * npcId   = drop[0]
			 * chance  = drop[1]
			 * itemId  = drop[2,4,6,8...]
			 * itemQty = drop[3,5,7,9...]
			 */
			if (npcId == drop[0])
			{
				final int chance = Rnd.get(100);
				if (chance < drop[1])
				{
					int i = 2 + 2 * Rnd.get((drop.length - 2) / 2);
					int itemId = drop[i + 0];
					int itemQty = drop[i + 1];
					if (itemQty > 1) itemQty = Rnd.get(1, itemQty);
					mob.dropItem(mob.getOwner(), itemId, itemQty);
					continue;
				}
			}
			if (npcId < drop[0])
				return; // not found
		}
	}

	private void randomSpawn(int bad, int good, int king, L2ChronoMonsterInstance mob)
	{
		//final TheInstance self = get(mob);
		if (w_nectar >= 5)
		{
			w_nectar = 0;
			int _random = Rnd.get(100);
			if ((_random -= 10) < 0)		// 10% 
				spawnNext(king, mob);
			else if ((_random -= 40) < 0)	// 40% 
				spawnNext(good, mob);
			else							// 50% 
				spawnNext(bad, mob);
		}
		else
		{
			nectarText(mob);
		}
	}

	private void randomSpawn(int king, L2ChronoMonsterInstance mob)
	{
		final TheInstance self = get(mob);
		if (++self.nectar > 5 && self.nectar <= 15 && Rnd.get(100) < 10)	// 10% 
			spawnNext(king, mob);
		else
			nectarText(mob);
	}

	private void autoChat(L2ChronoMonsterInstance mob, String text)
	{
		mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.ALL, mob.getName(), text));
	}
	private void chronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, CHRONO_TEXT[Rnd.get(CHRONO_TEXT.length)]);
	}
	private void noChronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, NOCHRONO_TEXT[Rnd.get(NOCHRONO_TEXT.length)]);
	}
	private void nectarText(L2ChronoMonsterInstance mob)
	{
	/*	if (Rnd.get(100) < 30)	*/
		autoChat(mob, NECTAR_TEXT[Rnd.get(NECTAR_TEXT.length)]);
	}

	private void spawnNext(int npcId, L2ChronoMonsterInstance oldMob)
	{
		remove(oldMob);
		L2ChronoMonsterInstance newMob = (L2ChronoMonsterInstance)addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0);
		newMob.setOwner(oldMob.getOwner());
		newMob.setTitle(oldMob.getTitle());
		oldMob.deleteMe();
	}

	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public Watermelon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : WATERMELON_LIST)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpawnId(mob);
			addSkillSeeId(mob);
		}

		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
		
		//addSpawn(MANAGER, 83063, 148843, -3477, 32219, false, 0);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		//String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		switch (npc.getNpcId())
		{
			case MANAGER: return "31227.htm";
		}
		throw new RuntimeException();
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		// 31227-1.htm
		return event;
	}

	public static void main(String[] args)
	{
		new Watermelon(-1, qn, "events");
	}
}