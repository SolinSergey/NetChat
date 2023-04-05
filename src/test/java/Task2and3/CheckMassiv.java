package Task2and3;

public class CheckMassiv {
    public static boolean checkMassiv1or4Elements(int[] mass){
        boolean resultFlag1 = false;
        boolean resultFlag4 = false;
        for (int i=0;i<mass.length;i++){
            if (!resultFlag1 && mass[i]==1){
                resultFlag1 = true;
            }
            if (!resultFlag4 && mass[i]==4){
                resultFlag4=true;
            }
        }
        return (resultFlag1 & resultFlag4);
    }
}
