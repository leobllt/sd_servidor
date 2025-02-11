package sd.servidor.backend;

// @Author Leonardo Bellato

import java.sql.*;
import java.util.ArrayList;
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

    private BD() {}

    public static BD conectar() throws SQLException {
        BD bd = new BD();
        bd.conexao = null;
        String path = "jdbc:sqlite:src/main/java/sd/servidor/backend/dados.db";

        bd.conexao = DriverManager.getConnection(path);
        bd.criarTabela();
        bd.queryInsertUser = bd.conexao.prepareStatement("INSERT INTO usuarios (nome, ra, senha, admin) VALUES(?, ?, ?, ?)");
        bd.querySelectUser = bd.conexao.prepareStatement("SELECT ra, senha, nome, admin FROM usuarios WHERE ra = ?");
        bd.queryUpdateUser = bd.conexao.prepareStatement("UPDATE usuarios SET nome = ?, senha = ? WHERE ra = ?");
        bd.queryAdminUser = bd.conexao.prepareStatement("UPDATE usuarios SET admin = 1 WHERE ra = ?");
        bd.queryDeleteUser = bd.conexao.prepareStatement("DELETE FROM usuarios WHERE ra = ?");

        bd.queryInsertCat = bd.conexao.prepareStatement("INSERT INTO categorias (nome, descricao) VALUES(?, ?)");
        bd.querySelectCat = bd.conexao.prepareStatement("SELECT id, nome, descricao FROM categorias WHERE id = ?");
        bd.queryUpdateCat = bd.conexao.prepareStatement("UPDATE categorias SET nome = ?, descricao = ? WHERE id = ?");
        bd.queryDeleteCat = bd.conexao.prepareStatement("DELETE FROM categorias WHERE id = ?");

        //admin
        bd.inserirUsuario("Administrador", "1234567", "1234");
        bd.subirAAdmin("1234567");

        if(bd.conexao != null) return bd;
        else return null;
    }

    public void desconectar() throws SQLException {
        this.conexao.close();
    }

    public void criarTabela() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "	nome TEXT NOT NULL,"
                + "	ra TEXT NOT NULL,"
                + "	senha TEXT NOT NULL,"
                + " admin INTEGER NOT NULL"
                + ")";

        String sql2 = "CREATE TABLE IF NOT EXISTS categorias ("
                + "	id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "	nome TEXT NOT NULL,"
                + "	descricao TEXT"
                + ")";

        Statement queryCreate = this.conexao.createStatement();
        queryCreate.execute(sql);
        queryCreate.execute(sql2);
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
    }

    public Categoria encontrarCategoria(int id) throws SQLException {
        this.querySelectCat.setInt(1, id);
        ResultSet resultado = this.querySelectCat.executeQuery();

        if (!resultado.next()) return null; // Se não houver resultados

        return new Categoria(
                String.valueOf(resultado.getInt("id")),
                resultado.getString("nome"),
                resultado.getString("descricao")
        );
    }

    public Categoria[] listarCategorias() throws SQLException {
        String sql = "SELECT * FROM categorias";
        Statement querySelectAllCat = this.conexao.createStatement();

        ResultSet resultado = querySelectAllCat.executeQuery(sql);

        if (!resultado.isBeforeFirst()) return null; // Se não houver resultados

        List<Categoria> categorias = new ArrayList<>();

        while (resultado.next()) {
            categorias.add(new Categoria(
                    String.valueOf(resultado.getInt("id")),
                    resultado.getString("nome"),
                    resultado.getString("descricao")
            ));
        }

        return categorias.toArray(Categoria[]::new);
    }

}
