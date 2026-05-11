package br.com.sgc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VendaDTO {

    private Long id;

    private LocalDateTime data;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    private String clienteNome;

    private String usuarioUsername;

    @NotEmpty(message = "Venda deve ter pelo menos um item")
    private List<ItemVendaDTO> itens;

    private BigDecimal valorTotal;
}
