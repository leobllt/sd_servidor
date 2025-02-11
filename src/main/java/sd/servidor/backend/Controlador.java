package sd.servidor.backend;

// @Author Leonardo Bellato

import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class Controlador {
    private final BD bancoDados;
    private final List<Pair<String,Boolean>> usuariosLogados;
    private final List<Categoria> categorias;
    private final Gson gson;

    public Controlador(List<Pair<String,Boolean>> usuariosLogados, List<Categoria> categorias) throws SQLException {
        this.usuariosLogados = usuariosLogados;
        this.categorias = categorias;
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
            this.bancoDados.inserirUsuario(nome, RA, senha);
            return gson.toJson(new Mensagem("100", "Successful account creation", null), Mensagem.class);

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("104", "Unknown error.", null));
        }
    }

    public String consultarUsuario(String RA, String token){
        try {
            boolean admin = this.bancoDados.encontrarUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta pesquisar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("113", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca o usuario
            Usuario usuario = this.bancoDados.encontrarUsuario(RA);
            if(usuario == null) {
                if (admin)
                    return gson.toJson(new Mensagem("114", "User not found.", null));
                else
                    return gson.toJson(new Mensagem("115", "Unknown error.", null));
            }
            else
                return gson.toJson(new Mensagem(
                        (usuario.isAdmin()) ? "111" : "110",
                        "Returns all information of the account.",
                        usuario.getRA(),
                        usuario.getSenha(),
                        usuario.getNome()
                ));

        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("115", "Unknown error.", null));
        }
    }

    public String editarUsuario(String RA, String token, String nome, String senha){
        try {
            boolean admin = this.bancoDados.encontrarUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta editar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("122", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca e edita o usuario
            Usuario usuario = this.bancoDados.encontrarUsuario(RA);
            if(usuario == null) {
                if (admin)
                    return gson.toJson(new Mensagem("123", "No user or token found.", null));
                else
                    return gson.toJson(new Mensagem("124", "Unknown error.", null));
            }
            else {
                if(nome == null) nome = usuario.getNome();
                if(senha == null) senha = usuario.getSenha();
                this.bancoDados.editarUsuario(RA, nome, senha);

                return gson.toJson(new Mensagem("120", "Account successfully updated.", null));
            }

        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("124", "Unknown error.", null));
        }
    }

    public String excluirUsuario(String RA, String token){
        try {
            boolean admin = this.bancoDados.encontrarUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta pesquisar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("133", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca o usuario
            Usuario usuario = this.bancoDados.encontrarUsuario(RA);
            if(usuario == null) {
                if (admin)
                    return gson.toJson(new Mensagem("134", "User not found.", null));
                else
                    return gson.toJson(new Mensagem("135", "Unknown error.", null));
            }
            else {
                this.bancoDados.deletarUsuario(RA);
                return gson.toJson(new Mensagem("130", "Account successfully deleted.", null));
            }
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("135", "Unknown error.", null));
        }
    }

    public String logarUsuario(String RA, String senha){
        Usuario usuario;

        try{
            usuario = this.bancoDados.encontrarUsuario(RA);
            if(usuario == null || !usuario.getSenha().equals(senha))
                return gson.toJson(new Mensagem("003", "Login failed.", null));

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("005", "Unknown error.", null));
        }

        synchronized (this.usuariosLogados) {
            if(!this.usuariosLogados.contains(new Pair<>(RA, usuario.isAdmin()))) {
                this.usuariosLogados.add(new Pair<>(RA, usuario.isAdmin()));
                Mensagem msg = new Mensagem((usuario.isAdmin()) ? "001" : "000", "Successful login.", null);
                msg.setToken(usuario.getRA());
                return gson.toJson(msg);
            }
            else
                return gson.toJson(new Mensagem("004", "Already logged in.", null));
        }
    }

    public String deslogarUsuario(String token){
        if(this.usuariosLogados.remove(new Pair<>(token, false))){
            return gson.toJson(new Mensagem("010", "Successful logout.", null), Mensagem.class);
        }
        return gson.toJson(new Mensagem("012", "User not logged in.", null));
    }

    public String processarRequisicao(String requisicaoJson){
        Mensagem req = gson.fromJson(requisicaoJson, Mensagem.class);

        if(req.getOp() == null)
            return gson.toJson(new Mensagem("401", "Op missing.", null));

        switch(req.getOp()){
            case "1":
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null || req.getPassword().isBlank() || req.getName() == null || req.getName().isBlank())
                    return gson.toJson(new Mensagem("101", "Fields missing.", null));
                if(!validarRA(req.getUser()) || req.getPassword().length() != 4)
                    return gson.toJson(new Mensagem("102", "Invalid information inserted: user or password.", null));
                return cadastrarUsuario(req.getName(), req.getUser(), req.getPassword());

            case "2":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("112", "Invalid or empty token.", null));
                return consultarUsuario(req.getUser(), req.getToken());

            case "3":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("121", "Invalid or empty token.", null));
                return editarUsuario(req.getUser(), req.getToken(), req.getName(), req.getPassword());

            case "4":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("132", "Invalid token.", null));
                return excluirUsuario(req.getUser(), req.getToken());

            case "5":
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null || req.getPassword().isBlank())
                    return gson.toJson(new Mensagem("002", "Fields missing.", null));
                return logarUsuario(req.getUser(), req.getPassword());

            case "6":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("011", "Fields missing.", null));
                return deslogarUsuario(req.getToken());

            default:
                return gson.toJson(new Mensagem("402", "Invalid op.", null));

        }
    }

    private boolean validarRA(String RA){
        if(RA.length() != 7) return false;
        String regex = "^[0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(RA).matches();
    }

}