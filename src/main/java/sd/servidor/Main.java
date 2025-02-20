

package sd.servidor;

// @Author Leonardo Bellato

import sd.servidor.backend.*;
import sd.servidor.frontend.JanelaPrincipal;

import javax.swing.*;
import java.awt.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Usuario> usuariosLogados = Collections.synchronizedList(new ArrayList<>());

        try {
            String ip = Inet4Address.getLocalHost().getHostAddress();
            String porta = JOptionPane.showInputDialog(null, "Informe a porta para iniciar o servidor:",
                    "Configuração do Servidor", JOptionPane.QUESTION_MESSAGE);

            if (porta == null || porta.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Porta inválida. Encerrando aplicação.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int portaServidor = Integer.parseInt(porta);
            Controlador controlador = new Controlador(usuariosLogados);

            // Exibir tela com IP do servidor
            SwingUtilities.invokeLater(() -> {new JanelaPrincipal(ip, String.valueOf(portaServidor), controlador);});

            // Iniciar o servidor em uma thread separada
            new Thread(() -> iniciarServidor(portaServidor, usuariosLogados, controlador)).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao iniciar o servidor: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void iniciarServidor(int porta, List<Usuario> usuariosLogados, Controlador controlador) {
        try (ServerSocket soqueteServidor = new ServerSocket(porta)) {
            System.out.println("Servidor iniciado na porta " + porta);
            System.out.println("Aguardando conexões...");

            while (true) {
                new Conexao(soqueteServidor.accept(), controlador, BD.conectar());
            }
        } catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
    }
}
