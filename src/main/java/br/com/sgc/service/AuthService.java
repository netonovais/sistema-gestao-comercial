package br.com.sgc.service;

import br.com.sgc.config.JwtService;
import br.com.sgc.domain.enums.PerfilUsuario;
import br.com.sgc.domain.model.Usuario;
import br.com.sgc.domain.repository.UsuarioRepository;
import br.com.sgc.dto.AuthRequestDTO;
import br.com.sgc.dto.AuthResponseDTO;
import br.com.sgc.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getSenha()));

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String token = jwtService.gerarToken(usuario);
        return new AuthResponseDTO(token, usuario.getUsername(), usuario.getPerfil().name());
    }

    public AuthResponseDTO registrar(String username, String senha, PerfilUsuario perfil) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessException("Username já está em uso");
        }

        Usuario usuario = Usuario.builder()
                .username(username)
                .senha(passwordEncoder.encode(senha))
                .perfil(perfil)
                .build();

        usuarioRepository.save(usuario);
        String token = jwtService.gerarToken(usuario);
        return new AuthResponseDTO(token, usuario.getUsername(), usuario.getPerfil().name());
    }
}
