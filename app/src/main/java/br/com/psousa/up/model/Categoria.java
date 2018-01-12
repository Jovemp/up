package br.com.psousa.up.model;

/**
 * Created by Paulo on 20/02/2017.
 */

public class Categoria {

    private String _id;
    private String descricao;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao.toUpperCase();
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public String toString() {
        return this.descricao;
    }
}
