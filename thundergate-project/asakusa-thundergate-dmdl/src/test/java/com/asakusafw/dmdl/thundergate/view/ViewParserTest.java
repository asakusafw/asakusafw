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
package com.asakusafw.dmdl.thundergate.view;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.dmdl.thundergate.view.model.CreateView;
import com.asakusafw.dmdl.thundergate.view.model.From;
import com.asakusafw.dmdl.thundergate.view.model.Join;
import com.asakusafw.dmdl.thundergate.view.model.Name;
import com.asakusafw.dmdl.thundergate.view.model.On;
import com.asakusafw.dmdl.thundergate.view.model.Select;

/**
 * Test for {@link ViewParser}.
 */
public class ViewParserTest {

    /**
     * 結合モデルの確認。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parseJoin() throws Exception {
        ViewDefinition def = def(
                "select" +
                "  `test`.`t1`.`pk` AS `pk`," +
                "  `test`.`t1`.`val1` AS `val1`," +
                "  `test`.`t2`.`val2` AS `val2`" +
                "from `test`.`t1`" +
                "join `test`.`t2`" +
                "where" +
                "  (`test`.`t1`.`pk` = `test`.`t2`.`pk`)");

        CreateView model = ViewParser.parse(def);
        assertThat(model, is(new CreateView(
                n("test"),
                Arrays.asList(new Select[] {
                        new Select(n("t1.pk"), Aggregator.IDENT, n("pk")),
                        new Select(n("t1.val1"), Aggregator.IDENT, n("val1")),
                        new Select(n("t2.val2"), Aggregator.IDENT, n("val2")),
                }),
                new From(
                        n("t1"),
                        null,
                        new Join(
                                n("t2"),
                                null,
                                Arrays.asList(new On(n("t1.pk"), n("t2.pk"))))),
                Arrays.<Name>asList())));
    }

    /**
     * エイリアス指定付きの結合モデルの確認。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parseJoin_withAlias() throws Exception {
        ViewDefinition def = def(
                "select" +
                "  `a`.`pk` AS `pk`," +
                "  `a`.`val1` AS `val1`," +
                "  `b`.`val2` AS `val2`" +
                "from `test`.`t1` `a`" +
                "join `test`.`t2` `b`" +
                "where" +
                "  (`test`.`a`.`pk` = `test`.`b`.`pk`)");

        CreateView model = ViewParser.parse(def);
        assertThat(model, is(new CreateView(
                n("test"),
                Arrays.asList(new Select[] {
                        new Select(n("a.pk"), Aggregator.IDENT, n("pk")),
                        new Select(n("a.val1"), Aggregator.IDENT, n("val1")),
                        new Select(n("b.val2"), Aggregator.IDENT, n("val2")),
                }),
                new From(
                        n("t1"),
                        "a",
                        new Join(
                                n("t2"),
                                "b",
                                Arrays.asList(new On(n("a.pk"), n("b.pk"))))),
                Arrays.<Name>asList())));
    }

    /**
     * 集計モデルの確認。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parseSummarize() throws Exception {
        ViewDefinition def = def(
                "select" +
                "  `test`.`t1`.`pk` AS `pk`," +
                "  count(`test`.`t1`.`pk`) AS `count`," +
                "  max(`test`.`t1`.`pk`) AS `max`," +
                "  sum(`test`.`t1`.`pk`) AS `sum`" +
                "from" +
                "  `test`.`t1`" +
                "group by" +
                "  `test`.`t1`.`pk`");

        CreateView model = ViewParser.parse(def);
        assertThat(model, is(new CreateView(
                n("test"),
                Arrays.asList(new Select[] {
                        new Select(n("t1.pk"), Aggregator.IDENT, n("pk")),
                        new Select(n("t1.pk"), Aggregator.COUNT, n("count")),
                        new Select(n("t1.pk"), Aggregator.MAX, n("max")),
                        new Select(n("t1.pk"), Aggregator.SUM, n("sum")),
                }),
                new From(
                        n("t1"),
                        null,
                        null),
                Arrays.<Name>asList(n("t1.pk")))));
    }

    /**
     * 集計モデルの確認。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parseSummarize_withAlias() throws Exception {
        ViewDefinition def = def(
                "select" +
                "  `test`.`t1`.`pk` AS `pk`," +
                "  count(`test`.`t1`.`pk`) AS `count`," +
                "  max(`test`.`t1`.`pk`) AS `max`," +
                "  sum(`test`.`t1`.`pk`) AS `sum`" +
                "from" +
                "  `test`.`x` `t1`" +
                "group by" +
                "  `test`.`t1`.`pk`");

        CreateView model = ViewParser.parse(def);
        assertThat(model, is(new CreateView(
                n("test"),
                Arrays.asList(new Select[] {
                        new Select(n("t1.pk"), Aggregator.IDENT, n("pk")),
                        new Select(n("t1.pk"), Aggregator.COUNT, n("count")),
                        new Select(n("t1.pk"), Aggregator.MAX, n("max")),
                        new Select(n("t1.pk"), Aggregator.SUM, n("sum")),
                }),
                new From(
                        n("x"),
                        "t1",
                        null),
                Arrays.<Name>asList(n("t1.pk")))));
    }

    /**
     * 集計モデルの確認。
     * @throws Exception 例外が発生した場合
     */
    @Test
    public void parseSummarize_multiGroupCoulmns() throws Exception {
        ViewDefinition def = def(
                "select" +
                "  `test`.`t1`.`pk` AS `pk`," +
                "  count(`test`.`t1`.`pk`) AS `count`," +
                "  max(`test`.`t1`.`pk`) AS `max`," +
                "  sum(`test`.`t1`.`pk`) AS `sum`" +
                "from" +
                "  `test`.`t1`" +
                "group by" +
                "  `test`.`t1`.`val1` , `test`.`t1`.`val2`");

        CreateView model = ViewParser.parse(def);
        assertThat(model, is(new CreateView(
                n("test"),
                Arrays.asList(new Select[] {
                        new Select(n("t1.pk"), Aggregator.IDENT, n("pk")),
                        new Select(n("t1.pk"), Aggregator.COUNT, n("count")),
                        new Select(n("t1.pk"), Aggregator.MAX, n("max")),
                        new Select(n("t1.pk"), Aggregator.SUM, n("sum")),
                }),
                new From(
                        n("t1"),
                        null,
                        null),
                Arrays.<Name>asList(n("t1.val1"), n("t1.val2")))));
    }

    private Name n(String name) {
        return new Name(name);
    }

    private ViewDefinition def(String statement) {
        return new ViewDefinition("test", statement);
    }
}
