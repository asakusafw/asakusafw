/***************************************************/
/* src/test/example-dataset ディレクトリを /tmp に */
/* コピーしてからこのSQLを実行してください。       */
/*                                                 */
/* ex.                                             */
/* cp -r src/test/example-dataset /tmp             */
/* mysql < src/test/sql/import-example-dataset.sql */
/***************************************************/
TRUNCATE TABLE SALES_DETAIL;
LOAD DATA
 INFILE '/tmp/example-dataset/sales/2011-04-01.csv'
 INTO TABLE SALES_DETAIL
 FIELDS TERMINATED BY ','
 IGNORE 1 LINES 
 (
    SALES_DATE_TIME
    ,STORE_CODE
    ,ITEM_CODE
    ,AMOUNT
    ,UNIT_SELLING_PRICE
    ,SELLING_PRICE
 );

TRUNCATE TABLE STORE_INFO;
LOAD DATA
 INFILE '/tmp/example-dataset/master/store_info.csv'
 INTO TABLE STORE_INFO
 FIELDS TERMINATED BY ','
 IGNORE 1 LINES
 (
    STORE_CODE
    ,STORE_NAME
 ); 

TRUNCATE TABLE ITEM_INFO;
LOAD DATA
 INFILE '/tmp/example-dataset/master/item_info.csv'
 INTO TABLE ITEM_INFO
 FIELDS TERMINATED BY ','
 IGNORE 1 LINES
 (
    ITEM_CODE
    ,ITEM_NAME
    ,DEPARTMENT_CODE
    ,DEPARTMENT_NAME
    ,CATEGORY_CODE
    ,CATEGORY_NAME
    ,UNIT_SELLING_PRICE
    ,REGISTERED_DATE
    ,BEGIN_DATE
    ,END_DATE
 ); 
