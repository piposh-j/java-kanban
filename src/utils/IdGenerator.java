package utils;

public class IdGenerator {

    private static int idSequence = 0;
    private IdGenerator(){

    }

    public static int getIdSequence(){
        return idSequence++;
    }

}
