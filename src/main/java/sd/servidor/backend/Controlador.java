package sd.servidor.backend;

// @Author Leonardo Bellato

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class Controlador {
    private final BD bancoDados;
    private final List<String> usuariosLogados;
    private final Gson gson;

    public Controlador(List<String> usuariosLogados) throws SQLException {
        this.usuariosLogados = usuariosLogados;
        gson = new Gson();
        this.bancoDados = BD.conectar();
    }

    public void desconectarBD(){
        if(bancoDados == null) return;
        try{
            bancoDados.desconectar();
        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
        }
    }

    public String cadastrarUsuario(String nome, String RA, String senha){
        try{
            if(this.bancoDados.encontrarUsuario(RA) != null)
                return gson.toJson(new Mensagem("103", "Already exists an account with the username.", null));

            nome = (nome.length() > 40) ? nome.substring(0,40) : nome;// Ajustando nome
            bancoDados.inserirUsuario(nome, RA, senha);
            return gson.toJson(new Mensagem("100", "Successful account creation", null), Mensagem.class);

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("104", "Unknown error.", null));
        }
    }

    public String logarUsuario(String RA, String senha){
        Usuario usuario;

        try{
            usuario = this.bancoDados.encontrarUsuario(RA);
            if(usuario == null || !usuario.comparePassword(senha))
                return gson.toJson(new Mensagem("003", "Login failed.", null));

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("003", "Login failed.", null));
        }

        synchronized (this.usuariosLogados) {
            if(!this.usuariosLogados.contains(RA)) {
                this.usuariosLogados.add(RA);
                return gson.toJson(new Mensagem("000", "Successful login.", RA));
            }
            else
                return gson.toJson(new Mensagem("004", "Already logged in.", RA));
        }
    }

    public String deslogarUsuario(String token){
        if(this.usuariosLogados.contains(token)){
            this.usuariosLogados.remove(token);
            return gson.toJson(new Mensagem("010", "Successful logout.", null), Mensagem.class);
        }
        return gson.toJson(new Mensagem("999", "Server error: token not found.", null));
    }

    public String processarRequisicao(String requisicaoJson){
        Mensagem req = gson.fromJson(requisicaoJson, Mensagem.class);

        switch(req.getOp()){
            case 1:
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null || req.getPassword().isBlank() || req.getName() == null || req.getName().isBlank())
                    return gson.toJson(new Mensagem("101", "Fields missing.", null));
                if(!validarRA(req.getUser()) || req.getPassword().length() != 4)
                    return gson.toJson(new Mensagem("102", "Invalid information inserted: user or password.", null));
                return cadastrarUsuario(req.getName(), req.getUser(), req.getPassword());

            //case 2:
            //case 3:
            //case 4:

            case 5:
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null || req.getPassword().isBlank())
                    return gson.toJson(new Mensagem("002", "Fields missing.", null));
                return logarUsuario(req.getUser(), req.getPassword());

            case 6:
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("011", "Fields missing.", null));
                this.desconectarBD();
                return deslogarUsuario(req.getToken());

            default:
                return gson.toJson(new Mensagem("999", "Server error: invalid operation field.", null));

        }
    }

    private boolean validarRA(String RA){
        if(RA.length() != 7) return false;
        String regex = "^[0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(RA).matches();
    }

}
