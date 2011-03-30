create database __asakusa_utest_thundergate;
GRANT ALL PRIVILEGES ON *.* TO __asakusa_ut_tg IDENTIFIED BY '__asakusa_ut_tg' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO __asakusa_ut_tg@"localhost" IDENTIFIED BY "__asakusa_ut_tg" WITH GRANT OPTION;
