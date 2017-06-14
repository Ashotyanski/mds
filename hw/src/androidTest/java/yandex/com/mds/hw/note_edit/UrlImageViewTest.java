package yandex.com.mds.hw.note_edit;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import yandex.com.mds.hw.MainActivity;
import yandex.com.mds.hw.R;
import yandex.com.mds.hw.TestUtils;
import yandex.com.mds.hw.navigation.NavigationManager;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.fail;
import static yandex.com.mds.hw.TestUtils.doWithView;
import static yandex.com.mds.hw.notes.NotesFragment.PREFERENCES_USER;

@RunWith(AndroidJUnit4.class)
public class UrlImageViewTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);
    private NavigationManager manager;
    private int userId;
    private MockWebServer webServer;
    private Solo mSolo;


    @Before
    public void setUp() throws Exception {
        manager = mActivityRule.getActivity().getNavigationManager();
        userId = mActivityRule.getActivity().getSharedPreferences(PREFERENCES_USER, MODE_PRIVATE).getInt("USER_ID", 20);
        mSolo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());
        manager.showNotes();
        webServer = new MockWebServer();
        webServer.start();
    }

    @Test
    public void testImage() throws Exception {
        manager.showNoteAdd(userId);
        mSolo.waitForFragmentByTag("NOTE_ADD", 100);
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        webServer.enqueue(new MockResponse().setBody(new Buffer().readFrom(
                mActivityRule.getActivity().getResources().openRawResource(R.raw.test_image)))
        );
        UrlImageView urlImageView = (UrlImageView) mActivityRule.getActivity().findViewById(R.id.url_image);
        urlImageView.setOnSuccessListener(new UrlImageView.OnSuccessListener() {
            @Override
            public void onSuccess() {
                testPassed[0] = true;
                latch.countDown();
            }
        });
        urlImageView.applyUrl(webServer.url("/image.png").toString());
        latch.await(500, TimeUnit.MILLISECONDS);
        if (!testPassed[0]) fail();
    }


    @Test
    public void testEmpty() throws Exception {
        manager.showNoteAdd(userId);
        mSolo.waitForFragmentByTag("NOTE_ADD", 500);
        onView(withId(R.id.url_image)).perform(doWithView(new TestUtils.OnViewGetInterface() {
            @Override
            public void onViewGet(View view) {
                UrlImageView urlImageView = (UrlImageView) view;
                urlImageView.applyUrl("");
            }
        }));
        onView(withText(R.string.url_image_status_empty)).check(matches(isDisplayed()));
    }

    @Test
    public void testFailed() throws Exception {
        manager.showNoteAdd(userId);
        mSolo.waitForFragmentByTag("NOTE_ADD", 100);
        UrlImageView urlImageView = (UrlImageView) mActivityRule.getActivity().findViewById(R.id.url_image);
        final CountDownLatch latch = new CountDownLatch(1);
        urlImageView.setOnFailureListener(new UrlImageView.OnFailureListener() {
            @Override
            public void onFailure() {
                latch.countDown();
            }
        });
        urlImageView.setOnSuccessListener(new UrlImageView.OnSuccessListener() {
            @Override
            public void onSuccess() {
                fail();
            }
        });
        urlImageView.applyUrl("/");
        latch.await();
    }

    @After
    public void tearDown() throws Exception {
        webServer.shutdown();
    }
}
