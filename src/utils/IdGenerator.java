package utils;

public class IdGenerator {

    private int idSequence;
    public IdGenerator(){

    }

    public int getIdSequence(){
        return ++idSequence;
    }

}
