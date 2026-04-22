package common;

import java.io.Serializable;

public class Dispositivo implements Serializable {

    private String id;
    private String tipo;
    private String valor;

    public Dispositivo(String id, String tipo, String valor) {
        this.id = id;
        this.tipo = tipo;
        this.valor = valor;
    }

    public String getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return id + " (" + tipo + ") -> " + valor;
    }
}
