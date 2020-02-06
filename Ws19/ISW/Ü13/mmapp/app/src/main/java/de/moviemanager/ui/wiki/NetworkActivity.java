package de.moviemanager.ui.wiki;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.concurrent.atomic.AtomicInteger;

import de.moviemanager.android.ResultHandlingActivity;

import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED;
import static de.moviemanager.ui.wiki.query.WikiQueryService.RESULT_ID;

public abstract class NetworkActivity <R> extends ResultHandlingActivity {

    protected ConnectivityManager connectivityManager;
    protected AtomicInteger requestId = new AtomicInteger(0);
    protected BroadcastReceiver serviceCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                handleServiceResponse(bundle);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    protected void sendRequestIfInternet(final R requestData) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (hasInternetConnection(activeNetwork)) {
            sendRequestIfWifi(requestData);
        } else {
            showFragment(NetworkInfoFragment.noInternetFragment(this));
        }
    }

    private boolean hasInternetConnection(NetworkInfo activeNetwork) {
        return activeNetwork != null && activeNetwork.isConnected();
    }

    protected void showFragment(Fragment fragment) {
        final FragmentManager manager = getSupportFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(getFragmentContainerId(), fragment);
        transaction.disallowAddToBackStack();
        transaction.commit();
    }

    protected abstract int getFragmentContainerId();

    private void sendRequestIfWifi(final R requestData) {
        Network network = connectivityManager.getActiveNetwork();
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities.hasCapability(NET_CAPABILITY_NOT_METERED)) {
            showFragment(NetworkInfoFragment.loadingFragment(this));
            sendRequestToService(requestId.incrementAndGet(), requestData);
        } else {
            showFragment(NetworkInfoFragment.noWifiFragment(this));
        }
    }

    protected abstract void sendRequestToService(int requestId, R requestData);

    private void handleServiceResponse(final Bundle extras) {
        int id = extras.getInt(RESULT_ID);
        if (id == requestId.get()) {
            latestServiceResponse(extras);
        }
    }

    protected abstract void latestServiceResponse(final Bundle extras);

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(serviceCallback, new IntentFilter(callbackIdentifier()));
    }

    protected abstract String callbackIdentifier();

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serviceCallback);
    }
}
