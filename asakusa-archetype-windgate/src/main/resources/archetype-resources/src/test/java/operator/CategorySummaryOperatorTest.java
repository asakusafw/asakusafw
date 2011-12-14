#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package}.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ${package}.modelgen.dmdl.model.ItemInfo;
import ${package}.modelgen.dmdl.model.SalesDetail;
import com.asakusafw.runtime.value.DateTime;

/**
 * {@link CategorySummaryOperator}のテスト。
 */
public class CategorySummaryOperatorTest {

    /**
     * {@link CategorySummaryOperator${symbol_pound}selectAvailableItem(List, SalesDetail)}のテスト。
     */
    @Test
    public void selectAvailableItem() {
        List<ItemInfo> candidates = new ArrayList<ItemInfo>();
        candidates.add(item("A", 0, 10));
        candidates.add(item("B", 10, 20));
        candidates.add(item("C", 20, 30));

        CategorySummaryOperator operator = new CategorySummaryOperatorImpl();
        ItemInfo item0 = operator.selectAvailableItem(candidates, sales(0));
        ItemInfo item5 = operator.selectAvailableItem(candidates, sales(5));
        ItemInfo item10 = operator.selectAvailableItem(candidates, sales(10));
        ItemInfo item15 = operator.selectAvailableItem(candidates, sales(15));
        ItemInfo item20 = operator.selectAvailableItem(candidates, sales(20));
        ItemInfo item30 = operator.selectAvailableItem(candidates, sales(30));

        assertThat(item0.getCategoryNameAsString(), is("A"));
        assertThat(item5.getCategoryNameAsString(), is("A"));
        assertThat(item10.getCategoryNameAsString(), is("B"));
        assertThat(item15.getCategoryNameAsString(), is("B"));
        assertThat(item20.getCategoryNameAsString(), is("C"));
        assertThat(item30, is(nullValue()));
    }

    private SalesDetail sales(int seconds) {
        SalesDetail object = new SalesDetail();
        object.setSalesDateTime(new DateTime(2011, 3, 31, 0, 0, seconds));
        return object;
    }

    private ItemInfo item(String categoryName, int begin, int end) {
        ItemInfo object = new ItemInfo();
        object.setCategoryNameAsString(categoryName);
        object.setBeginDateTime(new DateTime(2011, 3, 31, 0, 0, begin));
        object.setEndDateTime(new DateTime(2011, 3, 31, 0, 0, end));
        return object;
    }
}
