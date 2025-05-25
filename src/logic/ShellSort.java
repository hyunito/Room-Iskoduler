package logic;

public class ShellSort {

    public static void shellSort(int pcs[]) {
        int temp = 0;
        for(int interval = pcs.length/2; interval > 0; interval /= 2){
            for(int i = interval; i < pcs.length; i++){
                int j = 0;
                for(j = i; j >= interval && pcs[j - interval] < pcs[j]; j -= interval){
                    temp = pcs[j];
                    pcs[j] = pcs[j - interval];
                    pcs[j - interval] = temp;
                }
            }
        }
    }
}