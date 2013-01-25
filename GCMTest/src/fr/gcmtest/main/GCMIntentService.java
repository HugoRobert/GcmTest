package fr.gcmtest.main;

import static fr.gcmtest.main.CommonUtilities.SENDER_ID;
import static fr.gcmtest.main.CommonUtilities.displayMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
//import com.google.android.gcm.GCMBroadcastReceiver;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService 
{

	public GCMIntentService(String... senderIds) {
		super(senderIds);
	}

	@Override
	protected void onError(Context a_context, String a_errorId) 
	{
		Log.i(TAG, "Received error: " + a_errorId);
        displayMessage(a_context, getString(R.string.gcm_error, a_errorId));
	}
	
	@Override
    protected boolean onRecoverableError(Context a_context, String a_errorId) 
	{
        // log message
        Log.i(TAG, "Received recoverable error: " + a_errorId);
        displayMessage(a_context, getString(R.string.gcm_recoverable_error, a_errorId));
        return super.onRecoverableError(a_context, a_errorId);
    }

	@Override
	protected void onMessage(Context a_context, Intent a_intent) 
	{
		Log.i(TAG, "Received message");
        String message = getString(R.string.gcm_message);
        displayMessage(a_context, message);
        // notifies user
        generateNotification(a_context, message);
	}
	
	@Override
    protected void onDeletedMessages(Context a_context, int total)
	{
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(a_context, message);
        // notifies user
        generateNotification(a_context, message);
    }

	@Override
	protected void onRegistered(Context a_context, String a_regId) 
	{
		Log.i(TAG, "Device registered: regId = " + a_regId);
        displayMessage(a_context, getString(R.string.gcm_registered));
        ServerUtilities.register(a_context, a_regId);
	}

	@Override
	protected void onUnregistered(Context a_context, String a_regId) 
	{
		Log.i(TAG, "Device unregistered");
        displayMessage(a_context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(a_context)) 
        {
            ServerUtilities.unregister(a_context, a_regId);
        } 
        else 
        {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
	}
	
	/**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) 
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(context)
        		.setWhen(System.currentTimeMillis())
        		.setTicker(context.getString(R.string.app_name))
        		.setSmallIcon(R.drawable.ic_stat_gcm)
        		.setContentIntent(intent) ;
        
        notificationManager.notify(0, builder.build()) ;
    }
	
}
