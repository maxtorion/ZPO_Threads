import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {




    private static void threadsExample(List<Item> itemList)
    {
        final AtomicInteger SAFETOCONSUME
                = new AtomicInteger();
        final AtomicInteger ALREADYCONSUMED
                = new AtomicInteger();
        final AtomicInteger ALREADYPRODUCED
                = new AtomicInteger();
        List<Thread> prodThreadsList = Collections.synchronizedList(new ArrayList<Thread>());

        //wątki
        //create four "producing" threads
        for(int i = 0;i<4;i++)
        {
            prodThreadsList.add(new Thread(()->{
                try {
                    while (ALREADYPRODUCED.get()<100)
                    {
                        itemList.get(ALREADYPRODUCED.getAndIncrement()).produceMe();
                        SAFETOCONSUME.getAndIncrement();
                    }

                }catch(RuntimeException e)
                {
                    System.out.println(e);
                }

            }));
        }

        //create three "consuming threads" threads
        List<Thread> consumeThreadsList = Collections.synchronizedList(new ArrayList<Thread>());
        for(int i = 0;i<3;i++)
        {
            consumeThreadsList.add(new Thread(()->{
                try {
                    while (ALREADYCONSUMED.get()<100)

                        if(ALREADYCONSUMED.get()<SAFETOCONSUME.get())
                            itemList.get(ALREADYCONSUMED.getAndIncrement()).consumeMe();

                }catch(RuntimeException e)
                {

                    System.out.println(e);
                }

            }));
        }
        //concatenate both lists
        prodThreadsList.addAll(consumeThreadsList);
        //to make threads more random
        Collections.shuffle(prodThreadsList);


        //start all threads
        //Reanimator thread
        Thread reanimator = new Thread(()->{

            for (Thread thread:prodThreadsList)
            {
                if(!thread.isAlive())
                {
                    thread.start();
                }
            }

        });
        reanimator.start();

    }

    private static synchronized boolean compareValues(int val1,int val2, boolean reverse)
    {
        if(reverse ==false)
            return val1 < val2;
        else
            return  val1 > val2;
    }

    private static void poolThreadsExample(List<Item> itemList)
    {
        final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(7);
        final AtomicInteger SAFETOCONSUME
                = new AtomicInteger();
        final AtomicInteger ALREADYCONSUMED
                = new AtomicInteger();
        final AtomicInteger ALREADYPRODUCED
                = new AtomicInteger();
        final AtomicInteger BOUNDARY = new AtomicInteger(100);



        for(int i=0;i<7;i++)
        {

            threadPool.execute(()->{

                while (ALREADYCONSUMED.get()<BOUNDARY.get())
                {


                    if(compareValues(SAFETOCONSUME.get(),ALREADYCONSUMED.get(),true))
                    {

                            itemList.get(ALREADYCONSUMED.getAndIncrement()).consumeMe();

                    }
                    else
                    {

                            if(compareValues(ALREADYPRODUCED.get(),BOUNDARY.get(),false))
                            {

                                try {
                                    itemList.get(ALREADYPRODUCED.getAndIncrement()).produceMe();
                                }catch (IndexOutOfBoundsException e)
                                {
                                    continue;
                                }
                                SAFETOCONSUME.getAndIncrement();
                            }
                    }
                }
            });
        }

        threadPool.shutdown();

    }

    private static void parralerStreamExample(List<Item> itemList)
    {
        itemList.parallelStream().forEach(item -> {item.produceMe();item.consumeMe();});

    }

    public static void main(String[] args) {
        List<Item> itemList = Collections.synchronizedList(new ArrayList<Item>());
        //create 100 items in item list
        for(int i = 0;i<100;i++)
        {
            itemList.add(new Item());
        }
        //threadsExample(itemList);
        //poolThreadsExample(itemList);
        parralerStreamExample(itemList);


        }
    }

