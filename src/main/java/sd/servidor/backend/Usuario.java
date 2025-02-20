package sd.servidor.backend;

// @Author Leonardo Bellato

import java.util.Objects;

public class Usuario {
    private String id;
    private String RA;
    private String nome;
    private String token;
    private boolean admin;
    private String senha;

    public Usuario(String id, String RA, String nome, String senha, String token, boolean admin) {
        this.id = id;
        this.RA = RA;
        this.nome = nome;
        this.senha = senha;
        this.token = token;
        this.admin = admin;
    }

    public String getId() {
        return id;
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

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    @Override
    public String toString() {
        return "Usuario (" + this.RA + ", " + (admin ? "Admin" : "Comum") + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return this.RA.equals(usuario.RA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(RA);
    }
}
