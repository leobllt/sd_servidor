package sd.servidor.backend;

// @Author Leonardo Bellato

import java.util.Objects;

public class Anuncio {
    private String id;
    private String title;
    private String text;
    private String date;
    private String categoriaId;

    public Anuncio() {}

    public Anuncio(String id, String title, String text, String date, String categoriaId) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.date = date;
        this.categoriaId = categoriaId;
    }

    public String getId(){ return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle(){ return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText(){ return text; }
    public void setText(String text) { this.text = text; }

    public String getDate(){ return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategoriaId(){return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    @Override
    public String toString(){
        return id + " " + title + " " + text + " " + date + " " + categoriaId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Anuncio anuncio = (Anuncio) obj;
        return this.id.equals(anuncio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
