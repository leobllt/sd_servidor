package sd.servidor.backend;

// @Author Leonardo Bellato

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BD {
    private Connection conexao;
    // Queries usuário
    private PreparedStatement queryInsertUser;
    private PreparedStatement querySelectUser ;
    private PreparedStatement queryUpdateUser;
    private PreparedStatement queryAdminUser;
    private PreparedStatement queryDeleteUser;
    // Queries categoria
    private PreparedStatement queryInsertCat;
    private PreparedStatement querySelectCat;
    private PreparedStatement queryUpdateCat;
    private PreparedStatement queryDeleteCat;
    // Queries anuncio
    private PreparedStatement queryInsertAnunc;
    private PreparedStatement querySelectAnunc;
    private PreparedStatement queryUpdateAnunc;
    private PreparedStatement queryDeleteAnunc;
    // Queries anuncio
    private PreparedStatement queryInsertInsc;
    private PreparedStatement querySelectInsc;
    private PreparedStatement queryDeleteInsc;

    private BD() {}

    public static BD conectar() throws SQLException {
        BD bd = new BD();
        bd.conexao = null;
        String path = "jdbc:h2:./src/main/java/sd/servidor/backend/bancoDeDados.db";

        bd.conexao = DriverManager.getConnection(path, "sa", "");

        bd.criarTabela();
        bd.queryInsertUser = bd.conexao.prepareStatement("INSERT INTO usuarios (nome, ra, senha, admin) VALUES(?, ?, ?, ?)");
        bd.querySelectUser = bd.conexao.prepareStatement("SELECT id, nome, ra, senha, admin FROM usuarios WHERE ra = ?");
        bd.queryUpdateUser = bd.conexao.prepareStatement("UPDATE usuarios SET nome = ?, senha = ? WHERE ra = ?");
        bd.queryAdminUser = bd.conexao.prepareStatement("UPDATE usuarios SET admin = 1 WHERE ra = ?");
        bd.queryDeleteUser = bd.conexao.prepareStatement("DELETE FROM usuarios WHERE ra = ?");

        bd.queryInsertCat = bd.conexao.prepareStatement("INSERT INTO categorias (nome, descricao) VALUES(?, ?)");
        bd.querySelectCat = bd.conexao.prepareStatement("SELECT id, nome, descricao FROM categorias WHERE id = ?");
        bd.queryUpdateCat = bd.conexao.prepareStatement("UPDATE categorias SET nome = ?, descricao = ? WHERE id = ?");
        bd.queryDeleteCat = bd.conexao.prepareStatement("DELETE FROM categorias WHERE id = ?");

        bd.queryInsertAnunc = bd.conexao.prepareStatement("INSERT INTO anuncios (titulo, texto, data, categoriaId) VALUES(?, ?, ?, ?)");
        bd.querySelectAnunc = bd.conexao.prepareStatement("SELECT id, titulo, texto, data, categoriaId FROM anuncios WHERE id = ?");
        bd.queryUpdateAnunc = bd.conexao.prepareStatement("UPDATE anuncios SET titulo = ?, texto = ?, categoriaId = ? WHERE id = ?");
        bd.queryDeleteAnunc = bd.conexao.prepareStatement("DELETE FROM anuncios WHERE id = ?");

        bd.queryInsertInsc = bd.conexao.prepareStatement("INSERT INTO inscricoes (usuarioId, categoriaId) VALUES(?, ?)");
        bd.querySelectInsc = bd.conexao.prepareStatement("SELECT categoriaId FROM inscricoes WHERE usuarioId = ?");
        bd.queryDeleteInsc = bd.conexao.prepareStatement("DELETE FROM inscricoes WHERE usuarioId = ? and categoriaId = ?");

        //admin
        //bd.limparTabela();
        //bd.inserirUsuario("Administrador", "1234567", "1234");
        //bd.subirAAdmin("1234567");

        if(bd.conexao != null) return bd;
        else return null;
    }

    public void desconectar() throws SQLException {
        this.conexao.close();
    }

    public void limparTabela() throws SQLException {
        String sql = "DELETE FROM usuarios";

        Statement queryDelete = this.conexao.createStatement();
        queryDelete.execute(sql);
    }

    public void criarTabela() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, " // Use INT com AUTO_INCREMENT no H2
                + "nome VARCHAR(41) NOT NULL, "
                + "ra VARCHAR(8) NOT NULL, "
                + "senha VARCHAR(5) NOT NULL, "
                + "admin INT NOT NULL)"; // Boolean em vez de INTEGER para valores como 0 ou 1

        String sql2 = "CREATE TABLE IF NOT EXISTS categorias ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "nome VARCHAR(255) NOT NULL, "
                + "descricao VARCHAR(255))";

        String sql3 = "CREATE TABLE IF NOT EXISTS anuncios ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "titulo VARCHAR(255) NOT NULL, "
                + "texto TEXT NOT NULL, "
                + "data VARCHAR(15) NOT NULL, "
                + "categoriaId INT NOT NULL)";

        String sql4 = "CREATE TABLE IF NOT EXISTS inscricoes ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "usuarioId INT NOT NULL, "
                + "categoriaId INT NOT NULL)";

        Statement queryCreate = this.conexao.createStatement();
        queryCreate.execute(sql);
        queryCreate.execute(sql2);
        queryCreate.execute(sql3);
        queryCreate.execute(sql4);
    }


    public void inserirUsuario(String nome, String RA, String senha) throws SQLException {
        this.queryInsertUser.setString(1, nome);
        this.queryInsertUser.setString(2, RA);
        this.queryInsertUser.setString(3, senha);
        this.queryInsertUser.setInt(4, 0);
        this.queryInsertUser.executeUpdate();
    }

    public void editarUsuario(String RA, String nome, String senha) throws SQLException{
        this.queryUpdateUser.setString(1, nome);
        this.queryUpdateUser.setString(2, senha);
        this.queryUpdateUser.setString(3, RA);
        this.queryUpdateUser.executeUpdate();
    }

    public void deletarUsuario(String RA) throws SQLException {
        this.queryDeleteUser.setString(1, RA);
        this.queryDeleteUser.executeUpdate();
    }

    public void subirAAdmin(String RA) throws SQLException {
        this.queryAdminUser.setString(1, RA);
        this.queryAdminUser.executeUpdate();
    }

    public Usuario encontrarUsuario(String RA) throws SQLException {
        this.querySelectUser.setString(1, RA);
        ResultSet resultado = this.querySelectUser.executeQuery();

        if (!resultado.next()) return null; // Se não houver resultados

        return new Usuario(
                String.valueOf(resultado.getInt("id")),
                resultado.getString("ra"),
                resultado.getString("nome"),
                resultado.getString("senha"),
                null,
                (resultado.getInt("admin") == 1)
        );
    }

    public Usuario[] listarUsuarios() throws SQLException {
        String sql = "SELECT * FROM usuarios";
        Statement querySelectAllUser = this.conexao.createStatement();

        ResultSet resultado = querySelectAllUser.executeQuery(sql);

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Usuario> usuarios = new ArrayList<>();

        while (resultado.next()) {
            usuarios.add(new Usuario(
                    String.valueOf(resultado.getInt("id")),
                    resultado.getString("ra"),
                    resultado.getString("nome"),
                    resultado.getString("senha"),
                    null,
                    resultado.getInt("admin") == 1
            ));
        }

        return usuarios.toArray(Usuario[]::new);
    }

    public void inserirCategoria(String nome, String descricao) throws SQLException {
        this.queryInsertCat.setString(1, nome);
        this.queryInsertCat.setString(2, descricao);
        this.queryInsertCat.executeUpdate();
    }

    public void editarCategoria(int id, String nome, String descricao) throws SQLException, NumberFormatException{
        this.queryUpdateCat.setString(1, nome);
        this.queryUpdateCat.setString(2, descricao);
        this.queryUpdateCat.setInt(3, id);
        this.queryUpdateCat.executeUpdate();
    }

    public void deletarCategoria(int id) throws SQLException {
        this.queryDeleteCat.setInt(1, id);
        this.queryDeleteCat.executeUpdate();

        // remover todos os anuncios associados
        deletarInscricoes(id);
    }

    public Categoria encontrarCategoria(int id) throws SQLException {
        this.querySelectCat.setInt(1, id);
        ResultSet resultado = this.querySelectCat.executeQuery();

        if (!resultado.next()) return null; // Se não houver resultados

        return new Categoria(
                String.valueOf(resultado.getInt("id")),
                resultado.getString("nome"),
                resultado.getString("descricao"),
                null
        );
    }

    public Categoria[] listarCategorias(int usuarioId) throws SQLException {
        String sql = "SELECT * FROM categorias";
        Statement querySelectAllCat = this.conexao.createStatement();

        ResultSet resultado = querySelectAllCat.executeQuery(sql);

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Categoria> categorias = new ArrayList<>();

        while (resultado.next()) {
            categorias.add(new Categoria(
                    String.valueOf(resultado.getInt("id")),
                    resultado.getString("nome"),
                    resultado.getString("descricao"),
                    this.usuarioEstaInscrito(usuarioId, resultado.getInt("id")) ? "true" : "false"
            ));
        }

        return categorias.toArray(Categoria[]::new);
    }

    public void inserirAnuncio(String titulo, String texto, String data, int categoriaId) throws SQLException {
        this.queryInsertAnunc.setString(1, titulo);
        this.queryInsertAnunc.setString(2, texto);
        this.queryInsertAnunc.setString(3, data);
        this.queryInsertAnunc.setInt(4, categoriaId);
        this.queryInsertAnunc.executeUpdate();
    }

    public void editarAnuncio(int id, String titulo, String texto, int categoriaId) throws SQLException {
        this.queryUpdateAnunc.setString(1, titulo);
        this.queryUpdateAnunc.setString(2, texto);
        this.queryUpdateAnunc.setInt(3, categoriaId);
        this.queryUpdateAnunc.setInt(4, id);
        this.queryUpdateAnunc.executeUpdate();
    }

    public void deletarAnuncio(int id) throws SQLException {
        this.queryDeleteAnunc.setInt(1, id);
        this.queryDeleteAnunc.executeUpdate();
    }

    public Anuncio encontrarAnuncio(int id) throws SQLException {
        this.querySelectAnunc.setInt(1, id);
        ResultSet resultado = this.querySelectAnunc.executeQuery();

        if (!resultado.next()) return null; // Se não houver resultados

        return new Anuncio(
                String.valueOf(resultado.getInt("id")),
                resultado.getString("titulo"),
                resultado.getString("texto"),
                resultado.getString("data"),
                String.valueOf(resultado.getInt("categoriaId"))
        );
    }

    public List<Anuncio> listarAnuncios(int categoriaId) throws SQLException {
        String sql = "SELECT id, titulo, texto, data, categoriaId FROM anuncios WHERE categoriaId = " + categoriaId;
        Statement querySelectAllAnunc = this.conexao.createStatement();

        ResultSet resultado = querySelectAllAnunc.executeQuery(sql);

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Anuncio> anuncios = new ArrayList<>();

        while (resultado.next()) {
            anuncios.add(new Anuncio(
                    String.valueOf(resultado.getInt("id")),
                    resultado.getString("titulo"),
                    resultado.getString("texto"),
                    resultado.getString("data"),
                    String.valueOf(resultado.getInt("categoriaId"))
            ));
        }

        return anuncios;
    }

    public List<Anuncio> listarAnunciosAdmin() throws SQLException {
        String sql = "SELECT * FROM anuncios";
        Statement querySelectAllAnunc = this.conexao.createStatement();

        ResultSet resultado = querySelectAllAnunc.executeQuery(sql);

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Anuncio> anuncios = new ArrayList<>();

        while (resultado.next()) {
            anuncios.add(new Anuncio(
                    String.valueOf(resultado.getInt("id")),
                    resultado.getString("titulo"),
                    resultado.getString("texto"),
                    resultado.getString("data"),
                    String.valueOf(resultado.getInt("categoriaId"))
            ));
        }

        return anuncios;
    }

    public void inserirInscricao(int usuarioId, int categoriaId) throws SQLException {
        this.queryInsertInsc.setInt(1, usuarioId);
        this.queryInsertInsc.setInt(2, categoriaId);
        this.queryInsertInsc.executeUpdate();
    }

    // deletar a inscrição do usuário da categoria
    public void deletarInscricao(int usuarioId, int categoriaId) throws SQLException {
        this.queryDeleteInsc.setInt(1, usuarioId);
        this.queryDeleteInsc.setInt(2, categoriaId);
        this.queryDeleteInsc.executeUpdate();
    }

    // deletar a todas as incricões da categoria
    public void deletarInscricoes(int categoriaId) throws SQLException {
        String sql = "DELETE FROM inscricoes WHERE categoriaId = " + categoriaId;
        Statement temp = this.conexao.createStatement();
        temp.execute(sql);
    }

    // para saber quais categorias o usuário está inscrito
    public Integer[] listarInscricoes(int usuarioId) throws SQLException {
        this.querySelectInsc.setInt(1, usuarioId);
        ResultSet resultado = this.querySelectInsc.executeQuery();

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Integer> categorias = new ArrayList<>();

        while (resultado.next()) {
            categorias.add(resultado.getInt("categoriaId"));
        }

        return categorias.toArray(Integer[]::new);
    }

    public boolean usuarioEstaInscrito(int usuarioId, int categoriaId) throws SQLException {
        String sql = "SELECT id FROM inscricoes WHERE usuarioId = ? AND categoriaId = ?";

        try (PreparedStatement stmt = this.conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, categoriaId);

            try (ResultSet resultado = stmt.executeQuery()) {
                return resultado.next(); // Retorna true se encontrou um registro
            }
        }
    }

    public boolean categoriaEmUso(int categoriaId) throws SQLException {
        String sql = "SELECT id FROM inscricoes WHERE categoriaId = ?";

        try (PreparedStatement stmt = this.conexao.prepareStatement(sql)) {
            stmt.setInt(1, categoriaId);

            try (ResultSet resultado = stmt.executeQuery()) {
                return resultado.next(); // Retorna true se encontrou um registro
            }
        }
    }

}
