package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.api.ApiException;
import br.com.sgc.desktop.model.ClienteDTO;
import br.com.sgc.desktop.model.VendaDTO;
import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel de relatórios:
 *  - Vendas Anuais: gráfico de barras com o total vendido por mês,
 *    para o ano selecionado (representação gráfica das vendas anuais)
 *  - Vendas por Cliente: lista todas as vendas de um cliente específico
 *
 * Consome os endpoints /vendas (com filtros de período e cliente) da API REST.
 */
public class RelatorioPanel extends JPanel {

    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] MESES = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };

    public RelatorioPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vendas Anuais", criarPainelVendasAnuais());
        tabs.addTab("Vendas por Cliente", criarPainelVendasPorCliente());

        add(tabs, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────────────────────────
    // ABA: VENDAS ANUAIS (GRÁFICO)
    // ──────────────────────────────────────────────────────────────────

    private JPanel criarPainelVendasAnuais() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int anoAtual = Year.now().getValue();
        JComboBox<Integer> anoCombo = new JComboBox<>();
        for (int ano = anoAtual; ano >= anoAtual - 4; ano--) {
            anoCombo.addItem(ano);
        }

        JButton gerarBtn = new JButton("Gerar Relatório");

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Ano:"));
        topo.add(anoCombo);
        topo.add(gerarBtn);

        BarChartPanel chartPanel = new BarChartPanel();

        JLabel totalAnualLabel = new JLabel("Total no ano: R$ 0.00");
        totalAnualLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalAnualLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(topo, BorderLayout.NORTH);
        painel.add(chartPanel, BorderLayout.CENTER);
        painel.add(totalAnualLabel, BorderLayout.SOUTH);

        gerarBtn.addActionListener(e -> {
            int ano = (Integer) anoCombo.getSelectedItem();
            gerarRelatorioAnual(ano, chartPanel, totalAnualLabel);
        });

        // Gera automaticamente para o ano atual ao abrir a aba
        gerarRelatorioAnual(anoAtual, chartPanel, totalAnualLabel);

        return painel;
    }

    private void gerarRelatorioAnual(int ano, BarChartPanel chartPanel, JLabel totalLabel) {
        try {
            LocalDateTime inicio = LocalDateTime.of(ano, 1, 1, 0, 0, 0);
            LocalDateTime fim = LocalDateTime.of(ano, 12, 31, 23, 59, 59);

            List<VendaDTO> vendas = SessionManager.getInstance().getApiClient()
                    .getVendasPorPeriodo(inicio.toString(), fim.toString());

            // Inicializa todos os meses com zero
            Map<String, BigDecimal> totalPorMes = new LinkedHashMap<>();
            for (String mes : MESES) {
                totalPorMes.put(mes, BigDecimal.ZERO);
            }

            BigDecimal totalAno = BigDecimal.ZERO;

            for (VendaDTO v : vendas) {
                if (v.getData() == null || v.getValorTotal() == null) {
                    continue;
                }
                int mesIndex = v.getData().getMonthValue() - 1; // 0-11
                String mesLabel = MESES[mesIndex];
                totalPorMes.put(mesLabel, totalPorMes.get(mesLabel).add(v.getValorTotal()));
                totalAno = totalAno.add(v.getValorTotal());
            }

            chartPanel.setDados(totalPorMes, "Vendas por mês - " + ano);
            totalLabel.setText("Total no ano " + ano + ": R$ " + totalAno.setScale(2, java.math.RoundingMode.HALF_UP));

        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar relatório:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // ABA: VENDAS POR CLIENTE
    // ──────────────────────────────────────────────────────────────────

    private JPanel criarPainelVendasPorCliente() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<ClienteDTO> clienteCombo = new JComboBox<>();
        clienteCombo.setPreferredSize(new Dimension(300, 26));

        JButton buscarBtn = new JButton("Buscar Vendas");
        JButton atualizarClientesBtn = new JButton("Atualizar Clientes");

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Cliente:"));
        topo.add(clienteCombo);
        topo.add(buscarBtn);
        topo.add(atualizarClientesBtn);

        String[] colunas = {"ID", "Data", "Vendedor", "Qtd. Itens", "Total (R$)"};
        DefaultTableModel tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);

        JLabel totalClienteLabel = new JLabel("Total: R$ 0.00");
        totalClienteLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalClienteLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(table), BorderLayout.CENTER);
        painel.add(totalClienteLabel, BorderLayout.SOUTH);

        Runnable carregarClientes = () -> {
            try {
                List<ClienteDTO> clientes = SessionManager.getInstance().getApiClient().getClientes();
                clienteCombo.removeAllItems();
                for (ClienteDTO c : clientes) {
                    clienteCombo.addItem(c);
                }
            } catch (ApiException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao carregar clientes:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        };

        atualizarClientesBtn.addActionListener(e -> carregarClientes.run());

        buscarBtn.addActionListener(e -> {
            ClienteDTO cliente = (ClienteDTO) clienteCombo.getSelectedItem();
            if (cliente == null) {
                JOptionPane.showMessageDialog(this, "Selecione um cliente.");
                return;
            }
            try {
                List<VendaDTO> vendas = SessionManager.getInstance().getApiClient()
                        .getVendasPorCliente(cliente.getId());

                tableModel.setRowCount(0);
                BigDecimal total = BigDecimal.ZERO;
                for (VendaDTO v : vendas) {
                    tableModel.addRow(new Object[]{
                            v.getId(),
                            v.getData() != null ? v.getData().format(DATA_FORMAT) : "",
                            v.getUsuarioUsername(),
                            v.getItens() != null ? v.getItens().size() : 0,
                            v.getValorTotal()
                    });
                    if (v.getValorTotal() != null) {
                        total = total.add(v.getValorTotal());
                    }
                }
                totalClienteLabel.setText("Total comprado por " + cliente.getNome()
                        + ": R$ " + total.setScale(2, java.math.RoundingMode.HALF_UP)
                        + "  (" + vendas.size() + " venda(s))");

            } catch (ApiException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao buscar vendas:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        carregarClientes.run();

        return painel;
    }
}
