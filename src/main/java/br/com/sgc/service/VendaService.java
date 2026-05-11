package br.com.sgc.service;

import br.com.sgc.domain.model.*;
import br.com.sgc.domain.repository.*;
import br.com.sgc.dto.*;
import br.com.sgc.exception.*;
import br.com.sgc.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoService produtoService;
    private final UsuarioRepository usuarioRepository;

    public List<VendaDTO> listarTodas() {
        return vendaRepository.findAll()
                .stream()
                .map(MapperUtil::toVendaDTO)
                .toList();
    }

    public VendaDTO buscarPorId(Long id) {
        return MapperUtil.toVendaDTO(findOrThrow(id));
    }

    public List<VendaDTO> buscarPorCliente(Long clienteId) {
        return vendaRepository.findByClienteId(clienteId)
                .stream()
                .map(MapperUtil::toVendaDTO)
                .toList();
    }

    public List<VendaDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return vendaRepository.findByPeriodo(inicio, fim)
                .stream()
                .map(MapperUtil::toVendaDTO)
                .toList();
    }

    @Transactional
    public VendaDTO registrar(VendaDTO dto) {
        // Regra: venda deve ter itens
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new BusinessException("A venda deve conter pelo menos um item.");
        }

        // Busca cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", dto.getClienteId()));

        // Busca usuário logado
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Monta a venda
        Venda venda = Venda.builder()
                .data(LocalDateTime.now())
                .cliente(cliente)
                .usuario(usuario)
                .itens(new ArrayList<>())
                .valorTotal(java.math.BigDecimal.ZERO)
                .build();

        // Processa cada item
        for (ItemVendaDTO itemDTO : dto.getItens()) {
            Produto produto = produtoService.findEntityOrThrow(itemDTO.getProdutoId());

            // Regra: verificar estoque suficiente
            if (!produto.temEstoqueSuficiente(itemDTO.getQuantidade())) {
                throw new BusinessException(
                        "Estoque insuficiente para o produto: " + produto.getNome() +
                        ". Disponível: " + produto.getQuantidadeEstoque() +
                        ", Solicitado: " + itemDTO.getQuantidade());
            }

            ItemVenda item = ItemVenda.builder()
                    .venda(venda)
                    .produto(produto)
                    .quantidade(itemDTO.getQuantidade())
                    .precoUnitario(produto.getPreco())
                    .build();

            venda.getItens().add(item);

            // Regra: atualiza estoque após venda
            produto.diminuirEstoque(itemDTO.getQuantidade());
        }

        // Calcula total automaticamente
        venda.calcularTotal();

        return MapperUtil.toVendaDTO(vendaRepository.save(venda));
    }

    private Venda findOrThrow(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda", id));
    }
}
