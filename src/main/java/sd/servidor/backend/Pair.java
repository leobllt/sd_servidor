package sd.servidor.backend;

import java.util.Objects;

public class Pair<T,U>{
    private T RA;
    private U eAdmin;

    public Pair(T RA, U eAdmin){
        this.RA = RA;
        this.eAdmin = eAdmin;
    }

    public T getRA(){
        return RA;
    }

    public U getEAdmin(){
        return eAdmin;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return RA.equals(pair.RA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(RA);
    }
}