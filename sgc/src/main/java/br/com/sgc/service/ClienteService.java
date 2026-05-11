package br.com.sgc.service;

import br.com.sgc.domain.model.Cliente;
import br.com.sgc.domain.repository.ClienteRepository;
import br.com.sgc.domain.repository.VendaRepository;
import br.com.sgc.dto.ClienteDTO;
import br.com.sgc.exception.BusinessException;
import br.com.sgc.exception.ResourceNotFoundException;
import br.com.sgc.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;

    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(MapperUtil::toClienteDTO)
                .toList();
    }

    public ClienteDTO buscarPorId(Long id) {
        return MapperUtil.toClienteDTO(findOrThrow(id));
    }

    @Transactional
    public ClienteDTO criar(ClienteDTO dto) {
        // Regra: CPF não pode ser duplicado
        if (clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        // Regra: Email não pode ser duplicado
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + dto.getEmail());
        }

        Cliente cliente = MapperUtil.toCliente(dto);
        return MapperUtil.toClienteDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = findOrThrow(id);

        // Se CPF mudou, verifica duplicidade
        if (!cliente.getCpf().equals(dto.getCpf()) && clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        // Se email mudou, verifica duplicidade
        if (!cliente.getEmail().equals(dto.getEmail()) && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("E-mail já cadastrado: " + dto.getEmail());
        }

        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());

        return MapperUtil.toClienteDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void deletar(Long id) {
        findOrThrow(id);

        // Regra: cliente com vendas não pode ser removido
        if (vendaRepository.existsByClienteId(id)) {
            throw new BusinessException("Cliente possui vendas registradas e não pode ser removido.");
        }

        clienteRepository.deleteById(id);
    }

    private Cliente findOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}
