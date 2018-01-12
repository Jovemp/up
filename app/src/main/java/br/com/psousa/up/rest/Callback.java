package br.com.psousa.up.rest;

import br.com.psousa.up.model.Erro;

/**
 * Created by Paulo on 01/03/2016.
 */
public interface Callback<T> {

    void callback(T t, Erro erro);

}
