package br.com.psousa.up;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;


import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Servico;
import br.com.psousa.up.model.Token;
import br.com.psousa.up.model.Usuario;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.Util;

public class PrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PerfilFragment.OnLogoutOnListener, ServicoFragment.OnListFragmentInteractionListener {

    private TextView mmlbLogin;
    private TextView mmlbClickAqui;
    private ImageView mmimgPerfil;
    private View headerLayout;
    private FragmentManager fragmentManager;
    private SharedPreferences prefs;
    private NetworkHandler networkHandler;
    private ProgressDialog mmCarregandoPesquisando;
    private ProgressDialog mmCarregandoUsuario;

    private Usuario usuarioLogado;

    static final int FAZER_LOGIN_REQUEST = 1;
    public static final int ACTIOn_CAMERA = 5678;
    public static final int ACTION_GALERIA = 1234;

    Fragment fragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        prefs = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);

        FacebookSdk.sdkInitialize(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerLayout = navigationView.getHeaderView(0);

        // Initialize the SDK before executing any other operations
        AppEventsLogger.activateApp(this);

        mmlbLogin = (TextView) headerLayout.findViewById(R.id.lbLogin);
        mmlbClickAqui = (TextView) headerLayout.findViewById(R.id.lb_clique_aqui);
        mmimgPerfil = (ImageView) headerLayout.findViewById(R.id.img_perfil);

        if (Util.verificaConexao(this)) {
            if (AccessToken.getCurrentAccessToken() != null) {
                acessarLogin(AccessToken.getCurrentAccessToken());
                mmlbClickAqui.setText("");
            } else if (loadAccessToken() != ""){
                pesquisaUsuarioToken();
            } else {
                    mmlbClickAqui.setOnClickListener(new EntrarOnClickListener());
                    mmlbLogin.setOnClickListener(new EntrarOnClickListener());
                    mmimgPerfil.setOnClickListener(new EntrarOnClickListener());
            }

        }
        fragment = ServicoFragment.newInstance(loadAccessToken());
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == FAZER_LOGIN_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    AccessToken acess =  data.getParcelableExtra("accessToken");
                    acessarLogin(acess);
                    mmlbClickAqui.setOnClickListener(null);
                    mmlbLogin.setOnClickListener(null);
                    mmimgPerfil.setOnClickListener(null);
                } else {
                    pesquisaUsuarioToken();
                    mmlbClickAqui.setOnClickListener(null);
                    mmlbLogin.setOnClickListener(null);
                    mmimgPerfil.setOnClickListener(null);
                }
            }
        } else if(requestCode == ACTION_GALERIA && resultCode == RESULT_OK){
            //imagem veio da galeria
            if (data != null) {
                Uri uriImagemGaleria = data.getData();
                String caminho = "";
                String[] projection = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(uriImagemGaleria, projection, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    caminho = cursor.getString(column_index);
                    cursor.close();
                }
                if (caminho == "") {
                    caminho = uriImagemGaleria.getPath();
                }
                //caminho = getPath(uriImagemGaleria);
                Bitmap bitmap = BitmapFactory.decodeFile(caminho);
                ((CadastroServicoFragment) fragment).adicionarFoto(bitmap, caminho);
            }
        }
        else if(requestCode == ACTIOn_CAMERA && resultCode == RESULT_OK){
            //imagem veio da camera
            ((CadastroServicoFragment) fragment).adicionarFoto(null, "");
        }
    }


    private void acessarLogin(final AccessToken acess){
        if (acess != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    acess,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            Log.v("TAG", "JSON: " + object);
                            try {
                                //String foto = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                String id = object.getString("id");
                                String foto = "https://graph.facebook.com/" + id + "/picture?height=120&width=120";
                                String nome = object.getString("name");

                                final Usuario usuario = new Usuario();
                                usuario.setNome(nome);
                                usuario.setId_facebook(acess.getUserId());

                                pesquisaOuAdicionaToken(usuario);

                                mmlbLogin.setText(nome);

                                Glide.with(PrincipalActivity.this)
                                        .load(foto)
                                        .centerCrop()
                                        .into(mmimgPerfil); // id do teu imageView.


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,picture.width(120).height(120)");
            request.setParameters(parameters);
            request.executeAsync();
        } else {
            mmlbLogin.setText(getResources().getString(R.string.acesse_sua_conta));
            mmlbClickAqui.setText(getResources().getString(R.string.clique_aqui));
            mmimgPerfil.setImageResource(R.mipmap.ic_usuario);
        }
    }

    public void pesquisaUsuarioToken(){
        networkHandler = NetworkHandler.getInstance();

        String token = loadAccessToken();

        if (token != "") {
            mmCarregandoUsuario = ProgressDialog.show(this, getResources().getString(R.string.aguarde), "");


            networkHandler.read(Util.URL_USUARIO, token, Usuario.class, new Callback<Usuario>() {
                @Override
                public void callback(Usuario usuario, Erro erro) {
                    if (usuario != null) {
                        usuarioLogado = usuario;
                        mmlbLogin.setText(usuario.getNome());
                        mmlbClickAqui.setText(usuario.getEmail());
                    } else {
                        Toast.makeText(PrincipalActivity.this, "Erro ao carregar dados!", Toast.LENGTH_LONG).show();
                    }
                    mmCarregandoUsuario.dismiss();
                }
            });
        } else {
            mmlbLogin.setText(getResources().getString(R.string.acesse_sua_conta));
            mmlbClickAqui.setText(getResources().getString(R.string.clique_aqui));
            mmimgPerfil.setImageResource(R.mipmap.ic_usuario);
        }
    }

    public void pesquisaOuAdicionaToken(final Usuario usuario){
        networkHandler = NetworkHandler.getInstance();
        mmCarregandoPesquisando = ProgressDialog.show(this, getResources().getString(R.string.aguarde),"");
        final Usuario usu =  usuario;

            networkHandler.post(Util.URL_TOKEN, Token.class, usu, new Callback<Token>() {
                @Override
                public void callback(Token retornoUsuario, Erro erro) {
                    if (retornoUsuario != null) {
                        saveAccessToken(retornoUsuario);
                        usuarioLogado = usu;
                        mmCarregandoPesquisando.dismiss();
                    } else {
                        networkHandler.post(Util.URL_USUARIO, Usuario.class, usu, new Callback<Usuario>() {
                            @Override
                            public void callback(Usuario retorno, Erro erro) {
                                pesquisaOuAdicionaToken(retorno);
                                mmCarregandoPesquisando.dismiss();
                            }
                        });
                    }
                }
            });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    private void saveAccessToken(Token token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.ACCESS_TOKEN, token.getToken());
        editor.commit();
    }

    private String loadAccessToken() {
        return prefs.getString(Util.ACCESS_TOKEN, "");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        int id = item.getItemId();

        if (id == R.id.nav_anuncio) {
            fragment = ServicoFragment.newInstance(loadAccessToken());
        } else if (id == R.id.nav_inserir_anuncio) {
            if (Util.verificaConexao(this)) {
                if ((AccessToken.getCurrentAccessToken() != null) ||
                        (loadAccessToken() != "")) {
                    fragment = CadastroServicoFragment.newInstance(loadAccessToken(), "");
                } else {
                    Intent it = new Intent(PrincipalActivity.this, EntrarActivity.class);
                    startActivityForResult(it, FAZER_LOGIN_REQUEST);
                }
            }
        } else if (id == R.id.nav_minha_conta) {
            if (Util.verificaConexao(this)) {
                if ((AccessToken.getCurrentAccessToken() != null) ||
                    (loadAccessToken() != "")){
                    fragment = MinhaContaFragment.newInstance(usuarioLogado, loadAccessToken());
                } else {
                    Intent it = new Intent(PrincipalActivity.this, EntrarActivity.class);
                    startActivityForResult(it, FAZER_LOGIN_REQUEST);
                }
            }
        }

        if (fragment != null) {
            // Insert the fragment by replacing any existing fragment
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            // Highlight the selected item has been done by NavigationView
            // Set action bar title
            setTitle(item.getTitle());
            // Close the navigation drawer
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnLogout() {
        acessarLogin(null);
        pesquisaUsuarioToken();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, new Fragment()).commit();
    }

    @Override
    public void onListFragmentInteraction(Servico item) {

    }

    private class EntrarOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            this.abrirTelaEntrar();
        }

        public void abrirTelaEntrar(){
            Intent it = new Intent(PrincipalActivity.this, EntrarActivity.class);
            startActivityForResult(it, FAZER_LOGIN_REQUEST);
        }


    }
}
