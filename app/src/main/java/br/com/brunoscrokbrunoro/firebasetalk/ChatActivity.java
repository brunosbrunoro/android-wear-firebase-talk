package br.com.brunoscrokbrunoro.firebasetalk;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.brunoscrokbrunoro.firebasetalk.adapter.MensagensAdapter;
import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

/**
 * @author Bruno Scrok Brunoro
 * @create 8/8/16 20:15
 * @project Firebase-Talk
 */
public class ChatActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = ChatActivity.class.getCanonicalName();

    private static final String COUNT_KEY = "com.example.key.count";

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseUser user;

    private MensagensAdapter adapter;
    private List<Mensagem> mensagens;

    private ListView lstChat;
    private EditText edtMensagem;
    private Button butEnviar;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        lstChat = (ListView) findViewById(R.id.lstChat);
        edtMensagem = (EditText) findViewById(R.id.edtMensagem);
        butEnviar = (Button) findViewById(R.id.butEnviar);
        mensagens = new ArrayList<Mensagem>();
        adapter = new MensagensAdapter(getLayoutInflater(),mensagens);
        lstChat.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        database = FirebaseDatabase.getInstance();

        myRef = database.getReference("mensagens");

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user.getDisplayName() == null){
            NomeDialog nomeDialog = NomeDialog.getInstance();
            nomeDialog.setCancelable(false);
            nomeDialog.show(getSupportFragmentManager(), "nomeDialog");
        }
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                mensagens.clear();
                ArrayList<DataMap> list = new ArrayList<DataMap>();
                for (DataSnapshot dataSnapshot1 : dataSnapshots) {
                    Mensagem mensagem = dataSnapshot1.getValue(Mensagem.class);
                    mensagens.add(mensagem);
                    list.add(mensagem.toDataMap());
                }
                adapter.notifyDataSetChanged();
                lstChat.setSelection(adapter.getCount() - 1);
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/mensagemNew");
                putDataMapReq.getDataMap().putDataMapArrayList("mensagens", list);
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

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });


        butEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                Mensagem mensagem = new Mensagem();
                mensagem.setMensagem(edtMensagem.getText().toString());
                if(user.getDisplayName() != null) {
                    mensagem.setUsuario(user.getDisplayName());
                }else{
                    mensagem.setUsuario(user.getEmail());
                }
                mensagem.setUid(myRef.push().getKey());
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(mensagem.getUid(), mensagem.toMap());
                myRef.updateChildren(childUpdates);
                edtMensagem.setText("");

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
    }
    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }


}
