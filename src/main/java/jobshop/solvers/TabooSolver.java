package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import sun.security.krb5.internal.crypto.Des;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

public class TabooSolver implements Solver {


    public static class Block {

        final int machine;
        final int firstTask;
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

    static class Swap {
        final int machine;
        final int t1;
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        public void applyOn(ResourceOrder order) {
            Task aux = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = aux;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {
        Map<Swap,Integer> tabou = new HashMap<Swap, Integer>(); // memorise les couples de taches tabou et leurs durée
        int dureeTabou = 10;
        int maxIter = 100;
        //Est_LrptSolver solInit = new Est_LrptSolver();
        //Result resultLRPT = solInit.solve(instance, deadline);
        DescentSolver solInit = new DescentSolver();
        Result resultDescent = solInit.solve(instance, deadline);

        ResourceOrder orderCourant = new ResourceOrder(resultDescent.schedule); // Courante
        //ResourceOrder orderCourant = new ResourceOrder(resultLRPT.schedule); // Courante
        ResourceOrder orderStar = orderCourant.copy(); // Meilleure
        ResourceOrder aux ; //auxilliaire

        int k =0; // compte les iterations
        List<TabooSolver.Block> blockList;
        List<TabooSolver.Swap> voisins ;
        Swap auxSwap;
        int meilleur;


        //Boucle -- Exploration des voisinages successif
        while((deadline - System.currentTimeMillis())>1 && k<maxIter){
            k+=1;
            blockList = blocksOfCriticalPath(orderCourant);

            for(int i=0; i<blockList.size(); i++){ // pour chaque bloc on récupère ses voisins
                voisins = neighbors(blockList.get(i));

                for(int j=0; j<voisins.size();j++){
                    aux = orderCourant.copy();
                    if(Tabou(tabou,voisins.get(j))==false){
                        voisins.get(j).applyOn(aux);
                        orderCourant = aux;
                        tabou.put(voisins.get(j),(k+dureeTabou));
                        auxSwap = new Swap(voisins.get(j).machine,voisins.get(j).t2,voisins.get(j).t1);
                        tabou.put(auxSwap,(k+dureeTabou));
                        tabou.remove(k);

                        if(orderCourant.toSchedule().makespan() < orderStar.toSchedule().makespan()){
                            orderStar = orderCourant.copy();
                        }
                    }
                }
            }
        }

        return new Result(instance, orderStar.toSchedule(), Result.ExitCause.Blocked);
    }


    public static boolean Tabou( Map<Swap,Integer> tabou, Swap swp){ // renvoi true si la solution est tabou et false sinon
        Swap aux = new Swap(swp.machine, swp.t2,swp.t1);
        boolean retour = false;
        if(tabou.containsKey(swp) || tabou.containsKey(aux)){
            retour = true;
        }
        return retour;
    }

    
    public static List<TabooSolver.Block> blocksOfCriticalPath(ResourceOrder order) {
        Instance inst = order.instance;
        List<Task> listB = order.toSchedule().criticalPath();
        ArrayList<TabooSolver.Block> blocs = new ArrayList<TabooSolver.Block>();

        for (int u = 0; u < listB.size(); u++) {
            if ((blocs.size() > 0) && (blocs.get(blocs.size() - 1).machine == inst.machine(listB.get(u).job, listB.get(u).task))) {
                int first = blocs.get(blocs.size() - 1).firstTask;
                TabooSolver.Block bloc = new TabooSolver.Block(inst.machine(listB.get(u)), first, Indice(order, listB.get(u)));
                blocs.remove(blocs.size() - 1);
                blocs.add(bloc);
            }
            else {
                TabooSolver.Block bloc = new TabooSolver.Block(inst.machine(listB.get(u)), Indice(order, listB.get(u)), Indice(order, listB.get(u)));
                blocs.add(bloc);
            }
        }

        return blocs;
    }


    public static int Indice(ResourceOrder order, Task t) {
        int resultat = 0;
        for (int i = 0; i < order.instance.numJobs; i++) {
            if (order.tasksByMachine[order.instance.machine(t.job, t.task)][i].equals(t)) {
                resultat = i;
            }
        }
        return resultat;
    }

    List<TabooSolver.Swap> neighbors(TabooSolver.Block block) {
        List<TabooSolver.Swap> swp = new ArrayList<TabooSolver.Swap>();
        if ((block.lastTask - block.firstTask) > 1) {
            swp.add(new TabooSolver.Swap(block.machine, block.firstTask, block.firstTask + 1));
            swp.add(new TabooSolver.Swap(block.machine, block.lastTask - 1, block.lastTask));
        } else {
            swp.add(new TabooSolver.Swap(block.machine, block.firstTask, block.firstTask));
        }
        return swp;
    }
}
