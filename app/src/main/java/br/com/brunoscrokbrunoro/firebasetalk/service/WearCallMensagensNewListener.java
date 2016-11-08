package br.com.brunoscrokbrunoro.firebasetalk.service;

import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.util.Log;

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
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

/**
 * Created by bruno on 10/26/16.
 */

public class WearCallMensagensNewListener extends WearableListenerService {


    private static ArrayList<DataMap> listMensagemList;

    @Override
    public void onCreate(){
        super.onCreate();
        listMensagemList = new ArrayList<DataMap>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("mensagens");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                listMensagemList.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshots) {
                    Mensagem mensagem = dataSnapshot1.getValue(Mensagem.class);
                    listMensagemList.add(mensagem.toDataMap());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.i("MensagensNewListener","onDataChanged");
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                ArrayList<DataMap> list = new ArrayList<DataMap>();
                list.addAll(listMensagemList);
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/mensagemNew");
                putDataMapReq.getDataMap().putString("data", new Date().toString());
                putDataMapReq.getDataMap().putDataMapArrayList("mensagens", list);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult =
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if(dataItemResult.getStatus().isSuccess()){
                            Log.v("MensagensNewListener", "DataMap successfully sent!");
                        }else{
                            Log.v("MensagensNewListener", "ERROR: Failed to send DataMap to data layer");
                        }
                    }
                });

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
            }
        }
    }
}
