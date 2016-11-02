package br.com.brunoscrokbrunoro.firebasetalk.model;

import com.google.android.gms.wearable.DataMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Scrok Brunoro
 * @create 8/8/16 20:21
 * @project Firebase-Talk
 */
public class Mensagem {

    private String uid;
    private String usuario;
    private String mensagem;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Mensagem(String uid, String usuario, String mensagem) {
        this.uid = uid;
        this.usuario = usuario;
        this.mensagem = mensagem;
    }

    public Mensagem() {
    }

    public Mensagem(DataMap dataMap) {
        this.uid = dataMap.getString("uid");
        this.mensagem = dataMap.getString("mensagem");
        this.usuario = dataMap.getString("usuario");
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("mensagem", mensagem);
        result.put("usuario", usuario);

        return result;
    }
    public DataMap toDataMap() {
        DataMap result = new DataMap();
        result.putString("uid", uid);
        result.putString("mensagem", mensagem);
        result.putString("usuario", usuario);

        return result;
    }
}
