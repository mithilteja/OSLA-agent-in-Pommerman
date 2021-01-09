package players;

import core.GameState;
import players.heuristics.CustomHeuristic;
import players.heuristics.StateHeuristic;
import utils.Types;
import java.util.ArrayList;
import java.util.Random;


public class DLSPlayer extends Player {
    public long Time = 0;
    public long averageTime = 0;
    public long numactions = 0;
    public int depth = 4;
    public double[] totals = new double[6];
    public GameState gscopy[] = new GameState[100];
    private Random rand;
    private StateHeuristic rootStateHeuristic;

    public DLSPlayer(long seed, int id) {
        super(seed, id);
        rand = new Random(seed);
    }

    @Override
    public Types.ACTIONS act(GameState gs) {
        /** To get the Start and End Timing of ac function, use the code in the line 29 and line 58 to 62 */
//     long startTime = System.currentTimeMillis();

        rootStateHeuristic = new CustomHeuristic(gs);
        ArrayList<Types.ACTIONS> actionsList = Types.ACTIONS.all();
        double maxQ = Double.NEGATIVE_INFINITY;
        Types.ACTIONS bestAction = null; //it is used to store the best action by creating a variable
        int inAction = 0;

        for (int a = 0; a < 6; a++) {
            totals[a] = 0;
        }
        for (Types.ACTIONS action : actionsList) {
            gscopy[0] = gs.copy();  // It is used to copy the game state
            int curDepth = 0;
            nextState(gscopy[0], action);
            double evalveState = rootStateHeuristic.evaluateState(gscopy[0]);

            /** In the line 47, it is used to explore the branches in the Decision tree */
            nodeSearch(gscopy[0], curDepth, inAction);

            double D = evalveState;
            D += totals[inAction];
            if (D > maxQ) {
                maxQ = D;
                bestAction = action;
            }
            inAction += 1;
        }

 /*       long endTime = System.currentTimeMillis();
        System.out.println("It has taken" + (endTime - startTime) + "milliSeconds");
        Time += (endTime - startTime);
        numactions += 1;
        averageTime = Time/numactions;
*/
        // Line 75 ruturns the best action with highest score
        return bestAction;
    }

    public void nodeSearch(GameState state, int curDepth, int inAction) {
        ArrayList<Types.ACTIONS> actList = Types.ACTIONS.all();
        curDepth += 1;
        int iteration = curDepth;
        if (curDepth == depth) {
            return;
        }
        for (Types.ACTIONS act : actList) {
            /** From line 78 to 80, it makes a copy of current game state, simulates game with given action
             * and recursion to explore branch further in the decision tree */
            gscopy[iteration] = state.copy();
            nextState(gscopy[iteration], act);
            nodeSearch(gscopy[iteration], curDepth, inAction);
            /** To power the current depth, add the heuristic score of the currnt game state
             * and give it a weight in the line 83*/
            totals[inAction] += (rootStateHeuristic.evaluateState(gscopy[iteration]) / Math.pow(0.1, (iteration + 1)));
        }
    }

    @Override
    public int[] getMessage() {
        return new int[Types.MESSAGE_LENGTH];
    }

    @Override
    public Player copy() {
        return new DLSPlayer(seed, playerID);
    }

    private void nextState(GameState gs, Types.ACTIONS act) {
        Types.TILETYPE[][] board = gs.getBoard();
        ArrayList<Types.TILETYPE> enmObs = gs.getAliveEnemyIDs();
        int boardSizeX = board.length;
        int boardSizeY = board[0].length;
        int[][] playerPositions = new int[4][3];

        for (int x = 0; x < boardSizeX; x++) {
            for (int y = 0; y < boardSizeY; y++) {
                Types.TILETYPE type = board[y][x];

                if (Types.TILETYPE.getAgentTypes().contains(type) && type.getKey() != gs.getPlayerId()) ;
                {
                    if (enmObs.contains(type)) {
                        playerPositions[type.getKey() - 10][0] = x;
                        playerPositions[type.getKey() - 10][1] = y;
                    }
                }
            }
            for (int a = 1; a < 4; a++) {
                int Xdist = Math.abs(playerPositions[a][0] - playerPositions[0][0]);
                int Ydist = Math.abs(playerPositions[a][1] - playerPositions[0][1]);

                if (Xdist == 0) {
                    if (Ydist < 3) {
                        playerPositions[a][2] = 1;
                    }
                }
                if (Ydist == 0) {
                    if (Xdist < 3) {
                        playerPositions[a][2] = 1;
                    }
                }
            }
            int xPlayers = 4;
            Types.ACTIONS[] actionsAll = new Types.ACTIONS[4];
            for (int a = 0; a < xPlayers; ++a) {
                if (a == getPlayerID() - Types.TILETYPE.AGENT0.getKey()) {
                    actionsAll[a] = act;
                } else {
                    if (playerPositions[a][2] == 1) {
                        actionsAll[a] = Types.ACTIONS.all().get(5);
                        playerPositions[a][2] = 0;
                    } else {
                        int actIDx = rand.nextInt(gs.nActions());
                        actionsAll[a] = Types.ACTIONS.all().get(actIDx);
                    }
                }
            }
            gs.next(actionsAll);
        }
    }
}
