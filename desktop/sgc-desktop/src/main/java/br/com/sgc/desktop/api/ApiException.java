package br.com.sgc.desktop.api;

/**
 * Exceção lançada quando a API retorna um erro
 * (status HTTP >= 400) ou ocorre um problema de conexão.
 */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
