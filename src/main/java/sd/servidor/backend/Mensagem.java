package sd.servidor.backend;

// @Author Leonardo Bellato

import java.util.List;

public class Mensagem {
    private String op;
    private String user;
    private String password;
    private String name;
    private String token;
    private String response;
    private String message;
    private Categoria[] categories;
    private String[] categoryIds;
    private String title;
    private String text;
    private String categoryId;
    private Anuncio[] announcements;
    private String id;

    public Mensagem(String response, String message, Categoria[] categories){
        this.response = response;
        this.message = message;
        this.categories = categories;
    }

    public Mensagem(String response, String message, String user, String password, String name){
        this.response = response;
        this.message = message;
        this.user = user;
        this.password = password;
        this.name = name;
    }

    public Mensagem(Anuncio[] announcements, String response, String message){
        this.announcements = announcements;
        this.response = response;
        this.message = message;
    }

    public String getOp() {
        return this.op;
    }
    public void setOp(String op) { this.op = op; }

    public String getUser() {
        return this.user;
    }
    public void setUser(String user) { this.user = user; }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) { this.password = password; }

    public String getName() {
        return this.name;
    }
    public void setName(String name) { this.name = name; }

    public String getToken() {
        return this.token;
    }
    public void setToken(String token) { this.token = token; }

    public String getResponse() {
        return this.response;
    }
    public void setResponse(String response) { this.response = response; }

    public String getMessage() {
        return this.message;
    }
    public void setMessage(String message) { this.message = message; }

    public Categoria[] getCategories() { return this.categories; }
    public void setCategories(Categoria[] categories) { this.categories = categories; }

    public String[] getCategoryIds() { return this.categoryIds; }
    public void setCategoryIds(String[] categoryIds) { this.categoryIds = categoryIds; }

    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return this.text; }
    public void setText(String text) { this.text = text; }

    public String getCategoryId() { return this.categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public Anuncio[] getAnnouncements() { return this.announcements; }
    public void setAnnouncements(Anuncio[] announcements) { this.announcements = announcements; }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }
}
