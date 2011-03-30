/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.bulkloader.common;

/**
 * ���O�o�͂Ŏg�p����郁�b�Z�[�WID���ꌳ�Ǘ����邽�߂̃N���X�ł��B
 * ���̃N���X�̓��b�Z�[�W�t�@�C���ɂ�莩���������ꂽ���̂Ȃ̂ŁA���ڕҏW���Ȃ��ł��������B
 *
 * @author logmessage.xls
 * @version $Id$
 */
public final class MessageIdConst {

    /** [ERROR] DB�R�l�N�V�����̎擾�Ɏ��s���܂����B�G���[���e�F{0} */
    public static final String CMN_DB_CONN_ERROR = "TG-COMMON-00001";

    /** [ERROR] Import�����p��DSL�v���p�e�B�̓ǂݍ��݂Ɏ��s���܂����B�G���[���e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�t�@�C����{3} */
    public static final String CMN_IMP_DSL_LOADERROR = "TG-COMMON-00002";

    /** [ERROR] Export�����p��DSL�v���p�e�B�̓ǂݍ��݂Ɏ��s���܂����B�G���[���e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�t�@�C����{3} */
    public static final String CMN_EXP_DSL_LOADERROR = "TG-COMMON-00003";

    /** [ERROR] Import�����p��DSL�v���p�e�B�̃`�F�b�N�Ɏ��s���܂����B�G���[���e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�e�[�u�����F{3}�A�t�@�C����{4} */
    public static final String CMN_IMP_DSL_CHECKERROR = "TG-COMMON-00004";

    /** [ERROR] Export�����p��DSL�v���p�e�B�̃`�F�b�N�Ɏ��s���܂����B�G���[���e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�e�[�u�����F{3}�A�t�@�C����{4} */
    public static final String CMN_EXP_DSL_CHECKERROR = "TG-COMMON-00005";

    /** [WARN] Import�����p��DSL�v���p�e�B�̃`�F�b�N�ŕs���Ȑݒ肪����܂����B�s�����e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�e�[�u�����F{3}�A�t�@�C����{4} */
    public static final String CMN_IMP_DSL_CHECKWARN = "TG-COMMON-00006";

    /** [WARN] Export�����p��DSL�v���p�e�B�̃`�F�b�N�ŕs���Ȑݒ肪����܂����B�s�����e�F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[ID�F{2}�A�e�[�u�����F{3}�A�t�@�C����{4} */
    public static final String CMN_EXP_DSL_CHECKWARN = "TG-COMMON-00007";

    /** [ERROR] �v���p�e�B�t�@�C���̐ݒ�l���s���ł��B�s�����e�F{0} */
    public static final String CMN_PROP_CHECK_ERROR = "TG-COMMON-00008";

    /** [ERROR] �J������{0}��Export����TSV�t�@�C���Ɋ܂܂�܂���B */
    public static final String CMN_COLUMN_INCLUDE_ERROR = "TG-COMMON-00009";

    /** [ERROR] DBMS�̐ڑ������L�q�����v���p�e�B�t�@�C���̓ǂݍ��݂Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String CMN_JDBCCONF_LOAD_ERROR = "TG-COMMON-00010";

    /** [ERROR] DBMS�̐ڑ������L�q�����v���p�e�B�t�@�C���̐ݒ�l���s���ł��B�s�����e�F{0} */
    public static final String CMN_JDBCCONF_CHECK_ERROR = "TG-COMMON-00011";

    /** [ERROR] DBMS�̐ڑ������L�q�����v���p�e�B�t�@�C���̓ǂݍ��݂Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String CMN_JDBCCONF_READ_ERROR = "TG-COMMON-00012";

    /** [ERROR] JDBC�h���C�o�̃��[�h�Ɏ��s���܂����BJDBC�h���C�o���F{0} */
    public static final String CMN_JDBCDRIVER_LOAD_ERROR = "TG-COMMON-00013";

    /** [ERROR] SQL�̎��s�Ɏ��s���܂����BSQL���F{0} �p�����[�^�F{1} */
    public static final String CMN_DB_SQL_EXEC_ERROR = "TG-COMMON-00014";

    /** [ERROR] �g�����U�N�V�����̃R�~�b�g�Ɏ��s���܂����B */
    public static final String CMN_DB_CONN_COMMIT_ERROR = "TG-COMMON-00015";

    /** [ERROR] �g�����U�N�V�����̃��[���o�b�N�Ɏ��s���܂����B */
    public static final String CMN_DB_CONN_ROLLBACK_ERROR = "TG-COMMON-00016";

    /** [ERROR] Import�t�@�C���𐶐�����f�B���N�g�������݂��܂���B�f�B���N�g�����F{0} */
    public static final String CMN_IMP_DIR_NOT_FIND_ERROR = "TG-COMMON-00017";

    /** [ERROR] HDFS��URI���s���ł��BURI�F{0} */
    public static final String CMN_IMP_HDFS_PATH_ERROR = "TG-COMMON-00018";

    /** [ERROR] HDFS�̃t�@�C���V�X�e���̎擾�Ɏ��s���܂����BURI�F{0} */
    public static final String CMN_IMP_HDFS_FILESYS_ERROR = "TG-COMMON-00019";

    /** [INFO] ����W���u�t���[���sID�ŕ����v���Z�X�����삵�Ȃ��ׂ̃��b�N���擾���܂��BSQL�F{0} �W���u�t���[���sID�F{1} */
    public static final String CMN_EXECUTIONID_LOCK = "TG-COMMON-00020";

    /** [INFO] ����W���u�t���[���sID�ŕ����v���Z�X�����삵�Ȃ��ׂ̃��b�N���������܂��B */
    public static final String CMN_EXECUTIOND_LOCK_RELEASE = "TG-COMMON-00021";

    /** [DEBUG] SQL�����s���܂��BSQL�F{0} �p�����[�^�F{1} */
    public static final String CMN_SQL_EXECUTE_BEFORE = "TG-COMMON-00022";

    /** [DEBUG] SQL�����s���܂����B���s����(�~���b)�F{0} �����F{1} SQL�F{2} �p�����[�^�F{3} */
    public static final String CMN_SQL_EXECUTE_AFTER = "TG-COMMON-00023";

    /** [DEBUG] �g�����U�N�V�������R�~�b�g���܂��B */
    public static final String CMN_COMMIT_EXECUTE_BEFORE = "TG-COMMON-00024";

    /** [DEBUG] �g�����U�N�V�������R�~�b�g���܂����B�R�~�b�g����(�~���b)�F{0} */
    public static final String CMN_COMMIT_EXECUTE_AFTER = "TG-COMMON-00025";

    /** [DEBUG] �g�����U�N�V���������[���o�b�N���܂��B */
    public static final String CMN_ROLLBACK_EXECUTE_BEFORE = "TG-COMMON-00026";

    /** [DEBUG] �g�����U�N�V���������[���o�b�N���܂����B�R�~�b�g����(�~���b)�F{0} */
    public static final String CMN_ROLLBACK_EXECUTE_AFTER = "TG-COMMON-00027";

    /** [ERROR] �X�g���[���̃��_�C���N�g�Ɏ��s���܂����B */
    public static final String CMN_LOG_REDIRECT_ERROR = "TG-COMMON-00028";

    /** [INFO] Importer�̏������J�n���܂��B�J�n�����F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_START = "TG-IMPORTER-01001";

    /** [INFO] Importer�̏����𐳏�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_EXIT = "TG-IMPORTER-01002";

    /** [ERROR] Importer�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_INIT_ERROR = "TG-IMPORTER-01003";

    /** [ERROR] Importer�Ń��b�N�擾�����Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_LOCK_ERROR = "TG-IMPORTER-01004";

    /** [ERROR] Importer��Import�Ώۃt�@�C�����������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_CREATEFILE_ERROR = "TG-IMPORTER-01005";

    /** [ERROR] Importer��Import�Ώۃf�[�^���M�����Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_SENDDATA_ERROR = "TG-IMPORTER-01006";

    /** [ERROR] Importer�ŃL���b�V���̎��o�������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_CACHE_ERROR = "TG-IMPORTER-01007";

    /** [ERROR] Importer�Ɏw�肷��p�����[�^���s���ł��B�s�����e�F{0} �l�F{1} */
    public static final String IMP_PARAMCHECK_ERROR = "TG-IMPORTER-01008";

    /** [ERROR] Importer�Ńp�����[�^�̓�̓`�F�b�N�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_PARAM_ERROR = "TG-IMPORTER-01009";

    /** [ERROR] Importer�ŕs���ȃG���[���������܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_EXCEPRION = "TG-IMPORTER-01010";

    /** [INFO] �W���u�t���[���s�e�[�u���ɃW���u�t���[�̎��s���L�^���܂����B����I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_TARGET_NO_EXIST_SUCCESS = "TG-IMPORTER-01011";

    /** [ERROR] �W���u�t���[���s�e�[�u���փW���u�t���[���s�̋L�^�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_INSERT_RNNINGJOBFLOW_ERROR = "TG-IMPORTER-01012";

    /** [ERROR] �w�肳�ꂽ�W���u�t���[���sID�͑��v���Z�X�ɂ�菈�����̂��߁AImporter���ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_INSTANCE_ID_LOCKED = "TG-IMPORTER-01013";

    /** [ERROR] �W���u�t���[���sID�ɂ��r������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_INSTANCE_ID_LOCK_ERROR = "TG-IMPORTER-01014";

    /** [INFO] �W���u�t���[���sID�ɂ��r��������s���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_INSTANCE_ID_LOCK = "TG-IMPORTER-01015";

    /** [INFO] �W���u�t���[���sID�ɂ��r������ɐ������܂����BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_INSTANCE_ID_LOCK_SUCCESS = "TG-IMPORTER-01016";

    /** [INFO] Import�Ώۃe�[�u���̃��b�N���擾���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_LOCK = "TG-IMPORTER-01017";

    /** [INFO] Import�Ώۃe�[�u���̃��b�N�擾�ɐ������܂����BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_LOCK_SUCCESS = "TG-IMPORTER-01018";

    /** [INFO] Import�Ώۃe�[�u�������݂��Ȃ����߁A�C���|�[�g�͍s�킸�ɃW���u�t���[���s�e�[�u���ɃW���u�t���[���s�̋L�^�݂̂��s���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_TARGET_NO_EXIST = "TG-IMPORTER-01019";

    /** [INFO] Import�Ώۃe�[�u�������݂��Ȃ����߁A�C���|�[�g�͍s�킸��Importer�𐳏�I�����܂��B�I�������F{0} Import�����敪�F{1} �^�[�Q�b�g���F{2} �o�b�`ID�F{3} �W���u�t���[ID�F{4} �W���u�t���[���sID�F{5} */
    public static final String IMP_TARGET_NO_EXIST_SECONDARY = "TG-IMPORTER-01020";

    /** [INFO] Import�Ώۃt�@�C���̐������s���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_CREATEFILE = "TG-IMPORTER-01021";

    /** [INFO] Import�Ώۃt�@�C���̐����ɐ������܂����BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_CREATEFILE_SUCCESS = "TG-IMPORTER-01022";

    /** [INFO] Import�Ώۃt�@�C���̑��M���s���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_SENDDATA = "TG-IMPORTER-01023";

    /** [INFO] Import�Ώۃt�@�C���̑��M���ɐ������܂����BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_SENDDATA_SUCCESS = "TG-IMPORTER-01024";

    /** [INFO] ��������Import�Ώۃt�@�C���iTSV���ԃt�@�C���j���폜���܂��BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_TSV_FILE_DELETE = "TG-IMPORTER-01025";

    /** [INFO] �C���|�[�g����������I�������ꍇ��TSV�t�@�C���폜�L���Ɂu0�F�폜���Ȃ��v���ݒ肳��Ă��邽�߁A��������Import�Ώۃt�@�C���iTSV���ԃt�@�C���j���폜���܂���BImport�����敪�F{0} �^�[�Q�b�g���F{1} �o�b�`ID�F{2} �W���u�t���[ID�F{3} �W���u�t���[���sID�F{4} */
    public static final String IMP_TSV_FILE_NOT_DELETE = "TG-IMPORTER-01026";

    /** [INFO] ����W���u�t���[���sID�̃��R�[�h�����݂��邽�߃��b�N�擾�������X�L�b�v���܂��B�^�[�Q�b�g���F{0} �W���u�t���[���sID�F{1} �W���u�t���[SID�F{2} */
    public static final String IMP_EXISTS_JOBNET_INSTANCEID = "TG-IMPORTER-02001";

    /** [ERROR] ���b�N�擾�����Ń��g���C���s���A���g���C�C���^�[�o�����X���[�v���ɗ�O���������܂����B */
    public static final String IMP_GET_LOCK_SLEEP_ERROR = "TG-IMPORTER-02002";

    /** [ERROR] ���b�N�擾���������g���C�I�[�o�[���܂����B�ُ�I�����܂��B */
    public static final String IMP_GET_LOCK_RETRY_ORVER = "TG-IMPORTER-02003";

    /** [WARN] ���b�N�擾�����Ń��g���C�\�ȃG���[���������܂����B�G���[���e�F{0}�A�e�[�u����{1} */
    public static final String IMP_GET_LOCK_RETRY = "TG-IMPORTER-02004";

    /** [INFO] ���b�N�擾�̃g�����U�N�V�������J�n���܂��B�^�[�Q�b�g���F{0} �W���u�t���[���sID�F{1} */
    public static final String IMP_LOCK__TRAN_START = "TG-IMPORTER-02005";

    /** [INFO] ���b�N�擾�̃g�����U�N�V�������I�����܂��B�^�[�Q�b�g���F{0} �W���u�t���[���sID�F{1} */
    public static final String IMP_LOCK__TRAN_END = "TG-IMPORTER-02006";

    /** [INFO] �W���u�t���[���s�e�[�u���ɃW���u�t���[�̎��s���L�^���܂��BSQL�F{0} �o�b�`ID{1} �W���u�t���[ID�F{2} �^�[�Q�b�g��{3} �W���u�t���[���sID�F{4} �I���\�莞���F{5} */
    public static final String IMP_INSERT_RNNINGJOBFLOW = "TG-IMPORTER-02007";

    /** [INFO] �e�[�u�����b�N�e�[�u����Import�Ώۃe�[�u���s��TX���b�N���擾���A���b�N�擾/��������̔r��������s���܂��BSQL�F{0} */
    public static final String IMP_LOCK_EXCLUSIVE = "TG-IMPORTER-02008";

    /** [INFO] �W���u�t���[�ݒu�ɏ]���AImport�Ώۃe�[�u���ɑ΂��郍�b�N���擾���܂��BImport�Ώۃe�[�u�����F{0} ���b�N�擾�^�C�v�F{1} ���b�N�ς݂̏ꍇ�̋����F{2} ���������F{3} */
    public static final String IMP_IMPORT_TARGET_LOCK = "TG-IMPORTER-02009";

    /** [INFO] Import�Ώۃe�[�u���ɑ΂��郍�b�N�擾���I�����܂��BImport�Ώۃe�[�u�����F{0} ���b�N�擾�^�C�v�F{1} ���b�N�ς݂̏ꍇ�̋����F{2} ���������F{3} */
    public static final String IMP_IMPORT_TARGET_LOCK_END = "TG-IMPORTER-02010";

    /** [INFO] ���b�N�擾�^�C�v���u���b�N���Ȃ��v�A���b�N�ς݂̏ꍇ�̋������u���b�N�L���Ɋւ�炸�����ΏۂƂ���v�̂��߁A���YImport�Ώۃe�[�u���ɑ΂��郍�b�N�擾�y�у��b�N�ς݂̃`�F�b�N�͍s���܂���BImport�Ώۃe�[�u�����F{0}  */
    public static final String IMP_NONE_FORCE_DONE = "TG-IMPORTER-02011";

    /** [INFO] ���b�N�擾�^�C�v���u�s���b�N�v�A���b�N�ς݂̏ꍇ�̋������u�����Ώۂ���O���v�ł���A���YImport�Ώۃe�[�u���ɑ΂���e�[�u�����b�N����Ɏ擾����Ă��邽�߁A���b�N�擾�͍s���܂���BImport�Ώۃe�[�u�����F{0}  */
    public static final String IMP_TABLE_LOCKED = "TG-IMPORTER-02012";

    /** [INFO] ���b�N�擾�^�C�v���u���b�N���Ȃ��v�A���b�N�ς݂̏ꍇ�̋������u�G���[�Ƃ���v�ł���A���YImport�Ώۃe�[�u���ɑ΂���e�[�u�����b�N�y�у��R�[�h���b�N���擾����Ă��Ȃ����߁A���b�N�擾�͍s�킸�Ƀ��b�N�擾�������I�����܂��BImport�Ώۃe�[�u�����F{0}  */
    public static final String IMP_NONE_ERROR_DONE = "TG-IMPORTER-02013";

    /** [INFO] Import�Ώۃe�[�u���ɑ΂���e�[�u�����b�N���擾���܂��BSQL�F{0} �W���u�t���[SID�F{1} �e�[�u�����F{2} */
    public static final String IMP_GET_TABLE_LOCK = "TG-IMPORTER-02014";

    /** [INFO] Import�Ώۃe�[�u���ɑ΂��郌�R�[�h���b�N���擾���܂��BSQL�F{0} �W���u�t���[SID�F{1} */
    public static final String IMP_GET_RECORD_LOCK = "TG-IMPORTER-02015";

    /** [ERROR] Import�t�@�C�����������Ŋ�ɑ��݂���Import�t�@�C���̍폜�Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String IMP_EXISTSFILE_DELETE_ERROR = "TG-IMPORTER-03001";

    /** [ERROR] Import�t�@�C�����������őΏۃf�[�^�����݂����A0byte�̃t�@�C�������Ɏ��s���܂����B */
    public static final String IMP_CREATEFILE_EXCEPTION = "TG-IMPORTER-03002";

    /** [INFO] Import�t�@�C���𐶐����܂��BImport�Ώۃe�[�u�����F{0} ���b�N�擾�^�C�v�F{1} �t�@�C�����F{2} */
    public static final String IMP_CREATE_FILE = "TG-IMPORTER-03003";

    /** [INFO] Import�t�@�C���𐶐����܂����BImport�Ώۃe�[�u�����F{0} ���b�N�擾�^�C�v�F{1} �t�@�C�����F{2} */
    public static final String IMP_CREATE_FILE_SUCCESS = "TG-IMPORTER-03004";

    /** [INFO] Import�t�@�C������������Ȃ������ׁA���Import�t�@�C���𐶐����܂����BImport�Ώۃe�[�u�����F{0} ���b�N�擾�^�C�v�F{1} �t�@�C�����F{2} */
    public static final String IMP_CREATE_ZERO_FILE = "TG-IMPORTER-03005";

    /** [INFO] �W���u�t���[SID�������Ƀ��R�[�h�𒊏o���ăt�@�C���𐶐����܂��BSQL�F{0} �W���u�t���[SID�F{1} */
    public static final String IMP_CREATE_FILE_WITH_JOBFLOWSID = "TG-IMPORTER-03006";

    /** [INFO] ���������Ń��R�[�h�𒊏o���ăt�@�C���𐶐����܂��BSQL�F{0} */
    public static final String IMP_CREATE_FILE_WITH_CONDITION = "TG-IMPORTER-03007";

    /** [ERROR] Import�t�@�C�����M�����ŃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String IMP_SENDFILE_EXCEPTION = "TG-IMPORTER-04001";

    /** [ERROR] Import�t�@�C�����M�����ŋN�������T�u�v���Z�X���ُ�I�����܂����B�I���R�[�h�F{0} */
    public static final String IMP_EXTRACTOR_ERROR = "TG-IMPORTER-04002";

    /** [INFO] Import�t�@�C�����M�ׂ̈̃T�u�v���Z�X���N�����܂��BSSH�̃p�X�F{0} �}�X�^�[�m�[�h�̃z�X�g�F{1} �}�X�^�[�m�[�h�̃��[�U�[�F{2} Extractor�̃V�F�����F{3} �^�[�Q�b�g���F{4} �o�b�`ID�F{5} �W���u�t���[ID�F{6} �W���u�t���[���sID�F{7} */
    public static final String IMP_START_SUB_PROCESS = "TG-IMPORTER-04003";

    /** [INFO] Import�t�@�C���𑗐M���܂��BImport�Ώۃe�[�u�����F{0} Import�t�@�C���F{1} Import�t�@�C����ZipEntry���F{2} ZIP���k�L���F{3} */
    public static final String IMP_FILE_SEND = "TG-IMPORTER-04004";

    /** [INFO] Import�t�@�C���𑗐M���܂����BImport�Ώۃe�[�u�����F{0} Import�t�@�C���F{1} Import�t�@�C����ZipEntry���F{2} ZIP���k�L���F{3} */
    public static final String IMP_FILE_SEND_END = "TG-IMPORTER-04005";

    /** [WARN] Import�t�@�C���폜������Import�t�@�C���̍폜�Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String IMP_FILEDELETE_ERROR = "TG-IMPORTER-05001";

    /** [WARN] Import�t�@�C���폜������Import�t�@�C���i�[�f�B���N�g���̍폜�Ɏ��s���܂����B�f�B���N�g�����F{0} */
    public static final String IMP_DIRDELETE_ERROR = "TG-IMPORTER-05002";

    /** [INFO] Extractor�̏������J�n���܂��B�J�n�����F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_START = "TG-EXTRACTOR-01001";

    /** [INFO] Extractor�̏����𐳏�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_EXIT = "TG-EXTRACTOR-01002";

    /** [ERROR] Extractor�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_INIT_ERROR = "TG-EXTRACTOR-01003";

    /** [ERROR] Extractor��Import�t�@�C����HDFS�ւ̏����o���Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_CREATEFILE_ERROR = "TG-EXTRACTOR-01004";

    /** [ERROR] Extractor�Ɏw�肷��p�����[�^���s���ł��B�s�����e�F{0}�A�l�F{1} */
    public static final String EXT_PARAMCHECK_ERROR = "TG-EXTRACTOR-01005";

    /** [ERROR] Extractor�Ńp�����[�^�̓�̓`�F�b�N�Ɏ��s���܂����B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_PARAM_ERROR = "TG-EXTRACTOR-01006";

    /** [ERROR] Extractor�ŕs���ȃG���[���������܂����B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String EXT_EXCEPRION = "TG-EXTRACTOR-01007";

    /** [INFO] Import�t�@�C��������AHDFS�ɏ����o���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3}�A���[�U�[���F{4} */
    public static final String EXT_CREATEFILE = "TG-EXTRACTOR-01008";

    /** [INFO] Import�t�@�C����HDFS�֏����o���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3}�A���[�U�[���F{4} */
    public static final String EXT_CREATEFILE_SUCCESS = "TG-EXTRACTOR-01009";

    /** [ERROR] Import�t�@�C����HDFS�ɏ����o�������ŃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String EXT_CREATE_HDFSFILE_EXCEPTION = "TG-EXTRACTOR-02001";

    /** [INFO] Import�t�@�C����HDFS�ɏ����o���܂��BImport�Ώۃe�[�u�����F{0} HDFS�̃p�X�F{1} Model�N���X�F{2} */
    public static final String EXT_CREATE_HDFSFILE = "TG-EXTRACTOR-02002";

    /** [INFO] Import�t�@�C����HDFS�ɏ����o���܂����BImport�Ώۃe�[�u�����F{0} HDFS�̃p�X�F{1} Model�N���X�F{2} */
    public static final String EXT_CREATE_HDFSFILE_SUCCESS = "TG-EXTRACTOR-02003";

    /** [WARN] HDFS��SequenceFile���C���|�[�g���鎞��SequenceFile�����k���邩�̎w�肪�s���ł��B�u���k�Ȃ��v�̐ݒ��K�p���܂��B���k�w��F{0} */
    public static final String EXT_SEQ_COMP_TYPE_FAIL = "TG-EXTRACTOR-02004";

    /** [INFO] Exporter�̏������J�n���܂��B�J�n�����F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_START = "TG-EXPORTER-01001";

    /** [INFO] Exporter�̏����𐳏�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_EXIT = "TG-EXPORTER-01002";

    /** [ERROR] Exporter�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_INIT_ERROR = "TG-EXPORTER-01003";

    /** [ERROR] Exporter�ŕs���ȃG���[���������܂����B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_EXCEPRION = "TG-EXPORTER-01004";

    /** [ERROR] Exporter�Ɏw�肷��p�����[�^���s���ł��B�s�����e�F{0} �l�F{1} */
    public static final String EXP_PARAMCHECK_ERROR = "TG-EXPORTER-01005";

    /** [ERROR] Exporter�Ńp�����[�^�̓�̓`�F�b�N�Ɏ��s���܂����B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_PARAM_ERROR = "TG-EXPORTER-01006";

    /** [ERROR] Exporter��Export�t�@�C���̎�M�����Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_FILERECEIVE_ERROR = "TG-EXPORTER-01007";

    /** [ERROR] Exporter�Ńe���|�����e�[�u���ւ�Export�t�@�C���̃��[�h�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_FILELOAD_ERROR = "TG-EXPORTER-01008";

    /** [ERROR] Exporter�Ń��b�N�̉����Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_RELEASELOCK_ERROR = "TG-EXPORTER-01009";

    /** [ERROR] Exporter�ŃW���u�t���[SID�̎擾�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_GETJOBFLOWSID_ERROR = "TG-EXPORTER-01010";

    /** [ERROR] �I�������W���u�t���[�ɑ΂���Export�������Ď��s���́AImporter�����s����Ă��Ȃ��W���u�t���[�ɑ΂���Export���������s����܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_JOBFLOW_EXIT_ERROR = "TG-EXPORTER-01011";

    /** [ERROR] Exporter�œ��Y�W���u�t���[SID�ɑΉ�����e���|�����e�[�u���̏��擾�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_TEMP_INFO_ERROR = "TG-EXPORTER-01012";

    /** [ERROR] Exporter�œ��Y�W���u�t���[SID�ɑΉ�����e���|�����e�[�u���̍폜�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} �W���u�t���[SID�F{5} */
    public static final String EXP_TEMP_DELETE_ERROR = "TG-EXPORTER-01013";

    /** [ERROR] Exporter�Ńe���|�����e�[�u������Export�Ώۃe�[�u���ւ̃f�[�^�̃R�s�[�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_DATA_COPY_ERROR = "TG-EXPORTER-01014";

    /** [ERROR] Export�Ώۃe�[�u���ɍX�V�Ώۂ̃��R�[�h�����݂��Ȃ��f�[�^�������ď������s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_DATA_UPDATE_NOT_EXIT = "TG-EXPORTER-01015";

    /** [ERROR] �w�肳�ꂽ�W���u�t���[���sID�͑��v���Z�X�ɂ�菈�����̂��߁AExporter���ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_INSTANCE_ID_LOCKED = "TG-EXPORTER-01016";

    /** [ERROR] �W���u�t���[���sID�ɂ��r������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4} */
    public static final String EXP_INSTANCE_ID_LOCK_ERROR = "TG-EXPORTER-01017";

    /** [INFO] �W���u�t���[���sID�ɂ��r��������s���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_INSTANCE_ID_LOCK = "TG-EXPORTER-01018";

    /** [INFO] �W���u�t���[���sID�ɂ��r������ɐ������܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_INSTANCE_ID_LOCK_SUCCESS = "TG-EXPORTER-01019";

    /** [INFO] ���Y�W���u�t���[SID�ɑΉ�����e���|�����e�[�u�����폜���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} �W���u�t���[SID�F{4} */
    public static final String EXP_TEMP_DELETE = "TG-EXPORTER-01020";

    /** [INFO] ���Y�W���u�t���[SID�ɑΉ�����e���|�����e�[�u�����폜���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} �W���u�t���[SID�F{4} */
    public static final String EXP_TEMP_DELETE_SUCCESS = "TG-EXPORTER-01021";

    /** [INFO] Export�t�@�C������M���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_FILERECEIVE = "TG-EXPORTER-01022";

    /** [INFO] Export�t�@�C������M���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_FILERECEIVE_SUCCESS = "TG-EXPORTER-01023";

    /** [INFO] Export�e���|�����e�[�u����Export�t�@�C�������[�h���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_FILELOAD = "TG-EXPORTER-01024";

    /** [INFO] Export�e���|�����e�[�u����Export�t�@�C�������[�h���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_FILELOAD_SUCCESS = "TG-EXPORTER-01025";

    /** [INFO] Export�e���|�����e�[�u������Export�Ώۃe�[�u���փf�[�^���R�s�[���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_DATA_COPY = "TG-EXPORTER-01026";

    /** [INFO] Export�e���|�����e�[�u������Export�Ώۃe�[�u���փf�[�^���R�s�[���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_DATA_COPY_SUCCESS = "TG-EXPORTER-01027";

    /** [INFO] ���b�N�̉������s���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_RELEASELOCK = "TG-EXPORTER-01028";

    /** [INFO] ���b�N�̉������s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_RELEASELOCK_SUCCESS = "TG-EXPORTER-01029";

    /** [INFO] ��������Export�Ώۃt�@�C���iTSV���ԃt�@�C���j���폜���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_TSV_FILE_DELETE = "TG-EXPORTER-01030";

    /** [INFO] Exporter�Ŏ��s���鏈���𔻒f���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} �e���|�����e�[�u���폜�����F{4} Export�t�@�C����M�����F{5} Export�t�@�C�����[�h�����F{6} Export�f�[�^�R�s�[�����F{7} ���b�N���������F{8} ���ԃt�@�C���폜�����F{9} */
    public static final String EXP_EXEC_PROCESS_JUDGE = "TG-EXPORTER-01031";

    /** [INFO] �G�N�X�|�[�g����������I�������ꍇ��TSV�t�@�C���폜�L���Ɂu0�F�폜���Ȃ��v���ݒ肳��Ă��邽�߁A�G�N�X�|�[�g�����ɐ��������ꍇ����������Export�Ώۃt�@�C���iTSV���ԃt�@�C���j���폜���܂���B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3} */
    public static final String EXP_TSV_FILE_NOT_DELETE = "TG-EXPORTER-01032";

    /** [ERROR] Export�t�@�C����M�����Ńt�@�C���𐶐�����f�B���N�g�������݂��܂���B�f�B���N�g�����F{0} */
    public static final String EXP_DIR_NOT_EXISTS_ERROR = "TG-EXPORTER-02001";

    /** [ERROR] Export�t�@�C����M�����ŃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String EXP_FILERECEIV_EXCEPTION = "TG-EXPORTER-02002";

    /** [ERROR] Export�t�@�C����M������ZIP�G���g���ɑΉ�����e�[�u���̒�`��DSL���݂��܂���BZIP�G���g�����F{0} �e�[�u�����F{1} */
    public static final String EXP_DSL_NOTFOUND = "TG-EXPORTER-02003";

    /** [ERROR] Export�t�@�C����M������Export�t�@�C���Ɠ����̃t�@�C������ɑ��݂��A�폜�Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String EXP_DELETEFILE_FAILED = "TG-EXPORTER-02004";

    /** [ERROR] Export�t�@�C����M�����ŋN�������T�u�v���Z�X���ُ�I�����܂����B�I���R�[�h�F{0} */
    public static final String EXP_COLLECTOR_ERROR = "TG-EXPORTER-02005";

    /** [ERROR] �W���u�t���[���sID����W���u�t���[SID���擾���鏈���ŗ�O���������܂����B�W���u�t���[���sID�F{0} */
    public static final String EXP_JOBFLOWSID_ERROR = "TG-EXPORTER-02006";

    /** [INFO] Export�t�@�C����M�ׂ̈̃T�u�v���Z�X���N�����܂��BSSH�̃p�X�F{0} �}�X�^�[�m�[�h�̃z�X�g�F{1} �}�X�^�[�m�[�h�̃��[�U�[�F{2} Collector�̃V�F�����F{3} �^�[�Q�b�g���F{4} �o�b�`ID�F{5} �W���u�t���[ID�F{6} �W���u�t���[���sID�F{7} */
    public static final String EXP_START_SUB_PROCESS = "TG-EXPORTER-02007";

    /** [INFO] ��M�����t�@�C���𐶐����܂��B�e�[�u�����F{0} ���[�J���t�@�C�����F{1} */
    public static final String EXP_FILERECEIV = "TG-EXPORTER-02008";

    /** [INFO] ��M�����t�@�C���𐶐����܂����B�e�[�u�����F{0} ���[�J���t�@�C�����F{1} */
    public static final String EXP_FILERECEIV_SUCCESS = "TG-EXPORTER-02009";

    /** [ERROR] Export�t�@�C����LOAD���鏈���ŃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String EXP_LOADFILE_EXCEPTION = "TG-EXPORTER-03001";

    /** [INFO] �G�N�X�|�[�g�e���|�����Ǘ��e�[�u���ɍ쐬�\��̃G�N�X�|�[�g�e���|�����e�[�u���̏���o�^���܂����B�W���u�t���[SID�F{0} */
    public static final String EXP_INSERT_TEMP_INFO = "TG-EXPORTER-03002";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u�����쐬���܂����B�W���u�t���[SID�F{0} �G�N�X�|�[�g�Ώۃe�[�u�����F{1} �G�N�X�|�[�g�e���|�����e�[�u�����F{2} SQL�F{3} */
    public static final String EXP_CREATE_TEMP_TABLE = "TG-EXPORTER-03003";

    /** [INFO] �G�N�X�|�[�g����TSV�t�@�C�����G�N�X�|�[�g�e���|�����e�[�u���Ƀ��[�h���܂����B�W���u�t���[SID�F{0} �G�N�X�|�[�g�Ώۃe�[�u�����F{1} �G�N�X�|�[�g�e���|�����e�[�u�����F{2} �G�N�X�|�[�g����TSV�t�@�C���F{3} */
    public static final String EXP_TSV_FILE_LOAD = "TG-EXPORTER-03004";

    /** [INFO] �G�N�X�|�[�g�e���|�����Ǘ��e�[�u���̃X�e�[�^�X���u���[�h�����v�ɍX�V���܂����B�W���u�t���[SID�F{0} �G�N�X�|�[�g�Ώۃe�[�u�����F{1} �G�N�X�|�[�g�e���|�����e�[�u�����F{2} */
    public static final String EXP_LOAD_EXIT = "TG-EXPORTER-03005";

    /** [INFO] �S�ẴG�N�X�|�[�g����TSV�t�@�C���̃��[�h���I�����A�G�N�X�|�[�g�e���|�����Ǘ��e�[�u���̃X�e�[�^�X���u�R�s�[�J�n�O�v�ɍX�V���܂����B�W���u�t���[SID�F{0}  */
    public static final String EXP_BEFORE_COPY = "TG-EXPORTER-03006";

    /** [ERROR] Export����TSV�t�@�C���̃J������Export�Ώۃe�[�u��/�ُ�f�[�^�e�[�u���̉��ꂩ�e�[�u���Ɋ܂܂��J�����ł���K�v������܂��B�J�������F{0} */
    public static final String EXP_TSV_COLUMN_NOT_FOUND = "TG-EXPORTER-03007";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u�����쐬���܂����B�W���u�t���[SID�F{0} �G�N�X�|�[�g�e���|�����e�[�u�����F{1} �d���t���O�e�[�u�����F{2} SQL�F{3} */
    public static final String EXP_CREATE_DUPLCATE_TABLE = "TG-EXPORTER-03008";

    /** [ERROR] ���b�N����Ń��g���C�s�ȃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String EXP_RELEASE_LOCK_ERROR = "TG-EXPORTER-04001";

    /** [ERROR] ���b�N��������g���C�I�[�o�[���܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String EXP_RELEASE_LOCK_RETRY_ORVER = "TG-EXPORTER-04002";

    /** [WARN] ���b�N����Ń��g���C�\�ȃG���[���������܂����B���g���C���܂��B�G���[�����F{0} */
    public static final String EXP_RELEASE_LOCK_RETRY = "TG-EXPORTER-04003";

    /** [INFO] �e�[�u�����b�N�e�[�u����Import/Export�Ώۃe�[�u���s��TX���b�N���擾���A���b�N�擾/��������̔r��������s���܂��BSQL�F{0} */
    public static final String EXP_LOCK_EXCLUSIVE = "TG-EXPORTER-04004";

    /** [INFO] �e�[�u�����b�N���������܂��BSQL�F{0} �W���u�t���[SID�F{1} */
    public static final String EXP_TABLE_LOCK_RELEASE = "TG-EXPORTER-04005";

    /** [INFO] ���R�[�h���b�N���������܂��B���b�N�ς݃��R�[�h�̃��R�[�h�폜SQL�F{0} ���R�[�h���b�N�̃��R�[�h�폜SQL�F{1} �W���u�t���[SID�F{2} Import/Export�Ώۃe�[�u�����F{3} */
    public static final String EXP_RECORD_LOCK_RELEASE = "TG-EXPORTER-04006";

    /** [INFO] �W���u�t���[���s�e�[�u���̃��R�[�h���폜���܂��BSQL�F{0} �W���u�t���[SID�F{1} */
    public static final String EXP_DELETE_RUNNING_JOBFLOW = "TG-EXPORTER-04007";

    /** [WARN] Export�t�@�C���폜������Export�t�@�C���̍폜�Ɏ��s���܂����B�t�@�C�����F{0} */
    public static final String EXP_FILEDELETE_ERROR = "TG-EXPORTER-05001";

    /** [WARN] Export�t�@�C���폜������Export�t�@�C���i�[�f�B���N�g���̍폜�Ɏ��s���܂����B�f�B���N�g�����F{0} */
    public static final String EXP_DIRDELETE_ERROR = "TG-EXPORTER-05002";

    /** [ERROR] Exporter�Ńe���|�����e�[�u������Export�Ώۃe�[�u���ւ̃f�[�^�̃R�s�[���ɁAExport�Ώۃe�[�u���ɍX�V�Ώۂ̃��R�[�h��������܂���ł����BExport�Ώۃe�[�u�����F{0} Export�e���|�����e�[�u�����F{1} Export�Ώۃe�[�u���ɍX�V�Ώۂ�������Ȃ��������R�[�h�F{2} */
    public static final String EXP_DATA_COPY_UPDATE_ERROR = "TG-EXPORTER-06001";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u������G�N�X�|�[�g�Ώۃe�[�u���փf�[�^���R�s�[���܂��B�W���u�t���[SID�F{0} Export�Ώۃe�[�u���F{1} Export�e���|�����e�[�u���F{2} */
    public static final String EXP_COPY_START = "TG-EXPORTER-06002";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u������G�N�X�|�[�g�Ώۃe�[�u���փf�[�^���R�s�[���܂����B�W���u�t���[SID�F{0} Export�Ώۃe�[�u�����F{1} Export�e���|�����e�[�u�����F{2} �S�Ẵf�[�^���R�s�[�����F{3} */
    public static final String EXP_COPY_END = "TG-EXPORTER-06003";

    /** [INFO] ���Y�G�N�X�|�[�g�e���|�����e�[�u���͊�ɃR�s�[���I�����Ă��邽�߁A�R�s�[�͍s���܂���B�W���u�t���[SID�F{0} Export�Ώۃe�[�u���F{1} Export�e���|�����e�[�u���F{2} */
    public static final String EXP_COPY_ALREADY_ENDED = "TG-EXPORTER-06004";

    /** [INFO] �G�N�X�|�[�g�Ώۃe�[�u���ɑΉ�����G�N�X�|�[�g�e���|�����e�[�u�������݂��Ȃ����߁A�R�s�[�͍s���܂���B�W���u�t���[SID�F{0} Export�Ώۃe�[�u�����F{1} Export�e���|�����e�[�u�����F{2} */
    public static final String EXP_TEMP_TABLE_NOT_FOUND = "TG-EXPORTER-06005";

    /** [INFO] ���Y�G�N�X�|�[�g�Ώۃe�[�u���ɑ΂��郌�R�[�h���b�N�e�[�u���̃��R�[�h���C���T�[�g���܂����BSQL�F{0} �W���u�t���[SID�F{1} �G�N�X�|�[�g�Ώۃe�[�u�����F{2} */
    public static final String EXP_RECORD_LOCK = "TG-EXPORTER-06006";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u������G�N�X�|�[�g�Ώۃe�[�u���ɐV�K���R�[�h���R�s�[���܂����BExport�Ώۃe�[�u�����F{0} Export�e���|�����e�[�u�����F{1} �R�s�[SQL�F{2} ���R�[�h���b�N�擾SQL�F{3} ���R�[�h�폜SQL�F{4} */
    public static final String EXP_NEW_RECORD_COPY = "TG-EXPORTER-06007";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u������ُ�f�[�^�e�[�u���ɏd�����R�[�h���R�s�[���܂����B�ُ�f�[�^�e�[�u�����F{0} Export�e���|�����e�[�u�����F{1} �R�s�[SQL�F{2} ���R�[�h�폜SQL�F{3} */
    public static final String EXP_DUPLICATE_RECORD_COPY = "TG-EXPORTER-06008";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u������G�N�X�|�[�g�Ώۃe�[�u���ɍX�V���R�[�h���R�s�[���܂����BExport�Ώۃe�[�u�����F{0} Export�e���|�����e�[�u�����F{1} �R�s�[SQL�F{2} ���R�[�h�폜SQL�F{3} ���݂̃e���|����SID�̈ʒu�F{4} �R�s�[����e���|����SID�̍ő�l�F{5} */
    public static final String EXP_UPDATE_RECORD_COPY = "TG-EXPORTER-06009";

    /** [INFO] �S�ẴG�N�X�|�[�g�e���|�����e�[�u���̃��R�[�h���G�N�X�|�[�g�Ώۃe�[�u���ɃR�s�[���܂����BExport�Ώۃe�[�u�����F{0} Export�e���|�����e�[�u�����F{1}  */
    public static final String EXP_ALL_DATA_COPY = "TG-EXPORTER-06010";

    /** [INFO] �G�N�X�|�[�g�e���|�����Ǘ��e�[�u���̃X�e�[�^�X���u�R�s�[�����v�ɍX�V���܂����B�W���u�t���[SID�F{0} �G�N�X�|�[�g�Ώۃe�[�u�����F{1} */
    public static final String EXP_TABLE_COPY_EXIT = "TG-EXPORTER-06011";

    /** [INFO] �G�N�X�|�[�g�e���|�����Ǘ��e�[�u���̃��R�[�h���폜���܂��BSQL�F{0} �W���u�t���[SID�F{1} �e�[�u�����F{2} */
    public static final String EXP_TEMP_INFO_RECORD_DELETE = "TG-EXPORTER-07001";

    /** [INFO] �G�N�X�|�[�g�e���|�����e�[�u�����폜���܂����BSQL�F{0} */
    public static final String EXP_TEMP_TABLE_DROP = "TG-EXPORTER-07002";

    /** [INFO] ���Y�G�N�X�|�[�g�e���|�����e�[�u���̃X�e�[�^�X���u'2'�FExport�Ώۃe�[�u���Ƀf�[�^���R�s�[�����v�ȊO�̂��߁A�폜���s���܂���B�G�N�X�|�[�g�e���|�����e�[�u�����F{0} �X�e�[�^�X{1} */
    public static final String EXP_TEMP_TABLE_NOT_DROP = "TG-EXPORTER-07003";

    /** [INFO] �d���t���O�e�[�u�����폜���܂����BSQL�F{0} */
    public static final String EXP_DUP_FLG_TABLE_DROP = "TG-EXPORTER-07004";

    /** [INFO] Collector�̏������J�n���܂��B�J�n�����F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_START = "TG-COLLECTOR-01001";

    /** [INFO] Collector�̏����𐳏�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_EXIT = "TG-COLLECTOR-01002";

    /** [ERROR] Collector�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_INIT_ERROR = "TG-COLLECTOR-01003";

    /** [ERROR] Collector�ŕs���ȃG���[���������܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_EXCEPRION = "TG-COLLECTOR-01004";

    /** [ERROR] Collector�Ɏw�肷��p�����[�^���s���ł��B�s�����e�F{0} �l�F{1} */
    public static final String COL_PARAMCHECK_ERROR = "TG-COLLECTOR-01005";

    /** [ERROR] Collector�Ńp�����[�^�̓�̓`�F�b�N�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_PARAM_ERROR = "TG-COLLECTOR-01006";

    /** [ERROR] Collector��Export�t�@�C���̑��M�����Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�o�b�`ID�F{2}�A�W���u�t���[ID�F{3}�A�W���u�t���[���sID�F{4}�A���[�U�[���F{5} */
    public static final String COL_FILESEND_ERROR = "TG-COLLECTOR-01007";

    /** [INFO] HDFS���Export�t�@�C����DB�T�[�o�ɑ��M���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3}�A���[�U�[���F{4} */
    public static final String COL_FILESEND = "TG-COLLECTOR-01008";

    /** [INFO] HDFS���Export�t�@�C����DB�T�[�o�ɑ��M���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1}�A�W���u�t���[ID�F{2}�A�W���u�t���[���sID�F{3}�A���[�U�[���F{4} */
    public static final String COL_FILESEND_SUCCESS = "TG-COLLECTOR-01009";

    /** [ERROR] Export�t�@�C���𑗐M���鏈���ŃG���[���������܂����B�ُ�I�����܂��B�G���[�����F{0} */
    public static final String COL_SENDFILE_EXCEPTION = "TG-COLLECTOR-02001";

    /** [INFO] �W���u�t���[�ݒ�Ɏw�肳�ꂽ�p�X��Export�t�@�C���𑗐M���܂��BExport�Ώۃe�[�u�����F{0} HDFS�̃p�X�F{1} ZIP���k�L���F{2} Model�N���X�F{3} */
    public static final String COL_SEND_HDFSFILE = "TG-COLLECTOR-02002";

    /** [INFO] �W���u�t���[�ݒ�Ɏw�肳�ꂽ�p�X��Export�t�@�C���𑗐M���܂����BExport�Ώۃe�[�u�����F{0} HDFS�̃p�X�F{1} ZIP���k�L���F{2} Model�N���X�F{3} */
    public static final String COL_SEND_HDFSFILE_SUCCESS = "TG-COLLECTOR-02003";

    /** [INFO] Export�t�@�C���𑗐M���܂��BExport�Ώۃe�[�u�����F{0} HDFS�̃t�@�C���p�X�F{1} Export�t�@�C����ZipEntry���F{2} */
    public static final String COL_SENDFILE = "TG-COLLECTOR-02004";

    /** [INFO] Export�t�@�C���𑗐M���܂����BExport�Ώۃe�[�u�����F{0} HDFS�̃t�@�C���p�X�F{1} Export�t�@�C����ZipEntry���F{2} */
    public static final String COL_SENDFILE_SUCCESS = "TG-COLLECTOR-02005";

    /** [INFO] �W���u�t���[�ݒ�Ɏw�肳�ꂽ�p�X�ɊY������Export�t�@�C�������݂��܂���ł����BExport�Ώۃe�[�u�����F{0} HDFS�̃p�X�F{1} */
    public static final String COL_EXPORT_FILE_NOT_FOUND = "TG-COLLECTOR-02006";

    /** [INFO] �W���u�t���[�ݒ�Ɏw�肳�ꂽ�p�X��{0}����Export�t�@�C�������݂��܂����B���M���s���܂��BExport�Ώۃe�[�u�����F{1} HDFS�̃p�X�F{2} */
    public static final String COL_EXPORT_FILE_FOUND = "TG-COLLECTOR-02007";

    /** [INFO] Recoverer�̏������J�n���܂��B�J�n�����F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[���sID�F{2} */
    public static final String RCV_START = "TG-RECOVERER-01001";

    /** [INFO] Recoverer�̏������I�����܂��B�������ʁF{0}�A�I�������F{1}�A�^�[�Q�b�g���F{2}�A�W���u�t���[���sID�F{3} */
    public static final String RCV_EXIT = "TG-RECOVERER-01002";

    /** [ERROR] Recoverer�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[���sID�F{2} */
    public static final String RCV_INIT_ERROR = "TG-RECOVERER-01003";

    /** [ERROR] Recoverer�ŕs���ȃG���[���������܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[���sID�F{2} */
    public static final String RCV_EXCEPRION = "TG-RECOVERER-01004";

    /** [ERROR] Recoverer�Ɏw�肷����s���ł��B�ُ�I�����܂��B��̐��F{0}�A�I�������F{1}�A�^�[�Q�b�g���F{2}�A�W���u�t���[���sID�F{3} */
    public static final String RCV_ARGSCHECK_ERROR = "TG-RECOVERER-01005";

    /** [ERROR] Recoverer�ŃW���u�t���[���s�e�[�u���̎擾�Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0}�A�^�[�Q�b�g���F{1}�A�W���u�t���[���sID�F{2} */
    public static final String RCV_GETRUNNUNG_JOBFLOW_ERROR = "TG-RECOVERER-01006";

    /** [ERROR] Recoverer�Ɏw�肷��p�����[�^���s���ł��B�s�����e�F{0} �l�F{1} �W���u�t���[���sID�F{2} */
    public static final String RCV_PARAMCHECK_ERROR = "TG-RECOVERER-01007";

    /** [INFO] ���Y�W���u�t���[���sID�͑��v���Z�X�ɂ�菈�����̂��߁A���J�o���ΏۊO�Ƃ��܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_INSTANCE_ID_LOCK_ERROR = "TG-RECOVERER-01008";

    /** [ERROR] Recoverer�̏������J�n���ꂽ��ɁA���Y�W���u�t���[���sID�����v���Z�X�ɂ�菈������܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{3} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_INSTANCE_ID_NOT_FOUND = "TG-RECOVERER-01009";

    /** [INFO] ���Y�W���u�t���[���sID�͎��s���̂��߁A���J�o���ΏۊO�Ƃ��܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{3} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_MM_EXEC_INCTANCE = "TG-RECOVERER-01010";

    /** [ERROR] ���Y�W���u�t���[SID�ɑΉ�����e���|�����e�[�u���̏��擾�Ɏ��s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{3} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_TEMP_INFO_ERROR = "TG-RECOVERER-01011";

    /** [ERROR] �W���u�t���[�C���X�^���X�ɑ΂��郍�[���t�H���[�h��Export�e���|�����e�[�u������Export�Ώۃe�[�u���ւ̃f�[�^�̃R�s�[�Ɏ��s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_DATA_COPY_ERROR = "TG-RECOVERER-01012";

    /** [ERROR] �W���u�t���[�C���X�^���X�ɑ΂��郍�b�N�̉����Ɏ��s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_RELEASELOCK_ERROR = "TG-RECOVERER-01013";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郊�J�o�����������s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}�A�������e�F{5} */
    public static final String RCV_JOBFLOW_RECOVERY_EXIT = "TG-RECOVERER-01014";

    /** [ERROR] Export�Ώۃe�[�u���ɍX�V�Ώۂ̃��R�[�h�����݂��Ȃ����߁A�W���u�t���[�C���X�^���X�ɑ΂��郊�J�o���������s���S�ɏI�����܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}�A�������e�F{5} */
    public static final String RCV_COPY_NOT_EXIT = "TG-RECOVERER-01015";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郊�J�o���������J�n���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4} */
    public static final String RCV_INSTANCE_START = "TG-RECOVERER-01016";

    /** [INFO] �W���u�t���[���sID�ɂ��r��������s���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_INSTANCE_ID_LOCK = "TG-RECOVERER-01017";

    /** [INFO] �W���u�t���[���sID�ɂ��r������ɐ������܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_INSTANCE_ID_LOCK_SUCCESS = "TG-RECOVERER-01018";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郍�[���t�H���[�h��Export�e���|�����e�[�u������Export�Ώۃe�[�u���փf�[�^���R�s�[���܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_DATA_COPY = "TG-RECOVERER-01019";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郍�[���t�H���[�h��Export�e���|�����e�[�u������Export�Ώۃe�[�u���փf�[�^���R�s�[���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_DATA_COPY_SUCCESS = "TG-RECOVERER-01020";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郍�b�N���������܂��B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_RELEASELOCK = "TG-RECOVERER-01021";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郍�b�N�̉������s���܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}  */
    public static final String RCV_RELEASELOCK_SUCCESS = "TG-RECOVERER-01022";

    /** [INFO] �W���u�t���[�C���X�^���X�ɑ΂��郊�J�o���������e�����肵�܂����B�^�[�Q�b�g���F{0}�A�o�b�`ID�F{1} �W���u�t���[ID�F{2} �W���u�t���[SID�F{3} �W���u�t���[���sID�F{4}�A�������e�F{5} */
    public static final String RCV_JUDGE_PROCESS = "TG-RECOVERER-01023";

    /**
     * �R���X�g���N�^
     */
    private MessageIdConst() {
    }
}
