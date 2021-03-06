import java.util.*;
import java.io.*;

public class test {
    
public static int move_sliding_window_A(boolean [] x, int LimitSeqNo, int WindowSize){
         
    
    LimitSeqNo = LimitSeqNo - 1;
    
               // wrap around case:
                if(x[LimitSeqNo] == true){
                    
                    if(x[0] == false){
                        
                        x[0] = true;
                        
                        for(int i = 0; i < x.length-1; i++){
                           
                            if(x[i] == false && x[i+1] == true){
                                
                                System.out.println(i);

                                x[i+1] = false;
                                return 0;
                            }
                            
                        }
                        //p_sender_window[LimitSeqNo - WindowSize] = null; // remove the packet
                        //a_times[LimitSeqNo - WindowSize] = 0.0; //remove its timer
                        
                        //return((LimitSeqNo+1)-WindowSize);
                        
                    }
                    // wrap around case:
                if(x[LimitSeqNo] == true && (LimitSeqNo+1) == 2){
                     
                        x[0] = true;
                        x[1] = false;
                        return(0);
                       
                    }
                    // case if window size has already started to wrap around
                    else{
                        
                        for(int k = 0; k < x.length; k++){
                        
                            if(x[k] == false && x[k+1] == true){
                                
                                x[k+1] = false;
                                
                                System.out.println((0 + LimitSeqNo - k)-1);
                                
                                if(((0 + LimitSeqNo - k)-1) == 0)
                                    x[(0 + (WindowSize-1))] = true;
                                    
                                else
                                    x[(0 + LimitSeqNo - k)-1] = true;
                                
                                return((0 + LimitSeqNo - k)-1);
                                
                            }
                       
                        }
                        
                    }
                    
                }
                // no wrap around == move the window along
                
                else if(x[LimitSeqNo] == false){
                    
                        for(int k = 0; k < x.length; k++){
                        
                            if(x[k] == true && x[k+1] == false){
                            
                                x[k+1] = true;
                            
                                x[k+1 - (WindowSize)] = false;
                            
                                return(k+1 - WindowSize);
                        
                            }
                        }
                    }
                   
                return 0;
                }
           

   public final static void main(String[] argv){
       
       boolean [] x = new boolean [8];
       
       for(int i = 0; i< 4; i++)
           x[i] = true;
       
       System.out.println(Arrays.toString(x));
       
       int z = move_sliding_window_A(x, x.length, 4);
       
       System.out.println(Arrays.toString(x));
       
       int c = move_sliding_window_A(x, x.length, 4);
       
       System.out.println(Arrays.toString(x));
               
       int d = move_sliding_window_A(x, x.length, 4);
         
       System.out.println(Arrays.toString(x));
       
       int e = move_sliding_window_A(x, x.length, 4);
         
       System.out.println(Arrays.toString(x));
       
       int f = move_sliding_window_A(x, x.length,4);
         
       System.out.println(Arrays.toString(x));
       
       
   }
}