package sd.servidor.backend;

// @Author Leonardo Bellato

import java.util.Objects;

public class Categoria {
    private String id;
    private String name;
    private String description;
    private String subscribed;

    public Categoria() {}

    public Categoria(String id, String name, String description, String subscribed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subscribed = subscribed;
    }

    public String getId(){ return id; }
    public void setId(String id) { this.id = id; }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription(){ return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSubscribed(){ return subscribed; }
    public void setSubscribed(String subscribed) { this.subscribed = subscribed; }

    @Override
    public String toString(){
        return id + " - " + name + ": " + description + " [" + (subscribed.equals("true") ? "Inscrito" : "Não inscrito") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Categoria categoria = (Categoria) obj;
        return this.id.equals(categoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
