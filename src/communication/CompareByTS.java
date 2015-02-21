package communication;

import java.util.Comparator;

public class CompareByTS implements Comparator<Instance>
{
	@Override
    public int compare(Instance a, Instance b) 
	{
        if (a.TS > b.TS){
            return +1;
        }else if (a.TS < b.TS){
            return -1;
        }else{
            return 0;
        }
    }//compare

}//class
