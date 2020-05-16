package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

//SPT (Shortest Processing Time) : donne priorité à la tâche la plus courte ;

public class SptSolver  implements Solver {
    @Override
    //SPT (Shortest Processing Time) : donne priorité à la tâche la plus courte ;
    public Result solve(Instance instance, long deadline) {
        Task init[] = new Task[instance.numJobs]; // Tableau pour Initialisation
        ArrayList<Task> realisables = new ArrayList<Task>();
        int nbTachesTotal = instance.numJobs*instance.numTasks;
        Schedule sched;
        ResourceOrder resource = new ResourceOrder(instance);
        // Initialisation
        Initialisation(instance, realisables);

        //Boucle: tant qu’il y a des tâches réalisables

        while(nbTachesTotal >0){

            Task aux;
            //placer cette tâche sur la ressource qu’elle demande
            aux = Spt(instance,realisables);
            resource.tasksByMachine[instance.machine(aux.job,aux.task)][resource.nextFreeSlot[instance.machine(aux.job,aux.task)]] = aux;
            resource.nextFreeSlot[instance.machine(aux.job,aux.task)]++;

            //verifier si la tache à un suivant
            if(aux.task+1<instance.numTasks){
                //créer nouvelle tache
                realisables.add(new Task(aux.job,(aux.task+1)));
            }
            realisables.remove(aux);
            nbTachesTotal--;
        }
        return new Result(instance, resource.toSchedule(), Result.ExitCause.Blocked);
    }

    public static Task Spt(Instance inst, ArrayList<Task> real){
        Task aux = real.get(0);
        for(int j=0; j< real.size();j++){
            if(inst.duration(aux)> inst.duration(real.get(j))){
                aux = real.get(j);
            }
        }
        return aux;
    }

    public static void Initialisation(Instance instance, ArrayList<Task> realisables) {
        for (int j = 0; j < instance.numJobs; j++) {
            realisables.add(new Task(j,0));
        }
    }
}


