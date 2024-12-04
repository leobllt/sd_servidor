package sd.servidor.backend;

// @Author Leonardo Bellato

public class Mensagem {
    private Integer op;
    private String user;
    private String password;
    private String name;
    private String token;
    private String response;
    private String message;

    public Mensagem(int op, String user, String password, String name, String token, String response, String message) {
        this.op = op;
        this.user = user;
        this.password = password;
        this.name = name;
        this.token = token;
        this.response = response;
        this.message = message;
    }

    public Mensagem(String response, String message, String token) {
        this.response = response;
        this.message = message;
        this.token = token;
    }

    @Override
    public String toString(){
        return "Requisicao(" + this.op + ", " + this.user + ", " + this.password + ", " + this.name +
                             ", " + this.token + ", " + this.response + ", " + this.message + ")";
    }

    public int getOp() {
        return op;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }

    public String getResponse() {
        return response;
    }

    public String getMessage() {
        return message;
    }
}
