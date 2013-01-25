package fr.gcmtest.main;

import static fr.gcmtest.main.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static fr.gcmtest.main.CommonUtilities.EXTRA_MESSAGE;
import static fr.gcmtest.main.CommonUtilities.SENDER_ID;
import static fr.gcmtest.main.CommonUtilities.SERVER_URL;

import com.google.android.gcm.GCMRegistrar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
//import android.widget.Toast;

public class MainActivity extends Activity {
	
	//public static final String SENDER_ID = "696885573231";
	
	TextView m_Display;
    AsyncTask<Void, Void, Void> m_RegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
		
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		
		setContentView(R.layout.activity_main);
		m_Display = (TextView) findViewById(R.id.display) ;
		
		registerReceiver(m_HandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
		
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) 
		{
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SENDER_ID);
		} 
		else 
		{
			// Device is already registered on GCM, check server.
            // Skips registration.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
                m_Display.append(getString(R.string.already_registered) + "\n");
            } 
			else 
			{
            	// Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                m_RegisterTask = new AsyncTask<Void, Void, Void>() {
                	
                	@Override
                    protected Void doInBackground(Void... params) {
                        boolean registered =
                                ServerUtilities.register(context, regId);
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        m_RegisterTask = null;
                    }
                    
                };
                m_RegisterTask.execute(null, null, null);
            }
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
    protected void onDestroy() {
        if (m_RegisterTask != null) {
            m_RegisterTask.cancel(true);
        }
        unregisterReceiver(m_HandleMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }
	
	private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }
	
	private final BroadcastReceiver m_HandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            m_Display.append(newMessage + "\n");
        }
    };

}
