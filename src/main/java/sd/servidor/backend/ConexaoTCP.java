package sd.servidor.backend;

// @Author Leonardo Bellato

import java.io.*;
import java.net.Socket;

public class ConexaoTCP extends Thread {
    // Exclusivo de cada thread:
    private Socket soqueteCliente;
    private Controlador controlador;
    private BufferedReader entradaSoquete;
    private PrintWriter saidaSoquete;
    private boolean executando;

    public ConexaoTCP(Socket soqueteCliente, Controlador controlador) {
        this.soqueteCliente = soqueteCliente;
        this.controlador = controlador;
        executando = true;
        System.out.println("Conectado com " + soqueteCliente.getInetAddress().getHostAddress() + ":" + soqueteCliente.getPort());
        start();
    }

    public void run(){
        while(this.executando && !this.soqueteCliente.isClosed()){
            try{
                this.entradaSoquete = new BufferedReader(new InputStreamReader(this.soqueteCliente.getInputStream()));
                this.saidaSoquete = new PrintWriter(this.soqueteCliente.getOutputStream(), true);
                this.comunicar();
            } catch (Exception e) {
                System.out.println("ERRO: " + e.getMessage());
            }
        }
    }

    public void comunicar() throws IOException {
        // Aguardando requisição
        String requisicaoJson = this.entradaSoquete.readLine();
        if(requisicaoJson != null) {
            System.out.println("CLIENTE " + this.soqueteCliente.getInetAddress().getHostAddress() + ": " + requisicaoJson);
            // Processando pedido
            String respostaJson = this.controlador.processarRequisicao(requisicaoJson);
            // Respondendo
            this.saidaSoquete.println(respostaJson);
            System.out.println("SERVIDOR: " + respostaJson);
            // Verificando se é logout
            if (respostaJson.contains("010")) {
                this.encerrarSoquete();
                this.executando = false;
            }
        } else {
            this.encerrarSoquete();
            this.executando = false;
        }
    }

    public void encerrarSoquete() throws IOException{
        this.entradaSoquete.close();
        this.saidaSoquete.close();
        this.soqueteCliente.close();
        System.out.println("Conexão com " + this.soqueteCliente.getInetAddress().getHostAddress() + " encerrada.");
    }
}

