package com.koonen.photostream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * @author Glick
 * 
 */
public class ServiceConnector {

	private static final String TAG = ServiceConnector.class.getCanonicalName();

	private IRotationService.Stub stub = null;
	private ServiceConnection connection = null;
	private boolean serviceAttached = false;
	private Context context;

	private static ServiceConnector serviceConnector = null;

	public static void init(Context context) {
		if (serviceConnector == null) {
			serviceConnector = new ServiceConnector(context);
		}
	}

	public static ServiceConnector getInstance() {
		return serviceConnector;
	}

	private ServiceConnector(Context context) {
		this.context = context.getApplicationContext();
		initService();
	}

	private synchronized void initService() {
		if (connection == null) {
			connection = new ServiceConnection() {

				@Override
				public void onServiceConnected(ComponentName componentname,
						IBinder ibinder) {
					if (!serviceAttached) {
						stub = (IRotationService.Stub) ibinder;
						runService();
					}
				}

				@Override
				public void onServiceDisconnected(ComponentName componentname) {
					serviceAttached = false;
				}

			};
			Intent intent = new Intent();
			intent.setClassName(context, RotationService.class.getName());
			context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
		} else {
			runService();
		}
	}

	private synchronized void runService() {
		try {
			if (!serviceAttached) {
				stub.run();
				serviceAttached = true;
			}
		} catch (RemoteException e) {
			Log.e(TAG, e.getMessage());
			serviceAttached = false;
		}
	}
}
