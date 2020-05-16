package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.DescentSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class DebuggingMain {

    public static void main(String[] args) {
        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
            //ResourceOrder order = new ResourceOrder();
            //System.out.println("order \n" +order.toString());
            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            //JobNumbers enc1 = new JobNumbers(testFT06);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("machines : " + instance.numMachines);
            System.out.println("job : " + instance.numJobs);

            //System.out.println("\nENCODING: " + enc);
            //System.out.println("\nmachines1 : " + testFT06.numMachines);
            //System.out.println("\njob1 : " + testFT06.numJobs);
            //System.out.println("\nduration1 : " + testFT06.durations.toString());

            Schedule sched = enc.toSchedule();
            // TEST GREEDY
            //System.out.println("GREEDY " + grd.Gsolver(instance));
            //System.out.println("yyy"+sched.criticalPath());
            //
            ResourceOrder order2 = new ResourceOrder(sched);

            List<DescentSolver.Block> bloc = DescentSolver.blocksOfCriticalPath(order2);
            System.out.println("SIZE  = "+ bloc.size());
            System.out.println(bloc.toString());
            System.out.println("order2 \n" + order2.toString());
            // TODO: make it print something meaningful
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());
            System.out.println("NUMJOB LIST: " + enc.toString());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}