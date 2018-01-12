package br.com.psousa.up;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import br.com.psousa.up.ServicoFragment.OnListFragmentInteractionListener;
import br.com.psousa.up.model.Servico;
import br.com.psousa.up.util.Util;

import java.util.List;

public class MyServicoRecyclerViewAdapter extends RecyclerView.Adapter<MyServicoRecyclerViewAdapter.ViewHolder> {

    private final List<Servico> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context context;



    public MyServicoRecyclerViewAdapter(Context context, List<Servico> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_servico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mProgressView.setVisibility(View.VISIBLE);

        Picasso.with(context).load(Util.URL_IMAGEM + mValues.get(position).getFotos()[0]).fit().into(holder.mImagemView, new Callback() {
            @Override
            public void onSuccess() {

                holder.mProgressView.setVisibility(View.GONE);
                holder.mImagemView.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onError() {
                holder.mProgressView.setVisibility(View.GONE);
            }
        });

        holder.mDescricaoView.setText(mValues.get(position).getDescricao());
        holder.mContatoView.setText(mValues.get(position).getContato());



        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImagemView;
        public final TextView mDescricaoView;
        public final TextView mContatoView;
        public final ProgressBar mProgressView;
        public Servico mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProgressView = (ProgressBar) view.findViewById(R.id.progressImg);
            mImagemView = (ImageView) view.findViewById(R.id.img_item_servico);
            mDescricaoView = (TextView) view.findViewById(R.id.txt_item_descricao);
            mContatoView = (TextView) view.findViewById(R.id.txt_item_contato);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDescricaoView.getText() + "'";
        }
    }
}
