/**********************************/
/* テーブル名: 売上明細           */
/**********************************/
DROP TABLE IF EXISTS SALES_DETAIL;
CREATE TABLE SALES_DETAIL(
    -- System columns for BulkLoader
    SID               BIGINT     PRIMARY KEY AUTO_INCREMENT,
    VERSION_NO BIGINT            NULL,
    RGST_DATETIME DATETIME           NULL,
    UPDT_DATETIME DATETIME           NULL,
    
    -- Application columns
    SALES_DATE_TIME     DATETIME NOT NULL,
    STORE_CODE          VARCHAR(50) NOT NULL,
    ITEM_CODE           VARCHAR(50) NOT NULL,
    AMOUNT              BIGINT NOT NULL,
    UNIT_SELLING_PRICE  BIGINT NULL,
    SELLING_PRICE       BIGINT NULL
) ENGINE=InnoDB;

/**********************************/
/* テーブル名: 店舗マスタ         */
/**********************************/
DROP TABLE IF EXISTS STORE_INFO;
CREATE TABLE STORE_INFO(
    -- System columns for BulkLoader
    SID               BIGINT     PRIMARY KEY AUTO_INCREMENT,
    VERSION_NO BIGINT            NULL,
    RGST_DATETIME DATETIME           NULL,
    UPDT_DATETIME DATETIME           NULL,
    
    -- Application columns
    STORE_CODE          VARCHAR(50) NOT NULL,
    STORE_NAME          VARCHAR(50) NULL
) ENGINE=InnoDB;

/**********************************/
/* テーブル名: 商品マスタ         */
/**********************************/
DROP TABLE IF EXISTS ITEM_INFO;
CREATE TABLE ITEM_INFO(
    -- System columns for BulkLoader
    SID               BIGINT     PRIMARY KEY AUTO_INCREMENT,
    VERSION_NO BIGINT            NULL,
    RGST_DATETIME DATETIME           NULL,
    UPDT_DATETIME DATETIME           NULL,

    -- Application columns
    ITEM_CODE           VARCHAR(50) NOT NULL,
    ITEM_NAME           VARCHAR(50) NULL,
    DEPARTMENT_CODE     VARCHAR(50) NULL,
    DEPARTMENT_NAME     VARCHAR(50) NULL,
    CATEGORY_CODE       VARCHAR(50) NULL,
    CATEGORY_NAME       VARCHAR(50) NULL,
    UNIT_SELLING_PRICE  BIGINT NULL,
    REGISTERED_DATE     DATE NULL,
    BEGIN_DATE          DATE NULL,
    END_DATE            DATE NULL
) ENGINE=InnoDB;

/**********************************/
/* テーブル名: カテゴリ別売上集計 */
/**********************************/
DROP TABLE IF EXISTS CATEGORY_SUMMARY;
CREATE TABLE CATEGORY_SUMMARY(
    -- System columns for BulkLoader
    SID               BIGINT     PRIMARY KEY AUTO_INCREMENT,
    VERSION_NO BIGINT            NULL,
    RGST_DATETIME DATETIME           NULL,
    UPDT_DATETIME DATETIME           NULL,

    -- Application columns
    CATEGORY_CODE       VARCHAR(50) NULL,
    AMOUNT_TOTAL              BIGINT NOT NULL,
    SELLING_PRICE_TOTAL       BIGINT NULL
) ENGINE=InnoDB;

/**********************************/
/* テーブル名: エラー情報         */
/**********************************/
DROP TABLE IF EXISTS ERROR_RECORD;
CREATE TABLE ERROR_RECORD(
    -- System columns for BulkLoader
    SID               BIGINT     PRIMARY KEY AUTO_INCREMENT,
    VERSION_NO BIGINT            NULL,
    RGST_DATETIME DATETIME           NULL,
    UPDT_DATETIME DATETIME           NULL,

    -- Application columns
    SALES_DATE_TIME     DATETIME NOT NULL,
    STORE_CODE          VARCHAR(50) NOT NULL,
    ITEM_CODE           VARCHAR(50) NOT NULL,
    MESSAGE             VARCHAR(1024) NULL
) ENGINE=InnoDB;
