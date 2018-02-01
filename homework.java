
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/***********************************************************************************************
 * Name: Eric J. Hachuel
 * USC loginid: hachuelb
 * CSCI 561 - Artificial Intelligence, Fall 2017 (Homework 2 - Adverserial Search - Game Playing)
 * Algorithm Implemented: Minimax with alpha-beta prunning (Iterative Deepening Search)
 * Due: October 17th, 2017
 ***********************************************************************************************/

/*****************************************************************************
 * The Node Class stores information about the state, connected component list
 * and total value for each player for a specific move on the board
 ****************************************************************************/
class Node{
    //TODO: SHOULD COMPARABLE BE IMPLEMENTED TO RETURN THE NODE WITH BEST VALUE SO FAR BY SORTING?
    private static final int EMPTY_CELL = -1;
    private final ArrayList<ConnectedComponent> actionsList;
    private final String player;
    private final int[][] currentState;
    private final int depth;
    private final double maxCurrValue;
    private final double minCurrValue;
    private double maxPotentialValue;
    private double minPotentialValue;
    private double terminalValue;
    
    /**
     * Node constructor, instantiates a new node
     * @param currentState the input state
     * @param depth the depth of the node
     * @param maxCurrValue the "MAX" value of the node
     * @param minCurrValue the "MIN" value of the node
     * @param maxPotentialValue the potential value of "MAX" for the given node
     * @param minPotentialValue the potential value of "MIN" for the given node
     */
    public Node(int[][] currentState, int depth, double maxCurrValue, double minCurrValue, String player){
        this.actionsList = new ArrayList<>();
        this.player = player;
        this.depth = depth;
        this.maxCurrValue = maxCurrValue;
        this.minCurrValue = minCurrValue;
        this.currentState = currentState;
        this.maxPotentialValue = 0;
        this.minPotentialValue = 0;
        //Generate the list of connected components for the current Node
        generateActions();
        //Calculate Potential Value using generated list of actions
        calculatePotentialValue();
    }
    
    /**
     * calculatePotentialValue calculates the Potential Value for Min and Max of the current Node
     */
    public final void calculatePotentialValue(){
        //Calculate potential value of the node
        //This function takes the sorted actions list and estimates the value of each player in the future
        if(player.contentEquals("MAX")){
            for(int i = 0; i < actionsList.size(); i++){
                //Check if current index is even, add to Max
                if(i % 2 == 0){
                    maxPotentialValue+= actionsList.get(i).componentScore();
                }
                //if current index is odd, add value to Min
                else{
                    minPotentialValue+= actionsList.get(i).componentScore();
                }
            }
        }
        else{
            for(int i = 0; i < actionsList.size(); i++){
                //Check if current index is even, add to Max
                if(i % 2 == 0){
                    minPotentialValue+= actionsList.get(i).componentScore();
                }
                //if current index is odd, add value to Min
                else{
                    maxPotentialValue+= actionsList.get(i).componentScore();
                }
            }
        }
    }
    
    /**
     * generateActions takes in the state and recursively generates a list of all the connected
     * components in the given state. We will loop over the resulting list in our 
     * minimax code to generate the DFS tree.
     * @param currentNode the node to generate actions for
     */
    public final void generateActions(){
        //Store length of state 
        int stateLength = currentState.length;
        //Initialize boolean array of visited cells (all cells initially false)
        boolean[][] visitedCellsArray = new boolean[stateLength][stateLength];
        //Loop through state and recurse
        for(int i = 0; i < stateLength; i++){
            for(int j = 0; j < stateLength; j++){
                if(!visitedCellsArray[i][j] && currentState[i][j] != EMPTY_CELL){
                    //New Board Position 
                    BoardPosition initialPosition = new BoardPosition(currentState[i][j], i, j);
                    //Create new connected Component
                    ConnectedComponent newComponent = new ConnectedComponent();
                    //Recurse with the helper function
                    generateActionsRecursive(initialPosition, visitedCellsArray, newComponent, currentState);
                    //Add connected component to the actions list
                    actionsList.add(newComponent);
                }
            }
        }
        //Sort the actions list by size of the connected components
        Collections.sort(actionsList, Collections.reverseOrder());
    }
    
    /**
     * generateActionsRecursive is the helper recursive function
     * @param newPosition the next Board position
     * @param visitedArray the array of booleans
     * @param connectedComp the connected component
     * @param currentState the currents state configuration
     */
    public static void generateActionsRecursive(BoardPosition newPosition, boolean[][] visitedArray, ConnectedComponent connectedComp, int[][] currentState){
        //Get row and column of Board Position
        int row = newPosition.getRow();
        int col = newPosition.getCol();
        //Get the fruit type of the new Board Position
        int fruitType = newPosition.getFruitType();
        //Set the current Board Position (in the visited cells array) to true
        visitedArray[row][col] = true;
        //Check if the position is legal: next row
        if(isLegalBP(fruitType, row + 1, col, currentState, visitedArray)){
            //Create new Board Position and set cell to visited
            BoardPosition nextPosition = new BoardPosition(fruitType, row + 1, col);
            visitedArray[row + 1][col] = true;
            //Recurse on the new legal position
            generateActionsRecursive(nextPosition, visitedArray, connectedComp, currentState);
        }
        //Check if the position is legal: prior row
        if(isLegalBP(fruitType, row - 1, col, currentState, visitedArray)){
            //Create new Board Position and set cell to visited
            BoardPosition nextPosition = new BoardPosition(fruitType, row - 1, col);
            visitedArray[row - 1][col] = true;
            //Recurse on the new legal position
            generateActionsRecursive(nextPosition, visitedArray, connectedComp, currentState);
        }
        //Check if the position is legal: next column
        if(isLegalBP(fruitType, row, col + 1, currentState, visitedArray)){
            //Create new Board Position and set cell to visited
            BoardPosition nextPosition = new BoardPosition(fruitType, row, col + 1);
            visitedArray[row][col + 1] = true;
            //Recurse on the new legal position
            generateActionsRecursive(nextPosition, visitedArray, connectedComp, currentState);
        }
        //Check if the position is legal: prior column
        if(isLegalBP(fruitType, row, col - 1, currentState, visitedArray)){
            //Create new Board Position and set cell to visited
            BoardPosition nextPosition = new BoardPosition(fruitType, row, col - 1);
            visitedArray[row][col - 1] = true;
            //Recurse on the new legal position
            generateActionsRecursive(nextPosition, visitedArray, connectedComp, currentState);   
        }
        //If position is legal, add to the connected component
        connectedComp.addPosition(newPosition);
    }
    
    /**
     * isLegalBP returns true if the next available cell is a legal move
     * @param fruitType the type of fruit
     * @param currRow the current row
     * @param currCol the current column
     * @param currentState the 2D state configuration
     * @param visitedArray the array of boolean values to tell whether we have already visited the cell
     * @return true if the next available cell is a legal move
     */
    public static boolean isLegalBP(int fruitType, int currRow, int currCol, int[][] currentState, boolean[][] visitedArray){
        //Store the length of the state
        int stateLength = currentState.length;
        //Return true if the next cell is a legal move (i.e if it is within the limits of the board)
        return (currRow >= 0) && (currRow < stateLength) && (currCol >= 0) && (currCol < stateLength) && (!visitedArray[currRow][currCol])
                &&(currentState[currRow][currCol] != EMPTY_CELL) && (fruitType == currentState[currRow][currCol]);
    }
    
    /**
     * getTerminalValue calculates the Terminal value of the node
     * @return the terminal value of the node
     */
    public double getTerminalValue(){
        this.terminalValue = (this.maxCurrValue + this.maxPotentialValue) - (this.minCurrValue + this.minPotentialValue);
        return this.terminalValue;
    }
    
    /**
     * getState gets the state configuration of the board
     * @return the state configuration of the board
     */
    public int[][] getState(){
        return this.currentState;
    }
    
    /**
     * getPlayer returns the player type (MAX or MIN) of the Node
     * @return the player type (MAX or MIN) of the Node
     */
    public String getPlayer(){
        return player;
    }
    
    /**
     * getActionsList return the ArrayList of connected Components
     * @return return the ArrayList of connected Components
     */
    public ArrayList<ConnectedComponent> getActionsList(){
        return actionsList;
    }
    
    /**
     * getNumChildren returns the number of connected components in the actions List
     * @return the number of connected components in the actions List
     */
    public int getNumChildren(){
        return actionsList.size();
    }
    
    /**
     * getDepth returns the depth of the current Node
     * @return the depth of the current Node
     */
    public int getDepth(){
        return depth;
    }
    
    /**
     * getMaxValue returns the Max value of the node (running sum)
     * @return the Max value of the node
     */
    public double getMaxValue(){
        return maxCurrValue;
    }
    
    /**
     * getMinValue returns the Min value of the node (running sum)
     * @return the Min value of the node
     */
    public double getMinValue(){
        return minCurrValue;
    }
    
    /**
     * getMaxPotential returns the downwards potential for the "MAX" player
     * @return the downwards potential for the "MAX" player
     */
    public double getMaxPotential(){
        return maxPotentialValue;
    }
    
    /**
     * getMinPotential returns the downwards potential for the "MIN" player
     * @return the downwards potential for the "MIN" player
     */
    public double getMinPotential(){
        return minPotentialValue;
    }
}

/***************************************************************************
 * The ConnectedComponent class creates a list of boardPositions that are
 * connected to each other. Allows comparison of connected components,
 * ordered by the size of the internal lists. (allows to sort actions Lists)
 ***************************************************************************/
class ConnectedComponent implements Comparable{
    //The ArrayList containing Board Positions
    private final ArrayList<BoardPosition> connectedComp;
    
    //Constructor for a new Connected Component
    public ConnectedComponent(){
        //Instantiate arrayListof board Positions
        this.connectedComp = new ArrayList<>();
    }
    
    /**
     * addPosition adds a new Board Position to a specific connected component
     * @param newPosition the Board position to add to the connected component
     */
    public void addPosition(BoardPosition newPosition){
        connectedComp.add(newPosition);
    }
    
    /**
     * getBoardPosition returns the Board position at a specified index of the Connected Component
     * @param arrayIndex the index of the Board Position within the Connected Component
     * @return the selected Board Position
     */
    public BoardPosition getBoardPosition(int arrayIndex){
        return connectedComp.get(arrayIndex);
    }
    
    /**
     * getNumComp returns the size of the connected component
     * @return the number of board positions within a connected component
     */
    public int getNumComp(){
        return connectedComp.size();
    }                        
    
    /**
     * componentScore returns the value of the connected component
     * @return the value of the connected component (number of fruits squared)
     */
    public double componentScore(){
        return Math.pow((double) connectedComp.size(), 2.0);
    }

    @Override
    public int compareTo(Object other) {
        //Cast the other Object into an object of ConnectedComponent Type
        ConnectedComponent otherComponent = (ConnectedComponent) other;
        //Compare objects
        if(connectedComp.size() < otherComponent.getNumComp()){return -1;}
        else if(connectedComp.size() > otherComponent.getNumComp()){return 1;}
        else{return 0;}
    }
    
     //Function to visualize components of the Connected Component
    @Override
    public String toString(){
        String componentElements = "";
        for(int i = 0; i < connectedComp.size(); i++){
            componentElements += connectedComp.get(i).toString() + " ";
        }
        return componentElements;
    }
}

/**********************************************************
 * The boardPosition class stores a location on the board
 * allowing to compare locations and return location in the
 * format required by the problem (A1, A2, etc.)
 **********************************************************/
class BoardPosition{
    private final int row;
    private final int col;
    private final int fruitType;
    private static final int ROW_OFFSET = 1;
    
    /**
     * BoardPosition is the constructor creating a (fruit,i,j) location on the board
     * @param row the row
     * @param col the column
     * @param fruitType the fruit type, or '*', of the given board position
     */
    public BoardPosition(int fruitType, int row, int col){
        this.fruitType = fruitType;
        this.row = row;
        this.col = col;
    }
    
    /**
     * getRow returns the row for the given point
     * @return the row 
     */
    public int getRow(){
        return row;
    }
    
    /**
     * getCol returns the column for the given point
     * @return the column
     */
    public int getCol(){
        return col;
    }
    
    /**
     * getFruitType returns the type of fruit in the given Board Position
     * @return the type of fruit in the given Board Position
     */
    public int getFruitType(){
        return fruitType;
    }
    
    /**
     * equals returns true if the two BoardPositions being compared are the same fruit
     * @param other the board position we are comparing to
     * @return true if positions are the same fruit
     */
    @Override
    public boolean equals(Object other){
        //ensure other object is not null
        if(other == null){
            return false;
        }
        //Make sure you are comparing board positions
        if(!(other instanceof BoardPosition)){
            return false;
        }
        //Instantiate other as a Board
        BoardPosition otherPosition = (BoardPosition) other;
        return this.fruitType == otherPosition.fruitType;
    }
    
    /**
     * Returns the boardPosition in the required format
     * @return the board Position as a string
     */
    @Override
    public String toString(){
        //Array for retrieval of Character
        char[] intToChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        //Row count starts at 1
        int rowNumber = row + ROW_OFFSET;
        //Column Character
        char columnChar = intToChar[col];
        //return the board position in the desired format
        return Character.toString(columnChar) + rowNumber;
    }
}

/********************************************************************************
 * The homework class is the "main" class of the homework (the only public class)
 * It contains the main function
 ********************************************************************************/
public class homework {
    private static final double SEC_TO_MS = 1000;    
    private static final int EMPTY_CELL = -1;
    private static final int BLANK_CELL = -10;
    private static final double INFINITY = Double.POSITIVE_INFINITY;
    private static final double NEG_INFINITY = Double.NEGATIVE_INFINITY;
    private static final double BREAK_RUN = Double.NEGATIVE_INFINITY;
    private static final int GLOBAL_MAX_DEPTH = 8;
   //Variables to store index and value of best move
    private static int bestIndex = 0;
    private static double alphaBetaResult;
    //To count number of empty cells in board
    private static int numEmpty;
    private static boolean timeLeft = true;
    
    //TESTING VARIABLES************************
    private static int numNodes = 0;
    
    
    /**
     * generateNewState replaces all items of the connected component from the
     * selected board position, applies gravity, and returns a new node
     * @param currentNode the input Node
     * @param selectedCompIndex the index of the actions list to generate new node with
     * @return thew new Node after replacing selected cell, its Connected Comp. and applying gravity
     */
    public static Node generateNewNode(Node currentNode, int selectedCompIndex){
        //Instantiate new Node to null
        Node newNode = null;
        //Get Length of State
        int stateLength = currentNode.getState().length;
        //Get the player of the current node
        String player = currentNode.getPlayer();
        //Instantiate new Player
        String newPlayer;
        if(player.contentEquals("MAX")){
            newPlayer = "MIN";
        }
        else{
            newPlayer = "MAX";
        }
        //Get the list of Connected Components of the Node
        ArrayList<ConnectedComponent> actionsList = currentNode.getActionsList();
        //If you are at an empyt assignment (terminal node) return null
        if(actionsList.isEmpty()){
            return null;
        }
        else{
            //Get the selected component (actions list in order of size)
            ConnectedComponent selectedComponent = actionsList.get(selectedCompIndex);
            //Create boolean array to track number of removed cells per column
            boolean[] removedInCol = new boolean[stateLength];
            //Generate new State for new node
            int[][] newState = new int[stateLength][stateLength];
            //Populate state with state of current Node
            for(int i = 0; i < stateLength; i++){
                for(int j = 0; j < stateLength; j++){
                    newState[i][j] = currentNode.getState()[i][j];
                }
            }
            
            /*********************REMOVE CONNECTED COMPONENT*******************/
            //Remove the cells from the selected component from the new State
            for(int i = 0; i < selectedComponent.getNumComp(); i++){
                newState[selectedComponent.getBoardPosition(i).getRow()][selectedComponent.getBoardPosition(i).getCol()] = BLANK_CELL;
                //Set column to true in boolean array
                removedInCol[selectedComponent.getBoardPosition(i).getCol()] = true;
            }
            /*********************APPLY GRAVITY TO STATE*******************/
            //Loop until you find a column with a removed Board Position
            for(int col = 0; col < stateLength; col++){
                //If the column contains removed cells, apply gravity
                if(removedInCol[col]){
                    //Loop from the bottom of the array to the top
                    for(int row = stateLength - 1; row >= 0; row--){
                        //Find the first removed cell
                        if(newState[row][col] == BLANK_CELL){
                            //Local variable for number of cells above
                            int numbAbove = 0;
                            for(int rowCount = row; rowCount >= 0; rowCount--){
                                //Check if we
                                if(newState[rowCount][col] == BLANK_CELL){
                                    numbAbove++;
                                }
                                else{ break;}
                            }
                            //Use the number of blank cells to apply required "gravity"
                            for(int blankCell = row; blankCell >=0; blankCell--){
                                //If there are blank cells all the way to the top, change all to '*'
                                if(blankCell - numbAbove < 0){
                                    newState[blankCell][col] = EMPTY_CELL;        
                                }
                                else{
                                    newState[blankCell][col] = newState[blankCell - numbAbove][col];
                                }
                            }
                        }
                    }
                }
            }
            /*********************CALCULATE VALUE AND GENERATE NODES*******************/
            //Get selected component Score and initialize other value parameters
             double currCompValue = selectedComponent.componentScore();
             //Instantiate the value of the node (for type of player)
             double totalValue;
            //If the newPlayer is MIN, add value of component removed at prior depth to MAX's running sum
            if(newPlayer.contentEquals("MIN")){
               //Generate new value for new Node given value of selected component
               totalValue = currentNode.getMaxValue() + currCompValue;
               //Update new Max Node
               newNode = new Node(newState, currentNode.getDepth() + 1, totalValue, currentNode.getMinValue(), newPlayer);
            }
            //If the newPlayer is MAX, add value of component removed at prior depth to MIN's running sum
            else{
                //Generate new value for new Node
                totalValue = currentNode.getMinValue() + currCompValue;
                //Update new Min Node
                newNode = new Node(newState, currentNode.getDepth() + 1, currentNode.getMaxValue(), totalValue, newPlayer);
            }
        }
        return newNode;
    }
    
    /*********************ALPHA-BETA PRUNNING*******************/

    /**
     * alphaBeta returns the minimax value and updates the index of the chosen solution
     * @param currNode the node on which we are running alpha-beta "prunning"
     * @param alpha the value of alpha
     * @param beta the value of beta
     * @param maxDepth the maximum depth of search
     * @param timeLimit the time limit
     * @return the alpha beta value. Also updates the bestIndex variable to generate output node
     */
    public static double alphaBeta(Node currNode, double alpha, double beta, int maxDepth, double timeLimit){
        //Check if you have run out of time, return neg_infinity
        double currentTime = System.currentTimeMillis();
        if(currentTime >= timeLimit){
            return BREAK_RUN;
        }
        //Test for the cutoff requirements
        if(cutOffSearch(currNode, maxDepth)){
            //Return the terminal value of the node
            return currNode.getTerminalValue();
        }
        //Get the current player from the Node
        String player = currNode.getPlayer();
        //If player is MAX, set v <- -INF, recurse through children
        if(player.contentEquals("MAX")){
            //Instantiate v to negative infinity
            double value = NEG_INFINITY;
            //Get actions fro current Node
            ArrayList<ConnectedComponent> actionsList = currNode.getActionsList();
            //Loop through actions list
            for(int i = 0; i < actionsList.size(); i++){
                //Generate new node
                Node newNode = generateNewNode(currNode, i);
                
                //TEST***********************************************
                numNodes++;

                //Set value to recurse on alphaBeta
                value = alphaBeta(newNode, alpha, beta, maxDepth, timeLimit);
                //Check if you have run out of time, return neg_infinity
                if(value == BREAK_RUN){
                    return BREAK_RUN;
                }
                if(value > alpha){
                    alpha = value;
                    //Save the value of the index at depth = 0
                    if(currNode.getDepth() == 0){
                        //Save the index of the best solution (at depth = 0)
                        bestIndex = i;
                    }
                }
                //Check if prunning possible
                if(alpha >= beta){
                    break;
                }
            }
            //return the value
            return alpha;
        }
        //If player is MIN, set v <- INF, recurse through children
        else{
            //Instantiate v to infinity
            double value = INFINITY;
            //Get actions fro current Node
            ArrayList<ConnectedComponent> actionsList = currNode.getActionsList();
            //Loop through actions list
            for(int i = 0; i < actionsList.size(); i++){
                //Generate new node
                Node newNode = generateNewNode(currNode, i);
                
                //TEST***********************************************
                numNodes++;
                
                //Set value to recurse on alphaBeta
                value = alphaBeta(newNode, alpha, beta, maxDepth, timeLimit);
                //Check if you have run out of time, and break the run
                if(value == BREAK_RUN){
                    return BREAK_RUN;
                }
                if(value < beta){
                    //Set beta to value (since value < beta)
                    beta = value;
                    //Save the value of the index at depth = 0
                    if(currNode.getDepth() == 0){
                        //Save the index of the best solution (at depth = 0)
                        bestIndex = i;
                    }
                }
                //Check if prunning possible
                if(alpha >= beta){ 
                    break;
                }
            }
            //return the value
            return beta;
        }
    }
    
    /**
     * cutOffSearch tests whether the current node meets the termination requirements
     * @param newNode the node being tested
     * @param maxDepth the maximum depth of search
     * @return true iff we are at terminating conditions (leaf node or max depth)
     */
    public static boolean cutOffSearch(Node newNode, int maxDepth){
        return ((newNode.getActionsList().isEmpty()) || (newNode.getDepth() >= maxDepth));
    }
    
    /**
     * printNode is a test method to print Nodes and other relevant information
     * @param selectedMove 
     * @param node 
     */
    public static void printNode(ConnectedComponent selectedMove, Node node){
        //Print selected Move (BoardPosition.toString())
        if(selectedMove != null){System.out.println("The selected Component: " + selectedMove.toString());}
            char[] intToChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            for(int i = 0; i < node.getState().length; i++){
                System.out.print(intToChar[i]);
            }
            System.out.println();
            //Print Board after the move and applying gravity
            for(int i = 0; i < node.getState().length; i ++){
                for(int j = 0; j < node.getState().length; j++){
                    if(node.getState()[i][j] == EMPTY_CELL){
                        System.out.print("*");
                    }
                    else{
                        System.out.print(node.getState()[i][j]);
                    }
                }
                //Skip to next line of the board
                System.out.println();
            }
            System.out.println("The player type of the node is: " + node.getPlayer());
            System.out.println("The depth of this node is: " + node.getDepth());
            System.out.println("The number of Connected Components is: " + node.getActionsList().size());
            System.out.println("The actions list of this Node is: " + node.getActionsList());
            System.out.println("The Max Value of the Node is:" + node.getMaxValue());
            System.out.println("The Min Value of the Node is:" + node.getMinValue());
            System.out.println("The Terminal Value of the Node is:" + node.getTerminalValue());
            System.out.println("The Potential Max Value of the Node is:" + node.getMaxPotential());
            System.out.println("The Potential Min Value of the Node is:" + node.getMinPotential());
            System.out.println();
    }
    
    /**
     * printOutput prints the  output into an output file in the requested format
     * @param selectedMove the board cell selected by the AI
     * @param newBoard the board after the selected cell and its connected component
     * has been removed from the board and gravity has been applied
     */
    public static void printOutput(String selectedMove, int[][] newBoard){
        //Print selected Move (BoardPosition.toString())
        System.out.println(selectedMove);
        //Print Board after the move and applying gravity
        for(int i = 0; i < newBoard.length; i ++){
            for(int j = 0; j < newBoard.length; j++){
                if(newBoard[i][j] == EMPTY_CELL){
                    System.out.print("*");
                }
                else{
                    System.out.print(newBoard[i][j]);
                }
            }
            //Skip to next line of the board
            System.out.println();
        }
    }
    
    /**
     * printOutFile prints the  output into an output file in the requested format
     * @param selectedMove the board cell selected by the AI
     * @param newBoard the board after the selected cell and its connected component
     * has been removed from the board and gravity has been applied
     */
    public static void printOutFile(String selectedMove, int[][] newBoard){
        PrintStream outputFileStream = null;
        try {
            outputFileStream = new PrintStream( new FileOutputStream("output.txt"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(homework.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Print selected Move (BoardPosition.toString())
        outputFileStream.println(selectedMove);
        //Print Board after the move and applying gravity
        for(int i = 0; i < newBoard.length; i ++){
            for(int j = 0; j < newBoard.length; j++){
                if(newBoard[i][j] == EMPTY_CELL){
                    outputFileStream.print("*");
                }
                else{
                    outputFileStream.print(newBoard[i][j]);
                }
            }
            //Skip to next line of the board
            outputFileStream.println();
        }
    }
    
    /**
     * timeAllocation generates the allocated time for the current configuration
     * @param fruitNumArray
     * @param boardSize the size of the board
     * @param remainingTime the remaining time
     * @param numEmpty the number of empty cells
     * @param numComponents the number of components (for branching factor approximation)
     * @return the allocated amount of seconds for the current run
     */
    public static double timeAllocation(int[] fruitNumArray, int boardSize, double remainingTime, int numEmpty, int numComponents){
        //Calculate the average amount of fruits per fruit type
        double allocatedTime;
        double mean;
        double variance;
        double stdev;
        double tempVariance = 0.0;
        double sum = 0.0;
        int fruitCount = 0;
        double coeffOfVar;
        //Calculate the sum
        for(int i = 0; i < fruitNumArray.length; i++){
            sum+= fruitNumArray[i];
            if(fruitNumArray[i] != 0){
                fruitCount++;
            }
        }
        //calculate the mean
        mean = (double) sum / fruitCount;
        //Calculate the variance
        for(int i = 0; i < fruitNumArray.length; i++){
            if(fruitNumArray[i] != 0){
                tempVariance+= Math.pow(fruitNumArray[i] - mean, 2);
            }
        }
        //Calculate Variance
        variance = tempVariance / fruitCount;
        //Calculate the stdev
        stdev = Math.sqrt(variance);
        //Calculate the coefficient of variance
        coeffOfVar = stdev / mean;
        //Calculate the number of remaining components for max palyer
        double numCompMax = (numComponents / 2.0) + 1.0;
        //Calculate the multiplier 
        double multiplier = (1.0 + coeffOfVar);
        //Calculate the allocated time and return
        allocatedTime =  remainingTime / (numCompMax * multiplier);   
        //Calculate the ratio between the allocated time and remaining time, return minimum 3%
        if(allocatedTime / remainingTime < 0.03){
            allocatedTime = remainingTime * 0.03;
        }
        return allocatedTime;
    }
    
    public static void main(String[] args) {
        //Initialize number of empty cells to zero
        numEmpty = 0;
        //Get the Start Time (ms) for speed calculation
        double startTime = System.currentTimeMillis();
        //Read input file from current directory                   
        //Store file and pass to new Scanner object for reading
        File inputFile = new File("input.txt");
        
        try {
            //Instantiate Scanner and pass the input file
            Scanner in = new Scanner(inputFile);
            //Read first line (width and height) of the of the square board (<= 26)
            int boardSize = in.nextInt();
            //Read the second line of the board, the number of fruit types (<=9)
            int fruitTypes = in.nextInt();
            //array to store number of each type of fruit
            int[] fruitNumArray = new int[10];
            //Read the third line of the board, the remaining time in seconds
            double remainingTime = in.nextDouble();
            //Consume line to start looping through array
            in.nextLine();
            //Instantiate a new 2D array to store board configuration
            int[][] inputBoard = new int[boardSize][boardSize];
            //Read file into inputBoard array
            for(int i = 0; i < boardSize; i++){
                //Read line by line and store data in array
                String nextRow = in.nextLine();
                //Read each column value, convert character to int
                for(int j = 0; j < boardSize; j++){
                    if(nextRow.charAt(j) == '*'){
                        //increment the number of empty cells
                        numEmpty++;
                        inputBoard[i][j] = EMPTY_CELL;
                    }
                    else{
                        inputBoard[i][j] = Character.getNumericValue(nextRow.charAt(j));
                        fruitNumArray[inputBoard[i][j]]++;
                    }
                }
            }
            //Generate root node, set to MAX
            Node newNode = new Node(inputBoard,0,0,0, "MAX");
            //Time allocation calculations 
            double allocatedTime = timeAllocation(fruitNumArray, boardSize, remainingTime, numEmpty, newNode.getNumChildren()) * SEC_TO_MS;
            //Calculate total time
            double totalTime = startTime + allocatedTime;
            //Set initial maximum depth to 0
            int maxDepth = 0;
            //Previous best index
            int previousBest = 0;
            //If only one action possible, return that action
            if(newNode.getActionsList().size() == 1){
                bestIndex = 0;
            }
            /***********************ITERATIVE DEEPENING***********************/
            else{
                while(timeLeft && maxDepth < GLOBAL_MAX_DEPTH){
                    //Increment the maximum depth
                    maxDepth++;
                    //check if time allocated is over
                    double currentTime = System.currentTimeMillis();
                    if(currentTime >= totalTime){
                        timeLeft = false;
                        break;
                    }
                    //Apply Alpha-Beta Prunning to the node, get index of best action
                    alphaBetaResult = alphaBeta(newNode,NEG_INFINITY,INFINITY, maxDepth, totalTime);
                    //If the result is negative infinity, return the best index of the previous iteration
                    if(alphaBetaResult == NEG_INFINITY){
                        bestIndex = previousBest;
                        maxDepth--;
                        break;
                    }
                    else{
                        //Store the value of the best index in current iteration
                        previousBest = bestIndex;
                    }
                }
            }
            //Generate node with given result index
            Node bestNode = generateNewNode(newNode, bestIndex);
            //Get Board Position within selected Connected Component of best action (first element, for example)
            BoardPosition selectedBP = newNode.getActionsList().get(bestIndex).getBoardPosition(0);
            //print result
            printOutput(selectedBP.toString(), bestNode.getState());
            //print to output file in current directory
            printOutFile(selectedBP.toString(), bestNode.getState());
   
            
            //TEST******************************************************
            System.out.println("The max depth reached is: " + maxDepth);
            System.out.println("The number of generated nodes: " + numNodes);

            double endTime = System.currentTimeMillis();
            
            System.out.println("Total Time spent: " + (endTime - startTime) + " ms");
            System.out.println("Nodes/sec: " + numNodes / ((endTime - startTime) / 1000));
        } 
        
        catch (FileNotFoundException ex) {
            Logger.getLogger(homework.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
