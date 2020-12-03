package Neural;

import game.GameBoard;
import game.Graph;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.mdp.gym.GymEnv;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.RmsProp;



import java.io.IOException;

public class testNeural {
    public static void main(String args[]) throws IOException, InterruptedException {
        GameBoard r = new GameBoard(3,3);
        for(int w=0;w<10;w++) {
            DQNPolicy<testState> t = dots();
            t.save("QLearning.bin");
            System.out.println("SAVED: Turn "+(w+1));
        }
        System.out.println("DONE");
    }

    private static DQNPolicy<testState> dots() throws IOException {

        QLearningConfiguration DOTS_QL = QLearningConfiguration.builder()
                .seed(1L)                //Random seed (for reproducability)
                .maxEpochStep(500)        // Max step By epoch
                .maxStep(1000*50)           // Max step
                .expRepMaxSize(4000)    // Max size of experience replay
                .batchSize(6)            // size of batches
                .targetDqnUpdateFreq(2000) // target update (hard)
                .updateStart(0)          // num step noop warmup
                .rewardFactor(0.01)       // reward scaling
                .gamma(0.95)              // gamma
                .errorClamp(1.0)          // /td-error clipping
                .minEpsilon(0.1f)         // min epsilon
                .epsilonNbStep(1000)      // num step for eps greedy anneal
                .doubleDQN(true)          // double DQN
                .build();

        /*
        DQNDenseNetworkConfiguration DOTS_NET =
                DQNDenseNetworkConfiguration.builder()
                        .l2(0)
                        .updater(new Adam(0.005))
                        .numHiddenNodes(36)
                        .numLayers(3)
                        .build();
         */



        DQNPolicy p = DQNPolicy.load("QLearning.bin");
        IDQN DOTS_NET = p.getNeuralNet();


        // The neural network used by the agent. Note that there is no need to specify the number of inputs/outputs.
        // These will be read from the gym environment at the start of training.

        MDP<testState,Integer,DiscreteSpace> env = new testEnv();
        QLearningDiscreteDense<testState> dql = new QLearningDiscreteDense<testState>(env, DOTS_NET, DOTS_QL);
        System.out.println(dql.toString());
        dql.train();
        return dql.getPolicy();
    }
}
