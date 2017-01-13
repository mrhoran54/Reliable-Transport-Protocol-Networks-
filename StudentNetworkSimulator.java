import java.util.*;
import java.io.*;
import java.util.Timer;

public class StudentNetworkSimulator extends NetworkSimulator
{
    /*
     * Predefined Constants (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and
     *                     Packet payload
     *
     *   int A           : a predefined integer that represents entity A
     *   int B           : a predefined integer that represents entity B 
     *
     * Predefined Member Methods:
     *
     *  void stopTimer(int entity): 
     *       Stops the timer running at "entity" [A or B]
     *  void startTimer(int entity, double increment): 
     *       Starts a timer running at "entity" [A or B], which will expire in
     *       "increment" time units, causing the interrupt handler to be
     *       called.  You should only call this with A.
     *  void toLayer3(int callingEntity, Packet p)
     *       Puts the packet "p" into the network from "callingEntity" [A or B]
     *  void toLayer5(String dataSent)
     *       Passes "dataSent" up to layer 5
     *  double getTime()
     *       Returns the current time in the simulator.  Might be useful for
     *       debugging.
     *  int getTraceLevel()
     *       Returns TraceLevel
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for
     *       debugging, but probably not.
     *
     *
     *  Predefined Classes:
     *
     *  Message: Used to encapsulate a message coming from layer 5
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      boolean setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *          returns true on success, false otherwise
     *      String getData():
     *          returns the data contained in the message
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet that is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload)
     *          creates a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and a
     *          payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an
     *          ack field of "ack", a checksum field of "check", and
     *          an empty payload
     *    Methods:
     *      boolean setSeqnum(int n)
     *          sets the Packet's sequence field to "n"
     *          returns true on success, false otherwise
     *      boolean setAcknum(int n)
     *          sets the Packet's ack field to "n"
     *          returns true on success, false otherwise
     *      boolean setChecksum(int n)
     *          sets the Packet's checksum to "n"
     *          returns true on success, false otherwise
     *      boolean setPayload(String newPayload)
     *          sets the Packet's payload to "newPayload"
     *          returns true on success, false otherwise
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      int getPayload()
     *          returns the Packet's payload
     *
     */

    /*   Please use the following variables in your routines.
     *   int WindowSize  : the window size
     *   double RxmtInterval   : the retransmission timeout
     *   int LimitSeqNo  : when sequence number reaches this value, it wraps around
     */

    // Add any necessary class variables here.  Remember, you cannot use
    // these variables to send messages error free!  They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    
    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    
    // FOR A SIDE
    private LinkedList<Message> messages_waiting;
    
    public int SEND_BASE = -1;
   
    // simulated sliding window
    public boolean [] sliding_window;
    
    // this is the packets waiting to be sent within the window
    private Packet [] p_sender_window;
    
    // keeps track of the different send times of the packets
    private double [] a_times;
    
      // this is for seeing what packets have been acked
    private boolean [] ack_recieved;
    
    public int START_OF_WINDOW = 0; // in other words the send base
   
    
    // FOR B SIDE
    
    public int LAST_RECEIVED = -1;
   
    // this is for seeing what packets have been acked
    private boolean [] pck_received;
   
    // the current window of recieveing packets
    private boolean [] sliding_window_B;
    
    // buffering packets
    private Packet [] buffering_packets; 
    
    /*HELPR METHODS LISTED
     
     protected boolean is_sliding_window_full();
     protected boolean is_sliding_window_empty(){
     protected int check_sum(int sequence_num, int ack, String data);
     protected int move_sliding_window_B();
     protected int move_sliding_window_A();
     protected boolean no_acks();
     
    */
    
    // STATISTICS 
    
    public int corruption_count_sender = 0;
    public int loss_count = 0;
    
    public int retransmitted =0;
    public int original_data_pckts_sent = 0;

    public int acks_recieved = 0;
    
    
    // Calc time
//    
    public int RTT_CORRUPT_AVERAGE = 0;
    public int c = 0;
    
    public int RTT_LOSS_AVERAGE = 0;
    public int l = 0;
    
    public int RTT_AVERAGE =0;
    public int r = 0;
    
    public double [] RTT; // the sliding window of the sender side
    
    public double [] RTT_CORRUPT;
    
    public double [] RTT_LOSS;
    // keeps track of the different send times of the packets
    
    
    // This is the constructor.  Don't touch!
    public StudentNetworkSimulator(int numMessages,
                                   double loss,
                                   double corrupt,
                                   double avgDelay,
                                   int trace,
                                   int seed,
                                   int winsize,
                                   double delay)
    {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
 WindowSize = winsize;
 LimitSeqNo = winsize*2;
 RxmtInterval = delay;
    }

    
    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send.  It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    
    protected void aOutput(Message message)
    {
        // Three main things can happen
        //1. sliding window size is empty and so start sending PCK at the start of the next window (should only happen at time "0" in SR)
        
        //2. p_sender_window is not full and so make a packet in the next avaliable spot and send it 
        
        //3. p_sender_window window is full so do nothing ==> it was already buffered in the message queue
        
        
        System.out.println("aOutput");
        System.out.println("Receiveing message from layer 5 at " + getTime());

                       
        // adding message to the end of the buffer
        
        messages_waiting.add(message);

        // case 1
       
        if(is_sliding_window_empty() == true && no_acks()== true){
            
            // new window 
            System.out.println("Creating a new PCK "+ START_OF_WINDOW +" and sending it to B.");

            Message x = messages_waiting.removeFirst();
               
            int check = check_sum(START_OF_WINDOW, START_OF_WINDOW, x.getData());
                    
            Packet next = new Packet(START_OF_WINDOW, START_OF_WINDOW, check, x.getData()); 
                
            p_sender_window[START_OF_WINDOW] = next; // setting up packet in ther sending window
            
            a_times[START_OF_WINDOW] = getTime()+RxmtInterval; // setting its "timer" in my times array
            
            System.out.println("pckt " +START_OF_WINDOW + " will timeout at time " +a_times[START_OF_WINDOW]);
            
            toLayer3(0, next);
            
            // timer for stats
            RTT[START_OF_WINDOW] = getTime();
            
            original_data_pckts_sent++; // stats
           
            System.out.println("Initalizing the first timer.");
            
            startTimer(0, RxmtInterval);
            
        }
        
        // case3
       
        else if(is_p_sender_window_available() == false){
            
            System.out.println("Buffering Message");
        }
        
        // case 2

        // see if there is stll room in the sending window to add future packets 
        // Adding packets to p_sender_window to ensure that when we want to move the window and send packets, we can do so.
        
        else if(is_p_sender_window_available() == true){ 
            
            // protocol for SR
            
            // this start of window and k updating is just some logic to make sure I wrap around and check the WHOLE sending window
            
            int k = START_OF_WINDOW;
            
            while(messages_waiting.isEmpty() == false && k < p_sender_window.length){
                
                if(p_sender_window[k] == null && sliding_window[k] == true){ // you know you can queue up a packet in the next available
                
                    Message x = messages_waiting.removeFirst(); // get the message from my linked list

                    int check = check_sum(k, k, x.getData());
                           
                    Packet next = new Packet(k, k, check, x.getData()); // make the new packet to queue in spot k in the p_sender_window
                
                    p_sender_window[k] = next;
                    
                    System.out.println("Sending packet "+  k + " to B side."); 
                         
                    toLayer3(0, p_sender_window[k]); // actually send it to B_side
                    
                    original_data_pckts_sent++; // stats
                    
                    a_times[k] = getTime()+ RxmtInterval; // set up its "timer" with my times array

                    RTT[k] = getTime(); // for stats
                    
                    System.out.println("packet " +k + " will timeout at "+ (getTime()+RxmtInterval));
                     
                  // wrap around check to see if the window wraps around the limtiseq no
                    
                    if(k == LimitSeqNo-1 && sliding_window[0] == false)
                        
                        break;
                    else if(k == LimitSeqNo-1 && p_sender_window[0] == null && sliding_window[0] == true)
                        
                        k = 0;
                    
                    else
                        k++;
         
                }
                else if(p_sender_window[k] != null && sliding_window[k] == true){ // otherwise you've found a packet but you still might not be at the end
                                                      // so keep checking!!
                    
                     if(k == LimitSeqNo-1 && p_sender_window[0] == null && sliding_window[0] == true){
                        
                        k = 0;
                        
                    }
                    else if(k == LimitSeqNo-1 && sliding_window[0] == false)
                        
                        break;
                    
                    else{
                        
                        k++;
                        continue;
                    }
                }
               else
                   return;
                   
            }
         
        }
        
        else
            return;
        
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side.  "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    
    protected void aInput(Packet packet)
    {
        
                System.out.println("\n Packets waiting to be sent in the sender window:");
                System.out.println(Arrays.toString(p_sender_window));

        
        System.out.println("I am in aInput at time " + getTime());
        
         // get info from the packet
        System.out.println(Arrays.toString(ack_recieved));
                           
        String data = packet.getPayload();
            
        int seq_num = packet.getSeqnum();
        
        int ack_num = packet.getAcknum();

        int check_sum = packet.getChecksum();
        
        // check to see if the ACk packet has been corrupted
        int check = check_sum(seq_num, ack_num, data);
        
        if(check != check_sum){ // the data we got back has been corrupted so resend the packet again
             
            System.out.println("Ack  " +ack_num +" has been corrupted. Packet is now being resent to side B.");

            if(p_sender_window[seq_num] != null){
             // send it again and don't move window at all because just sending a duplicate
             Packet re_send = p_sender_window[seq_num];
             
             corruption_count_sender++;
             
             // updateing the time this packet has been in the system
             
             RTT_CORRUPT[seq_num] = getTime() - RTT[seq_num];
             
             RTT[seq_num] = 0;
             
              // finally resend the packet to reciever side again 
             toLayer3(0, re_send);

             original_data_pckts_sent++; // stats
             
             // set up timing apparatus for the new retrasmission time for the resent packet
             a_times[seq_num] = getTime() + RxmtInterval;

             System.out.println("Packet "+ seq_num +" timeout set to " + a_times[seq_num]);
             
             return;
            }
        }

        // check for duplicate packet and retransmit data if need be
        
        else{  // there has been no corruption in ack pck from B side.
              
            
            System.err.println("Ack  for packet " + seq_num + " has not been corrupted and has been received on A side.");
            
            ack_recieved[seq_num] = true;  // that packet has been acknowledged
            
            if(RTT[seq_num] == 0){
            
                if(RTT_LOSS[seq_num] == 0){
                    
                    RTT_CORRUPT_AVERAGE += RTT_CORRUPT[seq_num];
                    c++;
                }
                else{
                    RTT_LOSS_AVERAGE += RTT_LOSS[seq_num];
                    l++;
                }
            }
            
            else{
                 RTT_AVERAGE += (getTime() - RTT[seq_num]);
                 r++;
            }
            
            acks_recieved++;
        }
            // S&W : if you recieve the ack you simply know that it is time to move the window
            
            if (WindowSize == 1){
                

                move_sliding_window_A();
               
            }
             // SR : selective repeat is more complicated
            // you want to see if there is a packet you are waiting on in your window, and when to move the window based on that
            // you do this by constantly updating your "send_base" ie the value of the lask acked packet
            
            else{
                
                System.out.println("\n All the arrays before sending window is potentially moved.");
                
                System.out.println("\n Acknowledgements recieved:");
                System.out.println(Arrays.toString(ack_recieved));
                
                System.out.println("\n Sender sliding window:");
                System.out.println(Arrays.toString(sliding_window));
                                
                System.out.println("\n Packets waiting to be sent in the sender window:");
                System.out.println(Arrays.toString(p_sender_window));

                 System.out.println("\n");
                 

                // you wont be able to slide the window unless all packets have been sent AND acked
                
                // so there is the potential to slide the window a bunch of times in one sitting
                      
                  // initial starter case for the first packet ever sent
                
                if(SEND_BASE == -1 && seq_num == 0){
                    
                    System.out.println("We have recieved the packet " + 0.);
                   
                    ack_recieved[seq_num] = true;
                    
                    SEND_BASE = 0;
                   
                 }
                    
                // if you have recieved the send base, see if it is time to move the window and how much by checking
                // how mant other acknowledged packets there are
                
                 else if(seq_num == SEND_BASE){
                    
                    loss_count++;
                    System.out.println("We have recieved the ACK " + seq_num + " that we had been waiting on.");
                    
                    //see how many packets you are actually waiting on withing your sending window
                
                    for(int i = SEND_BASE; i < p_sender_window.length; i++){
                    
                        if(p_sender_window[i] != null && ack_recieved[i] == true){
                        
                            System.out.println("Packet " + i + " has been acknowledged. Window Moved.");
                            
                            SEND_BASE = i;
                            
                            move_sliding_window_A();
                            
                           
                        }
                        
                    }
                    
                    System.out.println("\n Current sending window arrays.");
                
                    System.out.println("\n Acknowledgements recieved:");
                    System.out.println(Arrays.toString(ack_recieved));
                
                    System.out.println("\n Sender sliding window:");
                    System.out.println(Arrays.toString(sliding_window));
                                
                    System.out.println("\n Packets waiting to be sent in the sender window:");
                    System.out.println(Arrays.toString(p_sender_window));
   
                }
                
                // BEST CASE:
                //if the packet you recieved is the next in sequential order, time to send it to above and slide the window over 1
                
                else if((seq_num - SEND_BASE) == 1 || (seq_num - SEND_BASE) == -1){
                        
                    System.out.println("Packet " +  seq_num + " was in order. Time to slide sending window over 1.");

                    SEND_BASE = seq_num;
                    
                    System.out.println(" SEND_BASE is now " + SEND_BASE);
                    
                    move_sliding_window_A();
                    
                }
                
                //check if the packet is greater than the last recieved ack and set that last acked packet as the send base
                // set all the packets you haven't recieved in the interm in p_sender_window but DONT slide the window
                
                else if((seq_num - SEND_BASE) > 1){
                    
                    System.out.println("Packet"+ seq_num +" is larger than other outstanding unacknowledged packets. Buffer packet.");
                    
                    // you want to buffer the packet
                    p_sender_window[seq_num] = packet;
                    ack_recieved[seq_num] = true;
                    loss_count++;
                    
                    if(SEND_BASE == -1){
                        
                        SEND_BASE = 0;
                    }
                   
                    // don't update last received because there is still a packet we are waiting for
                    
                    
                }
             
                else{
                
                    System.out.println("\nIt is time NOT to slide the sending window.");
                    
                }
            }
      }


    // This routine will be called when A's timer expires (thus generating a 
    // timer interrupt). You'll probably want to use this routine to control 
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped. 
    
    protected void aTimerInterrupt()
    {
        // The way I use my timer is with an array, so if packet 1 has a timeout time of x, then 
        // the index in my a_times[1] would be x
        // This function finds the packet that expires, resends a packet and then set the timer to the next RxmtInterval time
        
        System.out.println("Retransmission Timeout at " + getTime());
        System.err.println(Arrays.toString(a_times));
        
            int timed_out_packet = 0;
            
            double smallest_val = 0;
            int smallest_val_index = 0;
            
            // check which index of first real time value in the array
            for(int i = 0; i < a_times.length; i++){
            
                if(a_times[i] == 0)
                    continue;
                
              // if youve reached the end of the array and there are no new timers to set, then return and dont set a new timer 
                
                else if(a_times[i]== 0 && i == a_times.length-1){
                    
                    System.out.println("No timers to set.");
                    return;
                }
                
                // find the first real non zero entry in the array
                else if(a_times[i] != 0){
                   
                    smallest_val = a_times[i];
                   
                }
            }
            
            // the smallest value  and use the index of the smallest values to find the the packet that timed out
            for(int i = 0; i < a_times.length; i++){
            
                if(a_times[i] == 0)
                    continue;
                

                else if(a_times[i] != 0){
                   
                    if(smallest_val > a_times[i]){
                        
                        smallest_val = a_times[i];
                        smallest_val_index = i;
                    }
                   
                }
            }
            
            if(p_sender_window[smallest_val_index] == null){
                
                System.out.println("old timer ");
                return;
            }
            else{
                
                RTT_LOSS_AVERAGE += RTT[smallest_val_index];
                
                RTT[smallest_val_index] = 0.0;
                
                System.out.println("Packet " + smallest_val_index + " timed. Sending it to layer 3 again.");
            
                retransmitted++;

            
                // time to resend that packet and set its timer again
                toLayer3(0, p_sender_window[smallest_val_index]);
                    
                a_times[smallest_val_index] = getTime() + RxmtInterval;
                    
                    
                startTimer(0, RxmtInterval);
                    
                return;
            }
                
    }
    
    // This routine will be called once, before any of your other A-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit()
    {
        
        messages_waiting = new LinkedList<Message>(); //the buffer where I store the messages from layer 5
   
        sliding_window = new boolean[LimitSeqNo]; // the sliding window of the sender side
        
        p_sender_window = new Packet[LimitSeqNo]; // the packets stroed in the sender side sliding window
    
        a_times = new double[LimitSeqNo]; // my timer array
     
        ack_recieved = new boolean[LimitSeqNo]; // acks for packets in the sender windows side
        
        RTT = new double[LimitSeqNo];
        
        RTT_LOSS = new double[LimitSeqNo];
        RTT_CORRUPT = new double[LimitSeqNo];
        
        // initialize the sliding window at first
        
       for(int j = 0 ; j < WindowSize; j++){
            
            sliding_window[j] = true;
       }
        
           // initialize the p_sender_window to all null at first(since there are no packets yet
        for(int j =0 ; j < LimitSeqNo; j++){
            
            p_sender_window[j] = null;
            
        }
           // initialize the a_times to all null at first(since there are no packets yet
        for(int j =0 ; j < LimitSeqNo; j++){
            
            a_times[j] = 0;
            
        }
        
    }
    
    // This routine will be called whenever a packet sent from the B-side 
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side.  "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    
    protected void bInput(Packet packet){
        
        System.out.println("I am in b_input " + getTime());

        // get the packet from layer 3 and checks if its corrupted
        
        String data = packet.getPayload();
        
        int seq_num = packet.getSeqnum();
        
        int ack_num = packet.getAcknum();
        
        int check_sum = packet.getChecksum();
        
        int check = check_sum(seq_num, ack_num, data);
        
        // S&W protocal
        
        if(WindowSize == 1){
            
                //CORRUPT PACKETS
            
            if(check != check_sum){ // the data we got back has been corrupted, dont send ack and dont buffer it
                
                pck_received[seq_num] = false; // you have not recived this packet correctly now its the one youre waiting for
        
                System.err.println("Packet " + seq_num + " has been corrupted and dropped.");

                corruption_count_sender++;
                return;

            }
             // if you have a duplicate , drop the packet and  send an ack
            else if(pck_received[seq_num] == true){// && pck_received[seq_num] == true){
                    
                    System.out.println("Duplicate packet " + seq_num + " dropped. ");
                    
                    return;
            }
             
            else{
           
                 // otherwise the packet isnt corrupt or dup so we can send the acks and send it to layer 5
                
                System.out.println("Packet " +  seq_num + " has not been corrupted. ACK now being sent");
                
                
                Packet ACK =  new Packet(seq_num, ack_num, check_sum, data);
            
                toLayer3(1, ACK); // send ACK to layer A

                //pck_received[seq_num] = true;
                
                toLayer5(data);
                

                // move sliding window
                move_sliding_window_B();
          
                return;
            }
            
        }
         
        //S&R protocol
        else{
            
            //CORRUPT PACKETS
            if(check != check_sum){ // the data we got back has been corrupted, dont send ack and dont buffer it
                
                pck_received[seq_num] = false; // you have not recived this packet correctly now its the one youre waiting for
                
                System.err.println("Packet " + seq_num + " has been corrupted and dropped. No ACK");
                
                corruption_count_sender++;
                
                RTT_CORRUPT[seq_num] = getTime() - RTT[seq_num];
                RTT[seq_num] = 0.0;
                
                
                return;
                
            }
            // duplicate check: just drop it and dont sent ACK
            
            if(sliding_window_B[seq_num] == false){
                    
                    System.out.println("Duplicate packet " + seq_num + " dropped");
                   
                     return;
            }
                
            
            // NON CORRUPT/Duplicate PACKETS
            
            else{
                  
                System.out.println("Packet " +  seq_num + " has not been corrupted. ACK now being sent.");
                
             
                
                Packet ACK =  new Packet(seq_num, ack_num, check_sum, data);
                
                toLayer3(1, ACK); // send ACK to layer A
                
                // BUFFERING AND/OR SENDING TO LAYER 5 STEPS
                
                // if this is the last acknowledged packet we were waiting for, then we have to see if there are buffered packets waiting
                // on this packet. ie a later cumulative ack moves sender window by more than 1
                
                System.err.println("Last recieved packet is now " + LAST_RECEIVED);
                   
                if(SEND_BASE == -1 && seq_num == 0){
                    
                    System.out.println("We have recieved the packet " + 0.);
                   
                    ack_recieved[seq_num] = true;
                    
                    SEND_BASE = 0;
                   
                 }
                    
               
                else if(seq_num == LAST_RECEIVED){
                    
                    loss_count++;
                    System.out.println("We have recieved the packet " + seq_num + " that we had been waiting on.");
                    
                    //see how many packets you are actually waiting on withing your sending window
                
                    for(int i = LAST_RECEIVED; i < buffering_packets.length; i++){
                    
                        if(buffering_packets[i] != null && pck_received[i] == true){
                        
                            String data2 = buffering_packets[i].getPayload();
        
                            System.out.println("Sending buffered packet " +  i + " to layer 5 now.");
                            
                            toLayer5(data2);
                            
                            LAST_RECEIVED = i;
                           
                            move_sliding_window_B();
                            
                           
                        }
                        
                    }
                    
                }
                
                // BEST CASE:
                //if the packet you recieved is the next in sequential order, time to send it to above and slide the window
                
                else if((seq_num - LAST_RECEIVED) == 1 || (seq_num - LAST_RECEIVED) == -1){
                        
                    System.out.println("Packet " +  seq_num + " was in order: it has now been sent to layer 5.");

                    toLayer5(data);
                    
                    LAST_RECEIVED = seq_num;
                    
                    System.out.println(" LAST_RECEIVED is now " + LAST_RECEIVED);
                    
                    move_sliding_window_B();
                    
                }
                
                //check if the packet is greater than the last recieved packet and set that last recieved packet as the last acked
                // set all the packets you haven't recieved in the interm as waiting on 
                
                else if((seq_num - LAST_RECEIVED) > 1){
                    
                    System.out.println("Packet"+ seq_num +" is larger than other outstanding unacknowledged packets. Buffer packet.");
                    
                    // you want to buffer the packet
                    buffering_packets[seq_num] = packet;
                    pck_received[seq_num] = true;
                    loss_count++;
                    
                    if(LAST_RECEIVED == -1){
                        
                        LAST_RECEIVED = 0;
                    }
                  
                    // don't update last received because there is still a packet we are waiting for
                    
                    
                }
            }
            
        }
      
    }
 
    // This routine will be called once, before any of your other B-side 
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    
    protected void bInit(){
        
        // initialize all my helper variables and the arrays I will use for the reciever side
        
        // an variable to keep track of the last packet recieved in the sender window
        // for the purpose of not sliding the window before all packets have been recieved and sent to layer 5
        // in the sender window
        LAST_RECEIVED = -1;
        
        pck_received = new boolean[LimitSeqNo]; // tell if a packet for a certain index has actually been recieved
        
        sliding_window_B = new boolean[LimitSeqNo]; // this is a indicator of where the sliding window is located
    
        buffering_packets = new Packet[LimitSeqNo]; // this will tell the sender side which packets it recieves from the A side
    
        // initialize the sliding window to the correct place
        
        for(int k = 0; k < WindowSize; k++){
            
            sliding_window_B[k] = true;
        }
        
        // initialize the buffred packets to null
        for(int i = 0; i < LimitSeqNo; i++){
            
            buffering_packets[i] = null;
        }


    }
    
    // this checks if there are acks array which checks if packets have been acked on the sender window size
    
     protected boolean no_acks(){
        
        for(int i = 0; i < ack_recieved.length; i++){ // go through the sliding window array and see if the window is filled up
            
            if(ack_recieved[i] == false && sliding_window[i] == true && p_sender_window[i] != null) 
                
                continue;
             
            else if(ack_recieved[i] == false && sliding_window[i] == true && p_sender_window[i] != null)
                 
                return false;
        }
        return true;
        
    }
    
     // check sum function: It will asccify the data string
    protected int check_sum(int sequence_num, int ack, String data){
        
        //find string length
        int str_len = data.length();
        int sum = 0;
        
        // get ascii version of the data
        
        for(int i = 0; i < str_len; i++){
            
            sum += (int) data.charAt(i);
            
        }
        
        sum = sum + sequence_num;
        return sum;
        
    }
    
// this function checks if the sliding window for the sender is full 
    
    protected boolean is_sliding_window_full(){
        
        for(int i = 0; i < sliding_window.length; i++){ // go through the sliding window array and see if the window is filled up
            
            if(sliding_window[i] == true && p_sender_window[i] == null)
                
                return false;
             
            else if(sliding_window[i] == true && p_sender_window[i] != null && sliding_window[i+1] == false)
                 
                return true;
        }
        return false;
        
    }
    // this fucntion sees if there is still room in the sender window to add packets to send 
    
    protected boolean is_p_sender_window_available(){
        
        for(int i = 0; i < p_sender_window.length; i++){ // go through the sliding window array and see if the window is filled up
            
            if(p_sender_window[i] == null && sliding_window[i] == true)
                
                return true;
            
            else
                continue;
        }
        return false;
        
    }
    
    // this function checks if the sliding window is full
  
    protected boolean is_sliding_window_empty(){
        
        for(int i = 0; i < sliding_window.length; i++){ // go through the sliding window array and see if the window is filled up
            
            if(sliding_window[i] == true && p_sender_window[i] == null)
                
                continue;
             
            else if(sliding_window[i] == true && p_sender_window[i] != null)
                 
                return false;
        }
        return true;
        
    }
    
    // this function is a helper function for a_output 
   // and will return the index of the next avaliable place in order to put the arrays
    
        
    //updating the start of window variable: seeing where the start of the sliding window is on the sender A side
    
    protected void add_start_of_window(){
        
        
        if(WindowSize == 1){
            
            if(START_OF_WINDOW == 0)
                START_OF_WINDOW = 1;
            else
                START_OF_WINDOW = 0;
        }
        else{
        
            if(START_OF_WINDOW + 1 > LimitSeqNo){
            
                START_OF_WINDOW = START_OF_WINDOW % LimitSeqNo;
            }
        
            else
        
                START_OF_WINDOW++;
        }
    }
    
    // this function will move along the sliding window, set the most recent recieved packet to null and return the next seq_num to send
    protected int move_sliding_window_A(){
        
        // case for S&W
                    
        if(WindowSize == 1){
            
            if(sliding_window[0] == true){
            
                sliding_window[1] = true;

                START_OF_WINDOW = 1;
                sliding_window[0] = false;
                p_sender_window[0] = null;
                a_times[0] = 0.0;
                return 0;
            }
            
            else{
                
                sliding_window[0] = true;
                START_OF_WINDOW = 0;
                sliding_window[1] = false;
                p_sender_window[1] = null;
                a_times[1] = 0.0;
                return 1;
            }
        }
        
        else{
            
        add_start_of_window();
        
        // wrap around case: it has reached the end of the array
        if(sliding_window[LimitSeqNo-1] == true){
            
            if(sliding_window[0] == false){
                
                sliding_window[0] = true;
                
                for(int i = 0; i < sliding_window.length-1; i++){
                    
                    if(sliding_window[i] == false && sliding_window[i+1] == true){
                        
                        System.out.println(i);
                        
                        sliding_window[i+1] = false;
                        p_sender_window[i+1] = null;
                        a_times[i+1] = 0.0;
                        return 0;
                    }
                    
                }
            }
            
            // wrap around case: it has already begun to wrap arou
            
            if(sliding_window[LimitSeqNo-1] == true && (LimitSeqNo) == 2){
                
                sliding_window[0] = true;
                sliding_window[1] = false;
                p_sender_window[0] = null;
                a_times[0] = 0.0;
                return(0);
                
            }
            // case if window size has already started to wrap around
            else{
                
                for(int k = 0; k < sliding_window.length; k++){
                    
                    if(sliding_window[k] == false && sliding_window[k+1] == true){
                        
                        sliding_window[k+1] = false;
                        p_sender_window[k+1] = null;
                        a_times[k+1] = 0.0;
                        
                        System.out.println((0 + (LimitSeqNo-1) - k)-1);
                        
                        if(((0 + (LimitSeqNo-1) - k)-1) == 0)
                            sliding_window[(0 + (WindowSize-1))] = true;
                        
                        else
                            sliding_window[(0 + (LimitSeqNo-1) - k)-1] = true;
                        
                        return((0 + (LimitSeqNo-1) - k)-1);
                        
                    }
                    
                }
                
            }
            
        }
        // no wrap around == move the window along
        
        else if(sliding_window[LimitSeqNo-1] == false){
            
            for(int k = 0; k < sliding_window.length; k++){
                
                if(sliding_window[k] == true && sliding_window[k+1] == false){
                    
                    sliding_window[k+1] = true;
                    
                    sliding_window[k+1 - (WindowSize)] = false;
                    p_sender_window[k+1 - (WindowSize)] = null;
                    a_times[k+1 - (WindowSize)] = 0.0;
                    
                    return(k+1 - WindowSize);
                    
                }
            }
        }
        
        return 0;
        }
        
    }
    
       
    // function to move B's sliding window
    
    protected int move_sliding_window_B(){
        
         // case for S&W
                    
        if(WindowSize == 1){
                     
            if(sliding_window_B[0] == true){
            
                START_OF_WINDOW = 0;
                
                sliding_window_B[1] = true;

                sliding_window_B[0] = false;
                buffering_packets[0] = null; // remove that packet from memory
                pck_received[0] = false;
                

                return 0;
            }
            else{
                
                sliding_window_B[0] = true;
                START_OF_WINDOW = 1;
                sliding_window_B[1] = false;
                buffering_packets[1] = null; // remove that packet from memory
                pck_received[1] = false;
                
                return 1;
            }
        }
        
        // case for SR protocol 
        
        int z = LimitSeqNo - 1;
        
         // wrap around case: ie the sliding window is at the end of the LimitSeqNo 

        if(sliding_window_B[z] == true){
                    
            if(sliding_window_B[0] == false){
                        
                sliding_window_B[0] = true;
                        
                for(int i = 0; i < sliding_window_B.length-1; i++){
                           
                    if(sliding_window_B[i] == false && sliding_window_B[i+1] == true){
                                
                        sliding_window_B[i+1] = false;
                        buffering_packets[i+1] = null; // remove that packet from memory
                        pck_received[i+1] = false;
                                
                        return 0; //next seq_num to send
                    }
  
                }
                    
            }
           // case if window size has already started to wrap around
            else{
                        
                for(int k = 0; k < sliding_window_B.length; k++){
                        
                    if(sliding_window_B[k] == false && sliding_window_B[k+1] == true){
                                
                        sliding_window_B[k+1] = false;                 // keep all the arrays in order
                        buffering_packets[k+1] = null;                            
                        pck_received[k+1] = false;
                        
                        System.out.println((0 + z - k)-1);
                        
                        
                        if(((0 + z - k)-1) == 0){ // if the wrap around has just begun
                            
                            sliding_window_B[(0 + (WindowSize-1))] = true; 
                            
                            return((0 + (WindowSize-1))); //next seq_num to send
                        }
                        
                        else{  //if the window has already started wrapping around
                            sliding_window_B[(0 + z - k)-1] = true;   
                            
                            return((0 + z - k)-1); //next seq_num to send
                            
                        }
                        
                        
                    }
                    
                }
                
            }
            
        }
        // no wrap around == move the window along
        
        else if(sliding_window_B[z] == false){
            
            
            
            for(int k = 0; k < sliding_window_B.length; k++){
                
                if(sliding_window_B[k] == true && sliding_window_B[k+1] == false){
                    
                    sliding_window_B[k+1] = true;
                    
                    sliding_window_B[k+1 - (WindowSize)] = false;
                    
                    buffering_packets[k+1 - (WindowSize)] = null; // keep arrays in order
                    
                    pck_received[k+1 - (WindowSize)] = false;
                    
                    return(k+1); // next seq_num to send
                    
                }
            }
        }
        return 0;
    }
       
   
    // Use to print final statistics
    protected void Simulation_done()
    {
        
        
       System.out.println("retransmits as a function of corruption = " + corruption_count_sender);
       System.out.println("retransmits as a function of loss = " + loss_count);
       
       System.out.println("original_data_pckts_sent = " + original_data_pckts_sent);
       
       System.out.println("retransmitted = " + retransmitted);
       
       System.out.println("acks_recieved = " + acks_recieved);
    
       System.out.println("RTT average time(including loss and corruption) = " + RTT_AVERAGE);
       
       System.out.println("RTT_CORRUPT_AVERAGE  = " + RTT_CORRUPT_AVERAGE);
       
       System.out.println("RTT_LOSS_AVERAGE = " +RTT_LOSS_AVERAGE);
                                         
       

    } 

}
