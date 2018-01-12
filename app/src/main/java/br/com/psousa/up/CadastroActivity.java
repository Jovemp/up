package br.com.psousa.up;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Usuario;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.CNP;
import br.com.psousa.up.util.Mask;
import br.com.psousa.up.util.Util;

public class CadastroActivity extends AppCompatActivity {

    private EditText mmEdtNome;
    private EditText mmEdtEmail;
    private EditText mmEdtCpf;
    private EditText mmEdtTelefone;
    private EditText mmEdtSenha;
    private Button mmBtnEnviar;
    private TextWatcher cpfMask;
    private TextWatcher telefoneMask;
    private CheckBox mmChkTermo;

    private Resources resources;

    private NetworkHandler networkHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        resources = getResources();

        mmEdtNome = (EditText) findViewById(R.id.edt_cadastro_nome);
        mmEdtCpf = (EditText) findViewById(R.id.edt_cadastro_cpf);
        mmEdtEmail = (EditText) findViewById(R.id.edt_cadastro_email);
        mmEdtSenha = (EditText) findViewById(R.id.edt_cadastro_senha);
        mmEdtTelefone = (EditText) findViewById(R.id.edt_cadastro_telefone);
        mmBtnEnviar = (Button) findViewById(R.id.btn_cadastro_enviar);
        mmChkTermo = (CheckBox) findViewById(R.id.chk_cadastro_concordo);

        mmChkTermo.setOnCheckedChangeListener(new TermoOnCheckedListener());

        mmBtnEnviar.setOnClickListener(new EnviarOnClickListener());

        cpfMask = Mask.insert("###.###.###-##", mmEdtCpf);
        mmEdtCpf.addTextChangedListener(cpfMask);

        telefoneMask = Mask.insert("(##) ####-####", mmEdtTelefone);
        mmEdtTelefone.addTextChangedListener(telefoneMask);
    }

    private class EnviarOnClickListener implements View.OnClickListener {



        @Override
        public void onClick(View v) {
            Usuario usuario;

            if (!Util.validarEditVazio(mmEdtNome, resources)) {
                if (!Util.validarEditVazio(mmEdtCpf, resources)) {
                    if (!Util.validarEditVazio(mmEdtEmail, resources)) {
                        if (!Util.validarEditVazio(mmEdtSenha, resources)) {
                            String nome = mmEdtNome.getText().toString();
                            String cpf = mmEdtCpf.getText().toString();
                            String email = mmEdtEmail.getText().toString();
                            String senha = mmEdtSenha.getText().toString();
                            String telefone = mmEdtTelefone.getText().toString();

                            if (mmChkTermo.isChecked()) {

                                if (CNP.isValidCPF(cpf.replace(".", "").replace("-", ""))) {

                                    usuario = new Usuario();
                                    usuario.setNome(nome);
                                    usuario.setEmail(email);
                                    usuario.setCpf(cpf);
                                    usuario.setSenha(senha);

                                    String[] telefones = new String[1];

                                    telefones[0] = telefone;

                                    usuario.setTelefones(telefones);

                                    this.cadastrar(usuario);
                                } else {
                                    mmEdtCpf.setError(resources.getString(R.string.cpf_invalido));
                                    mmEdtCpf.requestFocus();
                                }
                            } else {
                                mmChkTermo.setError(resources.getString(R.string.campo_obrigatorio));
                                mmChkTermo.requestFocus();
                            }
                        }
                    }
                }
            }
        }

        public void cadastrar(Usuario usuario){
            final ProgressDialog mmCarregando = ProgressDialog.show(CadastroActivity.this, getResources().getString(R.string.aguarde),"");
            networkHandler = NetworkHandler.getInstance();

            networkHandler.post(Util.URL_USUARIO, Usuario.class, usuario, new Callback<Usuario>() {
                @Override
                public void callback(Usuario retornoUsuario, Erro erro) {
                    if (retornoUsuario != null) {
                        Toast.makeText(CadastroActivity.this, resources.getString(R.string.cadastrado_com_sucesso), Toast.LENGTH_LONG).show();
                        mmCarregando.dismiss();
                        finish();
                    } else {
                        Toast.makeText(CadastroActivity.this, resources.getString(R.string.erro_ao_cadastrar), Toast.LENGTH_LONG).show();
                        mmCarregando.dismiss();
                    }
                }
            });
        }

    }

    private class TermoOnCheckedListener  implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                buttonView.setError(null);
            }
        }
    }

}
