package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal da aplicação.
 * Contém as abas: Clientes, Produtos, Vendas e Relatórios.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        SessionManager session = SessionManager.getInstance();

        setTitle("SGC - Sistema de Gestão Comercial   |   Usuário: "
                + session.getUsername() + "  (" + session.getPerfil() + ")");
        setSize(1000, 650);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Barra superior com info do usuário e botão de logout
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel userLabel = new JLabel("Conectado como: " + session.getUsername()
                + "  —  Perfil: " + session.getPerfil());
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton logoutButton = new JButton("Sair");
        logoutButton.addActionListener(e -> logout());

        topBar.add(userLabel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        // Abas principais
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", new ClientePanel());
        tabs.addTab("Produtos", new ProdutoPanel());
        tabs.addTab("Vendas", new VendaPanel());
        tabs.addTab("Relatórios", new RelatorioPanel());

        setLayout(new BorderLayout());
        add(topBar, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void logout() {
        SessionManager.getInstance().clear();
        new LoginFrame().setVisible(true);
        dispose();
    }
}
