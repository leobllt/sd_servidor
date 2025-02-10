package sd.servidor.backend;

// @Author Leonardo Bellato

public class Categoria {
    String id;
    String name;
    String description;

    public Categoria() {}

    public Categoria(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId(){ return id; }
    public void setId(String id) { this.id = id; }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription(){ return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString(){
        return id + " " + name + " " + description;
    }
}
