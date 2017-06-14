package yandex.com.mds.hw.notes.synchronizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import yandex.com.mds.hw.models.Note;

import static junit.framework.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NoteSynchronizerTest {
    Note a, b;

    boolean result;

    static Calendar calendar = Calendar.getInstance();

    static Date now;
    static Date nowWithoutMilliseconds;
    static Date after = calendar.getTime();

    static {
        calendar.set(Calendar.MILLISECOND, 496);
        now = calendar.getTime();
        calendar.set(Calendar.MILLISECOND, 0);
        nowWithoutMilliseconds = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        after = calendar.getTime();
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //empty
                {new Note(), new Note(), true},
                //id cases
                {new Note() {{
                    setId(2);
                }}, new Note() {{
                    setId(3);
                }}, true},
                {new Note() {{
                    setId(1);
                }}, new Note() {{
                    setId(1);
                }}, true},
                //color cases
                {new Note() {{
                    setColor(100);
                }}, new Note() {{
                    setColor(-1);
                }}, false},
                {new Note() {{
                    setColor(-1);
                }}, new Note() {{
                    setColor(-1);
                }}, true},
                // title cases
                {new Note() {{
                    setTitle("title");
                }}, new Note() {{
                    setTitle("title");
                }}, true},
                {new Note() {{
                    setTitle("title");
                }}, new Note() {{
                    setTitle("title2");
                }}, false},
                {new Note() {{
                    setTitle("");
                }}, new Note() {{
                    setTitle(null);
                }}, true},
                {new Note() {{
                    setTitle(null);
                }}, new Note() {{
                    setTitle(null);
                }}, true},
                //description cases
                {new Note() {{
                    setDescription("description");
                }}, new Note() {{
                    setDescription("description");
                }}, true},
                {new Note() {{
                    setDescription("description");
                }}, new Note() {{
                    setDescription("description2");
                }}, false},
                {new Note() {{
                    setDescription("");
                }}, new Note() {{
                    setDescription(null);
                }}, true},
                {new Note() {{
                    setDescription(null);
                }}, new Note() {{
                    setDescription(null);
                }}, true},
                // creation date cases
                {new Note() {{
                    setCreationDate(now);
                }}, new Note() {{
                    setCreationDate(now);
                }}, true},
                {new Note() {{
                    setCreationDate(now);
                }}, new Note() {{
                    setCreationDate(nowWithoutMilliseconds);
                }}, true},
                {new Note() {{
                    setCreationDate(now);
                }}, new Note() {{
                    setCreationDate(after);
                }}, false},
                {new Note() {{
                    setCreationDate(now);
                }}, new Note() {{
                    setCreationDate(null);
                }}, false},
                {new Note() {{
                    setCreationDate(null);
                }}, new Note() {{
                    setCreationDate(null);
                }}, true},
                // edit date cases
                {new Note() {{
                    setLastModificationDate(now);
                }}, new Note() {{
                    setLastModificationDate(now);
                }}, true},
                {new Note() {{
                    setLastModificationDate(now);
                }}, new Note() {{
                    setLastModificationDate(nowWithoutMilliseconds);
                }}, true},
                {new Note() {{
                    setLastModificationDate(now);
                }}, new Note() {{
                    setLastModificationDate(after);
                }}, false},
                {new Note() {{
                    setLastModificationDate(now);
                }}, new Note() {{
                    setLastModificationDate(null);
                }}, false},
                {new Note() {{
                    setLastModificationDate(null);
                }}, new Note() {{
                    setLastModificationDate(null);
                }}, true},
                // image url cases
                {new Note() {{
                    setImageUrl("image.jpg");
                }}, new Note() {{
                    setImageUrl("image.jpg");
                }}, true},
                {new Note() {{
                    setImageUrl("image.jpg");
                }}, new Note() {{
                    setImageUrl("image.jpg2");
                }}, false},
                {new Note() {{
                    setImageUrl("");
                }}, new Note() {{
                    setImageUrl(null);
                }}, true},
                {new Note() {{
                    setImageUrl(null);
                }}, new Note() {{
                    setImageUrl(null);
                }}, true}
        });
    }

    public NoteSynchronizerTest(Note a, Note b, boolean result) {
        this.a = a;
        this.b = b;
        this.result = result;
    }

    @Test
    public void testIsSame() throws Exception {
        assertEquals(result, NoteSynchronizer.isSame(a, b));
    }
}
