package info.atiar.inventory;

import android.app.Application;
import android.os.StrictMode;

import com.evernote.android.job.JobManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        JobManager.create(this).addJobCreator(new NoteJobCreator());
    }
}
