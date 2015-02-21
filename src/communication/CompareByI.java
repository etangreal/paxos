package communication;

import java.util.Comparator;

public class CompareByI implements Comparator<Instance>
{
	@Override
    public int compare(Instance a, Instance b) 
	{
        if (a.I > b.I){
            return +1;
        }else if (a.I < b.I){
            return -1;
        }else{
            return 0;
        }
    }//compare

}//class
