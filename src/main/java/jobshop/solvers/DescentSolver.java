package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    public static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }

        @Override
        public String toString() {
            return "(" + String.valueOf(this.firstTask) + " , " + String.valueOf(this.lastTask) + " ) ";
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task aux = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = aux;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        //Initialisation -- générer une solution réalisable avec la méthodede votre choix;
        Est_LrptSolver solution = new Est_LrptSolver();
        Result resultLRPT = solution.solve(instance, deadline);
        ResourceOrder orderStar = new ResourceOrder(resultLRPT.schedule);
        ResourceOrder order = orderStar.copy();
        ResourceOrder aux;
        List<Block> blockList;
        List<Swap> voisins ;
        boolean modif = true;

        //Boucle -- Exploration des voisinages successif
        while((modif ==true) && (deadline - System.currentTimeMillis())>1){
            blockList = blocksOfCriticalPath(order);
            //System.out.println(blockList.toString());
            for(int i=0; i<blockList.size(); i++){ // pour chaque bloc on récupère ses voisins
                voisins = neighbors(blockList.get(i));

                for(int j=0; j<voisins.size();j++){
                    aux = order.copy();
                    voisins.get(j).applyOn(aux);
                    if(aux.toSchedule().makespan() < order.toSchedule().makespan()){
                        order = aux;
                    }
                }
            }
            if(order.equals(orderStar)){
                modif = false;
            }
            else{
                modif = true;
                orderStar = order;
            }
        }

        return new Result(instance, orderStar.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    public static List<Block> blocksOfCriticalPath(ResourceOrder order) {
        Instance inst = order.instance;
        List<Task> listB = order.toSchedule().criticalPath();
        ArrayList<Block> blocs = new ArrayList<Block>();

        for(int u = 0; u<listB.size(); u++){
            if((blocs.size()>0) && (blocs.get(blocs.size()-1).machine == inst.machine(listB.get(u).job, listB.get(u).task))){
                int first = blocs.get(blocs.size()-1).firstTask;
                Block bloc = new Block(inst.machine(listB.get(u)),first, Indice(order, listB.get(u)));
                blocs.remove(blocs.size()-1);
                blocs.add(bloc);
            }
            else{
                Block bloc = new Block(inst.machine(listB.get(u)),Indice(order,listB.get(u)), Indice(order, listB.get(u)));
                blocs.add(bloc);
            }
        }

        return blocs;
    }

    public static int Indice(ResourceOrder order, Task t){
        int resultat = 0;
        for(int i=0; i<order.instance.numJobs; i++){
            if(order.tasksByMachine[order.instance.machine(t.job, t.task)][i].equals(t) ){
                resultat = i;
            }
        }
        return resultat;
    }
    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> swp = new ArrayList<Swap>();
        if((block.lastTask - block.firstTask)>1){
            swp.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
            swp.add(new Swap(block.machine, block.lastTask-1, block.lastTask));
        }
        else{
            swp.add(new Swap(block.machine, block.firstTask, block.firstTask));
        }
        return swp;
    }

}