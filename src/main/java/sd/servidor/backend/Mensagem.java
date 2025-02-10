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

    public String getOp() {
        return op;
    }
    public void setOp(String op) { this.setOp(op); }

    public String getUser() {
        return user;
    }
    public void setUser(String user) { this.setUser(user); }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) { this.setPassword(password); }

    public String getName() {
        return name;
    }
    public void setName(String name) { this.setName(name); }

    public String getToken() {
        return token;
    }
    public void setToken(String token) { this.setToken(token); }

    public String getResponse() {
        return response;
    }
    public void setResponse(String response) { this.setResponse(response); }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) { this.setMessage(message); }

    public Categoria[] getCategories() { return categories; }
    public void setCategories(List<Categoria> categories) { this.setCategories(categories); }

    public String[] getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.setCategoryIds(categoryIds); }
}
