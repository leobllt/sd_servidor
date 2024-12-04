package sd.servidor.backend;

// @Author Leonardo Bellato

import java.sql.*;

public class BD {
    private Connection conexao;
    private PreparedStatement queryInsert;
    private PreparedStatement querySelect;
    //private PreparedStatement queryUpdate;
    //private PreparedStatement queryDelete;

    private BD() {}

    public static BD conectar() throws SQLException {
        BD bd = new BD();
        bd.conexao = null;
        String path = "jdbc:sqlite:src/main/java/sd/servidor/backend/usuarios.db";

        bd.conexao = DriverManager.getConnection(path);
        bd.criarTabela();
        //bd.limparTabela();
        bd.queryInsert = bd.conexao.prepareStatement("INSERT INTO usuarios VALUES(?, ?, ?, ?, ?)");
        bd.querySelect = bd.conexao.prepareStatement("SELECT * FROM usuarios WHERE ra = ?");
        //bd.queryUpdate = conexao.prepareStatement("UPDATE usuarios SET id = ? WHERE id = ?");
        //bd.queryDelete = conexao.prepareStatement("DELETE FROM usuarios WHERE id = ?");

        if(bd.conexao != null) return bd;
        else return null;
    }

    public void desconectar() throws SQLException {
        this.conexao.close();
    }

    public void criarTabela() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                    + "	id INTEGER PRIMARY KEY,"
                    + "	nome TEXT NOT NULL,"
                    + "	ra TEXT NOT NULL,"
                    + "	senha TEXT NOT NULL,"
                    + " admin INTEGER NOT NULL"
                    + ")";

        Statement queryCreate = this.conexao.createStatement();
        queryCreate.execute(sql);
    }

    public void limparTabela() throws SQLException {
        String sql = "DELETE FROM usuarios";

        Statement queryDelete = this.conexao.createStatement();
        queryDelete.execute(sql);
    }

    public void inserirUsuario(String nome, String RA, String senha) throws SQLException {
        if(encontrarUsuario(RA) != null) return;

        this.queryInsert.setNull(1, Types.INTEGER);
        this.queryInsert.setString(2, nome);
        this.queryInsert.setString(3, RA);
        this.queryInsert.setString(4, senha);
        this.queryInsert.setInt(5, 0);
        this.queryInsert.executeUpdate();
    }

    public Usuario encontrarUsuario(String RA) throws SQLException {
        this.querySelect.setString(1, RA);
        ResultSet resultado = this.querySelect.executeQuery();

        if(!resultado.isBeforeFirst()) return null;

        resultado.next();
        return new Usuario(
                resultado.getString("ra"),
                resultado.getString("senha"),
                resultado.getString("nome"),
                null,
                (resultado.getInt("admin") == 1)
        );
    }
}
