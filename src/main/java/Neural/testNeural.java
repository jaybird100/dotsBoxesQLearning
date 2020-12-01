package Neural;

import game.GameBoard;
import game.Graph;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.gym.GymEnv;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
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
        DQNPolicy<testState> t = dots();
    }

    private static DQNPolicy<testState> dots() throws IOException {
        QLearningConfiguration DOTS_QL = QLearningConfiguration.builder()
                .seed(Long.valueOf(14546))                //Random seed (for reproducability)
                .maxEpochStep(500)        // Max step By epoch
                .maxStep(1000)           // Max step
                .expRepMaxSize(1500)    // Max size of experience replay
                .batchSize(2)            // size of batches
                .targetDqnUpdateFreq(100) // target update (hard)
                .updateStart(0)          // num step noop warmup
                .rewardFactor(0.1)       // reward scaling
                .gamma(0.95)              // gamma
                .errorClamp(1.0)          // /td-error clipping
                .minEpsilon(1f)         // min epsilon
                .epsilonNbStep(10000)      // num step for eps greedy anneal
                .doubleDQN(false)          // double DQN
                .build();
        DQNDenseNetworkConfiguration DOTS_NET =
                DQNDenseNetworkConfiguration.builder()
                        .l2(2)
                        .updater(new Adam(0.01))
                        .numHiddenNodes(60)
                        .numLayers(5)
                        .build();


        // The neural network used by the agent. Note that there is no need to specify the number of inputs/outputs.
        // These will be read from the gym environment at the start of training.

        testEnv env = new testEnv();
        QLearningDiscreteDense<testState> dql = new QLearningDiscreteDense<testState>(env, DOTS_NET, DOTS_QL);
        System.out.println(dql.toString());
        dql.train();
        return dql.getPolicy();
    }
}
