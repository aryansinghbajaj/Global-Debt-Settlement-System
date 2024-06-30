

import java.util.*;

// Utility class to represent a pair of values such as storing the transaction amount and the associated payment mode
class Pair<T1, T2> {
    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
}

// Class representing each nation, net amount balance, and the set of payment modes
class Nation {
    String name;
    int netAmount;  // The amount the nation owes or is owed
    Set<String> types;  // Set of payment types the nation supports

    public Nation(String name) {
        this.name = name;
        this.netAmount = 0;
        this.types = new HashSet<>();
    }
}


// Contains the logic for minimizing the number of transactions among nations
 public class paynode {
  public static void printAns(List<List<Pair<Integer, String>>> ansGraph, Nation[] input) {
    System.out.println("\nThe most optimised transactions are:\n");
  
    for (int i = 0; i < ansGraph.size(); i++) {
        for (int j = 0; j < ansGraph.get(i).size(); j++) {
  
            if (i == j) continue;//ignoring  the diagonal elements as they involve transaction among them selves
  
            Pair<Integer, String> ij = ansGraph.get(i).get(j);
            Pair<Integer, String> ji = ansGraph.get(j).get(i);
  
            if (ij.first != 0 && ji.first != 0) {
  
                if (ij.first == ji.first) {
                    ij.first = 0;
                    ji.first = 0;
                } else if (ij.first > ji.first) {//i owning more money to j than j owns to i
                    ij.first -= ji.first;
                    ji.first = 0;
                    System.out.println(input[i].name + " pays Rs " + ij.first + " to " + input[j].name + " via " + ij.second);
                } else {
                  // j owning more money to i as i ownning to j
                    ji.first -= ij.first;
                    ij.first = 0;
                    System.out.println(input[j].name + " pays Rs " + ji.first + " to " + input[i].name + " via " + ji.second);
                }
            } else if (ij.first != 0) {
                System.out.println(input[i].name + " pays Rs " + ij.first + " to " + input[j].name + " via " + ij.second);
            } else if (ji.first != 0) {
                System.out.println(input[j].name + " pays Rs " + ji.first + " to " + input[i].name + " via " + ji.second);
            }
  
            ij.first = 0;
            ji.first = 0;
        }
    }
    System.out.println();
  }
    // Method to find the nation with the smallest net amount (the nation with the maximum debt)
    public static int getMinNation(Nation[] listOfNetAmounts) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0)
                continue;
            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    // Method to find the nation with the max net amount (max creditor)
    public static int getMaxNation(Nation[] listOfNetAmounts) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0)
                continue;
            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    // Method to find the nation which could settle the debt of another nation considering payment modes
    public static Pair<Integer, String> getMaxIndex(Nation[] listOfNetAmounts, int minIndex, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = "";
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0)
                continue;
            if (listOfNetAmounts[i].netAmount < 0)
                continue;
            // Find intersection of payment types
            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);
            // Find the best creditor (nation with max net amount and common payment mode)
            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();  // Store one common payment mode
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    // Method using a graph-based approach to settle debts among nations
    public static void minimizeTransaction(int numNations, Nation[] input, Map<String, Integer> indexOf, int[][] graph, int maxNumTypes) {
        // Finding the net amount of each nation
        Nation[] listOfNetAmounts = new Nation[numNations];
        // Calculating net amounts for each nation
        for (int n = 0; n < numNations; n++) {
            listOfNetAmounts[n] = new Nation(input[n].name);
            listOfNetAmounts[n].types = input[n].types;
            int amount = 0;
            // Calculate incoming transactions (column traversal in the graph)
            for (int i = 0; i < numNations; i++) {
                amount += graph[i][n];
            }
            // Calculate outgoing transactions (row traversal in the graph)
            for (int j = 0; j < numNations; j++) {
                amount -= graph[n][j];
            }
            listOfNetAmounts[n].netAmount = amount;
        }
        // Initialize the graph to store transaction details
        List<List<Pair<Integer, String>>> ansGraph = new ArrayList<>();
        for (int i = 0; i < numNations; i++) {
            List<Pair<Integer, String>> row = new ArrayList<>();
            for (int j = 0; j < numNations; j++) {
                row.add(new Pair<>(0, ""));
            }
            ansGraph.add(row);
        }
        // Count the number of nations with 0 net balance
        int zeroNetAmounts = 0;
        for (Nation nation : listOfNetAmounts) {
            if (nation.netAmount == 0)
                zeroNetAmounts++;
        }
        // Run loop until all nations have 0 net amount (no creditor, no debtor)
        while (zeroNetAmounts != numNations) {
            // Find the nation with minimum net amount (largest debtor)
            int minIndex = getMinNation(listOfNetAmounts);
            // Find the best creditor for the nation at minIndex
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, minIndex, maxNumTypes);
            int maxIndex = maxAns.first;
            if (maxIndex == -1) {
                // Handle case where no suitable creditor found with a common paymentmode 
                //transfer the debt to UnitedNations(index 0)
                ansGraph.get(minIndex).get(0).first+=Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(minIndex).get(0).second = listOfNetAmounts[minIndex].types.iterator().next();
                //find the nation with maximum net amount (largest creditor)
                int simpleMaxIndex = getMaxNation(listOfNetAmounts);
                //record the transaction to UnitedNation(index 0)
                ansGraph.get(0).get(simpleMaxIndex).first+=Math.abs(listOfNetAmounts[minIndex].netAmount);
                ansGraph.get(0).get(simpleMaxIndex).second = listOfNetAmounts[simpleMaxIndex].types.iterator().next();
                //adjust the net amounts after the settlement
                listOfNetAmounts[simpleMaxIndex].netAmount+=listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount=0;
                //update the count of nations having 0 net amounts
                if(listOfNetAmounts[minIndex].netAmount==0)
                zeroNetAmounts ++;
                if(listOfNetAmounts[maxIndex].netAmount==0)
                zeroNetAmounts++;
            }
                else
            {
                 // Perform a transaction between minIndex (debtor) and maxIndex (creditor)
            int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);

            // Record the transaction in the graph
            ansGraph.get(minIndex).get(maxIndex).first += transactionAmount;
            ansGraph.get(minIndex).get(maxIndex).second = maxAns.second;

            // Adjust net amounts after settlement
            listOfNetAmounts[minIndex].netAmount += transactionAmount;
            listOfNetAmounts[maxIndex].netAmount -= transactionAmount;

            // Update count of nations with zero net amounts
            if (listOfNetAmounts[minIndex].netAmount == 0) zeroNetAmounts++;
            if (listOfNetAmounts[maxIndex].netAmount == 0) zeroNetAmounts++;
             }
             //print the optimised transactions
             printAns(ansGraph , input);
        }
    }
      public static void main(String[] args) {
          Scanner sc = new Scanner(System.in);
  
          System.out.println("\n\t\t\t\t********************* Welcome to GLOBAL DEBT SETTLEMENT SYSTEM ***********************\n\n\n");
          System.out.println("This system minimizes the number of transactions among multiple nations in different parts of the world that use different modes of payment. We have United Nation to settle the debt between the nation where there is no common mode of payment.\n\n");
          System.out.println("Enter the number of nations participating in the transactions.");
          int numNations = sc.nextInt();
  
          Nation[] input = new Nation[numNations];
          Map<String, Integer> indexOf = new HashMap<>();
  
          System.out.println("Enter the details of the nations and transactions as stated:");
          System.out.println("Nation name, number of payment modes it has, and the payment modes.");
          System.out.println("Nation name and payment modes should not contain spaces");
  
          int maxNumTypes = 0;
  
          for (int i = 0; i < numNations; i++) {
              if (i == 0) {
                  System.out.print("United Nation representative country: ");
              } else {
                  System.out.print("Nation " + i + ": ");
              }
  
              String name = sc.next();
              input[i] = new Nation(name);
              indexOf.put(name, i);
  
              int numTypes = sc.nextInt();
              if (i == 0) maxNumTypes = numTypes;
  
              for (int j = 0; j < numTypes; j++) {
                  String type = sc.next();
                  input[i].types.add(type);
              }
          }
  
          System.out.println("Enter the number of transactions.");
          int numTransactions = sc.nextInt();
  
          int[][] graph = new int[numNations][numNations];
  
          System.out.println("Enter the details of each transaction as stated:");
          System.out.println("Debtor Nation, creditor Nation and amount");
          System.out.println("The transactions can be in any order");
          for (int i = 0; i < numTransactions; i++) {
              System.out.print(i + " th transaction: ");
              String s1 = sc.next();
              String s2 = sc.next();
              int amount = sc.nextInt();
  
              graph[indexOf.get(s1)][indexOf.get(s2)] = amount;
          }
  
          // Settle the transactions
          minimizeTransaction(numNations, input, indexOf, graph, maxNumTypes);
  
          sc.close();
      }
  
}



