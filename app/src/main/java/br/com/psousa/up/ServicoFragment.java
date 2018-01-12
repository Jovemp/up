package br.com.psousa.up;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Servico;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.Util;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ServicoFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TOKEN = "token";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String mToken;
    private OnListFragmentInteractionListener mListener;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView recyclerView;
    private ProgressDialog progress;
    private NetworkHandler networkHandler;
    private List<Servico> servicos;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServicoFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ServicoFragment newInstance(String token) {
        ServicoFragment fragment = new ServicoFragment();
        Bundle args = new Bundle();
        args.putString(TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mToken = getArguments().getString(TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_servico_list, container, false);

        // Set the adapter
            Context context = view.getContext();
            recyclerView = (RecyclerView) view.findViewById(R.id.list);
            //if (mColumnCount <= 1) {
            //    recyclerView.setLayoutManager(new LinearLayoutManager(context));
            //} else {
                GridLayoutManager g = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(g);
            //}
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);

            // Swipe to Refresh
            swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
            swipeLayout.setOnRefreshListener(OnRefreshListener());
            swipeLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        taskServicos(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Por padrão busca os carros do banco de dados.
        taskServicos(false);
    }

    public void updateView(List<Servico> servicos) {
        if (servicos != null) {
            this.servicos = servicos;
            // Atualiza a view na UI Thread
            if (recyclerView != null) {
                recyclerView.setAdapter(new MyServicoRecyclerViewAdapter(getContext(), this.servicos, mListener));
            }
            //toast("update ("+carros.size()+"): " + carros);
        }
    }

    private SwipeRefreshLayout.OnRefreshListener OnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Atualiza ao fazer o gesto Swipe To Refresh
                if (Util.isNetworkAvailable(getContext())) {
                    taskServicos(true);
                } else {
                    alert(R.string.error_conexao_indisponivel);
                }
            }
        };
    }

    private void taskServicos(final boolean pullToRefresh) {

        if (Util.verificaConexao(getActivity())) {
            networkHandler = NetworkHandler.getInstance();

            showProgress(networkHandler.readList(Util.URL_SERVICO, mToken, Servico[].class, new Callback<List<Servico>>() {
                @Override
                public void callback(List<Servico> retorno, Erro erro) {
                    if (retorno != null) {
                        // Create an ArrayAdapter using the string array and a default spinner layout
                        Log.i("INTEGRA", retorno.toString());
                        updateView(retorno);
                        closeProgress(pullToRefresh ? R.id.swipeToRefresh : R.id.progress);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.erro_ao_cadastrar), Toast.LENGTH_LONG).show();
                        closeProgress(pullToRefresh ? R.id.swipeToRefresh : R.id.progress);
                        ;
                    }
                }
            }), pullToRefresh ? R.id.swipeToRefresh : R.id.progress);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_conexao_indisponivel), Toast.LENGTH_SHORT).show();
        }
    }

    private void closeProgress(int progressId) {
        if (progressId > 0 && getView() != null) {
            View view = getView().findViewById(progressId);
            if (view != null) {
                if (view instanceof SwipeRefreshLayout) {
                    SwipeRefreshLayout srl = (SwipeRefreshLayout) view;
                    srl.setRefreshing(false);
                } else {
                    view.setVisibility(View.GONE);
                }
                return;
            }
        }

        Log.d("UP", "closeProgress()");
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
            progress = null;
        }
    }

    protected void showProgress(final AsyncTask task, int progressId) {
        if (progressId > 0 && getView() != null) {
            View view = getView().findViewById(progressId);
            if (view != null) {
                if (view instanceof SwipeRefreshLayout) {
                    SwipeRefreshLayout srl = (SwipeRefreshLayout) view;
                    if (!srl.isRefreshing()) {
                        srl.setRefreshing(true);
                    }
                } else {
                    view.setVisibility(View.VISIBLE);
                }
                return;
            }
        }

        // Mostra o dialog e permite cancelar
        if (progress == null) {
            progress = ProgressDialog.show(getActivity(), "Aguarde", "Por favor aguarde...");
            progress.setCancelable(true);
            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // Cancela a AsyncTask
                    task.cancel(true);
                }
            });
        }
    }


    protected void alert(int msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Servico item);
    }
}
