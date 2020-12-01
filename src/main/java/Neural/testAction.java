package Neural;

import org.deeplearning4j.rl4j.space.DiscreteSpace;

public class testAction extends DiscreteSpace {
    public testAction(int size) {
        super(size);
    }
    @Override
    public Object encode(Integer a) {
        System.out.println("a: "+a);
        return a;
    }
}
