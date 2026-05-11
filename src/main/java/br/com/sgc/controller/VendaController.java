package br.com.sgc.controller;

import br.com.sgc.dto.VendaDTO;
import br.com.sgc.service.VendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
@Tag(name = "Vendas", description = "Registro e consulta de vendas")
@SecurityRequirement(name = "bearerAuth")
public class VendaController {

    private final VendaService vendaService;

    @GetMapping
    @Operation(summary = "Lista todas as vendas ou filtra por período/cliente")
    public ResponseEntity<List<VendaDTO>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        if (clienteId != null) {
            return ResponseEntity.ok(vendaService.buscarPorCliente(clienteId));
        }
        if (inicio != null && fim != null) {
            return ResponseEntity.ok(vendaService.buscarPorPeriodo(inicio, fim));
        }
        return ResponseEntity.ok(vendaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca venda por ID")
    public ResponseEntity<VendaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registra nova venda")
    public ResponseEntity<VendaDTO> registrar(@Valid @RequestBody VendaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendaService.registrar(dto));
    }
}
