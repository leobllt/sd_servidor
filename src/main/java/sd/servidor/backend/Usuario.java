package sd.servidor.backend;

// @Author Leonardo Bellato

public class Usuario {
    private final String RA;
    private final String nome;
    private final String token;
    private final String password;
    private final boolean admin;

    public Usuario(String RA, String password, String nome, String token, boolean admin) {
        this.RA = RA;
        this.password = password;
        this.nome = nome;
        this.token = token;
        this.admin = admin;
    }

    public String getRA() {
        return this.RA;
    }

    public String getNome() {
        return this.nome;
    }

    public String getToken() {
        return this.token;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public boolean comparePassword(String password) {
        return this.password.equals(password);
    }
}
