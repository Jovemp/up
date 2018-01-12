package br.com.psousa.up;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Servico;
import br.com.psousa.up.model.Usuario;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.Util;


public class MinhaContaFragment extends Fragment {

    private static final String ARG_USUARIO = "usuario";
    private static final String ARG_TOKEN = "token";


    private Usuario mUsuarioLogado;
    private String mToken;
    private TextView mNomeUsuario;
    private TextView mEmailUsuario;
    private TextView mQtdAnuncio;
    private List<Servico> mListaServico;

    public MinhaContaFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MinhaContaFragment newInstance(Usuario usuario, String token) {
        MinhaContaFragment fragment = new MinhaContaFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USUARIO, usuario);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsuarioLogado = getArguments().getParcelable(ARG_USUARIO);
            mToken = getArguments().getString(ARG_TOKEN);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_minha_conta, container, false);

        mNomeUsuario = (TextView) view.findViewById(R.id.txt_minha_conta_usuario);
        mEmailUsuario = (TextView) view.findViewById(R.id.txt_minha_conta_email);
        mQtdAnuncio = (TextView) view.findViewById(R.id.txt_minha_conta_qtd_meus_anuncios);

        mNomeUsuario.setText(mUsuarioLogado.getNome());
        mEmailUsuario.setText(mUsuarioLogado.getEmail());

        NetworkHandler network = NetworkHandler.getInstance();

        network.readList(Util.URL_SERVICO_USU, mToken, Servico[].class, new Callback<List<Servico>>() {
            @Override
            public void callback(List<Servico> servicos, Erro erro) {
                if (servicos != null){
                    mListaServico = servicos;
                    mQtdAnuncio.setText(mListaServico.size()+"");
                } else {
                    Toast.makeText(getActivity() ,getResources().getString(R.string.error_conexao_indisponivel), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.minha_conta, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_editar) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, PerfilFragment.newInstance(mUsuarioLogado)).commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
