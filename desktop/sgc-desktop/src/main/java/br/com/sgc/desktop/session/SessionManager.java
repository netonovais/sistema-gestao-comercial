package br.com.sgc.desktop.session;

import br.com.sgc.desktop.api.ApiClient;

/**
 * Mantém o estado da sessão do usuário logado:
 * cliente de API autenticado, username e perfil.
 *
 * Implementado como Singleton para ser acessado
 * facilmente por todas as telas (panels) da aplicação.
 */
public class SessionManager {

    private static SessionManager instance;

    private ApiClient apiClient;
    private String username;
    private String perfil;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(perfil);
    }

    public void clear() {
        this.apiClient = null;
        this.username = null;
        this.perfil = null;
    }
}
