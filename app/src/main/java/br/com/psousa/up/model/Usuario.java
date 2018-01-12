package br.com.psousa.up.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Paulo on 27/12/2016.
 */

public class Usuario implements Parcelable {

    private String _id;
    private String nome;
    private String email;
    private String senha;
    private String id_facebook;
    private String tooken_facebook;
    private String cpf;
    private String[] telefones;
    private boolean termo_1;

    public Usuario(){

    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getId_facebook() {
        return id_facebook;
    }

    public void setId_facebook(String id_facebook) {
        this.id_facebook = id_facebook;
    }

    public String getTooken_facebook() {
        return tooken_facebook;
    }

    public void setTooken_facebook(String tooken_facebook) {
        this.tooken_facebook = tooken_facebook;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String[] getTelefones() {
        return telefones;
    }

    public void setTelefones(String[] telefones) {
        this.telefones = telefones;
    }

    public boolean isTermo_1() {
        return termo_1;
    }

    public void setTermo_1(boolean termo_1) {
        this.termo_1 = termo_1;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(senha);
        dest.writeString(id_facebook);
        dest.writeString(tooken_facebook);
        dest.writeString(cpf);
        dest.writeStringArray(telefones);
    }

    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

    // "De-parcel object
    public Usuario(Parcel in) {
        _id = in.readString();
        nome = in.readString();
        email = in.readString();
        senha = in.readString();
        id_facebook = in.readString();
        tooken_facebook = in.readString();
        cpf = in.readString();
        telefones = in.createStringArray();
        in.readStringArray(telefones);
    }



}
