package br.com.sgc.desktop;

import br.com.sgc.desktop.ui.LoginFrame;

import javax.swing.*;

/**
 * Ponto de entrada da aplicação desktop (Swing) do SGC.
 *
 * Esta aplicação consome a API REST do backend Spring Boot
 * (projeto "sgc"), que deve estar rodando antes de iniciar
 * esta interface (por padrão em http://localhost:8080).
 */
public class DesktopApplication {

    public static void main(String[] args) {
        // Look and Feel nativo do sistema operacional
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se falhar, usa o padrão do Swing
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
