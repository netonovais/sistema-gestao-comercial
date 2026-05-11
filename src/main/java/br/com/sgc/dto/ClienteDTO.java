package br.com.sgc.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "E-mail é obrigatório")
    private String email;

    private String telefone;
    private String endereco;
}
