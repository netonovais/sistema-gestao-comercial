package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.api.ApiException;
import br.com.sgc.desktop.model.ProdutoDTO;
import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel de gestão de produtos: listagem, criação, edição,
 * remoção e indicação visual de estoque baixo.
 * Consome os endpoints /produtos da API REST.
 */
public class ProdutoPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<ProdutoDTO> produtos;

    public ProdutoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "Descrição", "Preço (R$)", "Estoque", "Estoque Mín."};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);

        // Destaca em vermelho linhas com estoque <= estoque mínimo
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected && produtos != null && row < produtos.size()) {
                    ProdutoDTO p = produtos.get(row);
                    if (p.getQuantidadeEstoque() != null && p.getEstoqueMinimo() != null
                            && p.getQuantidadeEstoque() <= p.getEstoqueMinimo()) {
                        c.setBackground(new Color(255, 224, 224));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton novoBtn = new JButton("Novo Produto");
        JButton editarBtn = new JButton("Editar");
        JButton excluirBtn = new JButton("Excluir");
        JButton atualizarBtn = new JButton("Atualizar Lista");

        buttons.add(novoBtn);
        buttons.add(editarBtn);
        buttons.add(excluirBtn);
        buttons.add(atualizarBtn);

        JLabel legenda = new JLabel("   Linhas em vermelho = estoque no nível mínimo ou abaixo");
        legenda.setForeground(Color.GRAY);
        buttons.add(legenda);

        add(buttons, BorderLayout.NORTH);

        novoBtn.addActionListener(e -> abrirFormulario(null));
        editarBtn.addActionListener(e -> editarSelecionado());
        excluirBtn.addActionListener(e -> excluirSelecionado());
        atualizarBtn.addActionListener(e -> carregarProdutos());

        carregarProdutos();
    }

    /** Recarrega a lista de produtos a partir da API. */
    void carregarProdutos() {
        try {
            produtos = SessionManager.getInstance().getApiClient().getProdutos();
            tableModel.setRowCount(0);
            for (ProdutoDTO p : produtos) {
                tableModel.addRow(new Object[]{
                        p.getId(), p.getNome(), p.getDescricao(),
                        p.getPreco(), p.getQuantidadeEstoque(), p.getEstoqueMinimo()
                });
            }
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar produtos:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarSelecionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela.");
            return;
        }
        abrirFormulario(produtos.get(row));
    }

    private void excluirSelecionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela.");
            return;
        }
        ProdutoDTO p = produtos.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o produto \"" + p.getNome() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                SessionManager.getInstance().getApiClient().deleteProduto(p.getId());
                carregarProdutos();
            } catch (ApiException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Abre um formulário para criar (produtoExistente == null) ou editar um produto. */
    private void abrirFormulario(ProdutoDTO produtoExistente) {
        JTextField nomeField = new JTextField(produtoExistente != null ? produtoExistente.getNome() : "");
        JTextField descricaoField = new JTextField(produtoExistente != null ? produtoExistente.getDescricao() : "");
        JTextField precoField = new JTextField(produtoExistente != null && produtoExistente.getPreco() != null
                ? produtoExistente.getPreco().toString() : "");
        JTextField estoqueField = new JTextField(produtoExistente != null && produtoExistente.getQuantidadeEstoque() != null
                ? produtoExistente.getQuantidadeEstoque().toString() : "0");
        JTextField estoqueMinField = new JTextField(produtoExistente != null && produtoExistente.getEstoqueMinimo() != null
                ? produtoExistente.getEstoqueMinimo().toString() : "0");

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Nome: *"));
        form.add(nomeField);
        form.add(new JLabel("Descrição:"));
        form.add(descricaoField);
        form.add(new JLabel("Preço (ex: 99.90): *"));
        form.add(precoField);
        form.add(new JLabel("Quantidade em estoque: *"));
        form.add(estoqueField);
        form.add(new JLabel("Estoque mínimo:"));
        form.add(estoqueMinField);

        String titulo = produtoExistente == null ? "Novo Produto" : "Editar Produto";

        int result = JOptionPane.showConfirmDialog(this, form, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (nomeField.getText().isBlank() || precoField.getText().isBlank() || estoqueField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Os campos Nome, Preço e Quantidade em estoque são obrigatórios.",
                    "Campos obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal preco;
        int estoque;
        int estoqueMin;
        try {
            preco = new BigDecimal(precoField.getText().trim().replace(",", "."));
            estoque = Integer.parseInt(estoqueField.getText().trim());
            estoqueMin = estoqueMinField.getText().isBlank() ? 0 : Integer.parseInt(estoqueMinField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Preço e quantidades devem ser números válidos.\n"
                            + "Use ponto ou vírgula para decimais (ex: 99.90).",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "O preço deve ser maior que zero.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (estoque < 0 || estoqueMin < 0) {
            JOptionPane.showMessageDialog(this, "As quantidades de estoque não podem ser negativas.",
                    "Valor inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProdutoDTO dto = new ProdutoDTO();
        dto.setNome(nomeField.getText().trim());
        dto.setDescricao(descricaoField.getText().trim());
        dto.setPreco(preco);
        dto.setQuantidadeEstoque(estoque);
        dto.setEstoqueMinimo(estoqueMin);

        try {
            if (produtoExistente == null) {
                SessionManager.getInstance().getApiClient().createProduto(dto);
            } else {
                SessionManager.getInstance().getApiClient().updateProduto(produtoExistente.getId(), dto);
            }
            carregarProdutos();
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar produto:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
