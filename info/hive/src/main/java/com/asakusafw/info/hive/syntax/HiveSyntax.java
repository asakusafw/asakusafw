/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.hive.syntax;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Syntactic utilities about Hive.
 * @see <a href="https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL">Hive DDL</a>
 * @since 0.8.1
 */
public final class HiveSyntax {

    private static final char LITERAL_ESCAPE = '\\';

    private static final Pattern PATTERN_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private HiveSyntax() {
        return;
    }

    private static final Set<String> DDL_KEYWORDS;
    static {
        String[] keywords = { "ADD", "ADMIN", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "ARCHIVE", "ARRAY", "AS",
                "ASC", "AUTHORIZATION", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BOOLEAN", "BOTH", "BUCKET", "BUCKETS",
                "BY", "CASCADE", "CASE", "CAST", "CHANGE", "CHAR", "CLUSTER", "CLUSTERED", "CLUSTERSTATUS",
                "COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMPACT", "COMPACTIONS", "COMPUTE", "CONCATENATE",
                "CONF", "CONTINUE", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_TIMESTAMP", "CURSOR",
                "DATA", "DATABASE", "DATABASES", "DATE", "DATETIME", "DAY", "DBPROPERTIES", "DECIMAL", "DEFERRED",
                "DEFINED", "DELETE", "DELIMITED", "DEPENDENCY", "DESC", "DESCRIBE", "DIRECTORIES", "DIRECTORY",
                "DISABLE", "DISTINCT", "DISTRIBUTE", "DOUBLE", "DROP", "ELEM_TYPE", "ELSE", "ENABLE", "END", "ESCAPED",
                "EXCHANGE", "EXCLUSIVE", "EXISTS", "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", "FALSE", "FETCH",
                "FIELDS", "FILE", "FILEFORMAT", "FIRST", "FLOAT", "FOLLOWING", "FOR", "FORMAT", "FORMATTED", "FROM",
                "FULL", "FUNCTION", "FUNCTIONS", "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD_DDLTIME", "HOUR",
                "IDXPROPERTIES", "IF", "IGNORE", "IMPORT", "IN", "INDEX", "INDEXES", "INNER", "INPATH", "INPUTDRIVER",
                "INPUTFORMAT", "INSERT", "INT", "INTERSECT", "INTERVAL", "INTO", "IS", "ITEMS", "JAR", "JOIN", "KEYS",
                "KEY_TYPE", "LATERAL", "LEFT", "LESS", "LIKE", "LIMIT", "LINES", "LOAD", "LOCAL", "LOCATION", "LOCK",
                "LOCKS", "LOGICAL", "LONG", "MACRO", "MAP", "MAPJOIN", "MATERIALIZED", "MINUS", "MINUTE", "MONTH",
                "MORE", "MSCK", "NONE", "NOSCAN", "NOT", "NO_DROP", "NULL", "OF", "OFFLINE", "ON", "OPTION", "OR",
                "ORDER", "OUT", "OUTER", "OUTPUTDRIVER", "OUTPUTFORMAT", "OVER", "OVERWRITE", "OWNER", "PARTIALSCAN",
                "PARTITION", "PARTITIONED", "PARTITIONS", "PERCENT", "PLUS", "PRECEDING", "PRESERVE", "PRETTY",
                "PRINCIPALS", "PROCEDURE", "PROTECTION", "PURGE", "RANGE", "READ", "READONLY", "READS", "REBUILD",
                "RECORDREADER", "RECORDWRITER", "REDUCE", "REGEXP", "RELOAD", "RENAME", "REPAIR", "REPLACE", "RESTRICT",
                "REVOKE", "REWRITE", "RIGHT", "RLIKE", "ROLE", "ROLES", "ROLLUP", "ROW", "ROWS", "SCHEMA", "SCHEMAS",
                "SECOND", "SELECT", "SEMI", "SERDE", "SERDEPROPERTIES", "SERVER", "SET", "SETS", "SHARED", "SHOW",
                "SHOW_DATABASE", "SKEWED", "SMALLINT", "SORT", "SORTED", "SSL", "STATISTICS", "STORED", "STREAMTABLE",
                "STRING", "STRUCT", "TABLE", "TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED",
                "THEN", "TIMESTAMP", "TINYINT", "TO", "TOUCH", "TRANSACTIONS", "TRANSFORM", "TRIGGER", "TRUE",
                "TRUNCATE", "UNARCHIVE", "UNBOUNDED", "UNDO", "UNION", "UNIONTYPE", "UNIQUEJOIN", "UNLOCK", "UNSET",
                "UNSIGNED", "UPDATE", "URI", "USE", "USER", "USING", "UTC", "UTCTIMESTAMP", "VALUES", "VALUE_TYPE",
                "VARCHAR", "VIEW", "WHEN", "WHERE", "WHILE", "WINDOW", "WITH", "YEAR", };
        Set<String> s = new HashSet<>(keywords.length * 2);
        Collections.addAll(s, keywords);
        DDL_KEYWORDS = Collections.unmodifiableSet(s);
    }

    /**
     * Returns whether the identifier is a DDL keyword or not.
     * @param identifier the target identifier
     * @return {@code true} if the identifier is one of DDL keyword, otherwise {@code false}
     */
    public static boolean isDdlKeyword(String identifier) {
        return DDL_KEYWORDS.contains(identifier.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Returns the valid identifier token in Hive DDL.
     * @param identifier the bare identifier
     * @return the valid identifier token
     */
    public static String quoteIdentifier(String identifier) {
        if (isDdlKeyword(identifier) || PATTERN_IDENTIFIER.matcher(identifier).matches() == false) {
            // see https://issues.apache.org/jira/secure/attachment/12618321/QuotedIdentifier.html
            // the identifier must be a 'RegexComponent', but we does not check it here
            return "`" + identifier + "`";
        } else {
            return identifier;
        }
    }

    /**
     * Returns the quoted string literal.
     * @param quote the quote character
     * @param string the target string
     * @return the quoted string literal
     */
    public static String quoteLiteral(char quote, String string) {
        StringBuilder buf = new StringBuilder();
        buf.append(quote);
        for (int i = 0, n = string.length(); i < n; i++) {
            char c = string.charAt(i);
            if (c == quote || c == LITERAL_ESCAPE) {
                buf.append(LITERAL_ESCAPE);
                buf.append(c);
            } else if (Character.isISOControl(c)) {
                buf.append(String.format("\\%03o", (int) c)); //$NON-NLS-1$
            } else {
                buf.append(c);
            }
        }
        buf.append(quote);
        return buf.toString();
    }
}
