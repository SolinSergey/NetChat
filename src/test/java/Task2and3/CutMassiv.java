package Task2and3;

public class CutMassiv {

    public static int[] getRightSideMassiv(int[] mass) throws RuntimeException{
        int position=300000;
        boolean positionStatus=false;
        for (int i= mass.length-1;i>=0;i--){
            if (mass[i]==4){
                position=i;
                positionStatus=true;
                break;
            }
        }
        if (positionStatus){
            int[] resultMassiv = new int[mass.length-position-1];
            for (int i=position+1;i<mass.length;i++){
                resultMassiv[i-position-1]=mass[i];
            }
            return resultMassiv;
        }else{
            throw new RuntimeException("Четверки отсутствуют");
        }



    }
}
