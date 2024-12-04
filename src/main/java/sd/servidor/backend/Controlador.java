package sd.servidor.backend;

// @Author Leonardo Bellato

import com.google.gson.Gson;

import java.sql.SQLException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Controlador {
    private BD bancoDados;
    private final SecureRandom secureRandom;
    private final Base64.Encoder base64Encoder;
    private HashMap<String,String> usuariosLogados;
    private Gson gson;

    public Controlador() {
        secureRandom= new SecureRandom(); //threadsafe
        base64Encoder = Base64.getUrlEncoder(); //threadsafe
        usuariosLogados = new HashMap<>();
        gson = new Gson();
    }

    public boolean conectarBD(){
        if(bancoDados != null) return false;

        try{
            bancoDados = BD.conectar();
            return true;
        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return false;
        }
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
            return gson.toJson(new Mensagem("999", "Server error: database error.", null));
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
            return gson.toJson(new Mensagem("999", "Server error: database error.", null));
        }

        if(this.usuariosLogados.containsKey(RA))
            return gson.toJson(new Mensagem(usuario.isAdmin() ? "001" : "000", "Successful login.", this.usuariosLogados.get(RA)));

        // Gerando novo token
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        String token =  base64Encoder.encodeToString(randomBytes);
        this.usuariosLogados.put(RA, token);
        return gson.toJson(new Mensagem("000", "Successful login.", token));
    }

    public String deslogarUsuario(String token){
        for(String RA : this.usuariosLogados.keySet()){
            if(this.usuariosLogados.get(RA).equals(token)) {
                this.usuariosLogados.remove(RA);
                return gson.toJson(new Mensagem("010", "Successful logout.", null), Mensagem.class);
            }
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
