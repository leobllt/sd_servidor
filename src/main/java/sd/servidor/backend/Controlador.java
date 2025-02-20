package sd.servidor.backend;

// @Author Leonardo Bellato

import com.google.gson.Gson;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Controlador {
    private final List<Usuario> usuariosLogados;
    private Gson gson;
    private AtualizacaoGUIListener guiListener;

    public Controlador(List<Usuario> usuariosLogados) {
        this.usuariosLogados = usuariosLogados;
        gson = new Gson();
    }

    public void setGuiListener(AtualizacaoGUIListener listener) {
        this.guiListener = listener;
    }

    public void desconectarBD(BD bancoDados){
        if(bancoDados == null) return;
        try{
            bancoDados.desconectar();
        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
        }
    }

    public String cadastrarUsuario(BD bancoDados, String nome, String RA, String senha){
        try{
            if(bancoDados.encontrarUsuario(RA) != null)
                return gson.toJson(new Mensagem("103", "Already exists an account with the username.", null));

            nome = (nome.length() > 40) ? nome.substring(0,40) : nome;// Ajustando nome
            bancoDados.inserirUsuario(nome, RA, senha);
            return gson.toJson(new Mensagem("100", "Successful account creation", null), Mensagem.class);

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("104", "Unknown error.", null));
        }
    }

    public String consultarUsuario(BD bancoDados, String RA, String token){
        try {
            boolean admin = this.qualUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta pesquisar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("113", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca o usuario
            Usuario usuario = bancoDados.encontrarUsuario(RA);
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

    public String editarUsuario(BD bancoDados, String RA, String token, String nome, String senha){
        try {
            boolean admin = this.qualUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta editar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("122", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca e edita o usuario
            Usuario usuario = bancoDados.encontrarUsuario(RA);
            if(usuario == null) {
                if (admin)
                    return gson.toJson(new Mensagem("123", "No user or token found.", null));
                else
                    return gson.toJson(new Mensagem("124", "Unknown error.", null));
            }
            else {
                if(nome == null || nome.isBlank()) nome = usuario.getNome();
                nome = (nome.length() > 40) ? nome.substring(0,40) : nome;// Ajustando nome

                if(senha == null || senha.isBlank()) senha = usuario.getSenha();
                if(senha.length() != 4)
                    return gson.toJson(new Mensagem("124", "Unknown error.", null));

                bancoDados.editarUsuario(RA, nome, senha);
                return gson.toJson(new Mensagem("120", "Account successfully updated.", null));
            }

        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("124", "Unknown error.", null));
        }
    }

    public String excluirUsuario(BD bancoDados, String RA, String token){
        try {
            boolean admin = this.qualUsuario(token).isAdmin();

            if(RA == null || RA.isBlank() || RA.equals(token)) // usuario tenta pesquisar a si proprio
                RA = token;
            else if(!admin) // usuario nao for admin
                return gson.toJson(new Mensagem("133", "Invalid Permission, user does not have permission to visualize other users data", null));

            // de fato busca o usuario
            Usuario usuario = bancoDados.encontrarUsuario(RA);
            if(usuario == null) {
                if (admin)
                    return gson.toJson(new Mensagem("134", "User not found.", null));
                else
                    return gson.toJson(new Mensagem("135", "Unknown error.", null));
            }
            else {
                bancoDados.deletarUsuario(RA);
                return gson.toJson(new Mensagem("130", "Account successfully deleted.", null));
            }
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("135", "Unknown error.", null));
        }
    }

    public String logarUsuario(BD bancoDados, String RA, String senha){
        Usuario usuario;

        try{
            usuario = bancoDados.encontrarUsuario(RA);
            if(usuario == null || !usuario.getSenha().equals(senha))
                return gson.toJson(new Mensagem("003", "Login failed.", null));

        } catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("005", "Unknown error.", null));
        }

        synchronized (this.usuariosLogados) {
            if(!this.usuariosLogados.contains(usuario)) {
                usuario.setToken(RA);
                this.usuariosLogados.add(usuario);
                Mensagem msg = new Mensagem((usuario.isAdmin()) ? "001" : "000", "Successful login.", null);
                msg.setToken(usuario.getRA());
                // atualiza gui
                guiListener.atualizarGUI(usuario);
                return gson.toJson(msg);
            }
            else
                return gson.toJson(new Mensagem("004", "Already logged in.", null));
        }
    }

    public String deslogarUsuario(String token){
        Usuario usuario = qualUsuario(token);
        if(this.usuariosLogados.remove(usuario)){
            guiListener.atualizarGUI(usuario);
            return gson.toJson(new Mensagem("010", "Successful logout.", null), Mensagem.class);
        }
        return gson.toJson(new Mensagem("012", "User not logged in.", null));
    }

    /* Aviso:
                Meu programa vai lendo as categorias e cadastrando normalmente.
                Se encontrar uma categoria com erro (faltando campo), vai parar o cadastro e retornar erro.
                Porém, as categorias cadastradas antes ficaram salvas.
    */
    public String cadastrarCategorias(BD bancoDados, String token, Categoria[] categorias){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin()) // usuario nao for admin
                return gson.toJson(new Mensagem("202", "Invalid token.", null));

            for(Categoria c: categorias){
                if(c == null)
                    return gson.toJson(new Mensagem("400", "Invalid JSON.", null));

                if(c.getName() == null || c.getName().isBlank())
                    return gson.toJson(new Mensagem("201", "Fields missing.", null));
                if(c.getDescription() == null) c.setDescription("");

                bancoDados.inserirCategoria(c.getName(), c.getDescription());
            }

            return gson.toJson(new Mensagem("200", "Successful category creation.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("203", "Unknown error.", null));
        }
    }

    public String consultarCategorias(BD bancoDados, String token){
        try {
            if(!validarRA(token))
                return gson.toJson(new Mensagem("212", "Invalid token.", null));

            return gson.toJson(new Mensagem(
                    "210",
                    "Successful category read.",
                    bancoDados.listarCategorias(Integer.parseInt(qualUsuario(token).getId()))
            ));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("213", "Unknown error.", null));
        }
    }

    private Categoria procurarCategoria(BD bancoDados, String id) throws SQLException{
        return bancoDados.encontrarCategoria(Integer.parseInt(id));
    }

    public String editarCategorias(BD bancoDados, String token, Categoria[] categorias){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin()) // usuario nao for admin
                return gson.toJson(new Mensagem("222", "Invalid token.", null));

            if(categorias == null || categorias.length == 0)
                return gson.toJson(new Mensagem("221", "Fields missing.", null));

            for(Categoria c: categorias){
                if(c == null)
                    return gson.toJson(new Mensagem("400", "Invalid JSON.", null));

                if(c.getId() == null || c.getId().isBlank())
                    return gson.toJson(new Mensagem("221", "Fields missing.", null));

                Categoria temp = this.procurarCategoria(bancoDados, c.getId());
                if(temp == null)
                    return gson.toJson(new Mensagem("223", "Invalid information inserted.", null));

                if(c.getName() == null || c.getName().isBlank()) c.setName(temp.getName());
                if(c.getDescription() == null || c.getDescription().isBlank()) c.setDescription("");

                bancoDados.editarCategoria(Integer.parseInt(c.getId()), c.getName(), c.getDescription());
            }

            return gson.toJson(new Mensagem("220", "Successful category update.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("224", "Unknown error.", null));
        }
        catch (Exception e) {
            return gson.toJson(new Mensagem("224", "Unknown error.", null));
        }
    }

    public String excluirCategorias(BD bancoDados, String token, String[] categoriasIds){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin()) // usuario nao for admin
                return gson.toJson(new Mensagem("232", "Invalid token.", null));

            if(categoriasIds == null || categoriasIds.length == 0)
                return gson.toJson(new Mensagem("231", "Fields missing.", null));

            for(String id: categoriasIds){
                if(id == null)
                    return gson.toJson(new Mensagem("400", "Invalid JSON.", null));

                Categoria temp = this.procurarCategoria(bancoDados, id);
                if(temp == null)
                    return gson.toJson(new Mensagem("233", "Invalid information inserted.", null));

                if(bancoDados.categoriaEmUso(Integer.parseInt(id)))
                    return gson.toJson(new Mensagem("234", "Category in use.", null));

                bancoDados.deletarCategoria(Integer.parseInt(id));
            }

            return gson.toJson(new Mensagem("230", "Successful category deletion.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("235", "Unknown error.", null));
        }
        catch (Exception e) {
            return gson.toJson(new Mensagem("235", "Unknown error.", null));
        }
    }

    public String cadastrarAnuncio(BD bancoDados, String token, String titulo, String texto, String categoriaId){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin())
                return gson.toJson(new Mensagem("302", "Invalid token.", null));

            bancoDados.inserirAnuncio(titulo, texto, DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now()), Integer.parseInt(categoriaId));

            return gson.toJson(new Mensagem("300", "Successful announcement creation.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("303", "Unknown error.", null));
        }
    }

    public String consultarAnuncios(BD bancoDados, String token){
        try {
            if (!validarRA(token))
                return gson.toJson(new Mensagem("312", "Invalid token.", null));

            List<Anuncio> anuncios = new ArrayList<Anuncio>();
            if (qualUsuario(token).isAdmin()) {
                List<Anuncio> temp = bancoDados.listarAnunciosAdmin();
                if (temp != null && !temp.isEmpty())
                    anuncios.addAll(temp);
            } else {
                Integer[] res = bancoDados.listarInscricoes(Integer.parseInt(qualUsuario(token).getId()));
                if (res != null) {
                    for(int categoriaId : res) {
                        List<Anuncio> temp = bancoDados.listarAnuncios(categoriaId);
                        if (temp != null && !temp.isEmpty())
                            anuncios.addAll(temp);
                    }
                }
            }

            return gson.toJson(new Mensagem( (!anuncios.isEmpty()) ? anuncios.toArray(Anuncio[]::new) : null, "310", "Successful announcement read."));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("313", "Unknown error.", null));
        }
    }

    public String editarAnuncio(BD bancoDados, String id, String token, String titulo, String texto, String categoriaId){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin())
                return gson.toJson(new Mensagem("322", "Invalid token.", null));

            if(titulo == null && texto == null && categoriaId == null)
                return gson.toJson(new Mensagem("321", "Missing fields.", null));

            Anuncio anuncio = bancoDados.encontrarAnuncio(Integer.parseInt(id));
            if(anuncio == null)
                return gson.toJson(new Mensagem("323", "Invalid information inserted.", null));

            if(titulo == null || titulo.isBlank()) titulo = anuncio.getTitle();
            if(texto == null || texto.isBlank()) texto = anuncio.getText();
            if(categoriaId == null || categoriaId.isBlank()) categoriaId = anuncio.getCategoriaId();

            bancoDados.editarAnuncio(Integer.parseInt(id), titulo, texto, Integer.parseInt(categoriaId));

            return gson.toJson(new Mensagem("320", "Successful announcement update.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("324", "Unknown error.", null));
        }
    }

    public String excluirAnuncio(BD bancoDados, String id, String token){
        try {
            if(!validarRA(token) || !this.qualUsuario(token).isAdmin())
                return gson.toJson(new Mensagem("332", "Invalid token.", null));

            Anuncio anuncio = bancoDados.encontrarAnuncio(Integer.parseInt(id));
            if(anuncio == null)
                return gson.toJson(new Mensagem("333", "Invalid information inserted.", null));

            bancoDados.deletarAnuncio(Integer.parseInt(id));

            return gson.toJson(new Mensagem("330", "Successful announcement deletion.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("334", "Unknown error.", null));
        }
    }

    public String inscrever(BD bancoDados, String token, String categoriaId){
        try {
            if(!validarRA(token))
                return gson.toJson(new Mensagem("342", "Invalid token.", null));

            if(bancoDados.encontrarCategoria(Integer.parseInt(categoriaId)) == null)
                return gson.toJson(new Mensagem("343", "Invalid information inserted.", null));

            if(bancoDados.usuarioEstaInscrito(Integer.parseInt(qualUsuario(token).getId()), Integer.parseInt(categoriaId)))
                return gson.toJson(new Mensagem("344", "Unknown error.", null));

            bancoDados.inserirInscricao(Integer.parseInt(qualUsuario(token).getId()), Integer.parseInt(categoriaId));

            return gson.toJson(new Mensagem("340", "Successful subscription.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("344", "Unknown error.", null));
        }
    }

    public String cancelarInscricao(BD bancoDados, String token, String categoriaId){
        try {
            if(!validarRA(token))
                return gson.toJson(new Mensagem("352", "Invalid token.", null));

            if(bancoDados.encontrarCategoria(Integer.parseInt(categoriaId)) == null)
                return gson.toJson(new Mensagem("353", "Invalid information inserted.", null));

            if(!bancoDados.usuarioEstaInscrito(Integer.parseInt(qualUsuario(token).getId()), Integer.parseInt(categoriaId)))
                return gson.toJson(new Mensagem("354", "Unknown error.", null));

            bancoDados.deletarInscricao(Integer.parseInt(qualUsuario(token).getId()), Integer.parseInt(categoriaId));

            return gson.toJson(new Mensagem("350", "Successfully unsubscribed.", null));
        }
        catch (SQLException e) {
            System.out.println("ERRO de BD: " + e.getMessage());
            return gson.toJson(new Mensagem("354", "Unknown error.", null));
        }
    }

    public String processarRequisicao(String requisicaoJson, BD bancoDados){
        Mensagem req = gson.fromJson(requisicaoJson, Mensagem.class);

        if(req.getOp() == null)
            return gson.toJson(new Mensagem("401", "Op missing.", null));

        switch(req.getOp()){
            case "1":
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null
                        || req.getPassword().isBlank() || req.getName() == null || req.getName().isBlank())
                    return gson.toJson(new Mensagem("101", "Fields missing.", null));
                if(!regexRA(req.getUser()) || req.getPassword().length() != 4)
                    return gson.toJson(new Mensagem("102", "Invalid information inserted: user or password.", null));
                return cadastrarUsuario(bancoDados, req.getName(), req.getUser(), req.getPassword());

            case "2":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("112", "Invalid or empty token.", null));
                return consultarUsuario(bancoDados, req.getUser(), req.getToken());

            case "3":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("121", "Invalid or empty token.", null));
                return editarUsuario(bancoDados, req.getUser(), req.getToken(), req.getName(), req.getPassword());

            case "4":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("132", "Invalid token.", null));
                return excluirUsuario(bancoDados, req.getUser(), req.getToken());

            case "5":
                if(req.getUser() == null || req.getUser().isBlank() || req.getPassword() == null || req.getPassword().isBlank())
                    return gson.toJson(new Mensagem("002", "Fields missing.", null));
                return logarUsuario(bancoDados, req.getUser(), req.getPassword());

            case "6":
                if(req.getToken() == null || req.getToken().isBlank() || !validarRA(req.getToken()))
                    return gson.toJson(new Mensagem("011", "Fields missing.", null));
                return deslogarUsuario(req.getToken());

            case "7":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("201", "Fields missing.", null));
                return cadastrarCategorias(bancoDados, req.getToken(), req.getCategories());

            case "8":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("211", "Fields missing.", null));
                return consultarCategorias(bancoDados, req.getToken());

            case "9":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("221", "Fields missing.", null));
                return editarCategorias(bancoDados, req.getToken(), req.getCategories());

            case "10":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("231", "Fields missing.", null));
                return excluirCategorias(bancoDados, req.getToken(), req.getCategoryIds());

            case "11":
                if(req.getToken() == null || req.getToken().isBlank() || req.getTitle() == null
                        || req.getTitle().isBlank() || req.getText() == null || req.getText().isBlank()
                        || req.getCategoryId() == null || req.getCategoryId().isBlank())
                    return gson.toJson(new Mensagem("301", "Fields missing.", null));
                return cadastrarAnuncio(bancoDados, req.getToken(), req.getTitle(), req.getText(), req.getCategoryId());

            case "12":
                if(req.getToken() == null || req.getToken().isBlank())
                    return gson.toJson(new Mensagem("311", "Fields missing.", null));
                return consultarAnuncios(bancoDados, req.getToken());

            case "13":
                if(req.getToken() == null || req.getToken().isBlank() || req.getId() == null || req.getId().isBlank())
                    return gson.toJson(new Mensagem("321", "Fields missing.", null));
                return editarAnuncio(bancoDados, req.getId(), req.getToken(), req.getTitle(), req.getText(), req.getCategoryId());

            case "14":
                if(req.getToken() == null || req.getToken().isBlank() || req.getId() == null || req.getId().isBlank())
                    return gson.toJson(new Mensagem("331", "Fields missing.", null));
                return excluirAnuncio(bancoDados, req.getId(), req.getToken());

            case "15":
                if(req.getToken() == null || req.getToken().isBlank() || req.getCategoryId() == null || req.getCategoryId().isBlank())
                    return gson.toJson(new Mensagem("341", "Fields missing.", null));
                return inscrever(bancoDados, req.getToken(), req.getCategoryId());

            case "16":
                if(req.getToken() == null || req.getToken().isBlank() || req.getCategoryId() == null || req.getCategoryId().isBlank())
                    return gson.toJson(new Mensagem("351", "Fields missing.", null));
                return cancelarInscricao(bancoDados, req.getToken(), req.getCategoryId());

            default:
                return gson.toJson(new Mensagem("402", "Invalid op.", null));

        }
    }

    private boolean regexRA(String RA){
        if(RA.length() != 7) return false;
        String regex = "^[0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(RA).matches();
    }

    private boolean validarRA(String RA){
        return regexRA(RA) && this.qualUsuario(RA) != null;
    }

    private Usuario qualUsuario(String token){
        return this.usuariosLogados.stream()
                .filter(u -> u.getToken().equals(token))
                .findFirst()
                .orElse(null);
    }
}