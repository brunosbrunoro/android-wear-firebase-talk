package br.com.brunoscrokbrunoro.firebasetalk.adapter;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.brunoscrokbrunoro.firebasetalk.model.Mensagem;

/**
 * Created by bruno on 10/26/16.
 */

public class MensagensAdapter extends WearableListView.Adapter {
    private List<Mensagem> mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    public MensagensAdapter(Context context, List<Mensagem> dataset) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public static class ViewHolder extends WearableListView.ViewHolder {
        private TextView txtMensagem;
        private TextView txtUsuario;
        public ViewHolder(View itemView) {
            super(itemView);
            txtMensagem = (TextView) itemView.findViewById(android.R.id.text1);
            txtUsuario = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        return new ViewHolder(mInflater.inflate(android.R.layout.simple_list_item_2, null));
    }
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ViewHolder itemHolder = (ViewHolder) holder;
        TextView txtMensagem = itemHolder.txtMensagem;
        TextView txtUsuario = itemHolder.txtUsuario;

        Mensagem mensagem = mDataset.get(position);

        txtMensagem.setText(mensagem.getMensagem());
        txtUsuario.setText(mensagem.getUsuario());

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
