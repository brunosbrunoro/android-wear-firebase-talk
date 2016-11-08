package br.com.brunoscrokbrunoro.firebasetalk.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
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
import java.util.HashMap;
import java.util.Map;

import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

/**
 * Created by bruno on 10/26/16.
 */

public class WearCallMensagensSendListener extends WearableListenerService {


    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("mensagens");

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if(dataMap.getString("mensagem") != null) {
                    Log.i("MensangemSend",dataMap.getString("mensagem"));
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Mensagem mensagem = new Mensagem();c
                    mensagem.setMensagem(dataMap.getString("mensagem"));
                    if (user.getDisplayName() != null) {
                        mensagem.setUsuario(user.getDisplayName());
                    } else {
                        mensagem.setUsuario(user.getEmail());
                    }
                    mensagem.setUid(myRef.push().getKey());
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(mensagem.getUid(), mensagem.toMap());
                    myRef.updateChildren(childUpdates);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
            }
        }
    }
}
