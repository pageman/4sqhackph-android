package com.poweroflove.anomeron.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.poweroflove.anomeron.R;
import com.poweroflove.anomeron.adapter.EntryArrayAdapter;
import com.poweroflove.anomeron.adapter.EntryArrayAdapter.EntryArrayAdapterActionListener;
import com.poweroflove.anomeron.model.Entry;
import com.poweroflove.anomeron.service.AnoMeronService;

public class MainActivity extends SherlockListActivity implements
		ActionBar.OnNavigationListener, EntryArrayAdapterActionListener {

	private String[] mLocations = { "Nearby", "The Podium" };

	private ActionBar mActionBar;

	private boolean mIsBound;
	private AnoMeronService mBoundService;
	
	private List<Entry> mEntries;
	
	private BroadcastReceiver mBr = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			
			if(action.equals("refresh feed")) {
				System.out.println("refresh");
				mEntries.clear();
				mEntries.addAll(Entry.getEntries());
				System.out.println(mEntries);
				mAdapter.notifyDataSetChanged();
			}
		}
		
	};
	

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((AnoMeronService.LocalBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;
		}
	};

	private ListView mLv;
	private EntryArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		doBindService();

		ArrayAdapter<CharSequence> list = new ArrayAdapter(
				getApplicationContext(), R.layout.sherlock_spinner_item,
				mLocations);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBar.setListNavigationCallbacks(list, this);

		mLv = getListView();
		mEntries = Entry.getEntries();
		mAdapter = new EntryArrayAdapter(getApplicationContext(), 0,
				mEntries);
		mAdapter.mListener = this;
		mLv.setAdapter(mAdapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("refresh feed");
		registerReceiver(mBr, filter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		doUnbindService();
		unregisterReceiver(mBr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Refresh").setIcon(R.drawable.ic_action_refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add("Search").setIcon(R.drawable.ic_action_search)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add("Compose").setIcon(R.drawable.ic_action_compose)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		startService(new Intent(MainActivity.this, AnoMeronService.class));
		bindService(new Intent(MainActivity.this, AnoMeronService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public void onItemSelected(long id) {
		Intent i = new Intent(MainActivity.this, DetailsActivity.class);
		i.putExtra("id", id);
		startActivity(i);
	}

}
