package sd.servidor;

// @Author Leonardo Bellato

import sd.servidor.backend.ConexaoTCP;
import sd.servidor.backend.Controlador;
import sd.servidor.backend.Pair;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        Scanner entradaUsuario;
        ServerSocket soqueteServidor = null;
        List<Pair<String,Boolean>> usuariosLogados = Collections.synchronizedList(new ArrayList<Pair<String,Boolean>>());

        try {
            System.out.println("O IP desta máquina é " + Inet4Address.getLocalHost().getHostAddress());
            System.out.print("Para iniciar o servidor, informe a porta: ");
            entradaUsuario = new Scanner(System.in);
            soqueteServidor = new ServerSocket(entradaUsuario.nextInt());
            entradaUsuario.close();

            System.out.println("Aguardando conexões");
            while(true) {
                new ConexaoTCP(soqueteServidor.accept(), new Controlador(usuariosLogados));
            }
        }
        catch (Exception e) {
            System.out.println("ERRO: " + e.getMessage());
        }
        finally {
            try {
                assert soqueteServidor != null;
                soqueteServidor.close();
            } catch (Exception e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }
}