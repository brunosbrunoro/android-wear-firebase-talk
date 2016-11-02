package br.com.brunoscrokbrunoro.firebasetalk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

/**
 * Created by bruno on 10/26/16.
 */

public class MensagensAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Mensagem> mensagens;

    public MensagensAdapter(LayoutInflater inflater, List<Mensagem> mensagens){
        this.inflater = inflater;
        this.mensagens = mensagens;

    }

    @Override
    public int getCount() {
        return mensagens.size();
    }

    @Override
    public Mensagem getItem(int position) {
        return mensagens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            convertView = inflater.inflate(android.R.layout.simple_list_item_2,null);
            holder.mensagem = (TextView) convertView.findViewById(android.R.id.text1);
            holder.usuario = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);

        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        Mensagem mensagem = mensagens.get(position);
        holder.mensagem.setText(mensagem.getMensagem());
        holder.usuario.setText(mensagem.getUsuario());

        return convertView;
    }

    class ViewHolder{
        public TextView mensagem;
        public TextView usuario;
    }
}
