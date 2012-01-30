/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package ${package}.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ${package}.modelgen.dmdl.model.ItemInfo;
import ${package}.modelgen.dmdl.model.SalesDetail;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * {@link CategorySummaryOperator}のテスト。
 */
public class CategorySummaryOperatorTest {

    /**
     * {@link CategorySummaryOperator#selectAvailableItem(List, SalesDetail)}のテスト。
     */
    @Test
    public void selectAvailableItem() {
        List<ItemInfo> candidates = new ArrayList<ItemInfo>();
        candidates.add(item("A", 1, 10));
        candidates.add(item("B", 11, 20));
        candidates.add(item("C", 21, 30));

        CategorySummaryOperator operator = new CategorySummaryOperatorImpl();
        ItemInfo item1 = operator.selectAvailableItem(candidates, sales(1));
        ItemInfo item5 = operator.selectAvailableItem(candidates, sales(5));
        ItemInfo item10 = operator.selectAvailableItem(candidates, sales(10));
        ItemInfo item15 = operator.selectAvailableItem(candidates, sales(11));
        ItemInfo item20 = operator.selectAvailableItem(candidates, sales(20));
        ItemInfo item30 = operator.selectAvailableItem(candidates, sales(30));
        ItemInfo item31 = operator.selectAvailableItem(candidates, sales(31));

        assertThat(item1.getCategoryCodeAsString(), is("A"));
        assertThat(item5.getCategoryCodeAsString(), is("A"));
        assertThat(item10.getCategoryCodeAsString(), is("A"));
        assertThat(item15.getCategoryCodeAsString(), is("B"));
        assertThat(item20.getCategoryCodeAsString(), is("B"));
        assertThat(item30.getCategoryCodeAsString(), is("C"));
        assertThat(item31, is(nullValue()));
    }

    private SalesDetail sales(int day) {
        SalesDetail object = new SalesDetail();
        object.setSalesDateTime(new DateTime(2011, 1, day, 0, 0, 0));
        return object;
    }

    private ItemInfo item(String categoryCode, int begin, int end) {
        ItemInfo object = new ItemInfo();
        object.setCategoryCodeAsString(categoryCode);
        object.setBeginDate(new Date(2011, 1, begin));
        object.setEndDate(new Date(2011, 1, end));
        return object;
    }
}
