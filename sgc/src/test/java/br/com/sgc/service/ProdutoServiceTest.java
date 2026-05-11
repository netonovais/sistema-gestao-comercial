package br.com.sgc.service;

import br.com.sgc.domain.model.Produto;
import br.com.sgc.domain.repository.ProdutoRepository;
import br.com.sgc.dto.ProdutoDTO;
import br.com.sgc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock private ProdutoRepository produtoRepository;

    @InjectMocks private ProdutoService produtoService;

    @Test
    void deveCriarProdutoComSucesso() {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setNome("Notebook Dell");
        dto.setPreco(new BigDecimal("3500.00"));
        dto.setQuantidadeEstoque(10);
        dto.setEstoqueMinimo(2);

        Produto salvo = Produto.builder()
                .id(1L).nome("Notebook Dell")
                .preco(new BigDecimal("3500.00"))
                .quantidadeEstoque(10).estoqueMinimo(2).build();

        when(produtoRepository.save(any(Produto.class))).thenReturn(salvo);

        ProdutoDTO resultado = produtoService.criar(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNome()).isEqualTo("Notebook Dell");
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveVerificarEstoqueSuficiente() {
        Produto p = Produto.builder()
                .id(1L).nome("Mouse").preco(BigDecimal.TEN)
                .quantidadeEstoque(5).estoqueMinimo(1).build();

        assertThat(p.temEstoqueSuficiente(5)).isTrue();
        assertThat(p.temEstoqueSuficiente(6)).isFalse();
    }
}
