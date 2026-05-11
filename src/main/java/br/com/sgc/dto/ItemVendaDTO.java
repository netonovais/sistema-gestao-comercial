package br.com.sgc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemVendaDTO {

    private Long id;

    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;

    private String produtoNome;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade mínima é 1")
    private Integer quantidade;

    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
