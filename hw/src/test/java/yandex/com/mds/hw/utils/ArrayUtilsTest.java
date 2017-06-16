package yandex.com.mds.hw.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ArrayUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private String[] a, b, result;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new String[]{}, new String[]{}, new String[]{}},
                {null, null, null},
                {null, new String[]{}, null},
                {new String[]{}, null, null},
                {new String[]{}, new String[]{"3", "4"}, new String[]{"3", "4"}},
                {new String[]{"1", "2"}, new String[]{}, new String[]{"1", "2"}},
                {new String[]{"1", "2"}, new String[]{"3", "4"}, new String[]{"1", "2", "3", "4"}},
        });
    }

    public ArrayUtilsTest(String[] a, String[] b, String[] result) {
        this.a = a;
        this.b = b;
        this.result = result;
    }

    @Test
    public void testConcatStringArrays() throws Exception {
        if (a == null || b == null) {
            expectedException.expect(NullPointerException.class);
            ArrayUtils.concatStringArrays(a, b);
        } else
            assertTrue(Arrays.equals(ArrayUtils.concatStringArrays(a, b), result));
    }
}
