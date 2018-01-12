package br.com.psousa.up;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Usuario;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.CNP;
import br.com.psousa.up.util.Mask;
import br.com.psousa.up.util.Util;


public class PerfilFragment extends Fragment {

    public PerfilFragment() {
        // Required empty public constructor
    }


    private static final String ARG_USUARIO = "usuario";
    private Usuario mUsuarioLogado;

    private OnLogoutOnListener mmLogoutOnListener;
    private LoginButton mmBtnSairFacebook;
    private Button mBtnSairEmail;
    private EditText mEdtSenha;
    private SharedPreferences prefs;
    private TextView txtNome;
    private EditText edtCpf;
    private EditText edtEmail;
    private EditText edtTelefone;
    private Button btnEnviar;
    private TextWatcher cpfMask;
    private TextWatcher telefoneMask;
    private Resources resources;
    private NetworkHandler networkHandler;

    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(Usuario usuario) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USUARIO, usuario);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUsuarioLogado = getArguments().getParcelable(ARG_USUARIO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_perfil, container, false);

        resources = getResources();

        mmBtnSairFacebook = (LoginButton) v.findViewById(R.id.btn_perfil_facebook);
        mBtnSairEmail = (Button) v.findViewById(R.id.btn_perfil_sair);
        mEdtSenha = (EditText) v.findViewById(R.id.edt_perfil_senha);
        txtNome = (TextView) v.findViewById(R.id.txt_perfil_nome);
        edtCpf = (EditText) v.findViewById(R.id.edt_perfil_cpf);
        edtEmail = (EditText) v.findViewById(R.id.edt_perfil_email);
        edtTelefone = (EditText) v.findViewById(R.id.edt_perfil_telefone);
        btnEnviar = (Button) v.findViewById(R.id.btn_perfil_enviar);

        btnEnviar.setOnClickListener(new EnviarOnClickListener());

        cpfMask = Mask.insert("###.###.###-##", edtCpf);
        edtCpf.addTextChangedListener(cpfMask);

        telefoneMask = Mask.insert("(##) ####-####", edtTelefone);
        edtTelefone.addTextChangedListener(telefoneMask);

        prefs = getActivity().getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);

        txtNome.setText(mUsuarioLogado.getNome());
        edtCpf.setText(mUsuarioLogado.getCpf());
        edtTelefone.setText(mUsuarioLogado.getTelefones()[0]);
        edtEmail.setText(mUsuarioLogado.getEmail());
        mEdtSenha.setText(mUsuarioLogado.getSenha());

        if (AccessToken.getCurrentAccessToken() != null) {
            mEdtSenha.setVisibility(View.INVISIBLE);
            mmBtnSairFacebook.setVisibility(View.VISIBLE);
            mBtnSairEmail.setVisibility(View.INVISIBLE);
            mmBtnSairFacebook.setOnClickListener(new LogoutOnClickListener());
        } else {
            mEdtSenha.setVisibility(View.VISIBLE);
            mmBtnSairFacebook.setVisibility(View.INVISIBLE);
            mBtnSairEmail.setVisibility(View.VISIBLE);
            mBtnSairEmail.setOnClickListener(new LogoutOnClickListener());
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogoutOnListener) {
            mmLogoutOnListener = (OnLogoutOnListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void saveAccessToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.ACCESS_TOKEN, token);
        editor.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mmLogoutOnListener = null;
    }

    private class LogoutOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            saveAccessToken("");
            mmLogoutOnListener.OnLogout();
        }

    }

    private String loadAccessToken() {
        return prefs.getString(Util.ACCESS_TOKEN, "");
    }

    public interface OnLogoutOnListener {
        // TODO: Update argument type and name
        void OnLogout();
    }

    private class EnviarOnClickListener implements View.OnClickListener {


        @Override
        public void onClick(View v) {
            Usuario usuario;

            if (!Util.validarEditVazio(edtCpf, resources)) {
                if (!Util.validarEditVazio(edtEmail, resources)) {
                    if (!Util.validarEditVazio(mEdtSenha, resources)) {
                        String cpf = edtCpf.getText().toString();
                        String email = edtEmail.getText().toString();
                        String senha = mEdtSenha.getText().toString();
                        String telefone = edtTelefone.getText().toString();

                        if (CNP.isValidCPF(cpf.replace(".", "").replace("-", ""))) {
                            mUsuarioLogado.setEmail(email);
                            mUsuarioLogado.setCpf(cpf);
                            mUsuarioLogado.setSenha(senha);

                            String[] telefones = new String[1];

                            telefones[0] = telefone;

                            mUsuarioLogado.setTelefones(telefones);

                            this.cadastrar(mUsuarioLogado);
                        } else {
                            edtCpf.setError(resources.getString(R.string.cpf_invalido));
                            edtCpf.requestFocus();
                        }
                    }
                }
            }
        }

        public void cadastrar(Usuario usuario) {
            final ProgressDialog mmCarregando = ProgressDialog.show(getActivity(), getResources().getString(R.string.aguarde), "");
            networkHandler = NetworkHandler.getInstance();

            networkHandler.post(Util.URL_USUARIO, Usuario.class, usuario, new Callback<Usuario>() {
                @Override
                public void callback(Usuario retornoUsuario, Erro erro) {
                    if (retornoUsuario != null) {
                        Toast.makeText(getActivity(), "Salvo com sucesso!", Toast.LENGTH_LONG).show();
                        mmCarregando.dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Erro ao Cadastrar!", Toast.LENGTH_LONG).show();
                        mmCarregando.dismiss();
                    }
                }
            });
        }
    }

}
