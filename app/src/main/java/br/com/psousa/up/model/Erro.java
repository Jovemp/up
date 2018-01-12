package br.com.psousa.up.model;

/**
 * Created by Paulo on 30/12/2016.
 */

public class Erro {

    private int status;
    private String descricao;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {

        this.descricao = descricao;
    }
}
