package br.com.sgc.desktop.api;

import br.com.sgc.desktop.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP responsável por toda a comunicação com a API REST do SGC.
 * Usa java.net.http.HttpClient (nativo do Java) + Jackson para JSON.
 */
public class ApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String token;

    public ApiClient(String baseUrl) {
        // remove barra final, se existir
        this.baseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setToken(String token) {
        this.token = token;
    }

    // ──────────────────────────────────────────────────────────────────
    // AUTENTICAÇÃO
    // ──────────────────────────────────────────────────────────────────

    public AuthResponseDTO login(String username, String senha) throws ApiException {
        AuthRequestDTO request = new AuthRequestDTO(username, senha);

        String jsonBody = toJson(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        String body = send(httpRequest);
        AuthResponseDTO auth = fromJson(body, AuthResponseDTO.class);
        this.token = auth.getToken();
        return auth;
    }

    // ──────────────────────────────────────────────────────────────────
    // CLIENTES
    // ──────────────────────────────────────────────────────────────────

    public List<ClienteDTO> getClientes() throws ApiException {
        String body = doGet("/clientes");
        return fromJsonList(body, ClienteDTO.class);
    }

    public ClienteDTO createCliente(ClienteDTO dto) throws ApiException {
        String body = doPost("/clientes", toJson(dto));
        return fromJson(body, ClienteDTO.class);
    }

    public ClienteDTO updateCliente(Long id, ClienteDTO dto) throws ApiException {
        String body = doPut("/clientes/" + id, toJson(dto));
        return fromJson(body, ClienteDTO.class);
    }

    public void deleteCliente(Long id) throws ApiException {
        doDelete("/clientes/" + id);
    }

    // ──────────────────────────────────────────────────────────────────
    // PRODUTOS
    // ──────────────────────────────────────────────────────────────────

    public List<ProdutoDTO> getProdutos() throws ApiException {
        String body = doGet("/produtos");
        return fromJsonList(body, ProdutoDTO.class);
    }

    public ProdutoDTO createProduto(ProdutoDTO dto) throws ApiException {
        String body = doPost("/produtos", toJson(dto));
        return fromJson(body, ProdutoDTO.class);
    }

    public ProdutoDTO updateProduto(Long id, ProdutoDTO dto) throws ApiException {
        String body = doPut("/produtos/" + id, toJson(dto));
        return fromJson(body, ProdutoDTO.class);
    }

    public void deleteProduto(Long id) throws ApiException {
        doDelete("/produtos/" + id);
    }

    // ──────────────────────────────────────────────────────────────────
    // VENDAS
    // ──────────────────────────────────────────────────────────────────

    public List<VendaDTO> getVendas() throws ApiException {
        String body = doGet("/vendas");
        return fromJsonList(body, VendaDTO.class);
    }

    public List<VendaDTO> getVendasPorCliente(Long clienteId) throws ApiException {
        String body = doGet("/vendas?clienteId=" + clienteId);
        return fromJsonList(body, VendaDTO.class);
    }

    public List<VendaDTO> getVendasPorPeriodo(String inicioIso, String fimIso) throws ApiException {
        String body = doGet("/vendas?inicio=" + inicioIso + "&fim=" + fimIso);
        return fromJsonList(body, VendaDTO.class);
    }

    public VendaDTO registrarVenda(VendaDTO dto) throws ApiException {
        String body = doPost("/vendas", toJson(dto));
        return fromJson(body, VendaDTO.class);
    }

    // ──────────────────────────────────────────────────────────────────
    // MÉTODOS HTTP GENÉRICOS
    // ──────────────────────────────────────────────────────────────────

    private String doGet(String path) throws ApiException {
        HttpRequest request = baseRequest(path)
                .GET()
                .build();
        return send(request);
    }

    private String doPost(String path, String jsonBody) throws ApiException {
        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return send(request);
    }

    private String doPut(String path, String jsonBody) throws ApiException {
        HttpRequest request = baseRequest(path)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return send(request);
    }

    private String doDelete(String path) throws ApiException {
        HttpRequest request = baseRequest(path)
                .DELETE()
                .build();
        return send(request);
    }

    private HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private String send(HttpRequest request) throws ApiException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            String body = response.body();

            if (status >= 200 && status < 300) {
                return body;
            }

            throw new ApiException(status, extractErrorMessage(body, status));

        } catch (IOException e) {
            throw new ApiException(0,
                    "Não foi possível conectar ao servidor (" + baseUrl + "). "
                            + "Verifique se o backend está rodando.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Requisição interrompida.");
        }
    }

    private String extractErrorMessage(String body, int status) {
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("mensagem")) {
                return node.get("mensagem").asText();
            }
            if (node.has("erro")) {
                return node.get("erro").asText();
            }
        } catch (Exception ignored) {
            // corpo não é JSON, usa texto puro abaixo
        }
        if (body == null || body.isBlank()) {
            return "Erro HTTP " + status;
        }
        return body;
    }

    // ──────────────────────────────────────────────────────────────────
    // SERIALIZAÇÃO JSON
    // ──────────────────────────────────────────────────────────────────

    private String toJson(Object obj) throws ApiException {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new ApiException(0, "Erro ao gerar JSON: " + e.getMessage());
        }
    }

    private <T> T fromJson(String json, Class<T> type) throws ApiException {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ApiException(0, "Erro ao interpretar resposta do servidor: " + e.getMessage());
        }
    }

    private <T> List<T> fromJsonList(String json, Class<T> type) throws ApiException {
        try {
            return objectMapper.readerFor(objectMapper.getTypeFactory().constructCollectionType(List.class, type))
                    .readValue(json);
        } catch (JsonProcessingException e) {
            throw new ApiException(0, "Erro ao interpretar resposta do servidor: " + e.getMessage());
        }
    }
}
