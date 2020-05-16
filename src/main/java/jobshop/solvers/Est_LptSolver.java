package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class Est_LptSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {
        Task init[] = new Task[instance.numJobs]; // Tableau pour Initialisation
        int duree[] = new int[instance.numJobs]; //on calcule la somme des temps des taches executés pour chaque job
        int machine[] = new int[instance.numMachines];
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
            aux = Lpt(instance,realisables, duree, machine);
            resource.tasksByMachine[instance.machine(aux.job,aux.task)][resource.nextFreeSlot[instance.machine(aux.job,aux.task)]] = aux;
            resource.nextFreeSlot[instance.machine(aux.job,aux.task)]++;
            //verifier si la tache à un suivant
            if(aux.task+1<instance.numTasks){
                //créer nouvelle tache
                realisables.add(new Task(aux.job,(aux.task+1)));
            }
            realisables.remove(aux);
            machine[instance.machine(aux)] =  Math.max(machine[instance.machine(aux)], duree[aux.job])+instance.duration(aux);
            duree[aux.job] = machine[instance.machine(aux)];

            nbTachesTotal--;
        }
        return new Result(instance, resource.toSchedule(), Result.ExitCause.Blocked);
    }


    public static Task Lpt(Instance instance, ArrayList<Task> taches, int[] duree, int[] machine){
        Task aux = taches.get(0);
        for(int j=1; j< taches.size();j++){
            if(Math.max(duree[taches.get(j).job],machine[instance.machine(taches.get(j))] ) < Math.max(duree[aux.job],machine[instance.machine(aux)])){
                aux = taches.get(j);
            }
            else if(Math.max(duree[taches.get(j).job],machine[instance.machine(taches.get(j))] ) == Math.max(duree[aux.job],machine[instance.machine(aux)])){
                if(instance.duration(aux)< instance.duration(taches.get(j))){
                    aux = taches.get(j);
                }
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
