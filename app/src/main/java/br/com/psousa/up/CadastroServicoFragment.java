package br.com.psousa.up;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import br.com.psousa.up.model.Categoria;
import br.com.psousa.up.model.Erro;
import br.com.psousa.up.model.Servico;
import br.com.psousa.up.rest.Callback;
import br.com.psousa.up.rest.NetworkHandler;
import br.com.psousa.up.util.Util;


public class CadastroServicoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TOKEN = "token";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mToken;
    private String mParam2;

    private static ImageView mImgFotoServico;
    private EditText mEdtDescricao;
    private EditText mEdtContato;
    private Spinner mSpnCategoria;
    private Button mBtnSalvar;
    private String caminhoCamera;

    private Uri file;
    private Servico servico;

    private Resources resources;

    private NetworkHandler networkHandler;

    public CadastroServicoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param2 Parameter 2.
     * @return A new instance of fragment CadastroServicoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CadastroServicoFragment newInstance(String token, String param2) {
        CadastroServicoFragment fragment = new CadastroServicoFragment();
        Bundle args = new Bundle();
        args.putString(TOKEN, token);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mToken = getArguments().getString(TOKEN);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_cadastro_servico, container, false);

        resources = getResources();

        mImgFotoServico = (ImageView) v.findViewById(R.id.img_cadastro_servico_imagem);
        mEdtContato = (EditText) v.findViewById(R.id.edt_cadastro_servico_contato);
        mEdtDescricao = (EditText) v.findViewById(R.id.edt_cadastro_servico_descricao);
        mSpnCategoria = (Spinner) v.findViewById(R.id.spn_cadastro_servico_categoria);
        mBtnSalvar = (Button) v.findViewById(R.id.btn_cadastro_servico_enviar);


        mImgFotoServico.setOnClickListener(new FotoOnClickListener());
        mBtnSalvar.setOnClickListener(new EnviarOnClickListener());

        servico = new Servico();


        buscaCategoria();

        return v;
    }

    public void adicionarFoto(Bitmap bitmap, String caminho){
        if (caminho == "") {
            Bitmap bit = BitmapFactory.decodeFile(caminhoCamera);
            mImgFotoServico.setImageBitmap(bit);
            mImgFotoServico.setTag(caminhoCamera);
        } else {
            mImgFotoServico.setImageBitmap(bitmap);
            mImgFotoServico.setTag(caminho);
        }
    }

    public void buscaCategoria(){
        final ProgressDialog mmCarregando = ProgressDialog.show(getActivity(), getResources().getString(R.string.aguarde),"");
        networkHandler = NetworkHandler.getInstance();

        networkHandler.readList(Util.URL_CATEGORIA, mToken, Categoria[].class, new Callback<List<Categoria>>() {
            @Override
            public void callback(List<Categoria> retorno, Erro erro) {
                if (retorno != null) {
                    // Create an ArrayAdapter using the string array and a default spinner layout
                    Categoria c = new Categoria();
                    c.setDescricao(resources.getString(R.string.vazio));
                    retorno.add(0, c);
                    ArrayAdapter<Categoria> adapter = new ArrayAdapter<Categoria>(getActivity(),
                            android.R.layout.simple_spinner_item, retorno);
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    // Apply the adapter to the spinner
                    mSpnCategoria.setAdapter(adapter);
                    mmCarregando.dismiss();
                } else {
                    Toast.makeText(getActivity(), resources.getString(R.string.erro_ao_cadastrar), Toast.LENGTH_LONG).show();
                    mmCarregando.dismiss();
                }
            }
        });
    }


    private class EnviarOnClickListener implements  View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!Util.validarEditVazio(mEdtDescricao, resources)) {
                if (!Util.validarSpinnerVazio(mSpnCategoria, resources)) {
                    if (!Util.validarEditVazio(mEdtContato, resources)) {
                        preencherCampos();
                        final ProgressDialog mmCarregando = ProgressDialog.show(getActivity(), getResources().getString(R.string.aguarde),"");
                        networkHandler = NetworkHandler.getInstance();

                        networkHandler.write(Util.URL_SERVICO, mToken, servico, Servico.class, servico, new Callback<Servico>() {
                            @Override
                            public void callback(Servico retornoUsuario, Erro erro) {
                                if (retornoUsuario != null) {
                                    Toast.makeText(getActivity(), resources.getString(R.string.cadastrado_com_sucesso), Toast.LENGTH_LONG).show();
                                    mmCarregando.dismiss();
                                } else {
                                    Toast.makeText(getActivity(), resources.getString(R.string.erro_ao_cadastrar), Toast.LENGTH_LONG).show();
                                    mmCarregando.dismiss();
                                }
                            }
                        });
                    }
                }
            }
        }

        private void preencherCampos(){
            servico.setCategoria((Categoria) mSpnCategoria.getSelectedItem());
            servico.setContato(mEdtContato.getText().toString());
            servico.setDescricao(mEdtDescricao.getText().toString());
            //Bitmap bmp = drawableToBitmap(referenciaImagemInicial);
            servico.setFoto(new File((String) mImgFotoServico.getTag()));
            servico.setAtivo(true);
        }



        public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                        Bitmap.CompressFormat format, int quality) {

            File imageFile = new File(dir,fileName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);

                bm.compress(format,quality,fos);

                fos.close();

                return true;
            }
            catch (IOException e) {
                Log.e("UP",e.getMessage());
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return false;
        }


    }

    private class FotoOnClickListener implements View.OnClickListener {

        AlertDialog alerta;

        @Override
        public void onClick(View v) {
            ArrayList<String> itens = new ArrayList<String>();
            itens.add("Galeria");
            itens.add("Camera");

            //adapter utilizando um layout customizado (TextView)
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.lista_menu_item, itens);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Escolha uma forma");
            //define o diálogo como uma lista, passa o adapter.
            builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    if (arg1 == 0){
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        getActivity().startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), PrincipalActivity.ACTION_GALERIA);
                    } else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        file = Uri.fromFile(getOutputMediaFile());
                        caminhoCamera = "";
                        String[] projection = { MediaStore.Images.Media.DATA };

                        Cursor cursor = getActivity().getContentResolver().query(file, projection, null, null, null);
                        if( cursor != null ){
                            cursor.moveToFirst();
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            caminhoCamera = cursor.getString(column_index);
                            cursor.close();
                        }
                        if (caminhoCamera == "") {
                            caminhoCamera = file.getPath();
                        }

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);
                        getActivity().startActivityForResult(takePictureIntent, PrincipalActivity.ACTIOn_CAMERA);
                    }
                    alerta.dismiss();
                }
            });

            alerta = builder.create();
            alerta.show();

        }

        private File getOutputMediaFile(){
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "UP Imagem");

            if (!mediaStorageDir.exists()){
                if (!mediaStorageDir.mkdirs()){
                    return null;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        }
    }


}
