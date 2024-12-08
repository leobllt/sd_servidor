package sd.servidor;

// @Author Leonardo Bellato

import sd.servidor.backend.ConexaoTCP;
import sd.servidor.backend.Controlador;

import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Controlador controlador = new Controlador();
        if(!controlador.conectarBD()) System.exit(1);

        Scanner entradaUsuario;
        ServerSocket soqueteServidor = null;

        try {
            System.out.println("O IP desta máquina é " + Inet4Address.getLocalHost().getHostAddress());
            System.out.print("Para iniciar o servidor, informe a porta: ");
            entradaUsuario = new Scanner(System.in);
            soqueteServidor = new ServerSocket(entradaUsuario.nextInt());
            entradaUsuario.close();

            System.out.println("Aguardando conexões");
            while(true)
                new ConexaoTCP(soqueteServidor.accept(), controlador);
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

        controlador.desconectarBD();
    }
}