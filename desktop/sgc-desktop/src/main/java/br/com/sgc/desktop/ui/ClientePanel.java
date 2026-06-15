package br.com.sgc.desktop.ui;

import br.com.sgc.desktop.api.ApiException;
import br.com.sgc.desktop.model.ClienteDTO;
import br.com.sgc.desktop.session.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Painel de gestão de clientes: listagem, criação, edição e remoção.
 * Consome os endpoints /clientes da API REST.
 */
public class ClientePanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<ClienteDTO> clientes;

    public ClientePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] colunas = {"ID", "Nome", "CPF", "E-mail", "Telefone", "Endereço"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton novoBtn = new JButton("Novo Cliente");
        JButton editarBtn = new JButton("Editar");
        JButton excluirBtn = new JButton("Excluir");
        JButton atualizarBtn = new JButton("Atualizar Lista");

        buttons.add(novoBtn);
        buttons.add(editarBtn);
        buttons.add(excluirBtn);
        buttons.add(atualizarBtn);
        add(buttons, BorderLayout.NORTH);

        novoBtn.addActionListener(e -> abrirFormulario(null));
        editarBtn.addActionListener(e -> editarSelecionado());
        excluirBtn.addActionListener(e -> excluirSelecionado());
        atualizarBtn.addActionListener(e -> carregarClientes());

        carregarClientes();
    }

    /** Recarrega a lista de clientes a partir da API. */
    void carregarClientes() {
        try {
            clientes = SessionManager.getInstance().getApiClient().getClientes();
            tableModel.setRowCount(0);
            for (ClienteDTO c : clientes) {
                tableModel.addRow(new Object[]{
                        c.getId(), c.getNome(), c.getCpf(), c.getEmail(), c.getTelefone(), c.getEndereco()
                });
            }
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar clientes:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarSelecionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela.");
            return;
        }
        abrirFormulario(clientes.get(row));
    }

    private void excluirSelecionado() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela.");
            return;
        }
        ClienteDTO c = clientes.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o cliente \"" + c.getNome() + "\"?",
                "Confirmar exclusão", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                SessionManager.getInstance().getApiClient().deleteCliente(c.getId());
                carregarClientes();
            } catch (ApiException ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir:\n" + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Abre um formulário para criar (clienteExistente == null) ou editar um cliente. */
    private void abrirFormulario(ClienteDTO clienteExistente) {
        JTextField nomeField = new JTextField(clienteExistente != null ? clienteExistente.getNome() : "");
        JTextField cpfField = new JTextField(clienteExistente != null ? clienteExistente.getCpf() : "");
        JTextField emailField = new JTextField(clienteExistente != null ? clienteExistente.getEmail() : "");
        JTextField telefoneField = new JTextField(clienteExistente != null ? clienteExistente.getTelefone() : "");
        JTextField enderecoField = new JTextField(clienteExistente != null ? clienteExistente.getEndereco() : "");

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Nome: *"));
        form.add(nomeField);
        form.add(new JLabel("CPF: *"));
        form.add(cpfField);
        form.add(new JLabel("E-mail: *"));
        form.add(emailField);
        form.add(new JLabel("Telefone:"));
        form.add(telefoneField);
        form.add(new JLabel("Endereço:"));
        form.add(enderecoField);

        String titulo = clienteExistente == null ? "Novo Cliente" : "Editar Cliente";

        int result = JOptionPane.showConfirmDialog(this, form, titulo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (nomeField.getText().isBlank() || cpfField.getText().isBlank() || emailField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "Os campos Nome, CPF e E-mail são obrigatórios.",
                    "Campos obrigatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClienteDTO dto = new ClienteDTO();
        dto.setNome(nomeField.getText().trim());
        dto.setCpf(cpfField.getText().trim());
        dto.setEmail(emailField.getText().trim());
        dto.setTelefone(telefoneField.getText().trim());
        dto.setEndereco(enderecoField.getText().trim());

        try {
            if (clienteExistente == null) {
                SessionManager.getInstance().getApiClient().createCliente(dto);
            } else {
                SessionManager.getInstance().getApiClient().updateCliente(clienteExistente.getId(), dto);
            }
            carregarClientes();
        } catch (ApiException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar cliente:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
