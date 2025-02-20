package sd.servidor.frontend;

// @Author Leonardo Bellato

import sd.servidor.backend.Controlador;
import sd.servidor.backend.Usuario;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

// Classe que permite logar e cadastrar
public class JanelaPrincipal extends JFrame {
    private final Controlador controlador;
    private DefaultListModel<Usuario> modelUsuarios = new DefaultListModel<>();;
    private JList<Usuario> listaUsuarios = new JList<>(modelUsuarios);
    private JScrollPane usuariosPane = new JScrollPane(listaUsuarios);

    Font fontePrincipal;
    Font fonteSecundaria;

    public JanelaPrincipal(String ip, String porta, Controlador controlador) {
        super();
        this.controlador = controlador;
        this.montarInterface(ip, porta);
    }

    public void montarInterface(String ip, String porta) {
        // Configurações iniciais
        this.setResizable(false);
        this.setLocationRelativeTo(null); // Posiciona em relação ao pai
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Fechando aplicação...");
                System.exit(0); // Encerra todas as threads e finaliza o programa
            }
        });
        this.setSize(370, 370);

        // Definindo fontes
        fontePrincipal = new Font("Arial", Font.PLAIN, 14);

        // Criando layout
        listaUsuarios.setFont(new Font("Arial", Font.PLAIN, 14));
        usuariosPane.setSize(new Dimension(350, 350));
        usuariosPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        usuariosPane.setBorder(BorderFactory.createTitledBorder("Usuários logados"));

        // Ajusta o tamanho da fonte do título
        TitledBorder titledBorder = (TitledBorder) usuariosPane.getBorder();
        titledBorder.setTitleFont(new Font("Arial", Font.BOLD, 14));

        // Criando o label com IP e porta em negrito
        JLabel label = new JLabel("<html>Servidor ativo em: <b>" + ip + ":" + porta + "</b></html>", SwingConstants.CENTER);
        label.setFont(fontePrincipal);

        // Criando um painel para adicionar espaço abaixo do label
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(label, BorderLayout.CENTER);
        labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // Espaço de 10px abaixo do label

        // Criando painel principal para adicionar padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Adicionando padding

        // Adicionando componentes ao painel principal
        mainPanel.add(labelPanel, BorderLayout.NORTH);
        mainPanel.add(usuariosPane, BorderLayout.CENTER);

        // Adicionando o painel principal ao frame
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);

        this.setVisible(true);

        controlador.setGuiListener((usuario) -> SwingUtilities.invokeLater(() -> {
            try {
                if (this.modelUsuarios == null) throw new Exception("Não carregou model");
                if (!this.modelUsuarios.removeElement(usuario))
                    this.modelUsuarios.addElement(usuario);
                this.usuariosPane.updateUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }));
    }


}
