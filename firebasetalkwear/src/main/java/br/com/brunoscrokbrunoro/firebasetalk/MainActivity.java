package br.com.brunoscrokbrunoro.firebasetalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.ProgressSpinner;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.brunoscrokbrunoro.firebasetalk.adapter.MensagensAdapter;
import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

public class MainActivity extends Activity  implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private GoogleApiClient mGoogleApiClient;

    private List<Mensagem> mensagemList;

    private MensagensAdapter adapter;

    private WearableListView listView;
    private ProgressBar progressBar;
    private BoxInsetLayout boxInsetLayout;

    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mensagemList = new ArrayList<Mensagem>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        listView = (WearableListView) findViewById(R.id.wearable_list);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        boxInsetLayout = (BoxInsetLayout) findViewById(R.id.container);

        adapter = new MensagensAdapter(this,mensagemList);

        listView.setAdapter(adapter);

        boxInsetLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                Log.i(TAG,"onSwipeLeft");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent, SPEECH_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        mGoogleApiClient.connect();
        carregarMensagens();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        mensagemList.clear();
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/mensagemNew") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    if(dataMap.getDataMapArrayList("mensagens") != null) {
                        for (DataMap dataMapItem : dataMap.getDataMapArrayList("mensagens")) {
                            mensagemList.add(new Mensagem(dataMapItem));
                        }
                        progressBar.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        listView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.i(TAG,spokenText);
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/mensagemSend");
            putDataMapReq.getDataMap().putString("mensagem", spokenText);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    if(dataItemResult.getStatus().isSuccess()){
                        Log.v("dataMapSender_Wear", "DataMap successfully sent!");
                        carregarMensagens();
                    }else{
                        Log.v("dataMapSender_Wear", "ERROR: Failed to send DataMap to data layer");
                    }
                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void carregarMensagens(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        listView.setVisibility(View.GONE);
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/mensagemRequest");
        putDataMapReq.getDataMap().putString("data", new Date().toString());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                if(dataItemResult.getStatus().isSuccess()){
                    Log.v("dataMapSender_Wear", "DataMap successfully sent!");
                }else{
                    Log.v("dataMapSender_Wear", "ERROR: Failed to send DataMap to data layer");
                }
            }
        });
    }
}
