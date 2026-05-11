package br.com.sgc.service;

import br.com.sgc.domain.model.Cliente;
import br.com.sgc.domain.repository.ClienteRepository;
import br.com.sgc.domain.repository.VendaRepository;
import br.com.sgc.dto.ClienteDTO;
import br.com.sgc.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private VendaRepository vendaRepository;

    @InjectMocks private ClienteService clienteService;

    private ClienteDTO dto;

    @BeforeEach
    void setup() {
        dto = new ClienteDTO();
        dto.setNome("João Silva");
        dto.setCpf("123.456.789-00");
        dto.setEmail("joao@email.com");
        dto.setTelefone("(61) 99999-0000");
        dto.setEndereco("Rua Teste, 123");
    }

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpf(dto.getCpf())).thenReturn(false);
        when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        Cliente clienteSalvo = Cliente.builder()
                .id(1L).nome(dto.getNome()).cpf(dto.getCpf()).email(dto.getEmail()).build();
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteSalvo);

        ClienteDTO resultado = clienteService.criar(dto);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("João Silva");
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoQuandoCpfDuplicado() {
        when(clienteRepository.existsByCpf(dto.getCpf())).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");
    }

    @Test
    void deveLancarExcecaoAoDeletarClienteComVendas() {
        Cliente cliente = Cliente.builder().id(1L).nome("João").cpf("000").email("j@j.com").build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vendaRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.deletar(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vendas registradas");
    }
}
