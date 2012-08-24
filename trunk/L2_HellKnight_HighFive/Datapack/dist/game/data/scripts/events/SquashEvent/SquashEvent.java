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
package events.SquashEvent;

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
 * @author Gnacik
 * @version 1.0
 * Update By pmq 04-09-2010
 */
public class SquashEvent extends Quest
{
	private static final String qn = "SquashEvent";
/**
 * ���ʰ���� ������ Npc ID (31255)
 */
	private static final int MANAGER = 31255;
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
 * ����������Ī   Mob ID (12774) * �u�}����Ī     Mob ID (12775) * ���}����Ī   Mob ID (12776)
 * ���������j��Ī Mob ID (12777) * �u�}���j��Ī   Mob ID (12778) * ���}���j��Ī Mob ID (12779)
 * �u�}����Ī��   Mob ID (13016) * �u�}���j��Ī�� Mob ID (13017)
 */
	private static final int[] SQUASH_LIST =
	{
		12774,12775,12776,
		12777,12778,12779,
		13016,13017
	};

/*
99700	�x�H�o�O����H���^�ơH�O�֥s�ڡH
99701	�K��~�p��Ī�j�ݵn���F�I
99702	�H�H...�A��ڶ�...�H
99703	�p��Ī�n���F�I�{�b�}�l�|�v���������I
99704	����A�o�O�h�[�S�������H�ڡH
99705	���ߵo�]�I
99706	��ڶܡH��ڷF����H�H���|�X�{����ܡH
99707	���۸����I�Q�ݧڪ������ܡH
99708	�c�\�I�X�ӤF�I���N�n�n���ԧڬݬݧa�I
99709	�i�o�n�A���ߵo�]~�i���n�A�ܦ��^��~
99710	�K��~��~��~�I
99711	�A�q�ڬO�u�}�٬O���}���O�H
99712	�r�I�A���j�F�I�񰨹L�ӡI
99713	���۸����I���j�a�ܱj�a�I
99714	�ܦ��a���J�I..�i�O�w�g���j�F�H
99715	���ߵo�]�I�n�n�����}�ݬݡI
99716	�����j�F�I���N�Ө��H�o~
99717	�Y��L�ڴN���A�@�d�U�����I�K�Ǳo���ܡH
99718	�ӡI�Aı�o�ڸ̭��˵ۤ���H
99719	�ܦn�ܦn�A������~ �n�C���{�b���D�Ӱ�����F�a�H
99720	�ޡI���I�߰��I���y�X�ӤF���I
99721	��~�u�ΪA�I�A���ݡI
99722	�B�P�B�P~�ܦn�I�٦��S���ڡH
99723	�˷ǳ����|�ܡH�y�����a���O~
99724	�o�O���O�V�F���ڡH���D��|�o�˩O�H
99725	��~�ܦn�ܦn�C�A�~��A�C�C���n�_�ӤF�C
99726	�ޡI���O����I�O�o�̰աA�o�̡I�O�ݧڤp�A�ҥH�H�K���O�ܡH
99727	��éA�A�I�A�o�O����I�O���s�ܡH
99728	�n�n���I�u�n�����~�N�|�ܦ��j��Ī���I
99729	���s���̦n���G�M�O��Ī���s�I���������I
99730	���r�H���ڡH���ڬO�ܡH
99731	����I���M�{�b���ڶå��H���O�n�A�寫�s�ܡH
99732	�޳ޡA���F�a�H�o�˷|�\�@�H
99733	���i�ڤϦӷQ�Y���ڬO�ܡH�n�A�H�A�K~�������s�N�����A�ݡI
99734	�A���r�I�A���r�I
99735	�o�˷|�\��~�ڤ����o~
99736	�ޡA�ڳo�˦��F�A�����_�����S���F�@�H���s������öQ�ܡH
99737	�A�N�n�n~���նO�u�ҧa�I
99738	�D�H�ϱϧڧa~�s�@�w���s���S�|�L�N�n����~
99739	�o�˦��F�N�ܦ��^����~
99740	�O�q�����W�ɮ@�I�A��30��N�i�H�}�ȤF�I
99741	20���N�P�A�̻��A���F�I
99742	�n�C�A�u��10��F�I 9... 8... 7...!
99743	�n�O�������s���ܡA2������N�|�]���o~
99744	�n�O�������s���ܡA1������N�������o~
99745	�n�C�I�k�]���\�I�o�ǲ³J�A�n�۬����a�I
99746	�U��A��~�ܥi���A�j��Ī�ڭn�����աI
99747	���ɤw��~�ڭn���H�F~�n�n����a~
99748	��������~�����F�C���_�Ҧ��Z���A�D�ԤU�Ӿ��|�a~
99749	�u�i���C~���i�j�F���������]�F�I�n�O�ڡA���N�h����աI�z�������I
99750	�@�A�n��������H
99751	�־������A���S���q���C�C�n�ڨӰۤ@���ܡH
99752	�u�Ϊ����֡I
99753	�Pı�u�n~�A�u�u�ݡI
99754	�J�|�ժ��۫߯u�O�H�ɤߡI
99755	�n�n���t�t���I������F�I
99756	�u�O�R���V�ڡI�D�`�n�I
99757	�ڰ�~~���l�N�n���}�F�I
99758	�@�A�o�X���I�u�O���b�F�I�A�u�u�ݡI
99759	�N�O�o�ӡI�o�N�O�ڭn���۫ߡI�A�A�Q���Q��q�P�ڡH
99760	�A���V�o���N��A�]�o����n�F�誺�աI
99761	�ܵh�C�I�u��μ־��V�աI
99762	�u���n���֤~�യ�}�ڪ����l�I
99763	�o�Ӥ���A���O�����ӶܡH�N�O���ӷ�C�@�������~���A�N�Ψ��ӺV�a�I
99764	����S�����֡H�ڭn���o�H�u���n���F��H
99765	���O�o�ب�ժ������I�ά������۫߰աI
99766	�o�j��Ī�u���έ��֤~�ॴ�o�}~�ΪZ���O���檺�I
99767	�μ־��V�աI�N��A�����O�o�ӡA�μ־��V�աI
99768	�ܦ���O�@�I�������O�նO�u�Ұ�~
99769	�Ψ��K����򤣥h�~�Y���y�Ǫ��O�H�ڭn���O�J�|�աI
99770	��Ī�z�}�աI�I�I
99771	�A�h�����ܦ��^���a�I
99772	�z�X�ǧ��ǧa�I
99773	���ߵo�]�I�I
99774	�O��ڪ����T�y�ǩ�~�I
99775	�ڡA�z�l�y�X�ӤF�I
99776	���۳o�ӵ��ںu�J~
99777	�ذڰڡI�n���j�I���ӯ�k�����K�I
99778	�U��A�o�̪���Ī�z�}�աI���_�����U�Ӯ@�I�z������
99779	�u��I�z�}�աI�̭����F��M�ռM��~
99780	�z�������A�n�n�������ݡI
99781	���r�H�ٯu������l�@�H
99782	�A���r�I�A���I
99783	�ڦ]�A�������Ӧ����I
99784	�A���I�l��~�o�˧ڷ|���H����~
99785	�~�o���I�O�N�H���ڷ|�z�}�ܡH
99786	�����������A�ٺ�ॴ�����a�ǡC
99787	���̨��̡I�k��@�I�I��~�n�ΪA�C
99788	���]���O�����ܡH��Ӧ���O�I���ӧa�I
99789	���Υh�Q�I�u�ޥ��I���a�I
99790	�⯫�s���ӡI��Ī���s�I
99791	�u���ܯ��s�A�ڤ~����j�@~
99792	�ӡA�־i�i�ݧa�I�i�o�n���ߵo�]�A�i���n�ܦ��^���I
99793	���ڤ@�I���s�a~�{�l�n�j~
99794	�ӡA�֥h�����s�ӧa�C
99795	�Y�����s�ӡA�ܤF����ڴN���A�ֳt�������I
99796	�o�˪��p��Ī�]�n�Y�H�����s�ӬݬݡA�ڴN�A���j���A�ݡI
99797	�z�������A�i�n���n�F��~���i���n�O�H�ڤ]�����D~
99798	�Q�n�o�j�]�ܡH���ڳ��w�p�^���C~
99799	�ӡA�۫H�ڡA���ޱN���s��W�I�I�ڷ|���A�o�j�]�աI�I�I
99601	�հհ�~���ѤS�O�Ӵr�֪��ȳ~~�o���h�䤰��F��n�O~
*/
	private static final String[] SPAWN_TEXT =
	{
		"����A�o�O�h�[�S�������H�ڡH",
		"���ߵo�]�I",
		"�Q�ݧڬ��R�����A��~�H",
		"�x~�I�o�O���̰�~�O����s�ڪ��r~�H",
		"�K��~�I��Ī�n����~�I"
	};
	private static final String[] GROWUP_TEXT =
	{
		"�n�ܤj~�٬O�n�ܱj~�H�I���X�z���@��a~~�I",
		"�����긬Ī�I...���L�A�w�g�����F�C�H",
		"���I���j�o~�I�����񰨹L�ӡI",
		"�K��~��~��~�F~�I",
		"�ܦn~�ܦn~���o~�ܦn�C���U�Ӫ��D�n������F�a�H",
		"�w�g�����F~�I�ڭn�k���o~^^"
	};
	// ���z��Ī�����
	private static final String[] KILL_TEXT =
	{
		"�z�X�ǧ��ǧa�I�I�I",
		"���ߡI���ߡI",
		"��Ī�z�}�աI�I�I",
		"�u��I�z�}�աI�̭����F��M�ռM��~",
		"�ڡA�z�l�y�X�ӤF�I",
		"���ߵo�]�I",
		"���۳o�ӵ��ںu�J~",
		"�O��ڪ����T�y�ǩ�~�I",
		"�A�h�����ܤ^���a�I",
		"�ذڰڡI�n���j�I���ӯ�k����...",
		"�U��A�o�̪���Ī�z�}�աI���_�����U�Ӯ@�I�z������"
	};
	// �S�Ϋ��w�Z�����Ǫ����
	private static final String[] NOCHRONO_TEXT =
	{
		"You cannot kill me without Chrono",
		"Hehe...keep trying...",
		"Nice try...",
		"Tired ?",
		"Go go ! haha..."
	};
	// �Ϋ��w�Z�����Ǫ����
	private static final String[] CHRONO_TEXT =
	{
		"Arghh... Chrono weapon...",
		"My end is coming...",
		"Please leave me !",
		"Heeellpppp...",
		"Somebody help me please..."
	};
	// �ί��s������Ī�����
	private static final String[] NECTAR_TEXT =
	{
		"Yummy... Nectar...",
		"Plase give me more...",
		"Hmmm.. More.. I need more...",
		"I will like you more if you give me more...",
		"Hmmmmmmm...",
		"My favourite..."
	};
	// ���~ �i ID �Ǫ� , NO ���v , ID ���~, NO �ƶq �j
	private static final int[][] DROPLIST =
	{
		/**
		 * must be sorted by npcId !
		 * npcId, chance, itemId,qty [,itemId,qty...]
		 *
		 * Young Squash
		 * ����������Ī
		 */
		{ 12774,100,  6391,2 },		// Nectar
		/**
		 * Low Quality Squash
		 * ���}����Ī
		 */
		{ 12776,100,  6391,10 },	// Nectar
		/**
		 * High Quality Squash
		 * �u�}����Ī
		 */
		{ 12775,100,  6391,30 },	// Nectar
		/**
		 * King Squash
		 * �u�}����Ī��
		 */
		{ 13016,100,  6391,50 },	// Nectar
		/**
		 * Large Young Squash
		 * ���������j��Ī
		 */
		{ 12777,100, 14701,2,		// �W�j�O������O�v¡�Ĥ�
		             14700,2 },		// �W�j�O��O�v¡�Ĥ�
		/**
		 * Low Quality Large
		 * ���}���j��Ī
		 */
		{ 12779, 50,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2 },		// ���֪�����j�ƨ��b-A��
		{ 12779, 30,  6622,1 },		// ���H���g��
		{ 12779, 10,  8750,1 },		// ���ťͩR��-67��
		{ 12779, 10,  8751,1 },		// ���ťͩR��-70��
		{ 12779, 99, 14701,4,		// �W�j�O������O�v¡�Ĥ�
		             14700,4 },		// �W�j�O��O�v¡�Ĥ�
		{ 12779, 50,  1461,4 },		// ����-A��
		{ 12779, 30,  1462,3 },		// ����-S��
		{ 12779, 50,  2133,4 },		// �_��-A��
		{ 12779, 30,  2134,3 },		// �_��-S��
		/**
		 * High Quality Large
		 * �u�}���j��Ī
		 */
		{ 12778,  7,  9570,1,		// ����j���-���q14
		              9571,1,		// �Ŧ�j���-���q14
		              9572,1,		// ���j���-���q14
		             10480,1,		// ����j���-���q15
		             10481,1,		// �Ŧ�j���-���q15
		             10482,1,		// ���j���-���q15
		             13071,1,		// ����j���-���q16
		             13072,1,		// �Ŧ�j���-���q16
		             13073,1 },		// ���j���-���q16
		{ 12778, 35,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		               959,3,		// �Z���j�ƨ��b-S��
		               960,3,		// ����j�ƨ��b-S��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2,		// ���֪�����j�ƨ��b-A��
		              6577,1,		// ���֪��Z���j�ƨ��b-S��
		              6578,1 },		// ���֪�����j�ƨ��b-S��
		{ 12778, 28,  6622,3,		// ���H���g��
		              9625,2,		// ���H���g��-��ѽg
		              9626,2,		// ���H���g��-�V�m�g
		              9627,2 },		// ���H���g��-���m�g
		{ 12778, 14,  8750,10 },	// ���ťͩR��-67��
		{ 12778, 14,  8751,8 },		// ���ťͩR��-70��
		{ 12778, 14,  8752,6 },		// ���ťͩR��-76��
		{ 12778, 14,  9575,4 },		// ���ťͩR��-80��
		{ 12778, 14, 10485,2 },		// ���ťͩR��-82��
		{ 12778, 14, 14168,1 },		// ���ťͩR��-84��
		{ 12778, 21,  8760,1,		// �S�ťͩR��-67��
		              8761,1,		// �S�ťͩR��-70��
		              8762,1,		// �S�ťͩR��-76��
		              9576,1,		// �S�ťͩR��-80��
		             10486,1,		// �S�ťͩR��-82��
		             14169,1 },		// �S�ťͩR��-84��
		{ 12778, 21, 14683,1,		// �W�j�O�ͩR�F��-D��
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
		{ 12778, 99, 14701,9,		// �W�j�O������O�v¡�Ĥ�
		             14700,9 },		// �W�j�O��O�v¡�Ĥ�
		{ 12778, 63,  1461,8 },		// ����-A��
		{ 12778, 49,  1462,5 },		// ����-S��
		{ 12778, 63,  2133,6 },		// �_��-A��
		{ 12778, 49,  2134,4 },		// �_��-S��
		/**
		 * Emperor Squash
		 * �u�}���j��Ī��
		 */
		{ 13017, 10,  9570,1,		// ����j���-���q14
		              9571,1,		// �Ŧ�j���-���q14
		              9572,1,		// ���j���-���q14
		             10480,1,		// ����j���-���q15
		             10481,1,		// �Ŧ�j���-���q15
		             10482,1,		// ���j���-���q15
		             13071,1,		// ����j���-���q16
		             13072,1,		// �Ŧ�j���-���q16
		             13073,1 },		// ���j���-���q16
		{ 13017, 50,   729,4,		// �Z���j�ƨ��b-A��
		               730,4,		// ����j�ƨ��b-A��
		               959,3,		// �Z���j�ƨ��b-S��
		               960,3,		// ����j�ƨ��b-S��
		              6569,2,		// ���֪��Z���j�ƨ��b-A��
		              6570,2,		// ���֪�����j�ƨ��b-A��
		              6577,1,		// ���֪��Z���j�ƨ��b-S��
		              6578,1 },		// ���֪�����j�ƨ��b-S��
		{ 13017, 40,  6622,3,		// ���H���g��
		              9625,2,		// ���H���g��-��ѽg
		              9626,2,		// ���H���g��-�V�m�g
		              9627,2 },		// ���H���g��-���m�g
		{ 13017, 20,  8750,10 },	// ���ťͩR��-67��
		{ 13017, 20,  8751,8 },		// ���ťͩR��-70��
		{ 13017, 20,  8752,6 },		// ���ťͩR��-76��
		{ 13017, 20,  9575,4 },		// ���ťͩR��-80��
		{ 13017, 20, 10485,2 },		// ���ťͩR��-82��
		{ 13017, 20, 14168,1 },		// ���ťͩR��-84��
		{ 13017, 30,  8760,1,		// �S�ťͩR��-67��
		              8761,1,		// �S�ťͩR��-70��
		              8762,1,		// �S�ťͩR��-76��
		              9576,1,		// �S�ťͩR��-80��80
		             10486,1,		// �S�ťͩR��-82��
		             14169,1 },		// �S�ťͩR��-84��
		{ 13017, 30, 14683,1,		// �W�j�O�ͩR�F��-D��
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
		{ 13017, 99, 14701,12,		// �W�j�O������O�v¡�Ĥ�
		             14700,12 },	// �W�j�O��O�v¡�Ĥ�
		{ 13017, 90,  1461,8 },		// ����-A��
		{ 13017, 70,  1462,5 },		// ����-S��
		{ 13017, 90,  2133,6 },		// �_��-A��
		{ 13017, 70,  2134,4 },		// �_��-S��
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
			final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
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
			final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
			mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
		}
		return "";
		//return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		L2Weapon weapon;
		final boolean isChronoAttack = !isPet && (weapon = attacker.getActiveWeaponItem()) != null && contains(CHRONO_LIST, weapon.getItemId());
		switch (mob.getNpcId())
		{
			case 12774:
			case 12775:
			case 12776:
			case 13016:
				if (isChronoAttack)
				{
					chronoText(mob);
				}
				else
				{
					noChronoText(mob);
				}
				break;
			case 12777:
			case 12778:
			case 12779:
			case 13017:
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
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		if (skill.getId() == NECTAR_SKILL && targets[0] == mob)
		{
			switch(mob.getNpcId())
			{
				case 12774:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12775, 12775, 13016, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(12776, 12776, 12776, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(12776, 12775, 13016, mob);
					break;
				case 12777:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(12778, 12778, 13017, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(12779, 12779, 12779, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(12779, 12778, 13017, mob);
					break;
				case 12775:
					mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13016, mob);
					break;
				case 12778:
					mob.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13017, mob);
					break;
				case 12776:
				case 12779:
					autoChat(mob, "�����j�F�I���N�Ө��H�o~");
					break;
			}
		}
		return super.onSkillSee(npc,caster,skill,targets,isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
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
		
		final L2ChronoMonsterInstance mob = ((L2ChronoMonsterInstance)npc);
		mob.setOnKillDelay(1500);	//Default 5000ms.
		final TheInstance self = create(mob);
		switch(mob.getNpcId())
		{
			case 12774:
			case 12777:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_FIRST, mob, null);
				self.nectar = 0;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_FIRST;
				autoChat(mob, SPAWN_TEXT[Rnd.get(SPAWN_TEXT.length)]);
				break;
			case 12775:
			case 12776:
			case 12778:
			case 12779:
			case 13016:
			case 13017:
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

	public SquashEvent(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : SQUASH_LIST)
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
			case MANAGER: return "31255.htm";
		}
		throw new RuntimeException();
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		// 31255-1.htm
		return event;
	}

	public static void main(String[] args)
	{
		new SquashEvent(-1, qn, "events");
	}
}