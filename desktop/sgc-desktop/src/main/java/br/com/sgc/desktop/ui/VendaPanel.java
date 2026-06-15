package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.api.ApiException;
import br.com.sgc.desktop.model.ClienteDTO;
import br.com.sgc.desktop.model.ItemVendaDTO;
import br.com.sgc.desktop.model.ProdutoDTO;
import br.com.sgc.desktop.model.VendaDTO;
import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Painel de vendas:
 *  - Parte superior: montagem e registro de uma nova venda
 *    (seleção de cliente, adição de itens, cálculo automático do total)
 *  - Parte inferior: histórico de vendas registradas
 *
 * Consome os endpoints /vendas, /clientes e /produtos da API REST.
 */
public class VendaPanel extends JPanel {

    private static final DateTimeFormatter DATA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Combos
    private JComboBox<ClienteDTO> clienteCombo;
    private JComboBox<ProdutoDTO> produtoCombo;
    private JSpinner quantidadeSpinner;

    // Itens da venda em construção
    private final List<ItemVendaDTO> itensVenda = new ArrayList<>();
    private DefaultTableModel itensModel;
    private JTable itensTable;
    private JLabel totalLabel;

    // Histórico de vendas
    private DefaultTableModel vendasModel;
    private JTable vendasTable;
    private List<VendaDTO> vendas;

    public VendaPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                criarPainelNovaVenda(), criarPainelHistorico());
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerLocation(330);

        add(splitPane, BorderLayout.CENTER);

        carregarClientes();
        carregarProdutos();
        carregarVendas();
    }

    // ──────────────────────────────────────────────────────────────────
    // PAINEL: NOVA VENDA
    // ──────────────────────────────────────────────────────────────────

    private JPanel criarPainelNovaVenda() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Registrar Nova Venda"));

        // Linha: seleção de cliente
        JPanel clientePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clienteCombo = new JComboBox<>();
        clienteCombo.setPreferredSize(new Dimension(300, 26));
        clientePanel.add(new JLabel("Cliente:"));
        clientePanel.add(clienteCombo);

        JButton atualizarClientesBtn = new JButton("Atualizar Clientes");
        atualizarClientesBtn.addActionListener(e -> carregarClientes());
        clientePanel.add(atualizarClientesBtn);

        // Linha: seleção de produto + quantidade + adicionar
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        produtoCombo = new JComboBox<>();
        produtoCombo.setPreferredSize(new Dimension(300, 26));
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        quantidadeSpinner.setPreferredSize(new Dimension(70, 26));

        JButton adicionarItemBtn = new JButton("Adicionar Item");
        JButton removerItemBtn = new JButton("Remover Item Selecionado");
        JButton atualizarProdutosBtn = new JButton("Atualizar Produtos");

        itemPanel.add(new JLabel("Produto:"));
        itemPanel.add(produtoCombo);
        itemPanel.add(new JLabel("Qtd:"));
        itemPanel.add(quantidadeSpinner);
        itemPanel.add(adicionarItemBtn);
        itemPanel.add(removerItemBtn);
        itemPanel.add(atualizarProdutosBtn);

        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.add(clientePanel);
        topo.add(itemPanel);

        // Tabela de itens da venda
        String[] colunas = {"Produto", "Quantidade", "Preço Unit. (R$)", "Subtotal (R$)"};
        itensModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        itensTable = new JTable(itensModel);
        itensTable.setRowHeight(22);

        // Rodapé: total + botão registrar
        JPanel rodape = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: R$ 0.00");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        totalLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton registrarBtn = new JButton("Registrar Venda");
        registrarBtn.setFont(new Font("SansSerif", Font.BOLD, 13));

        rodape.add(totalLabel, BorderLayout.WEST);
        rodape.add(registrarBtn, BorderLayout.EAST);

        painel.add(topo, BorderLayout.NORTH);
        painel.add(new JScrollPane(itensTable), BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);

        adicionarItemBtn.addActionListener(e -> adicionarItem());
        removerItemBtn.addActionListener(e -> removerItemSelecionado());
        atualizarProdutosBtn.addActionListener(e -> carregarProdutos());
        registrarBtn.addActionListener(e -> registrarVenda());

        return painel;
    }

    private void carregarClientes() {
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
    }

    private void carregarProdutos() {
        try {
            List<ProdutoDTO> produtos = SessionManager.getInstance().getApiClient().getProdutos();
            produtoCombo.removeAllItems();
            for (ProdutoDTO p : produtos) {
                produtoCombo.addItem(p);
            }
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar produtos:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarItem() {
        ProdutoDTO produto = (ProdutoDTO) produtoCombo.getSelectedItem();
        if (produto == null) {
            JOptionPane.showMessageDialog(this, "Nenhum produto disponível. Cadastre um produto primeiro.");
            return;
        }

        int quantidade = (Integer) quantidadeSpinner.getValue();

        // Se o produto já está na lista, soma a quantidade
        for (ItemVendaDTO item : itensVenda) {
            if (item.getProdutoId().equals(produto.getId())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
                atualizarTabelaItens();
                return;
            }
        }

        ItemVendaDTO item = new ItemVendaDTO();
        item.setProdutoId(produto.getId());
        item.setProdutoNome(produto.getNome());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));

        itensVenda.add(item);
        atualizarTabelaItens();
    }

    private void removerItemSelecionado() {
        int row = itensTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item da venda para remover.");
            return;
        }
        itensVenda.remove(row);
        atualizarTabelaItens();
    }

    private void atualizarTabelaItens() {
        itensModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVendaDTO item : itensVenda) {
            itensModel.addRow(new Object[]{
                    item.getProdutoNome(), item.getQuantidade(),
                    item.getPrecoUnitario(), item.getSubtotal()
            });
            total = total.add(item.getSubtotal());
        }
        totalLabel.setText("Total: R$ " + total.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private void registrarVenda() {
        ClienteDTO cliente = (ClienteDTO) clienteCombo.getSelectedItem();
        if (cliente == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um cliente. Se não houver clientes, cadastre um na aba Clientes.",
                    "Cliente obrigatório", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (itensVenda.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Adicione pelo menos um item à venda.",
                    "Itens obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        VendaDTO dto = new VendaDTO();
        dto.setClienteId(cliente.getId());
        dto.setItens(itensVenda);

        try {
            VendaDTO registrada = SessionManager.getInstance().getApiClient().registrarVenda(dto);

            JOptionPane.showMessageDialog(this,
                    "Venda registrada com sucesso!\n"
                            + "Cliente: " + registrada.getClienteNome() + "\n"
                            + "Total: R$ " + registrada.getValorTotal(),
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);

            // Limpa formulário e recarrega listas (estoque mudou)
            itensVenda.clear();
            atualizarTabelaItens();
            carregarProdutos();
            carregarVendas();

        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao registrar venda:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // PAINEL: HISTÓRICO DE VENDAS
    // ──────────────────────────────────────────────────────────────────

    private JPanel criarPainelHistorico() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        painel.setBorder(BorderFactory.createTitledBorder("Vendas Registradas"));

        String[] colunas = {"ID", "Data", "Cliente", "Vendedor", "Qtd. Itens", "Total (R$)"};
        vendasModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        vendasTable = new JTable(vendasModel);
        vendasTable.setRowHeight(22);

        painel.add(new JScrollPane(vendasTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton atualizarBtn = new JButton("Atualizar Histórico");
        JButton detalhesBtn = new JButton("Ver Itens da Venda Selecionada");
        buttons.add(atualizarBtn);
        buttons.add(detalhesBtn);
        painel.add(buttons, BorderLayout.NORTH);

        atualizarBtn.addActionListener(e -> carregarVendas());
        detalhesBtn.addActionListener(e -> verDetalhes());

        return painel;
    }

    void carregarVendas() {
        try {
            vendas = SessionManager.getInstance().getApiClient().getVendas();
            vendasModel.setRowCount(0);
            for (VendaDTO v : vendas) {
                vendasModel.addRow(new Object[]{
                        v.getId(),
                        v.getData() != null ? v.getData().format(DATA_FORMAT) : "",
                        v.getClienteNome(),
                        v.getUsuarioUsername(),
                        v.getItens() != null ? v.getItens().size() : 0,
                        v.getValorTotal()
                });
            }
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar vendas:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void verDetalhes() {
        int row = vendasTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma venda na tabela.");
            return;
        }
        VendaDTO venda = vendas.get(row);

        StringBuilder sb = new StringBuilder();
        sb.append("Venda #").append(venda.getId()).append("\n");
        sb.append("Cliente: ").append(venda.getClienteNome()).append("\n");
        sb.append("Data: ").append(venda.getData() != null ? venda.getData().format(DATA_FORMAT) : "").append("\n\n");
        sb.append("Itens:\n");
        for (ItemVendaDTO item : venda.getItens()) {
            sb.append(" - ").append(item.getProdutoNome())
                    .append(" | Qtd: ").append(item.getQuantidade())
                    .append(" | Unit: R$ ").append(item.getPrecoUnitario())
                    .append(" | Subtotal: R$ ").append(item.getSubtotal())
                    .append("\n");
        }
        sb.append("\nTotal: R$ ").append(venda.getValorTotal());

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
                "Detalhes da Venda", JOptionPane.PLAIN_MESSAGE);
    }
}
