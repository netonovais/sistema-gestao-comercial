package br.com.sgc.service;

import br.com.sgc.domain.model.Produto;
import br.com.sgc.domain.repository.ProdutoRepository;
import br.com.sgc.dto.ProdutoDTO;
import br.com.sgc.exception.ResourceNotFoundException;
import br.com.sgc.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public List<ProdutoDTO> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(MapperUtil::toProdutoDTO)
                .toList();
    }

    public ProdutoDTO buscarPorId(Long id) {
        return MapperUtil.toProdutoDTO(findOrThrow(id));
    }

    public List<ProdutoDTO> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(MapperUtil::toProdutoDTO)
                .toList();
    }

    public List<ProdutoDTO> listarEstoqueBaixo() {
        return produtoRepository.findProdutosComEstoqueBaixo()
                .stream()
                .map(MapperUtil::toProdutoDTO)
                .toList();
    }

    @Transactional
    public ProdutoDTO criar(ProdutoDTO dto) {
        Produto produto = MapperUtil.toProduto(dto);
        return MapperUtil.toProdutoDTO(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoDTO dto) {
        Produto produto = findOrThrow(id);

        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setQuantidadeEstoque(dto.getQuantidadeEstoque());
        produto.setEstoqueMinimo(dto.getEstoqueMinimo() != null ? dto.getEstoqueMinimo() : 0);

        return MapperUtil.toProdutoDTO(produtoRepository.save(produto));
    }

    @Transactional
    public void deletar(Long id) {
        findOrThrow(id);
        produtoRepository.deleteById(id);
    }

    // Método interno usado pelo VendaService
    public Produto findEntityOrThrow(Long id) {
        return findOrThrow(id);
    }

    private Produto findOrThrow(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id));
    }
}
