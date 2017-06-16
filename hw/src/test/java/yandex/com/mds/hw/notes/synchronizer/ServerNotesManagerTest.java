package yandex.com.mds.hw.notes.synchronizer;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import yandex.com.mds.hw.models.Note;
import yandex.com.mds.hw.network.NoteService;
import yandex.com.mds.hw.network.NoteServiceResponse;
import yandex.com.mds.hw.network.ServiceGenerator;
import yandex.com.mds.hw.utils.SerializationUtils;

import static junit.framework.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@PrepareForTest(Log.class)
@RunWith(PowerMockRunner.class)
// https://medium.com/android-testing-daily/testing-with-okhttp3-and-powermock-c5a326f00ab
@PowerMockIgnore("javax.net.ssl.*")
public class ServerNotesManagerTest {
    private ServerNotesManager manager;
    private MockWebServer webServer;
    private CompletableFuture future;

    @Before
    public void setUp() throws Exception {
        webServer = new MockWebServer();
        webServer.start();
        manager = new ServerNotesManager();
        manager.setService(ServiceGenerator.createService(NoteService.class, webServer.url("/").toString()));
        mockStatic(Log.class);
        PowerMockito.when(Log.d(Mockito.anyString(), Mockito.anyString())).thenReturn(0);
    }

    @Test
    public void testAdd() throws Exception {
        Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        try {
            manager.add(note);
            fail();
        } catch (IOException e) {
        }
        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", 1));
        webServer.enqueue(new MockResponse().setBody(response));
        manager.add(note);

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", 1));
        webServer.enqueue(new MockResponse().setBody(response));
        try {
            manager.add(note);
            fail();
        } catch (IOException e) {
        }
    }

    @Test
    public void testAddAsync() throws Exception {
        final Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        future = new CompletableFuture();
        manager.addAsync(note, new ServerNotesManager.ActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();

        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", 1));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.addAsync(note, new ServerNotesManager.ActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer response) {
                future.complete(null);
            }

            @Override
            public void onFailure(Exception e) {
                future.completeExceptionally(e);
            }
        });
        future.get();

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", 1));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.addAsync(note, new ServerNotesManager.ActionCallback<Integer>() {
            @Override
            public void onSuccess(Integer response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();
    }

    @Test
    public void testDelete() throws Exception {
        Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        try {
            manager.delete(note);
            fail();
        } catch (IOException e) {
        }
        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", null));
        webServer.enqueue(new MockResponse().setBody(response));
        manager.delete(note);

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", null));
        webServer.enqueue(new MockResponse().setBody(response));
        try {
            manager.delete(note);
            fail();
        } catch (IOException e) {
        }
    }

    @Test
    public void testDeleteAsync() throws Exception {
        Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        future = new CompletableFuture();
        manager.deleteAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();

        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", null));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.deleteAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.complete(null);
            }

            @Override
            public void onFailure(Exception e) {
                future.completeExceptionally(e);
            }
        });
        future.get();

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", null));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.deleteAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();
    }

    @Test
    public void testSave() throws Exception {
        Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        try {
            manager.save(note);
            fail();
        } catch (IOException e) {
        }
        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", null));
        webServer.enqueue(new MockResponse().setBody(response));
        manager.save(note);

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", null));
        webServer.enqueue(new MockResponse().setBody(response));
        try {
            manager.save(note);
            fail();
        } catch (IOException e) {
        }
    }

    @Test
    public void testSaveAsync() throws Exception {
        Note note = new Note();
        webServer.enqueue(new MockResponse().setBody(""));
        future = new CompletableFuture();
        manager.saveAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();

        String response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("ok", null));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.saveAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.complete(null);
            }

            @Override
            public void onFailure(Exception e) {
                future.completeExceptionally(e);
            }
        });
        future.get();

        response = SerializationUtils.GSON_SERVER.toJson(new NoteServiceResponse<>("error", null));
        webServer.enqueue(new MockResponse().setBody(response));
        future = new CompletableFuture();
        manager.saveAsync(note, new ServerNotesManager.ActionCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                future.cancel(true);
            }

            @Override
            public void onFailure(Exception e) {
                future.complete(null);
            }
        });
        future.get();
    }

    @After
    public void tearDown() throws Exception {
        webServer.shutdown();
    }
}