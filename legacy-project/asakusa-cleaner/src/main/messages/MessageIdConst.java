/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.cleaner.common;

/**
 * ���O�o�͂Ŏg�p����郁�b�Z�[�WID���ꌳ�Ǘ����邽�߂̃N���X�ł��B
 * ���̃N���X�̓��b�Z�[�W�t�@�C���ɂ�莩���������ꂽ���̂Ȃ̂ŁA���ڕҏW���Ȃ��ł��������B
 *
 * @author logmessage.xls
 * @version $Id$
 */
public final class MessageIdConst {

    /** [ERROR] �v���p�e�B�t�@�C���̐ݒ�l���s���ł��B�s�����e�F{0} */
    public static final String CMN_PROP_CHECK_ERROR = "CL-COMMON-00001";

    /** [INFO] LocalFileCleaner�̏������J�n���܂��B�J�n�����F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String LCLN_START = "CL-LOCALCLEAN-01001";

    /** [INFO] LocalFileCleaner�̏����𐳏�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String LCLN_EXIT_SUCCESS = "CL-LOCALCLEAN-01002";

    /** [INFO] LocalFileCleaner�̏������x���I�����܂��B�ꕔ�f�B���N�g�����̓t�@�C���̍폜�Ɏ��s���܂����B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String LCLN_EXIT_WARNING = "CL-LOCALCLEAN-01003";

    /** [ERROR] LocalFileCleaner�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String LCLN_INIT_ERROR = "CL-LOCALCLEAN-01004";

    /** [ERROR] LocalFileCleaner�ŕs���ȃG���[���������܂����B�ُ�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String LCLN_EXCEPRION = "CL-LOCALCLEAN-01005";

    /** [ERROR] LocalFileCleaner�Ɏw�肷��p�����[�^���s���ł��B�ُ�I�����܂��B�s�����e�F{0} �l�F{1} �I�������F{2} ���샂�[�h�F{3} �v���p�e�B�F{4} */
    public static final String LCLN_PARAMCHECK_ERROR = "CL-LOCALCLEAN-01006";

    /** [INFO] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����ɐ������܂����B�N���[�j���O�Ώۃf�B���N�g���F{0} �ێ���ԁF{1} ���샂�[�h�F{2} */
    public static final String LCLN_CLEN_DIR_SUCCESS = "CL-LOCALCLEAN-01007";

    /** [WARN] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����Ɏ��s���܂����B�N���[�j���O�Ώۃf�B���N�g���F{0} �ێ���ԁF{1} ���샂�[�h�F{2} */
    public static final String LCLN_CLEN_DIR_FAIL = "CL-LOCALCLEAN-01008";

    /** [ERROR] LocalFileCleaner�Ɏw�肷��N���[�j���O�Ώۃf�B���N�g�����s���ł��B�s�����e�F{0} �l�F{1} */
    public static final String LCLN_CLEN_DIR_ERROR = "CL-LOCALCLEAN-01009";

    /** [WARN] LocalFileCleaner�ō폜�����Ɏ��s���܂����B�폜�ΏہF{0} �p�X�F{1} */
    public static final String LCLN_CLEN_FAIL = "CL-LOCALCLEAN-01010";

    /** [INFO] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����{���܂��B�N���[�j���O�Ώۃf�B���N�g���F{0} �폜�p�^�[���F{1} �ێ���ԁF{2} ���샂�[�h�F{3} �N���[�j���O�J�n�����F{4} */
    public static final String LCLN_CLEN_FILE = "CL-LOCALCLEAN-01011";

    /** [DEBUG] �t�@�C�����N���[�j���O���܂��B�f�B���N�g���F{0} */
    public static final String LCLN_FILE_DELETE = "CL-LOCALCLEAN-01012";

    /** [DEBUG] �f�B���N�g�������N���[�j���O���܂����B�f�B���N�g���F{0} �f�B���N�g���폜�����F{1} �t�@�C���폜�����F{2} */
    public static final String LCLN_FILE_DELETE_SUCCESS = "CL-LOCALCLEAN-01013";

    /** [DEBUG] �f�B���N�g�����폜���܂����B�f�B���N�g���F{0} */
    public static final String LCLN_DIR_DELETE = "CL-LOCALCLEAN-01014";

    /** [ERROR] �t�@�C���̍폜�w��p�^�[���̐��K�\�����s���ł��B�p�^�[���F{0} */
    public static final String LCLN_PATTERN_FAIL = "CL-LOCALCLEAN-01015";

    /** [DEBUG] �t�@�C�����폜���܂����B�t�@�C���F{0} */
    public static final String LCLN_DELETE_FILE = "CL-LOCALCLEAN-01016";

    /** [ERROR] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���폜�p�^�[�����w�肳��Ă��܂���B�N���[�j���O�Ώۃf�B���N�g����key�F{0} �N���[�j���O�Ώۃf�B���N�g���F{1} �폜�p�^�[����key�F{2} */
    public static final String LCLN_PATTERN_NOT_FOUND = "CL-LOCALCLEAN-01017";

    /** [INFO] HDFSCleaner�̏������J�n���܂��B�J�n�����F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String HCLN_START = "CL-HDFSCLEAN-01001";

    /** [INFO] HDFSCleaner�̏����𐳏�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String HCLN_EXIT_SUCCESS = "CL-HDFSCLEAN-01002";

    /** [INFO] HDFSCleaner�̏������x���I�����܂��B�ꕔ�f�B���N�g�����̓t�@�C���̍폜�Ɏ��s���܂����B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String HCLN_EXIT_WARNING = "CL-HDFSCLEAN-01003";

    /** [ERROR] HDFSCleaner�ŏ������Ɏ��s���܂����B�ُ�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String HCLN_INIT_ERROR = "CL-HDFSCLEAN-01004";

    /** [ERROR] HDFSCleaner�ŕs���ȃG���[���������܂����B�ُ�I�����܂��B�I�������F{0} ���샂�[�h�F{1} �v���p�e�B�F{2} */
    public static final String HCLN_EXCEPRION = "CL-HDFSCLEAN-01005";

    /** [ERROR] HDFSCleaner�Ɏw�肷��p�����[�^���s���ł��B�ُ�I�����܂��B�s�����e�F{0} �l�F{1} �I�������F{2} ���샂�[�h�F{3} �v���p�e�B�F{4} */
    public static final String HCLN_PARAMCHECK_ERROR = "CL-HDFSCLEAN-01006";

    /** [INFO] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����ɐ������܂����B�N���[�j���O�Ώۃf�B���N�g���F{0} �ێ���ԁF{1} ���샂�[�h�F{2} */
    public static final String HCLN_CLEN_DIR_SUCCESS = "CL-HDFSCLEAN-01007";

    /** [WARN] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����Ɏ��s���܂����B�N���[�j���O�Ώۃf�B���N�g���F{0} �ێ���ԁF{1} ���샂�[�h�F{2} */
    public static final String HCLN_CLEN_DIR_FAIL = "CL-HDFSCLEAN-01008";

    /** [ERROR] HDFSCleaner�Ɏw�肷��N���[�j���O�Ώۃf�B���N�g�����s���ł��B�s�����e�F{0} �l�F{1} */
    public static final String HCLN_CLEN_DIR_ERROR = "CL-HDFSCLEAN-01009";

    /** [WARN] HDFSCleaner�ō폜�����Ɏ��s���܂����B�폜�ΏہF{0} �p�X�F{1} */
    public static final String HCLN_CLEN_FAIL = "CL-HDFSCLEAN-01010";

    /** [ERROR] HDFSCleaner�ŃN���[�j���O��������IO��O���������܂����B�폜�Ώۃf�B���N�g���F{0} */
    public static final String HCLN_CLEN_DIR_EXCEPTION = "CL-HDFSCLEAN-01011";

    /** [INFO] �N���[�j���O�Ώۃf�B���N�g�����������̂��߁A�N���[�j���O�ΏۊO�Ƃ��܂��B�N���[�j���O�Ώۃf�B���N�g���F{0} */
    public static final String HCLN_CLEN_DIR_EXEC = "CL-HDFSCLEAN-01012";

    /** [INFO] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���N���[�j���O�����{���܂��B�N���[�j���O�Ώۃf�B���N�g���F{0} �폜�p�^�[���F{1} �ێ���ԁF{2} ���샂�[�h�F{3} �W���u�t���[�C���X�^���X���s���̖₢���킹���s�����F{4} �N���[�j���O�J�n�����F{5} */
    public static final String HCLN_CLEN_FILE = "CL-HDFSCLEAN-01013";

    /** [DEBUG] �t�@�C�����N���[�j���O���܂��B�f�B���N�g���F{0} */
    public static final String HCLN_FILE_DELETE = "CL-HDFSCLEAN-01014";

    /** [DEBUG] �f�B���N�g�������N���[�j���O���܂����B�f�B���N�g���F{0} �f�B���N�g���폜�����F{1} �t�@�C���폜�����F{2} */
    public static final String HCLN_FILE_DELETE_SUCCESS = "CL-HDFSCLEAN-01015";

    /** [DEBUG] �f�B���N�g�����폜���܂����B�f�B���N�g���F{0} */
    public static final String HCLN_DIR_DELETE = "CL-HDFSCLEAN-01016";

    /** [ERROR] �t�@�C���̍폜�w��p�^�[���̐��K�\�����s���ł��B�p�^�[���F{0} */
    public static final String HCLN_PATTERN_FAIL = "CL-HDFSCLEAN-01017";

    /** [DEBUG] �t�@�C�����폜���܂����B�t�@�C���F{0} */
    public static final String HCLN_DELETE_FILE = "CL-HDFSCLEAN-01018";

    /** [ERROR] �N���[�j���O�Ώۃf�B���N�g���ɑ΂���폜�p�^�[�����w�肳��Ă��܂���B�N���[�j���O�Ώۃf�B���N�g����key�F{0} �N���[�j���O�Ώۃf�B���N�g���F{1} �폜�p�^�[����key�F{2} */
    public static final String HCLN_PATTERN_NOT_FOUND = "CL-HDFSCLEAN-01019";

    /**
     * �R���X�g���N�^
     */
    private MessageIdConst() {
    }
}
