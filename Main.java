import java.util.Random;
import java.util.ArrayList;

public class Main
 {
    static int numberGenerationsRun = 0;
    public static void main(String[] args)
     {
        final int numQueens = 25;
        final int numIterations = 1;
        int problemArray[];
        int solutionArray[];
        long startTime;
        long endTime;
        double tempTime;
        double totalTime = 0.0;
        //genetic
        final int numGenerations = 50000;
        final int initialPopSize = 30;
        int averageSearchCost = 0;
        double numberGeneticSuccesses = 0.0;
        //annealing
        final int smallT = 20000;
        final double temp = 2000;
        double numberAnnealingSuccesses = 0.0;


         System.out.println("Simmulated Annealing Algorithm ");
         for(int i = 0; i < numIterations; i++)
          {
             problemArray = createBoard(numQueens);
             startTime = System.nanoTime();
             solutionArray = simulatedAnneaing(problemArray, temp, smallT);
             endTime = System.nanoTime();
             tempTime = (endTime - startTime) / 1000000000.0;
             totalTime += tempTime;
             if(numQueensAttacking(solutionArray) == 0)
                 numberAnnealingSuccesses++;
             if(numIterations == 1)
                 printSolution(solutionArray);

          }
        System.out.println("Stats");
        if(numIterations > 1)
         {
           System.out.println("Percent Success:" + ((numberAnnealingSuccesses/numIterations) * 100));
           System.out.println("Average Time Taken: " + (totalTime / numIterations) + " Seconds");
           System.out.println("Average Search cost: " + smallT);
           totalTime = 0;
         }
        else
         {
           if(numberAnnealingSuccesses == 1)
               System.out.println("Success");
           else
               System.out.println("Fail");
           System.out.println("Time Taken: " + (totalTime / numIterations) + " Seconds");
           System.out.println("Search cost: " + smallT);
           totalTime = 0;
         }

        System.out.println("\nGenetic Algorithm ");
        for(int i = 0; i < numIterations; i++)
         {
          startTime = System.nanoTime();
          solutionArray = genetic(numGenerations, numQueens, initialPopSize);
          endTime = System.nanoTime();
          tempTime = (endTime - startTime) / 1000000000.0;
          totalTime += tempTime;
          averageSearchCost += numberGenerationsRun;
          if(numQueensAttacking(solutionArray) == 0)
             numberGeneticSuccesses++;
           if(numIterations == 1)
             printSolution(solutionArray);

         }
         System.out.println("Stats");
         if(numIterations > 1)
         {
             System.out.println("Percent Success:" + ((numberGeneticSuccesses/numIterations) * 100));
             System.out.println("Average Time Taken: " + (totalTime / numIterations) + " Seconds");
             System.out.println("Average Search cost: " + ((averageSearchCost * initialPopSize) / numIterations));
             totalTime = 0;
         }
         else
         {
             if(numberGeneticSuccesses == 1)
                 System.out.println("Success");
             else
                 System.out.println("Fail");
             System.out.println("Time Taken: " + (totalTime / numIterations) + " Seconds");
             System.out.println("Search cost: " + ((averageSearchCost * initialPopSize) / numIterations));
             totalTime = 0;
         }

     }
     //Genetic Stuff
     static int[] genetic(int generations, int numQueens, int initialPopSize)
      {
       //tournament based genetic algorithm
       final int mutationChance = 10;
       final int k = 8;
       numberGenerationsRun = 0;
       int tempBoard[];
       int parentX[];
       int parentY[];
       int child[];
       boolean found = false;
       Random rand = new Random();
       int z = 0; //counter for generations
       int fittestLocation = -1;
       int temp;
       int r;
       int mutationTo;
       //create initial population i.e all possible movements from this board
       //arraylist of those possible board states
       ArrayList<int[]> population = new ArrayList<int[]>();
       ArrayList<int[]> newPopulation = new ArrayList<int[]>();
       ArrayList<int[]> kPopulation = new ArrayList<int[]>();
       int randRow;
       int kCounter = 0;
       //generate random initial population
       for(int i = 0; i < initialPopSize; i++)
        {
         tempBoard = createBoard(numQueens);
         population.add(tempBoard.clone());
        }

       //actual genetic algorithm
       while(z < generations && !found)
        {
          newPopulation.clear();
          for(int y = 0; y < population.size(); y++)
          {

              kPopulation.clear();
              //select k number of nodes from population
              while(kCounter < k)
               {
                randRow = rand.nextInt(population.size());
                kPopulation.add(population.get(randRow).clone());
                kCounter++;
               }
              kCounter = 0;
              parentX = selection(kPopulation);
              parentY = selection(kPopulation);

              child = reproduce(parentX,parentY);
              r = rand.nextInt(101);
              if(r < mutationChance)
              {
                  //mutate
                  r = rand.nextInt(child.length);
                  mutationTo = rand.nextInt(child.length);
                  child[r] = mutationTo;
              }

              newPopulation.add(child.clone());
          }
          //new population becomes the population
          population.clear();

         //check if a fit enough individual exists within the new population
         for(int i = 0; i < newPopulation.size(); i++)
          {
           temp = numQueensAttacking(newPopulation.get(i));
           if(temp == 0)
            {
             found = true;
             fittestLocation = i;
             break;
            }
          }

         for(int i = 0; i < newPopulation.size(); i++)
          {
           population.add(newPopulation.get(i).clone());
          }

         z++;
         numberGenerationsRun++;
         //end while loop
        }

        //if found is false, just return the best thing we ended up getting
        /*
        int bestQueens = Integer.MAX_VALUE;
        int temp;
        if(found == false)
         {
          for(int i = 0; i < newPopulation.size(); i++)
           {
             temp = numQueensAttacking(newPopulation.get(i));
             if(temp < bestQueens)
              {
               bestQueens = temp;
               fittestLocation = i;
              }
           }
         }
         */
       //For the purposes of speed, I commented out the above code which would return the best
       //board if the solution wasn't found, since in this case we only care about success percentage.
       if(!found)
           fittestLocation = 0;
       return newPopulation.get(fittestLocation); //.clone
      }

     static int[] reproduce(int[] parentX, int[] parentY)
      {
        int childArray[] = new int[parentX.length];
        Random rand = new Random();
        //generate a random crossover point. length-2 +1 generates range from 2nd element to 2nd to last element
        // crossover copies everything left from crossover from X and anything right(inclusive) from Y and places into child array
        //this is done as otherwise it could just be a carbon copy of either X or Y
        int crossover = rand.nextInt((parentX.length)-2) + 1;
        for(int i = 0; i < parentX.length; i++)
         {
           if(i < crossover)
             childArray[i] = parentX[i];
           else
             childArray[i] = parentY[i];

         }
        return childArray; //.clone()
      }

     static int[] selection(ArrayList<int[]> kPopulation)
      {
       int location = -1;
       int highest = -1;
       int attackValue;

       for(int i = 0; i < kPopulation.size(); i++)
        {
         attackValue = numberOfQueensNotAttacking(kPopulation.get(i));
         if(attackValue > highest)
          {
           highest = attackValue;
           location = i;
          }
        }


       return kPopulation.get(location); //.clone
      }

      //end of genetic methods

      //Simulated annealing stuff
      static int[] simulatedAnneaing(int[] problemArray, double temp, int smallT)
       {
        final double decayConstant = 0.03;
        int initialArray[] = problemArray.clone();
        int currentArray[] = problemArray.clone();
        int tempArray[];
        int nextArray[];
        int deltaE;
        int nextVal;
        int currentVal;
        int randCol;
        int randRow;
        double exponential;
        double randomDouble;
        double currentTemp = temp;
        Random rand = new Random();
        //body of
        //small t value cuts off algo once it hits a low enough 0.0000 value
        for(int i = 0; i < smallT; i++)
         {
          currentTemp = schedule(decayConstant, currentTemp);
          if(currentTemp == 0.0)
               return currentArray;
          //generate random successor. Resued from genetic
          randRow = rand.nextInt(initialArray.length);
          randCol = rand.nextInt(initialArray.length);
          tempArray = currentArray.clone();
          tempArray[randRow] = randCol;
          nextArray = tempArray.clone();
          //calculate DeltaE
          nextVal = numberOfQueensNotAttacking(nextArray);
          currentVal = numberOfQueensNotAttacking(currentArray);
          deltaE = (nextVal - currentVal);
          if(deltaE > 0)
              currentArray = nextArray.clone();
          else
           {
             exponential = Math.exp((deltaE / currentTemp));
             randomDouble = ((rand.nextInt(100) + 1) / 100.0);
             if(exponential > randomDouble)
               currentArray = nextArray.clone();
           }
         }
        return currentArray;
       }

     static double schedule(double decayConstant, double currentTemp)
      {
       double decay = currentTemp * decayConstant;
       double newTemp = currentTemp - decay;
       if(newTemp <= 0.0)
           return 0.0;
       else
           return newTemp;
      }


     //helper fuctions
     static int[] createBoard(int numQueens)
     {
      //Create a 1-D array that represents board
      //Since, for this implementation we do not have to worry about queens attacking in the columns
      //since each queen will be placed in its own, index of array will represent column
      //and the value at that index represents which row its at
      //0
      //.
      //.
      //24
      int array[] = new int[numQueens];
      Random rand = new Random();
      int randSpot;
      //fill array
      for(int i = 0; i < array.length; i++)
       {
        randSpot = rand.nextInt(numQueens);
        array[i] = randSpot;
       }

      return array; //.clone
     }

    static int numQueensAttacking(int[] board)
     {
      int numQueensAttacking = 0;
      //check if any row attacks
      for(int i = 0; i < board.length; i++)
       {
        for(int x = 0; x < board.length; x++)
         {
           if(board[i] == board[x] && (x != i))
           {
               numQueensAttacking++;
           }
         }
       }

      //check for diagonal attacks
      for(int i = 0; i < board.length; i++)
       {
        for(int x = 0; x < board.length; x++)
         {
           if(Math.abs(i - x) == Math.abs(board[i] - board[x]) && (x != i))
            {
              numQueensAttacking++;
            }
         }
       }

      return numQueensAttacking;
     }

   static int numberOfQueensNotAttacking(int[] board)
    {
      int maxNumQueenNotAttacking = (board.length * (board.length - 1));
      int queensAttacking = numQueensAttacking(board);
      return (maxNumQueenNotAttacking - queensAttacking);
    }

    static void printSolution(int[] board)
     {
      for(int i = 0; i < board.length; i++) //rows
       {
        for(int x = 0; x < board.length; x++)   //columns
         {
             //incomplete
          if(board[x] == i)
              System.out.print(" Q");
          else
              System.out.print(" -");
         }
         System.out.println();
       }
     }
 }
