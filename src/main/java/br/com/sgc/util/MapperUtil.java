package br.com.sgc.util;

import br.com.sgc.domain.model.*;
import br.com.sgc.dto.*;

/**
 * Utilitário de mapeamento entre Entidades e DTOs.
 *
 * Design Pattern aplicado: DTO (Data Transfer Object)
 * Motivo: Isolar a camada de domínio da camada de apresentação,
 * evitando expor detalhes internos das entidades JPA na API REST.
 */
public class MapperUtil {

    private MapperUtil() { /* utilitário estático */ }

    // ── Cliente ──────────────────────────────────────────────────────────

    public static ClienteDTO toClienteDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setCpf(c.getCpf());
        dto.setEmail(c.getEmail());
        dto.setTelefone(c.getTelefone());
        dto.setEndereco(c.getEndereco());
        return dto;
    }

    public static Cliente toCliente(ClienteDTO dto) {
        return Cliente.builder()
                .nome(dto.getNome())
                .cpf(dto.getCpf())
                .email(dto.getEmail())
                .telefone(dto.getTelefone())
                .endereco(dto.getEndereco())
                .build();
    }

    // ── Produto ──────────────────────────────────────────────────────────

    public static ProdutoDTO toProdutoDTO(Produto p) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setPreco(p.getPreco());
        dto.setQuantidadeEstoque(p.getQuantidadeEstoque());
        dto.setEstoqueMinimo(p.getEstoqueMinimo());
        return dto;
    }

    public static Produto toProduto(ProdutoDTO dto) {
        return Produto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .preco(dto.getPreco())
                .quantidadeEstoque(dto.getQuantidadeEstoque())
                .estoqueMinimo(dto.getEstoqueMinimo() != null ? dto.getEstoqueMinimo() : 0)
                .build();
    }

    // ── Venda ────────────────────────────────────────────────────────────

    public static VendaDTO toVendaDTO(Venda v) {
        VendaDTO dto = new VendaDTO();
        dto.setId(v.getId());
        dto.setData(v.getData());
        dto.setClienteId(v.getCliente().getId());
        dto.setClienteNome(v.getCliente().getNome());
        dto.setUsuarioUsername(v.getUsuario().getUsername());
        dto.setValorTotal(v.getValorTotal());
        dto.setItens(v.getItens().stream().map(MapperUtil::toItemVendaDTO).toList());
        return dto;
    }

    public static ItemVendaDTO toItemVendaDTO(ItemVenda i) {
        ItemVendaDTO dto = new ItemVendaDTO();
        dto.setId(i.getId());
        dto.setProdutoId(i.getProduto().getId());
        dto.setProdutoNome(i.getProduto().getNome());
        dto.setQuantidade(i.getQuantidade());
        dto.setPrecoUnitario(i.getPrecoUnitario());
        dto.setSubtotal(i.getSubtotal());
        return dto;
    }
}
