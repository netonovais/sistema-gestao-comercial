package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.api.ApiClient;
import br.com.sgc.desktop.api.ApiException;
import br.com.sgc.desktop.model.AuthResponseDTO;
import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Tela inicial: permite informar o endereço do backend,
 * usuário e senha, e autentica via POST /auth/login.
 */
public class LoginFrame extends JFrame {

    private final JTextField urlField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel statusLabel;
    private final JButton loginButton;

    public LoginFrame() {
        setTitle("SGC - Sistema de Gestão Comercial");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Sistema de Gestão Comercial", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        JLabel subtitulo = new JLabel("Faça login para continuar", SwingConstants.CENTER);
        subtitulo.setForeground(Color.GRAY);
        gbc.gridy = 1;
        panel.add(subtitulo, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Servidor:"), gbc);

        urlField = new JTextField("http://localhost:8080");
        gbc.gridx = 1;
        panel.add(urlField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Usuário:"), gbc);

        userField = new JTextField("admin");
        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Senha:"), gbc);

        passField = new JPasswordField();
        gbc.gridx = 1;
        panel.add(passField, gbc);

        loginButton = new JButton("Entrar");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 6;
        panel.add(statusLabel, gbc);

        JLabel hint = new JLabel("<html><center>Usuários padrão: admin / admin123<br>ou funcionario / func123</center></html>");
        hint.setForeground(Color.GRAY);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        gbc.gridy = 7;
        panel.add(hint, gbc);

        add(panel);

        loginButton.addActionListener(e -> doLogin());
        passField.addActionListener(e -> doLogin());

        getRootPane().setDefaultButton(loginButton);
    }

    private void doLogin() {
        String url = urlField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            mostrarErro("Preencha todos os campos.");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setText("Conectando...");

        try {
            ApiClient client = new ApiClient(url);
            AuthResponseDTO auth = client.login(user, pass);

            SessionManager session = SessionManager.getInstance();
            session.setApiClient(client);
            session.setUsername(auth.getUsername());
            session.setPerfil(auth.getPerfil());

            new MainFrame().setVisible(true);
            dispose();

        } catch (ApiException ex) {
            mostrarErro("Erro: " + ex.getMessage());
        } catch (Exception ex) {
            mostrarErro("Erro inesperado: " + ex.getMessage());
        } finally {
            loginButton.setEnabled(true);
        }
    }

    private void mostrarErro(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(msg);
    }
}
