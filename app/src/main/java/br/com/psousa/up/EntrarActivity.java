package br.com.psousa.up;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Token;
import br.com.psousa.up.model.Usuario;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.Util;

public class EntrarActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private AccessToken accessToken;
    private SharedPreferences prefs;
    private Button mmBtnCriarConta;
    private NetworkHandler networkHandler;
    private ProgressDialog mmCarregando;

    private EditText mmEdtEmail;
    private EditText mmEdtSenha;
    private Button mmBtnEntrar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrar);

        prefs = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);

        mmEdtEmail = (EditText) findViewById(R.id.edt_entrar_email);
        mmEdtSenha = (EditText) findViewById(R.id.edt_entrar_senha);
        mmBtnEntrar = (Button) findViewById(R.id.btn_entrar_entrar);
        mmBtnEntrar.setOnClickListener(new EntrarOnClickListener());

        mmBtnCriarConta = (Button) findViewById(R.id.btn_entrar_criar_conta);
        mmBtnCriarConta.setOnClickListener(new CadastroOnClickListener());

        loginButton = (LoginButton) findViewById(R.id.btn_entrar_facebook);
        loginButton.setReadPermissions("user_posts");
        // If using in a fragment
        //loginButton.setFragment(this);
        // Other app specific specialization

        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        LoginManager.getInstance().registerCallback(
        callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("accessToken", loginResult.getAccessToken());
                Intent it = new Intent();
                it.putExtras(bundle);
                setResult(RESULT_OK, it);
                finish();
            }

            @Override
            public void onCancel() {
                // App code
                Log.w("FACEBOOK", "Foi Cancelada");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.w("FACEBOOK", exception.getMessage());
            }
        });


        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mmCarregando != null) {
            mmCarregando.dismiss();
            mmCarregando = null;
        }
    }

    public void pesquisaToken(Usuario usuario){
        mmCarregando = ProgressDialog.show(this, getResources().getString(R.string.aguarde),"");
        networkHandler = NetworkHandler.getInstance();

            networkHandler.post(Util.URL_TOKEN, Token.class, usuario, new Callback<Token>() {
                @Override
                public void callback(Token retornoUsuario, Erro erro) {
                    if (retornoUsuario != null) {
                        saveAccessToken(retornoUsuario);
                        if (mmCarregando != null){
                            mmCarregando.dismiss();
                        }
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(EntrarActivity.this, "Usuário ou Senha inválido!", Toast.LENGTH_LONG).show();
                        if (mmCarregando != null){
                            mmCarregando.dismiss();
                        }
                    }
                }
            });
    }

    private void saveAccessToken(Token token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.ACCESS_TOKEN, token.getToken());
        editor.commit();
    }

    private class CadastroOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            abrirTelaEntrar();
        }

        public void abrirTelaEntrar(){
            Intent it = new Intent(EntrarActivity.this, CadastroActivity.class);
            startActivity(it);
        }
    }

    private class EntrarOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            entrar();
        }

        public void entrar(){
            String email = mmEdtEmail.getText().toString();
            String senha = mmEdtSenha.getText().toString();

            if (!email.trim().equals("") &&
                    !senha.trim().equals("")) {

                Usuario usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setSenha(senha);
                pesquisaToken(usuario);
            } else {
                Toast.makeText(EntrarActivity.this, "Usuário ou Senha Inválido!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
