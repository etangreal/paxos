package communication;

import java.util.Comparator;

public class CompareByB implements Comparator<Instance>
{
	@Override
    public int compare(Instance a, Instance b) 
	{
        if (a.B > b.B){
            return +1;
        }else if (a.B < b.B){
            return -1;
        }else{
            return 0;
        }
    }//compare

}//class
