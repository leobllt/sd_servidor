package sd.servidor.backend;

// @Author Leonardo Bellato

public class Usuario {
    private final String RA;
    private String nome;
    private final String token;
    private final boolean admin;
    private String senha;

    public Usuario(String RA, String nome, String senha, String token, boolean admin) {
        this.RA = RA;
        this.nome = nome;
        this.senha = senha;
        this.token = token;
        this.admin = admin;
    }

    public String getRA() {
        return this.RA;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getToken() {
        return this.token;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    @Override
    public String toString() {
        return "Usuario(" + this.RA + ", " + this.nome + ", " + token + ", " + senha + ", " + admin + ")";
    }
}
