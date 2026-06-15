package br.com.sgc.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaDTO {

    private Long id;
    private LocalDateTime data;
    private Long clienteId;
    private String clienteNome;
    private String usuarioUsername;
    private List<ItemVendaDTO> itens = new ArrayList<>();
    private BigDecimal valorTotal;

    public VendaDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }

    public List<ItemVendaDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemVendaDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
